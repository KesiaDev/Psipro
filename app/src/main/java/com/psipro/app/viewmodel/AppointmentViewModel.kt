package com.psipro.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psipro.app.data.entities.Appointment
import com.psipro.app.data.entities.AppointmentStatus
import com.psipro.app.data.entities.RecurrenceType
import com.psipro.app.data.repository.AppointmentRepository
import com.psipro.app.notification.AppointmentNotificationService
import com.psipro.app.data.repository.CobrancaAgendamentoRepository
import com.psipro.app.data.entities.CobrancaAgendamento
import com.psipro.app.data.entities.StatusPagamento
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import java.util.Calendar
import javax.inject.Inject
import com.psipro.app.data.entities.generateRecurrenceDates

@HiltViewModel
class AppointmentViewModel @Inject constructor(
    private val repository: AppointmentRepository,
    private val notificationService: AppointmentNotificationService,
    private val cobrancaRepository: CobrancaAgendamentoRepository
) : ViewModel() {
    
    // Propriedade para todos os agendamentos
    val allAppointments: Flow<List<Appointment>> = repository.getAllAppointments()

    fun getAppointmentsByDate(date: Date): Flow<List<Appointment>> {
        return repository.getAppointmentsBetweenDates(date, date)
    }

    fun getAppointmentsByPatient(patientId: Long): Flow<List<Appointment>> {
        return repository.getAppointmentsByPatient(patientId)
    }

    fun getAppointmentsByDateRange(startDate: Date, endDate: Date): Flow<List<Appointment>> {
        return repository.getAppointmentsByDateRange(startDate, endDate)
    }

    fun getAppointmentsByPatientAndStatus(
        patientId: Long,
        status: AppointmentStatus
    ): Flow<List<Appointment>> {
        return repository.getAppointmentsByPatient(patientId)
            .map { appointments -> appointments.filter { it.status == status } }
    }

    suspend fun hasConflictingAppointments(appointment: Appointment): Boolean {
        return repository.hasConflictingAppointments(
            appointment.date,
            appointment.startTime,
            appointment.endTime,
            appointment.id ?: 0L
        )
    }

    suspend fun insertAppointment(appointment: Appointment): Long {
        return repository.insertAppointment(appointment)
    }

    suspend fun updateAppointment(appointment: Appointment) {
        repository.updateAppointment(appointment)
    }

    suspend fun deleteAppointment(appointment: Appointment) {
        repository.deleteAppointment(appointment)
    }

    suspend fun updateAppointmentStatus(id: Long, status: AppointmentStatus) {
        repository.updateAppointmentStatus(id, status)
    }

    fun addAppointment(
        appointment: Appointment,
        onConflict: () -> Unit,
        onSuccess: (Long) -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (appointment.recurrenceType != RecurrenceType.NONE) {
                    // Gerar um recurrenceSeriesId único
                    val seriesId = System.currentTimeMillis()
                    val dates = generateRecurrenceDates(
                        appointment.date,
                        appointment.recurrenceType,
                        appointment.recurrenceInterval,
                        appointment.recurrenceEndDate,
                        appointment.recurrenceCount
                    )
                    var firstId: Long? = null
                    for ((idx, date) in dates.withIndex()) {
                        val occ = appointment.copy(
                            date = date,
                            recurrenceSeriesId = seriesId
                        )
                        if (hasConflictingAppointments(occ)) {
                            onConflict()
                            return@launch
                        }
                        val id = repository.insertAppointment(occ)
                        if (idx == 0) firstId = id
                        if (occ.reminderEnabled) {
                            notificationService.scheduleAppointmentReminder(occ.copy(id = id))
                        }
                    }
                    onSuccess(firstId ?: -1L)
                } else {
                    if (hasConflictingAppointments(appointment)) {
                        onConflict()
                        return@launch
                    }
                    val id = repository.insertAppointment(appointment)
                    if (appointment.reminderEnabled) {
                        notificationService.scheduleAppointmentReminder(appointment.copy(id = id))
                    }
                    onSuccess(id)
                }
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun updateAppointment(
        appointment: Appointment,
        onConflict: () -> Unit,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (hasConflictingAppointments(appointment)) {
                    onConflict()
                    return@launch
                }

                repository.updateAppointment(appointment)
                if (appointment.reminderEnabled) {
                    notificationService.scheduleAppointmentReminder(appointment)
                } else {
                    notificationService.cancelAppointmentReminder(appointment.id)
                }
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun updateAppointmentStatus(
        appointmentId: Long,
        status: AppointmentStatus,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.updateAppointmentStatus(appointmentId, status)
                
                // Se o status for REALIZADO, criar cobrança automática
                if (status == AppointmentStatus.REALIZADO) {
                    val appointment = repository.getAppointmentById(appointmentId)
                    if (appointment != null && appointment.sessionValue > 0) {
                        criarCobrancaAutomatica(appointment)
                    }
                }
                
                if (status in listOf(AppointmentStatus.CANCELOU, AppointmentStatus.CONFIRMADO)) {
                    notificationService.cancelAppointmentReminder(appointmentId)
                }
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun deleteAppointment(
        appointment: Appointment,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.deleteAppointment(appointment)
                notificationService.cancelAppointmentReminder(appointment.id)
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun deleteAppointmentsBySeriesId(
        seriesId: Long,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.deleteAppointmentsBySeriesId(seriesId)
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun updateAppointmentsBySeriesId(
        seriesId: Long,
        fromDate: Date,
        title: String,
        description: String?,
        startTime: String,
        endTime: String,
        reminderEnabled: Boolean,
        reminderMinutes: Int,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.updateAppointmentsBySeriesId(
                    seriesId, fromDate, title, description, startTime, endTime, reminderEnabled, reminderMinutes
                )
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun getAppointmentsBySeriesId(seriesId: Long): Flow<List<Appointment>> =
        repository.getAppointmentsBySeriesId(seriesId)

    private fun criarCobrancaAutomatica(appointment: Appointment) {
        viewModelScope.launch {
            try {
                val dataVencimento = Calendar.getInstance().apply {
                    time = appointment.date
                    add(Calendar.DAY_OF_MONTH, 7) // Vencimento em 7 dias
                }.time
                
                val cobranca = CobrancaAgendamento(
                    patientId = appointment.patientId ?: 0L,
                    appointmentId = appointment.id,
                    valor = appointment.sessionValue,
                    dataAgendamento = appointment.date,
                    dataVencimento = dataVencimento,
                    status = StatusPagamento.A_RECEBER,
                    motivo = "CONSULTA_REALIZADA",
                    observacoes = "Cobrança automática gerada para consulta realizada"
                )
                
                cobrancaRepository.insertCobranca(cobranca)
            } catch (e: Exception) {
                // Log do erro, mas não falha a operação principal
                println("Erro ao criar cobrança automática: ${e.message}")
            }
        }
    }
    
    fun updateConfirmation(
        appointmentId: Long,
        isConfirmed: Boolean?,
        confirmationDate: Date?,
        absenceReason: String?,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.updateConfirmation(appointmentId, isConfirmed, confirmationDate, absenceReason)
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    suspend fun getAppointmentById(id: Long): Appointment? {
        return repository.getAppointmentById(id)
    }
}



