package com.example.psipro.ui.screens

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.example.psipro.ui.compose.PsiproTheme
import com.example.psipro.data.entities.AnamneseGroup
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SimplifiedAnamneseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val patientId = intent.getLongExtra("PATIENT_ID", -1)
        val patientName = intent.getStringExtra("PATIENT_NAME") ?: "Paciente"
        val anamneseGroup = intent.getStringExtra("ANAMNESE_GROUP")?.let { 
            try { AnamneseGroup.valueOf(it) } catch (e: Exception) { AnamneseGroup.ADULTO }
        } ?: AnamneseGroup.ADULTO
        
        setContent {
            // Usar a mesma detecção de tema que o resto do aplicativo
            val isDarkTheme = isSystemInDarkTheme()
            
            // Log para debug
            Log.d("SimplifiedAnamneseActivity", "isSystemInDarkTheme(): $isDarkTheme")
            Log.d("SimplifiedAnamneseActivity", "Final theme decision: ${if (isDarkTheme) "DARK" else "LIGHT"}")
            
            PsiproTheme(useDarkTheme = isDarkTheme) {
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    SimplifiedAnamneseScreen(
                        navController = rememberNavController(),
                        patientName = patientName,
                        anamneseGroup = anamneseGroup,
                        onBack = { finish() }
                    )
                }
            }
        }
    }
} 