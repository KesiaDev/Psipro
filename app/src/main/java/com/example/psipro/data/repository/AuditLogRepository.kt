package com.example.psipro.data.repository

import com.example.psipro.data.dao.AuditLogDao
import com.example.psipro.data.entities.AuditLog
import javax.inject.Inject
import kotlinx.coroutines.runBlocking

class AuditLogRepository @Inject constructor(
    private val auditLogDao: AuditLogDao
) {
    suspend fun getAllLogs(): List<AuditLog> = auditLogDao.getAllLogs()
    suspend fun insert(auditLog: AuditLog) = auditLogDao.insertLog(auditLog)

    fun getAllLogsBlocking(): List<AuditLog> = runBlocking { getAllLogs() }
    fun insertBlocking(auditLog: AuditLog) = runBlocking { insert(auditLog) }
} 