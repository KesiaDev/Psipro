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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.psipro.data.entities.AnamnesePreenchida
import com.example.psipro.data.entities.AnamneseModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AnamneseListScreen(
    anamneses: List<AnamnesePreenchida>,
    modelos: List<AnamneseModel>,
    onNovaAnamnese: () -> Unit,
    onEditarAnamnese: (AnamnesePreenchida) -> Unit,
    onExportarAnamnese: (AnamnesePreenchida) -> Unit,
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
                "Anamneses",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                color = bronze
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onNovaAnamnese) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Nova anamnese",
                    tint = bronze
                )
            }
        }
        // Botão Anamnese Dinâmica
        Button(
            onClick = { onNovaAnamnese() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = bronze)
        ) {
            Text("Anamnese Dinâmica", color = MaterialTheme.colorScheme.onPrimary)
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
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (anamneses.isEmpty()) {
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
                                    "Nenhuma anamnese encontrada",
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Crie sua primeira anamnese para começar",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = onSurface.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = onNovaAnamnese,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Nova Anamnese")
                                }
                            }
                        }
                    }
                } else {
                    items(anamneses) { anamnese ->
                        val modelo = modelos.find { it.id == anamnese.modeloId }
                        val dataFormatada = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR"))
                            .format(anamnese.data)
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Description,
                                        contentDescription = null,
                                        tint = bronze,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            modelo?.nome ?: "Modelo não encontrado",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = onSurface
                                        )
                                        Text(
                                            "Preenchida em $dataFormatada",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = onSurface.copy(alpha = 0.7f)
                                        )
                                        if (anamnese.versao > 1) {
                                            Text(
                                                "Versão ${anamnese.versao}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = bronze
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    OutlinedButton(
                                        onClick = { onEditarAnamnese(anamnese) },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Editar")
                                    }
                                    
                                    OutlinedButton(
                                        onClick = { onExportarAnamnese(anamnese) },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Exportar")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
} 