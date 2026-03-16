package com.psipro.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.psipro.app.utils.VoiceAction

/**
 * Estado do diálogo de comando de voz.
 */
data class VoiceCommandState(
    val isListening: Boolean = false,
    val recognizedText: String? = null,
    val recognizedAction: VoiceAction? = null,
    val error: String? = null
)

@Composable
fun VoiceCommandDialog(
    state: VoiceCommandState,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val feedbackText = when {
        state.isListening -> "Ouvindo... Fale o comando."
        state.recognizedText != null -> {
            val actionName = state.recognizedAction?.displayName?.takeIf { it.isNotEmpty() }
                ?: state.recognizedText
            "Comando reconhecido: $actionName"
        }
        state.error != null -> state.error
        else -> "Toque no microfone para dar um comando de voz"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Comando de voz")
        },
        text = {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Diálogo de comando de voz. $feedbackText"
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (state.isListening) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Ouvindo",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = feedbackText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Ex.: \"nova sessão\", \"pacientes\", \"agenda de hoje\", \"dashboard\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (state.isListening) {
                    OutlinedButton(
                        onClick = onStopListening,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "Parar escuta" }
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(8.dp))
                        Text("Parar")
                    }
                } else {
                    Button(
                        onClick = onStartListening,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "Comando de voz. Toque para falar" }
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(8.dp))
                        Text("Ouvir comando")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(if (state.recognizedText != null || state.error != null) "Fechar" else "Cancelar")
            }
        },
        dismissButton = if (state.recognizedText != null || state.error != null) {
            {
                OutlinedButton(onClick = onStartListening) {
                    Text("Tentar novamente")
                }
            }
        } else null
    )
}
