package com.psipro.app.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.psipro.app.data.AppDatabase
import com.psipro.app.data.service.WhatsAppService
import com.psipro.app.ui.viewmodels.PatientMessageViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import com.psipro.app.ui.compose.PsiproTheme

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
            PsiproTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
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
    }
} 



