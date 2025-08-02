package com.psipro.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.psipro.app.ui.compose.PsiproTheme
import com.psipro.app.ui.screens.FinanceiroPacienteScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FinanceiroPacienteActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val patientId = intent.getLongExtra("PATIENT_ID", -1)
        val patientName = intent.getStringExtra("PATIENT_NAME") ?: ""
        
        if (patientId == -1L) {
            finish()
            return
        }
        
        setContent {
            PsiproTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FinanceiroPacienteScreen(
                        patientId = patientId,
                        patientName = patientName,
                        onBack = { finish() }
                    )
                }
            }
        }
    }
} 



