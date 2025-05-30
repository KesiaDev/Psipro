package com.example.psipro.cache

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientCacheDao {
    @Query("SELECT * FROM patient_cache")
    suspend fun getAll(): List<PatientCache>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(patients: List<PatientCache>)
    
    @Query("DELETE FROM patient_cache WHERE lastUpdated < :timestamp")
    suspend fun deleteExpired(timestamp: Long)
    
    @Query("DELETE FROM patient_cache")
    suspend fun deleteAll()
}

@Dao
interface AppointmentCacheDao {
    @Query("SELECT * FROM appointment_cache")
    suspend fun getAll(): List<AppointmentCache>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(appointments: List<AppointmentCache>)
    
    @Query("DELETE FROM appointment_cache WHERE lastUpdated < :timestamp")
    suspend fun deleteExpired(timestamp: Long)
    
    @Query("DELETE FROM appointment_cache")
    suspend fun deleteAll()
} 