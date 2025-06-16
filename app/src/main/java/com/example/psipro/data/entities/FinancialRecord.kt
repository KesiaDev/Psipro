package com.example.psipro.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "financial_records")
data class FinancialRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patientId: Long? = null,
    val description: String,
    val value: Double,
    val type: String, // RECEITA ou DESPESA
    val date: Date = Date()
) 