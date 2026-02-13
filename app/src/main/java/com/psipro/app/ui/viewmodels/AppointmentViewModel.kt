package com.psipro.app.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psipro.app.data.entities.Appointment
import com.psipro.app.data.entities.AppointmentStatus
import com.psipro.app.data.entities.CobrancaAgendamento
import com.psipro.app.data.entities.CobrancaSessao
import com.psipro.app.data.entities.StatusPagamento
import com.psipro.app.data.models.AppointmentSummary
import com.psipro.app.data.repository.AppointmentRepository
import com.psipro.app.data.repository.CobrancaAgendamentoRepository
import com.psipro.app.data.repository.CobrancaSessaoRepository
import com.psipro.app.data.repository.PatientRepository
import com.psipro.app.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AppointmentViewModel @Inject constructor(
    private val repository: AppointmentRepository,
    private val cobrancaAgendamentoRepository: CobrancaAgendamentoRepository,
    private val cobrancaSessaoRepository: CobrancaSessaoRepository,
    private val patientRepository: PatientRepository
) : ViewModel() {

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments

    private val _selectedDate = MutableStateFlow<Date>(Date())
    val selectedDate: StateFlow<Date> = _selectedDate

    private val _error = MutableLiveData<Event<String>>()
    val error: LiveData<Event<String>> = _error

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _appointmentSaved = MutableSharedFlow<Boolean>()
    val appointmentSaved = _appointmentSaved.asSharedFlow()

    private val _appointmentError = MutableSharedFlow<String>()
    val appointmentError = _appointmentError.asSharedFlow()

    // Estados para integração financeira
    private val _showBillingDialog = MutableStateFlow(false)
    val showBillingDialog: StateFlow<Boolean> = _showBillingDialog.asStateFlow()

    private val _billingAppointment = MutableStateFlow<Appointment?>(null)
    val billingAppointment: StateFlow<Appointment?> = _billingAppointment.asStateFlow()

    private val _billingMessage = MutableStateFlow("")
    val billingMessage: StateFlow<String> = _billingMessage.asStateFlow()
    
    // Estado para perguntar se sessão foi paga
    private val _showPaymentDialog = MutableStateFlow(false)
    val showPaymentDialog: StateFlow<Boolean> = _showPaymentDialog.asStateFlow()
    
    private val _sessionValue = MutableStateFlow(0.0)
    val sessionValue: StateFlow<Double> = _sessionValue.asStateFlow()

    val appointmentsByDate = repository.getAllAppointments()

    fun searchAppointments(query: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                repository.searchAppointments(query).collect { appointments ->
                    _appointments.value = appointments
                }
            } catch (e: Exception) {
                _error.value = Event("Erro ao pesquisar consultas: ${e.message}")
            } finally {
                _loading.value = false
            }
        }
    }

    fun getAppointmentsByDate(date: Date): Flow<List<Appointment>> {
        val endDate = Calendar.getInstance().apply {
            time = date
            add(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MILLISECOND, -1)
        }.time

        return repository.getAppointmentsBetweenDates(date, endDate)
    }

    fun getTodaySummary(): Flow<AppointmentSummary> {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val endOfDay = Calendar.getInstance().apply {
            time = today
            add(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MILLISECOND, -1)
        }.time

        return repository.getAppointmentsBetweenDates(today, endOfDay)
            .map { appointments ->
                AppointmentSummary(
                    activePatients = appointments.map { it.patientId }.distinct().size,
                    completedAppointments = appointments.count { it.status == AppointmentStatus.CONFIRMADO },
                    cancelledAppointments = appointments.count { it.status == AppointmentStatus.CANCELOU }
                )
            }
    }

    fun loadAppointments(date: Date) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _selectedDate.value = date
                getAppointmentsByDate(date).collect { appointments ->
                    _appointments.value = appointments
                }
            } catch (e: Exception) {
                _error.value = Event("Erro ao carregar consultas: ${e.message}")
            } finally {
                _loading.value = false
            }
        }
    }

    fun createAppointment(appointment: Appointment) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val hasConflicts = repository.hasConflictingAppointments(
                    appointment.date,
                    appointment.startTime,
                    appointment.endTime,
                    0L
                )
                
                if (hasConflicts) {
                    _appointmentError.emit("Já existe uma consulta agendada neste horário")
                    return@launch
                }

                val appointmentId = repository.insertAppointment(appointment)
                
                // Integração financeira para CONFIRMADO
                if (appointment.status == AppointmentStatus.CONFIRMADO) {
                    handleConfirmedAppointment(appointment.copy(id = appointmentId))
                }
                
                _appointmentSaved.emit(true)
                loadAppointments(_selectedDate.value)
            } catch (e: Exception) {
                _appointmentError.emit("Erro ao criar consulta: ${e.message}")
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateAppointment(appointment: Appointment) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val hasConflicts = repository.hasConflictingAppointments(
                    appointment.date,
                    appointment.startTime,
                    appointment.endTime,
                    appointment.id
                )

                if (hasConflicts) {
                    _appointmentError.emit("Já existe uma consulta agendada neste horário")
                    return@launch
                }

                repository.updateAppointment(appointment)
                
                // Integração financeira baseada no status
                handleAppointmentStatusChange(appointment)
                
                _appointmentSaved.emit(true)
                loadAppointments(_selectedDate.value)
            } catch (e: Exception) {
                _appointmentError.emit("Erro ao atualizar consulta: ${e.message}")
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteAppointment(appointment: Appointment) {
        viewModelScope.launch {
            try {
                _loading.value = true
                repository.deleteAppointment(appointment)
                loadAppointments(_selectedDate.value)
            } catch (e: Exception) {
                _error.value = Event("Erro ao deletar consulta: ${e.message}")
            } finally {
                _loading.value = false
            }
        }
    }

    fun getAppointmentsByDateRange(startDate: Date, endDate: Date): Flow<List<Appointment>> {
        return repository.getAppointmentsBetweenDates(startDate, endDate)
    }

    fun getAppointmentsByPatient(patientId: Long) {
        viewModelScope.launch {
            try {
                _loading.value = true
                repository.getAppointmentsByPatient(patientId).collect { appointments ->
                    _appointments.value = appointments
                }
            } catch (e: Exception) {
                _error.value = Event("Erro ao carregar consultas do paciente: ${e.message}")
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateAppointmentStatus(
        appointmentId: Long, 
        status: AppointmentStatus,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _loading.value = true
                repository.updateAppointmentStatus(appointmentId, status)
                
                // Buscar o agendamento atualizado
                val appointment = repository.getAppointmentById(appointmentId)
                appointment?.let { updatedAppointment ->
                    handleAppointmentStatusChange(updatedAppointment.copy(status = status))
                }
                
                loadAppointments(_selectedDate.value)
                onSuccess()
            } catch (e: Exception) {
                _error.value = Event("Erro ao atualizar status da consulta: ${e.message}")
                onError(e)
            } finally {
                _loading.value = false
            }
        }
    }
    
    // Integração financeira baseada no status
    private fun handleAppointmentStatusChange(appointment: Appointment) {
        when (appointment.status) {
            AppointmentStatus.CONFIRMADO -> {
                handleConfirmedAppointment(appointment)
            }
            AppointmentStatus.REALIZADO -> {
                handleCompletedAppointment(appointment)
            }
            AppointmentStatus.FALTOU -> {
                handleNoShowAppointment(appointment)
            }
            AppointmentStatus.CANCELOU -> {
                handleCancelledAppointment(appointment)
            }
        }
    }
    
    private fun handleConfirmedAppointment(appointment: Appointment) {
        viewModelScope.launch {
            try {
                // CONCEITO: Agendamento NÃO é cobrança
                // Apenas confirma o agendamento, sem criar cobrança financeira
                // A cobrança será criada apenas quando a sessão for marcada como REALIZADA
                _billingMessage.value = "✅ Consulta confirmada!\n📅 Agendamento registrado com sucesso."
            } catch (e: Exception) {
                _error.value = Event("Erro ao processar confirmação: ${e.message}")
            }
        }
    }
    
    private fun handleCompletedAppointment(appointment: Appointment) {
        viewModelScope.launch {
            try {
                val patient = appointment.patientId?.let { patientRepository.getPatientById(it) }
                val valorSessao = patient?.sessionValue ?: appointment.sessionValue ?: 0.0
                
                if (valorSessao > 0) {
                    // CONCEITO: Sessão confirmada É cobrança
                    // Perguntar se foi paga no momento antes de criar
                    _billingAppointment.value = appointment
                    _sessionValue.value = valorSessao
                    _billingMessage.value = "✅ Sessão realizada!\n💰 Valor: R$ ${String.format("%.2f", valorSessao)}\n\nFoi paga agora?"
                    _showPaymentDialog.value = true
                } else {
                    // Sessão sem valor - apenas confirmar
                    _billingMessage.value = "✅ Sessão realizada com sucesso!"
                    _showBillingDialog.value = true
                }
            } catch (e: Exception) {
                _error.value = Event("Erro ao processar realização: ${e.message}")
            }
        }
    }
    
    /**
     * Confirma o pagamento da sessão realizada.
     * Se foiPago = true, marca como PAGA. Caso contrário, cria como PENDENTE.
     */
    fun confirmSessionPayment(wasPaid: Boolean, metodoPagamento: String = "") {
        viewModelScope.launch {
            try {
                val appointment = _billingAppointment.value ?: return@launch
                val patientId = appointment.patientId ?: return@launch
                val patient = patientRepository.getPatientById(patientId) ?: return@launch
                val valorSessao = _sessionValue.value
                
                if (valorSessao <= 0) return@launch
                
                // Calcular número da sessão
                val cobrancasExistentes = cobrancaSessaoRepository.getByPatientId(patientId).first()
                val numeroSessao = (cobrancasExistentes.maxOfOrNull { it.numeroSessao } ?: 0) + 1
                
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
                    status = if (wasPaid) StatusPagamento.PAGO else StatusPagamento.A_RECEBER,
                    dataPagamento = if (wasPaid) Date() else null,
                    metodoPagamento = metodoPagamento,
                    observacoes = "Sessão realizada - ${appointment.title}",
                    createdAt = Date(),
                    updatedAt = Date()
                )
                
                cobrancaSessaoRepository.insert(cobrancaSessao)
                
                val statusText = if (wasPaid) "paga" else "registrada (pendente)"
                _billingMessage.value = "✅ Sessão $statusText!\n💰 R$ ${String.format("%.2f", valorSessao)}"
                _showPaymentDialog.value = false
                _showBillingDialog.value = true
            } catch (e: Exception) {
                _error.value = Event("Erro ao processar pagamento: ${e.message}")
            }
        }
    }
    
    fun dismissPaymentDialog() {
        _showPaymentDialog.value = false
        _billingAppointment.value = null
    }
    
    private fun handleNoShowAppointment(appointment: Appointment) {
        viewModelScope.launch {
            try {
                val patient = appointment.patientId?.let { patientRepository.getPatientById(it) }
                val valorSessao = patient?.sessionValue ?: appointment.sessionValue
                
                if (valorSessao > 0) {
                    _billingAppointment.value = appointment
                    _billingMessage.value = "❌ Paciente faltou na consulta.\n💰 Deseja gerar cobrança pela falta?\nValor: R$ ${String.format("%.2f", valorSessao)}"
                    _showBillingDialog.value = true
                }
            } catch (e: Exception) {
                _error.value = Event("Erro ao processar falta: ${e.message}")
            }
        }
    }
    
    private fun handleCancelledAppointment(appointment: Appointment) {
        viewModelScope.launch {
            try {
                val patient = appointment.patientId?.let { patientRepository.getPatientById(it) }
                val valorSessao = patient?.sessionValue ?: appointment.sessionValue
                
                if (valorSessao > 0) {
                    _billingAppointment.value = appointment
                    _billingMessage.value = "❌ Consulta cancelada.\n💰 Deseja gerar cobrança pelo cancelamento?\nValor: R$ ${String.format("%.2f", valorSessao)}"
                    _showBillingDialog.value = true
                }
            } catch (e: Exception) {
                _error.value = Event("Erro ao processar cancelamento: ${e.message}")
            }
        }
    }
    
    // Função para confirmar geração de cobrança
    fun confirmBilling(generateBilling: Boolean) {
        viewModelScope.launch {
            try {
                val appointment = _billingAppointment.value
                if (appointment != null && generateBilling) {
                    val patient = appointment.patientId?.let { patientRepository.getPatientById(it) }
                    val valorSessao = patient?.sessionValue ?: appointment.sessionValue
                    
                    val dataVencimento = Calendar.getInstance().apply {
                        time = appointment.date
                        add(Calendar.DAY_OF_MONTH, 7) // Vencimento em 7 dias
                    }.time
                    
                    val motivo = when (appointment.status) {
                        AppointmentStatus.FALTOU -> "FALTA"
                        AppointmentStatus.CANCELOU -> "CANCELAMENTO"
                        else -> "OUTRO"
                    }
                    
                    val observacoes = when (appointment.status) {
                        AppointmentStatus.FALTOU -> "Cobrança por falta - ${appointment.title}"
                        AppointmentStatus.CANCELOU -> "Cobrança por cancelamento - ${appointment.title}"
                        else -> "Cobrança - ${appointment.title}"
                    }
                    
                    val cobranca = CobrancaAgendamento(
                        patientId = appointment.patientId ?: 0L,
                        appointmentId = appointment.id,
                        valor = valorSessao,
                        dataAgendamento = appointment.date,
                        dataEvento = appointment.date, // Data do evento (falta/cancelamento)
                        dataVencimento = dataVencimento,
                        motivo = motivo,
                        observacoes = observacoes
                    )
                    
                    cobrancaAgendamentoRepository.insertCobranca(cobranca)
                    
                    _billingMessage.value = "✅ Cobrança gerada com sucesso!\n💰 Valor: R$ ${String.format("%.2f", valorSessao)}"
                }
                
                _showBillingDialog.value = false
                _billingAppointment.value = null
            } catch (e: Exception) {
                _error.value = Event("Erro ao gerar cobrança: ${e.message}")
            }
        }
    }
    
    // Função para fechar o diálogo de cobrança
    fun dismissBillingDialog() {
        _showBillingDialog.value = false
        _billingAppointment.value = null
        _billingMessage.value = ""
    }
} 



