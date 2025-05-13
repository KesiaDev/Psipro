package com.example.apppisc.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.apppisc.data.AppDatabase
import com.example.apppisc.data.service.WhatsAppService
import com.example.apppisc.ui.viewmodel.AppointmentViewModel

class WhatsAppHistoryActivity : ComponentActivity() {
    private lateinit var appointmentViewModel: AppointmentViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val patientId = intent.getLongExtra("PATIENT_ID", -1)
        val patientName = intent.getStringExtra("PATIENT_NAME") ?: ""

        // Obter DAOs e Service manualmente
        val db = AppDatabase.getInstance(applicationContext)
        val appointmentDao = db.appointmentDao()
        val patientDao = db.patientDao()
        val conversationDao = db.whatsappConversationDao()
        val whatsAppService = WhatsAppService(applicationContext, conversationDao)

        appointmentViewModel = AppointmentViewModel(
            appointmentDao,
            patientDao,
            whatsAppService,
            conversationDao
        )
        appointmentViewModel.loadConversations(patientId)
        setContent {
            val conversations = appointmentViewModel.conversations.value
            WhatsAppHistoryScreen(
                patientName = patientName,
                conversations = conversations,
                onBackClick = { finish() },
                onSendMessage = { message ->
                    // Aqui você pode buscar o telefone do paciente se necessário
                    // e chamar appointmentViewModel.sendWhatsAppMessage(...)
                }
            )
        }
    }
} 