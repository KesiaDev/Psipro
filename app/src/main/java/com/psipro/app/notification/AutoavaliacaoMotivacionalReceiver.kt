package com.psipro.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.psipro.app.utils.AIMotivationalService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AutoavaliacaoMotivacionalReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var notificationService: AutoavaliacaoNotificationService
    
    private val aiService = AIMotivationalService()
    
    override fun onReceive(context: Context, intent: Intent) {
        // Gera uma mensagem motivacional personalizada
        val motivationalMessage = aiService.generateDefaultMessage()
        
        // Mostra a notificação
        notificationService.showMotivationalNotification(motivationalMessage)
    }
} 



