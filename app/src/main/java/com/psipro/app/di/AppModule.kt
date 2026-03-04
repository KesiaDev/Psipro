package com.psipro.app.di

import android.content.Context
import com.psipro.app.data.AppDatabase

import com.psipro.app.data.repository.AppointmentRepository
import com.psipro.app.data.repository.PatientRepository
import com.psipro.app.data.repository.CobrancaAgendamentoRepository
import com.psipro.app.data.repositories.PatientMessageRepository
import com.psipro.app.data.repository.FinancialRecordRepository
import com.psipro.app.data.repository.AutoavaliacaoRepository
import com.psipro.app.data.repository.DocumentoRepository
import com.psipro.app.data.repository.ArquivoRepository
import com.psipro.app.data.dao.AppointmentDao
import com.psipro.app.data.dao.PatientDao
import com.psipro.app.data.dao.PatientMessageDao
import com.psipro.app.data.dao.FinancialRecordDao
import com.psipro.app.data.dao.CobrancaAgendamentoDao
import com.psipro.app.data.dao.AutoavaliacaoDao
import com.psipro.app.data.dao.DocumentoDao
import com.psipro.app.data.dao.ArquivoDao
import com.psipro.app.sync.SyncAppointmentsManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideAppointmentRepository(
        dao: AppointmentDao,
        syncManager: SyncAppointmentsManager
    ): AppointmentRepository {
        return AppointmentRepository(dao, syncManager)
    }

    @Provides
    @Singleton
    fun providePatientRepository(dao: PatientDao): PatientRepository {
        return PatientRepository(dao)
    }

    @Provides
    @Singleton
    fun providePatientMessageRepository(dao: PatientMessageDao): PatientMessageRepository {
        return PatientMessageRepository(dao)
    }



    @Provides
    @Singleton
    fun provideFinancialRecordRepository(dao: FinancialRecordDao): FinancialRecordRepository = FinancialRecordRepository(dao)
    
    @Provides
    @Singleton
    fun provideCobrancaAgendamentoRepository(dao: CobrancaAgendamentoDao): CobrancaAgendamentoRepository = CobrancaAgendamentoRepository(dao)
    
    @Provides
    fun provideAutoavaliacaoRepository(
        autoavaliacaoDao: AutoavaliacaoDao
    ): AutoavaliacaoRepository {
        return AutoavaliacaoRepository(autoavaliacaoDao)
    }
    
    @Provides
    fun provideDocumentoRepository(
        documentoDao: DocumentoDao
    ): DocumentoRepository {
        return DocumentoRepository(documentoDao)
    }
    
    @Provides
    @Singleton
    fun provideArquivoRepository(
        arquivoDao: ArquivoDao
    ): ArquivoRepository {
        return ArquivoRepository(arquivoDao)
    }
    
    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context
    
    @Provides
    @Singleton
    fun provideInsightProvider(): com.psipro.app.ui.screens.home.InsightProvider {
        return com.psipro.app.ui.screens.home.LocalInsightProvider()
    }
} 



