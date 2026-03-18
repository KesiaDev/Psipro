package com.psipro.app.sync

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.psipro.app.auth.TokenManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sessão de sync. Tokens delegados ao TokenManager.
 * Fallback para SharedPreferences normal se o Keystore falhar (ex: KeyStoreException após reinstalação).
 */
@Singleton
class BackendSessionStore @Inject constructor(
    @ApplicationContext context: Context,
    private val tokenManager: TokenManager
) {
    private val prefs by lazy {
        try {
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
        } catch (e: Exception) {
            Log.w("BackendSessionStore", "Erro ao criar EncryptedSharedPreferences, usando SharedPreferences normal", e)
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    fun getAccessToken(): String? = tokenManager.getAccessToken()
    fun setAccessToken(token: String) = tokenManager.setAccessToken(token)

    fun getRefreshToken(): String? = tokenManager.getRefreshToken()
    fun setRefreshToken(token: String) = tokenManager.setRefreshToken(token)

    fun getClinicId(): String? = tokenManager.getActiveClinic()
    fun setClinicId(clinicId: String?) {
        clinicId?.let { tokenManager.saveActiveClinic(it) }
    }
    fun clearClinicId() {
        tokenManager.clearActiveClinic()
    }

    fun getProfessionalType(): String? = prefs.getString(KEY_PROFESSIONAL_TYPE, null)
    fun setProfessionalType(professionalType: String?) {
        prefs.edit().putString(KEY_PROFESSIONAL_TYPE, professionalType ?: "").apply()
    }

    fun hasLgpdConsent(): Boolean = prefs.getBoolean(KEY_LGPD_CONSENT, false)
    fun setLgpdConsent(accepted: Boolean) {
        prefs.edit().putBoolean(KEY_LGPD_CONSENT, accepted).apply()
    }

    fun getLastPatientsSyncAtIso(): String? = prefs.getString(KEY_LAST_PATIENTS_SYNC_AT, null)
    fun setLastPatientsSyncAtIso(iso: String?) {
        prefs.edit().putString(KEY_LAST_PATIENTS_SYNC_AT, iso).apply()
    }

    fun getLastAppointmentsSyncAtIso(): String? = prefs.getString(KEY_LAST_APPOINTMENTS_SYNC_AT, null)
    fun setLastAppointmentsSyncAtIso(iso: String?) {
        prefs.edit().apply {
            if (iso != null) putString(KEY_LAST_APPOINTMENTS_SYNC_AT, iso)
            else remove(KEY_LAST_APPOINTMENTS_SYNC_AT)
        }.apply()
    }

    fun getLastSessionsSyncAtIso(): String? = prefs.getString(KEY_LAST_SESSIONS_SYNC_AT, null)
    fun setLastSessionsSyncAtIso(iso: String?) {
        prefs.edit().putString(KEY_LAST_SESSIONS_SYNC_AT, iso).apply()
    }

    fun getLastPaymentsSyncAtIso(): String? = prefs.getString(KEY_LAST_PAYMENTS_SYNC_AT, null)
    fun setLastPaymentsSyncAtIso(iso: String?) {
        prefs.edit().putString(KEY_LAST_PAYMENTS_SYNC_AT, iso).apply()
    }

    fun getLastDocumentsSyncAtIso(): String? = prefs.getString(KEY_LAST_DOCUMENTS_SYNC_AT, null)
    fun setLastDocumentsSyncAtIso(iso: String?) {
        prefs.edit().putString(KEY_LAST_DOCUMENTS_SYNC_AT, iso).apply()
    }

    /** Limpa watermarks de sync para forçar sincronização completa (ex: agenda vazia). */
    fun clearSyncWatermarks() {
        prefs.edit()
            .remove(KEY_LAST_PATIENTS_SYNC_AT)
            .remove(KEY_LAST_APPOINTMENTS_SYNC_AT)
            .remove(KEY_LAST_SESSIONS_SYNC_AT)
            .remove(KEY_LAST_PAYMENTS_SYNC_AT)
            .remove(KEY_LAST_DOCUMENTS_SYNC_AT)
            .apply()
    }

    fun clear() {
        tokenManager.clearTokens()
        prefs.edit()
            .remove(KEY_LGPD_CONSENT)
            .remove(KEY_LAST_PATIENTS_SYNC_AT)
            .remove(KEY_LAST_APPOINTMENTS_SYNC_AT)
            .remove(KEY_LAST_SESSIONS_SYNC_AT)
            .remove(KEY_LAST_PAYMENTS_SYNC_AT)
            .remove(KEY_LAST_DOCUMENTS_SYNC_AT)
            .remove(KEY_PROFESSIONAL_TYPE)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "psipro_backend_session"
        private const val KEY_PROFESSIONAL_TYPE = "professional_type"
        private const val KEY_LGPD_CONSENT = "lgpd_consent"
        private const val KEY_LAST_PATIENTS_SYNC_AT = "patients_last_sync_at"
        private const val KEY_LAST_APPOINTMENTS_SYNC_AT = "appointments_last_sync_at"
        private const val KEY_LAST_SESSIONS_SYNC_AT = "sessions_last_sync_at"
        private const val KEY_LAST_PAYMENTS_SYNC_AT = "payments_last_sync_at"
        private const val KEY_LAST_DOCUMENTS_SYNC_AT = "documents_last_sync_at"
    }
}
