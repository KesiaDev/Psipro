package com.example.apppisc.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.apppisc.data.converters.DateConverter
import com.example.apppisc.data.dao.*
import com.example.apppisc.data.entities.*
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Database(
    entities = [
        Patient::class,
        PatientNote::class,
        PatientMessage::class,
        PatientReport::class,
        Appointment::class,
        User::class,
        AuditLog::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
@Singleton
abstract class AppDatabase : RoomDatabase() {
    abstract fun patientDao(): PatientDao
    abstract fun patientNoteDao(): PatientNoteDao
    abstract fun patientMessageDao(): PatientMessageDao
    abstract fun patientReportDao(): PatientReportDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun userDao(): UserDao
    abstract fun auditLogDao(): AuditLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_pisc_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
} 