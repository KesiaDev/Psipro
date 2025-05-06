package com.example.apppisc.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apppisc.data.entities.Patient
import com.example.apppisc.data.repositories.PatientRepository
import com.example.apppisc.security.EncryptionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class PatientViewModel @Inject constructor(
    private val repository: PatientRepository,
    private val encryptionManager: EncryptionManager
) : ViewModel() {

    private val _patients = MutableStateFlow<List<Patient>>(emptyList())
    val patients: StateFlow<List<Patient>> = _patients

    private val _searchResults = MutableStateFlow<List<Patient>>(emptyList())
    val searchResults: StateFlow<List<Patient>> = _searchResults

    private val _currentPatient = MutableStateFlow<Patient?>(null)
    val currentPatient: StateFlow<Patient?> = _currentPatient

    private val _patientCount = MutableStateFlow(0)
    val patientCount: StateFlow<Int> = _patientCount

    private val _isNearLimit = MutableStateFlow(false)
    val isNearLimit: StateFlow<Boolean> = _isNearLimit

    private val _uiState = MutableStateFlow<PatientUiState>(PatientUiState.Loading)
    val uiState: StateFlow<PatientUiState> = _uiState.asStateFlow()

    init {
        loadPatients()
        checkPatientCount()
    }

    private fun checkPatientCount() {
        viewModelScope.launch {
            _patientCount.value = repository.getPatientCount()
            _isNearLimit.value = repository.isNearLimit()
        }
    }

    private fun loadPatients() {
        viewModelScope.launch {
            try {
                repository.allPatients.collect { allPatients ->
                    _patients.value = allPatients.map { decryptPatientData(it) }
                    _uiState.value = PatientUiState.Success(allPatients)
                    checkPatientLimit()
                }
            } catch (e: Exception) {
                _uiState.value = PatientUiState.Error("Erro ao carregar pacientes: ${e.message}")
            }
        }
    }

    fun searchPatients(query: String) {
        viewModelScope.launch {
            try {
                if (query.isBlank()) {
                    repository.allPatients.collect { allPatients ->
                        _searchResults.value = allPatients
                    }
                } else {
                    repository.searchPatients(query).collect { filteredPatients ->
                        _searchResults.value = filteredPatients
                    }
                }
            } catch (e: Exception) {
                _uiState.value = PatientUiState.Error("Erro ao buscar pacientes: ${e.message}")
            }
        }
    }

    fun loadPatient(patientId: Long) {
        viewModelScope.launch {
            val patient = repository.getPatientById(patientId)
            _currentPatient.value = patient?.let { decryptPatientData(it) }
        }
    }

    suspend fun canAddMorePatients(): Boolean {
        return repository.canAddMorePatients()
    }

    fun savePatient(patient: Patient) {
        viewModelScope.launch {
            if (!repository.canAddMorePatients()) {
                // Emitir evento de limite atingido
                return@launch
            }

            val encryptedPatient = encryptPatientData(patient)
            if (patient.id == 0L) {
                repository.insertPatient(encryptedPatient)
            } else {
                repository.updatePatient(encryptedPatient)
            }
            loadPatients()
        }
    }

    fun deletePatient(patient: Patient) {
        viewModelScope.launch {
            repository.deletePatient(patient)
            loadPatients()
        }
    }

    suspend fun getPatientByCpf(cpf: String): Patient? {
        return repository.getPatientByCpf(cpf)?.let { decryptPatientData(it) }
    }

    private fun encryptPatientData(patient: Patient): Patient {
        if (patient.isEncrypted) return patient

        return patient.copy(
            clinicalHistory = patient.clinicalHistory?.let { encryptionManager.encrypt(it) },
            medications = patient.medications?.let { encryptionManager.encrypt(it) },
            allergies = patient.allergies?.let { encryptionManager.encrypt(it) },
            isEncrypted = true
        )
    }

    private fun decryptPatientData(patient: Patient): Patient {
        if (!patient.isEncrypted) return patient

        return patient.copy(
            clinicalHistory = patient.clinicalHistory?.let { encryptionManager.decrypt(it) },
            medications = patient.medications?.let { encryptionManager.decrypt(it) },
            allergies = patient.allergies?.let { encryptionManager.decrypt(it) },
            isEncrypted = false
        )
    }

    fun checkPatientLimit() {
        viewModelScope.launch {
            try {
                val currentCount = repository.getPatientCount()
                val maxPatients = 100 // Limite máximo de pacientes
                val warningThreshold = 90 // Limite para mostrar aviso

                if (currentCount >= maxPatients) {
                    _uiState.value = PatientUiState.Error("Limite máximo de pacientes atingido!")
                    _isNearLimit.value = true
                } else if (currentCount >= warningThreshold) {
                    val remaining = maxPatients - currentCount
                    _uiState.value = PatientUiState.Warning("Atenção: Restam apenas $remaining vagas para pacientes!")
                    _isNearLimit.value = true
                } else {
                    _isNearLimit.value = false
                }
            } catch (e: Exception) {
                _uiState.value = PatientUiState.Error("Erro ao verificar limite de pacientes: ${e.message}")
            }
        }
    }
}

sealed class PatientUiState {
    object Loading : PatientUiState()
    data class Success(val patients: List<Patient>) : PatientUiState()
    data class Error(val message: String) : PatientUiState()
    data class Warning(val message: String) : PatientUiState()
} 