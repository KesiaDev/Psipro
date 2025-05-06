package com.example.apppisc.utils

import android.content.Context
import com.example.apppisc.data.AppDatabase
import com.example.apppisc.data.entities.AuditLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AuditLogger {
    fun log(context: Context, userId: Long?, action: String, target: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(context)
            db.auditLogDao().insertLog(
                AuditLog(
                    userId = userId,
                    action = action,
                    target = target
                )
            )
        }
    }
} 