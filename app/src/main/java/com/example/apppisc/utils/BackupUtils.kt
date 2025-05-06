package com.example.apppisc.utils

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object BackupUtils {
    private const val ALGORITHM = "AES/CBC/PKCS7Padding"
    private const val KEY_SIZE = 256
    private const val ITERATION_COUNT = 65536
    private const val SALT_SIZE = 16
    private const val IV_SIZE = 16

    // Gera uma chave a partir de senha do usuário
    fun generateKey(password: CharArray, salt: ByteArray): SecretKey {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password, salt, ITERATION_COUNT, KEY_SIZE)
        return SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
    }

    // Exporta o banco de dados criptografado
    fun exportDatabase(context: Context, password: CharArray, outputFile: File): Boolean {
        val dbFile = context.getDatabasePath("app_pisc_database")
        if (!dbFile.exists()) return false
        val salt = ByteArray(SALT_SIZE).apply { SecureRandom().nextBytes(this) }
        val iv = ByteArray(IV_SIZE).apply { SecureRandom().nextBytes(this) }
        val key = generateKey(password, salt)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))
        FileInputStream(dbFile).use { fis ->
            FileOutputStream(outputFile).use { fos ->
                fos.write(salt)
                fos.write(iv)
                CipherOutputStream(fos, cipher).use { cos ->
                    fis.copyTo(cos)
                }
            }
        }
        return true
    }

    // Importa/restaura o banco de dados criptografado
    fun importDatabase(context: Context, password: CharArray, inputFile: File): Boolean {
        val dbFile = context.getDatabasePath("app_pisc_database")
        FileInputStream(inputFile).use { fis ->
            val salt = ByteArray(SALT_SIZE)
            val iv = ByteArray(IV_SIZE)
            fis.read(salt)
            fis.read(iv)
            val key = generateKey(password, salt)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
            FileOutputStream(dbFile).use { fos ->
                CipherInputStream(fis, cipher).use { cis ->
                    cis.copyTo(fos)
                }
            }
        }
        return true
    }

    // Criptografa um arquivo anexo
    fun encryptFile(input: File, output: File, key: SecretKey, iv: ByteArray) {
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))
        FileInputStream(input).use { fis ->
            FileOutputStream(output).use { fos ->
                CipherOutputStream(fos, cipher).use { cos ->
                    fis.copyTo(cos)
                }
            }
        }
    }

    // Descriptografa um arquivo anexo
    fun decryptFile(input: File, output: File, key: SecretKey, iv: ByteArray) {
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
        FileInputStream(input).use { fis ->
            FileOutputStream(output).use { fos ->
                CipherInputStream(fis, cipher).use { cis ->
                    cis.copyTo(fos)
                }
            }
        }
    }
} 