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
) {
    var preenchendoNova by remember { mutableStateOf(false) }
    var selecionandoModelo by remember { mutableStateOf(false) }
    var modeloSelecionado by remember { mutableStateOf<AnamneseModel?>(null) }

    if (preenchendoNova) {
        if (modeloSelecionado == null) {
            selecionandoModelo = true
        } else if (camposModelo.isNotEmpty()) {
            AnamneseFormScreen(
                campos = camposModelo,
                onSalvar = {
                    onSalvar(it)
                    preenchendoNova = false
                    modeloSelecionado = null
                }
            )
        } else {
            // Aguardando carregamento dos campos
            Column(Modifier.fillMaxSize().padding(24.dp)) {
                Text("Carregando campos do modelo...", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))
                CircularProgressIndicator()
            }
        }
    }

    if (selecionandoModelo) {
        Column(Modifier.fillMaxSize().padding(24.dp)) {
            Text("Selecione o modelo de anamnese", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
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
            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                selecionandoModelo = false
                preenchendoNova = false
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Cancelar")
            }
        }
    }

    if (!preenchendoNova && !selecionandoModelo) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Anamnese", style = MaterialTheme.typography.titleLarge)
                Button(onClick = { preenchendoNova = true }) {
                    Text("Nova Anamnese")
                }
            }
            Spacer(Modifier.height(16.dp))
            if (anamneses.isEmpty()) {
                Text("Nenhuma anamnese preenchida.", style = MaterialTheme.typography.bodyMedium)
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(anamneses) { anamnese ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            onClick = { /* Visualizar/editar anamnese */ }
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text("Data: ${anamnese.data}", style = MaterialTheme.typography.bodySmall)
                                Text("Versão: ${anamnese.versao}", style = MaterialTheme.typography.bodySmall)
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    TextButton(onClick = { onExportar(anamnese) }) {
                                        Text("Exportar PDF")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = onVoltar, modifier = Modifier.fillMaxWidth()) {
                Text("Voltar")
            }
        }
    }
} 