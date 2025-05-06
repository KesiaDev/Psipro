package com.example.apppisc.data.dao

import androidx.room.*
import com.example.apppisc.data.entities.PatientNote
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientNoteDao {
    @Query("SELECT * FROM patient_notes WHERE patientId = :patientId ORDER BY data DESC")
    fun getNotesForPatient(patientId: Long): Flow<List<PatientNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: PatientNote): Long

    @Update
    suspend fun updateNote(note: PatientNote)

    @Delete
    suspend fun deleteNote(note: PatientNote)

    @Query("DELETE FROM patient_notes WHERE patientId = :patientId")
    suspend fun deleteAllNotesForPatient(patientId: Long)
} 