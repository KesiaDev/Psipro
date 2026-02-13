package com.psipro.app.sync.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

object PatientsSyncScheduler {
    private const val UNIQUE_WORK_NAME = "psipro_patients_sync"

    fun enqueue(context: Context, reason: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<PatientsSyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                androidx.work.BackoffPolicy.EXPONENTIAL,
                30,
                TimeUnit.SECONDS
            )
            .setInputData(workDataOf(PatientsSyncWorker.KEY_REASON to reason))
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.KEEP, request)
    }
}

