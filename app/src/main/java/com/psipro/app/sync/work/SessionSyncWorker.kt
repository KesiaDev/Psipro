package com.psipro.app.sync.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.psipro.app.sync.di.SyncEntryPoint
import dagger.hilt.android.EntryPointAccessors

class SessionSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val reason = inputData.getString(KEY_REASON) ?: "worker"
        return try {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                SyncEntryPoint::class.java
            )
            entryPoint.syncSessionsManager().sync(reason)
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "SessionSyncWorker failed", e)
            Result.success()
        }
    }

    companion object {
        private const val TAG = "SessionSyncWorker"
        const val KEY_REASON = "reason"
    }
}
