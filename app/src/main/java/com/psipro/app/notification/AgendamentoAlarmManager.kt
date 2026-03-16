package com.psipro.app.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import javax.inject.Inject
import java.util.*

class AgendamentoAlarmManager @Inject constructor(
    private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun agendarLembrete(
        titulo: String,
        paciente: String,
        dataHora: Date,
        tipoEvento: String,
        minutosAntes: Int
    ) {
        try {
            val tempoLembrete = Calendar.getInstance().apply {
                time = dataHora
                add(Calendar.MINUTE, -minutosAntes)
            }

            // Só agenda se o lembrete for no futuro
            if (tempoLembrete.time.after(Date())) {
                val intent = Intent(context, AgendamentoNotificationReceiver::class.java).apply {
                    putExtra("titulo", titulo)
                    putExtra("paciente", paciente)
                    putExtra("dataHora", dataHora.time)
                    putExtra("tipoEvento", tipoEvento)
                    putExtra("minutosAntes", minutosAntes)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    generateRequestCode(titulo, dataHora, minutosAntes),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    tempoLembrete.timeInMillis,
                    pendingIntent
                )

                Log.d("AgendamentoAlarmManager", "Lembrete agendado para ${tempoLembrete.time} (${minutosAntes}min antes)")
            } else {
                Log.w("AgendamentoAlarmManager", "Tentativa de agendar lembrete no passado")
            }
        } catch (e: Exception) {
            Log.e("AgendamentoAlarmManager", "Erro ao agendar lembrete", e)
        }
    }

    fun setAlarm(
        context: Context,
        triggerTime: Long,
        appointmentId: Long,
        title: String,
        patientName: String,
        patientPhone: String,
        appointmentDate: Date,
        startTime: String,
        endTime: String,
        reminderMinutes: Int,
        notificationService: AgendamentoNotificationService
    ) {
        try {
            val intent = Intent(context, AgendamentoNotificationReceiver::class.java).apply {
                putExtra("appointmentId", appointmentId)
                putExtra("titulo", title)
                putExtra("paciente", patientName)
                putExtra("telefone", patientPhone)
                putExtra("dataHora", appointmentDate.time)
                putExtra("horaInicio", startTime)
                putExtra("horaFim", endTime)
                putExtra("minutosAntes", reminderMinutes)
                putExtra("tipoEvento", "Consulta")
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                generateRequestCode(title, appointmentDate, reminderMinutes),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )

            Log.d("AgendamentoAlarmManager", "Alarme agendado para ${Date(triggerTime)}")
        } catch (e: Exception) {
            Log.e("AgendamentoAlarmManager", "Erro ao agendar alarme", e)
        }
    }

    fun cancelarLembrete(titulo: String, dataHora: Date, minutosAntes: Int) {
        try {
            val intent = Intent(context, AgendamentoNotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                generateRequestCode(titulo, dataHora, minutosAntes),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.cancel(pendingIntent)
            Log.d("AgendamentoAlarmManager", "Lembrete cancelado")
        } catch (e: Exception) {
            Log.e("AgendamentoAlarmManager", "Erro ao cancelar lembrete", e)
        }
    }

    private fun generateRequestCode(titulo: String, dataHora: Date, minutosAntes: Int): Int {
        return (titulo.hashCode() + dataHora.time + minutosAntes).toInt()
    }
} 