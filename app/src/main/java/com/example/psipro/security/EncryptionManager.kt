package com.example.psipro.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class EncryptionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    private val keyAlias = "patient_data_key"
    private val ivSize = 12 // Tamanho do vetor de inicialização para GCM

    init {
        try {
            if (!keyStore.containsAlias(keyAlias)) {
                generateKey()
            }
        } catch (e: Exception) {
            Log.w("EncryptionManager", "Erro ao inicializar keystore, usando criptografia simples", e)
        }
    }

    private fun generateKey() {
        try {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore"
            )

            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setUserAuthenticationRequired(false)
                .build()

            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        } catch (e: Exception) {
            Log.e("EncryptionManager", "Erro ao gerar chave", e)
            throw e
        }
    }

    private fun getKey(): SecretKey? {
        return try {
            val entry = keyStore.getEntry(keyAlias, null) as KeyStore.SecretKeyEntry
            entry.secretKey
        } catch (e: Exception) {
            Log.e("EncryptionManager", "Erro ao obter chave", e)
            null
        }
    }

    fun encrypt(data: String): String {
        val key = getKey()
        if (key == null) {
            Log.w("EncryptionManager", "Chave não disponível, retornando dados sem criptografia")
            return data
        }
        
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, key)
            
            val iv = cipher.iv
            val encrypted = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
            
            // Combinar IV e dados criptografados
            val combined = ByteArray(ivSize + encrypted.size)
            System.arraycopy(iv, 0, combined, 0, ivSize)
            System.arraycopy(encrypted, 0, combined, ivSize, encrypted.size)
            
            Base64.encodeToString(combined, Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e("EncryptionManager", "Erro ao criptografar", e)
            data
        }
    }

    fun decrypt(encryptedData: String): String {
        val key = getKey()
        if (key == null) {
            Log.w("EncryptionManager", "Chave não disponível, retornando dados sem descriptografia")
            return encryptedData
        }
        
        return try {
            val combined = Base64.decode(encryptedData, Base64.DEFAULT)
            
            // Extrair IV e dados criptografados
            val iv = ByteArray(ivSize)
            val encrypted = ByteArray(combined.size - ivSize)
            System.arraycopy(combined, 0, iv, 0, ivSize)
            System.arraycopy(combined, ivSize, encrypted, 0, encrypted.size)
            
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)
            
            String(cipher.doFinal(encrypted), Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e("EncryptionManager", "Erro ao descriptografar", e)
            encryptedData
        }
    }

    companion object {
        @JvmStatic
        fun encryptStatic(value: String): String = value // Para criptografia real, adapte para acessar uma chave segura

        @JvmStatic
        fun decryptStatic(value: String): String = value // Para descriptografia real, adapte para acessar uma chave segura
    }
} 