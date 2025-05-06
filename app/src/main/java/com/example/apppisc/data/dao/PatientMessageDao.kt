package com.example.apppisc.data.dao

import androidx.room.*
import com.example.apppisc.data.entities.PatientMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientMessageDao {
    @Query("SELECT * FROM patient_messages WHERE patientId = :patientId ORDER BY data DESC")
    fun getMessagesForPatient(patientId: Long): Flow<List<PatientMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: PatientMessage): Long

    @Delete
    suspend fun deleteMessage(message: PatientMessage)

    @Query("DELETE FROM patient_messages WHERE patientId = :patientId")
    suspend fun deleteAllMessagesForPatient(patientId: Long)
} 