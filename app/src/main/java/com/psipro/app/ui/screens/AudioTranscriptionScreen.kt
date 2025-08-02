package com.psipro.app.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.psipro.app.ui.viewmodels.AudioTranscriptionViewModel
import androidx.compose.ui.unit.dp

@Composable
fun AudioTranscriptionScreen(
    pacienteId: String,
    dataSessao: String,
    viewModel: AudioTranscriptionViewModel = viewModel()
) {
    val isRecording by viewModel.isRecording.collectAsState()
    val status by viewModel.status.collectAsState()
    var texto by remember { mutableStateOf("") }
    val transcription by viewModel.transcription.collectAsState()
    val context = LocalContext.current
    val selectAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.transcribeFromUri(context, uri, pacienteId, dataSessao)
        }
    }

    LaunchedEffect(transcription) {
        texto = transcription
    }

    Column(modifier = Modifier.padding(24.dp)) {
        Button(
            onClick = { if (!isRecording) viewModel.startRecording() else viewModel.stopRecording() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (!isRecording) "Gravar Áudio" else "Parar Gravação")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { selectAudioLauncher.launch("audio/*") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isRecording
        ) {
            Text("Selecionar Áudio para Transcrição")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { viewModel.transcribe(pacienteId, dataSessao) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isRecording
        ) {
            Text("Transcrever e Salvar")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = status)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = texto,
            onValueChange = { texto = it },
            label = { Text("Transcrição") },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { viewModel.atualizarTranscricaoFirestore(texto) },
            modifier = Modifier.fillMaxWidth(),
            enabled = texto.isNotBlank()
        ) {
            Text("Atualizar Transcrição no Firestore")
        }
    }
} 



