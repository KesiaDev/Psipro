package com.example.apppisc.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long?,
    val action: String, // ex: VIEW, EXPORT, EDIT, DELETE
    val target: String, // ex: "Paciente:123", "Relatorio:45"
    val timestamp: Long = System.currentTimeMillis()
) 