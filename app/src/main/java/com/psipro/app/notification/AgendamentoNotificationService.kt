package com.psipro.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.psipro.app.R
import com.psipro.app.ui.AppointmentScheduleActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.*

class AgendamentoNotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = "agendamento_channel"
    private val channelName = "Lembretes de Agendamento"
    private val channelDescription = "Notificações de lembretes de consultas e agendamentos"

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = channelDescription
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun mostrarNotificacaoAgendamento(
        titulo: String,
        paciente: String,
        dataHora: Date,
        tipoEvento: String,
        minutosAntes: Int
    ) {
        val intent = Intent(context, AppointmentScheduleActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dateFormatter = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR"))
        val tempoTexto = when (minutosAntes) {
            15 -> "15 minutos"
            30 -> "30 minutos"
            45 -> "45 minutos"
            else -> "$minutosAntes minutos"
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("⏰ Lembrete de $tipoEvento")
            .setContentText("$paciente - $tempoTexto")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("""
                    📅 $tipoEvento: $titulo
                    👤 Paciente: $paciente
                    🕐 Horário: ${dateFormatter.format(dataHora)}
                    ⏰ Lembrete: $tempoTexto antes
                    
                    Toque para ver detalhes
                """.trimIndent()))
            .setSmallIcon(R.drawable.ic_notification_calendar)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        notificationManager.notify(generateNotificationId(titulo, dataHora, minutosAntes), notification)
    }

    fun cancelarNotificacaoAgendamento(titulo: String, dataHora: Date, minutosAntes: Int) {
        val notificationId = generateNotificationId(titulo, dataHora, minutosAntes)
        notificationManager.cancel(notificationId)
    }

    private fun generateNotificationId(titulo: String, dataHora: Date, minutosAntes: Int): Int {
        return (titulo.hashCode() + dataHora.time + minutosAntes).toInt()
    }
} 