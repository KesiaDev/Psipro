package com.example.psipro.di

import android.content.Context
import com.example.psipro.data.AppDatabase
import com.example.psipro.security.EncryptionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return androidx.room.Room.databaseBuilder(
            context,
            com.example.psipro.data.AppDatabase::class.java,
            "app_pisc_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideEncryptionManager(
        @ApplicationContext context: Context
    ): EncryptionManager {
        return EncryptionManager(context)
    }

    @Provides
    fun provideProntuarioDao(db: AppDatabase) = db.prontuarioDao()
} 