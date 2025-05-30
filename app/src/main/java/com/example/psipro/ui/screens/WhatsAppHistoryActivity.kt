package com.example.psipro.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.psipro.data.AppDatabase
import com.example.psipro.data.service.WhatsAppService
import com.example.psipro.ui.viewmodels.PatientMessageViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState

class WhatsAppHistoryActivity : ComponentActivity() {
    private val messageViewModel: PatientMessageViewModel by viewModels()

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

        setContent {
            val messages by messageViewModel.getMessagesForPatient(patientId).collectAsState(initial = emptyList())
            WhatsAppHistoryScreen(
                patientName = patientName,
                conversations = messages,
                onBackClick = { finish() },
                onSendMessage = { message ->
                    // Aqui você pode buscar o telefone do paciente se necessário
                    // e chamar messageViewModel.insertMessage(...)
                }
            )
        }
    }
} 