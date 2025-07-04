package com.example.psipro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.psipro.data.entities.TipoSessao
import com.example.psipro.ui.viewmodels.AnotacaoSessaoViewModel
import com.example.psipro.ui.viewmodels.TipoSessaoViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.focus.onFocusChanged

@Composable
fun NovaSessaoScreen(
    patientId: Long,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    viewModel: AnotacaoSessaoViewModel = hiltViewModel(),
    tipoSessaoViewModel: TipoSessaoViewModel = hiltViewModel()
) {
    val tiposSessao by tipoSessaoViewModel.tiposSessao.collectAsState()
    var tipoSelecionado by remember { mutableStateOf<TipoSessao?>(null) }
    var valorSessao by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var assuntos by remember { mutableStateOf("") }
    var estadoEmocional by remember { mutableStateOf("") }
    var intervencoes by remember { mutableStateOf("") }
    var tarefas by remember { mutableStateOf("") }
    var evolucao by remember { mutableStateOf("") }
    var observacoes by remember { mutableStateOf("") }
    var tipoBusca by remember { mutableStateOf("") }
    val tiposFiltrados = if (tipoBusca.isBlank()) tiposSessao else tiposSessao.filter { it.nome.contains(tipoBusca, ignoreCase = true) }
    val campoShape = RoundedCornerShape(16.dp)
    val botaoShape = RoundedCornerShape(28.dp)

    // Sugestões automáticas
    val ultimosAssuntos = viewModel.anotacoes.value.map { it.assuntos }.filter { it.isNotBlank() }.distinct().takeLast(5).reversed()
    val ultimasTarefas = viewModel.anotacoes.value.map { it.tarefas }.filter { it.isNotBlank() }.distinct().takeLast(5).reversed()
    var showSugestoesAssuntos by remember { mutableStateOf(false) }
    var showSugestoesTarefas by remember { mutableStateOf(false) }
    
    // Tipos mais usados pelo psicólogo
    val tiposMaisUsados = viewModel.anotacoes.value
        .mapNotNull { it.tipoSessaoId }
        .groupBy { it }
        .mapValues { it.value.size }
        .entries
        .sortedByDescending { it.value }
        .take(3)
        .mapNotNull { entry -> tiposSessao.find { it.id == entry.key } }

    LaunchedEffect(tipoSelecionado) {
        tipoSelecionado?.let { valorSessao = it.valorPadrao.toString() }
    }

    Box(Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Nova Sessão", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        val ult = viewModel.anotacoes.value.lastOrNull()
                        if (ult != null) {
                            assuntos = ult.assuntos
                            estadoEmocional = ult.estadoEmocional
                            intervencoes = ult.intervencoes
                            tarefas = ult.tarefas
                            evolucao = ult.evolucao
                            observacoes = ult.observacoes
                            tipoSelecionado = tiposSessao.find { it.id == ult.tipoSessaoId }
                            valorSessao = ult.tipoSessaoId?.let { tiposSessao.find { t -> t.id == it }?.valorPadrao?.toString() } ?: ""
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.Default.Description, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Duplicar última sessão")
                }
                Spacer(Modifier.height(8.dp))
                Box {
                    OutlinedTextField(
                        value = tipoBusca,
                        onValueChange = {
                            tipoBusca = it
                            expanded = true
                        },
                        label = { Text("Tipo de sessão") },
                        leadingIcon = { Icon(Icons.Default.Flag, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().clickable { expanded = true },
                        readOnly = false,
                        singleLine = true
                    )
                    DropdownMenu(
                        expanded = expanded && tiposFiltrados.isNotEmpty(),
                        onDismissRequest = { expanded = false }
                    ) {
                        tiposFiltrados.forEach { tipo ->
                            DropdownMenuItem(
                                text = { Text(tipo.nome) },
                                onClick = {
                                    tipoSelecionado = tipo
                                    tipoBusca = tipo.nome
                                    valorSessao = tipo.valorPadrao.toString()
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
                    leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null) },
                    shape = campoShape,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = assuntos,
                    onValueChange = { assuntos = it },
                    label = { Text("Assuntos abordados") },
                    leadingIcon = { Icon(Icons.Default.ChatBubbleOutline, contentDescription = null) },
                    shape = campoShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { showSugestoesAssuntos = it.isFocused },
                    maxLines = 8,
                    minLines = 2
                )
                if (showSugestoesAssuntos && ultimosAssuntos.isNotEmpty()) {
                    LazyRow(Modifier.padding(vertical = 4.dp)) {
                        items(ultimosAssuntos) { sugestao ->
                            AssistChip(
                                onClick = { assuntos = sugestao },
                                label = { Text(sugestao, maxLines = 1) },
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = estadoEmocional,
                    onValueChange = { estadoEmocional = it },
                    label = { Text("Estado emocional") },
                    leadingIcon = { Icon(Icons.Default.SentimentSatisfied, contentDescription = null) },
                    shape = campoShape,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4,
                    minLines = 1
                )
                // Chips de estados emocionais comuns
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val estadosComuns = listOf("Calmo", "Ansioso", "Triste", "Irritado", "Feliz", "Confuso", "Estressado", "Motivado")
                    items(estadosComuns) { estado ->
                        AssistChip(
                            onClick = { 
                                if (estadoEmocional.isBlank()) {
                                    estadoEmocional = estado
                                } else {
                                    estadoEmocional = if (estadoEmocional.contains(estado)) {
                                        estadoEmocional.replace(estado, "").trim().replace(",,", ",").trim(',')
                                    } else {
                                        "$estadoEmocional, $estado"
                                    }
                                }
                            },
                            label = { Text(estado) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (estadoEmocional.contains(estado)) 
                                    MaterialTheme.colorScheme.primaryContainer 
                                else 
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = intervencoes,
                    onValueChange = { intervencoes = it },
                    label = { Text("Intervenções realizadas") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                    shape = campoShape,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 6,
                    minLines = 1
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = tarefas,
                    onValueChange = { tarefas = it },
                    label = { Text("Tarefas propostas") },
                    leadingIcon = { Icon(Icons.Default.Checklist, contentDescription = null) },
                    shape = campoShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { showSugestoesTarefas = it.isFocused },
                    maxLines = 6,
                    minLines = 1
                )
                if (showSugestoesTarefas && ultimasTarefas.isNotEmpty()) {
                    LazyRow(Modifier.padding(vertical = 4.dp)) {
                        items(ultimasTarefas) { sugestao ->
                            AssistChip(
                                onClick = { tarefas = sugestao },
                                label = { Text(sugestao, maxLines = 1) },
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = evolucao,
                    onValueChange = { evolucao = it },
                    label = { Text("Evolução") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                    shape = campoShape,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 6,
                    minLines = 1
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = observacoes,
                    onValueChange = { observacoes = it },
                    label = { Text("Observações gerais") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                    shape = campoShape,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 6,
                    minLines = 1
                )
                Spacer(Modifier.height(8.dp))
                // Chips de tipos de sessão mais comuns
                Text("Tipos mais usados:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val tiposComuns = listOf("Terapia Individual", "Avaliação", "Retorno", "Emergência", "Primeira Consulta")
                    items(tiposComuns) { tipoNome ->
                        val tipo = tiposSessao.find { it.nome.equals(tipoNome, ignoreCase = true) }
                        AssistChip(
                            onClick = {
                                tipo?.let {
                                    tipoSelecionado = it
                                    tipoBusca = it.nome
                                    valorSessao = it.valorPadrao.toString()
                                }
                            },
                            label = { Text(tipoNome) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (tipoSelecionado?.nome == tipoNome) 
                                    MaterialTheme.colorScheme.primaryContainer 
                                else 
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
                
                // Chips dos tipos mais usados pelo psicólogo
                if (tiposMaisUsados.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text("Seus tipos mais usados:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(tiposMaisUsados) { tipo ->
                            AssistChip(
                                onClick = {
                                    tipoSelecionado = tipo
                                    tipoBusca = tipo.nome
                                    valorSessao = tipo.valorPadrao.toString()
                                },
                                label = { Text(tipo.nome) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (tipoSelecionado?.id == tipo.id) 
                                        MaterialTheme.colorScheme.secondaryContainer 
                                    else 
                                        MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = {
                            viewModel.salvarAnotacao(
                                patientId = patientId,
                                numeroSessao = viewModel.getProximoNumeroSessao(patientId),
                                assuntos = assuntos,
                                estadoEmocional = estadoEmocional,
                                intervencoes = intervencoes,
                                tarefas = tarefas,
                                evolucao = evolucao,
                                observacoes = observacoes,
                                tipoSessaoId = tipoSelecionado?.id,
                                valorSessao = valorSessao.toDoubleOrNull()
                            )
                            onSave()
                        },
                        shape = botaoShape,
                        modifier = Modifier.weight(1f)
                    ) { Text("Salvar") }
                    OutlinedButton(
                        onClick = onCancel,
                        shape = botaoShape,
                        modifier = Modifier.weight(1f)
                    ) { Text("Cancelar") }
                }
            }
        }
        FloatingActionButton(
            onClick = {
                viewModel.salvarAnotacao(
                    patientId = patientId,
                    numeroSessao = viewModel.getProximoNumeroSessao(patientId),
                    assuntos = assuntos,
                    estadoEmocional = estadoEmocional,
                    intervencoes = intervencoes,
                    tarefas = tarefas,
                    evolucao = evolucao,
                    observacoes = observacoes,
                    tipoSessaoId = tipoSelecionado?.id,
                    valorSessao = valorSessao.toDoubleOrNull()
                )
                onSave()
            },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp)
        ) {
            Icon(Icons.Default.Save, contentDescription = "Salvar")
        }
    }
} 