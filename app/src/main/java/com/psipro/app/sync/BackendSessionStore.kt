package com.psipro.app.sync

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackendSessionStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences

    init {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        prefs = EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)
    fun setAccessToken(token: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    fun getClinicId(): String? = prefs.getString(KEY_CLINIC_ID, null)
    fun setClinicId(clinicId: String?) {
        prefs.edit().putString(KEY_CLINIC_ID, clinicId).apply()
    }

    fun getLastPatientsSyncAtIso(): String? = prefs.getString(KEY_LAST_PATIENTS_SYNC_AT, null)
    fun setLastPatientsSyncAtIso(iso: String?) {
        prefs.edit().putString(KEY_LAST_PATIENTS_SYNC_AT, iso).apply()
    }

    fun clear() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_CLINIC_ID)
            .remove(KEY_LAST_PATIENTS_SYNC_AT)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "psipro_backend_session"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_CLINIC_ID = "clinic_id"
        private const val KEY_LAST_PATIENTS_SYNC_AT = "patients_last_sync_at"
    }
}

