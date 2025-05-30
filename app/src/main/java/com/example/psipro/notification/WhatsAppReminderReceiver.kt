package com.example.psipro.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.psipro.data.AppDatabase
import com.example.psipro.data.entities.PatientMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.util.Date

class WhatsAppReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val phone = intent.getStringExtra("phone") ?: return
        val message = intent.getStringExtra("message") ?: return
        val patientId = intent.getLongExtra("patientId", -1)
        if (patientId == -1L) return

        // Salvar no histórico do paciente (Room exige thread separada)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val msg = PatientMessage(
                    patientId = patientId,
                    texto = message,
                    data = Date()
                )
                db.patientMessageDao().insertMessage(msg)
            } catch (e: Exception) {
                // Logar erro se necessário
            }
        }

        // Abrir WhatsApp com a mensagem pronta
        val url = "https://wa.me/55$phone?text=" + URLEncoder.encode(message, "UTF-8")
        val sendIntent = Intent(Intent.ACTION_VIEW)
        sendIntent.data = Uri.parse(url)
        sendIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        try {
            context.startActivity(sendIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp não encontrado.", Toast.LENGTH_SHORT).show()
        }
    }
} 