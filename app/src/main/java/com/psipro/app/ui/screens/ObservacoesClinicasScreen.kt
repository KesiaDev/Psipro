package com.psipro.app.ui.screens

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import com.psipro.app.ui.viewmodels.ObservacoesClinicasViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObservacoesClinicasScreen(
    patientId: Long,
    onSave: () -> Unit = {},
    onBack: () -> Unit,
    viewModel: ObservacoesClinicasViewModel = hiltViewModel()
) {
    Text("DEBUG: Entrou no Composable Observações Clínicas")
    Text("ID: $patientId")
    if (patientId <= 0) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("ID de paciente inválido", color = MaterialTheme.colorScheme.error)
        }
        return
    }
    val observacoes by viewModel.observacoes.collectAsState()
    val salvoComSucesso by viewModel.salvoComSucesso.collectAsState()
    var texto by remember { mutableStateOf("") }
    
    // Carregar dados reais ao abrir
    LaunchedEffect(patientId) {
        viewModel.carregar(patientId)
    }
    // Preencher campos ao carregar do banco
    LaunchedEffect(observacoes) {
        observacoes?.let {
            texto = it.observacoes
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
                title = { Text("Observações Clínicas") },
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
            Text("Anote aqui qualquer observação clínica relevante:", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = campoShape,
                maxLines = Int.MAX_VALUE,
                placeholder = { Text("Digite suas observações clínicas...") }
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    val novo = com.psipro.app.data.entities.ObservacoesClinicas(
                        patientId = patientId,
                        observacoes = texto
                    )
                    if (observacoes == null) viewModel.salvar(novo) else viewModel.editar(novo)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = botaoShape
            ) {
                Text("Salvar")
            }
            observacoes?.let { obs ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(obs.observacoes)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { texto = obs.observacoes }) {
                            Text("Editar")
                        }
                    }
                }
            }
        }
    }
} 



