package com.example.psipro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.psipro.data.entities.AppointmentStatus
import com.example.psipro.data.repository.AppointmentRepository
import com.example.psipro.data.repository.PatientRepository
import com.example.psipro.reports.AppointmentReportGenerator
import com.example.psipro.reports.PatientReport
import com.example.psipro.reports.PeriodReport
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val patientRepository: PatientRepository,
    private val reportGenerator: AppointmentReportGenerator
) : ViewModel() {

    suspend fun generatePatientReport(
        patientId: Long,
        onSuccess: (PatientReport) -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val patient = patientRepository.getPatientById(patientId)
                if (patient == null) {
                    onError(Exception("Paciente não encontrado"))
                    return@launch
                }

                val appointments = appointmentRepository.getAppointmentsByPatient(patientId).first()
                val report = reportGenerator.generatePatientReport(patient, appointments)
                onSuccess(report)
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    suspend fun generatePeriodReport(
        startDate: Date,
        endDate: Date,
        onSuccess: (PeriodReport) -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val appointments = appointmentRepository.getAppointmentsByDateRange(startDate, endDate).first()
                val patientIds = appointments.map { it.patientId }.distinct()
                
                val patients = patientIds.mapNotNull { id ->
                    patientRepository.getPatientById(id)?.let { patient ->
                        id to patient
                    }
                }.toMap()

                val report = reportGenerator.generatePeriodReport(
                    startDate = startDate,
                    endDate = endDate,
                    appointments = appointments,
                    patients = patients
                )
                onSuccess(report)
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    suspend fun generatePatientAppointmentHistory(
        patientId: Long,
        status: AppointmentStatus? = null,
        onSuccess: (PatientReport) -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val patient = patientRepository.getPatientById(patientId)
                if (patient == null) {
                    onError(Exception("Paciente não encontrado"))
                    return@launch
                }

                val appointments = appointmentRepository.getAppointmentsByPatient(patientId).first()
                    .let { list -> if (status != null) list.filter { it.status == status } else list }

                val report = reportGenerator.generatePatientReport(patient, appointments)
                onSuccess(report)
            } catch (e: Exception) {
                onError(e)
            }
        }
    }
} 