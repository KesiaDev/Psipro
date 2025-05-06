package com.example.apppisc.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "financial_records")
data class FinancialRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patientId: Long,
    val description: String,
    val amount: Double,
    val date: Date,
    val status: String, // "Pago", "Pendente", "Cancelado"
    val paymentMethod: String? = null,
    val paymentDate: Date? = null,
    val notes: String? = null
) 