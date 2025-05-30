package com.example.psipro.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.psipro.auth.AuthManager
import com.example.psipro.data.AppDatabase
import com.example.psipro.data.entities.User
import com.example.psipro.data.repository.UserRepository
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: UserRepository
    private val authManager = AuthManager.getInstance()

    init {
        val userDao = AppDatabase.getInstance(application).userDao()
        repository = UserRepository(userDao)
    }

    fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        if (email.isEmpty() || password.isEmpty()) {
            onResult(false)
            return
        }

        viewModelScope.launch {
            try {
                val success = authManager.login(email, password)
                onResult(success)
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao fazer login", e)
                onResult(false)
            }
        }
    }

    fun register(email: String, password: String, isAdmin: Boolean = false, onResult: (Boolean) -> Unit) {
        if (email.isEmpty() || password.isEmpty()) {
            onResult(false)
            return
        }

        viewModelScope.launch {
            try {
                // Verificar se já existe um usuário com este email
                if (repository.userExists(email)) {
                    onResult(false)
                    return@launch
                }

                // Criar novo usuário
                val user = User(
                    email = email,
                    password = password,
                    isAdmin = isAdmin
                )
                repository.insertUser(user)
                onResult(true)
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao registrar usuário", e)
                onResult(false)
            }
        }
    }

    fun isLoggedIn(): Boolean {
        return authManager.isLoggedIn()
    }

    fun logout() {
        authManager.logout()
    }

    companion object {
        private const val TAG = "AuthViewModel"
    }
} 