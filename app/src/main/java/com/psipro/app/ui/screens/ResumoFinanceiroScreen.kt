package com.psipro.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.psipro.app.ui.compose.StatusColors
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.psipro.app.data.entities.StatusPagamento
import com.psipro.app.ui.viewmodels.CobrancaSessaoViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import com.psipro.app.ui.screens.StatusChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumoFinanceiroScreen(
    onBack: () -> Unit,
    onCobrancaClick: (Long) -> Unit,
    viewModel: CobrancaSessaoViewModel = hiltViewModel()
) {
    val resumo by viewModel.resumoFinanceiro.collectAsState()
    val cobrancas by viewModel.cobrancas.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var filtroSelecionado by remember { mutableStateOf<StatusPagamento?>(null) }
    var mostrarFiltros by remember { mutableStateOf(false) }
    
    val formatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

    LaunchedEffect(Unit) {
        viewModel.carregarCobrancasPorStatus(StatusPagamento.A_RECEBER)
    }

    LaunchedEffect(filtroSelecionado) {
        filtroSelecionado?.let { status ->
            viewModel.carregarCobrancasPorStatus(status)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resumo Financeiro") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Cards de resumo
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // Cards de totais
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = StatusColors.Success)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Recebido",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )
                                Text(
                                    text = formatter.format(resumo.totalRecebido),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = StatusColors.Warning)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "A Receber",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )
                                Text(
                                    text = formatter.format(resumo.totalAReceber),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = StatusColors.Error)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Vencidas",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )
                                Text(
                                    text = resumo.countVencidas.toString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = StatusColors.Info)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Pending,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Pendentes",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )
                                Text(
                                    text = resumo.countPendentes.toString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Filtros
                if (mostrarFiltros) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Filtros",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = filtroSelecionado == StatusPagamento.A_RECEBER,
                                        onClick = { 
                                            filtroSelecionado = if (filtroSelecionado == StatusPagamento.A_RECEBER) null else StatusPagamento.A_RECEBER
                                        },
                                        label = { Text("A Receber") },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = StatusColors.Warning
                                        )
                                    )
                                    FilterChip(
                                        selected = filtroSelecionado == StatusPagamento.VENCIDO,
                                        onClick = { 
                                            filtroSelecionado = if (filtroSelecionado == StatusPagamento.VENCIDO) null else StatusPagamento.VENCIDO
                                        },
                                        label = { Text("Vencidas") },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = StatusColors.Error
                                        )
                                    )
                                    FilterChip(
                                        selected = filtroSelecionado == StatusPagamento.PAGO,
                                        onClick = { 
                                            filtroSelecionado = if (filtroSelecionado == StatusPagamento.PAGO) null else StatusPagamento.PAGO
                                        },
                                        label = { Text("Pagas") },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = StatusColors.Success
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Lista de cobranças
                items(cobrancas) { cobranca ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCobrancaClick(cobranca.id) },
                        colors = CardDefaults.cardColors(
                                                    containerColor = when (cobranca.status) {
                            StatusPagamento.PAGO -> StatusColors.SuccessBackground
                            StatusPagamento.A_RECEBER -> StatusColors.WarningBackground
                            StatusPagamento.VENCIDO -> StatusColors.ErrorBackground
                            StatusPagamento.CANCELADO -> StatusColors.NeutralBackground
                        }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Sessão ${cobranca.numeroSessao}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Data: ${dateFormatter.format(cobranca.dataSessao)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "Vencimento: ${dateFormatter.format(cobranca.dataVencimento)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = formatter.format(cobranca.valor),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                StatusChip(status = cobranca.status)
                            }
                        }
                    }
                }
            }
        }
    }

    // Loading e error states
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    error?.let { errorMessage ->
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                TextButton(onClick = { viewModel.limparErro() }) {
                    Text("OK")
                }
            }
        ) {
            Text(errorMessage)
        }
    }
} 



