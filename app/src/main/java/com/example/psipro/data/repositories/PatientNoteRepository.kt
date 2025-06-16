package com.example.psipro.data.repositories

import com.example.psipro.data.dao.PatientNoteDao
import com.example.psipro.data.entities.PatientNote
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatientNoteRepository @Inject constructor(
    private val patientNoteDao: PatientNoteDao
) {
    fun getNotesForPatient(patientId: Long): Flow<List<PatientNote>> = 
        patientNoteDao.getNotesByPatient(patientId)

    suspend fun insertNote(note: PatientNote): Long = 
        patientNoteDao.insertNote(note)

    suspend fun updateNote(note: PatientNote) = 
        patientNoteDao.updateNote(note)

    suspend fun deleteNote(note: PatientNote) = 
        patientNoteDao.deleteNote(note)

    suspend fun deleteAllNotesForPatient(patientId: Long) = 
        patientNoteDao.deleteAllNotesForPatient(patientId)
} 