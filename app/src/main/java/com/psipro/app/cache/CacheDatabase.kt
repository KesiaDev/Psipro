package com.psipro.app.cache

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.psipro.app.cache.DateConverter

@Database(
    entities = [
        PatientCache::class,
        AppointmentCache::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class CacheDatabase : RoomDatabase() {
    abstract fun patientCacheDao(): PatientCacheDao
    abstract fun appointmentCacheDao(): AppointmentCacheDao
} 



