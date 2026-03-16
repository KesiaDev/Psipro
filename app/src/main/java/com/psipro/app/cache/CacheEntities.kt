package com.psipro.app.cache

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.psipro.app.data.entities.AppointmentStatus
import java.util.Date

@Entity(tableName = "patient_cache")
data class PatientCache(
    @PrimaryKey
    val id: Long,
    val name: String,
    val cpf: String,
    val phone: String,
    val lastUpdated: Long
)

@Entity(tableName = "appointment_cache")
data class AppointmentCache(
    @PrimaryKey
    val id: Long,
    val patientId: Long?,
    val date: Date,
    val startTime: String,
    val endTime: String,
    val status: AppointmentStatus,
    val lastUpdated: Long
) 



