package com.psipro.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class AgendamentoNotificationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationService: AgendamentoNotificationService

    override fun onReceive(context: Context, intent: Intent) {
        try {
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