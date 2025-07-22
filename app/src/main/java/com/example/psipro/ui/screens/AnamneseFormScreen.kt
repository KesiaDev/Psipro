package com.example.psipro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.psipro.data.entities.AnamneseCampo
import com.example.psipro.data.entities.AnamneseModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AnamneseFormScreen(
    modelo: AnamneseModel,
    campos: List<AnamneseCampo>,
    onSalvar: (Map<Long, String>) -> Unit,
    onCancel: () -> Unit = {},
    isLoading: Boolean = false
) {
    val respostas = remember { mutableStateMapOf<Long, String>() }
    var erro by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var camposComErro by remember { mutableStateOf<Set<Long>>(emptySet()) }

    val bronze = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // TopBar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCancel) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Voltar",
                    tint = bronze
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    modelo.nome,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                    color = bronze
                )
                Text(
                    "Preencha os campos abaixo",
                    style = MaterialTheme.typography.bodySmall,
                    color = onSurface.copy(alpha = 0.7f)
                )
            }
            IconButton(
                onClick = {
                    if (validarCampos(campos, respostas)) {
                        isSaving = true
                        onSalvar(respostas.toMap())
                    }
                },
                enabled = !isLoading && !isSaving
            ) {
                Icon(
                    Icons.Default.Save,
                    contentDescription = "Salvar",
                    tint = if (isLoading || isSaving) onSurface.copy(alpha = 0.5f) else bronze
                )
            }
        }

        if (isLoading || isSaving) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        if (isSaving) "Salvando..." else "Carregando...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Mostrar erro se houver
                erro?.let { errorMessage ->
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                // Campos do formulário
                items(campos) { campo ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            when (campo.tipo) {
                                "TEXTO_CURTO" -> {
                                    val obrigatorio = campo.obrigatorio
                                    val labelText = if (obrigatorio) "${campo.label} *" else campo.label
                                    val temErro = camposComErro.contains(campo.id) && (respostas[campo.id]?.isBlank() != false)
                                    
                                    OutlinedTextField(
                                        value = respostas[campo.id] ?: "",
                                        onValueChange = { 
                                            respostas[campo.id] = it
                                            if (temErro && it.isNotBlank()) {
                                                camposComErro = camposComErro - campo.id
                                            }
                                        },
                                        label = { Text(labelText) },
                                        isError = temErro,
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                }
                                
                                "TEXTO_LONGO" -> {
                                    val obrigatorio = campo.obrigatorio
                                    val labelText = if (obrigatorio) "${campo.label} *" else campo.label
                                    val temErro = camposComErro.contains(campo.id) && (respostas[campo.id]?.isBlank() != false)
                                    
                                    OutlinedTextField(
                                        value = respostas[campo.id] ?: "",
                                        onValueChange = { 
                                            respostas[campo.id] = it
                                            if (temErro && it.isNotBlank()) {
                                                camposComErro = camposComErro - campo.id
                                            }
                                        },
                                        label = { Text(labelText) },
                                        isError = temErro,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(120.dp),
                                        maxLines = 5
                                    )
                                }
                                
                                "DATA" -> {
                                    val obrigatorio = campo.obrigatorio
                                    val labelText = if (obrigatorio) "${campo.label} *" else campo.label
                                    val temErro = camposComErro.contains(campo.id) && (respostas[campo.id]?.isBlank() != false)
                                    
                                    OutlinedTextField(
                                        value = respostas[campo.id] ?: "",
                                        onValueChange = { 
                                            respostas[campo.id] = it
                                            if (temErro && it.isNotBlank()) {
                                                camposComErro = camposComErro - campo.id
                                            }
                                        },
                                        label = { Text(labelText) },
                                        isError = temErro,
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        placeholder = { Text("DD/MM/AAAA") }
                                    )
                                }
                                
                                "SELECAO_UNICA" -> {
                                    val opcoes = campo.opcoes?.split(",") ?: emptyList()
                                    val selecionado = respostas[campo.id] ?: ""
                                    val obrigatorio = campo.obrigatorio
                                    val labelText = if (obrigatorio) "${campo.label} *" else campo.label
                                    val temErro = camposComErro.contains(campo.id) && selecionado.isBlank()
                                    
                                    Column {
                                        Text(
                                            labelText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        opcoes.forEach { opcao ->
                                            Row(
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                RadioButton(
                                                    selected = selecionado == opcao,
                                                    onClick = {
                                                        respostas[campo.id] = opcao
                                                        if (temErro) {
                                                            camposComErro = camposComErro - campo.id
                                                        }
                                                    }
                                                )
                                                Text(
                                                    opcao, 
                                                    Modifier.padding(start = 8.dp),
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                        }
                                        if (temErro) {
                                            Text(
                                                "Selecione uma opção",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }
                                }
                                
                                "MULTIPLA_ESCOLHA" -> {
                                    val opcoes = campo.opcoes?.split(",") ?: emptyList()
                                    val selecionados = remember { mutableStateListOf<String>() }
                                    val obrigatorio = campo.obrigatorio
                                    val labelText = if (obrigatorio) "${campo.label} *" else campo.label
                                    val temErro = camposComErro.contains(campo.id) && selecionados.isEmpty()
                                    
                                    Column {
                                        Text(
                                            labelText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        opcoes.forEach { opcao ->
                                            Row(
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(
                                                    checked = selecionados.contains(opcao),
                                                    onCheckedChange = { checked ->
                                                        if (checked) {
                                                            selecionados.add(opcao)
                                                        } else {
                                                            selecionados.remove(opcao)
                                                        }
                                                        respostas[campo.id] = selecionados.joinToString(",")
                                                        if (temErro && selecionados.isNotEmpty()) {
                                                            camposComErro = camposComErro - campo.id
                                                        }
                                                    }
                                                )
                                                Text(
                                                    opcao, 
                                                    Modifier.padding(start = 8.dp),
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                        }
                                        if (temErro) {
                                            Text(
                                                "Selecione pelo menos uma opção",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }
                                }
                                
                                "TITULO" -> {
                                    Text(
                                        campo.label,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = bronze,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                                
                                else -> {
                                    Text(
                                        "Tipo de campo não suportado: ${campo.tipo}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }

                // Botão salvar
                item {
                    Button(
                        onClick = {
                            if (validarCampos(campos, respostas)) {
                                isSaving = true
                                onSalvar(respostas.toMap())
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Salvar Anamnese", fontSize = 16.sp)
                        }
                    }
                }

                // Espaço extra no final
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

private fun validarCampos(campos: List<AnamneseCampo>, respostas: Map<Long, String>): Boolean {
    val camposObrigatorios = campos.filter { it.obrigatorio }
    val camposVazios = camposObrigatorios.filter { campo ->
        val resposta = respostas[campo.id] ?: ""
        resposta.isBlank()
    }
    
    return camposVazios.isEmpty()
} 