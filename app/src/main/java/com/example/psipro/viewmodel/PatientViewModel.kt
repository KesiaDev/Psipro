package com.example.psipro.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.psipro.data.entities.Patient
import com.example.psipro.data.repository.PatientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatientViewModel @Inject constructor(
    private val repository: PatientRepository
) : BaseViewModel() {
    
    private val _patientSaved = MutableStateFlow(false)
    val patientSaved: StateFlow<Boolean> = _patientSaved
    
    fun savePatient(patient: Patient) {
        launchWithRetry {
            repository.insertPatient(patient)
            _patientSaved.value = true
        }
    }
    
    fun updatePatient(patient: Patient) {
        launchWithRetry {
            repository.updatePatient(patient)
            _patientSaved.value = true
        }
    }
    
    fun deletePatient(patient: Patient) {
        launchWithRetry {
            repository.deletePatient(patient)
            _patientSaved.value = true
        }
    }
    
    fun getPatientById(id: Long, onResult: (Patient?) -> Unit) {
        viewModelScope.launch {
            try {
                val patient = repository.getPatientById(id)
                onResult(patient)
            } catch (e: Exception) {
                _error.value = "Erro ao buscar paciente: "+e.message
                onResult(null)
            }
        }
    }
    
    fun searchPatients(query: String) {
        launchWithTimeout {
            try {
                val patients = repository.searchPatients(query)
                // TODO: Implementar StateFlow para lista de pacientes
            } catch (e: Exception) {
                _error.value = "Erro na busca: ${e.message}"
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        _patientSaved.value = false
    }
} 