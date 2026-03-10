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

/** Resultado do sync para feedback ao usuário. */
data class SyncPatientsResult(
    val success: Boolean,
    val message: String,
    val pushCount: Int = 0,
    val pullCount: Int = 0
)

@Singleton
class SyncPatientsManager @Inject constructor(
    private val api: BackendApiService,
    private val auth: BackendAuthManager,
    private val store: BackendSessionStore,
    private val patientDao: PatientDao
) {
    suspend fun sync(reason: String) {
        syncWithResult(reason)
    }

    /** Executa sync e retorna resultado para exibir feedback ao usuário. */
    suspend fun syncWithResult(reason: String): SyncPatientsResult = withContext(Dispatchers.IO) {
        try {
            if (!auth.isBackendAuthenticated()) {
                Log.w(TAG, "SYNC_SKIP_NO_BACKEND_TOKEN reason=$reason")
                return@withContext SyncPatientsResult(false, "Faça login novamente para sincronizar.")
            }

            val clinicId = auth.ensureClinicId()
            if (clinicId.isNullOrBlank()) {
                Log.w(TAG, "SYNC_SKIP_NO_CLINIC reason=$reason")
                return@withContext SyncPatientsResult(false, "Clínica não identificada. Faça login novamente.")
            }

            Log.i(TAG, "SYNC_PATIENTS_START reason=$reason clinicId=$clinicId")
            var pushCount = 0
            var pullCount = 0
            var effectiveClinicId = clinicId

            // 1) Push: enviar pacientes locais "dirty"
            val dirty = ensureUuidsPersisted(patientDao.getDirtyPatients())
            if (dirty.isNotEmpty()) {
                val cid = effectiveClinicId
                var pushResp = api.syncPatients(clinicId = cid, body = SyncPatientsRequest(dirty.map { it.toSyncPayload(cid) }))
                // Se 403 "não pertence à clínica": clinicId em cache pode estar errado. Limpar e buscar de /auth/me.
                if (!pushResp.isSuccessful && pushResp.code() == 403) {
                    val errBody = pushResp.errorBody()?.string() ?: ""
                    if (errBody.contains("pertence") || errBody.contains("clinica") || errBody.contains("clínica")) {
                        Log.w(TAG, "SYNC_PUSH_403 clinicId inválido, buscando clinicId correto...")
                        store.clearClinicId()
                        val freshClinicId = auth.ensureClinicId()
                        if (!freshClinicId.isNullOrBlank()) {
                            effectiveClinicId = freshClinicId
                            val cidRetry = freshClinicId
                            pushResp = api.syncPatients(clinicId = cidRetry, body = SyncPatientsRequest(dirty.map { it.toSyncPayload(cidRetry) }))
                            Log.i(TAG, "SYNC_PUSH_RETRY clinicId=$cidRetry")
                        }
                    }
                }
                if (pushResp.isSuccessful && pushResp.body() != null) {
                    applyRemotePatients(pushResp.body()!!, effectiveClinicId, applyOverDirty = true)
                    val uuids = dirty.mapNotNull { it.uuid }.distinct()
                    if (uuids.isNotEmpty()) {
                        patientDao.markPatientsSyncedByUuid(uuids, Date())
                    }
                    pushCount = dirty.size
                    Log.i(TAG, "SYNC_PUSH_OK count=$pushCount")
                } else {
                    val errBody = pushResp.errorBody()?.string() ?: ""
                    Log.e(TAG, "SYNC_PUSH_FAIL http=${pushResp.code()} count=${dirty.size} body=$errBody")
                    val msg = parseErrorMessage(pushResp.code(), errBody)
                    return@withContext SyncPatientsResult(false, "Erro ao enviar: $msg")
                }
            } else {
                Log.d(TAG, "SYNC_PUSH_SKIP no dirty patients")
            }

            // 2) Pull: buscar pacientes do backend
            val updatedAfter = store.getLastPatientsSyncAtIso()
            val pullResp = api.getPatients(clinicId = effectiveClinicId, updatedAfter = updatedAfter)
            if (pullResp.isSuccessful && pullResp.body() != null) {
                val remote = pullResp.body()!!
                applyRemotePatients(remote, effectiveClinicId, applyOverDirty = false)
                store.setLastPatientsSyncAtIso(computeNextWatermarkIso(remote) ?: nowIsoUtc())
                pullCount = remote.size
                Log.i(TAG, "SYNC_PULL_OK count=$pullCount updatedAfter=${updatedAfter ?: "null"}")
            } else {
                val errBody = pullResp.errorBody()?.string() ?: ""
                Log.e(TAG, "SYNC_PULL_FAIL http=${pullResp.code()} body=$errBody")
                val msg = parseErrorMessage(pullResp.code(), errBody)
                return@withContext SyncPatientsResult(false, "Erro ao buscar: $msg")
            }

            Log.i(TAG, "SYNC_PATIENTS_END reason=$reason")
            val msg = when {
                pushCount > 0 && pullCount > 0 -> "Sincronizado: $pushCount enviados, $pullCount recebidos"
                pushCount > 0 -> "Sincronizado: $pushCount paciente(s) enviado(s)"
                pullCount > 0 -> "Sincronizado: $pullCount paciente(s) recebido(s)"
                else -> "Sincronização concluída"
            }
            SyncPatientsResult(true, msg, pushCount, pullCount)
        } catch (e: Exception) {
            Log.e(TAG, "SYNC_PATIENTS_ERROR reason=$reason", e)
            SyncPatientsResult(false, "Erro: ${e.message ?: "Verifique sua conexão"}")
        }
    }

    private fun parseErrorMessage(code: Int, body: String): String {
        if (body.contains("x-clinic-id")) return "clínica não identificada"
        if (code == 401) return "sessão expirada"
        if (code == 403) return "sem permissão"
        if (code == 400) return "dados inválidos"
        if (body.length > 100) return "erro $code"
        return body.ifBlank { "erro $code" }
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

