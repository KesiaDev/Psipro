package com.psipro.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.psipro.app.data.entities.AnamneseCampo
import com.psipro.app.data.entities.AnamneseModel
import com.psipro.app.data.entities.AnamnesePreenchida
import com.psipro.app.ui.viewmodels.AnamneseViewModel
import kotlinx.coroutines.launch

enum class AnamneseScreen {
    LIST,
    MODEL_SELECTION,
    MODEL_EDIT,
    FORM_FILL
}

@Composable
fun AnamneseCompleteFlow(
    pacienteId: Long,
    pacienteNome: String,
    anamneseViewModel: AnamneseViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    var currentScreen by remember { mutableStateOf(AnamneseScreen.LIST) }
    var selectedModel by remember { mutableStateOf<AnamneseModel?>(null) }
    var editingModel by remember { mutableStateOf<AnamneseModel?>(null) }
    var editingFields by remember { mutableStateOf<List<AnamneseCampo>>(emptyList()) }
    var showSnackbar by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    val modelos by anamneseViewModel.modelos.collectAsState()
    val anamneses by anamneseViewModel.anamneses.collectAsState()
    val camposModelo by anamneseViewModel.camposModelo.collectAsState()
    val isLoading by anamneseViewModel.isLoading.collectAsState()
    val error by anamneseViewModel.error.collectAsState()

    // Carregar dados iniciais
    LaunchedEffect(Unit) {
        anamneseViewModel.carregarModelos()
        anamneseViewModel.carregarAnamnesesPaciente(pacienteId)
    }

    // Mostrar erros
    LaunchedEffect(error) {
        error?.let { showSnackbar = it }
    }

    // Limpar erro quando mudar de tela
    LaunchedEffect(currentScreen) {
        anamneseViewModel.limparErro()
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = remember { SnackbarHostState() }
            ) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { showSnackbar = null }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(showSnackbar ?: "")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentScreen) {
                AnamneseScreen.LIST -> {
                    AnamneseListScreen(
                        anamneses = anamneses,
                        modelos = modelos,
                        onNovaAnamnese = { currentScreen = AnamneseScreen.MODEL_SELECTION },
                        onEditarAnamnese = { anamnese ->
                            // Implementar edição de anamnese preenchida
                            showSnackbar = "Edição de anamnese em desenvolvimento"
                        },
                        onExportarAnamnese = { anamnese ->
                            // Implementar exportação
                            showSnackbar = "Exportação em desenvolvimento"
                        },
                        onBack = onBack,
                        isLoading = isLoading
                    )
                }
                
                AnamneseScreen.MODEL_SELECTION -> {
                    AnamneseModelSelectionScreen(
                        modelos = modelos,
                        onModeloSelecionado = { modelo ->
                            selectedModel = modelo
                            anamneseViewModel.carregarCampos(modelo.id)
                            currentScreen = AnamneseScreen.FORM_FILL
                        },
                        onCriarModelo = {
                            editingModel = null
                            editingFields = emptyList()
                            currentScreen = AnamneseScreen.MODEL_EDIT
                        },
                        onEditarModelo = { modelo ->
                            editingModel = modelo
                            scope.launch {
                                editingFields = anamneseViewModel.carregarCamposModelo(modelo.id)
                                currentScreen = AnamneseScreen.MODEL_EDIT
                            }
                        },
                        onRemoverModelo = { modelo ->
                            anamneseViewModel.removerModelo(modelo) { success ->
                                if (success) {
                                    showSnackbar = "Modelo removido com sucesso"
                                }
                            }
                        },
                        onBack = { currentScreen = AnamneseScreen.LIST },
                        isLoading = isLoading
                    )
                }
                
                AnamneseScreen.MODEL_EDIT -> {
                    AnamneseModelEditScreen(
                        modelo = editingModel,
                        camposIniciais = editingFields,
                        onSalvar = { modelo, campos ->
                            if (editingModel == null) {
                                anamneseViewModel.criarModelo(modelo.nome, campos) { success ->
                                    if (success) {
                                        showSnackbar = "Modelo criado com sucesso"
                                        currentScreen = AnamneseScreen.MODEL_SELECTION
                                    }
                                }
                            } else {
                                anamneseViewModel.editarModelo(modelo, campos) { success ->
                                    if (success) {
                                        showSnackbar = "Modelo atualizado com sucesso"
                                        currentScreen = AnamneseScreen.MODEL_SELECTION
                                    }
                                }
                            }
                        },
                        onBack = { currentScreen = AnamneseScreen.MODEL_SELECTION }
                    )
                }
                
                AnamneseScreen.FORM_FILL -> {
                    if (selectedModel != null && camposModelo.isNotEmpty()) {
                        AnamneseFormScreen(
                            campos = camposModelo,
                            onSalvar = { respostas ->
                                anamneseViewModel.salvarAnamnese(
                                    pacienteId = pacienteId,
                                    modeloId = selectedModel!!.id,
                                    respostas = respostas
                                )
                                showSnackbar = "Anamnese salva com sucesso"
                                currentScreen = AnamneseScreen.LIST
                            },
                            onCancelar = { currentScreen = AnamneseScreen.MODEL_SELECTION }
                        )
                    } else {
                        // Loading ou erro
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator()
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Erro ao carregar modelo")
                                    Button(
                                        onClick = { currentScreen = AnamneseScreen.MODEL_SELECTION }
                                    ) {
                                        Text("Voltar")
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



