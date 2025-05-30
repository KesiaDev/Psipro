package com.example.psipro.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.psipro.data.entities.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var sessionJob: kotlinx.coroutines.Job? = null
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val securePrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser
    
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.NotAuthenticated)
    val sessionState: StateFlow<SessionState> = _sessionState
    
    init {
        restoreSession()
    }
    
    private fun restoreSession() {
        scope.launch {
            val userId = securePrefs.getLong(KEY_USER_ID, -1)
            val lastActivity = securePrefs.getLong(KEY_LAST_ACTIVITY, 0)
            
            if (userId != -1L && !isSessionExpired(lastActivity)) {
                // Restaurar sessão
                _sessionState.value = SessionState.Authenticated
                // Carregar dados do usuário do banco de dados
                // TODO: Implementar carregamento do usuário
            } else {
                logout()
            }
        }
    }
    
    fun login(user: User) {
        scope.launch {
            securePrefs.edit().apply {
                putLong(KEY_USER_ID, user.id)
                putLong(KEY_LAST_ACTIVITY, System.currentTimeMillis())
                apply()
            }
            _currentUser.value = user
            _sessionState.value = SessionState.Authenticated
            startSessionMonitoring()
        }
    }
    
    fun logout() {
        scope.launch {
            securePrefs.edit().clear().apply()
            _currentUser.value = null
            _sessionState.value = SessionState.NotAuthenticated
            stopSessionMonitoring()
        }
    }
    
    fun updateLastActivity() {
        securePrefs.edit().putLong(KEY_LAST_ACTIVITY, System.currentTimeMillis()).apply()
    }
    
    private fun startSessionMonitoring() {
        sessionJob?.cancel()
        sessionJob = scope.launch {
            while (true) {
                val lastActivity = securePrefs.getLong(KEY_LAST_ACTIVITY, 0)
                if (isSessionExpired(lastActivity)) {
                    logout()
                    break
                }
                kotlinx.coroutines.delay(SESSION_CHECK_INTERVAL)
            }
        }
    }
    
    private fun stopSessionMonitoring() {
        sessionJob?.cancel()
        sessionJob = null
    }
    
    private fun isSessionExpired(lastActivity: Long): Boolean {
        val currentTime = System.currentTimeMillis()
        val timeDiff = currentTime - lastActivity
        return timeDiff > SESSION_TIMEOUT
    }
    
    sealed class SessionState {
        object NotAuthenticated : SessionState()
        object Authenticated : SessionState()
        object Expired : SessionState()
    }
    
    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_LAST_ACTIVITY = "last_activity"
        private val SESSION_TIMEOUT = java.util.concurrent.TimeUnit.HOURS.toMillis(24) // 24 horas
        private val SESSION_CHECK_INTERVAL = java.util.concurrent.TimeUnit.MINUTES.toMillis(5) // 5 minutos
    }
} 