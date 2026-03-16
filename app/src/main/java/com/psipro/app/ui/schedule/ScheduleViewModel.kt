package com.psipro.app.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psipro.app.data.dao.PatientDao
import com.psipro.app.data.entities.Patient
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
            try {
                patientDao.getAllPatients()
                    .catch { exception ->
                        android.util.Log.e("ScheduleViewModel", "Erro ao buscar pacientes: ${exception.message}")
                    }
                    .collect { patientList ->
                        _patients.value = patientList
                        android.util.Log.d("ScheduleViewModel", "Pacientes carregados: ${patientList.size}")
                    }
            } catch (e: Exception) {
                android.util.Log.e("ScheduleViewModel", "Erro crítico ao buscar pacientes: ${e.message}")
                _patients.value = emptyList()
            }
        }
    }
} 



