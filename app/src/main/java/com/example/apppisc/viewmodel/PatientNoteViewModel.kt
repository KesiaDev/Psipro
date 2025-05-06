package com.example.apppisc.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.apppisc.data.AppDatabase
import com.example.apppisc.data.entities.PatientNote
import com.example.apppisc.data.repository.PatientNoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class PatientNoteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PatientNoteRepository

    init {
        val patientNoteDao = AppDatabase.getInstance(application).patientNoteDao()
        repository = PatientNoteRepository(patientNoteDao)
    }

    fun getNotesForPatient(patientId: Long): Flow<List<PatientNote>> {
        return repository.getNotesForPatient(patientId)
    }

    fun insertNote(note: PatientNote, onSuccess: (Long) -> Unit, onError: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                val id = repository.insertNote(note)
                onSuccess(id)
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun updateNote(note: PatientNote, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                repository.updateNote(note)
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun deleteNote(note: PatientNote, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteNote(note)
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun deleteAllNotesForPatient(patientId: Long, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteAllNotesForPatient(patientId)
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }
} 