package com.example.apppisc.utils

import android.content.Context
import java.io.File
import java.security.SecureRandom
import javax.crypto.SecretKey

object AttachmentManager {
    private const val ANEXOS_DIR = "anexos"
    private const val SALT_SIZE = 16
    private const val IV_SIZE = 16

    // Salva um anexo criptografado
    fun saveEncryptedAttachment(context: Context, inputFile: File, password: CharArray, fileName: String): Boolean {
        val anexosDir = File(context.filesDir, ANEXOS_DIR)
        if (!anexosDir.exists()) anexosDir.mkdirs()
        val outputFile = File(anexosDir, fileName)
        val salt = ByteArray(SALT_SIZE).apply { SecureRandom().nextBytes(this) }
        val iv = ByteArray(IV_SIZE).apply { SecureRandom().nextBytes(this) }
        val key = BackupUtils.generateKey(password, salt)
        // Salva salt e iv no início do arquivo
        outputFile.outputStream().use { fos ->
            fos.write(salt)
            fos.write(iv)
            BackupUtils.encryptFile(inputFile, outputFile, key, iv)
        }
        return true
    }

    // Lê um anexo descriptografado (gera arquivo temporário)
    fun getDecryptedAttachment(context: Context, fileName: String, password: CharArray): File? {
        val anexosDir = File(context.filesDir, ANEXOS_DIR)
        val encryptedFile = File(anexosDir, fileName)
        if (!encryptedFile.exists()) return null
        val tempFile = File(context.cacheDir, "${fileName}_temp")
        encryptedFile.inputStream().use { fis ->
            val salt = ByteArray(SALT_SIZE)
            val iv = ByteArray(IV_SIZE)
            fis.read(salt)
            fis.read(iv)
            val key = BackupUtils.generateKey(password, salt)
            BackupUtils.decryptFile(encryptedFile, tempFile, key, iv)
        }
        return tempFile
    }

    // Remove um anexo criptografado
    fun deleteAttachment(context: Context, fileName: String): Boolean {
        val anexosDir = File(context.filesDir, ANEXOS_DIR)
        val file = File(anexosDir, fileName)
        return file.delete()
    }
} 