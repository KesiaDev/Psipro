package com.example.psipro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.psipro.data.entities.AnamneseModel

@Composable
fun ModelosAnamneseScreen(
    modelos: List<AnamneseModel>,
    onNovoModelo: () -> Unit,
    onClonar: (AnamneseModel) -> Unit,
    onEditar: (AnamneseModel) -> Unit,
    onExcluir: (AnamneseModel) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text("Modelos de Anamnese", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onNovoModelo, modifier = Modifier.fillMaxWidth()) {
            Text("Novo Modelo")
        }
        Spacer(Modifier.height(16.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(modelos) { modelo ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(modelo.nome, style = MaterialTheme.typography.titleMedium)
                            if (modelo.isDefault) {
                                Text("Modelo pronto", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            } else {
                                Text("Meu modelo", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        if (modelo.isDefault) {
                            OutlinedButton(onClick = { onClonar(modelo) }) {
                                Text("Clonar")
                            }
                        } else {
                            OutlinedButton(onClick = { onEditar(modelo) }) {
                                Text("Editar")
                            }
                            Spacer(Modifier.width(8.dp))
                            OutlinedButton(onClick = { onExcluir(modelo) }) {
                                Text("Excluir")
                            }
                        }
                    }
                }
            }
        }
    }
} 