package com.example.psipro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.psipro.data.entities.AnamneseCampo
import com.example.psipro.data.entities.AnamnesePreenchida
import com.example.psipro.data.entities.AnamneseModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign

@Composable
fun AnamneseSection(
    pacienteId: Long,
    camposModelo: List<AnamneseCampo>,
    anamneses: List<AnamnesePreenchida>,
    onSalvar: (Map<Long, String>) -> Unit,
    onExportar: (AnamnesePreenchida) -> Unit,
    onVoltar: () -> Unit,
    modelos: List<AnamneseModel> = emptyList(),
    onSelecionarModelo: (AnamneseModel) -> Unit = {},
    isLoading: Boolean = false,
    error: String? = null,
    onRetry: () -> Unit = {}
) {
    var preenchendoNova by remember { mutableStateOf(false) }
    var selecionandoModelo by remember { mutableStateOf(false) }
    var modeloSelecionado by remember { mutableStateOf<AnamneseModel?>(null) }

    // Mostrar loading global
    if (isLoading) {
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
                    text = "Carregando...",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        return
    }

    // Mostrar erro se houver
    error?.let { errorMessage ->
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Erro",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry
            ) {
                Text("Tentar Novamente")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onVoltar
            ) {
                Text("Voltar")
            }
        }
        return
    }

    if (preenchendoNova) {
        if (modeloSelecionado == null) {
            selecionandoModelo = true
        } else if (camposModelo.isNotEmpty()) {
            AnamneseFormScreen(
                modelo = modeloSelecionado!!,
                campos = camposModelo,
                onSalvar = {
                    onSalvar(it)
                    preenchendoNova = false
                    modeloSelecionado = null
                },
                onCancel = {
                    preenchendoNova = false
                    modeloSelecionado = null
                }
            )
        } else {
            // Aguardando carregamento dos campos
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Carregando campos do modelo...", 
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }

    if (selecionandoModelo) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp)
        ) {
            Text(
                "Selecione o modelo de anamnese", 
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (modelos.isEmpty()) {
                Text(
                    "Nenhum modelo disponível",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                modelos.forEach { modelo ->
                    Button(
                        onClick = {
                            modeloSelecionado = modelo
                            selecionandoModelo = false
                            onSelecionarModelo(modelo)
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text(modelo.nome)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = {
                    selecionandoModelo = false
                    preenchendoNova = false
                }, 
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }
        }
    }

    if (!preenchendoNova && !selecionandoModelo) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Anamnese", 
                    style = MaterialTheme.typography.titleLarge
                )
                Button(
                    onClick = { preenchendoNova = true }
                ) {
                    Text("Nova Anamnese")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            if (anamneses.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Nenhuma anamnese preenchida.", 
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Clique em 'Nova Anamnese' para começar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(anamneses) { anamnese ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    "Data: ${anamnese.data}", 
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    "Versão: ${anamnese.versao}", 
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(), 
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = { onExportar(anamnese) }
                                    ) {
                                        Text("Exportar PDF")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onVoltar, 
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Voltar")
            }
        }
    }
} 