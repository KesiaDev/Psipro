package com.psipro.app.sync.di

import com.psipro.app.sync.api.BackendApiService
import com.psipro.app.sync.BackendAuthManager
import com.psipro.app.sync.BackendSessionStore
import com.psipro.app.sync.SyncPatientsManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SyncEntryPoint {
    fun backendAuthManager(): BackendAuthManager
    fun backendApiService(): BackendApiService
    fun sessionStore(): BackendSessionStore
    fun syncPatientsManager(): SyncPatientsManager
}

