package com.example.psipro.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.psipro.ui.compose.PsiproTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AnotacoesSessaoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val patientId = intent.getLongExtra("PATIENT_ID", -1)
        setContent {
            PsiproTheme {
                Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier) {
                    AnotacoesSessaoScreen(
                        patientId = patientId,
                        onBack = { finish() },
                        viewModel = androidx.hilt.navigation.compose.hiltViewModel()
                    )
                }
            }
        }
    }
} 