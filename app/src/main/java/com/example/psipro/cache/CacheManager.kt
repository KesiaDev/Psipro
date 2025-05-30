package com.example.psipro.cache

import android.content.Context
import androidx.room.Room
import com.example.psipro.data.AppDatabase
import com.example.psipro.data.entities.Patient
import com.example.psipro.data.entities.Appointment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CacheManager @Inject constructor(
    private val context: Context
) {
    private val cacheDatabase = Room.databaseBuilder(
        context,
        CacheDatabase::class.java,
        "cache_database"
    ).build()
    
    private val cacheExpiration = TimeUnit.HOURS.toMillis(1) // 1 hora
    
    suspend fun cachePatients(patients: List<Patient>) {
        cacheDatabase.patientCacheDao().insertAll(patients.map { 
            PatientCache(
                id = it.id,
                name = it.name,
                cpf = it.cpf,
                phone = it.phone,
                lastUpdated = System.currentTimeMillis()
            )
        })
    }
    
    suspend fun cacheAppointments(appointments: List<Appointment>) {
        cacheDatabase.appointmentCacheDao().insertAll(appointments.map {
            AppointmentCache(
                id = it.id,
                patientId = it.patientId,
                date = it.date,
                startTime = it.startTime,
                endTime = it.endTime,
                status = it.status,
                lastUpdated = System.currentTimeMillis()
            )
        })
    }
    
    fun getCachedPatients(): Flow<List<Patient>> = flow {
        val cachedPatients = cacheDatabase.patientCacheDao().getAll()
        val currentTime = System.currentTimeMillis()
        
        if (cachedPatients.isNotEmpty() && 
            currentTime - cachedPatients.first().lastUpdated < cacheExpiration) {
            emit(cachedPatients.map {
                Patient(
                    id = it.id,
                    name = it.name,
                    cpf = it.cpf,
                    birthDate = java.util.Date(), // valor padrão, ajuste se necessário
                    phone = it.phone,
                    email = "",
                    cep = "",
                    endereco = "",
                    numero = "",
                    bairro = "",
                    cidade = "",
                    estado = "",
                    complemento = "",
                    sessionValue = 0.0,
                    diaCobranca = 1,
                    lembreteCobranca = false,
                    clinicalHistory = null,
                    medications = null,
                    allergies = null,
                    isEncrypted = false,
                    createdAt = java.util.Date(),
                    updatedAt = java.util.Date()
                )
            })
        } else {
            emit(emptyList())
        }
    }
    
    fun getCachedAppointments(): Flow<List<Appointment>> = flow {
        val cachedAppointments = cacheDatabase.appointmentCacheDao().getAll()
        val currentTime = System.currentTimeMillis()
        
        if (cachedAppointments.isNotEmpty() && 
            currentTime - cachedAppointments.first().lastUpdated < cacheExpiration) {
            emit(cachedAppointments.map {
                Appointment(
                    id = it.id,
                    patientId = it.patientId,
                    date = it.date,
                    startTime = it.startTime,
                    endTime = it.endTime,
                    status = it.status,
                    title = "",
                    patientName = "",
                    patientPhone = "",
                    description = null,
                    reminderEnabled = false,
                    reminderMinutes = 30,
                    isConfirmed = null,
                    confirmationDate = null,
                    absenceReason = null,
                    createdAt = java.util.Date(),
                    updatedAt = java.util.Date()
                )
            })
        } else {
            emit(emptyList())
        }
    }
    
    suspend fun clearCache() {
        cacheDatabase.clearAllTables()
    }
    
    suspend fun clearExpiredCache() {
        val currentTime = System.currentTimeMillis()
        cacheDatabase.patientCacheDao().deleteExpired(currentTime - cacheExpiration)
        cacheDatabase.appointmentCacheDao().deleteExpired(currentTime - cacheExpiration)
    }
} 