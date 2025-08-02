package com.psipro.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import com.psipro.app.ui.viewmodels.HistoricoMedicoViewModel

// Data class auxiliar para manter o histórico salvo na tela
data class HistoricoMedicoData(
    val condicoes: String,
    val medicamentos: String,
    val internacoes: String,
    val queixasFisicas: String,
    val motivoTerapia: String,
    val tempoSintomas: String,
    val jaFezTerapia: String,
    val sono: String,
    val alimentacao: String,
    val substancias: String,
    val vidaSocial: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoricoMedicoScreen(
    patientId: Long,
    onSave: () -> Unit = {},
    onBack: () -> Unit,
    viewModel: HistoricoMedicoViewModel
) {
    val historico by viewModel.historico.collectAsState()
    var condicoes by remember { mutableStateOf("") }
    var medicamentos by remember { mutableStateOf("") }
    var internacoes by remember { mutableStateOf("") }
    var queixasFisicas by remember { mutableStateOf("") }
    var motivoTerapia by remember { mutableStateOf("") }
    var tempoSintomas by remember { mutableStateOf("") }
    var jaFezTerapia by remember { mutableStateOf("") }
    var sono by remember { mutableStateOf("") }
    var alimentacao by remember { mutableStateOf("") }
    var substancias by remember { mutableStateOf("") }
    var vidaSocial by remember { mutableStateOf("") }
    
    // Carregar dados reais ao abrir
    LaunchedEffect(patientId) {
        viewModel.carregar(patientId)
    }
    // Preencher campos ao carregar do banco
    LaunchedEffect(historico) {
        historico?.let {
            condicoes = it.condicoes
            medicamentos = it.medicamentos
            internacoes = it.internacoes
            queixasFisicas = it.queixasFisicas
            motivoTerapia = it.motivoTerapia
            tempoSintomas = it.tempoSintomas
            jaFezTerapia = it.jaFezTerapia
            sono = it.sono
            alimentacao = it.alimentacao
            substancias = it.substancias
            vidaSocial = it.vidaSocial
        }
    }

    val scrollState = rememberScrollState()
    val campoShape = RoundedCornerShape(16.dp)
    val botaoShape = RoundedCornerShape(28.dp)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Histórico Médico") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            Text("Condições médicas atuais", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = condicoes,
                onValueChange = { condicoes = it },
                modifier = Modifier.fillMaxWidth(),
                shape = campoShape
            )
            Text("Uso de medicamentos", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = medicamentos,
                onValueChange = { medicamentos = it },
                modifier = Modifier.fillMaxWidth(),
                shape = campoShape
            )
            Text("Histórico de internações", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = internacoes,
                onValueChange = { internacoes = it },
                modifier = Modifier.fillMaxWidth(),
                shape = campoShape
            )
            Text("Queixas físicas", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = queixasFisicas,
                onValueChange = { queixasFisicas = it },
                modifier = Modifier.fillMaxWidth(),
                shape = campoShape
            )

            Spacer(Modifier.height(16.dp))
            Text("😔 Queixas e Motivos da Consulta", fontWeight = FontWeight.Bold)
            Text("O que motivou a busca por terapia?")
            OutlinedTextField(
                value = motivoTerapia,
                onValueChange = { motivoTerapia = it },
                modifier = Modifier.fillMaxWidth(),
                shape = campoShape
            )
            Text("Há quanto tempo os sintomas começaram?")
            OutlinedTextField(
                value = tempoSintomas,
                onValueChange = { tempoSintomas = it },
                modifier = Modifier.fillMaxWidth(),
                shape = campoShape
            )
            Text("Já fez terapia antes?")
            OutlinedTextField(
                value = jaFezTerapia,
                onValueChange = { jaFezTerapia = it },
                modifier = Modifier.fillMaxWidth(),
                shape = campoShape
            )

            Spacer(Modifier.height(16.dp))
            Text("🕰️ Rotina e Hábitos", fontWeight = FontWeight.Bold)
            Text("Sono")
            OutlinedTextField(
                value = sono,
                onValueChange = { sono = it },
                modifier = Modifier.fillMaxWidth(),
                shape = campoShape
            )
            Text("Alimentação")
            OutlinedTextField(
                value = alimentacao,
                onValueChange = { alimentacao = it },
                modifier = Modifier.fillMaxWidth(),
                shape = campoShape
            )
            Text("Uso de substâncias")
            OutlinedTextField(
                value = substancias,
                onValueChange = { substancias = it },
                modifier = Modifier.fillMaxWidth(),
                shape = campoShape
            )
            Text("Vida social")
            OutlinedTextField(
                value = vidaSocial,
                onValueChange = { vidaSocial = it },
                modifier = Modifier.fillMaxWidth(),
                shape = campoShape
            )

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    val novo = com.psipro.app.data.entities.HistoricoMedico(
                        patientId = patientId,
                        condicoes = condicoes,
                        medicamentos = medicamentos,
                        internacoes = internacoes,
                        queixasFisicas = queixasFisicas,
                        motivoTerapia = motivoTerapia,
                        tempoSintomas = tempoSintomas,
                        jaFezTerapia = jaFezTerapia,
                        sono = sono,
                        alimentacao = alimentacao,
                        substancias = substancias,
                        vidaSocial = vidaSocial
                    )
                    if (historico == null) viewModel.salvar(novo) else viewModel.editar(novo)
                    onSave()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = botaoShape
            ) {
                Text("Salvar")
            }

            // Exibe o card abaixo do formulário se houver histórico salvo
            historico?.let { historico ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Condições médicas atuais: ${historico.condicoes}")
                        Text("Uso de medicamentos: ${historico.medicamentos}")
                        Text("Histórico de internações: ${historico.internacoes}")
                        Text("Queixas físicas: ${historico.queixasFisicas}")
                        Text("Motivo da terapia: ${historico.motivoTerapia}")
                        Text("Tempo de sintomas: ${historico.tempoSintomas}")
                        Text("Já fez terapia antes: ${historico.jaFezTerapia}")
                        Text("Sono: ${historico.sono}")
                        Text("Alimentação: ${historico.alimentacao}")
                        Text("Uso de substâncias: ${historico.substancias}")
                        Text("Vida social: ${historico.vidaSocial}")
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = {
                            condicoes = historico.condicoes
                            medicamentos = historico.medicamentos
                            internacoes = historico.internacoes
                            queixasFisicas = historico.queixasFisicas
                            motivoTerapia = historico.motivoTerapia
                            tempoSintomas = historico.tempoSintomas
                            jaFezTerapia = historico.jaFezTerapia
                            sono = historico.sono
                            alimentacao = historico.alimentacao
                            substancias = historico.substancias
                            vidaSocial = historico.vidaSocial
                        }) {
                            Text("Editar")
                        }
                    }
                }
            }
        }
    }
} 



