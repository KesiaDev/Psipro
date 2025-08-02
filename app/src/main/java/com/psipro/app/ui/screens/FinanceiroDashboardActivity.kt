package com.psipro.app.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.psipro.app.ui.compose.PsiproTheme
import com.psipro.app.ui.viewmodels.FinanceiroUnificadoViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FinanceiroDashboardActivity : ComponentActivity() {
    private val viewModel: FinanceiroUnificadoViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PsiproTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FinanceiroDashboardScreen(
                        onBack = { 
                            try {
                                viewModel.limparEstado()
                                finish() 
                            } catch (e: Exception) {
                                // Fallback se houver erro
                                onBackPressed()
                            }
                        },
                        onCobrancaClick = { cobrancaId ->
                            // TODO: Implementar navegação para detalhes da cobrança
                        }
                    )
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        try {
            viewModel.limparEstado()
        } catch (e: Exception) {
            // Ignorar erros durante a destruição
        }
    }
} 



