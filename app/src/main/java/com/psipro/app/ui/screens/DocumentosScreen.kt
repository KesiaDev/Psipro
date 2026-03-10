package com.psipro.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.psipro.app.data.entities.Documento
import com.psipro.app.data.entities.TipoDocumento
import com.psipro.app.ui.viewmodels.DocumentoViewModel
import java.text.SimpleDateFormat
import java.util.*
import com.psipro.app.data.entities.Patient
import android.util.Log
import android.content.Context

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentosScreen(
    patientId: Long,
    patientName: String,
    onBack: () -> Unit,
    viewModel: DocumentoViewModel = hiltViewModel()
) {
    val documentos by viewModel.documentos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedTipo by remember { mutableStateOf<TipoDocumento?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var documentoParaEditar by remember { mutableStateOf<Documento?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var documentoParaExcluir by remember { mutableStateOf<Documento?>(null) }
    var showTemplatePreview by remember { mutableStateOf(false) }
    var templatePreview by remember { mutableStateOf("") }
    var templateTitle by remember { mutableStateOf("") }
    
    // Buscar dados reais do paciente e psicólogo
    var patientData by remember { mutableStateOf<Patient?>(null) }
    var psychologistData by remember { mutableStateOf<String?>(null) }
    var psychologistCRP by remember { mutableStateOf<String?>(null) }
    var psychologistLocation by remember { mutableStateOf<String?>(null) }
    
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    
    // Cores dinâmicas baseadas no tema
    val isDarkTheme = MaterialTheme.colorScheme.surface == Color(0xFF1E1E1E)
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    val context = LocalContext.current

    // Função para mostrar preview do template
    fun showTemplatePreview(template: String, title: String) {
        // Usar dados reais se disponíveis, senão usar dados padrão
        val nomePaciente = patientData?.name ?: "Nome do Paciente"
        val cpfPaciente = patientData?.cpf ?: "CPF não informado"
        val dataNascimento = patientData?.birthDate?.let { dateFormatter.format(it) } ?: "Data não informada"
        val nomePsicologo = psychologistData ?: "Profissional"
        val crpPsicologo = psychologistCRP ?: "CRP"
        val local = psychologistLocation ?: "Local não informado"
        
        Log.d("DocumentosScreen", "Preenchendo template com dados:")
        Log.d("DocumentosScreen", "Nome do paciente: $nomePaciente")
        Log.d("DocumentosScreen", "CPF do paciente: $cpfPaciente")
        Log.d("DocumentosScreen", "Data de nascimento: $dataNascimento")
        Log.d("DocumentosScreen", "Nome do psicólogo: $nomePsicologo")
        Log.d("DocumentosScreen", "CRP do psicólogo: $crpPsicologo")
        Log.d("DocumentosScreen", "Local: $local")
        
        // Preencher template com dados reais
        val templatePreenchido = viewModel.preencherTemplateComDadosPaciente(
            template = template,
            nomePaciente = nomePaciente,
            cpfPaciente = cpfPaciente,
            dataNascimento = dataNascimento,
            nomePsicologo = nomePsicologo,
            crpPsicologo = crpPsicologo,
            local = local
        )
        
        templatePreview = converterHtmlParaTexto(templatePreenchido)
        templateTitle = title
        showTemplatePreview = true
    }

    // Carregar documentos apenas uma vez quando a tela é criada
    LaunchedEffect(Unit) {
        viewModel.carregarDocumentos(patientId)
    }

    // Buscar dados do paciente e psicólogo
    LaunchedEffect(patientId) {
        try {
            Log.d("DocumentosScreen", "Buscando dados do paciente ID: $patientId")
            
            // Buscar dados do paciente
            val patient = viewModel.getPatientById(patientId)
            patientData = patient
            
            Log.d("DocumentosScreen", "Dados do paciente carregados: ${patient?.name} - CPF: ${patient?.cpf}")
            
            // Buscar dados do psicólogo do SharedPreferences
            val (nome, crp) = viewModel.getPsychologistData()
            psychologistData = nome
            psychologistCRP = crp
            psychologistLocation = viewModel.getPsychologistLocation()
            
            Log.d("DocumentosScreen", "Dados do psicólogo carregados: $nome - CRP: $crp - Local: $psychologistLocation")
        } catch (e: Exception) {
            Log.e("DocumentosScreen", "Erro ao buscar dados", e)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Documentos - $patientName",
                        color = onSurfaceColor
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = "Voltar",
                            tint = onSurfaceColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = surfaceColor,
                    titleContentColor = onSurfaceColor,
                    navigationIconContentColor = onSurfaceColor,
                    actionIconContentColor = onSurfaceColor
                )
            )
        },
        containerColor = surfaceColor
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = primaryColor
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Seção de Modelos Prontos
                item {
                    Text(
                        text = "Modelos Prontos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = onSurfaceColor
                    )
                }
                items(getModelosProntos()) { modelo ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                // Mostrar template diretamente
                                val template = getTemplatePreview(modelo.tipo)
                                showTemplatePreview(template, modelo.titulo)
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = surfaceVariantColor
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                modelo.icon,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = modelo.titulo,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = onSurfaceColor
                            )
                        }
                    }
                }
                // Fim da seção de modelos prontos
                
                // Seção de documentos existentes
                if (documentos.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Documentos Existentes",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = onSurfaceColor
                        )
                    }
                    
                    items(documentos) { documento ->
                        DocumentoCard(
                            documento = documento,
                            dateFormatter = dateFormatter,
                            onEdit = { 
                                documentoParaEditar = documento
                                showEditDialog = true
                            },
                            onShare = { 
                                viewModel.compartilharDocumento(documento, context)
                            },
                            onDelete = { 
                                documentoParaExcluir = documento
                                showDeleteConfirmation = true
                            },
                            primaryColor = primaryColor,
                            surfaceColor = surfaceColor,
                            onSurfaceColor = onSurfaceColor,
                            onSurfaceVariantColor = onSurfaceVariantColor
                        )
                    }
                } else {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = surfaceVariantColor
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = onSurfaceVariantColor
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Nenhum documento criado ainda",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = onSurfaceVariantColor
                                )
                                Text(
                                    text = "Clique no botão + para criar seu primeiro documento",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = onSurfaceVariantColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dialog para editar documento
        if (showEditDialog && documentoParaEditar != null) {
            EditarDocumentoDialog(
                documento = documentoParaEditar!!,
                onDismiss = {
                    showEditDialog = false
                    documentoParaEditar = null
                },
                onConfirm = { novoConteudo ->
                    viewModel.editarDocumento(documentoParaEditar!!, novoConteudo)
                    showEditDialog = false
                    documentoParaEditar = null
                },
                primaryColor = primaryColor,
                surfaceColor = surfaceColor,
                onSurfaceColor = onSurfaceColor,
                onSurfaceVariantColor = onSurfaceVariantColor
            )
        }

        // Dialog de confirmação de exclusão
        if (showDeleteConfirmation && documentoParaExcluir != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteConfirmation = false
                    documentoParaExcluir = null
                },
                title = {
                    Text(
                        "Confirmar Exclusão",
                        color = onSurfaceColor
                    )
                },
                text = {
                    Text(
                        "Tem certeza que deseja excluir o documento \"${documentoParaExcluir!!.titulo}\"? Esta ação não pode ser desfeita.",
                        color = onSurfaceColor
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.excluirDocumento(documentoParaExcluir!!)
                            showDeleteConfirmation = false
                            documentoParaExcluir = null
                        }
                    ) {
                        Text("Excluir", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDeleteConfirmation = false
                            documentoParaExcluir = null
                        }
                    ) {
                        Text("Cancelar", color = onSurfaceVariantColor)
                    }
                },
                containerColor = surfaceColor,
                titleContentColor = onSurfaceColor,
                textContentColor = onSurfaceColor
            )
        }
        
        error?.let { errorMessage ->
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.limparErro() }) {
                        Text("OK", color = primaryColor)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(errorMessage, color = onSurfaceColor)
            }
        }
    }

    // Dialog de preview do template
    if (showTemplatePreview) {
        AlertDialog(
            onDismissRequest = { showTemplatePreview = false },
            title = {
                Text(
                    templateTitle,
                    color = onSurfaceColor
                )
            },
            text = {
                Text(
                    text = templatePreview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = onSurfaceColor
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Criar documento diretamente com dados reais
                        val nomePaciente = patientData?.name ?: "Nome do Paciente"
                        val cpfPaciente = patientData?.cpf ?: "CPF não informado"
                        val dataNascimento = patientData?.birthDate?.let { dateFormatter.format(it) } ?: "Data não informada"
                        val nomePsicologo = psychologistData ?: "Profissional"
                        val crpPsicologo = psychologistCRP ?: "CRP"
                        val local = psychologistLocation ?: "Local não informado"
                        
                        viewModel.criarDocumentoComDadosPaciente(
                            patientId = patientId,
                            titulo = templateTitle,
                            tipo = getTipoFromTitle(templateTitle),
                            nomePaciente = nomePaciente,
                            cpfPaciente = cpfPaciente,
                            dataNascimento = dataNascimento,
                            nomePsicologo = nomePsicologo,
                            crpPsicologo = crpPsicologo,
                            local = local
                        )
                        showTemplatePreview = false
                    }
                ) {
                    Text("Criar Documento", color = primaryColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTemplatePreview = false }) {
                    Text("Cancelar", color = onSurfaceVariantColor)
                }
            },
            containerColor = surfaceColor,
            titleContentColor = onSurfaceColor,
            textContentColor = onSurfaceColor
        )
    }
}

@Composable
fun DocumentoCard(
    documento: Documento,
    dateFormatter: SimpleDateFormat,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    primaryColor: Color,
    surfaceColor: Color,
    onSurfaceColor: Color,
    onSurfaceVariantColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = documento.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = onSurfaceColor
                    )
                    Text(
                        text = "Tipo: ${getTipoDocumentoString(documento.tipo)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurfaceVariantColor
                    )
                    Text(
                        text = "Criado em: ${dateFormatter.format(documento.dataCriacao)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurfaceVariantColor
                    )
                }
                
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit, 
                            contentDescription = "Editar",
                            tint = primaryColor
                        )
                    }
                    IconButton(onClick = onShare) {
                        Icon(
                            Icons.Default.Share, 
                            contentDescription = "Compartilhar",
                            tint = primaryColor
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete, 
                            contentDescription = "Excluir",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            // Status das assinaturas
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (documento.assinaturaPaciente.isNotEmpty()) Icons.Default.CheckCircle else Icons.Default.Schedule,
                    contentDescription = null,
                    tint = if (documento.assinaturaPaciente.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Paciente",
                    style = MaterialTheme.typography.bodySmall,
                    color = onSurfaceVariantColor
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Icon(
                    if (documento.assinaturaProfissional.isNotEmpty()) Icons.Default.CheckCircle else Icons.Default.Schedule,
                    contentDescription = null,
                    tint = if (documento.assinaturaProfissional.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Profissional",
                    style = MaterialTheme.typography.bodySmall,
                    color = onSurfaceVariantColor
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CriarDocumentoDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, TipoDocumento) -> Unit,
    tipoSelecionado: TipoDocumento?,
    primaryColor: Color,
    surfaceColor: Color,
    onSurfaceColor: Color,
    onSurfaceVariantColor: Color
) {
    var titulo by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf(tipoSelecionado ?: TipoDocumento.TERMO_CONSENTIMENTO) }
    var showPreview by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Criar Novo Documento",
                color = onSurfaceColor
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título do Documento") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Tipo de Documento:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = onSurfaceColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                TipoDocumento.values().forEach { tipoDoc ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { tipo = tipoDoc }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = tipo == tipoDoc,
                            onClick = { tipo = tipoDoc },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = primaryColor,
                                unselectedColor = onSurfaceVariantColor
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = getTipoDocumentoString(tipoDoc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = onSurfaceColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botão para visualizar template
                Button(
                    onClick = { showPreview = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Icon(
                        Icons.Default.Preview,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Visualizar Template")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (titulo.isNotBlank()) {
                        onConfirm(titulo, tipo)
                    }
                }
            ) {
                Text("Criar", color = primaryColor)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = onSurfaceVariantColor)
            }
        },
        containerColor = surfaceColor,
        titleContentColor = onSurfaceColor,
        textContentColor = onSurfaceColor
    )

    // Dialog de visualização do template
    if (showPreview) {
        val template = getTemplatePreview(tipo)
        AlertDialog(
            onDismissRequest = { showPreview = false },
            title = {
                Text(
                    "Visualização do Template",
                    color = onSurfaceColor
                )
            },
            text = {
                Text(
                    text = template,
                    style = MaterialTheme.typography.bodyMedium,
                    color = onSurfaceColor
                )
            },
            confirmButton = {
                TextButton(onClick = { showPreview = false }) {
                    Text("Fechar", color = primaryColor)
                }
            },
            containerColor = surfaceColor,
            titleContentColor = onSurfaceColor,
            textContentColor = onSurfaceColor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarDocumentoDialog(
    documento: Documento,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    primaryColor: Color,
    surfaceColor: Color,
    onSurfaceColor: Color,
    onSurfaceVariantColor: Color
) {
    var conteudo by remember { mutableStateOf(documento.conteudo) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Editar Documento",
                color = onSurfaceColor
            )
        },
        text = {
            Column {
                Text(
                    text = "Título: ${documento.titulo}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = onSurfaceColor
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Conteúdo:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = onSurfaceColor
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Converter HTML para texto legível
                val conteudoLegivel = converterHtmlParaTexto(conteudo)
                
                OutlinedTextField(
                    value = conteudoLegivel,
                    onValueChange = { 
                        conteudo = converterTextoParaHtml(it)
                    },
                    label = { Text("Conteúdo do documento") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = onSurfaceVariantColor,
                        focusedLabelColor = primaryColor,
                        unfocusedLabelColor = onSurfaceVariantColor
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (conteudo.isNotBlank()) {
                        onConfirm(conteudo)
                    }
                }
            ) {
                Text("Salvar", color = primaryColor)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = onSurfaceVariantColor)
            }
        },
        containerColor = surfaceColor,
        titleContentColor = onSurfaceColor,
        textContentColor = onSurfaceColor
    )
}

data class ModeloDocumento(
    val titulo: String,
    val tipo: TipoDocumento,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

fun getModelosProntos(): List<ModeloDocumento> {
    return listOf(
        ModeloDocumento("Termo de Consentimento", TipoDocumento.TERMO_CONSENTIMENTO, Icons.Default.Description),
        ModeloDocumento("Termo de Confidencialidade", TipoDocumento.TERMO_CONFIDENCIALIDADE, Icons.Default.Security),
        ModeloDocumento("Encaminhamento Psicológico", TipoDocumento.ENCAMINHAMENTO_PSICOLOGICO, Icons.Default.Send),
        ModeloDocumento("Declaração de Comparecimento", TipoDocumento.DECLARACAO_COMPARECIMENTO, Icons.Default.CheckCircle),
        ModeloDocumento("Solicitação de Exames", TipoDocumento.SOLICITACAO_EXAMES, Icons.Default.Assignment),
        ModeloDocumento("Autorização de Imagem", TipoDocumento.AUTORIZACAO_IMAGEM, Icons.Default.PhotoCamera),
        ModeloDocumento("Documento Personalizado", TipoDocumento.DOCUMENTO_PERSONALIZADO, Icons.Default.Edit)
    )
}

fun getTipoDocumentoString(tipo: TipoDocumento): String {
    return when (tipo) {
        TipoDocumento.TERMO_CONSENTIMENTO -> "Termo de Consentimento"
        TipoDocumento.TERMO_CONFIDENCIALIDADE -> "Termo de Confidencialidade"
        TipoDocumento.ENCAMINHAMENTO_PSICOLOGICO -> "Encaminhamento Psicológico"
        TipoDocumento.DECLARACAO_COMPARECIMENTO -> "Declaração de Comparecimento"
        TipoDocumento.SOLICITACAO_EXAMES -> "Solicitação de Exames"
        TipoDocumento.AUTORIZACAO_IMAGEM -> "Autorização de Imagem"
        TipoDocumento.DOCUMENTO_PERSONALIZADO -> "Documento Personalizado"
    }
} 

fun getTemplatePreview(tipo: TipoDocumento): String {
    return when (tipo) {
        TipoDocumento.TERMO_CONSENTIMENTO -> """
            TERMO DE CONSENTIMENTO LIVRE E ESCLARECIDO
            
            Eu, {NOME_PACIENTE}, inscrito(a) no CPF sob o nº {CPF_PACIENTE}, declaro que fui devidamente informado(a) pelo(a) profissional {NOME_PSICOLOGO}, CRP {CRP_PSICOLOGO}, sobre os objetivos, procedimentos, riscos e benefícios envolvidos no processo terapêutico.
            
            Declaro estar ciente de que a participação é voluntária, podendo ser interrompida a qualquer momento, sem prejuízo.
            
            {LOCAL}, {DATA_ATUAL}
            
            Assinatura do Paciente: _________________
            Assinatura do Profissional: _________________
        """.trimIndent()
        
        TipoDocumento.TERMO_CONFIDENCIALIDADE -> """
            TERMO DE CONFIDENCIALIDADE
            
            Eu, {NOME_PACIENTE}, CPF {CPF_PACIENTE}, declaro estar ciente de que todas as informações compartilhadas nas sessões de atendimento psicológico com {NOME_PSICOLOGO}, CRP {CRP_PSICOLOGO}, são confidenciais.
            
            Autorizo o uso de dados apenas para fins técnicos e científicos, resguardando sempre minha identidade e privacidade.
            
            {LOCAL}, {DATA_ATUAL}
            
            Assinatura do Paciente: _________________
            Assinatura do Profissional: _________________
        """.trimIndent()
        
        TipoDocumento.ENCAMINHAMENTO_PSICOLOGICO -> """
            ENCAMINHAMENTO PSICOLÓGICO
            
            Encaminhamos o(a) paciente {NOME_PACIENTE}, CPF {CPF_PACIENTE}, para avaliação/atendimento junto ao(à) profissional/especialidade: ____________________.
            
            Motivo do encaminhamento: _____________________________________________________________
            
            Atenciosamente,
            
            {NOME_PSICOLOGO}
            CRP {CRP_PSICOLOGO}
            {LOCAL}, {DATA_ATUAL}
            
            Assinatura do Profissional: _________________
        """.trimIndent()
        
        TipoDocumento.DECLARACAO_COMPARECIMENTO -> """
            DECLARAÇÃO DE COMPARECIMENTO
            
            Declaro, para os devidos fins, que o(a) paciente {NOME_PACIENTE}, CPF {CPF_PACIENTE}, compareceu à sessão de atendimento psicológico no dia {DATA_ATUAL}, com duração aproximada de 50 minutos.
            
            {NOME_PSICOLOGO}
            CRP {CRP_PSICOLOGO}
            {LOCAL}, {DATA_ATUAL}
            
            Assinatura do Profissional: _________________
        """.trimIndent()
        
        TipoDocumento.SOLICITACAO_EXAMES -> """
            SOLICITAÇÃO DE EXAMES
            
            Solicitamos que o(a) paciente {NOME_PACIENTE}, CPF {CPF_PACIENTE}, realize os seguintes exames/avaliações: ________________________________.
            
            Motivo da solicitação: _____________________________________________________________
            
            {NOME_PSICOLOGO}
            CRP {CRP_PSICOLOGO}
            {LOCAL}, {DATA_ATUAL}
            
            Assinatura do Profissional: _________________
        """.trimIndent()
        
        TipoDocumento.AUTORIZACAO_IMAGEM -> """
            AUTORIZAÇÃO DE USO DE IMAGEM
            
            Eu, {NOME_PACIENTE}, CPF {CPF_PACIENTE}, autorizo o uso de minha imagem, voz ou registros gráficos capturados durante as atividades realizadas com {NOME_PSICOLOGO}, CRP {CRP_PSICOLOGO}, para fins exclusivamente profissionais ou educativos, garantindo o sigilo e anonimato quando necessário.
            
            {LOCAL}, {DATA_ATUAL}
            
            Assinatura do Paciente: _________________
            Assinatura do Profissional: _________________
        """.trimIndent()
        
        TipoDocumento.DOCUMENTO_PERSONALIZADO -> """
            DOCUMENTO PERSONALIZADO
            
            Paciente: {NOME_PACIENTE}
            CPF: {CPF_PACIENTE}
            Data de Nascimento: {DATA_NASCIMENTO}
            Profissional: {NOME_PSICOLOGO}
            CRP: {CRP_PSICOLOGO}
            Data: {DATA_ATUAL}
            
            Conteúdo Personalizado:
            {CONTEUDO_PERSONALIZADO}
            
            Assinatura do Paciente: _________________
            Assinatura do Profissional: _________________
        """.trimIndent()
    }
} 

fun converterHtmlParaTexto(html: String): String {
    return html
        .replace("<h2>", "\n\n")
        .replace("</h2>", "\n")
        .replace("<p>", "")
        .replace("</p>", "\n")
        .replace("<strong>", "")
        .replace("</strong>", "")
        .replace("<br>", "\n")
        .replace("<div style=\"border: 1px solid #ccc; height: 100px; margin: 10px 0;\"></div>", "_________________")
        .replace("&nbsp;", " ")
        .trim()
}

fun converterTextoParaHtml(texto: String): String {
    return texto
        .replace("\n\n", "</p>\n<p>")
        .replace("\n", "<br>")
        .let { "<p>$it</p>" }
        .replace("<p></p>", "")
        .replace("<br>_________________<br>", "<div style=\"border: 1px solid #ccc; height: 100px; margin: 10px 0;\"></div>")
} 

fun getTipoFromTitle(title: String): TipoDocumento {
    return when {
        title.contains("Termo de Consentimento", ignoreCase = true) -> TipoDocumento.TERMO_CONSENTIMENTO
        title.contains("Termo de Confidencialidade", ignoreCase = true) -> TipoDocumento.TERMO_CONFIDENCIALIDADE
        title.contains("Encaminhamento Psicológico", ignoreCase = true) -> TipoDocumento.ENCAMINHAMENTO_PSICOLOGICO
        title.contains("Declaração de Comparecimento", ignoreCase = true) -> TipoDocumento.DECLARACAO_COMPARECIMENTO
        title.contains("Solicitação de Exames", ignoreCase = true) -> TipoDocumento.SOLICITACAO_EXAMES
        title.contains("Autorização de Imagem", ignoreCase = true) -> TipoDocumento.AUTORIZACAO_IMAGEM
        title.contains("Documento Personalizado", ignoreCase = true) -> TipoDocumento.DOCUMENTO_PERSONALIZADO
        else -> TipoDocumento.DOCUMENTO_PERSONALIZADO // Default
    }
} 



