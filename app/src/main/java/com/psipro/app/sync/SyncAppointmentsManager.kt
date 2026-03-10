package com.psipro.app.sync

import android.util.Log
import com.psipro.app.data.dao.AppointmentDao
import com.psipro.app.data.dao.PatientDao
import com.psipro.app.data.entities.Appointment
import com.psipro.app.data.entities.AppointmentStatus
import com.psipro.app.data.entities.Patient
import com.psipro.app.sync.api.BackendApiService
import com.psipro.app.sync.api.RemoteAppointment
import com.psipro.app.sync.api.SyncAppointmentPayload
import com.psipro.app.sync.api.SyncAppointmentsRequest
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
class SyncAppointmentsManager @Inject constructor(
    private val api: BackendApiService,
    private val auth: BackendAuthManager,
    private val store: BackendSessionStore,
    private val appointmentDao: AppointmentDao,
    private val patientDao: PatientDao
) {
    suspend fun sync(reason: String) {
        withContext(Dispatchers.IO) {
            try {
                if (!auth.isBackendAuthenticated()) {
                    Log.w(TAG, "APPT_SYNC_SKIP_NO_BACKEND reason=$reason")
                    return@withContext
                }
                val clinicId = auth.ensureClinicId()
                if (clinicId.isNullOrBlank()) {
                    Log.w(TAG, "APPT_SYNC_SKIP_NO_CLINIC reason=$reason")
                    return@withContext
                }
                Log.i(TAG, "APPT_SYNC_START reason=$reason clinicId=$clinicId")

                // 1) Push: enviar agendamentos locais dirty
                pushAppointments(clinicId)

                // 2) Pull: buscar agendamentos do backend
                pullAppointments(clinicId)
            } catch (e: Exception) {
                Log.e(TAG, "APPT_SYNC_ERROR reason=$reason", e)
            }
        }
    }

    suspend fun pushAppointments() {
        withContext(Dispatchers.IO) {
            val clinicId = auth.ensureClinicId() ?: return@withContext
            pushAppointments(clinicId)
        }
    }

    suspend fun pullAppointments() {
        withContext(Dispatchers.IO) {
            val clinicId = auth.ensureClinicId() ?: return@withContext
            pullAppointments(clinicId)
        }
    }

    private suspend fun pushAppointments(clinicId: String) {
        val dirty = ensureBackendIdsPersisted(appointmentDao.getDirtyAppointments())
        if (dirty.isEmpty()) {
            Log.d(TAG, "APPT_PUSH_SKIP no dirty appointments")
            return
        }
        val professionalId = getProfessionalId() ?: run {
            Log.e(TAG, "APPT_PUSH_SKIP no professionalId (me)")
            return
        }
        val payload = mutableListOf<SyncAppointmentPayload>()
        for (a in dirty) {
            val patientUuid = a.patientId?.let { patientDao.getPatientById(it)?.uuid }
            if (patientUuid.isNullOrBlank()) {
                Log.w(TAG, "APPT_PUSH_SKIP appt=${a.id} patient ${a.patientId} not synced or no uuid")
                continue
            }
            val scheduledAt = toScheduledAtIso(a.date, a.startTime)
            val duration = parseDurationMinutes(a.startTime, a.endTime)
            if (duration <= 0) {
                Log.w(TAG, "APPT_PUSH_SKIP appt=${a.id} invalid duration")
                continue
            }
            payload.add(
                SyncAppointmentPayload(
                    id = a.backendId!!,
                    patientId = patientUuid,
                    professionalId = professionalId,
                    scheduledAt = scheduledAt,
                    duration = duration,
                    updatedAt = formatIsoUtc(a.updatedAt),
                    type = a.type?.name,
                    notes = a.notes,
                    status = toBackendStatus(a.status)
                )
            )
        }
        if (payload.isEmpty()) {
            Log.d(TAG, "APPT_PUSH_SKIP no valid payload")
            return
        }
        val resp = api.syncAppointments(clinicId = clinicId, body = SyncAppointmentsRequest(payload))
        if (resp.isSuccessful && resp.body() != null) {
            val uuids = payload.map { it.id }.distinct()
            appointmentDao.markAppointmentsSyncedByBackendId(uuids, Date())
            Log.i(TAG, "APPT_PUSH_OK count=${payload.size}")
        } else {
            Log.e(TAG, "APPT_PUSH_FAIL http=${resp.code()} count=${payload.size}")
        }
    }

    private suspend fun pullAppointments(clinicId: String) {
        var updatedAfter = store.getLastAppointmentsSyncAtIso()
        var pullResp = api.getAppointments(clinicId = clinicId, updatedAfter = updatedAfter)
        if (!pullResp.isSuccessful || pullResp.body() == null) {
            Log.e(TAG, "APPT_PULL_FAIL http=${pullResp.code()} updatedAfter=$updatedAfter")
            return
        }
        var remote = pullResp.body()!!
        // Fallback: se watermark antigo fez retornar 0, forçar sync completo uma vez
        if (remote.isEmpty() && !updatedAfter.isNullOrBlank()) {
            Log.w(TAG, "APPT_PULL_EMPTY_WITH_WATERMARK clearing and retrying full sync")
            store.setLastAppointmentsSyncAtIso(null)
            updatedAfter = null
            pullResp = api.getAppointments(clinicId = clinicId, updatedAfter = null)
            if (pullResp.isSuccessful && pullResp.body() != null) {
                remote = pullResp.body()!!
            }
        }
        // Se recebemos 0 com updatedAfter definido, watermark pode estar errado (ex: sync anterior pulou todos)
        if (remote.isEmpty() && !updatedAfter.isNullOrBlank()) {
            Log.w(TAG, "APPT_PULL_EMPTY com updatedAfter - limpando watermark para forçar sync completo na próxima vez")
            store.setLastAppointmentsSyncAtIso(null)
            return
        }
        val processedUpdatedAts = mutableListOf<String>()
        for (rp in remote) {
            val existing = appointmentDao.getAppointmentByBackendId(rp.id)
            if (existing != null && existing.dirty) {
                Log.w(TAG, "APPT_PULL_SKIP_DIRTY backendId=${rp.id}")
                continue
            }
            var localPatient = patientDao.getPatientByUuid(rp.patientId)
            if (localPatient == null && rp.patient != null) {
                // Paciente criado no web: criar localmente para exibir o agendamento
                val now = Date()
                val defaultBirthDate = java.util.Calendar.getInstance().apply { set(1900, 0, 1) }.time
                val newPatient = Patient(
                    uuid = rp.patientId,
                    origin = "WEB",
                    dirty = false,
                    name = rp.patient.name,
                    phone = rp.patient.phone ?: "",
                    birthDate = defaultBirthDate,
                    createdAt = now,
                    updatedAt = now,
                    lastSyncedAt = now
                )
                val newId = patientDao.insertPatient(newPatient)
                localPatient = newPatient.copy(id = newId)
                Log.i(TAG, "APPT_PULL_CREATED_PATIENT patientId=${rp.patientId} name=${rp.patient.name}")
            }
            if (localPatient == null) {
                Log.w(TAG, "APPT_PULL_SKIP patient ${rp.patientId} not in local db and no patient data from backend")
                continue
            }
            val (date, startTime, endTime) = fromScheduledAtAndDuration(rp.scheduledAt, rp.duration)
            val merged = (existing ?: Appointment(
                title = localPatient.name,
                patientId = localPatient.id,
                patientName = localPatient.name,
                patientPhone = localPatient.phone,
                date = date,
                startTime = startTime,
                endTime = endTime
            )).copy(
                id = existing?.id ?: 0,
                backendId = rp.id,
                dirty = false,
                lastSyncedAt = Date(),
                title = existing?.title ?: localPatient.name,
                patientId = localPatient.id,
                patientName = existing?.patientName ?: localPatient.name,
                patientPhone = existing?.patientPhone ?: localPatient.phone,
                date = date,
                startTime = startTime,
                endTime = endTime,
                status = fromBackendStatus(rp.status),
                createdAt = rp.createdAt?.let { parseIsoToDate(it) } ?: existing?.createdAt ?: Date(),
                updatedAt = rp.updatedAt?.let { parseIsoToDate(it) } ?: Date(),
                notes = rp.notes ?: existing?.notes
            )
            if (existing == null) {
                appointmentDao.insertAppointment(merged)
            } else {
                appointmentDao.updateAppointment(merged)
            }
            rp.updatedAt?.let { processedUpdatedAts.add(it) }
        }
        val nextWatermark = processedUpdatedAts.maxOrNull() ?: updatedAfter
        if (processedUpdatedAts.isNotEmpty()) {
            store.setLastAppointmentsSyncAtIso(nextWatermark)
        }
        Log.i(TAG, "APPT_PULL_OK count=${remote.size} processed=${processedUpdatedAts.size} updatedAfter=$updatedAfter")
    }

    private suspend fun getProfessionalId(): String? {
        return try {
            val resp = api.me()
            if (resp.isSuccessful && resp.body() != null) resp.body()!!.id else null
        } catch (e: Exception) {
            Log.e(TAG, "getProfessionalId failed", e)
            null
        }
    }

    private suspend fun ensureBackendIdsPersisted(appointments: List<Appointment>): List<Appointment> {
        if (appointments.isEmpty()) return appointments
        val out = ArrayList<Appointment>(appointments.size)
        for (a in appointments) {
            if (!a.backendId.isNullOrBlank()) {
                out.add(a)
                continue
            }
            val generated = UUID.randomUUID().toString()
            val updated = a.copy(backendId = generated)
            try {
                appointmentDao.updateAppointment(updated)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to persist backendId for appt ${a.id}", e)
            }
            out.add(updated)
        }
        return out
    }

    private fun toScheduledAtIso(date: Date, startTime: String): String {
        val calendar = java.util.Calendar.getInstance().apply {
            time = date
            val parts = startTime.split(":")
            set(java.util.Calendar.HOUR_OF_DAY, parts.getOrNull(0)?.toIntOrNull() ?: 0)
            set(java.util.Calendar.MINUTE, parts.getOrNull(1)?.toIntOrNull() ?: 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return formatIsoUtc(calendar.time)
    }

    private fun parseDurationMinutes(startTime: String, endTime: String): Int {
        val start = parseTimeToMinutes(startTime)
        val end = parseTimeToMinutes(endTime)
        return if (start >= 0 && end > start) end - start else 60
    }

    private fun parseTimeToMinutes(time: String): Int {
        val parts = time.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return h * 60 + m
    }

    private fun fromScheduledAtAndDuration(scheduledAt: String, duration: Int): Triple<Date, String, String> {
        val date = parseIsoToDate(scheduledAt) ?: Date()
        val cal = java.util.Calendar.getInstance().apply { time = date }
        val startH = cal.get(java.util.Calendar.HOUR_OF_DAY)
        val startM = cal.get(java.util.Calendar.MINUTE)
        val startTime = String.format(Locale.US, "%02d:%02d", startH, startM)
        val endM = startH * 60 + startM + duration
        val endH = endM / 60
        val endMn = endM % 60
        val endTime = String.format(Locale.US, "%02d:%02d", endH, endMn)
        return Triple(date, startTime, endTime)
    }

    private fun toBackendStatus(s: AppointmentStatus): String = when (s) {
        AppointmentStatus.CONFIRMADO -> "confirmada"
        AppointmentStatus.REALIZADO -> "realizada"
        AppointmentStatus.FALTOU -> "falta"
        AppointmentStatus.CANCELOU -> "cancelada"
    }

    private fun fromBackendStatus(s: String?): AppointmentStatus = when (s?.lowercase()) {
        "agendada", "confirmada" -> AppointmentStatus.CONFIRMADO
        "realizada" -> AppointmentStatus.REALIZADO
        "falta" -> AppointmentStatus.FALTOU
        "cancelada" -> AppointmentStatus.CANCELOU
        else -> AppointmentStatus.CONFIRMADO
    }

    private fun formatIsoUtc(date: Date): String {
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        df.timeZone = TimeZone.getTimeZone("UTC")
        return df.format(date)
    }

    private fun nowIsoUtc() = formatIsoUtc(Date())

    private fun parseIsoToDate(iso: String): Date? = try {
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        df.timeZone = TimeZone.getTimeZone("UTC")
        df.parse(iso)
    } catch (_: Exception) {
        try {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.parse(iso)
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private const val TAG = "SyncAppointmentsManager"
    }
}
