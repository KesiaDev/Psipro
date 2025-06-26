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

@Composable
fun FichaPacienteScreen(
    pacienteId: Long,
    modeloId: Long,
    anamneseViewModel: AnamneseViewModel = hiltViewModel()
) {
    var mostrarMenuAnamnese by remember { mutableStateOf(false) }
    var modeloSelecionadoId by remember { mutableStateOf(modeloId) }
    val modelos by anamneseViewModel.modelos.collectAsState()
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
        // Mostra o menu de seções de anamnese
        val paciente = PacienteInfo(nome = "João da Silva", idade = 30) // Substitua por dados reais
        val secoes = getSecoesExemplo()
        var secaoSelecionada by remember { mutableStateOf<String?>(null) }
        if (secaoSelecionada == null) {
            MenuAnamneseScreen(
                paciente = paciente,
                secoes = secoes,
                onSecaoClick = { secao -> secaoSelecionada = secao.titulo },
                onBack = { mostrarMenuAnamnese = false }
            )
        } else {
            // Aqui você pode navegar para o formulário da seção selecionada
            Text(
                text = "Seção: $secaoSelecionada",
                modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)
            )
        }
    }
} 