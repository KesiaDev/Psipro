package com.example.psipro.data.dao

import androidx.room.*
import com.example.psipro.data.entities.PatientMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientMessageDao {
    @Query("SELECT * FROM patient_messages WHERE patientId = :patientId ORDER BY sentAt DESC")
    fun getMessagesByPatient(patientId: Long): Flow<List<PatientMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: PatientMessage): Long

    @Delete
    suspend fun deleteMessage(message: PatientMessage)

    @Query("DELETE FROM patient_messages WHERE patientId = :patientId")
    suspend fun deleteAllMessagesForPatient(patientId: Long)
} 