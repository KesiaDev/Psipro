package com.psipro.app.sync.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

/**
 * Encadeia sync: pacientes → agendamentos → sessões → pagamentos.
 * Ordem garante dependências (pagamentos referenciam sessões, etc).
 */
object SyncScheduler {
    private const val UNIQUE_WORK_NAME = "psipro_full_sync"

    fun enqueueBoth(context: Context, reason: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val patientsRequest = OneTimeWorkRequestBuilder<PatientsSyncWorker>()
            .setConstraints(constraints)
            .setInputData(workDataOf(PatientsSyncWorker.KEY_REASON to reason))
            .build()

        val appointmentsRequest = OneTimeWorkRequestBuilder<AppointmentSyncWorker>()
            .setConstraints(constraints)
            .setInputData(workDataOf(AppointmentSyncWorker.KEY_REASON to reason))
            .build()

        val sessionsRequest = OneTimeWorkRequestBuilder<SessionSyncWorker>()
            .setConstraints(constraints)
            .setInputData(workDataOf(SessionSyncWorker.KEY_REASON to reason))
            .build()

        val paymentsRequest = OneTimeWorkRequestBuilder<PaymentSyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                androidx.work.BackoffPolicy.EXPONENTIAL,
                30,
                TimeUnit.SECONDS
            )
            .setInputData(workDataOf(PaymentSyncWorker.KEY_REASON to reason))
            .build()

        WorkManager.getInstance(context)
            .beginUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.KEEP, patientsRequest)
            .then(appointmentsRequest)
            .then(sessionsRequest)
            .then(paymentsRequest)
            .enqueue()
    }
}
