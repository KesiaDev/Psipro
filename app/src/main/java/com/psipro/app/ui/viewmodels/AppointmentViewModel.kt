package com.psipro.app.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psipro.app.data.entities.Appointment
import com.psipro.app.data.entities.AppointmentStatus
import com.psipro.app.data.models.AppointmentSummary
import com.psipro.app.data.repository.AppointmentRepository
import com.psipro.app.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AppointmentViewModel @Inject constructor(
    private val repository: AppointmentRepository
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
                // B3: Conflito validado no backend. Tratar 409 quando API retornar.
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
                // B3: Conflito validado no backend. Tratar 409 quando API retornar.
                repository.updateAppointment(appointment)
                
                // Integração financeira baseada no status
                handleAppointmentStatusChange(appointment)
                
                _appointmentSaved.emit(true)
                loadAppointments(_selectedDate.value)
            } catch (e: Exception) {
                _appointmentError.emit(
                    if (com.psipro.app.utils.isConflict409(e)) "Já existe uma consulta agendada neste horário."
                    else "Erro ao atualizar consulta: ${e.message}"
                )
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
                // B3: Cobrança gerada no backend ao marcar REALIZADO. Apenas exibir confirmação.
                _billingMessage.value = "✅ Sessão realizada!\n💰 A cobrança será registrada automaticamente."
                _showBillingDialog.value = true
            } catch (e: Exception) {
                _error.value = Event("Erro ao processar realização: ${e.message}")
            }
        }
    }
    
    /**
     * Confirma o pagamento da sessão realizada.
     * B3: Cobrança criada no backend. Esta função apenas fecha o fluxo local.
     */
    fun confirmSessionPayment(wasPaid: Boolean, metodoPagamento: String = "") {
        _showPaymentDialog.value = false
        _showBillingDialog.value = true
        _billingMessage.value = "✅ Sessão realizada! A cobrança foi registrada pelo sistema."
    }
    
    fun dismissPaymentDialog() {
        _showPaymentDialog.value = false
        _billingAppointment.value = null
    }
    
    private fun handleNoShowAppointment(appointment: Appointment) {
        viewModelScope.launch {
            try {
                // B3: Cobrança por falta/cancelamento - backend-driven. Apenas exibir mensagem.
                _billingMessage.value = "❌ Paciente faltou na consulta."
                _showBillingDialog.value = true
            } catch (e: Exception) {
                _error.value = Event("Erro ao processar falta: ${e.message}")
            }
        }
    }
    
    private fun handleCancelledAppointment(appointment: Appointment) {
        viewModelScope.launch {
            try {
                // B3: Backend cancela cobrança vinculada ao marcar CANCELADO.
                _billingMessage.value = "❌ Consulta cancelada."
                _showBillingDialog.value = true
            } catch (e: Exception) {
                _error.value = Event("Erro ao processar cancelamento: ${e.message}")
            }
        }
    }
    
    // Função para confirmar geração de cobrança.
    // B3: Cobrança gerada no backend. Apenas fecha o diálogo.
    fun confirmBilling(generateBilling: Boolean) {
        _showBillingDialog.value = false
        _billingAppointment.value = null
    }
    
    // Função para fechar o diálogo de cobrança
    fun dismissBillingDialog() {
        _showBillingDialog.value = false
        _billingAppointment.value = null
        _billingMessage.value = ""
    }
} 



