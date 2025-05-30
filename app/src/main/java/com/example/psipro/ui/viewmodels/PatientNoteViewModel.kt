package com.example.psipro.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.psipro.data.entities.PatientNote
import com.example.psipro.data.repositories.PatientNoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatientNoteViewModel @Inject constructor(
    private val repository: PatientNoteRepository
) : ViewModel() {

    fun getNotesForPatient(patientId: Long): Flow<List<PatientNote>> =
        repository.getNotesForPatient(patientId)

    fun insertNote(
        note: PatientNote,
        onSuccess: (Long) -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val id = repository.insertNote(note)
                onSuccess(id)
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun updateNote(
        note: PatientNote,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.updateNote(note)
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun deleteNote(
        note: PatientNote,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.deleteNote(note)
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun deleteAllNotesForPatient(
        patientId: Long,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
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