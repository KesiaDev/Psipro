package com.example.psipro.data.repository

import com.example.psipro.data.dao.AuditLogDao
import com.example.psipro.data.entities.AuditLog
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first

class AuditLogRepository @Inject constructor(
    private val auditLogDao: AuditLogDao
) {
    fun getAllLogs(): Flow<List<AuditLog>> = auditLogDao.getAllLogs()
    suspend fun insert(auditLog: AuditLog) = auditLogDao.insertLog(auditLog)

    fun getAllLogsBlocking(): List<AuditLog> = runBlocking { getAllLogs().first() }
    fun insertBlocking(auditLog: AuditLog) = runBlocking { insert(auditLog) }
} 