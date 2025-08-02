package com.psipro.app

import android.app.Application
import android.util.Log
import com.psipro.app.auth.AuthManager
import com.psipro.app.data.AppDatabase
import com.psipro.app.data.entities.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import dagger.hilt.android.HiltAndroidApp
import com.google.firebase.FirebaseApp

@HiltAndroidApp
class App : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        
        try {
            // Inicializar Firebase primeiro
            FirebaseApp.initializeApp(this)
            Log.d(TAG, "Firebase inicializado com sucesso")
            
            // Inicializar componentes de forma assíncrona para evitar ANR
            initializeAppAsync()
            
            Log.d(TAG, "Aplicativo inicializado com sucesso")
        } catch (e: Exception) {
            Log.e(TAG, "Erro fatal na inicialização do aplicativo", e)
            // Não relançar exceção para evitar crash
        }
    }
    
    private fun initializeAppAsync() {
        applicationScope.launch {
            try {
                // Inicializar banco de dados de forma assíncrona
                database = AppDatabase.getInstance(applicationContext)
                
                // Inicializar AuthManager
                AuthManager.init(applicationContext)
                
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



