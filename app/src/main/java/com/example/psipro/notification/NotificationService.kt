package com.example.psipro.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.psipro.R
import com.example.psipro.data.entities.Appointment
import com.example.psipro.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NotificationService : FirebaseMessagingService() {
    
    @Inject
    lateinit var notificationManager: NotificationManager
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }
    
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        message.notification?.let { notification ->
            showNotification(
                title = notification.title ?: "PsiPro",
                message = notification.body ?: "",
                data = message.data
            )
        }
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO: Enviar token para o servidor
    }
    
    fun scheduleAppointmentReminder(appointment: Appointment) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("appointment_id", appointment.id)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            appointment.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_APPOINTMENTS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Lembrete de Consulta")
            .setContentText("Você tem uma consulta em 30 minutos")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        notificationManager.notify(appointment.id.toInt(), notification)
    }
    
    fun showNotification(title: String, message: String, data: Map<String, String> = emptyMap()) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            data.forEach { (key, value) -> putExtra(key, value) }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_DEFAULT)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_DEFAULT,
                    "Notificações Gerais",
                    NotificationManager.IMPORTANCE_DEFAULT
                ),
                NotificationChannel(
                    CHANNEL_APPOINTMENTS,
                    "Lembretes de Consulta",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    enableVibration(true)
                    enableLights(true)
                }
            )
            
            notificationManager.createNotificationChannels(channels)
        }
    }
    
    companion object {
        private const val CHANNEL_DEFAULT = "default"
        private const val CHANNEL_APPOINTMENTS = "appointments"
    }
} 