package com.example.psipro.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.psipro.data.dao.AppointmentDao
import com.example.psipro.data.dao.PatientDao
import com.example.psipro.data.dao.UserDao
import com.example.psipro.data.dao.PatientNoteDao
import com.example.psipro.data.dao.PatientMessageDao
import com.example.psipro.data.dao.PatientReportDao
import com.example.psipro.data.dao.FinancialRecordDao
import com.example.psipro.data.dao.ProntuarioDao
import com.example.psipro.data.dao.AuditLogDao
import com.example.psipro.data.dao.WhatsAppConversationDao
import com.example.psipro.data.entities.Appointment
import com.example.psipro.data.entities.Patient
import com.example.psipro.data.entities.User
import com.example.psipro.data.entities.PatientNote
import com.example.psipro.data.entities.PatientMessage
import com.example.psipro.data.entities.PatientReport
import com.example.psipro.data.entities.FinancialRecord
import com.example.psipro.data.entities.Prontuario
import com.example.psipro.data.entities.AuditLog
import com.example.psipro.data.entities.WhatsAppConversation
import com.example.psipro.data.converters.DateConverter

@Database(
    entities = [User::class, Patient::class, Appointment::class, PatientNote::class, PatientMessage::class, PatientReport::class, FinancialRecord::class, Prontuario::class, AuditLog::class, WhatsAppConversation::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun patientDao(): PatientDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun patientNoteDao(): PatientNoteDao
    abstract fun patientMessageDao(): PatientMessageDao
    abstract fun patientReportDao(): PatientReportDao
    abstract fun financialRecordDao(): FinancialRecordDao
    abstract fun prontuarioDao(): ProntuarioDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun whatsappConversationDao(): WhatsAppConversationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        @JvmStatic
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "psipro_database"
                ).fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
