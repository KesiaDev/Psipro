package com.psipro.app.ui.screens

import com.psipro.app.data.entities.TipoSessao
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import java.text.SimpleDateFormat
import java.util.*
import com.psipro.app.ui.viewmodels.AnotacaoSessaoViewModel
import com.psipro.app.ui.viewmodels.TipoSessaoViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import android.content.Context
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import android.util.Log
import androidx.compose.material.icons.filled.Add

// Dados auxiliares

data class SessaoInfo(
    val id: Long,
    val numero: Int,
    val total: Int,
    val dataHora: String,
    val status: String,
    val anotacao: AnotacaoSessaoData? = null
)

data class AnotacaoSessaoData(
    val assuntos: String = "",
    val estadoEmocional: String = "",
    val intervencoes: String = "",
    val tarefas: String = "",
    val evolucao: String = "",
    val observacoes: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnotacoesSessaoScreen(
    patientId: Long,
    patientName: String,
    onSave: () -> Unit = {},
    onExportPdf: (Long) -> Unit = {},
    onAttach: (Long) -> Unit = {},
    onBack: () -> Unit,
    viewModel: AnotacaoSessaoViewModel
) {
    val anotacoes by viewModel.anotacoes.collectAsState()
    val anotacaoSelecionada by viewModel.anotacaoSelecionada.collectAsState()
    
    var assuntos by remember { mutableStateOf("") }
    var estadoEmocional by remember { mutableStateOf("") }
    var intervencoes by remember { mutableStateOf("") }
    var tarefas by remember { mutableStateOf("") }
    var evolucao by remember { mutableStateOf("") }
    var observacoes by remember { mutableStateOf("") }
    
    var sessaoSelecionada by remember { mutableStateOf<Int?>(null) }
    
    // ViewModel para tipos de sessão
    val tipoSessaoViewModel: TipoSessaoViewModel = hiltViewModel()
    val tiposSessao by tipoSessaoViewModel.tiposSessao.collectAsState()
    var tipoSelecionado by remember { mutableStateOf<TipoSessao?>(null) }
    var valorSessao by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    
    val campoShape = RoundedCornerShape(16.dp)
    val botaoShape = RoundedCornerShape(28.dp)
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))

    var showDeleteDialog by remember { mutableStateOf<Long?>(null) }

    val statusPagamentos by viewModel.statusPagamentos.collectAsState()

    // Debug: logar statusPagamentos
    LaunchedEffect(statusPagamentos) {
        Log.d("StatusPagamentos", statusPagamentos.toString())
    }
    
    // Carregar anotações reais ao abrir
    LaunchedEffect(patientId) {
        viewModel.carregarAnotacoes(patientId)
    }
    
    // Preencher campos ao carregar anotação selecionada
    LaunchedEffect(anotacaoSelecionada) {
        anotacaoSelecionada?.let { anotacao ->
            assuntos = anotacao.assuntos
            estadoEmocional = anotacao.estadoEmocional
            intervencoes = anotacao.intervencoes
            tarefas = anotacao.tarefas
            evolucao = anotacao.evolucao
            observacoes = anotacao.observacoes
        }
    }

    // Preencher valor ao selecionar tipo
    LaunchedEffect(tipoSelecionado) {
        tipoSelecionado?.let { valorSessao = it.valorPadrao.toString() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Anotações da Sessão") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Row(Modifier.padding(innerPadding)) {
            // Lista de sessões
            LazyColumn(
                modifier = Modifier.weight(1f).padding(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(anotacoes) { anotacao ->
                    val context = LocalContext.current
                    val tipoSessao = tiposSessao.find { it.id == anotacao.tipoSessaoId }
                    val statusPagamento = statusPagamentos[anotacao.id] // Acesso direto ao Map
                    
                    // Determinar cor e texto do status
                    val (statusCor, statusTexto) = when {
                        anotacao.assuntos.isNotBlank() && statusPagamento != null -> {
                            MaterialTheme.colorScheme.primary to "Registrada" // Verde para sessão registrada
                        }
                        anotacao.assuntos.isNotBlank() -> {
                            MaterialTheme.colorScheme.secondary to "Preenchida" // Azul para preenchida
                        }
                        else -> {
                            MaterialTheme.colorScheme.primary to "Pendente" // Laranja para pendente
                        }
                    }
                    
                    Card(
                        onClick = {
                            val intent = Intent(context, NovaSessaoActivity::class.java)
                            intent.putExtra("PATIENT_ID", patientId)
                            intent.putExtra("PATIENT_NAME", patientName)
                            intent.putExtra("ANOTACAO_ID", anotacao.id)
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .height(120.dp), // Altura fixa para ficar mais retangular
                        shape = campoShape,
                        colors = CardDefaults.cardColors(
                            containerColor = if (anotacao.assuntos.isNotBlank() && statusPagamento != null) {
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        )
                    ) {
                        Column(
                            Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            // Primeira linha: Número da sessão e status
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Sessão ${anotacao.numeroSessao}",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(Modifier.weight(1f))
                                
                                // Indicador verde de sessão registrada
                                if (anotacao.assuntos.isNotBlank() && statusPagamento != null) {
                                                                            Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .background(
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                                    RoundedCornerShape(16.dp)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                "Registrada",
                                                color = MaterialTheme.colorScheme.primary,
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                }
                            }
                            
                            Spacer(Modifier.height(8.dp))
                            
                            // Segunda linha: Tipo de sessão e valor
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                tipoSessao?.let {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            it.nome,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                
                                Spacer(Modifier.weight(1f))
                                
                                // Valor da sessão
                                if (tipoSessao != null) {
                                    Text(
                                        "R$ %.2f".format(tipoSessao.valorPadrao),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                            
                            Spacer(Modifier.height(8.dp))
                            
                            // Terceira linha: Data e botão de exclusão
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Data: ${dateFormatter.format(anotacao.dataHora)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Spacer(Modifier.weight(1f))
                                
                                // Botão de exclusão
                                IconButton(
                                    onClick = { showDeleteDialog = anotacao.id },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Excluir sessão",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                // Botão para nova sessão
                item {
                    val context = LocalContext.current
                    Card(
                        onClick = {
                            val intent = Intent(context, NovaSessaoActivity::class.java)
                            intent.putExtra("PATIENT_ID", patientId)
                            intent.putExtra("PATIENT_NAME", patientName)
                            intent.putExtra("ANOTACAO_ID", anotacaoSelecionada?.id)
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .height(120.dp), // Mesma altura dos outros cards
                        shape = campoShape,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(
                            Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Nova sessão",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Nova Sessão",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Sessão ${viewModel.getProximoNumeroSessao(patientId)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            // Formulário estruturado
            Column(Modifier.weight(2f).padding(8.dp).verticalScroll(rememberScrollState())) {
                sessaoSelecionada?.let { numeroSessao ->
                    Text("Sessão $numeroSessao", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    
                    Box {
                        OutlinedTextField(
                            value = tipoSelecionado?.nome ?: "",
                            onValueChange = {},
                            label = { Text("Tipo de sessão") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().clickable { expanded = true }
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            tiposSessao.forEach { tipo ->
                                DropdownMenuItem(
                                    text = { Text(tipo.nome) },
                                    onClick = {
                                        tipoSelecionado = tipo
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = valorSessao,
                        onValueChange = { valorSessao = it },
                        label = { Text("Valor da sessão") },
                        shape = campoShape,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = assuntos,
                        onValueChange = { assuntos = it },
                        label = { Text("Assuntos abordados") },
                        shape = campoShape,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = estadoEmocional,
                        onValueChange = { estadoEmocional = it },
                        label = { Text("Estado emocional") },
                        shape = campoShape,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = intervencoes,
                        onValueChange = { intervencoes = it },
                        label = { Text("Intervenções realizadas") },
                        shape = campoShape,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = tarefas,
                        onValueChange = { tarefas = it },
                        label = { Text("Tarefas propostas") },
                        shape = campoShape,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = evolucao,
                        onValueChange = { evolucao = it },
                        label = { Text("Evolução") },
                        shape = campoShape,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = observacoes,
                        onValueChange = { observacoes = it },
                        label = { Text("Observações gerais") },
                        shape = campoShape,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                    
                    Row {
                        Button(
                            onClick = {
                                if (anotacaoSelecionada != null) {
                                    viewModel.editarAnotacao(anotacaoSelecionada!!.copy(
                                        assuntos = assuntos,
                                        estadoEmocional = estadoEmocional,
                                        intervencoes = intervencoes,
                                        tarefas = tarefas,
                                        evolucao = evolucao,
                                        observacoes = observacoes
                                    ))
                                } else {
                                    viewModel.salvarAnotacao(
                                        patientId = patientId,
                                        numeroSessao = numeroSessao,
                                        assuntos = assuntos,
                                        estadoEmocional = estadoEmocional,
                                        intervencoes = intervencoes,
                                        tarefas = tarefas,
                                        evolucao = evolucao,
                                        observacoes = observacoes,
                                        tipoSessaoId = tipoSelecionado?.id,
                                        valorSessao = valorSessao.toDoubleOrNull()
                                    )
                                }
                                onSave()
                            },
                            shape = botaoShape,
                            modifier = Modifier.weight(1f)
                        ) { Text("Salvar") }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = { anotacaoSelecionada?.let { onExportPdf(it.id) } },
                            shape = botaoShape,
                            modifier = Modifier.weight(1f)
                        ) { Text("Exportar PDF") }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = { anotacaoSelecionada?.let { onAttach(it.id) } },
                            shape = botaoShape,
                            modifier = Modifier.weight(1f)
                        ) { Text("Anexar") }
                    }
                }
            }
        }
    }
    
    // Dialog de confirmação de exclusão
    showDeleteDialog?.let { anotacaoId ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Excluir Sessão") },
            text = { Text("Tem certeza que deseja excluir esta sessão? Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.excluirAnotacao(anotacaoId)
                        showDeleteDialog = null
                        // Recarregar anotações após exclusão
                        viewModel.carregarAnotacoes(patientId)
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
} 



