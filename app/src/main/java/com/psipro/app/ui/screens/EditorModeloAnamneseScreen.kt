package com.psipro.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.psipro.app.data.entities.AnamneseCampo
import androidx.compose.ui.graphics.Color

enum class TipoCampoAnamnese(val label: String) {
    TEXTO_CURTO("Texto Curto"),
    TEXTO_LONGO("Texto Longo"),
    DATA("Data"),
    SELECAO_UNICA("Seleção Única"),
    MULTIPLA_ESCOLHA("Múltipla Escolha"),
    ESCALA("Escala de Humor"),
    TITULO("Título")
}

@Composable
fun EditorModeloAnamneseScreen(
    nomeModelo: String,
    campos: List<AnamneseCampo>,
    onSalvar: (String, List<AnamneseCampo>) -> Unit,
    onCancelar: () -> Unit
) {
    var nome by remember { mutableStateOf(nomeModelo) }
    var listaCampos by remember { mutableStateOf(campos.toMutableList()) }
    var campoArrastando by remember { mutableStateOf<Int?>(null) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Editar Modelo de Anamnese", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Nome do Modelo") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Text("Campos do Modelo", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            itemsIndexed(listaCampos, key = { _, campo -> campo.id }) { index, campo ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { campoArrastando = index },
                                onDragEnd = { campoArrastando = null },
                                onDragCancel = { campoArrastando = null },
                                onDrag = { change, dragAmount ->
                                    // Drag-and-drop simples: mover para cima/baixo
                                    val newIndex = (index + if (dragAmount.y > 0) 1 else -1).coerceIn(0, listaCampos.lastIndex)
                                    if (newIndex != index) {
                                        val mutable = listaCampos.toMutableList()
                                        val item = mutable.removeAt(index)
                                        mutable.add(newIndex, item)
                                        listaCampos = mutable
                                    }
                                }
                            )
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (campoArrastando == index) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(Icons.Default.DragHandle, contentDescription = "Arrastar", tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text(campo.label, style = MaterialTheme.typography.bodyLarge)
                            Text(TipoCampoAnamnese.valueOf(campo.tipo).label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        IconButton(onClick = {
                            listaCampos = listaCampos.toMutableList().also { it.removeAt(index) }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remover campo", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = {
                // Adicionar novo campo (exemplo: texto curto)
                val novoCampo = AnamneseCampo(
                    id = System.currentTimeMillis(),
                    modeloId = 0L,
                    tipo = TipoCampoAnamnese.TEXTO_CURTO.name,
                    label = "Novo Campo"
                )
                listaCampos = (listaCampos + novoCampo).toMutableList()
            }) {
                Text("Adicionar Campo")
            }
            Button(onClick = { onSalvar(nome, listaCampos) }) {
                Text("Salvar Modelo")
            }
            OutlinedButton(onClick = onCancelar) {
                Text("Cancelar")
            }
        }
    }
} 



