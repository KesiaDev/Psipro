package com.psipro.app.notification

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.EntryPointAccessors
import java.util.*
import com.psipro.app.notification.di.AgendamentoEntryPoint

class AgendamentoNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        try {
            val app = context.applicationContext as Application
            val entryPoint = EntryPointAccessors.fromApplication(app, AgendamentoEntryPoint::class.java)
            val notificationService = entryPoint.agendamentoNotificationService()

            val titulo = intent.getStringExtra("titulo") ?: "Agendamento"
            val paciente = intent.getStringExtra("paciente") ?: "Paciente"
            val dataHora = Date(intent.getLongExtra("dataHora", 0))
            val tipoEvento = intent.getStringExtra("tipoEvento") ?: "Evento"
            val minutosAntes = intent.getIntExtra("minutosAntes", 15)

            Log.d("AgendamentoNotificationReceiver", "Mostrando notificação para $titulo")

            notificationService.mostrarNotificacaoAgendamento(
                titulo = titulo,
                paciente = paciente,
                dataHora = dataHora,
                tipoEvento = tipoEvento,
                minutosAntes = minutosAntes
            )
        } catch (e: Exception) {
            Log.e("AgendamentoNotificationReceiver", "Erro ao processar notificação", e)
        }
    }
} 