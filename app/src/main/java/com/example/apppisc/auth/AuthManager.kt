package com.example.apppisc.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.util.Patterns
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.util.Date
import java.util.concurrent.TimeUnit

class AuthManager private constructor(context: Context) {
    private val sharedPreferences: SharedPreferences
    
    init {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "auth_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    private val listeners = mutableSetOf<AuthStateListener>()
    
    companion object {
        private const val TAG = "AuthManager"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_TOKEN_EXPIRY = "token_expiry"
        private const val TOKEN_VALIDITY_HOURS = 24L // Token válido por 24 horas
        
        @Volatile
        private var instance: AuthManager? = null
        
        fun init(context: Context) {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = AuthManager(context.applicationContext)
                    }
                }
            }
        }
        
        fun getInstance(): AuthManager {
            return instance ?: throw IllegalStateException("AuthManager must be initialized first")
        }
    }
    
    // Interface para callbacks de estado
    interface AuthStateListener {
        fun onAuthStateChanged(isLoggedIn: Boolean)
        fun onTokenExpired()
    }
    
    // Gerenciamento de Listeners
    fun addAuthStateListener(listener: AuthStateListener) {
        listeners.add(listener)
    }
    
    fun removeAuthStateListener(listener: AuthStateListener) {
        listeners.remove(listener)
    }
    
    private fun notifyAuthStateChanged(isLoggedIn: Boolean) {
        listeners.forEach { it.onAuthStateChanged(isLoggedIn) }
    }
    
    private fun notifyTokenExpired() {
        listeners.forEach { it.onTokenExpired() }
    }
    
    // Validação de Dados
    fun validateCredentials(email: String, password: String): Boolean {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return false
        }
        
        // Requisitos mínimos para senha
        if (password.length < 8) return false
        if (!password.any { it.isDigit() }) return false
        if (!password.any { it.isLetter() }) return false
        if (!password.any { it.isUpperCase() }) return false
        
        return true
    }
    
    // Gerenciamento de Sessão
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    fun getCurrentUserEmail(): String? {
        return sharedPreferences.getString(KEY_USER_EMAIL, null)
    }
    
    // Login
    fun login(email: String, password: String): Boolean {
        // TODO: Implement actual authentication logic
        if (email == "admin@teste.com" && password == "123456") {
            sharedPreferences.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_USER_EMAIL, email)
                .apply()
            return true
        }
        return false
    }
    
    // Logout
    fun logout() {
        sharedPreferences.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .remove(KEY_USER_EMAIL)
            .apply()
        
        notifyAuthStateChanged(false)
    }
    
    // Controle de Acesso
    fun hasPermission(permission: String): Boolean {
        // Implemente a lógica de verificação de permissões aqui
        // Por exemplo, verificar se o usuário tem determinada role ou permissão
        return isLoggedIn() // Por enquanto, só verifica se está logado
    }
    
    // Verificação de Token
    fun isTokenValid(): Boolean {
        return getCurrentUserEmail() != null && !isTokenExpired()
    }
    
    fun isTokenExpired(): Boolean {
        val expiryTime = sharedPreferences.getLong(KEY_TOKEN_EXPIRY, 0)
        return Date().time >= expiryTime
    }
    
    // Atualização de Token
    fun updateToken(newToken: String) {
        val expiryTime = Date().time + TimeUnit.HOURS.toMillis(TOKEN_VALIDITY_HOURS)
        
        sharedPreferences.edit().apply {
            putString(KEY_TOKEN, newToken)
            putLong(KEY_TOKEN_EXPIRY, expiryTime)
            apply()
        }
    }
    
    // Refresh Token (deve ser implementado de acordo com seu backend)
    suspend fun refreshToken(): Boolean {
        // Implemente a lógica de refresh token aqui
        // Por exemplo:
        // val response = api.refreshToken(getAuthToken())
        // if (response.isSuccessful) {
        //     updateToken(response.newToken)
        //     return true
        // }
        return false
    }
} 