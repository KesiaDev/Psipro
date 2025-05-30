package com.example.psipro.config

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class AppConfig @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val securePrefs = EncryptedSharedPreferences.create(
        context,
        "app_config",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    var isFirstRun: Boolean
        get() = securePrefs.getBoolean(KEY_FIRST_RUN, true)
        set(value) = securePrefs.edit().putBoolean(KEY_FIRST_RUN, value).apply()
    
    var isDarkMode: Boolean
        get() = securePrefs.getBoolean(KEY_DARK_MODE, false)
        set(value) = securePrefs.edit().putBoolean(KEY_DARK_MODE, value).apply()
    
    var lastBackupTime: Long
        get() = securePrefs.getLong(KEY_LAST_BACKUP, 0)
        set(value) = securePrefs.edit().putLong(KEY_LAST_BACKUP, value).apply()
    
    var notificationEnabled: Boolean
        get() = securePrefs.getBoolean(KEY_NOTIFICATIONS, true)
        set(value) = securePrefs.edit().putBoolean(KEY_NOTIFICATIONS, value).apply()
    
    var autoBackupEnabled: Boolean
        get() = securePrefs.getBoolean(KEY_AUTO_BACKUP, true)
        set(value) = securePrefs.edit().putBoolean(KEY_AUTO_BACKUP, value).apply()
    
    var backupFrequency: Int
        get() = securePrefs.getInt(KEY_BACKUP_FREQUENCY, 24) // horas
        set(value) = securePrefs.edit().putInt(KEY_BACKUP_FREQUENCY, value).apply()
    
    var language: String
        get() = securePrefs.getString(KEY_LANGUAGE, "pt") ?: "pt"
        set(value) = securePrefs.edit().putString(KEY_LANGUAGE, value).apply()
    
    companion object {
        private const val KEY_FIRST_RUN = "first_run"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_LAST_BACKUP = "last_backup"
        private const val KEY_NOTIFICATIONS = "notifications"
        private const val KEY_AUTO_BACKUP = "auto_backup"
        private const val KEY_BACKUP_FREQUENCY = "backup_frequency"
        private const val KEY_LANGUAGE = "language"
    }
} 