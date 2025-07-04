package com.example.psipro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.psipro.ui.viewmodels.VidaEmocionalViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.graphics.Color

data class VidaEmocionalData(
    val ansiedade: String,
    val depressao: String,
    val trauma: String,
    val luto: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VidaEmocionalScreen(
    patientId: Long,
    onSave: () -> Unit = {},
    onBack: () -> Unit,
    viewModel: VidaEmocionalViewModel = hiltViewModel()
) {
    Text("DEBUG: Entrou no Composable Vida Emocional")
    Text("ID: $patientId")
    if (patientId <= 0) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("ID de paciente inválido", color = MaterialTheme.colorScheme.error)
        }
        return
    }
    val vidaEmocional by viewModel.vidaEmocional.collectAsState()
    val salvoComSucesso by viewModel.salvoComSucesso.collectAsState()
    var ansiedade by remember { mutableStateOf("") }
    var depressao by remember { mutableStateOf("") }
    var trauma by remember { mutableStateOf("") }
    var luto by remember { mutableStateOf("") }
    
    // Carregar dados reais ao abrir
    LaunchedEffect(patientId) {
        viewModel.carregar(patientId)
    }
    // Preencher campos ao carregar do banco
    LaunchedEffect(vidaEmocional) {
        vidaEmocional?.let {
            ansiedade = it.ansiedade
            depressao = it.depressao
            trauma = it.trauma
            luto = it.luto
        }
    }
    // Navegação segura após salvar
    LaunchedEffect(salvoComSucesso) {
        if (salvoComSucesso) {
            onSave()
            viewModel.resetarSucesso()
        }
    }
    val scrollState = rememberScrollState()
    val campoShape = RoundedCornerShape(16.dp)
    val botaoShape = RoundedCornerShape(28.dp)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vida Emocional") },
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
            Text("Nível de ansiedade / estresse", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = ansiedade,
                onValueChange = { ansiedade = it },
                modifier = Modifier.fillMaxWidth(),
                shape = campoShape
            )
            Text("Episódios de depressão", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = depressao,
                onValueChange = { depressao = it },
                modifier = Modifier.fillMaxWidth(),
                shape = campoShape
            )
            Text("Situações traumáticas", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = trauma,
                onValueChange = { trauma = it },
                modifier = Modifier.fillMaxWidth(),
                shape = campoShape
            )
            Text("Luto ou perdas recentes", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = luto,
                onValueChange = { luto = it },
                modifier = Modifier.fillMaxWidth(),
                shape = campoShape
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    val novo = com.example.psipro.data.entities.VidaEmocional(
                        patientId = patientId,
                        ansiedade = ansiedade,
                        depressao = depressao,
                        trauma = trauma,
                        luto = luto
                    )
                    if (vidaEmocional == null) viewModel.salvar(novo) else viewModel.editar(novo)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = botaoShape
            ) {
                Text("Salvar")
            }
            vidaEmocional?.let { vidaEmocional ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Nível de ansiedade / estresse: ${vidaEmocional.ansiedade}")
                        Text("Episódios de depressão: ${vidaEmocional.depressao}")
                        Text("Situações traumáticas: ${vidaEmocional.trauma}")
                        Text("Luto ou perdas recentes: ${vidaEmocional.luto}")
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = {
                            ansiedade = vidaEmocional.ansiedade
                            depressao = vidaEmocional.depressao
                            trauma = vidaEmocional.trauma
                            luto = vidaEmocional.luto
                        }) {
                            Text("Editar")
                        }
                    }
                }
            }
        }
    }
} 