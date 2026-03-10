package com.psipro.app.viewmodel

import androidx.lifecycle.viewModelScope
import com.psipro.app.data.entities.AppointmentStatus
import com.psipro.app.data.entities.Patient
import com.psipro.app.data.repository.AppointmentRepository
import com.psipro.app.data.repository.PatientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatientViewModel @Inject constructor(
    private val repository: PatientRepository,
    private val appointmentRepository: AppointmentRepository
) : BaseViewModel() {
    
    private val _patientSaved = MutableStateFlow(false)
    val patientSaved: StateFlow<Boolean> = _patientSaved
    
    private val _patient = MutableStateFlow<Patient?>(null)
    val patient: StateFlow<Patient?> = _patient
    
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
    
    fun getPatient(id: Long) {
        viewModelScope.launch {
            try {
                val patient = repository.getPatientById(id)
                _patient.value = patient
            } catch (e: Exception) {
                _error.value = "Erro ao buscar paciente: "+e.message
                _patient.value = null
            }
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

    /** Retorna (sessões totais, realizadas, faltas) para o paciente. */
    fun getSessionCounts(patientId: Long, onResult: (Int, Int, Int) -> Unit) {
        viewModelScope.launch {
            try {
                val appointments = appointmentRepository.getAppointmentsByPatient(patientId).first()
                val total = appointments.size
                val attended = appointments.count { it.status == AppointmentStatus.REALIZADO }
                val absences = appointments.count { it.status == AppointmentStatus.FALTOU }
                onResult(total, attended, absences)
            } catch (e: Exception) {
                onResult(0, 0, 0)
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        _patientSaved.value = false
    }
} 



