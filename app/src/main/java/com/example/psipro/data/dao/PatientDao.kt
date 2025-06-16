package com.example.psipro.data.dao

import androidx.room.*
import com.example.psipro.data.entities.Patient
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientDao {
    @Query("SELECT * FROM patients ORDER BY name ASC")
    fun getAllPatients(): Flow<List<Patient>>

    @Query("SELECT * FROM patients WHERE id = :id")
    suspend fun getPatientById(id: Long): Patient?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: Patient): Long

    @Update
    suspend fun updatePatient(patient: Patient)

    @Delete
    suspend fun deletePatient(patient: Patient)

    @Query("SELECT * FROM patients WHERE name LIKE '%' || :query || '%'")
    fun searchPatients(query: String): Flow<List<Patient>>

    @Query("SELECT * FROM patients WHERE cpf = :cpf LIMIT 1")
    suspend fun getPatientByCpf(cpf: String): Patient?

    @Query("SELECT COUNT(*) FROM patients")
    suspend fun getPatientCount(): Int
} 