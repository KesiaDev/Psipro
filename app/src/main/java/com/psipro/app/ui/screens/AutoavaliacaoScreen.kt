package com.psipro.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

import androidx.compose.material.icons.filled.Tab
import androidx.compose.material3.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.psipro.app.ui.viewmodels.AutoavaliacaoViewModel
import com.psipro.app.data.entities.Autoavaliacao
import java.text.SimpleDateFormat
import java.util.*

enum class AutoavaliacaoTab {
    DASHBOARD, FORMULARIO, HISTORICO
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoavaliacaoScreen(
    onBack: () -> Unit,
    viewModel: AutoavaliacaoViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(AutoavaliacaoTab.DASHBOARD) }
    val autoavaliacoes by viewModel.autoavaliacoes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val currentAutoavaliacao by viewModel.currentAutoavaliacao.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.getLatestAutoavaliacao()
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                listOf("Dashboard", "Formulário", "Histórico").forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab.ordinal == index,
                        onClick = { selectedTab = AutoavaliacaoTab.values()[index] },
                        text = { 
                            Text(
                                title,
                                color = if (selectedTab.ordinal == index) Color(0xFFCD7F32) else if (isSystemInDarkTheme()) Color(0xFFD2691E) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            ) 
                        },
                        icon = {
                            Icon(
                                when (index) {
                                    0 -> Icons.Default.Dashboard
                                    1 -> Icons.Default.Assignment
                                    else -> Icons.Default.History
                                },
                                contentDescription = title,
                                tint = if (selectedTab.ordinal == index) Color(0xFFCD7F32) else if (isSystemInDarkTheme()) Color(0xFFD2691E) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    )
                }
            }
            
            // Content
            when (selectedTab) {
                AutoavaliacaoTab.DASHBOARD -> DashboardContent(
                    stats = stats,
                    currentAutoavaliacao = currentAutoavaliacao,
                    motivationalMessage = viewModel.getMotivationalMessage(),
                    onNewEvaluation = { selectedTab = AutoavaliacaoTab.FORMULARIO },
                    viewModel = viewModel
                )
                AutoavaliacaoTab.FORMULARIO -> FormularioContent(
                    onSave = { formData ->
                        viewModel.saveAutoavaliacao(
                            bemEstarEmocional = formData.bemEstarEmocional,
                            satisfacaoProfissional = formData.satisfacaoProfissional,
                            equilibrioVidaTrabalho = formData.equilibrioVidaTrabalho,
                            energiaVital = formData.energiaVital,
                            qualidadeSono = formData.qualidadeSono,
                            nivelEstresse = formData.nivelEstresse,
                            principaisDesafios = formData.principaisDesafios,
                            conquistasMes = formData.conquistasMes,
                            objetivosProximoMes = formData.objetivosProximoMes,
                            gratidao = formData.gratidao,
                            observacoes = formData.observacoes
                        )
                        selectedTab = AutoavaliacaoTab.DASHBOARD
                    }
                )
                AutoavaliacaoTab.HISTORICO -> HistoricoContent(
                    autoavaliacoes = autoavaliacoes,
                    onItemClick = { autoavaliacao ->
                        viewModel.getAutoavaliacaoById(autoavaliacao.id)
                    }
                )
            }
            
            // Error Snackbar
            error?.let { errorMessage ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(errorMessage)
                }
            }
            
            // Loading
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun DashboardContent(
    stats: AutoavaliacaoStats,
    currentAutoavaliacao: Autoavaliacao?,
    motivationalMessage: String,
    onNewEvaluation: () -> Unit,
    viewModel: AutoavaliacaoViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Mensagem motivacional
            Card(
                            colors = CardDefaults.cardColors(
                containerColor = if (isSystemInDarkTheme()) Color(0xFF1A1A1A) else Color(0xFFF5F5DC)
            )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Psychology,
                            contentDescription = null,
                            tint = Color(0xFFCD7F32) // primary_bronze
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mensagem do Dia",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSystemInDarkTheme()) Color(0xFFCD7F32) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = motivationalMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSystemInDarkTheme()) Color(0xFFE8E8E8) else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        
        item {
            // Configurações de notificação
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSystemInDarkTheme()) Color(0xFF2A2A2A) else Color(0xFFF5F5DC)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            tint = Color(0xFFCD7F32) // primary_bronze
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Notificações Diárias",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSystemInDarkTheme()) Color(0xFFCD7F32) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                            text = "Receba mensagens motivacionais diárias às 9h da manhã",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSystemInDarkTheme()) Color(0xFFD2691E) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ativar notificações",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSystemInDarkTheme()) Color(0xFFCD7F32) else MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = viewModel.notificationsEnabled.collectAsState().value,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    viewModel.enableDailyMotivationalNotifications()
                                } else {
                                    viewModel.disableDailyMotivationalNotifications()
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFCD7F32),
                                checkedTrackColor = Color(0xFFCD7F32).copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }
        }
        
        item {
            // Botão nova avaliação
            Button(
                onClick = onNewEvaluation,
                modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFCD7F32) // primary_bronze
            )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nova Autoavaliação")
            }
        }
        
        item {
            // Estatísticas
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "📊 Estatísticas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSystemInDarkTheme()) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatCard(
                            title = "Mês Atual",
                            value = stats.currentMonthAverage?.let { "%.1f".format(it) } ?: "N/A",
                            color = Color(0xFFCD7F32) // primary_bronze
                        )
                        StatCard(
                            title = "3 Meses",
                            value = stats.last3MonthsAverage?.let { "%.1f".format(it) } ?: "N/A",
                            color = Color(0xFFB87333) // secondary_bronze
                        )
                        StatCard(
                            title = "6 Meses",
                            value = stats.last6MonthsAverage?.let { "%.1f".format(it) } ?: "N/A",
                            color = Color(0xFFD2691E) // accent_bronze
                        )
                    }
                }
            }
        }
        
        item {
            // Última avaliação
            currentAutoavaliacao?.let { autoavaliacao ->
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "📝 Última Avaliação",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSystemInDarkTheme()) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
                        Text(
                            text = "Data: ${dateFormatter.format(autoavaliacao.dataAvaliacao)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSystemInDarkTheme()) Color(0xFFD2691E) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Score Geral:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSystemInDarkTheme()) Color(0xFFCD7F32) else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "%.1f".format(autoavaliacao.scoreGeral),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = getScoreColor(autoavaliacao.scoreGeral)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Categoria:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSystemInDarkTheme()) Color(0xFFCD7F32) else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = autoavaliacao.categoriaGeral,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = getCategoryColor(autoavaliacao.categoriaGeral)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSystemInDarkTheme()) Color(0xFFD2691E) else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun FormularioContent(
    onSave: (AutoavaliacaoFormData) -> Unit
) {
    var bemEstarEmocional by remember { mutableStateOf(5) }
    var satisfacaoProfissional by remember { mutableStateOf(5) }
    var equilibrioVidaTrabalho by remember { mutableStateOf(5) }
    var energiaVital by remember { mutableStateOf(5) }
    var qualidadeSono by remember { mutableStateOf(5) }
    var nivelEstresse by remember { mutableStateOf(5) }
    var principaisDesafios by remember { mutableStateOf("") }
    var conquistasMes by remember { mutableStateOf("") }
    var objetivosProximoMes by remember { mutableStateOf("") }
    var gratidao by remember { mutableStateOf("") }
    var observacoes by remember { mutableStateOf("") }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "📋 Formulário de Autoavaliação",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (isSystemInDarkTheme()) Color(0xFFCD7F32) else MaterialTheme.colorScheme.onSurface
            )
        }
        
        item {
            Text(
                text = "Avalie cada aspecto de 1 a 10:",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSystemInDarkTheme()) Color(0xFFD2691E) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Escalas
        item {
            ScaleQuestion(
                title = "Bem-estar Emocional",
                value = bemEstarEmocional,
                onValueChange = { bemEstarEmocional = it },
                description = "Como você se sente emocionalmente?"
            )
        }
        
        item {
            ScaleQuestion(
                title = "Satisfação Profissional",
                value = satisfacaoProfissional,
                onValueChange = { satisfacaoProfissional = it },
                description = "Quão satisfeito você está com seu trabalho?"
            )
        }
        
        item {
            ScaleQuestion(
                title = "Equilíbrio Vida-Trabalho",
                value = equilibrioVidaTrabalho,
                onValueChange = { equilibrioVidaTrabalho = it },
                description = "Como está o equilíbrio entre vida pessoal e profissional?"
            )
        }
        
        item {
            ScaleQuestion(
                title = "Energia Vital",
                value = energiaVital,
                onValueChange = { energiaVital = it },
                description = "Como você avalia seu nível de energia?"
            )
        }
        
        item {
            ScaleQuestion(
                title = "Qualidade do Sono",
                value = qualidadeSono,
                onValueChange = { qualidadeSono = it },
                description = "Como você avalia a qualidade do seu sono?"
            )
        }
        
        item {
            ScaleQuestion(
                title = "Nível de Estresse",
                value = nivelEstresse,
                onValueChange = { nivelEstresse = it },
                description = "Como você avalia seu nível de estresse? (10 = baixo estresse)"
            )
        }
        
        // Perguntas qualitativas
        item {
            Text(
                text = "Reflexões:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            TextField(
                value = principaisDesafios,
                onValueChange = { principaisDesafios = it },
                label = { Text("Principais desafios do mês") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        }
        
        item {
            TextField(
                value = conquistasMes,
                onValueChange = { conquistasMes = it },
                label = { Text("Conquistas do mês") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        }
        
        item {
            TextField(
                value = objetivosProximoMes,
                onValueChange = { objetivosProximoMes = it },
                label = { Text("Objetivos para o próximo mês") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        }
        
        item {
            TextField(
                value = gratidao,
                onValueChange = { gratidao = it },
                label = { Text("3 coisas pelas quais sou grato") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        }
        
        item {
            TextField(
                value = observacoes,
                onValueChange = { observacoes = it },
                label = { Text("Observações adicionais (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
        }
        
        item {
            Button(
                onClick = {
                    onSave(
                        AutoavaliacaoFormData(
                            bemEstarEmocional = bemEstarEmocional,
                            satisfacaoProfissional = satisfacaoProfissional,
                            equilibrioVidaTrabalho = equilibrioVidaTrabalho,
                            energiaVital = energiaVital,
                            qualidadeSono = qualidadeSono,
                            nivelEstresse = nivelEstresse,
                            principaisDesafios = principaisDesafios,
                            conquistasMes = conquistasMes,
                            objetivosProximoMes = objetivosProximoMes,
                            gratidao = gratidao,
                            observacoes = observacoes.ifEmpty { null }
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = principaisDesafios.isNotBlank() && conquistasMes.isNotBlank() && 
                         objetivosProximoMes.isNotBlank() && gratidao.isNotBlank()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Salvar Autoavaliação")
            }
        }
    }
}

@Composable
fun ScaleQuestion(
    title: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isSystemInDarkTheme()) Color(0xFFCD7F32) else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSystemInDarkTheme()) Color(0xFFD2691E) else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                (1..10).forEach { scaleValue ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                                color = if (scaleValue <= value) Color(0xFFCD7F32) // primary_bronze
                else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onValueChange(scaleValue) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = scaleValue.toString(),
                            style = MaterialTheme.typography.bodySmall,
                                            color = if (scaleValue <= value) Color.White
                else if (isSystemInDarkTheme()) Color(0xFFD2691E) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Valor selecionado: $value",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFCD7F32), // primary_bronze
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun HistoricoContent(
    autoavaliacoes: List<Autoavaliacao>,
    onItemClick: (Autoavaliacao) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "📚 Histórico de Autoavaliações",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (isSystemInDarkTheme()) Color(0xFFCD7F32) else MaterialTheme.colorScheme.onSurface
            )
        }
        
        if (autoavaliacoes.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                    containerColor = if (isSystemInDarkTheme()) Color(0xFF1A1A1A) else Color(0xFFF5F5DC)
                )
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Nenhuma autoavaliação encontrada",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = if (isSystemInDarkTheme()) Color(0xFFD2691E) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Faça sua primeira autoavaliação para começar a acompanhar sua evolução!",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = if (isSystemInDarkTheme()) Color(0xFFD2691E) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(autoavaliacoes) { autoavaliacao ->
                HistoricoItem(
                    autoavaliacao = autoavaliacao,
                    onClick = { onItemClick(autoavaliacao) }
                )
            }
        }
    }
}

@Composable
fun HistoricoItem(
    autoavaliacao: Autoavaliacao,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
                Text(
                    text = dateFormatter.format(autoavaliacao.dataAvaliacao),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isSystemInDarkTheme()) Color(0xFFCD7F32) else MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = autoavaliacao.categoriaGeral,
                    style = MaterialTheme.typography.bodySmall,
                    color = getCategoryColor(autoavaliacao.categoriaGeral),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Score Geral:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSystemInDarkTheme()) Color(0xFFCD7F32) else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "%.1f".format(autoavaliacao.scoreGeral),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = getScoreColor(autoavaliacao.scoreGeral)
                )
            }
            
            if (autoavaliacao.principaisDesafios.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Desafios: ${autoavaliacao.principaisDesafios.take(50)}${if (autoavaliacao.principaisDesafios.length > 50) "..." else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSystemInDarkTheme()) Color(0xFFD2691E) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

data class AutoavaliacaoFormData(
    val bemEstarEmocional: Int,
    val satisfacaoProfissional: Int,
    val equilibrioVidaTrabalho: Int,
    val energiaVital: Int,
    val qualidadeSono: Int,
    val nivelEstresse: Int,
    val principaisDesafios: String,
    val conquistasMes: String,
    val objetivosProximoMes: String,
    val gratidao: String,
    val observacoes: String?
)

data class AutoavaliacaoStats(
    val currentMonthAverage: Float? = null,
    val last3MonthsAverage: Float? = null,
    val last6MonthsAverage: Float? = null
)

fun getScoreColor(score: Float): Color {
    return when {
        score >= 8.5f -> Color(0xFF4CAF50) // Verde
        score >= 7.0f -> Color(0xFF8BC34A) // Verde claro
        score >= 5.5f -> Color(0xFFFF9800) // Laranja
        else -> Color(0xFFF44336) // Vermelho
    }
}

fun getCategoryColor(category: String): Color {
    return when (category) {
        "Excelente" -> Color(0xFF4CAF50) // Verde
        "Bom" -> Color(0xFF8BC34A) // Verde claro
        "Regular" -> Color(0xFFFF9800) // Laranja
        "Precisa Atenção" -> Color(0xFFF44336) // Vermelho
        else -> Color(0xFF9E9E9E) // Cinza
    }
} 



