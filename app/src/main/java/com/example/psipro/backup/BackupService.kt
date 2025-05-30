package com.example.psipro.backup

import android.content.Context
import com.example.psipro.config.AppConfig
import com.example.psipro.data.AppDatabase
import com.example.psipro.data.entities.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class BackupService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appConfig: AppConfig,
    private val database: AppDatabase
) {
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault())
    
    suspend fun createBackup(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val backupDir = File(context.filesDir, "backups").apply { mkdirs() }
            val backupFile = File(backupDir, "backup_${dateFormat.format(Date())}.enc")
            
            // Coletar dados
            val backupData = BackupData(
                patients = database.patientDao().getAllPatients().first(),
                appointments = database.appointmentDao().getAllAppointments().first(),
                notes = database.patientNoteDao().getAllNotes().first(),
                users = database.userDao().getAllUsers().first()
            )
            
            // Converter para JSON
            val jsonData = gson.toJson(backupData)
            
            // Criptografar
            val encryptedData = encryptData(jsonData)
            
            // Salvar arquivo
            backupFile.writeBytes(encryptedData)
            
            // Atualizar timestamp do último backup
            appConfig.lastBackupTime = System.currentTimeMillis()
            
            Result.success(backupFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun restoreBackup(backupPath: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val backupFile = File(backupPath)
            if (!backupFile.exists()) {
                return@withContext Result.failure(Exception("Arquivo de backup não encontrado"))
            }
            
            // Ler e descriptografar dados
            val encryptedData = backupFile.readBytes()
            val jsonData = decryptData(encryptedData)
            
            // Converter de JSON
            val backupData = gson.fromJson<BackupData>(jsonData, object : TypeToken<BackupData>() {}.type)
            
            // Restaurar dados
            database.clearAllTables()
            // Inserir dados do backup
            for (patient in backupData.patients) {
                database.patientDao().insertPatient(patient)
            }
            for (appointment in backupData.appointments) {
                database.appointmentDao().insertAppointment(appointment)
            }
            for (note in backupData.notes) {
                database.patientNoteDao().insertNote(note)
            }
            for (user in backupData.users) {
                database.userDao().insertUser(user)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun encryptData(data: String): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val key = generateKey()
        val iv = generateIV()
        cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))
        return iv + cipher.doFinal(data.toByteArray())
    }
    
    private fun decryptData(data: ByteArray): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val key = generateKey()
        val iv = data.copyOfRange(0, 12)
        val encryptedData = data.copyOfRange(12, data.size)
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
        return String(cipher.doFinal(encryptedData))
    }
    
    private fun generateKey(): SecretKey {
        // TODO: Implementar geração segura de chave
        throw NotImplementedError("Implementar geração de chave")
    }
    
    private fun generateIV(): ByteArray {
        val iv = ByteArray(12)
        Random().nextBytes(iv)
        return iv
    }
    
    data class BackupData(
        val patients: List<Patient>,
        val appointments: List<Appointment>,
        val notes: List<PatientNote>,
        val users: List<User>
    )
} 