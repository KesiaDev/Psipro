package com.example.psipro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.psipro.data.entities.Patient
import com.example.psipro.data.repository.PatientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DadosPessoaisViewModel @Inject constructor(
    private val repository: PatientRepository
) : ViewModel() {
    private val _patient = MutableStateFlow<Patient?>(null)
    val patient: StateFlow<Patient?> = _patient

    fun getPatient(id: Long) {
        viewModelScope.launch {
            _patient.value = repository.getPatientById(id)
        }
    }

    fun salvarPaciente(patient: Patient, onSuccess: () -> Unit = {}, onError: (Exception) -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.updatePatient(patient)
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }
} 