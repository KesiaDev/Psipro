package com.example.psipro.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val action: String,
    val user: String,
    val timestamp: Date = Date(),
    val details: String? = null
) 