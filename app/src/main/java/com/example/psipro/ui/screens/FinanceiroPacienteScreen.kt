package com.example.psipro.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.psipro.data.entities.StatusPagamento
import com.example.psipro.ui.viewmodels.CobrancaSessaoViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceiroPacienteScreen(
    patientId: Long,
    patientName: String,
    onBack: () -> Unit,
    viewModel: CobrancaSessaoViewModel = hiltViewModel(),
    patientViewModel: com.example.psipro.ui.viewmodels.PatientViewModel = hiltViewModel()
) {
    val resumo by viewModel.resumoFinanceiro.collectAsState()
    val cobrancas by viewModel.cobrancas.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    val formatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

    // Carregar dados do paciente
    val currentPatient by patientViewModel.currentPatient.collectAsState()
    var sessionValue by remember { mutableStateOf("") }
    var diaCobranca by remember { mutableStateOf(1) }
    var lembreteCobranca by remember { mutableStateOf(false) }
    var showSaved by remember { mutableStateOf(false) }

    LaunchedEffect(patientId) {
        viewModel.carregarCobrancasPorPaciente(patientId)
        patientViewModel.loadPatient(patientId)
    }

    // Atualiza campos ao carregar paciente
    LaunchedEffect(currentPatient) {
        currentPatient?.let {
            sessionValue = if (it.sessionValue == 0.0) "" else formatter.format(it.sessionValue)
            diaCobranca = it.diaCobranca
            lembreteCobranca = it.lembreteCobranca
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Financeiro - $patientName") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Formulário de edição dos dados financeiros
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Dados financeiros do paciente", style = MaterialTheme.typography.titleMedium)
                            OutlinedTextField(
                                value = sessionValue,
                                onValueChange = { sessionValue = it },
                                label = { Text("Valor da sessão (R$") },
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = diaCobranca.toString(),
                                onValueChange = { v -> v.toIntOrNull()?.let { diaCobranca = it } },
                                label = { Text("Dia da cobrança") },
                                singleLine = true
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = lembreteCobranca,
                                    onCheckedChange = { lembreteCobranca = it }
                                )
                                Text("Ativar lembrete de cobrança")
                            }
                            Button(onClick = {
                                currentPatient?.let { p ->
                                    val valor = sessionValue.replace(Regex("""[^\d,\.]"""), "").replace(",", ".").toDoubleOrNull() ?: 0.0
                                    val updated = p.copy(
                                        sessionValue = valor,
                                        diaCobranca = diaCobranca,
                                        lembreteCobranca = lembreteCobranca
                                    )
                                    patientViewModel.savePatient(updated)
                                    showSaved = true
                                }
                            }, modifier = Modifier.align(Alignment.End)) {
                                Text("Salvar")
                            }
                            if (showSaved) {
                                Text("Salvo com sucesso!", color = Color(0xFF4CAF50))
                            }
                        }
                    }
                }

                // Cards de resumo do paciente
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50))
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
                                    text = "Total Pago",
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
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFF9800))
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

                // Lista de cobranças do paciente
                if (cobrancas.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Nenhuma cobrança encontrada para este paciente.",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Cadastre uma anotação de sessão para gerar cobranças automaticamente.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(cobrancas) { cobranca ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // Aqui você pode abrir o detalhe da cobrança
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = when (cobranca.status) {
                                    StatusPagamento.PAGO -> Color(0xFFE8F5E8)
                                    StatusPagamento.A_RECEBER -> Color(0xFFFFF3E0)
                                    StatusPagamento.VENCIDO -> Color(0xFFFFEBEE)
                                    StatusPagamento.CANCELADO -> Color(0xFFF5F5F5)
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