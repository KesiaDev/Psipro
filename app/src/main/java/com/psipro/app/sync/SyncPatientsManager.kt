package com.psipro.app.sync

import android.util.Log
import com.psipro.app.data.dao.PatientDao
import com.psipro.app.data.entities.Patient
import com.psipro.app.sync.api.BackendApiService
import com.psipro.app.sync.api.RemotePatient
import com.psipro.app.sync.api.SyncPatientPayload
import com.psipro.app.sync.api.SyncPatientsRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncPatientsManager @Inject constructor(
    private val api: BackendApiService,
    private val auth: BackendAuthManager,
    private val store: BackendSessionStore,
    private val patientDao: PatientDao
) {
    suspend fun sync(reason: String) {
        withContext(Dispatchers.IO) {
            try {
                if (!auth.isBackendAuthenticated()) {
                    Log.w(TAG, "SYNC_SKIP_NO_BACKEND_TOKEN reason=$reason")
                    return@withContext
                }

                val clinicId = auth.ensureClinicId()
                if (clinicId.isNullOrBlank()) {
                    Log.w(TAG, "SYNC_SKIP_NO_CLINIC reason=$reason")
                    return@withContext
                }

                Log.i(TAG, "SYNC_PATIENTS_START reason=$reason clinicId=$clinicId")

                // 1) Push: enviar pacientes locais "dirty"
                val dirty = ensureUuidsPersisted(patientDao.getDirtyPatients())
                if (dirty.isNotEmpty()) {
                    val payload = dirty.map { it.toSyncPayload(clinicId) }
                    val resp = api.syncPatients(clinicId = clinicId, body = SyncPatientsRequest(payload))
                    if (resp.isSuccessful && resp.body() != null) {
                        applyRemotePatients(resp.body()!!, clinicId, applyOverDirty = true)
                        // Marcar como sincronizados (não apaga nada; só metadado)
                        val uuids = dirty.mapNotNull { it.uuid }.distinct()
                        if (uuids.isNotEmpty()) {
                            patientDao.markPatientsSyncedByUuid(uuids, Date())
                        }
                        Log.i(TAG, "SYNC_PUSH_OK count=${dirty.size}")
                    } else {
                        Log.e(TAG, "SYNC_PUSH_FAIL http=${resp.code()} count=${dirty.size}")
                    }
                } else {
                    Log.d(TAG, "SYNC_PUSH_SKIP no dirty patients")
                }

                // 2) Pull: buscar pacientes do backend atualizados após o último sync
                val updatedAfter = store.getLastPatientsSyncAtIso()
                val pullResp = api.getPatients(clinicId = clinicId, updatedAfter = updatedAfter)
                if (pullResp.isSuccessful && pullResp.body() != null) {
                    val remote = pullResp.body()!!
                    // Importante: não sobrescrever mudanças locais pendentes.
                    applyRemotePatients(remote, clinicId, applyOverDirty = false)
                    // Atualizar watermark do sync (preferir a maior updatedAt retornada pelo servidor)
                    store.setLastPatientsSyncAtIso(computeNextWatermarkIso(remote) ?: nowIsoUtc())
                    Log.i(TAG, "SYNC_PULL_OK count=${remote.size} updatedAfter=${updatedAfter ?: "null"}")
                } else {
                    Log.e(TAG, "SYNC_PULL_FAIL http=${pullResp.code()} updatedAfter=${updatedAfter ?: "null"}")
                }

                Log.i(TAG, "SYNC_PATIENTS_END reason=$reason")
            } catch (e: Exception) {
                Log.e(TAG, "SYNC_PATIENTS_ERROR reason=$reason", e)
            }
        }
    }

    private suspend fun applyRemotePatients(
        remotePatients: List<RemotePatient>,
        clinicId: String,
        applyOverDirty: Boolean
    ) {
        for (rp in remotePatients) {
            // Nunca apagar local: apenas upsert.
            val uuid = rp.id
            val existing = patientDao.getPatientByUuid(uuid)
            if (!applyOverDirty && existing?.dirty == true) {
                Log.w(TAG, "SYNC_PULL_SKIP_DIRTY uuid=$uuid")
                continue
            }
            val merged = mergeRemoteIntoLocal(existing, rp)
            if (existing == null) {
                patientDao.insertPatient(merged.copy(dirty = false, origin = rp.origin ?: "WEB"))
            } else {
                patientDao.updatePatient(merged.copy(id = existing.id, dirty = false, origin = rp.origin ?: existing.origin))
            }
        }
    }

    private fun mergeRemoteIntoLocal(existing: Patient?, rp: RemotePatient): Patient {
        // Atualiza apenas campos que existem no backend; preserva campos locais (ex.: sessionValue, diaCobranca, etc.)
        val birthDate = rp.birthDate?.let { parseIsoToDate(it) } ?: existing?.birthDate ?: Date()
        val updatedAt = rp.updatedAt?.let { parseIsoToDate(it) } ?: Date()
        val createdAt = rp.createdAt?.let { parseIsoToDate(it) } ?: existing?.createdAt ?: Date()

        // Mapear address textual para campos locais (simplificado: colocar em endereco)
        val endereco = rp.address ?: existing?.endereco ?: ""

        return Patient(
            id = existing?.id ?: 0,
            uuid = rp.id,
            origin = rp.origin ?: (existing?.origin ?: "WEB"),
            dirty = false,
            name = rp.name,
            cpf = rp.cpf ?: (existing?.cpf ?: ""),
            birthDate = birthDate,
            phone = rp.phone ?: (existing?.phone ?: ""),
            email = rp.email ?: (existing?.email ?: ""),
            cep = existing?.cep ?: "",
            endereco = endereco,
            numero = existing?.numero ?: "",
            bairro = existing?.bairro ?: "",
            cidade = existing?.cidade ?: "",
            estado = existing?.estado ?: "",
            complemento = existing?.complemento ?: "",
            sessionValue = existing?.sessionValue ?: 0.0,
            diaCobranca = existing?.diaCobranca ?: 1,
            lembreteCobranca = existing?.lembreteCobranca ?: false,
            clinicalHistory = existing?.clinicalHistory,
            medications = existing?.medications,
            allergies = existing?.allergies,
            isEncrypted = existing?.isEncrypted ?: false,
            notes = rp.observations ?: existing?.notes,
            createdAt = createdAt,
            updatedAt = updatedAt,
            lastSyncedAt = Date(),
            anamneseGroup = existing?.anamneseGroup ?: com.psipro.app.data.entities.AnamneseGroup.ADULTO
        )
    }

    private suspend fun ensureUuidsPersisted(patients: List<Patient>): List<Patient> {
        if (patients.isEmpty()) return patients

        val out = ArrayList<Patient>(patients.size)
        for (p in patients) {
            if (!p.uuid.isNullOrBlank()) {
                out.add(p)
                continue
            }

            // CRÍTICO: persistir UUID no Room antes de enviar, senão o app pode gerar um UUID novo a cada sync
            // e duplicar pacientes no backend.
            val generated = UUID.randomUUID().toString()
            val updated = p.copy(uuid = generated, origin = "ANDROID")
            try {
                patientDao.updatePatient(updated)
            } catch (e: Exception) {
                // Se falhar, ainda tentamos seguir com o objeto em memória para não travar o sync.
                Log.e(TAG, "Failed to persist generated UUID for patientId=${p.id}", e)
            }
            out.add(updated)
        }
        return out
    }

    private fun computeNextWatermarkIso(remote: List<RemotePatient>): String? {
        // ISO 8601 em UTC com milissegundos é ordenável por string (quando no mesmo formato).
        // O backend retorna updatedAt como ISO. Usamos o maior valor para evitar perder updates
        // quando o relógio do dispositivo não está alinhado.
        return remote.asSequence()
            .mapNotNull { it.updatedAt }
            .maxOrNull()
    }

    private fun Patient.toSyncPayload(clinicId: String): SyncPatientPayload {
        return SyncPatientPayload(
            id = this.uuid,
            clinicId = clinicId,
            name = this.name,
            birthDate = formatDateIsoUtc(this.birthDate),
            cpf = this.cpf.ifBlank { null },
            phone = this.phone.ifBlank { null },
            email = this.email.ifBlank { null },
            address = buildAddressString(this),
            observations = this.notes,
            origin = "ANDROID",
            source = "app",
            updatedAt = formatDateIsoUtc(this.updatedAt)
        )
    }

    private fun buildAddressString(p: Patient): String? {
        val parts = listOf(
            p.endereco,
            p.numero,
            p.bairro,
            p.cidade,
            p.estado,
            p.cep,
            p.complemento
        ).map { it.trim() }.filter { it.isNotBlank() }
        return if (parts.isEmpty()) null else parts.joinToString(", ")
    }

    private fun nowIsoUtc(): String = formatDateIsoUtc(Date())

    private fun formatDateIsoUtc(date: Date): String {
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        df.timeZone = TimeZone.getTimeZone("UTC")
        return df.format(date)
    }

    private fun parseIsoToDate(iso: String): Date? {
        return try {
            val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            df.timeZone = TimeZone.getTimeZone("UTC")
            df.parse(iso)
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private const val TAG = "SyncPatientsManager"
    }
}

