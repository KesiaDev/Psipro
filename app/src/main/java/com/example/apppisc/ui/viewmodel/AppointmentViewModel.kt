package com.example.apppisc.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apppisc.data.dao.AppointmentDao
import com.example.apppisc.data.dao.PatientDao
import com.example.apppisc.data.service.WhatsAppService
import com.example.apppisc.data.dao.WhatsAppConversationDao
import com.example.apppisc.data.model.WhatsAppConversation
import com.example.apppisc.data.entities.Appointment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppointmentViewModel(
    private val appointmentDao: AppointmentDao,
    private val patientDao: PatientDao,
    private val whatsAppService: WhatsAppService,
    private val conversationDao: WhatsAppConversationDao
) : ViewModel() {

    private val _conversations = MutableStateFlow<List<WhatsAppConversation>>(emptyList())
    val conversations: StateFlow<List<WhatsAppConversation>> = _conversations.asStateFlow()

    fun loadConversations(patientId: Long) {
        viewModelScope.launch {
            whatsAppService.getConversationsForPatient(patientId)
                .collect { conversations ->
                    _conversations.value = conversations
                }
        }
    }

    fun sendWhatsAppMessage(phoneNumber: String, message: String, patientId: Long) {
        viewModelScope.launch {
            whatsAppService.sendMessage(phoneNumber, message, patientId)
        }
    }

    fun sendConfirmationMessage(appointment: Appointment) {
        viewModelScope.launch {
            val patient = patientDao.getPatientById(appointment.patientId)
            patient?.let {
                val message = whatsAppService.formatConfirmationMessage(appointment)
                sendWhatsAppMessage(it.phone, message, it.id)
            }
        }
    }

    fun sendReminderMessage(appointment: Appointment) {
        viewModelScope.launch {
            val patient = patientDao.getPatientById(appointment.patientId)
            patient?.let {
                val message = whatsAppService.formatReminderMessage(appointment)
                sendWhatsAppMessage(it.phone, message, it.id)
            }
        }
    }
} 