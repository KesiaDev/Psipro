package com.psipro.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.psipro.app.ui.viewmodels.HistoricoFamiliarViewModel
import androidx.hilt.navigation.compose.hiltViewModel

data class HistoricoFamiliarData(
    val relacaoPais: String,
    val ambienteFamiliar: String,
    val transtornos: String,
    val vinculosAfetivos: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoricoFamiliarScreen(
    patientId: Long,
    onSave: () -> Unit = {},
    onBack: () -> Unit,
    viewModel: HistoricoFamiliarViewModel = hiltViewModel()
) {
    Text("DEBUG: Entrou no Composable Histórico Familiar")
    Text("ID: $patientId")
    if (patientId <= 0) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("ID de paciente inválido", color = MaterialTheme.colorScheme.error)
        }
        return
    }
    val historico by viewModel.historico.collectAsState()
    val salvoComSucesso by viewModel.salvoComSucesso.collectAsState()
    var relacaoPais by remember { mutableStateOf("") }
    var ambienteFamiliar by remember { mutableStateOf("") }
    var transtornos by remember { mutableStateOf("") }
    var vinculosAfetivos by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    val bronze = MaterialTheme.colorScheme.primary
    val campoShape = RoundedCornerShape(12.dp)
    var showSnackbar by remember { mutableStateOf(false) }
    
    // Carregar dados reais ao abrir
    LaunchedEffect(patientId) {
        viewModel.carregar(patientId)
    }
    // Preencher campos ao carregar do banco
    LaunchedEffect(historico) {
        historico?.let {
            relacaoPais = it.relacaoPais
            ambienteFamiliar = it.ambienteFamiliar
            transtornos = it.transtornos
            vinculosAfetivos = it.vinculosAfetivos
        }
    }
    // Navegação segura após salvar
    LaunchedEffect(salvoComSucesso) {
        if (salvoComSucesso) {
            onSave()
            viewModel.resetarSucesso()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Histórico Familiar", fontWeight = FontWeight.Bold, fontSize = 22.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Relação com pai/mãe
            Text(
                "👤 Relação com pai/mãe",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 2.dp),
                color = MaterialTheme.colorScheme.onBackground
            )
            OutlinedTextField(
                value = relacaoPais,
                onValueChange = { relacaoPais = it },
                leadingIcon = { Text("👤", fontSize = 20.sp) },
                shape = campoShape,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, campoShape),
                colors = androidx.compose.material3.TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = bronze,
                    cursorColor = bronze,
                    containerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
            )
            // Ambiente familiar atual
            Text(
                "🏡 Ambiente familiar atual",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 2.dp),
                color = MaterialTheme.colorScheme.onBackground
            )
            OutlinedTextField(
                value = ambienteFamiliar,
                onValueChange = { ambienteFamiliar = it },
                leadingIcon = { Text("🏡", fontSize = 20.sp) },
                shape = campoShape,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, campoShape),
                colors = androidx.compose.material3.TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = bronze,
                    cursorColor = bronze,
                    containerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
            )
            // Ocorrência de transtornos mentais na família
            Text(
                "🧠 Ocorrência de transtornos mentais na família",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 2.dp),
                color = MaterialTheme.colorScheme.onBackground
            )
            OutlinedTextField(
                value = transtornos,
                onValueChange = { transtornos = it },
                leadingIcon = { Text("🧠", fontSize = 20.sp) },
                shape = campoShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, campoShape),
                colors = androidx.compose.material3.TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = bronze,
                    cursorColor = bronze,
                    containerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                maxLines = 3
            )
            // Vínculos afetivos
            Text(
                "❤️ Vínculos afetivos",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 2.dp),
                color = MaterialTheme.colorScheme.onBackground
            )
            OutlinedTextField(
                value = vinculosAfetivos,
                onValueChange = { vinculosAfetivos = it },
                leadingIcon = { Text("❤️", fontSize = 20.sp) },
                shape = campoShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, campoShape),
                colors = androidx.compose.material3.TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = bronze,
                    cursorColor = bronze,
                    containerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    val novo = com.psipro.app.data.entities.HistoricoFamiliar(
                        patientId = patientId,
                        relacaoPais = relacaoPais,
                        ambienteFamiliar = ambienteFamiliar,
                        transtornos = transtornos,
                        vinculosAfetivos = vinculosAfetivos
                    )
                    if (historico == null) viewModel.salvar(novo) else viewModel.editar(novo)
                    showSnackbar = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(4.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = bronze)
            ) {
                Text("💾", fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Text("Salvar", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            if (showSnackbar) {
                LaunchedEffect(Unit) {
                    snackbarHostState.showSnackbar("Salvo com sucesso!")
                    showSnackbar = false
                }
            }
            // Card de dados salvos (abaixo do formulário)
            historico?.let { historico ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Relação com pai/mãe: ${historico.relacaoPais}", color = MaterialTheme.colorScheme.onSurface)
                        Text("Ambiente familiar: ${historico.ambienteFamiliar}", color = MaterialTheme.colorScheme.onSurface)
                        Text("Transtornos mentais: ${historico.transtornos}", color = MaterialTheme.colorScheme.onSurface)
                        Text("Vínculos afetivos: ${historico.vinculosAfetivos}", color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = {
                            relacaoPais = historico.relacaoPais
                            ambienteFamiliar = historico.ambienteFamiliar
                            transtornos = historico.transtornos
                            vinculosAfetivos = historico.vinculosAfetivos
                        }) {
                            Text("Editar")
                        }
                    }
                }
            }
        }
    }
} 



