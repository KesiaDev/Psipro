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
class FinanceiroDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PsiproTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FinanceiroDashboardScreen(
                        onBack = { finish() },
                        onCobrancaClick = { cobrancaId ->
                            // TODO: Implementar navegação para detalhes da cobrança
                        }
                    )
                }
            }
        }
    }
} 