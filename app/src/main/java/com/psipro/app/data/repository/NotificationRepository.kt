package com.psipro.app.data.repository

import com.psipro.app.data.dao.NotificationDao
import com.psipro.app.data.entities.Notification
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject

class NotificationRepository @Inject constructor(
    private val notificationDao: NotificationDao
) {
    
    fun getAllNotifications(): Flow<List<Notification>> = notificationDao.getAllNotifications()
    
    fun getUnreadNotifications(): Flow<List<Notification>> = notificationDao.getUnreadNotifications()
    
    suspend fun getUnreadCount(): Int = notificationDao.getUnreadCount()
    
    suspend fun insertNotification(notification: Notification): Long = 
        notificationDao.insertNotification(notification)
    
    suspend fun markAsRead(notificationId: Long) = 
        notificationDao.markAsRead(notificationId)
    
    suspend fun markAllAsRead() = notificationDao.markAllAsRead()
    
    suspend fun deleteNotification(notification: Notification) = 
        notificationDao.deleteNotification(notification)
    
    suspend fun deleteOldNotifications(daysOld: Int = 30) {
        val cutoffDate = Date(System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000L))
        notificationDao.deleteOldNotifications(cutoffDate)
    }
} 