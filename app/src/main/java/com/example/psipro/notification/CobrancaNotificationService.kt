package com.example.psipro.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.psipro.R
import com.example.psipro.data.repository.CobrancaSessaoRepository
import com.example.psipro.data.entities.StatusPagamento
import javax.inject.Inject

class CobrancaNotificationService @Inject constructor(
    private val context: Context,
    private val cobrancaRepository: CobrancaSessaoRepository
) {

    companion object {
        private const val CHANNEL_ID = "cobranca_channel"
        private const val NOTIFICATION_ID_VENCIDA = 1001
        private const val NOTIFICATION_ID_PENDENTE = 1002
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Cobranças"
            val descriptionText = "Notificações de cobranças e pagamentos"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun notificarCobrancaVencida(quantidade: Int) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_error)
            .setContentTitle("Cobranças Vencidas")
            .setContentText("Você tem $quantidade cobrança(s) vencida(s)")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(NOTIFICATION_ID_VENCIDA, builder.build())
    }

    fun notificarCobrancasPendentes(quantidade: Int) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_schedule)
            .setContentTitle("Cobranças Pendentes")
            .setContentText("Você tem $quantidade cobrança(s) pendente(s)")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(NOTIFICATION_ID_PENDENTE, builder.build())
    }
} 