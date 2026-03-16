package com.psipro.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psipro.app.data.entities.Notification
import com.psipro.app.data.entities.NotificationType
import com.psipro.app.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {
    
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()
    
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()
    
    init {
        loadNotifications()
        loadUnreadCount()
    }
    
    private fun loadNotifications() {
        viewModelScope.launch {
            notificationRepository.getAllNotifications().collect { notifications ->
                _notifications.value = notifications
            }
        }
    }
    
    private fun loadUnreadCount() {
        viewModelScope.launch {
            _unreadCount.value = notificationRepository.getUnreadCount()
        }
    }
    
    fun markAsRead(notificationId: Long) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
            loadUnreadCount()
        }
    }
    
    fun markAllAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead()
            loadUnreadCount()
        }
    }
    
    fun deleteNotification(notification: Notification) {
        viewModelScope.launch {
            notificationRepository.deleteNotification(notification)
        }
    }
    
    fun createAppointmentReminder(
        title: String,
        message: String,
        appointmentId: Long? = null,
        patientId: Long? = null,
        scheduledFor: Date? = null
    ) {
        viewModelScope.launch {
            val notification = Notification(
                title = title,
                message = message,
                type = NotificationType.APPOINTMENT_REMINDER,
                appointmentId = appointmentId,
                patientId = patientId,
                scheduledFor = scheduledFor
            )
            notificationRepository.insertNotification(notification)
            loadUnreadCount()
        }
    }
    
    fun createPaymentReminder(
        title: String,
        message: String,
        patientId: Long? = null
    ) {
        viewModelScope.launch {
            val notification = Notification(
                title = title,
                message = message,
                type = NotificationType.PAYMENT_PENDING,
                patientId = patientId
            )
            notificationRepository.insertNotification(notification)
            loadUnreadCount()
        }
    }
    
    fun createDailyReport(
        title: String,
        message: String
    ) {
        viewModelScope.launch {
            val notification = Notification(
                title = title,
                message = message,
                type = NotificationType.DAILY_REPORT
            )
            notificationRepository.insertNotification(notification)
            loadUnreadCount()
        }
    }
} 