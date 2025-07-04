package com.example.psipro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.psipro.ui.viewmodels.CobrancaSessaoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuPrincipalScreen(
    onPacientesClick: () -> Unit,
    onAgendamentosClick: () -> Unit,
    onAnamneseClick: () -> Unit,
    onFinanceiroClick: () -> Unit,
    onRelatoriosClick: () -> Unit,
    onConfiguracoesClick: () -> Unit,
    viewModel: CobrancaSessaoViewModel = hiltViewModel()
) {
    val resumoFinanceiro by viewModel.resumoFinanceiro.collectAsState()
    
    val menuItems = listOf(
        MenuItem(
            titulo = "Pacientes",
            icone = Icons.Default.People,
            cor = Color(0xFF2196F3),
            onClick = onPacientesClick
        ),
        MenuItem(
            titulo = "Agendamentos",
            icone = Icons.Default.Schedule,
            cor = Color(0xFF4CAF50),
            onClick = onAgendamentosClick
        ),
        MenuItem(
            titulo = "Anamnese",
            icone = Icons.Default.Assignment,
            cor = Color(0xFFFF9800),
            onClick = onAnamneseClick
        ),
        MenuItem(
            titulo = "Financeiro",
            icone = Icons.Default.AccountBalanceWallet,
            cor = Color(0xFF9C27B0),
            onClick = onFinanceiroClick,
            badge = resumoFinanceiro.countPendentes + resumoFinanceiro.countVencidas
        ),
        MenuItem(
            titulo = "Relatórios",
            icone = Icons.Default.Assessment,
            cor = Color(0xFF607D8B),
            onClick = onRelatoriosClick
        ),
        MenuItem(
            titulo = "Configurações",
            icone = Icons.Default.Settings,
            cor = Color(0xFF795548),
            onClick = onConfiguracoesClick
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PsiPro") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Resumo financeiro rápido
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Resumo Financeiro",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ResumoItem(
                            label = "A Receber",
                            value = "R$ ${String.format("%.2f", resumoFinanceiro.totalAReceber)}",
                            color = Color(0xFFFF9800)
                        )
                        ResumoItem(
                            label = "Vencidas",
                            value = resumoFinanceiro.countVencidas.toString(),
                            color = Color(0xFFF44336)
                        )
                        ResumoItem(
                            label = "Pendentes",
                            value = resumoFinanceiro.countPendentes.toString(),
                            color = Color(0xFF2196F3)
                        )
                    }
                }
            }

            // Grid de menu
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(menuItems) { item ->
                    MenuCard(item = item)
                }
            }
        }
    }
}

@Composable
fun MenuCard(item: MenuItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { item.onClick() },
        colors = CardDefaults.cardColors(containerColor = item.cor.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box {
                    Icon(
                        imageVector = item.icone,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = item.cor
                    )
                    
                    // Badge para notificações
                    item.badge?.let { badgeCount ->
                        if (badgeCount > 0) {
                            Surface(
                                color = Color(0xFFF44336),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 8.dp, y = (-8).dp)
                            ) {
                                Text(
                                    text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = item.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun ResumoItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

data class MenuItem(
    val titulo: String,
    val icone: androidx.compose.ui.graphics.vector.ImageVector,
    val cor: Color,
    val onClick: () -> Unit,
    val badge: Int? = null
) 