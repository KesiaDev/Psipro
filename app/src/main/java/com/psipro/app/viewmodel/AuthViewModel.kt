package com.psipro.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.psipro.app.auth.AuthManager
import com.psipro.app.data.AppDatabase
import com.psipro.app.data.entities.User
import com.psipro.app.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: UserRepository
    private val authManager: AuthManager

    init {
        // Inicializar AuthManager primeiro
        AuthManager.init(application)
        authManager = AuthManager.getInstance()
        
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
                val localSuccess = authManager.login(email, password)
                if (!localSuccess) {
                    onResult(false)
                    return@launch
                }
                // Agora autentica no FirebaseAuth também
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onResult(true)
                        } else {
                            // Se falhar no Firebase, faz logout local para evitar inconsistência
                            authManager.logout()
                            onResult(false)
                        }
                    }
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



