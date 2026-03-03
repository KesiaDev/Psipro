package com.psipro.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.psipro.app.auth.AuthManager

/**
 * ViewModel de autenticação. Delegado para AuthManager/BackendAuthManager.
 * Mantido para compatibilidade; login real ocorre em MainActivity via BackendAuthManager.
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val authManager: AuthManager

    init {
        AuthManager.init(application)
        authManager = AuthManager.getInstance()
    }

    fun isLoggedIn(): Boolean = authManager.isLoggedIn()

    fun logout() {
        authManager.logout()
    }

    companion object {
        private const val TAG = "AuthViewModel"
    }
}
