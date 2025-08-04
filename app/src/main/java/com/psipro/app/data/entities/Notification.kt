package com.psipro.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val message: String,
    val type: NotificationType,
    val appointmentId: Long? = null,
    val patientId: Long? = null,
    val isRead: Boolean = false,
    val createdAt: Date = Date(),
    val scheduledFor: Date? = null
)

enum class NotificationType {
    APPOINTMENT_REMINDER,
    PAYMENT_PENDING,
    DAILY_REPORT,
    BIRTHDAY_REMINDER,
    SYSTEM_MESSAGE
} 