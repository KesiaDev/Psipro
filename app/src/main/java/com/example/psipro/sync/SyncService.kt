package com.example.psipro.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.psipro.data.AppDatabase
import com.example.psipro.data.entities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncService @Inject constructor(
    private val context: Context,
    private val database: AppDatabase
) {
    
    suspend fun syncData(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!isNetworkAvailable()) {
                return@withContext Result.failure(Exception("Sem conexão com a internet"))
            }
            
            // Sincronizar pacientes
            val patients = database.patientDao().getAllPatients()
            // TODO: Implementar sincronização com servidor
            
            // Sincronizar consultas
            val appointments = database.appointmentDao().getAllAppointments()
            // TODO: Implementar sincronização com servidor
            
            // Sincronizar anotações
            val notes = database.patientNoteDao().getAllNotes()
            // TODO: Implementar sincronização com servidor
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
} 