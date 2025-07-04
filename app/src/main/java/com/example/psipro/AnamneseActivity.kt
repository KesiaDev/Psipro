package com.example.psipro

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import com.example.psipro.ui.screens.FichaPacienteScreen
import com.example.psipro.ui.compose.PsiproTheme
import dagger.hilt.android.AndroidEntryPoint
import com.example.psipro.ui.screens.MenuAnamneseScreen
import com.example.psipro.ui.screens.PacienteInfo
import com.example.psipro.ui.screens.getSecoesExemplo
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.activity.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.collectAsState
import com.example.psipro.ui.viewmodels.PatientViewModel
import java.util.Calendar
import java.util.Date
import com.example.psipro.ui.screens.HistoricoFamiliarScreen
import com.example.psipro.ui.screens.HistoricoMedicoScreen
import androidx.appcompat.app.AppCompatDelegate
import android.content.Context
import com.example.psipro.ui.screens.VidaEmocionalScreen
import com.example.psipro.ui.screens.ObservacoesClinicasScreen
import com.example.psipro.ui.screens.AnotacoesSessaoScreen
import com.example.psipro.ui.screens.SessaoInfo
import com.example.psipro.ui.screens.AnotacaoSessaoData
import com.example.psipro.ui.screens.SecaoAnamneseType

@AndroidEntryPoint
class AnamneseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val patientId = intent.getLongExtra("PATIENT_ID", -1)
        val patientName = intent.getStringExtra("PATIENT_NAME") ?: ""
        
        Log.d("AnamneseActivity", "onCreate - Patient ID: $patientId, Name: $patientName")
        
        if (patientId == -1L) {
            Log.e("AnamneseActivity", "Invalid patient ID")
            finish()
            return
        }
        
        setContent {
            val patientViewModel: PatientViewModel = viewModel()
            val patientState by patientViewModel.currentPatient.collectAsState()
            androidx.compose.runtime.LaunchedEffect(patientId) {
                patientViewModel.loadPatient(patientId)
            }
            PsiproTheme(useDarkTheme = isDarkModeActive(this)) {
                var secaoSelecionada by remember { mutableStateOf<SecaoAnamneseType?>(null) }
                val paciente = patientState?.let {
                    PacienteInfo(
                        nome = it.name,
                        idade = calcularIdade(it.birthDate)
                    )
                }
                val secoes = getSecoesExemplo()
                if (secaoSelecionada == null) {
                    MenuAnamneseScreen(
                        paciente = paciente ?: PacienteInfo("-", 0),
                        secoes = secoes,
                        onSecaoClick = { secao -> secaoSelecionada = secao.tipo },
                        onBack = { finish() }
                    )
                } else {
                    when (secaoSelecionada) {
                        SecaoAnamneseType.DADOS_PESSOAIS -> {
                            Scaffold(
                                topBar = {},
                                snackbarHost = { SnackbarHost(remember { SnackbarHostState() }) }
                            ) { innerPadding ->
                                Column(
                                    modifier = Modifier
                                        .padding(innerPadding)
                                        .fillMaxSize()
                                        .padding(24.dp)
                                ) {
                                    Text(
                                        text = "Dados Pessoais",
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(text = "Nome: ${paciente?.nome ?: "-"}", style = MaterialTheme.typography.bodyLarge)
                                    Text(text = "Idade: ${paciente?.idade ?: "-"} anos", style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                        SecaoAnamneseType.HISTORICO_FAMILIAR -> {
                            HistoricoFamiliarScreen(
                                patientId = patientId,
                                onSave = { secaoSelecionada = null },
                                onBack = { secaoSelecionada = null },
                                viewModel = androidx.hilt.navigation.compose.hiltViewModel()
                            )
                        }
                        SecaoAnamneseType.HISTORICO_MEDICO -> {
                            HistoricoMedicoScreen(
                                patientId = patientId,
                                onSave = { secaoSelecionada = null },
                                onBack = { secaoSelecionada = null },
                                viewModel = androidx.hilt.navigation.compose.hiltViewModel()
                            )
                        }
                        SecaoAnamneseType.VIDA_EMOCIONAL -> {
                            VidaEmocionalScreen(
                                patientId = patientId,
                                onSave = { secaoSelecionada = null },
                                onBack = { secaoSelecionada = null },
                                viewModel = androidx.hilt.navigation.compose.hiltViewModel()
                            )
                        }
                        SecaoAnamneseType.OBSERVACOES_CLINICAS -> {
                            ObservacoesClinicasScreen(
                                patientId = patientId,
                                onSave = { secaoSelecionada = null },
                                onBack = { secaoSelecionada = null },
                                viewModel = androidx.hilt.navigation.compose.hiltViewModel()
                            )
                        }
                        SecaoAnamneseType.ANOTACOES_SESSAO -> {
                            AnotacoesSessaoScreen(
                                patientId = patientId,
                                onSave = { secaoSelecionada = null },
                                onBack = { secaoSelecionada = null },
                                viewModel = androidx.hilt.navigation.compose.hiltViewModel()
                            )
                        }
                        null -> {
                            Scaffold(
                                topBar = {},
                                snackbarHost = { SnackbarHost(remember { SnackbarHostState() }) }
                            ) { innerPadding ->
                                androidx.compose.material3.Text(
                                    text = "Seção: ${secaoSelecionada?.name}",
                                    modifier = androidx.compose.ui.Modifier.padding(innerPadding).fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Função utilitária para calcular idade a partir da data de nascimento
fun calcularIdade(dataNascimento: Date): Int {
    val hoje = Calendar.getInstance()
    val nascimento = Calendar.getInstance().apply { time = dataNascimento }
    var idade = hoje.get(Calendar.YEAR) - nascimento.get(Calendar.YEAR)
    if (hoje.get(Calendar.DAY_OF_YEAR) < nascimento.get(Calendar.DAY_OF_YEAR)) {
        idade--
    }
    return idade
}

// Função utilitária para saber se o modo escuro está ativo
fun isDarkModeActive(context: Context): Boolean {
    return when (AppCompatDelegate.getDefaultNightMode()) {
        AppCompatDelegate.MODE_NIGHT_YES -> true
        AppCompatDelegate.MODE_NIGHT_NO -> false
        else -> {
            val uiMode = context.resources.configuration.uiMode
            (uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        }
    }
} 