package com.example.psipro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.psipro.data.entities.AnamneseModel

@Composable
fun AnamneseModelSelectionScreen(
    modelos: List<AnamneseModel>,
    onModeloSelecionado: (AnamneseModel) -> Unit,
    onCriarModelo: () -> Unit,
    onEditarModelo: (AnamneseModel) -> Unit,
    onRemoverModelo: (AnamneseModel) -> Unit,
    onBack: () -> Unit,
    isLoading: Boolean = false
) {
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
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Voltar",
                    tint = bronze
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Selecionar Modelo",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                color = bronze
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onCriarModelo) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Criar novo modelo",
                    tint = bronze
                )
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    Text(
                        "Escolha um modelo de anamnese para começar:",
                        style = MaterialTheme.typography.bodyLarge,
                        color = onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }

                if (modelos.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = onSurface.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Nenhum modelo disponível",
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Crie um novo modelo para começar",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = onSurface.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = onCriarModelo,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Criar Modelo")
                                }
                            }
                        }
                    }
                } else {
                    items(modelos) { modelo ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = surface),
                            onClick = { onModeloSelecionado(modelo) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = null,
                                    tint = bronze,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        modelo.nome,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = onSurface
                                    )
                                    if (modelo.isDefault) {
                                        Text(
                                            "Modelo padrão",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = bronze
                                        )
                                    }
                                }
                                IconButton(onClick = { onEditarModelo(modelo) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar modelo")
                                }
                                IconButton(onClick = { onRemoverModelo(modelo) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remover modelo")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
} 