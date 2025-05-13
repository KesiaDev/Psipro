package com.example.apppisc.di

import android.content.Context
import com.example.apppisc.data.AppDatabase
import com.example.apppisc.security.EncryptionManager
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
            com.example.apppisc.data.AppDatabase::class.java,
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