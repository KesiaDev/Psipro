package com.example.apppisc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apppisc.data.entities.Patient
import com.example.apppisc.data.repository.PatientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatientViewModel @Inject constructor(
    private val repository: PatientRepository
) : ViewModel() {
    private val _patients = MutableStateFlow<List<Patient>>(emptyList())
    val patients: StateFlow<List<Patient>> = _patients

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    init {
        loadAllPatients()
    }

    fun loadAllPatients() {
        viewModelScope.launch {
            repository.allPatients.collect { patientList ->
                _patients.value = patientList
            }
        }
    }

    fun searchPatients(query: String) {
        viewModelScope.launch {
            repository.searchPatients(query).collect { patientList ->
                _patients.value = patientList
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isNotBlank()) {
            searchPatients(query)
        } else {
            loadAllPatients()
        }
    }

    suspend fun getPatientById(id: Long): Patient? {
        return repository.getPatientById(id)
    }

    suspend fun getPatientByCpf(cpf: String): Patient? {
        return repository.getPatientByCpf(cpf)
    }

    fun insertPatient(patient: Patient, onSuccess: (Long) -> Unit, onError: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                val id = repository.insertPatient(patient)
                onSuccess(id)
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun updatePatient(patient: Patient, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                repository.updatePatient(patient)
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun deletePatient(patient: Patient, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                repository.deletePatient(patient)
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }
} 