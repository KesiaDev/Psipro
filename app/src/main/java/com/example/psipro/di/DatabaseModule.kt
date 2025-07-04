package com.example.psipro.di

import android.content.Context
import com.example.psipro.data.AppDatabase
import com.example.psipro.security.EncryptionManager
import com.example.psipro.data.dao.AnamneseCampoDao
import com.example.psipro.data.dao.AnamnesePreenchidaDao
import com.example.psipro.data.dao.TipoSessaoDao
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
        ).fallbackToDestructiveMigration()
         .build()
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

    @Provides
    @Singleton
    fun provideAnamneseCampoDao(db: AppDatabase): AnamneseCampoDao = db.anamneseCampoDao()

    @Provides
    @Singleton
    fun provideAnamnesePreenchidaDao(db: AppDatabase): AnamnesePreenchidaDao = db.anamnesePreenchidaDao()

    @Provides
    @Singleton
    fun provideAnamneseModelDao(db: AppDatabase) = db.anamneseModelDao()

    @Provides
    @Singleton
    fun provideHistoricoMedicoDao(db: AppDatabase) = db.historicoMedicoDao()

    @Provides
    @Singleton
    fun provideHistoricoFamiliarDao(db: AppDatabase) = db.historicoFamiliarDao()

    @Provides
    @Singleton
    fun provideVidaEmocionalDao(db: AppDatabase) = db.vidaEmocionalDao()

    @Provides
    @Singleton
    fun provideObservacoesClinicasDao(db: AppDatabase) = db.observacoesClinicasDao()

    @Provides
    @Singleton
    fun provideAnotacaoSessaoDao(db: AppDatabase) = db.anotacaoSessaoDao()

    @Provides
    @Singleton
    fun provideCobrancaSessaoDao(db: AppDatabase) = db.cobrancaSessaoDao()

    @Provides
    @Singleton
    fun provideTipoSessaoDao(db: AppDatabase): TipoSessaoDao = db.tipoSessaoDao()
} 