package com.psipro.app

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.psipro.app.data.AppDatabase
import com.psipro.app.config.AppConfig
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.psipro.app.sync.work.PatientsSyncScheduler

@HiltAndroidApp
class App : Application() {
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    companion object {
        lateinit var instance: App
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Configurar tratamento de exceções não capturadas
        setupUncaughtExceptionHandler()
        
        // Verificar inicialização do Firebase
        try {
            val firebaseApp = com.google.firebase.FirebaseApp.getInstance()
            Log.d("App", "Firebase inicializado: ${firebaseApp.name}")
            Log.d("App", "Firebase options: ${firebaseApp.options.projectId}")
        } catch (e: Exception) {
            Log.e("App", "Erro ao verificar Firebase", e)
        }
        
        // Verificar Firebase Auth
        try {
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
            Log.d("App", "Firebase Auth inicializado")
            Log.d("App", "Usuário atual: ${currentUser?.email ?: "Nenhum"}")
        } catch (e: Exception) {
            Log.e("App", "Erro ao verificar Firebase Auth", e)
        }
        
        // Inicializar componentes de forma assíncrona para evitar ANR
        applicationScope.launch {
            try {
                Log.d("App", "Inicializando componentes da aplicação...")
                
                // Aguardar um pouco para não bloquear a UI
                delay(50)
                
                Log.d("App", "Aplicação inicializada com sucesso")
            } catch (e: Exception) {
                Log.e("App", "Erro na inicialização da aplicação", e)
            }
        }

        // Sync quando o app volta ao foreground (global, independente da Activity atual).
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                PatientsSyncScheduler.enqueue(applicationContext, "foreground")
            }
        })
    }
    
    private fun setupUncaughtExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("App", "Exceção não capturada na thread: ${thread.name}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
    
    override fun onLowMemory() {
        super.onLowMemory()
        Log.w("App", "Memória baixa detectada")
        // Limpar caches se necessário
        System.gc()
    }
    
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Log.d("App", "Trim memory level: $level")
        when (level) {
            TRIM_MEMORY_RUNNING_CRITICAL, TRIM_MEMORY_RUNNING_LOW -> {
                // Limpar caches não essenciais
                System.gc()
            }
        }
    }
    
    override fun onTerminate() {
        super.onTerminate()
        // Cancelar coroutines pendentes
        applicationScope.cancel()
        Log.d("App", "Aplicação finalizada")
    }
} 



