package com.psipro.app.di

import android.content.Context
import com.psipro.app.data.AppDatabase
import com.psipro.app.security.EncryptionManager
import com.psipro.app.data.dao.AnamneseCampoDao
import com.psipro.app.data.dao.AnamnesePreenchidaDao
import com.psipro.app.data.dao.TipoSessaoDao
import com.psipro.app.data.dao.AutoavaliacaoDao
import com.psipro.app.data.dao.PatientDao
import com.psipro.app.data.dao.PatientNoteDao
import com.psipro.app.data.dao.PatientMessageDao
import com.psipro.app.data.dao.PatientReportDao
import com.psipro.app.data.dao.AppointmentDao
import com.psipro.app.data.dao.FinancialRecordDao
import com.psipro.app.data.dao.CobrancaAgendamentoDao
import com.psipro.app.data.dao.DocumentoDao
import com.psipro.app.data.dao.ArquivoDao
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
            com.psipro.app.data.AppDatabase::class.java,
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
    
    @Provides
    @Singleton
    fun provideCobrancaAgendamentoDao(db: AppDatabase) = db.cobrancaAgendamentoDao()
    
    @Provides
    @Singleton
    fun provideAutoavaliacaoDao(db: AppDatabase): AutoavaliacaoDao = db.autoavaliacaoDao()
    
    @Provides
    fun provideDocumentoDao(database: AppDatabase): DocumentoDao {
        return database.documentoDao()
    }
    
    @Provides
    @Singleton
    fun providePatientDao(db: AppDatabase): PatientDao = db.patientDao()

    @Provides
    @Singleton
    fun providePatientNoteDao(db: AppDatabase): PatientNoteDao = db.patientNoteDao()

    @Provides
    @Singleton
    fun providePatientMessageDao(db: AppDatabase): PatientMessageDao = db.patientMessageDao()

    @Provides
    @Singleton
    fun providePatientReportDao(db: AppDatabase): PatientReportDao = db.patientReportDao()

    @Provides
    @Singleton
    fun provideAppointmentDao(db: AppDatabase): AppointmentDao = db.appointmentDao()

    @Provides
    @Singleton
    fun provideFinancialRecordDao(db: AppDatabase): FinancialRecordDao = db.financialRecordDao()
    
    @Provides
    @Singleton
    fun provideArquivoDao(db: AppDatabase): ArquivoDao = db.arquivoDao()
} 



