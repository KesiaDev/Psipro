package com.psipro.app.notification

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.psipro.app.notification.di.AutoavaliacaoEntryPoint
import com.psipro.app.utils.AIMotivationalService
import dagger.hilt.android.EntryPointAccessors

class AutoavaliacaoMotivacionalReceiver : BroadcastReceiver() {

    private val aiService = AIMotivationalService()

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as Application
        val entryPoint = EntryPointAccessors.fromApplication(app, AutoavaliacaoEntryPoint::class.java)
        val notificationService = entryPoint.autoavaliacaoNotificationService()

        val motivationalMessage = aiService.generateDefaultMessage()
        notificationService.showMotivationalNotification(motivationalMessage)
    }
} 



