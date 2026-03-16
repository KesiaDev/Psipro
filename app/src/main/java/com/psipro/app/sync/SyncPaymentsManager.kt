package com.psipro.app.sync

import android.util.Log
import com.psipro.app.data.dao.AnotacaoSessaoDao
import com.psipro.app.data.dao.CobrancaSessaoDao
import com.psipro.app.data.dao.PatientDao
import com.psipro.app.data.entities.CobrancaSessao
import com.psipro.app.data.entities.StatusPagamento
import com.psipro.app.sync.api.BackendApiService
import com.psipro.app.sync.api.RemotePayment
import com.psipro.app.sync.api.SyncPaymentPayload
import com.psipro.app.sync.api.SyncPaymentsRequest
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
class SyncPaymentsManager @Inject constructor(
    private val api: BackendApiService,
    private val auth: BackendAuthManager,
    private val store: BackendSessionStore,
    private val paymentDao: CobrancaSessaoDao,
    private val sessionDao: AnotacaoSessaoDao,
    private val patientDao: PatientDao
) {
    suspend fun sync(reason: String) {
        withContext(Dispatchers.IO) {
            try {
                if (!auth.isBackendAuthenticated()) return@withContext
                val clinicId = auth.ensureClinicId() ?: return@withContext
                Log.i(TAG, "PAYMENT_SYNC_START reason=$reason")

                pushPayments(clinicId)
                pullPayments(clinicId)

                Log.i(TAG, "PAYMENT_SYNC_END reason=$reason")
            } catch (e: Exception) {
                Log.e(TAG, "PAYMENT_SYNC_ERROR reason=$reason", e)
            }
        }
    }

    private suspend fun pushPayments(clinicId: String) {
        val dirty = ensureBackendIdsPersisted(paymentDao.getDirtyPayments())
        if (dirty.isEmpty()) {
            Log.d(TAG, "PAYMENT_PUSH_SKIP no dirty")
            return
        }
        val payload = mutableListOf<SyncPaymentPayload>()
        for (p in dirty) {
            val sessionBackendId = p.anotacaoSessaoId?.let { sessionDao.getById(it)?.backendId }
            if (sessionBackendId.isNullOrBlank()) {
                Log.w(TAG, "PAYMENT_PUSH_SKIP payment=${p.id} no session backendId")
                continue
            }
            payload.add(
                SyncPaymentPayload(
                    id = p.backendId!!,
                    sessionId = sessionBackendId,
                    amount = p.valor,
                    updatedAt = formatIsoUtc(p.updatedAt),
                    status = toBackendStatus(p.status),
                    paidAt = p.dataPagamento?.let { formatIsoUtc(it) }
                )
            )
        }
        if (payload.isEmpty()) return
        val resp = api.syncPayments(clinicId = clinicId, body = SyncPaymentsRequest(payload))
        if (resp.isSuccessful && resp.body() != null) {
            paymentDao.markSyncedByBackendId(payload.map { it.id }.distinct(), Date())
            Log.i(TAG, "PAYMENT_PUSH_OK count=${payload.size}")
        } else {
            Log.e(TAG, "PAYMENT_PUSH_FAIL http=${resp.code()}")
        }
    }

    private fun toBackendStatus(s: StatusPagamento): String = when (s) {
        StatusPagamento.PAGO -> "pago"
        StatusPagamento.A_RECEBER, StatusPagamento.VENCIDO -> "pendente"
        StatusPagamento.CANCELADO -> "cancelado"
    }

    private fun fromBackendStatus(s: String?): StatusPagamento = when (s?.lowercase()) {
        "pago" -> StatusPagamento.PAGO
        "pendente" -> StatusPagamento.A_RECEBER
        "cancelado" -> StatusPagamento.CANCELADO
        else -> StatusPagamento.A_RECEBER
    }

    private suspend fun pullPayments(clinicId: String) {
        val updatedAfter = store.getLastPaymentsSyncAtIso()
        val resp = api.getPayments(clinicId = clinicId, updatedAfter = updatedAfter)
        if (!resp.isSuccessful || resp.body() == null) {
            Log.e(TAG, "PAYMENT_PULL_FAIL http=${resp.code()}")
            return
        }
        val remote = resp.body()!!
        for (rp in remote) {
            val existing = paymentDao.getByBackendId(rp.id)
            if (existing != null && existing.dirty) {
                Log.w(TAG, "PAYMENT_PULL_SKIP_DIRTY id=${rp.id}")
                continue
            }
            val sessionId = rp.sessionId ?: continue
            val localSession = sessionDao.getByBackendId(sessionId)
            if (localSession == null) {
                Log.w(TAG, "PAYMENT_PULL_SKIP session $sessionId not local")
                continue
            }
            val paidAt = rp.paidAt?.let { parseIsoToDate(it) }
            val merged = (existing ?: CobrancaSessao(
                patientId = localSession.patientId,
                anotacaoSessaoId = localSession.id,
                numeroSessao = localSession.numeroSessao,
                valor = rp.amount,
                dataSessao = localSession.dataHora,
                dataVencimento = localSession.dataHora
            )).copy(
                id = existing?.id ?: 0,
                backendId = rp.id,
                dirty = false,
                lastSyncedAt = Date(),
                patientId = localSession.patientId,
                anotacaoSessaoId = localSession.id,
                valor = rp.amount,
                status = fromBackendStatus(rp.status),
                dataPagamento = paidAt ?: existing?.dataPagamento,
                createdAt = rp.createdAt?.let { parseIsoToDate(it) } ?: existing?.createdAt ?: Date(),
                updatedAt = rp.updatedAt?.let { parseIsoToDate(it) } ?: Date()
            )
            if (existing == null) {
                paymentDao.insert(merged)
            } else {
                paymentDao.update(merged)
            }
        }
        val nextWatermark = remote.mapNotNull { it.updatedAt }.maxOrNull() ?: nowIsoUtc()
        store.setLastPaymentsSyncAtIso(nextWatermark)
        Log.i(TAG, "PAYMENT_PULL_OK count=${remote.size}")
    }

    private suspend fun ensureBackendIdsPersisted(payments: List<CobrancaSessao>): List<CobrancaSessao> {
        if (payments.isEmpty()) return payments
        return payments.map { p ->
            if (!p.backendId.isNullOrBlank()) p
            else {
                val id = UUID.randomUUID().toString()
                try { paymentDao.update(p.copy(backendId = id)) } catch (_: Exception) {}
                p.copy(backendId = id)
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

    companion object { private const val TAG = "SyncPaymentsManager" }
}
