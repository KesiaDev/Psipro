package com.example.psipro.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
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
import com.example.psipro.data.entities.AnamneseModel
import com.example.psipro.data.entities.AnamneseCampo
import com.example.psipro.data.entities.AnamnesePreenchida
import com.example.psipro.data.dao.AnamneseModelDao
import com.example.psipro.data.dao.AnamneseCampoDao
import com.example.psipro.data.dao.AnamnesePreenchidaDao
import com.example.psipro.data.converters.DateConverter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [User::class, Patient::class, Appointment::class, PatientNote::class, PatientMessage::class, PatientReport::class, FinancialRecord::class, Prontuario::class, AuditLog::class, WhatsAppConversation::class, AnamneseModel::class, AnamneseCampo::class, AnamnesePreenchida::class],
    version = 9,
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
    abstract fun anamneseModelDao(): AnamneseModelDao
    abstract fun anamneseCampoDao(): AnamneseCampoDao
    abstract fun anamnesePreenchidaDao(): AnamnesePreenchidaDao

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
                 .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed dos modelos prontos
                        GlobalScope.launch(Dispatchers.IO) {
                            val database = getInstance(context)
                            val modelDao = database.anamneseModelDao()
                            val campoDao = database.anamneseCampoDao()
                            // Modelos prontos
                            val adultoId = modelDao.insert(com.example.psipro.data.entities.AnamneseModel(nome = "Anamnese Adulto"))
                            val infantilId = modelDao.insert(com.example.psipro.data.entities.AnamneseModel(nome = "Anamnese Infantil"))
                            val casalId = modelDao.insert(com.example.psipro.data.entities.AnamneseModel(nome = "Anamnese Casal"))
                            // Campos exemplo para Adulto
                            campoDao.insert(com.example.psipro.data.entities.AnamneseCampo(modeloId = adultoId, tipo = "TEXTO_CURTO", label = "Nome", obrigatorio = true))
                            campoDao.insert(com.example.psipro.data.entities.AnamneseCampo(modeloId = adultoId, tipo = "DATA", label = "Data de nascimento", obrigatorio = true))
                            campoDao.insert(com.example.psipro.data.entities.AnamneseCampo(modeloId = adultoId, tipo = "TEXTO_LONGO", label = "Queixa principal", obrigatorio = true))
                            // Campos exemplo para Infantil
                            campoDao.insert(com.example.psipro.data.entities.AnamneseCampo(modeloId = infantilId, tipo = "TEXTO_CURTO", label = "Nome da criança", obrigatorio = true))
                            campoDao.insert(com.example.psipro.data.entities.AnamneseCampo(modeloId = infantilId, tipo = "DATA", label = "Data de nascimento", obrigatorio = true))
                            campoDao.insert(com.example.psipro.data.entities.AnamneseCampo(modeloId = infantilId, tipo = "TEXTO_LONGO", label = "Comportamento", obrigatorio = false))
                            // Campos exemplo para Casal
                            campoDao.insert(com.example.psipro.data.entities.AnamneseCampo(modeloId = casalId, tipo = "TEXTO_CURTO", label = "Nome do parceiro(a)", obrigatorio = true))
                            campoDao.insert(com.example.psipro.data.entities.AnamneseCampo(modeloId = casalId, tipo = "TEXTO_LONGO", label = "Histórico do relacionamento", obrigatorio = false))
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
