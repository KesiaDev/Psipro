package com.example.psipro.utils

import android.content.Context
import android.util.Log
import com.example.psipro.data.AppDatabase
import com.example.psipro.data.entities.AuditLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

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