package com.example.psipro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.psipro.data.entities.AnamneseCampo
import com.example.psipro.data.entities.AnamneseModel

@Composable
fun AnamneseModelEditScreen(
    modelo: AnamneseModel?,
    camposIniciais: List<AnamneseCampo> = emptyList(),
    onSalvar: (AnamneseModel, List<AnamneseCampo>) -> Unit,
    onBack: () -> Unit
) {
    var nomeModelo by remember { mutableStateOf(TextFieldValue(modelo?.nome ?: "")) }
    var campos by remember { mutableStateOf(camposIniciais.toMutableList()) }
    var showCampoDialog by remember { mutableStateOf(false) }
    var campoEditando by remember { mutableStateOf<AnamneseCampo?>(null) }
    var erroNome by remember { mutableStateOf<String?>(null) }
    var erroCampos by remember { mutableStateOf<String?>(null) }

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
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = bronze)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                if (modelo == null) "Novo Modelo" else "Editar Modelo",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                color = bronze
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    if (nomeModelo.text.isBlank()) {
                        erroNome = "Informe o nome do modelo"
                        return@Button
                    }
                    if (campos.isEmpty()) {
                        erroCampos = "Adicione pelo menos um campo"
                        return@Button
                    }
                    erroNome = null
                    erroCampos = null
                    onSalvar(
                        modelo?.copy(nome = nomeModelo.text) ?: AnamneseModel(nome = nomeModelo.text),
                        campos
                    )
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Salvar")
            }
        }

        OutlinedTextField(
            value = nomeModelo,
            onValueChange = { nomeModelo = it },
            label = { Text("Nome do Modelo") },
            isError = erroNome != null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            singleLine = true
        )
        if (erroNome != null) {
            Text(
                erroNome!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 24.dp, top = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Campos do Modelo", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { campoEditando = null; showCampoDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar campo", tint = bronze)
            }
        }
        if (erroCampos != null) {
            Text(
                erroCampos!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 24.dp, top = 2.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(campos) { campo ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = surface)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(campo.label, style = MaterialTheme.typography.bodyLarge)
                            Text(campo.tipo, style = MaterialTheme.typography.bodySmall, color = onSurface.copy(alpha = 0.7f))
                            if (campo.obrigatorio) Text("Obrigatório", style = MaterialTheme.typography.bodySmall, color = bronze)
                            if (!campo.opcoes.isNullOrBlank()) Text("Opções: ${campo.opcoes}", style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { campoEditando = campo; showCampoDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar campo")
                        }
                        IconButton(onClick = { campos = campos.filter { it != campo }.toMutableList() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remover campo")
                        }
                    }
                }
            }
        }
    }

    if (showCampoDialog) {
        CampoDialog(
            campo = campoEditando,
            onSalvar = { novoCampo ->
                if (campoEditando == null) {
                    campos = (campos + novoCampo).toMutableList()
                } else {
                    campos = campos.map { if (it.id == campoEditando!!.id) novoCampo else it }.toMutableList()
                }
                showCampoDialog = false
            },
            onCancelar = { showCampoDialog = false },
            proximoId = (campos.maxOfOrNull { it.id } ?: 0) + 1
        )
    }
}

@Composable
fun CampoDialog(
    campo: AnamneseCampo?,
    onSalvar: (AnamneseCampo) -> Unit,
    onCancelar: () -> Unit,
    proximoId: Long
) {
    var label by remember { mutableStateOf(TextFieldValue(campo?.label ?: "")) }
    var tipo by remember { mutableStateOf(campo?.tipo ?: "TEXTO_CURTO") }
    var obrigatorio by remember { mutableStateOf(campo?.obrigatorio ?: false) }
    var opcoes by remember { mutableStateOf(TextFieldValue(campo?.opcoes ?: "")) }
    var erro by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(if (campo == null) "Novo Campo" else "Editar Campo") },
        text = {
            Column {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label do campo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                DropdownMenuTipoCampo(tipo = tipo, onTipoSelecionado = { tipo = it })
                Spacer(modifier = Modifier.height(8.dp))
                if (tipo == "SELECAO_UNICA" || tipo == "MULTIPLA_ESCOLHA") {
                    OutlinedTextField(
                        value = opcoes,
                        onValueChange = { opcoes = it },
                        label = { Text("Opções (separadas por vírgula)") },
                        singleLine = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = obrigatorio, onCheckedChange = { obrigatorio = it })
                    Text("Obrigatório")
                }
                if (erro != null) {
                    Text(erro!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (label.text.isBlank()) {
                    erro = "Informe o label do campo"
                    return@Button
                }
                if ((tipo == "SELECAO_UNICA" || tipo == "MULTIPLA_ESCOLHA") && opcoes.text.isBlank()) {
                    erro = "Informe as opções"
                    return@Button
                }
                erro = null
                onSalvar(
                    campo?.copy(
                        label = label.text,
                        tipo = tipo,
                        obrigatorio = obrigatorio,
                        opcoes = if (tipo == "SELECAO_UNICA" || tipo == "MULTIPLA_ESCOLHA") opcoes.text else null
                    ) ?: AnamneseCampo(
                        id = proximoId,
                        modeloId = 0L, // será preenchido ao salvar modelo
                        tipo = tipo,
                        label = label.text,
                        opcoes = if (tipo == "SELECAO_UNICA" || tipo == "MULTIPLA_ESCOLHA") opcoes.text else null,
                        obrigatorio = obrigatorio
                    )
                )
            }) {
                Text("Salvar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onCancelar) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun DropdownMenuTipoCampo(tipo: String, onTipoSelecionado: (String) -> Unit) {
    val tipos = listOf(
        "TEXTO_CURTO" to "Texto Curto",
        "TEXTO_LONGO" to "Texto Longo",
        "DATA" to "Data",
        "SELECAO_UNICA" to "Seleção Única",
        "MULTIPLA_ESCOLHA" to "Múltipla Escolha",
        "TITULO" to "Título"
    )
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(tipos.find { it.first == tipo }?.second ?: "Tipo")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            tipos.forEach { (value, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onTipoSelecionado(value)
                        expanded = false
                    }
                )
            }
        }
    }
} 