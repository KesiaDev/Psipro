package com.psipro.app.data.dao

import androidx.room.*
import com.psipro.app.data.entities.Patient
import kotlinx.coroutines.flow.Flow
import java.util.Date

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

    @Query("SELECT * FROM patients WHERE uuid = :uuid LIMIT 1")
    suspend fun getPatientByUuid(uuid: String): Patient?

    @Query("SELECT * FROM patients WHERE dirty = 1")
    suspend fun getDirtyPatients(): List<Patient>

    @Query("UPDATE patients SET dirty = 0, lastSyncedAt = :syncedAt WHERE uuid IN (:uuids)")
    suspend fun markPatientsSyncedByUuid(uuids: List<String>, syncedAt: Date)

    @Query("SELECT COUNT(*) FROM patients")
    suspend fun getPatientCount(): Int

    @Query("SELECT * FROM patients")
    suspend fun getAll(): List<Patient>
} 



