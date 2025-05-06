package com.example.apppisc.data.repository

import com.example.apppisc.data.dao.PatientNoteDao
import com.example.apppisc.data.entities.PatientNote
import kotlinx.coroutines.flow.Flow

class PatientNoteRepository(private val patientNoteDao: PatientNoteDao) {
    fun getNotesForPatient(patientId: Long): Flow<List<PatientNote>> {
        return patientNoteDao.getNotesForPatient(patientId)
    }

    suspend fun insertNote(note: PatientNote): Long {
        return patientNoteDao.insertNote(note)
    }

    suspend fun updateNote(note: PatientNote) {
        patientNoteDao.updateNote(note)
    }

    suspend fun deleteNote(note: PatientNote) {
        patientNoteDao.deleteNote(note)
    }

    suspend fun deleteAllNotesForPatient(patientId: Long) {
        patientNoteDao.deleteAllNotesForPatient(patientId)
    }
} 