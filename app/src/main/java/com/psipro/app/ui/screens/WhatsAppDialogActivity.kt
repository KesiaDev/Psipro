package com.psipro.app.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.psipro.app.data.entities.Patient
import com.psipro.app.data.entities.AnamneseGroup
import com.psipro.app.ui.compose.PsiproTheme
import com.psipro.app.utils.TipoMensagemWhatsApp
import com.psipro.app.utils.WhatsAppUtils
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.Date
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WhatsAppDialogActivity : ComponentActivity() {
    
    @Inject
    lateinit var patientDao: com.psipro.app.data.dao.PatientDao
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val patientId = intent.getLongExtra("patient_id", -1)
        val patientName = intent.getStringExtra("patient_name") ?: ""
        val psicologoNome = intent.getStringExtra("psicologo_nome") ?: "Psicólogo"
        
        setContent {
            PsiproTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WhatsAppMensagemDialog(
                        patientId = patientId,
                        patientName = patientName,
                        psicologoNome = psicologoNome,
                        onDismiss = { finish() },
                        onMensagemSelecionada = { tipo, dadosExtras ->
                            // Obter o paciente completo do banco de dados usando Hilt
                            lifecycleScope.launch {
                                val paciente = patientDao.getPatientById(patientId)
                                if (paciente != null) {
                                    // Abrir WhatsApp com a mensagem selecionada
                                    WhatsAppUtils.abrirWhatsappComMensagem(
                                        this@WhatsAppDialogActivity,
                                        paciente,
                                        psicologoNome,
                                        tipo,
                                        dadosExtras
                                    )
                                } else {
                                    // Se não encontrar o paciente, usar dados básicos
                                    val pacienteBasico = Patient(
                                        id = patientId,
                                        name = patientName,
                                        phone = "", // Será validado no WhatsAppUtils
                                        email = "",
                                        birthDate = Date(),
                                        cpf = "",
                                        anamneseGroup = AnamneseGroup.ADULTO,
                                        createdAt = Date(),
                                        updatedAt = Date()
                                    )
                                    WhatsAppUtils.abrirWhatsappComMensagem(
                                        this@WhatsAppDialogActivity,
                                        pacienteBasico,
                                        psicologoNome,
                                        tipo,
                                        dadosExtras
                                    )
                                }
                                finish()
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppMensagemDialog(
    patientId: Long,
    patientName: String,
    psicologoNome: String,
    onDismiss: () -> Unit,
    onMensagemSelecionada: (TipoMensagemWhatsApp, Map<String, String>) -> Unit
) {
    var showPreview by remember { mutableStateOf<TipoMensagemWhatsApp?>(null) }
    var dadosExtras by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    
    // Criar um paciente temporário para usar nas funções
    val paciente = remember {
        Patient(
            id = patientId,
            name = patientName,
            phone = "", // Será preenchido pelo DetalhePacienteActivity
            email = "",
            birthDate = Date(),
            cpf = "",
            anamneseGroup = AnamneseGroup.ADULTO,
            createdAt = Date(),
            updatedAt = Date()
        )
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "WhatsApp",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Enviar Mensagem WhatsApp",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar"
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Informações do paciente
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Paciente: $patientName",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Psicólogo: $psicologoNome",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Lista de tipos de mensagem
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(WhatsAppUtils.getTiposMensagem()) { tipo ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                showPreview = tipo
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = if (showPreview == tipo) 
                                    MaterialTheme.colorScheme.primaryContainer 
                                else 
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = WhatsAppUtils.getTituloTipoMensagem(tipo),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = WhatsAppUtils.gerarMensagemPorTipo(tipo, paciente, psicologoNome, dadosExtras),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Botões
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                    
                    Button(
                        onClick = {
                            showPreview?.let { tipo ->
                                onMensagemSelecionada(tipo, dadosExtras)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = showPreview != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Enviar")
                    }
                }
            }
        }
    }
} 



