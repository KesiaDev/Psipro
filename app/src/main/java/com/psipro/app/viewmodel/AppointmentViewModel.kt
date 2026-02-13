package com.psipro.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psipro.app.data.entities.Appointment
import com.psipro.app.data.entities.AppointmentStatus
import com.psipro.app.data.entities.RecurrenceType
import com.psipro.app.data.repository.AppointmentRepository
import com.psipro.app.notification.AppointmentNotificationService
import com.psipro.app.data.repository.CobrancaAgendamentoRepository
import com.psipro.app.data.repository.CobrancaSessaoRepository
import com.psipro.app.data.repository.PatientRepository
import com.psipro.app.data.entities.CobrancaAgendamento
import com.psipro.app.data.entities.CobrancaSessao
import com.psipro.app.data.entities.StatusPagamento
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
    private val cobrancaAgendamentoRepository: CobrancaAgendamentoRepository,
    private val cobrancaSessaoRepository: CobrancaSessaoRepository,
    private val patientRepository: PatientRepository
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
                
                // Se o status for REALIZADO, criar CobrancaSessao (não CobrancaAgendamento)
                if (status == AppointmentStatus.REALIZADO) {
                    val appointment = repository.getAppointmentById(appointmentId)
                    if (appointment != null) {
                        criarCobrancaSessaoDeAgendamento(appointment)
                    }
                }
                
                // Se o status for FALTOU ou CANCELOU, criar CobrancaAgendamento
                if (status == AppointmentStatus.FALTOU || status == AppointmentStatus.CANCELOU) {
                    val appointment = repository.getAppointmentById(appointmentId)
                    if (appointment != null) {
                        criarCobrancaAgendamentoPorEvento(appointment, status)
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

    /**
     * Cria CobrancaSessao quando um agendamento é marcado como REALIZADO.
     * Esta é a cobrança da sessão realizada.
     */
    private fun criarCobrancaSessaoDeAgendamento(appointment: Appointment) {
        viewModelScope.launch {
            try {
                val patientId = appointment.patientId ?: return@launch
                val patient = patientRepository.getPatientById(patientId) ?: return@launch
                val valorSessao = patient.sessionValue ?: appointment.sessionValue ?: 0.0
                
                if (valorSessao <= 0) return@launch
                
                // Calcular número da sessão (próximo número sequencial do paciente)
                // Usar o máximo entre cobranças existentes e anotações existentes
                val cobrancasExistentes = cobrancaSessaoRepository.getByPatientId(patientId).first()
                val maxCobranca = cobrancasExistentes.maxOfOrNull { it.numeroSessao } ?: 0
                // Nota: Se houver anotação sem cobrança, considerar também
                // Por simplicidade, usamos apenas as cobranças (que são criadas junto com anotações)
                val numeroSessao = maxCobranca + 1
                
                val dataVencimento = Calendar.getInstance().apply {
                    time = appointment.date
                    add(Calendar.DAY_OF_MONTH, 7) // Vencimento em 7 dias
                }.time
                
                val cobrancaSessao = CobrancaSessao(
                    patientId = patientId,
                    anotacaoSessaoId = null, // Será preenchido quando anotação for criada
                    appointmentId = appointment.id,
                    numeroSessao = numeroSessao,
                    valor = valorSessao,
                    dataSessao = appointment.date,
                    dataVencimento = dataVencimento,
                    status = StatusPagamento.A_RECEBER,
                    metodoPagamento = "",
                    observacoes = "Sessão realizada - ${appointment.title}",
                    createdAt = Date(),
                    updatedAt = Date()
                )
                
                cobrancaSessaoRepository.insert(cobrancaSessao)
                android.util.Log.d("AppointmentViewModel", "✅ CobrancaSessao criada para agendamento REALIZADO: ID=${appointment.id}, Valor=$valorSessao")
            } catch (e: Exception) {
                android.util.Log.e("AppointmentViewModel", "Erro ao criar CobrancaSessao de agendamento REALIZADO: ${e.message}", e)
            }
        }
    }
    
    /**
     * Cria CobrancaAgendamento para eventos do agendamento (FALTA, CANCELAMENTO).
     */
    private fun criarCobrancaAgendamentoPorEvento(appointment: Appointment, status: AppointmentStatus) {
        viewModelScope.launch {
            try {
                val patientId = appointment.patientId ?: return@launch
                val patient = patientRepository.getPatientById(patientId) ?: return@launch
                val valorSessao = patient.sessionValue ?: appointment.sessionValue ?: 0.0
                
                if (valorSessao <= 0) return@launch
                
                val dataVencimento = Calendar.getInstance().apply {
                    time = appointment.date
                    add(Calendar.DAY_OF_MONTH, 7) // Vencimento em 7 dias
                }.time
                
                val motivo = when (status) {
                    AppointmentStatus.FALTOU -> "FALTA"
                    AppointmentStatus.CANCELOU -> "CANCELAMENTO"
                    else -> "OUTRO"
                }
                
                val observacoes = when (status) {
                    AppointmentStatus.FALTOU -> "Cobrança por falta - ${appointment.title}"
                    AppointmentStatus.CANCELOU -> "Cobrança por cancelamento - ${appointment.title}"
                    else -> "Cobrança - ${appointment.title}"
                }
                
                val cobrancaAgendamento = CobrancaAgendamento(
                    patientId = patientId,
                    appointmentId = appointment.id ?: 0L,
                    valor = valorSessao,
                    dataAgendamento = appointment.date,
                    dataEvento = appointment.date, // Data do evento (falta/cancelamento)
                    dataVencimento = dataVencimento,
                    status = StatusPagamento.A_RECEBER,
                    motivo = motivo,
                    observacoes = observacoes,
                    createdAt = Date(),
                    updatedAt = Date()
                )
                
                cobrancaAgendamentoRepository.insertCobranca(cobrancaAgendamento)
            } catch (e: Exception) {
                android.util.Log.e("AppointmentViewModel", "Erro ao criar CobrancaAgendamento por evento: ${e.message}", e)
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



