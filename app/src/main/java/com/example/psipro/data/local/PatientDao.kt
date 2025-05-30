package com.example.psipro.data.local

import androidx.room.*
import com.example.psipro.data.entities.Patient
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientDao {
    @Query("SELECT * FROM patients")
    fun getAllPatients(): Flow<List<Patient>>

    @Query("SELECT * FROM patients WHERE name LIKE :query OR cpf LIKE :query")
    fun searchPatients(query: String): Flow<List<Patient>>

    @Query("SELECT COUNT(*) FROM patients")
    suspend fun getPatientCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: Patient)

    @Update
    suspend fun updatePatient(patient: Patient)

    @Delete
    suspend fun deletePatient(patient: Patient)
} 