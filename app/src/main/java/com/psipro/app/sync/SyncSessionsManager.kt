package com.psipro.app.sync

import android.util.Log
import com.psipro.app.data.dao.AnotacaoSessaoDao
import com.psipro.app.data.dao.PatientDao
import com.psipro.app.data.entities.AnotacaoSessao
import com.psipro.app.sync.api.BackendApiService
import com.psipro.app.sync.api.RemoteSession
import com.psipro.app.sync.api.SyncSessionPayload
import com.psipro.app.sync.api.SyncSessionsRequest
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
class SyncSessionsManager @Inject constructor(
    private val api: BackendApiService,
    private val auth: BackendAuthManager,
    private val store: BackendSessionStore,
    private val sessionDao: AnotacaoSessaoDao,
    private val patientDao: PatientDao
) {
    suspend fun sync(reason: String) {
        withContext(Dispatchers.IO) {
            try {
                if (!auth.isBackendAuthenticated()) return@withContext
                val clinicId = auth.ensureClinicId() ?: return@withContext
                Log.i(TAG, "SESSION_SYNC_START reason=$reason")

                pushSessions(clinicId)
                pullSessions(clinicId)

                Log.i(TAG, "SESSION_SYNC_END reason=$reason")
            } catch (e: Exception) {
                Log.e(TAG, "SESSION_SYNC_ERROR reason=$reason", e)
            }
        }
    }

    private suspend fun pushSessions(clinicId: String) {
        val dirty = ensureBackendIdsPersisted(sessionDao.getDirtySessions())
        if (dirty.isEmpty()) {
            Log.d(TAG, "SESSION_PUSH_SKIP no dirty")
            return
        }
        val professionalId = getProfessionalId() ?: return
        val payload = mutableListOf<SyncSessionPayload>()
        for (s in dirty) {
            val patientUuid = patientDao.getPatientById(s.patientId)?.uuid
            if (patientUuid.isNullOrBlank()) {
                Log.w(TAG, "SESSION_PUSH_SKIP session=${s.id} patient not synced")
                continue
            }
            payload.add(
                SyncSessionPayload(
                    id = s.backendId!!,
                    patientId = patientUuid,
                    professionalId = professionalId,
                    date = formatIsoUtc(s.dataHora),
                    duration = 50,
                    updatedAt = formatIsoUtc(s.updatedAt),
                    notes = buildNotes(s),
                    status = "realizada"
                )
            )
        }
        if (payload.isEmpty()) return
        val resp = api.syncSessions(clinicId = clinicId, body = SyncSessionsRequest(payload))
        if (resp.isSuccessful && resp.body() != null) {
            sessionDao.markSyncedByBackendId(payload.map { it.id }.distinct(), Date())
            Log.i(TAG, "SESSION_PUSH_OK count=${payload.size}")
        } else {
            Log.e(TAG, "SESSION_PUSH_FAIL http=${resp.code()}")
        }
    }

    private fun buildNotes(s: AnotacaoSessao): String {
        val parts = listOf(
            s.assuntos.takeIf { it.isNotBlank() }?.let { "Assuntos: $it" },
            s.estadoEmocional.takeIf { it.isNotBlank() }?.let { "Estado: $it" },
            s.intervencoes.takeIf { it.isNotBlank() }?.let { "Intervenções: $it" },
            s.observacoes.takeIf { it.isNotBlank() }
        ).filterNotNull()
        return parts.joinToString("\n").ifBlank { "" }
    }

    private suspend fun pullSessions(clinicId: String) {
        val updatedAfter = store.getLastSessionsSyncAtIso()
        val resp = api.getSessions(clinicId = clinicId, updatedAfter = updatedAfter)
        if (!resp.isSuccessful || resp.body() == null) {
            Log.e(TAG, "SESSION_PULL_FAIL http=${resp.code()}")
            return
        }
        val remote = resp.body()!!
        for (rp in remote) {
            val existing = sessionDao.getByBackendId(rp.id)
            if (existing != null && existing.dirty) {
                Log.w(TAG, "SESSION_PULL_SKIP_DIRTY id=${rp.id}")
                continue
            }
            val localPatient = patientDao.getPatientByUuid(rp.patientId)
            if (localPatient == null) {
                Log.w(TAG, "SESSION_PULL_SKIP patient ${rp.patientId} not local")
                continue
            }
            val date = parseIsoToDate(rp.date) ?: Date()
            val notes = rp.notes ?: ""
            val merged = (existing ?: AnotacaoSessao(
                patientId = localPatient.id,
                numeroSessao = (sessionDao.getMaxSessionNumber(localPatient.id) ?: 0) + 1,
                dataHora = date,
                observacoes = notes
            )).copy(
                id = existing?.id ?: 0,
                backendId = rp.id,
                dirty = false,
                lastSyncedAt = Date(),
                patientId = localPatient.id,
                dataHora = date,
                observacoes = notes,
                createdAt = rp.createdAt?.let { parseIsoToDate(it) } ?: existing?.createdAt ?: Date(),
                updatedAt = rp.updatedAt?.let { parseIsoToDate(it) } ?: Date()
            )
            if (existing == null) {
                sessionDao.insert(merged)
            } else {
                sessionDao.update(merged)
            }
        }
        val nextWatermark = remote.mapNotNull { it.updatedAt }.maxOrNull() ?: nowIsoUtc()
        store.setLastSessionsSyncAtIso(nextWatermark)
        Log.i(TAG, "SESSION_PULL_OK count=${remote.size}")
    }

    private suspend fun getProfessionalId(): String? = try {
        api.me().body()?.id
    } catch (e: Exception) {
        Log.e(TAG, "getProfessionalId failed", e)
        null
    }

    private suspend fun ensureBackendIdsPersisted(sessions: List<AnotacaoSessao>): List<AnotacaoSessao> {
        if (sessions.isEmpty()) return sessions
        return sessions.map { s ->
            if (!s.backendId.isNullOrBlank()) s
            else {
                val id = UUID.randomUUID().toString()
                try { sessionDao.update(s.copy(backendId = id)) } catch (_: Exception) {}
                s.copy(backendId = id)
            }
        }
    }

    private fun formatIsoUtc(date: Date): String {
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        df.timeZone = TimeZone.getTimeZone("UTC")
        return df.format(date)
    }

    private fun nowIsoUtc() = formatIsoUtc(Date())

    private fun parseIsoToDate(iso: String): Date? = try {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.parse(iso) ?: SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.parse(iso)
    } catch (_: Exception) { null }

    companion object { private const val TAG = "SyncSessionsManager" }
}
