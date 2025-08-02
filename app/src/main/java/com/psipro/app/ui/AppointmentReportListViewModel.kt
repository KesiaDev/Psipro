package com.psipro.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psipro.app.data.entities.AppointmentReport
import com.psipro.app.data.repository.AppointmentReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class AppointmentReportListViewModel @Inject constructor(
    private val appointmentReportRepository: AppointmentReportRepository
) : ViewModel() {

    private val _appointmentReports = MutableStateFlow<List<AppointmentReport>>(emptyList())
    val appointmentReports: StateFlow<List<AppointmentReport>> = _appointmentReports

    init {
        loadAppointmentReports()
    }

    private fun loadAppointmentReports() {
        viewModelScope.launch {
            val reports = appointmentReportRepository.getAllAppointmentReports()
            _appointmentReports.value = reports
        }
    }
} 



