package com.example.apppisc.data.dao

import androidx.room.*
import com.example.apppisc.data.entities.PatientReport

@Dao
interface PatientReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(report: PatientReport): Long

    @Update
    suspend fun update(report: PatientReport)

    @Delete
    suspend fun delete(report: PatientReport)

    @Query("SELECT * FROM patient_reports WHERE patientId = :patientId ORDER BY dataCriacao DESC")
    suspend fun getReportsByPatient(patientId: Long): List<PatientReport>

    @Query("SELECT * FROM patient_reports WHERE id = :id LIMIT 1")
    suspend fun getReportById(id: Long): PatientReport?
} 