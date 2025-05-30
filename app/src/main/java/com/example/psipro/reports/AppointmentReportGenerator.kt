package com.example.psipro.reports

import com.example.psipro.data.entities.Appointment
import com.example.psipro.data.entities.AppointmentStatus
import com.example.psipro.data.entities.Patient
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppointmentReportGenerator @Inject constructor() {
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    private val timeFormat = SimpleDateFormat("HH:mm", Locale("pt", "BR"))

    fun generatePatientReport(
        patient: Patient,
        appointments: List<Appointment>
    ): PatientReport {
        val totalAppointments = appointments.size
        val completedAppointments = appointments.count { it.status == AppointmentStatus.COMPLETED }
        val cancelledAppointments = appointments.count { it.status == AppointmentStatus.CANCELLED }
        val upcomingAppointments = appointments.count { 
            it.status == AppointmentStatus.SCHEDULED && it.date.after(Date())
        }

        return PatientReport(
            patientName = patient.name,
            patientCpf = patient.cpf,
            totalAppointments = totalAppointments,
            completedAppointments = completedAppointments,
            cancelledAppointments = cancelledAppointments,
            upcomingAppointments = upcomingAppointments,
            appointmentHistory = appointments.map { appointment ->
                AppointmentHistoryItem(
                    date = dateFormat.format(appointment.date),
                    time = "${appointment.startTime} - ${appointment.endTime}",
                    status = appointment.status,
                    title = appointment.title,
                    description = appointment.description ?: ""
                )
            }
        )
    }

    fun generatePeriodReport(
        startDate: Date,
        endDate: Date,
        appointments: List<Appointment>,
        patients: Map<Long, Patient>
    ): PeriodReport {
        val totalAppointments = appointments.size
        val completedAppointments = appointments.count { it.status == AppointmentStatus.COMPLETED }
        val cancelledAppointments = appointments.count { it.status == AppointmentStatus.CANCELLED }
        val activePatients = appointments.map { it.patientId }.distinct().size

        val appointmentsByDay = appointments.groupBy { dateFormat.format(it.date) }
        val averageAppointmentsPerDay = if (appointmentsByDay.isNotEmpty()) {
            totalAppointments.toFloat() / appointmentsByDay.size
        } else 0f

        return PeriodReport(
            startDate = dateFormat.format(startDate),
            endDate = dateFormat.format(endDate),
            totalAppointments = totalAppointments,
            completedAppointments = completedAppointments,
            cancelledAppointments = cancelledAppointments,
            activePatients = activePatients,
            averageAppointmentsPerDay = averageAppointmentsPerDay,
            appointmentDetails = appointments.map { appointment ->
                AppointmentDetail(
                    date = dateFormat.format(appointment.date),
                    time = "${appointment.startTime} - ${appointment.endTime}",
                    patientName = patients[appointment.patientId]?.name ?: "Paciente não encontrado",
                    status = appointment.status,
                    title = appointment.title
                )
            }.sortedBy { it.date }
        )
    }

    fun generateReport(appointments: List<Pair<Appointment, Patient>>): String {
        return buildString {
            appendLine("Relatório de Consultas")
            appendLine("===================")
            appendLine()

            appointments.forEach { (appointment, patient) ->
                appendLine("Data: ${dateFormat.format(appointment.date)}")
                appendLine("Horário: ${appointment.startTime} - ${appointment.endTime}")
                appendLine("Paciente: ${patient.name}")
                appendLine("Telefone: ${patient.phone}")
                appendLine("Status: ${appointment.status.toDisplayString()}")
                if (!appointment.description.isNullOrBlank()) {
                    appendLine("Observações: ${appointment.description}")
                }
                appendLine("-------------------")
            }

            if (appointments.isEmpty()) {
                appendLine("Nenhuma consulta encontrada.")
            }
        }
    }

    private fun AppointmentStatus.toDisplayString(): String = when (this) {
        AppointmentStatus.SCHEDULED -> "Agendada"
        AppointmentStatus.COMPLETED -> "Realizada"
        AppointmentStatus.CANCELLED -> "Cancelada"
        AppointmentStatus.NO_SHOW -> "Faltou"
    }
}

data class PatientReport(
    val patientName: String,
    val patientCpf: String,
    val totalAppointments: Int,
    val completedAppointments: Int,
    val cancelledAppointments: Int,
    val upcomingAppointments: Int,
    val appointmentHistory: List<AppointmentHistoryItem>
)

data class AppointmentHistoryItem(
    val date: String,
    val time: String,
    val status: AppointmentStatus,
    val title: String,
    val description: String
)

data class PeriodReport(
    val startDate: String,
    val endDate: String,
    val totalAppointments: Int,
    val completedAppointments: Int,
    val cancelledAppointments: Int,
    val activePatients: Int,
    val averageAppointmentsPerDay: Float,
    val appointmentDetails: List<AppointmentDetail>
)

data class AppointmentDetail(
    val date: String,
    val time: String,
    val patientName: String,
    val status: AppointmentStatus,
    val title: String
) 