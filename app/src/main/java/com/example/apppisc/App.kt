package com.example.apppisc

import android.app.Application
import android.util.Log
import com.example.apppisc.auth.AuthManager
import com.example.apppisc.data.AppDatabase
import com.example.apppisc.data.entities.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        
        try {
            // Inicializar banco de dados de forma síncrona para garantir que está pronto
            database = AppDatabase.getInstance(applicationContext)
            
            // Inicializar AuthManager primeiro
            AuthManager.init(applicationContext)
            
            // Inicializar outros componentes de forma assíncrona
            initializeApp()
            
            Log.d(TAG, "Aplicativo inicializado com sucesso")
        } catch (e: Exception) {
            Log.e(TAG, "Erro fatal na inicialização do aplicativo", e)
            throw e // Relança a exceção para que o sistema saiba que houve falha na inicialização
        }
    }
    
    private fun initializeApp() {
        applicationScope.launch {
            try {
                // Verificar se já existe um usuário admin
                val userDao = database.userDao()
                val adminExists = userDao.getUserByEmail("admin@teste.com") != null
                
                if (!adminExists) {
                    // Criar usuário admin inicial
                    try {
                        val adminUser = User(
                            email = "admin@teste.com",
                            password = "123456",
                            isAdmin = true
                        )
                        userDao.insertUser(adminUser)
                        Log.d(TAG, "Usuário admin criado com sucesso")
                    } catch (e: Exception) {
                        Log.e(TAG, "Erro ao criar usuário inicial", e)
                    }
                } else {
                    Log.d(TAG, "Usuário admin já existe")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro na inicialização dos componentes do aplicativo", e)
            }
        }
    }
    
    companion object {
        private const val TAG = "App"
    }
} 