package com.example.apppisc.di

import android.content.Context
import com.example.apppisc.data.AppDatabase
import com.example.apppisc.data.dao.AppointmentDao
import com.example.apppisc.data.dao.PatientDao
import com.example.apppisc.data.dao.PatientNoteDao
import com.example.apppisc.data.dao.PatientMessageDao
import com.example.apppisc.data.dao.PatientReportDao
import com.example.apppisc.data.dao.FinancialRecordDao
import com.example.apppisc.data.repository.AppointmentRepository
import com.example.apppisc.data.repository.PatientRepository
import com.example.apppisc.data.repositories.PatientMessageRepository
import com.example.apppisc.data.repository.FinancialRecordRepository
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
    fun provideAppointmentRepository(dao: AppointmentDao): AppointmentRepository {
        return AppointmentRepository(dao)
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
    fun provideFinancialRecordRepository(dao: FinancialRecordDao): FinancialRecordRepository = FinancialRecordRepository(dao)
} 