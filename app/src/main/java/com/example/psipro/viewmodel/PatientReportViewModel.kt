package com.example.psipro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.psipro.data.entities.PatientReport
import com.example.psipro.data.repository.PatientReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.collect

@HiltViewModel
class PatientReportViewModel @Inject constructor(
    private val repository: PatientReportRepository
) : ViewModel() {
    private val _reports = MutableLiveData<List<PatientReport>>()
    val reports: LiveData<List<PatientReport>> = _reports

    private val _selectedReport = MutableLiveData<PatientReport?>()
    val selectedReport: LiveData<PatientReport?> = _selectedReport

    fun loadReportsByPatient(patientId: Long) {
        viewModelScope.launch {
            repository.getReportsByPatient(patientId).collect { reports ->
                _reports.value = reports
            }
        }
    }

    fun loadReportById(id: Long) {
        viewModelScope.launch {
            _selectedReport.value = repository.getReportById(id)
        }
    }

    fun insert(report: PatientReport, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.insert(report)
            onComplete?.invoke()
        }
    }

    fun update(report: PatientReport, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.update(report)
            onComplete?.invoke()
        }
    }

    fun delete(report: PatientReport, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.delete(report)
            onComplete?.invoke()
        }
    }
} 