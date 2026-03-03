package com.psipro.app.notification.di

import com.psipro.app.notification.AgendamentoNotificationService
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AgendamentoEntryPoint {
    fun agendamentoNotificationService(): AgendamentoNotificationService
}
