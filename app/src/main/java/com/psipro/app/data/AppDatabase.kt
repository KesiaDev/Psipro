package com.psipro.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.psipro.app.data.dao.AppointmentDao
import com.psipro.app.data.dao.PatientDao
import com.psipro.app.data.dao.UserDao
import com.psipro.app.data.dao.PatientNoteDao
import com.psipro.app.data.dao.PatientMessageDao
import com.psipro.app.data.dao.PatientReportDao
import com.psipro.app.data.dao.FinancialRecordDao
import com.psipro.app.data.dao.ProntuarioDao
import com.psipro.app.data.dao.AuditLogDao
import com.psipro.app.data.dao.WhatsAppConversationDao
import com.psipro.app.data.dao.DocumentoDao
import com.psipro.app.data.dao.ArquivoDao
import com.psipro.app.data.dao.NotificationDao
import com.psipro.app.data.entities.Appointment
import com.psipro.app.data.entities.Patient
import com.psipro.app.data.entities.User
import com.psipro.app.data.entities.PatientNote
import com.psipro.app.data.entities.PatientMessage
import com.psipro.app.data.entities.PatientReport
import com.psipro.app.data.entities.FinancialRecord
import com.psipro.app.data.entities.Prontuario
import com.psipro.app.data.entities.AuditLog
import com.psipro.app.data.entities.WhatsAppConversation
import com.psipro.app.data.entities.AnamneseModel
import com.psipro.app.data.entities.AnamneseCampo
import com.psipro.app.data.entities.AnamnesePreenchida
import com.psipro.app.data.entities.HistoricoFamiliar
import com.psipro.app.data.entities.HistoricoMedico
import com.psipro.app.data.entities.VidaEmocional
import com.psipro.app.data.entities.ObservacoesClinicas
import com.psipro.app.data.entities.AnotacaoSessao
import com.psipro.app.data.entities.CobrancaSessao
import com.psipro.app.data.entities.TipoSessao
import com.psipro.app.data.entities.CobrancaAgendamento
import com.psipro.app.data.entities.Autoavaliacao
import com.psipro.app.data.entities.Documento
import com.psipro.app.data.entities.Arquivo
import com.psipro.app.data.entities.Notification
import com.psipro.app.data.dao.AnamneseModelDao
import com.psipro.app.data.dao.AnamneseCampoDao
import com.psipro.app.data.dao.AnamnesePreenchidaDao
import com.psipro.app.data.dao.HistoricoFamiliarDao
import com.psipro.app.data.dao.HistoricoMedicoDao
import com.psipro.app.data.dao.VidaEmocionalDao
import com.psipro.app.data.dao.ObservacoesClinicasDao
import com.psipro.app.data.dao.AnotacaoSessaoDao
import com.psipro.app.data.dao.CobrancaSessaoDao
import com.psipro.app.data.dao.TipoSessaoDao
import com.psipro.app.data.dao.CobrancaAgendamentoDao
import com.psipro.app.data.dao.AutoavaliacaoDao
import com.psipro.app.data.converters.DateConverter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.room.migration.Migration

@Database(
    entities = [User::class, Patient::class, Appointment::class, PatientNote::class, PatientMessage::class, PatientReport::class, FinancialRecord::class, Prontuario::class, AuditLog::class, WhatsAppConversation::class, AnamneseModel::class, AnamneseCampo::class, AnamnesePreenchida::class, HistoricoFamiliar::class, HistoricoMedico::class, VidaEmocional::class, ObservacoesClinicas::class, AnotacaoSessao::class, CobrancaSessao::class, TipoSessao::class, CobrancaAgendamento::class, Autoavaliacao::class, Documento::class, Arquivo::class, Notification::class],
    version = 24,
    exportSchema = false
)
@TypeConverters(DateConverter::class, com.psipro.app.data.converters.AnamneseGroupConverter::class, com.psipro.app.data.converters.TipoDocumentoConverter::class, com.psipro.app.data.converters.CategoriaArquivoConverter::class, com.psipro.app.data.converters.TipoArquivoConverter::class)
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
    abstract fun cobrancaAgendamentoDao(): CobrancaAgendamentoDao
    abstract fun autoavaliacaoDao(): AutoavaliacaoDao
    abstract fun documentoDao(): DocumentoDao
    abstract fun arquivoDao(): ArquivoDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        @JvmStatic
        fun getInstance(context: Context): AppDatabase {
            return getDatabase(context)
        }
        
        private suspend fun seedDatabase(database: AppDatabase) {
            try {
                // Migrar pacientes existentes que podem ter anamneseGroup nulo
                val patientDao = database.patientDao()
                val allPatients = patientDao.getAll()
                allPatients.forEach { patient ->
                    if (patient.anamneseGroup == null) {
                        android.util.Log.d("AppDatabase", "Migrando paciente ${patient.name} com anamneseGroup nulo")
                        val updatedPatient = patient.copy(anamneseGroup = com.psipro.app.data.entities.AnamneseGroup.ADULTO)
                        patientDao.updatePatient(updatedPatient)
                    }
                }
                
                // Seed dos tipos de sessão padrão
                val tipoSessaoDao = database.tipoSessaoDao()
                if (tipoSessaoDao.countTiposSessao() == 0) {
                    tipoSessaoDao.insert(com.psipro.app.data.entities.TipoSessao(nome = "Individual", valorPadrao = 150.0))
                    tipoSessaoDao.insert(com.psipro.app.data.entities.TipoSessao(nome = "Casal", valorPadrao = 200.0))
                    tipoSessaoDao.insert(com.psipro.app.data.entities.TipoSessao(nome = "Avaliação", valorPadrao = 180.0))
                }
                
                // Seed dos modelos prontos
                val modelDao = database.anamneseModelDao()
                val campoDao = database.anamneseCampoDao()
                
                // Verificar se já existem modelos
                val modelosExistentes = modelDao.getAll()
                if (modelosExistentes.isEmpty()) {
                    // Inserir modelos de exemplo
                    val adultoId = modelDao.insert(com.psipro.app.data.entities.AnamneseModel(nome = "Anamnese Adulto", isDefault = true))
                    val infantilId = modelDao.insert(com.psipro.app.data.entities.AnamneseModel(nome = "Anamnese Infantil", isDefault = true))
                    val casalId = modelDao.insert(com.psipro.app.data.entities.AnamneseModel(nome = "Anamnese Casal", isDefault = true))
                    
                    // Inserir campos usando AnamneseTestUtils
                    val camposAdulto = com.psipro.app.utils.AnamneseTestUtils.createAdultoFields()
                    val camposInfantil = com.psipro.app.utils.AnamneseTestUtils.createInfantilFields()
                    val camposCasal = com.psipro.app.utils.AnamneseTestUtils.createCasalFields()
                    
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

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "psipro_database"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        GlobalScope.launch(Dispatchers.IO) {
                            seedDatabase(INSTANCE!!)
                        }
                    }
                })
                .addMigrations(
                    object : Migration(21, 22) {
                        override fun migrate(database: SupportSQLiteDatabase) {
                            // Criar tabela documentos
                            database.execSQL("""
                                CREATE TABLE IF NOT EXISTS documentos (
                                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                    patientId INTEGER NOT NULL,
                                    titulo TEXT NOT NULL,
                                    tipo TEXT NOT NULL,
                                    conteudo TEXT NOT NULL,
                                    conteudoOriginal TEXT NOT NULL,
                                    dataCriacao INTEGER NOT NULL,
                                    dataModificacao INTEGER NOT NULL,
                                    assinaturaPaciente TEXT NOT NULL,
                                    assinaturaProfissional TEXT NOT NULL,
                                    dataAssinaturaPaciente INTEGER,
                                    dataAssinaturaProfissional INTEGER,
                                    caminhoPDF TEXT NOT NULL,
                                    compartilhado INTEGER NOT NULL,
                                    observacoes TEXT NOT NULL,
                                    FOREIGN KEY (patientId) REFERENCES patients (id) ON DELETE CASCADE
                                )
                            """)
                        }
                    },
                    object : Migration(22, 23) {
                        override fun migrate(database: SupportSQLiteDatabase) {
                            // Criar tabela arquivos
                            database.execSQL("""
                                CREATE TABLE IF NOT EXISTS arquivos (
                                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                    patientId INTEGER NOT NULL,
                                    nome TEXT NOT NULL,
                                    caminhoArquivo TEXT NOT NULL,
                                    tipoArquivo TEXT NOT NULL,
                                    categoriaArquivo TEXT NOT NULL,
                                    tamanhoBytes INTEGER NOT NULL,
                                    descricao TEXT,
                                    dataUpload INTEGER NOT NULL,
                                    dataModificacao INTEGER NOT NULL,
                                    isEncrypted INTEGER NOT NULL,
                                    hashArquivo TEXT,
                                    FOREIGN KEY (patientId) REFERENCES patients (id) ON DELETE CASCADE
                                )
                            """)
                        }
                    }
                )
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}




