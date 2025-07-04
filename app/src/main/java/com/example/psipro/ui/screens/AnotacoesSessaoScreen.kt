package com.example.psipro.ui.screens

import com.example.psipro.data.entities.TipoSessao
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.psipro.ui.viewmodels.AnotacaoSessaoViewModel
import com.example.psipro.ui.viewmodels.TipoSessaoViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import android.content.Context
import android.content.Intent
import androidx.compose.ui.platform.LocalContext

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
            LazyColumn(Modifier.weight(1f).padding(8.dp)) {
                items(anotacoes) { anotacao ->
                    val tipoSessao = tiposSessao.find { it.id == anotacao.tipoSessaoId }
                    Card(
                        onClick = {
                            sessaoSelecionada = anotacao.numeroSessao
                            viewModel.carregarAnotacao(patientId, anotacao.numeroSessao)
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = campoShape
                    ) {
                        Column(Modifier.padding(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Sessão ${anotacao.numeroSessao}", fontWeight = FontWeight.Bold)
                                tipoSessao?.let {
                                    Spacer(Modifier.width(8.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            it.nome,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
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
                            Text("Data: ${dateFormatter.format(anotacao.dataHora)}")
                            Text("Status: ${if (anotacao.assuntos.isNotBlank()) "Preenchida" else "Vazia"}")
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
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = campoShape
                    ) {
                        Column(Modifier.padding(8.dp)) {
                            Text("+ Nova Sessão", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("Sessão ${viewModel.getProximoNumeroSessao(patientId)}")
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
} 