package com.example.psipro.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.psipro.ui.compose.PsiproTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AnamneseActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val pacienteId = intent.getLongExtra("PACIENTE_ID", -1L)
        val pacienteNome = intent.getStringExtra("PACIENTE_NOME") ?: "Paciente"
        
        if (pacienteId == -1L) {
            finish()
            return
        }
        
        setContent {
            PsiproTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AnamneseCompleteFlow(
                        pacienteId = pacienteId,
                        pacienteNome = pacienteNome,
                        onBack = { finish() }
                    )
                }
            }
        }
    }
} 