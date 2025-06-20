package com.example.psipro.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.psipro.data.dao.PatientDao
import com.example.psipro.data.entities.Patient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val patientDao: PatientDao
) : ViewModel() {

    private val _patients = MutableStateFlow<List<Patient>>(emptyList())
    val patients: StateFlow<List<Patient>> = _patients.asStateFlow()

    init {
        fetchPatients()
    }

    private fun fetchPatients() {
        viewModelScope.launch {
            patientDao.getAllPatients()
                .catch { exception ->
                    // Handle error
                }
                .collect { patientList ->
                    _patients.value = patientList
                }
        }
    }
} 