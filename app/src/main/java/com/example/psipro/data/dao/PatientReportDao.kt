package com.example.psipro.data.dao

import androidx.room.*
import com.example.psipro.data.entities.PatientReport
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientReportDao {
    @Query("SELECT * FROM patient_reports WHERE patientId = :patientId ORDER BY dataCriacao DESC")
    fun getReportsByPatient(patientId: Long): Flow<List<PatientReport>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: PatientReport): Long

    @Delete
    suspend fun deleteReport(report: PatientReport)

    @Update
    suspend fun update(report: PatientReport)

    @Query("SELECT * FROM patient_reports WHERE id = :id LIMIT 1")
    suspend fun getReportById(id: Long): PatientReport?
} 