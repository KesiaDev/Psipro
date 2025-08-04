package com.psipro.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.vector.ImageVector
import com.psipro.app.ui.compose.StatusColors
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.psipro.app.data.entities.StatusPagamento
import com.psipro.app.ui.viewmodels.FinanceiroUnificadoViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar

enum class FinanceiroTab {
    PAINEL, RECEITAS, DESPESAS
}

enum class PeriodoFiltro {
    HOJE, SEMANA, MES, TRIMESTRE, SEMESTRE, ANO, PERSONALIZADO
}

// Cores modernas para gradientes
object ModernColors {
    val PrimaryGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF667eea), Color(0xFF764ba2)),
        tileMode = TileMode.Clamp
    )
    val SuccessGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF56ab2f), Color(0xFFa8e6cf)),
        tileMode = TileMode.Clamp
    )
    val WarningGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFf093fb), Color(0xFFf5576c)),
        tileMode = TileMode.Clamp
    )
    val ErrorGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFff9a9e), Color(0xFFfecfef)),
        tileMode = TileMode.Clamp
    )
    val InfoGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF4facfe), Color(0xFF00f2fe)),
        tileMode = TileMode.Clamp
    )
    
    val CardBackground = Color(0xFFf8fafc)
    val CardShadow = Color(0xFFe2e8f0)
    val TextPrimary = Color(0xFF1e293b)
    val TextSecondary = Color(0xFF64748b)
    val TextMuted = Color(0xFF94a3b8)
}

// Componente moderno para card de estatística
@Composable
fun ModernStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    gradient: Brush,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(6.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(14.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ícone com background circular
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Valor
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                // Título
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// Componente para progresso visual
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(45.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 4.dp,
            color = Color(0xFF667eea),
            trackColor = Color(0xFFe2e8f0)
        )
        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = ModernColors.TextPrimary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceiroDashboardScreen(
    onBack: () -> Unit,
    onCobrancaClick: (Long) -> Unit,
    viewModel: FinanceiroUnificadoViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(FinanceiroTab.PAINEL) }
    var selectedDate by remember { mutableStateOf(Date()) }
    var showCustomCalendar by remember { mutableStateOf(false) }
    var selectedClient by remember { mutableStateOf("Todos") }
    var selectedPeriodo by remember { mutableStateOf(PeriodoFiltro.MES) }
    var showPeriodoMenu by remember { mutableStateOf(false) }
    
    val resumo by viewModel.resumoFinanceiro.collectAsState()
    val cobrancas by viewModel.cobrancas.collectAsState()
    val financialRecords by viewModel.financialRecords.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val periodoAtual by viewModel.periodoAtual.collectAsState()
    
    val formatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    val dateFormatter = SimpleDateFormat("MMMM/yyyy", Locale("pt", "BR"))
    val calendar = Calendar.getInstance()
    calendar.time = selectedDate
    
    val context = LocalContext.current
    
    LaunchedEffect(key1 = true) {
        // Carregar dados apenas uma vez quando a tela é criada
        viewModel.carregarDadosFinanceiros()
    }
    
    // Recarregar dados quando a tela voltar ao foco
    LaunchedEffect(key1 = Unit) {
        viewModel.carregarDadosFinanceiros()
    }
    
    // Adicionar um botão de refresh para atualizar manualmente
    val onRefresh = {
        viewModel.forcarRecarregamento()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ModernColors.CardBackground)
            .padding(16.dp)
    ) {
        // Header moderno com gradiente
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .background(ModernColors.PrimaryGradient)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .size(40.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color.White
                    )
                }
                
                Text(
                    text = "Dashboard Financeiro",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Atualizar",
                        tint = Color.White
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Cards de estatísticas modernos
        if (isLoading) {
            // Mostrar loading
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        color = ModernColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Carregando dados financeiros...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ModernColors.TextSecondary
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    ModernStatCard(
                        title = "Recebido",
                        value = formatter.format(resumo.totalRecebido),
                        icon = Icons.Outlined.AccountBalanceWallet,
                        gradient = ModernColors.SuccessGradient
                    )
                }
                item {
                    ModernStatCard(
                        title = "A Receber",
                        value = formatter.format(resumo.totalAReceber),
                        icon = Icons.Outlined.Schedule,
                        gradient = ModernColors.WarningGradient
                    )
                }
                item {
                    ModernStatCard(
                        title = "Pendentes",
                        value = "${resumo.countPendentes}",
                        icon = Icons.Outlined.Pending,
                        gradient = ModernColors.ErrorGradient
                    )
                }
                item {
                    ModernStatCard(
                        title = "Total",
                        value = formatter.format(resumo.totalRecebido + resumo.totalAReceber),
                        icon = Icons.Outlined.TrendingUp,
                        gradient = ModernColors.InfoGradient
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Seletor de período moderno
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.DateRange,
                            contentDescription = "Período",
                            tint = ModernColors.TextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Período Selecionado",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ModernColors.TextPrimary
                        )
                    }
                    
                    // Dropdown moderno
                    Box {
                        Card(
                            modifier = Modifier
                                .clickable { showPeriodoMenu = true }
                                .background(ModernColors.PrimaryGradient, RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = when (selectedPeriodo) {
                                        PeriodoFiltro.HOJE -> "Hoje"
                                        PeriodoFiltro.SEMANA -> "Última semana"
                                        PeriodoFiltro.MES -> "Este mês"
                                        PeriodoFiltro.TRIMESTRE -> "Este trimestre"
                                        PeriodoFiltro.SEMESTRE -> "Este semestre"
                                        PeriodoFiltro.ANO -> "Este ano"
                                        PeriodoFiltro.PERSONALIZADO -> dateFormatter.format(selectedDate)
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Selecionar período",
                                    tint = Color.White
                                )
                            }
                        }
                        
                        DropdownMenu(
                            expanded = showPeriodoMenu,
                            onDismissRequest = { showPeriodoMenu = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            PeriodoFiltro.values().forEach { periodo ->
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            when (periodo) {
                                                PeriodoFiltro.HOJE -> "Hoje"
                                                PeriodoFiltro.SEMANA -> "Última semana"
                                                PeriodoFiltro.MES -> "Este mês"
                                                PeriodoFiltro.TRIMESTRE -> "Este trimestre"
                                                PeriodoFiltro.SEMESTRE -> "Este semestre"
                                                PeriodoFiltro.ANO -> "Este ano"
                                                PeriodoFiltro.PERSONALIZADO -> "Personalizado"
                                            }
                                        )
                                    },
                                    onClick = {
                                        selectedPeriodo = periodo
                                        showPeriodoMenu = false
                                        if (periodo == PeriodoFiltro.PERSONALIZADO) {
                                            showCustomCalendar = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Progresso visual
                if (!isLoading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val total = resumo.totalRecebido + resumo.totalAReceber
                        val progress = if (total > 0) (resumo.totalRecebido / total).toFloat() else 0f
                        
                        ProgressRing(
                            progress = progress,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(
                            modifier = Modifier.weight(2f)
                        ) {
                            Text(
                                text = "Progresso de Recebimento",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = ModernColors.TextSecondary
                            )
                            Text(
                                text = "${(progress * 100).toInt()}% concluído",
                                style = MaterialTheme.typography.bodySmall,
                                color = ModernColors.TextMuted
                            )
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = ModernColors.TextPrimary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Carregando dados...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ModernColors.TextSecondary
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Tabs modernos
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = Color.White,
            contentColor = ModernColors.TextPrimary
        ) {
            FinanceiroTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = { 
                        Text(
                            text = when (tab) {
                                FinanceiroTab.PAINEL -> "Painel"
                                FinanceiroTab.RECEITAS -> "Receitas"
                                FinanceiroTab.DESPESAS -> "Despesas"
                            },
                            fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }
        
        // Conteúdo baseado na tab selecionada
        when (selectedTab) {
            FinanceiroTab.PAINEL -> {
                // Conteúdo do painel moderno
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text(
                                    text = "Resumo Financeiro",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = ModernColors.TextPrimary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Total de receitas: ${formatter.format(resumo.totalRecebido + resumo.totalAReceber)}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = ModernColors.TextSecondary
                                )
                            }
                        }
                    }
                }
            }
            FinanceiroTab.RECEITAS -> {
                // Conteúdo de receitas
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text(
                                    text = "Receitas do Período",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = ModernColors.TextPrimary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Lista de receitas
                                if (cobrancas.isNotEmpty()) {
                                    cobrancas.forEach { cobranca ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Paciente #${cobranca.patientId}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium,
                                                    color = ModernColors.TextPrimary
                                                )
                                                Text(
                                                    text = cobranca.dataSessao?.let { 
                                                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
                                                    } ?: "Data não informada",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = ModernColors.TextSecondary
                                                )
                                            }
                                            Text(
                                                text = formatter.format(cobranca.valor),
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = when (cobranca.status) {
                                                    StatusPagamento.PAGO -> Color(0xFF22c55e)
                                                    StatusPagamento.A_RECEBER -> Color(0xFFf59e0b)
                                                    StatusPagamento.CANCELADO -> Color(0xFFef4444)
                                                    else -> ModernColors.TextPrimary
                                                }
                                            )
                                        }
                                        if (cobranca != cobrancas.last()) {
                                            Divider(color = ModernColors.CardShadow, thickness = 1.dp)
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "Nenhuma receita encontrada para o período selecionado",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = ModernColors.TextSecondary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
            FinanceiroTab.DESPESAS -> {
                // Conteúdo de despesas
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text(
                                    text = "Despesas do Período",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = ModernColors.TextPrimary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Lista de despesas
                                if (financialRecords.isNotEmpty()) {
                                    financialRecords.forEach { record ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = record.description,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium,
                                                    color = ModernColors.TextPrimary
                                                )
                                                Text(
                                                    text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(record.date),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = ModernColors.TextSecondary
                                                )
                                            }
                                            Text(
                                                text = "-${formatter.format(record.value)}",
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFef4444)
                                            )
                                        }
                                        if (record != financialRecords.last()) {
                                            Divider(color = ModernColors.CardShadow, thickness = 1.dp)
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "Nenhuma despesa registrada para o período selecionado",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = ModernColors.TextSecondary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Calendário customizado
    if (showCustomCalendar) {
        AlertDialog(
            onDismissRequest = { showCustomCalendar = false },
            title = { Text("Selecionar Data") },
            text = { Text("Calendário em desenvolvimento") },
            confirmButton = {
                TextButton(onClick = { showCustomCalendar = false }) {
                    Text("OK")
                }
            }
        )
    }
} 



