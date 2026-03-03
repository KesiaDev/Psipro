package com.psipro.app.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.psipro.app.R
import com.psipro.app.data.AppDatabase
import com.psipro.app.ui.screens.FinanceiroDashboardActivity
import java.util.Calendar

class LembreteCobrancaWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getInstance(applicationContext)
            val patientDao = db.patientDao()
            val pacientes = patientDao.getAll()
            val hoje = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            val pacientesParaNotificar = pacientes.filter { it.lembreteCobranca && it.diaCobranca == hoje }
            val notificationManager = NotificationManagerCompat.from(applicationContext)
            pacientesParaNotificar.forEachIndexed { index, paciente ->
                val intent = Intent(applicationContext, FinanceiroDashboardActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    applicationContext,
                    1000 + index,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val builder = NotificationCompat.Builder(applicationContext, "cobranca_channel")
                    .setSmallIcon(R.drawable.ic_error)
                    .setContentTitle("Cobrança pendente: ${paciente.name}")
                    .setContentText("Toque para abrir o financeiro")
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



