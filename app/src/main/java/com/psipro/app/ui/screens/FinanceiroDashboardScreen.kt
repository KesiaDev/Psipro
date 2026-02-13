package com.psipro.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.psipro.app.ui.viewmodels.FinanceiroUnificadoViewModel
import com.psipro.app.ui.compose.StatusColors
import java.text.NumberFormat
import java.util.*

@Composable
fun FinanceiroDashboardScreen(
    onBack: () -> Unit,
    onCobrancaClick: (Long) -> Unit,
    viewModel: FinanceiroUnificadoViewModel = hiltViewModel()
) {
    // Estados ultra-simplificados - sem carregamento automático
    val resumo by viewModel.resumoFinanceiro.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    val formatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    
    // SEM LaunchedEffect para evitar problemas de inicialização
    // Carregamento apenas manual
    
    val onRefresh: () -> Unit = {
        try {
            android.util.Log.d("FinanceiroDashboard", "Recarregamento manual")
            viewModel.forcarRecarregamento()
        } catch (e: Exception) {
            android.util.Log.e("FinanceiroDashboard", "Erro ao recarregar dados", e)
        }
    }
    
    // Layout usando tema Material 3
    val colorScheme = MaterialTheme.colorScheme
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        // Header usando tema
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = colorScheme.primary
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Voltar",
                        tint = colorScheme.onPrimary
                    )
                }
                
                Text(
                    text = "Financeiro",
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.onPrimary
                )
                
                IconButton(onClick = onRefresh) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Atualizar",
                        tint = colorScheme.onPrimary
                    )
                }
            }
        }
        
        // Conteúdo principal usando tema
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Botão para carregar dados manualmente
            Button(
                onClick = onRefresh,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
            ) {
                Text("Carregar Dados Financeiros", color = colorScheme.onPrimary)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Mostrar erro se houver
            if (error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.errorContainer)
                ) {
                    Text(
                        text = "Erro: ${error}",
                        color = colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else if (isLoading) {
                // Loading usando tema
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Carregando...",
                        color = colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                // Dashboard usando tema e cards Material 3
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Header melhorado - UX: Clareza sobre o que está sendo mostrado
                    Column(modifier = Modifier.padding(bottom = 8.dp)) {
                        Text(
                            text = "Resumo do Consultório",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )
                        Text(
                            text = "Visão geral das finanças",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    // Card: Total Recebido
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = StatusColors.SuccessBackground)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Total Recebido",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatter.format(resumo.totalRecebido),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = StatusColors.Success
                            )
                        }
                    }
                    
                    // Card: A Receber
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = StatusColors.WarningBackground)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "A Receber",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatter.format(resumo.totalAReceber),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = StatusColors.Warning
                            )
                        }
                    }
                    
                    // Card: Pendentes e Vencidas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Pendentes",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = resumo.countPendentes.toString(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusColors.Warning
                                )
                            }
                        }
                        
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = StatusColors.ErrorBackground)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Vencidas",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = resumo.countVencidas.toString(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusColors.Error
                                )
                            }
                        }
                    }
                    
                    // Card: Total Geral
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Total Geral",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = formatter.format(resumo.totalRecebido + resumo.totalAReceber),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}