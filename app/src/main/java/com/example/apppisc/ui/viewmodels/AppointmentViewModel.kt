package com.example.apppisc.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apppisc.data.entities.Appointment
import com.example.apppisc.data.entities.AppointmentStatus
import com.example.apppisc.data.models.AppointmentSummary
import com.example.apppisc.data.repository.AppointmentRepository
import com.example.apppisc.data.repository.PatientRepository
import com.example.apppisc.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AppointmentViewModel @Inject constructor(
    private val repository: AppointmentRepository,
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
                    appointmentsCount = appointments.count { it.status == AppointmentStatus.SCHEDULED },
                    activePatients = appointments.map { it.patientId }.distinct().size,
                    completedAppointments = appointments.count { it.status == AppointmentStatus.COMPLETED },
                    cancelledAppointments = appointments.count { it.status == AppointmentStatus.CANCELLED }
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

                repository.insertAppointment(appointment)
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

    fun updateAppointmentStatus(appointmentId: Long, status: AppointmentStatus) {
        viewModelScope.launch {
            try {
                _loading.value = true
                repository.updateAppointmentStatus(appointmentId, status)
                loadAppointments(_selectedDate.value)
            } catch (e: Exception) {
                _error.value = Event("Erro ao atualizar status da consulta: ${e.message}")
            } finally {
                _loading.value = false
            }
        }
    }
} 