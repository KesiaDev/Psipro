package com.example.psipro.data.dao

import androidx.room.*
import com.example.psipro.data.entities.AuditLog
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<AuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLog): Long

    @Delete
    suspend fun deleteLog(log: AuditLog)

    @Query("SELECT * FROM audit_logs WHERE user = :user ORDER BY timestamp DESC")
    suspend fun getLogsForUser(user: String): List<AuditLog>
} 