package com.psipro.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.psipro.app.ui.compose.StatusColors
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.psipro.app.data.entities.CobrancaSessao
import com.psipro.app.data.entities.StatusPagamento
import com.psipro.app.ui.viewmodels.CobrancaSessaoViewModel
import com.psipro.app.utils.WhatsAppUtils
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import com.psipro.app.ui.screens.StatusChip
import androidx.compose.foundation.background

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalheCobrancaScreen(
    cobrancaId: Long,
    onBack: () -> Unit,
    viewModel: CobrancaSessaoViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val cobrancas by viewModel.cobrancas.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    val cobranca = cobrancas.find { it.id == cobrancaId }
    
    val formatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    val dateTimeFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))

    LaunchedEffect(cobrancaId) {
        // Carregar cobranças se necessário
        if (cobrancas.isEmpty()) {
            viewModel.carregarCobrancasPorStatus(StatusPagamento.A_RECEBER)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes da Cobrança") },
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
        } else if (cobranca == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Cobrança não encontrada")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Card principal
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (cobranca.status) {
                            StatusPagamento.PAGO -> StatusColors.SuccessBackground
                            StatusPagamento.A_RECEBER -> StatusColors.WarningBackground
                            StatusPagamento.VENCIDO -> StatusColors.ErrorBackground
                            StatusPagamento.CANCELADO -> StatusColors.NeutralBackground
                        }
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sessão ${cobranca.numeroSessao}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            StatusChip(status = cobranca.status)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = formatter.format(cobranca.valor),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Informações da sessão
                        InfoRow("Data da Sessão", dateTimeFormatter.format(cobranca.dataSessao))
                        InfoRow("Data de Vencimento", dateFormatter.format(cobranca.dataVencimento))
                        cobranca.dataPagamento?.let { dataPagamento ->
                            InfoRow("Data do Pagamento", dateTimeFormatter.format(dataPagamento))
                        }
                        
                        if (cobranca.observacoes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Observações:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = cobranca.observacoes,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Botões de ação
                if (cobranca.status != StatusPagamento.PAGO) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Ações",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.marcarComoPago(cobranca.id) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusColors.Success)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Marcar Pago")
                                }
                                
                                Button(
                                    onClick = {
                                        // Aqui você precisaria do telefone do paciente
                                        // Por enquanto, mostra um toast
                                        android.widget.Toast.makeText(
                                            context,
                                            "Funcionalidade WhatsApp - Telefone do paciente necessário",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusColors.Info)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("WhatsApp")
                                }
                            }
                        }
                    }
                }

                // PIX Copia e Cola
                if (cobranca.pixCopiaCola.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "PIX Copia e Cola",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = cobranca.pixCopiaCola,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surface,
                                        shape = MaterialTheme.shapes.small
                                    )
                                    .padding(8.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    WhatsAppUtils.copiarParaClipboard(context, cobranca.pixCopiaCola)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Copiar PIX")
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

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
} 



