package com.example.psipro.ui.screens

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.psipro.ui.viewmodels.AnamneseViewModel
import com.example.psipro.R
import androidx.hilt.navigation.compose.hiltViewModel
import android.util.Log
import com.example.psipro.ui.screens.MenuAnamneseScreen
import com.example.psipro.ui.screens.PacienteInfo
import com.example.psipro.ui.screens.getSecoesExemplo
import com.example.psipro.ui.viewmodels.HistoricoFamiliarViewModel
import com.example.psipro.ui.viewmodels.HistoricoMedicoViewModel
import com.example.psipro.ui.viewmodels.VidaEmocionalViewModel
import com.example.psipro.ui.viewmodels.ObservacoesClinicasViewModel
import com.example.psipro.ui.viewmodels.AnotacaoSessaoViewModel
import com.example.psipro.ui.screens.AnotacoesSessaoScreen
import com.example.psipro.ui.screens.SecaoAnamneseType

@Composable
fun FichaPacienteScreen(
    pacienteId: Long,
    modeloId: Long,
    anamneseViewModel: AnamneseViewModel = hiltViewModel()
) {
    var mostrarMenuAnamnese by remember { mutableStateOf(false) }
    var modeloSelecionadoId by remember { mutableStateOf(modeloId) }
    
    val modelos by anamneseViewModel.modelos.collectAsState()
    val secaoSelecionada by anamneseViewModel.secaoSelecionada.collectAsState()
    val isLoading by anamneseViewModel.isLoading.collectAsState()
    val error by anamneseViewModel.error.collectAsState()
    val secoesPreenchidas by anamneseViewModel.secoesPreenchidas.collectAsState()
    
    val bronze = MaterialTheme.colorScheme.primary
    val onBronze = MaterialTheme.colorScheme.onPrimary
    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface

    LaunchedEffect(Unit) {
        Log.d("FichaPacienteScreen", "LaunchedEffect - Carregando modelos")
        anamneseViewModel.carregarModelos()
    }

    LaunchedEffect(mostrarMenuAnamnese) {
        if (mostrarMenuAnamnese) {
            Log.d("FichaPacienteScreen", "LaunchedEffect - Carregando anamneses do paciente $pacienteId")
            anamneseViewModel.carregarAnamnesesPaciente(pacienteId)
        }
    }

    // Limpar erro quando sair do menu
    LaunchedEffect(mostrarMenuAnamnese) {
        if (!mostrarMenuAnamnese) {
            anamneseViewModel.limparErro()
            anamneseViewModel.setSecaoSelecionada(null)
        }
    }

    Log.d("FichaPacienteScreen", "Renderizando - Paciente ID: $pacienteId, Modelos: ${modelos.size}")

    if (!mostrarMenuAnamnese) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Ícone ilustrativo
                Surface(
                    shape = CircleShape,
                    color = bronze.copy(alpha = 0.12f),
                    modifier = Modifier.size(80.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_profile_placeholder),
                        contentDescription = "Paciente",
                        tint = bronze,
                        modifier = Modifier.padding(16.dp).size(48.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                // Título
                Text(
                    text = "Ficha do Paciente",
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 26.sp),
                    color = onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ID: $pacienteId",
                    style = MaterialTheme.typography.bodyMedium,
                    color = onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Mensagem amigável
                if (modelos.isEmpty()) {
                    Text(
                        text = "Nenhum modelo de anamnese disponível.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                } else {
                    Text(
                        text = "Modelos disponíveis: ${modelos.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Spacer(modifier = Modifier.height(24.dp))
                // Botão abrir anamnese
                Button(
                    onClick = { mostrarMenuAnamnese = true },
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = bronze,
                        contentColor = onBronze
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Text("Abrir Anamnese", fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                // Botão voltar
                OutlinedButton(
                    onClick = { mostrarMenuAnamnese = false },
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = bronze
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 2.dp,
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(bronze, bronze))
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Text("Voltar", fontSize = 18.sp)
                }
            }
        }
    } else {
        // Mostra o menu de seções de anamnese ou a seção selecionada
        val paciente = PacienteInfo(nome = "João da Silva", idade = 30) // Substitua por dados reais
        
        // Criar seções com status de preenchimento dinâmico
        val secoes = getSecoesExemplo().map { secao ->
            secao.copy(preenchido = secoesPreenchidas[secao.tipo] ?: false)
        }
        
        if (secaoSelecionada == null) {
            MenuAnamneseScreen(
                paciente = paciente,
                secoes = secoes,
                onSecaoClick = { secao -> 
                    anamneseViewModel.setSecaoSelecionada(secao.tipo)
                },
                onBack = { mostrarMenuAnamnese = false }
            )
        } else {
            // Mostrar loading se necessário
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Mostrar erro se houver
                error?.let { errorMessage ->
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
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
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { anamneseViewModel.limparErro() }
                        ) {
                            Text("Tentar Novamente")
                        }
                    }
                } ?: run {
                    // Navegar para a seção correta baseada no enum
                    when (secaoSelecionada) {
                        SecaoAnamneseType.HISTORICO_FAMILIAR -> HistoricoFamiliarScreen(
                            patientId = pacienteId,
                            onSave = { /* ação após salvar */ },
                            onBack = { anamneseViewModel.setSecaoSelecionada(null) },
                            viewModel = hiltViewModel<HistoricoFamiliarViewModel>()
                        )
                        SecaoAnamneseType.HISTORICO_MEDICO -> HistoricoMedicoScreen(
                            patientId = pacienteId,
                            onSave = { /* ação após salvar */ },
                            onBack = { anamneseViewModel.setSecaoSelecionada(null) },
                            viewModel = hiltViewModel<HistoricoMedicoViewModel>()
                        )
                        SecaoAnamneseType.VIDA_EMOCIONAL -> VidaEmocionalScreen(
                            patientId = pacienteId,
                            onSave = { /* ação após salvar */ },
                            onBack = { anamneseViewModel.setSecaoSelecionada(null) },
                            viewModel = hiltViewModel<VidaEmocionalViewModel>()
                        )
                        SecaoAnamneseType.OBSERVACOES_CLINICAS -> ObservacoesClinicasScreen(
                            patientId = pacienteId,
                            onSave = { /* ação após salvar */ },
                            onBack = { anamneseViewModel.setSecaoSelecionada(null) },
                            viewModel = hiltViewModel<ObservacoesClinicasViewModel>()
                        )
                        SecaoAnamneseType.ANOTACOES_SESSAO -> AnotacoesSessaoScreen(
                            patientId = pacienteId,
                            onSave = { /* ação após salvar */ },
                            onBack = { anamneseViewModel.setSecaoSelecionada(null) },
                            viewModel = hiltViewModel<AnotacaoSessaoViewModel>()
                        )
                        SecaoAnamneseType.DADOS_PESSOAIS -> {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Dados Pessoais",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Funcionalidade em desenvolvimento")
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { anamneseViewModel.setSecaoSelecionada(null) }
                                ) {
                                    Text("Voltar")
                                }
                            }
                        }
                        null -> { /* Não faz nada, placeholder vazio */ }
                    }
                }
            }
        }
    }
} 