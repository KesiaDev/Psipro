package com.psipro.app

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

/**
 * Composables stub para testes sem o app real.
 * Ao integrar no psipro-app, substitua as chamadas por LoginScreen(), DashboardScreen(), etc.
 */
object StubScreens {

    @Composable
    fun LoginScreen(
        onLoginSuccess: () -> Unit = {},
        onLoginError: (String) -> Unit = {}
    ) {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }
        var success by remember { mutableStateOf(false) }

        if (success) {
            Text("Dashboard")
        } else {
            Column {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-mail") }
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Senha") }
                )
                if (error != null) {
                    Text(error!!)
                }
                Button(onClick = {
                    if (email == "terapeutaclaudiacruz@gmail.com" && password == "senha123") {
                        success = true
                        onLoginSuccess()
                    } else {
                        error = "inválido"
                        onLoginError("inválido")
                    }
                }) { Text("Entrar") }
            }
        }
    }

    @Composable
    fun CadastroPacienteScreen() {
        var nome by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var telefone by remember { mutableStateOf("") }
        Column {
            OutlinedTextField(value = nome, onValueChange = { nome = it }, label = { Text("Nome") })
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("E-mail") })
            OutlinedTextField(value = telefone, onValueChange = { telefone = it }, label = { Text("Telefone") })
            Button(onClick = {}) { Text("Salvar") }
        }
    }

    @Composable
    fun ListaPacientesScreen() {
        Text("Pacientes")
    }

    @Composable
    fun ProntuarioScreen() {
        var anotacao by remember { mutableStateOf("") }
        Column {
            Text("Prontuário")
            OutlinedTextField(value = anotacao, onValueChange = { anotacao = it }, label = { Text("Anotação") })
            Button(onClick = {}) { Text("Salvar") }
        }
    }

    @Composable
    fun AgendaScreen() {
        Column {
            Text("Agenda")
            Text("Sessão")
            Button(onClick = {}) { Text("Nova sessão") }
        }
    }

    @Composable
    fun DashboardScreen() {
        Column {
            Text("Dashboard")
            Text("Resumo")
            Button(onClick = {}) { Text("Agenda") }
            Button(onClick = {}) { Text("Pacientes") }
        }
    }

    @Composable
    fun NotificationsScreen() {
        Text("Notificações")
    }

    @Composable
    fun VoiceScreen() {
        Column(modifier = Modifier.semantics { contentDescription = "Gravar áudio" }) {
            Button(onClick = {}) { Text("Gravar") }
        }
    }

    @Composable
    fun TranscriptScreen() {
        Column {
            Text("Transcrição")
            Text("Insights")
        }
    }

    @Composable
    fun SyncScreen() {
        Column {
            Text("Sincronizando")
            Button(onClick = {}) { Text("Sincronizar") }
        }
    }
}
