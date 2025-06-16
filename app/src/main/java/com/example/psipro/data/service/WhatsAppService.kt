package com.example.psipro.data.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.psipro.data.dao.WhatsAppConversationDao
import com.example.psipro.data.entities.WhatsAppConversation
import com.example.psipro.data.entities.MessageDirection
import com.example.psipro.data.entities.MessageStatus
import com.example.psipro.data.entities.Appointment
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import java.util.Date

class WhatsAppService(
    private val context: Context,
    private val conversationDao: WhatsAppConversationDao
) {
    companion object {
        private const val WHATSAPP_PACKAGE = "com.whatsapp"
        private const val WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b"
    }

    suspend fun sendMessage(phoneNumber: String, message: String, patientId: Long) {
        val formattedNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$formattedNumber&text=${Uri.encode(message)}")
        
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage(WHATSAPP_PACKAGE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        // Salva a mensagem enviada
        val conversation = WhatsAppConversation(
            patientId = patientId,
            message = message,
            sentAt = Date()
        )

        // Inicia o WhatsApp e salva a conversa
        context.startActivity(intent)
        conversationDao.insertConversation(conversation)
    }

    fun getConversationsForPatient(patientId: Long): Flow<List<WhatsAppConversation>> {
        return conversationDao.getConversationsByPatient(patientId)
    }

    fun getConversationsInDateRange(patientId: Long, startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<WhatsAppConversation>> {
        return conversationDao.getConversationsInDateRange(
            patientId,
            startDate.toEpochSecond(java.time.ZoneOffset.UTC) * 1000,
            endDate.toEpochSecond(java.time.ZoneOffset.UTC) * 1000
        )
    }

    fun formatConfirmationMessage(appointment: Appointment): String {
        val dateFormatted = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).format(appointment.date)
        val timeFormatted = "${appointment.startTime} - ${appointment.endTime}"
        return buildString {
            append("Olá! Confirmando sua consulta:\n\n")
            append("Data: $dateFormatted\n")
            append("Horário: $timeFormatted\n")
            append("Paciente: ${appointment.patientName}\n")
            if (!appointment.description.isNullOrBlank()) {
                append("\nObservações: ${appointment.description}")
            }
            append("\n\nPor favor, confirme sua presença respondendo esta mensagem.")
        }
    }

    fun formatReminderMessage(appointment: Appointment): String {
        val dateFormatted = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).format(appointment.date)
        val timeFormatted = "${appointment.startTime} - ${appointment.endTime}"
        return buildString {
            append("Olá! Lembrete da sua consulta:\n\n")
            append("Data: $dateFormatted\n")
            append("Horário: $timeFormatted\n")
            append("Paciente: ${appointment.patientName}\n")
            if (!appointment.description.isNullOrBlank()) {
                append("\nObservações: ${appointment.description}")
            }
            append("\n\nNão se esqueça da sua consulta!")
        }
    }

    fun isWhatsAppInstalled(): Boolean {
        val packageManager = context.packageManager
        return try {
            packageManager.getPackageInfo(WHATSAPP_PACKAGE, 0)
            true
        } catch (e: Exception) {
            try {
                packageManager.getPackageInfo(WHATSAPP_BUSINESS_PACKAGE, 0)
                true
            } catch (e: Exception) {
                false
            }
        }
    }
} 