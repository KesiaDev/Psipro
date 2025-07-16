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
import com.example.psipro.data.entities.HistoricoFamiliar
import com.example.psipro.data.entities.HistoricoMedico
import com.example.psipro.data.entities.VidaEmocional
import com.example.psipro.data.entities.ObservacoesClinicas
import com.example.psipro.data.entities.AnotacaoSessao
import com.example.psipro.data.entities.CobrancaSessao
import com.example.psipro.data.entities.TipoSessao
import com.example.psipro.data.dao.AnamneseModelDao
import com.example.psipro.data.dao.AnamneseCampoDao
import com.example.psipro.data.dao.AnamnesePreenchidaDao
import com.example.psipro.data.dao.HistoricoFamiliarDao
import com.example.psipro.data.dao.HistoricoMedicoDao
import com.example.psipro.data.dao.VidaEmocionalDao
import com.example.psipro.data.dao.ObservacoesClinicasDao
import com.example.psipro.data.dao.AnotacaoSessaoDao
import com.example.psipro.data.dao.CobrancaSessaoDao
import com.example.psipro.data.dao.TipoSessaoDao
import com.example.psipro.data.converters.DateConverter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [User::class, Patient::class, Appointment::class, PatientNote::class, PatientMessage::class, PatientReport::class, FinancialRecord::class, Prontuario::class, AuditLog::class, WhatsAppConversation::class, AnamneseModel::class, AnamneseCampo::class, AnamnesePreenchida::class, HistoricoFamiliar::class, HistoricoMedico::class, VidaEmocional::class, ObservacoesClinicas::class, AnotacaoSessao::class, CobrancaSessao::class, TipoSessao::class],
    version = 18,
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
    abstract fun historicoFamiliarDao(): HistoricoFamiliarDao
    abstract fun historicoMedicoDao(): HistoricoMedicoDao
    abstract fun vidaEmocionalDao(): VidaEmocionalDao
    abstract fun observacoesClinicasDao(): ObservacoesClinicasDao
    abstract fun anotacaoSessaoDao(): AnotacaoSessaoDao
    abstract fun cobrancaSessaoDao(): CobrancaSessaoDao
    abstract fun tipoSessaoDao(): TipoSessaoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private suspend fun seedDatabase(database: AppDatabase) {
            try {
                // Seed dos tipos de sessão padrão
                val tipoSessaoDao = database.tipoSessaoDao()
                if (tipoSessaoDao.countTiposSessao() == 0) {
                    tipoSessaoDao.insert(com.example.psipro.data.entities.TipoSessao(nome = "Individual", valorPadrao = 150.0))
                    tipoSessaoDao.insert(com.example.psipro.data.entities.TipoSessao(nome = "Casal", valorPadrao = 200.0))
                    tipoSessaoDao.insert(com.example.psipro.data.entities.TipoSessao(nome = "Avaliação", valorPadrao = 180.0))
                }
                
                // Seed dos modelos prontos
                val modelDao = database.anamneseModelDao()
                val campoDao = database.anamneseCampoDao()
                
                // Verificar se já existem modelos
                val modelosExistentes = modelDao.getAll()
                if (modelosExistentes.isEmpty()) {
                    // Inserir modelos de exemplo
                    val adultoId = modelDao.insert(com.example.psipro.data.entities.AnamneseModel(nome = "Anamnese Adulto", isDefault = true))
                    val infantilId = modelDao.insert(com.example.psipro.data.entities.AnamneseModel(nome = "Anamnese Infantil", isDefault = true))
                    val casalId = modelDao.insert(com.example.psipro.data.entities.AnamneseModel(nome = "Anamnese Casal", isDefault = true))
                    
                    // Inserir campos usando AnamneseTestUtils
                    val camposAdulto = com.example.psipro.utils.AnamneseTestUtils.createAdultoFields()
                    val camposInfantil = com.example.psipro.utils.AnamneseTestUtils.createInfantilFields()
                    val camposCasal = com.example.psipro.utils.AnamneseTestUtils.createCasalFields()
                    
                    // Inserir campos do adulto
                    camposAdulto.forEach { campo ->
                        campoDao.insert(campo.copy(modeloId = adultoId))
                    }
                    
                    // Inserir campos do infantil
                    camposInfantil.forEach { campo ->
                        campoDao.insert(campo.copy(modeloId = infantilId))
                    }
                    
                    // Inserir campos do casal
                    camposCasal.forEach { campo ->
                        campoDao.insert(campo.copy(modeloId = casalId))
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AppDatabase", "Erro no seed do banco", e)
            }
        }

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
                        // Executar seeds de forma assíncrona para evitar ANR
                        GlobalScope.launch(Dispatchers.IO) {
                            try {
                                val database = getInstance(context)
                                seedDatabase(database)
                            } catch (e: Exception) {
                                android.util.Log.e("AppDatabase", "Erro ao fazer seed do banco", e)
                            }
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
