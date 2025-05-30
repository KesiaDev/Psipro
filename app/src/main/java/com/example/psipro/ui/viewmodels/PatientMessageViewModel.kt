package com.example.psipro.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.psipro.data.entities.PatientMessage
import com.example.psipro.data.repositories.PatientMessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatientMessageViewModel @Inject constructor(
    private val repository: PatientMessageRepository
) : ViewModel() {

    fun getMessagesForPatient(patientId: Long): Flow<List<PatientMessage>> =
        repository.getMessagesForPatient(patientId)

    fun insertMessage(
        message: PatientMessage,
        onSuccess: (Long) -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val id = repository.insertMessage(message)
                onSuccess(id)
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun deleteMessage(
        message: PatientMessage,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.deleteMessage(message)
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun deleteAllMessagesForPatient(
        patientId: Long,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.deleteAllMessagesForPatient(patientId)
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }
} 