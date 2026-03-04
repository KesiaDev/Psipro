package com.psipro.app.sync

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.psipro.app.auth.TokenManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sessão de sync. Tokens delegados ao TokenManager.
 */
@Singleton
class BackendSessionStore @Inject constructor(
    @ApplicationContext context: Context,
    private val tokenManager: TokenManager
) {
    private val prefs: SharedPreferences by lazy {
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

    fun getAccessToken(): String? = tokenManager.getAccessToken()
    fun setAccessToken(token: String) = tokenManager.setAccessToken(token)

    fun getRefreshToken(): String? = tokenManager.getRefreshToken()
    fun setRefreshToken(token: String) = tokenManager.setRefreshToken(token)

    fun getClinicId(): String? = tokenManager.getActiveClinic()
    fun setClinicId(clinicId: String?) {
        clinicId?.let { tokenManager.saveActiveClinic(it) }
    }

    fun getLastPatientsSyncAtIso(): String? = prefs.getString(KEY_LAST_PATIENTS_SYNC_AT, null)
    fun setLastPatientsSyncAtIso(iso: String?) {
        prefs.edit().putString(KEY_LAST_PATIENTS_SYNC_AT, iso).apply()
    }

    fun getLastAppointmentsSyncAtIso(): String? = prefs.getString(KEY_LAST_APPOINTMENTS_SYNC_AT, null)
    fun setLastAppointmentsSyncAtIso(iso: String?) {
        prefs.edit().putString(KEY_LAST_APPOINTMENTS_SYNC_AT, iso).apply()
    }

    fun getLastSessionsSyncAtIso(): String? = prefs.getString(KEY_LAST_SESSIONS_SYNC_AT, null)
    fun setLastSessionsSyncAtIso(iso: String?) {
        prefs.edit().putString(KEY_LAST_SESSIONS_SYNC_AT, iso).apply()
    }

    fun getLastPaymentsSyncAtIso(): String? = prefs.getString(KEY_LAST_PAYMENTS_SYNC_AT, null)
    fun setLastPaymentsSyncAtIso(iso: String?) {
        prefs.edit().putString(KEY_LAST_PAYMENTS_SYNC_AT, iso).apply()
    }

    fun clear() {
        tokenManager.clearTokens()
        prefs.edit()
            .remove(KEY_LAST_PATIENTS_SYNC_AT)
            .remove(KEY_LAST_APPOINTMENTS_SYNC_AT)
            .remove(KEY_LAST_SESSIONS_SYNC_AT)
            .remove(KEY_LAST_PAYMENTS_SYNC_AT)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "psipro_backend_session"
        private const val KEY_LAST_PATIENTS_SYNC_AT = "patients_last_sync_at"
        private const val KEY_LAST_APPOINTMENTS_SYNC_AT = "appointments_last_sync_at"
        private const val KEY_LAST_SESSIONS_SYNC_AT = "sessions_last_sync_at"
        private const val KEY_LAST_PAYMENTS_SYNC_AT = "payments_last_sync_at"
    }
}

