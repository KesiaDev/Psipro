package com.example.psipro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.psipro.data.entities.AnamneseCampo

@Composable
fun AnamneseFormScreen(
    campos: List<AnamneseCampo>,
    onSalvar: (Map<Long, String>) -> Unit
) {
    val respostas = remember { mutableStateMapOf<Long, String>() }
    var erro by remember { mutableStateOf<String?>(null) }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(campos) { campo ->
            val obrigatorio = campo.obrigatorio
            val labelText = if (obrigatorio) "${campo.label} *" else campo.label
            when (campo.tipo) {
                "TEXTO_CURTO" -> {
                    OutlinedTextField(
                        value = respostas[campo.id] ?: "",
                        onValueChange = { respostas[campo.id] = it },
                        label = { Text(labelText) },
                        isError = obrigatorio && (respostas[campo.id]?.isBlank() != false),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                }
                "TEXTO_LONGO" -> {
                    OutlinedTextField(
                        value = respostas[campo.id] ?: "",
                        onValueChange = { respostas[campo.id] = it },
                        label = { Text(labelText) },
                        isError = obrigatorio && (respostas[campo.id]?.isBlank() != false),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .padding(vertical = 4.dp),
                        maxLines = 5
                    )
                }
                "DATA" -> {
                    OutlinedTextField(
                        value = respostas[campo.id] ?: "",
                        onValueChange = { respostas[campo.id] = it },
                        label = { Text(labelText) },
                        isError = obrigatorio && (respostas[campo.id]?.isBlank() != false),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                }
                "SELECAO_UNICA" -> {
                    val opcoes = campo.opcoes?.split(",") ?: emptyList()
                    var selecionado by remember { mutableStateOf(respostas[campo.id] ?: "") }
                    Column(Modifier.padding(vertical = 4.dp)) {
                        Text(labelText)
                        opcoes.forEach { opcao ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = selecionado == opcao,
                                        onClick = {
                                            selecionado = opcao
                                            respostas[campo.id] = opcao
                                        }
                                    )
                                    .padding(4.dp)
                            ) {
                                RadioButton(
                                    selected = selecionado == opcao,
                                    onClick = {
                                        selecionado = opcao
                                        respostas[campo.id] = opcao
                                    }
                                )
                                Text(opcao, Modifier.padding(start = 8.dp))
                            }
                        }
                        if (obrigatorio && (respostas[campo.id]?.isBlank() != false)) {
                            Text("Campo obrigatório", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                "MULTIPLA_ESCOLHA" -> {
                    val opcoes = campo.opcoes?.split(",") ?: emptyList()
                    val selecionados = remember { mutableStateListOf<String>() }
                    Column(Modifier.padding(vertical = 4.dp)) {
                        Text(labelText)
                        opcoes.forEach { opcao ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp)
                            ) {
                                Checkbox(
                                    checked = selecionados.contains(opcao),
                                    onCheckedChange = { checked ->
                                        if (checked) selecionados.add(opcao) else selecionados.remove(opcao)
                                        respostas[campo.id] = selecionados.joinToString(",")
                                    }
                                )
                                Text(opcao, Modifier.padding(start = 8.dp))
                            }
                        }
                        if (obrigatorio && (respostas[campo.id]?.isBlank() != false)) {
                            Text("Selecione pelo menos uma opção", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                "TITULO" -> {
                    Text(
                        text = campo.label,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
        item {
            erro?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 8.dp))
            }
            Button(
                onClick = {
                    val obrigatoriosNaoPreenchidos = campos.filter { it.obrigatorio && (respostas[it.id]?.isBlank() != false) }
                    if (obrigatoriosNaoPreenchidos.isNotEmpty()) {
                        erro = "Preencha todos os campos obrigatórios marcados com *"
                    } else {
                        erro = null
                        onSalvar(respostas)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text("Salvar Anamnese")
            }
        }
    }
} 