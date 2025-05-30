package com.example.psipro.data.dao

import androidx.room.*
import com.example.psipro.data.entities.AppointmentReport

@Dao
interface AppointmentReportDao {
    @Query("SELECT * FROM appointment_reports ORDER BY createdAt DESC")
    suspend fun getAllAppointmentReports(): List<AppointmentReport>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointmentReport(report: AppointmentReport): Long

    @Delete
    suspend fun deleteAppointmentReport(report: AppointmentReport)
} 