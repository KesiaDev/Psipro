package com.psipro.app.notification.di

import com.psipro.app.notification.AutoavaliacaoNotificationService
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AutoavaliacaoEntryPoint {
    fun autoavaliacaoNotificationService(): AutoavaliacaoNotificationService
}
