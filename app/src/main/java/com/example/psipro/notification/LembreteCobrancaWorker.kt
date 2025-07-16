package com.example.psipro.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.psipro.R
import com.example.psipro.data.AppDatabase
import com.example.psipro.utils.WhatsAppUtils
import java.util.Calendar

class LembreteCobrancaWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getInstance(applicationContext)
            val patientDao = db.patientDao()
            val pacientes = patientDao.getAll() // Certifique-se que este método existe
            val hoje = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            val pacientesParaNotificar = pacientes.filter { it.lembreteCobranca && it.diaCobranca == hoje }
            val notificationManager = NotificationManagerCompat.from(applicationContext)
            pacientesParaNotificar.forEachIndexed { index, paciente ->
                val mensagem = WhatsAppUtils.gerarMensagemLembretePaciente(paciente)
                val intent = WhatsAppUtils.intentWhatsApp(applicationContext, paciente.phone, mensagem)
                val pendingIntent = PendingIntent.getActivity(
                    applicationContext,
                    1000 + index,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val builder = NotificationCompat.Builder(applicationContext, "cobranca_channel")
                    .setSmallIcon(R.drawable.ic_error)
                    .setContentTitle("Cobrança para ${paciente.name}")
                    .setContentText("Toque para enviar WhatsApp de cobrança")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                try {
                    notificationManager.notify(3000 + index, builder.build())
                } catch (e: SecurityException) {
                    // Log do erro de permissão
                    android.util.Log.e("LembreteCobrancaWorker", "Erro de permissão para notificação: ${e.message}")
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
} 