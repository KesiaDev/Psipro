package com.psipro.app.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.locks.ReentrantReadWriteLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Gerencia accessToken, refreshToken e activeClinicId de forma segura.
 * Usa EncryptedSharedPreferences. Thread-safe.
 */
@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val lock = ReentrantReadWriteLock()
    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveTokens(accessToken: String, refreshToken: String) {
        lock.write {
            prefs.edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .apply()
        }
    }

    fun setAccessToken(token: String) {
        lock.write {
            prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
        }
    }

    fun setRefreshToken(token: String) {
        lock.write {
            prefs.edit().putString(KEY_REFRESH_TOKEN, token).apply()
        }
    }

    fun getAccessToken(): String? = lock.read { prefs.getString(KEY_ACCESS_TOKEN, null) }

    fun getRefreshToken(): String? = lock.read { prefs.getString(KEY_REFRESH_TOKEN, null) }

    fun clearTokens() {
        lock.write {
            prefs.edit()
                .remove(KEY_ACCESS_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .remove(KEY_ACTIVE_CLINIC)
                .apply()
        }
    }

    fun saveActiveClinic(clinicId: String) {
        lock.write {
            prefs.edit().putString(KEY_ACTIVE_CLINIC, clinicId).apply()
        }
    }

    fun getActiveClinic(): String? = lock.read { prefs.getString(KEY_ACTIVE_CLINIC, null) }

    companion object {
        private const val PREFS_NAME = "psipro_tokens"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_ACTIVE_CLINIC = "active_clinic_id"
    }
}
