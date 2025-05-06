package com.example.apppisc.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.apppisc.R
import com.example.apppisc.data.entities.Appointment
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppointmentNotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Appointment Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for appointment reminders"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleAppointmentReminder(appointment: Appointment) {
        val appointmentId = appointment.id ?: return
        
        // Só agenda se o lembrete estiver habilitado
        if (!appointment.reminderEnabled) return

        val calendar = Calendar.getInstance().apply {
            time = appointment.date
            val (hours, minutes) = appointment.startTime.split(":").map { it.toInt() }
            set(Calendar.HOUR_OF_DAY, hours)
            set(Calendar.MINUTE, minutes)
            add(Calendar.MINUTE, -appointment.reminderMinutes)
        }

        val intent = Intent(context, AppointmentReminderReceiver::class.java).apply {
            putExtra("appointment_id", appointmentId)
            putExtra("title", appointment.title)
            putExtra("time", appointment.startTime)
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            appointmentId.toInt(),
            intent,
            flags
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelAppointmentReminder(appointmentId: Long?) {
        appointmentId?.let { id ->
            val intent = Intent(context, AppointmentReminderReceiver::class.java)
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_NO_CREATE
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                id.toInt(),
                intent,
                flags
            )
            
            pendingIntent?.let { pi ->
                alarmManager.cancel(pi)
                pi.cancel()
            }
            notificationManager.cancel(id.toInt())
        }
    }

    companion object {
        const val CHANNEL_ID = "appointment_reminders"
    }
} 