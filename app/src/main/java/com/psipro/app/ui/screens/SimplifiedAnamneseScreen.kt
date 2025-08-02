package com.psipro.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.psipro.app.data.entities.AnamneseGroup
import android.util.Log
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.ButtonDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimplifiedAnamneseScreen(
    navController: NavController,
    patientName: String = "Paciente",
    anamneseGroup: AnamneseGroup = AnamneseGroup.ADULTO,
    onBack: () -> Unit = { navController.popBackStack() }
) {
    var expandedSections by remember { mutableStateOf(setOf<String>()) }
    
    Log.d("SimplifiedAnamneseScreen", "AnamneseGroup: $anamneseGroup")
    Log.d("SimplifiedAnamneseScreen", "Expanded sections: $expandedSections")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Anamnese - $patientName") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Cancelar")
                    }
                    
                    Button(
                        onClick = {
                            Log.d("SimplifiedAnamneseScreen", "Salvando anamnese...")
                            // TODO: Implementar salvamento
                            onBack()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Salvar Anamnese")
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 80.dp) // Espaço para o bottom bar
        ) {
            item {
                Text(
                    text = "Anamnese $patientName - ${getGroupDisplayName(anamneseGroup)}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            when (anamneseGroup) {
                AnamneseGroup.ADULTO -> {
                    item {
                        AnamneseSection(
                            title = "Atendimento",
                            isExpanded = expandedSections.contains("atendimento"),
                            onToggle = { 
                                expandedSections = if (expandedSections.contains("atendimento")) {
                                    expandedSections - "atendimento"
                                } else {
                                    expandedSections + "atendimento"
                                }
                            }
                        ) {
                            AtendimentoSection()
                        }
                    }
                    
                    item {
                        AnamneseSection(
                            title = "Dados pessoais e identificação",
                            isExpanded = expandedSections.contains("dados"),
                            onToggle = { 
                                expandedSections = if (expandedSections.contains("dados")) {
                                    expandedSections - "dados"
                                } else {
                                    expandedSections + "dados"
                                }
                            }
                        ) {
                            DadosPessoaisSection()
                        }
                    }
                    
                    item {
                        AnamneseSection(
                            title = "Profissão e escolaridade",
                            isExpanded = expandedSections.contains("profissao"),
                            onToggle = { 
                                expandedSections = if (expandedSections.contains("profissao")) {
                                    expandedSections - "profissao"
                                } else {
                                    expandedSections + "profissao"
                                }
                            }
                        ) {
                            ProfissaoEscolaridadeSection()
                        }
                    }
                    
                    item {
                        AnamneseSection(
                            title = "Cônjuge e filhos",
                            isExpanded = expandedSections.contains("conjuge"),
                            onToggle = { 
                                expandedSections = if (expandedSections.contains("conjuge")) {
                                    expandedSections - "conjuge"
                                } else {
                                    expandedSections + "conjuge"
                                }
                            }
                        ) {
                            ConjugeFilhosSection()
                        }
                    }
                    
                    item {
                        AnamneseSection(
                            title = "Ciclo social",
                            isExpanded = expandedSections.contains("ciclo_social"),
                            onToggle = { 
                                expandedSections = if (expandedSections.contains("ciclo_social")) {
                                    expandedSections - "ciclo_social"
                                } else {
                                    expandedSections + "ciclo_social"
                                }
                            }
                        ) {
                            CicloSocialSection()
                        }
                    }
                    
                    item {
                        AnamneseSection(
                            title = "Histórico familiar (relacionamento)",
                            isExpanded = expandedSections.contains("familiar"),
                            onToggle = { 
                                expandedSections = if (expandedSections.contains("familiar")) {
                                    expandedSections - "familiar"
                                } else {
                                    expandedSections + "familiar"
                                }
                            }
                        ) {
                            HistoriaFamiliarSection()
                        }
                    }
                    
                    item {
                        AnamneseSection(
                            title = "Histórico escolar",
                            isExpanded = expandedSections.contains("escolar"),
                            onToggle = { 
                                expandedSections = if (expandedSections.contains("escolar")) {
                                    expandedSections - "escolar"
                                } else {
                                    expandedSections + "escolar"
                                }
                            }
                        ) {
                            HistoriaEscolarAdultaSection()
                        }
                    }
                    
                    item {
                        AnamneseSection(
                            title = "História social e desenvolvimento",
                            isExpanded = expandedSections.contains("social"),
                            onToggle = { 
                                expandedSections = if (expandedSections.contains("social")) {
                                    expandedSections - "social"
                                } else {
                                    expandedSections + "social"
                                }
                            }
                        ) {
                            HistoriaSocialSection()
                        }
                    }
                    
                    item {
                        AnamneseSection(
                            title = "História médica / psiquiátrica",
                            isExpanded = expandedSections.contains("medica"),
                            onToggle = { 
                                expandedSections = if (expandedSections.contains("medica")) {
                                    expandedSections - "medica"
                                } else {
                                    expandedSections + "medica"
                                }
                            }
                        ) {
                            HistoriaMedicaSection()
                        }
                    }
                    
                    item {
                        AnamneseSection(
                            title = "História psicológica e psiquiátrica",
                            isExpanded = expandedSections.contains("psicologica"),
                            onToggle = { 
                                expandedSections = if (expandedSections.contains("psicologica")) {
                                    expandedSections - "psicologica"
                                } else {
                                    expandedSections + "psicologica"
                                }
                            }
                        ) {
                            HistoriaPsicologicaSection()
                        }
                    }
                    
                    item {
                        AnamneseSection(
                            title = "História pessoal",
                            isExpanded = expandedSections.contains("pessoal"),
                            onToggle = { 
                                expandedSections = if (expandedSections.contains("pessoal")) {
                                    expandedSections - "pessoal"
                                } else {
                                    expandedSections + "pessoal"
                                }
                            }
                        ) {
                            HistoriaPessoalSection()
                        }
                    }
                    
                    item {
                        AnamneseSection(
                            title = "Sono e rotina",
                            isExpanded = expandedSections.contains("sono"),
                            onToggle = { 
                                expandedSections = if (expandedSections.contains("sono")) {
                                    expandedSections - "sono"
                                } else {
                                    expandedSections + "sono"
                                }
                            }
                        ) {
                            SonoRotinaSection()
                        }
                    }
                    
                    item {
                        AnamneseSection(
                            title = "Queixa principal e história da doença atual",
                            isExpanded = expandedSections.contains("queixa"),
                            onToggle = { 
                                expandedSections = if (expandedSections.contains("queixa")) {
                                    expandedSections - "queixa"
                                } else {
                                    expandedSections + "queixa"
                                }
                            }
                        ) {
                            QueixaPrincipalSection()
                        }
                    }
                    
                    item {
                        AnamneseSection(
                            title = "Valores, habilidades, pontos fortes, aspirações",
                            isExpanded = expandedSections.contains("valores"),
                            onToggle = { 
                                expandedSections = if (expandedSections.contains("valores")) {
                                    expandedSections - "valores"
                                } else {
                                    expandedSections + "valores"
                                }
                            }
                        ) {
                            ValoresHabilidadesSection()
                        }
                    }
                }
                
                AnamneseGroup.CRIANCAS -> {
                    item {
                        AnamneseSection(
                            title = "Dados da criança",
                            isExpanded = expandedSections.contains("dados_crianca"),
                            onToggle = { 
                                expandedSections = if (expandedSections.contains("dados_crianca")) {
                                    expandedSections - "dados_crianca"
                                } else {
                                    expandedSections + "dados_crianca"
                                }
                            }
                        ) {
                            DadosCriancaSection()
                        }
                    }
                    
                    item {
                        AnamneseSection(
                            title = "Dados dos pais/responsáveis",
                            isExpanded = expandedSections.contains("dados_pais"),
                            onToggle = { 
                                expandedSections = if (expandedSections.contains("dados_pais")) {
                                    expandedSections - "dados_pais"
                                } else {
                                    expandedSections + "dados_pais"
                                }
                            }
                        ) {
                            DadosPaisSection()
                        }
                    }
                    
                    item {
                        AnamneseSection(
                            title = "Desenvolvimento da criança",
                            isExpanded = expandedSections.contains("desenvolvimento"),
                            onToggle = { 
                                expandedSections = if (expandedSections.contains("desenvolvimento")) {
                                    expandedSections - "desenvolvimento"
                                } else {
                                    expandedSections + "desenvolvimento"
                                }
                            }
                        ) {
                            DesenvolvimentoCriancaSection()
                        }
                    }
                    
                    item {
                        AnamneseSection(
                            title = "Comportamento e queixas",
                            isExpanded = expandedSections.contains("comportamento"),
                            onToggle = { 
                                expandedSections = if (expandedSections.contains("comportamento")) {
                                    expandedSections - "comportamento"
                                } else {
                                    expandedSections + "comportamento"
                                }
                            }
                        ) {
                            ComportamentoCriancaSection()
                        }
                    }
                    
                    item {
                        AnamneseSection(
                            title = "História escolar",
                            isExpanded = expandedSections.contains("escolar"),
                            onToggle = { 
                                expandedSections = if (expandedSections.contains("escolar")) {
                                    expandedSections - "escolar"
                                } else {
                                    expandedSections + "escolar"
                                }
                            }
                        ) {
                            HistoriaEscolarCriancaSection()
                        }
                    }
                    
                    item {
                        AnamneseSection(
                            title = "História médica",
                            isExpanded = expandedSections.contains("medica_crianca"),
                            onToggle = { 
                                expandedSections = if (expandedSections.contains("medica_crianca")) {
                                    expandedSections - "medica_crianca"
                                } else {
                                    expandedSections + "medica_crianca"
                                }
                            }
                        ) {
                            HistoriaMedicaCriancaSection()
                        }
                    }
                }
                
                AnamneseGroup.ADOLESCENTES -> {
                    item {
                        AnamneseSection(
                            title = "Dados do adolescente",
                            isExpanded = expandedSections.contains("dados_adolescente"),
                            onToggle = { 
                                expandedSections = if (expandedSections.contains("dados_adolescente")) {
                                    expandedSections - "dados_adolescente"
                                } else {
                                    expandedSections + "dados_adolescente"
                                }
                            }
                        ) {
                            DadosAdolescenteSection()
                        }
                    }
                    
                    item {
                        AnamneseSection(
                            title = "Dados dos pais",
                            isExpanded = expandedSections.contains("dados_pais_adolescente"),
                            onToggle = { 
                                expandedSections = if (expandedSections.contains("dados_pais_adolescente")) {
                                    expandedSections - "dados_pais_adolescente"
                                } else {
                                    expandedSections + "dados_pais_adolescente"
                                }
                            }
                        ) {
                            DadosPaisAdolescenteSection()
                        }
                    }
                    
                    item {
                        AnamneseSection(
                            title = "Queixas e problemas",
                            isExpanded = expandedSections.contains("queixas_adolescente"),
                            onToggle = { 
                                expandedSections = if (expandedSections.contains("queixas_adolescente")) {
                                    expandedSections - "queixas_adolescente"
                                } else {
                                    expandedSections + "queixas_adolescente"
                                }
                            }
                        ) {
                            QueixasAdolescenteSection()
                        }
                    }
                    
                    item {
                        AnamneseSection(
                            title = "Desenvolvimento psicossocial",
                            isExpanded = expandedSections.contains("desenvolvimento_adolescente"),
                            onToggle = { 
                                expandedSections = if (expandedSections.contains("desenvolvimento_adolescente")) {
                                    expandedSections - "desenvolvimento_adolescente"
                                } else {
                                    expandedSections + "desenvolvimento_adolescente"
                                }
                            }
                        ) {
                            DesenvolvimentoAdolescenteSection()
                        }
                    }
                    
                    item {
                        AnamneseSection(
                            title = "História escolar e social",
                            isExpanded = expandedSections.contains("escolar_social_adolescente"),
                            onToggle = { 
                                expandedSections = if (expandedSections.contains("escolar_social_adolescente")) {
                                    expandedSections - "escolar_social_adolescente"
                                } else {
                                    expandedSections + "escolar_social_adolescente"
                                }
                            }
                        ) {
                            HistoriaEscolarSocialAdolescenteSection()
                        }
                    }
                }
                
                AnamneseGroup.IDOSOS -> {
                    item {
                        AnamneseSection(
                            title = "Dados do idoso",
                            isExpanded = expandedSections.contains("dados_idoso"),
                            onToggle = { 
                                expandedSections = if (expandedSections.contains("dados_idoso")) {
                                    expandedSections - "dados_idoso"
                                } else {
                                    expandedSections + "dados_idoso"
                                }
                            }
                        ) {
                            DadosIdosoSection()
                        }
                    }
                    
                    item {
                        AnamneseSection(
                            title = "Queixas principais",
                            isExpanded = expandedSections.contains("queixas_idoso"),
                            onToggle = { 
                                expandedSections = if (expandedSections.contains("queixas_idoso")) {
                                    expandedSections - "queixas_idoso"
                                } else {
                                    expandedSections + "queixas_idoso"
                                }
                            }
                        ) {
                            QueixasIdosoSection()
                        }
                    }
                    
                    item {
                        AnamneseSection(
                            title = "História médica geriátrica",
                            isExpanded = expandedSections.contains("medica_idoso"),
                            onToggle = { 
                                expandedSections = if (expandedSections.contains("medica_idoso")) {
                                    expandedSections - "medica_idoso"
                                } else {
                                    expandedSections + "medica_idoso"
                                }
                            }
                        ) {
                            HistoriaMedicaIdosoSection()
                        }
                    }
                    
                    item {
                        AnamneseSection(
                            title = "Funcionalidade e independência",
                            isExpanded = expandedSections.contains("funcionalidade"),
                            onToggle = { 
                                expandedSections = if (expandedSections.contains("funcionalidade")) {
                                    expandedSections - "funcionalidade"
                                } else {
                                    expandedSections + "funcionalidade"
                                }
                            }
                        ) {
                            FuncionalidadeIdosoSection()
                        }
                    }
                    
                    item {
                        AnamneseSection(
                            title = "Suporte social e familiar",
                            isExpanded = expandedSections.contains("suporte_idoso"),
                            onToggle = { 
                                expandedSections = if (expandedSections.contains("suporte_idoso")) {
                                    expandedSections - "suporte_idoso"
                                } else {
                                    expandedSections + "suporte_idoso"
                                }
                            }
                        ) {
                            SuporteSocialIdosoSection()
                        }
                    }
                    
                    item {
                        AnamneseSection(
                            title = "Aspectos cognitivos e emocionais",
                            isExpanded = expandedSections.contains("cognitivo_idoso"),
                            onToggle = { 
                                expandedSections = if (expandedSections.contains("cognitivo_idoso")) {
                                    expandedSections - "cognitivo_idoso"
                                } else {
                                    expandedSections + "cognitivo_idoso"
                                }
                            }
                        ) {
                            AspectosCognitivosIdosoSection()
                        }
                    }
                }
            }
        }
    }
}

// Função auxiliar para obter o nome de exibição do grupo
private fun getGroupDisplayName(group: AnamneseGroup): String {
    return when (group) {
        AnamneseGroup.ADULTO -> "Adulto"
        AnamneseGroup.CRIANCAS -> "Crianças"
        AnamneseGroup.ADOLESCENTES -> "Adolescentes"
        AnamneseGroup.IDOSOS -> "Idosos"
    }
}

@Composable
fun AnamneseSection(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        Log.d("AnamneseSection", "Clique detectado em: $title")
                        onToggle() 
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                
                Icon(
                    imageVector = if (isExpanded) {
                        Icons.Default.KeyboardArrowUp
                    } else {
                        Icons.Default.KeyboardArrowDown
                    },
                    contentDescription = if (isExpanded) "Recolher" else "Expandir",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            
            if (isExpanded) {
                Log.d("AnamneseSection", "Renderizando conteúdo para: $title")
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    thickness = 1.dp
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
fun themedOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "Digite aqui (opcional)",
    modifier: Modifier = Modifier.fillMaxWidth(),
    minLines: Int = 1,
    isOptional: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { 
            Text(
                text = if (isOptional) "$label (opcional)" else label,
                style = MaterialTheme.typography.bodyMedium
            ) 
        },
        placeholder = { Text(placeholder) },
        modifier = modifier,
        minLines = minLines,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

// ===== SEÇÕES PARA ADULTOS =====
@Composable
fun AtendimentoSection() {
    var motivosAtendimento by remember { mutableStateOf("") }
    var objetivosTerapia by remember { mutableStateOf("") }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Descreva os motivos que o levaram a buscar atendimento psicológico:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        themedOutlinedTextField(
            value = motivosAtendimento,
            onValueChange = { motivosAtendimento = it },
            label = "Motivos do atendimento",
            placeholder = "Descreva os motivos que o levaram a buscar atendimento psicológico (opcional)"
        )
        
        Text(
            text = "Descreva os objetivos que você gostaria de alcançar com a terapia:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        themedOutlinedTextField(
            value = objetivosTerapia,
            onValueChange = { objetivosTerapia = it },
            label = "Objetivos da terapia",
            placeholder = "Descreva os objetivos que você gostaria de alcançar (opcional)"
        )
    }
}

@Composable
fun DadosPessoaisSection() {
    var endereco by remember { mutableStateOf("") }
    var bairro by remember { mutableStateOf("") }
    var cep by remember { mutableStateOf("") }
    var cidade by remember { mutableStateOf("") }
    var contatosEmergencia by remember { mutableStateOf("") }
    var religiao by remember { mutableStateOf("") }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        themedOutlinedTextField(
            value = endereco,
            onValueChange = { endereco = it },
            label = "Endereço com número"
        )
        
        themedOutlinedTextField(
            value = bairro,
            onValueChange = { bairro = it },
            label = "Bairro"
        )
        
        themedOutlinedTextField(
            value = cep,
            onValueChange = { cep = it },
            label = "CEP"
        )
        
        themedOutlinedTextField(
            value = cidade,
            onValueChange = { cidade = it },
            label = "Cidade"
        )
        
        themedOutlinedTextField(
            value = contatosEmergencia,
            onValueChange = { contatosEmergencia = it },
            label = "Contatos de emergência",
            placeholder = "Nome, telefone e relação (opcional)"
        )
        
        themedOutlinedTextField(
            value = religiao,
            onValueChange = { religiao = it },
            label = "Religião"
        )
    }
}

@Composable
fun ProfissaoEscolaridadeSection() {
    var profissao by remember { mutableStateOf("") }
    var horarioTrabalho by remember { mutableStateOf("") }
    var formacaoSelecionada by remember { mutableStateOf("Ensino Fundamental Completo") }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        themedOutlinedTextField(
            value = profissao,
            onValueChange = { profissao = it },
            label = "Profissão atual:"
        )
        
        themedOutlinedTextField(
            value = horarioTrabalho,
            onValueChange = { horarioTrabalho = it },
            label = "Horário de trabalho- (exemplo 08 às 18:00 horas):"
        )
        
        Text(
            text = "Qual sua formação? (ou grau de formação):",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        val formacoes = listOf(
            "Ensino Fundamental Incompleto",
            "Ensino Fundamental Completo",
            "Ensino Médio Incompleto",
            "Ensino Médio completo",
            "Ensino Superior Incompleto",
            "Ensino Superior Completo",
            "Outros"
        )
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            formacoes.forEach { formacao ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = formacaoSelecionada == formacao,
                        onClick = { formacaoSelecionada = formacao },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary,
                            unselectedColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    Text(
                        text = formacao,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ConjugeFilhosSection() {
    var estadoCivilSelecionado by remember { mutableStateOf("") }
    var nomeConjuge by remember { mutableStateOf("") }
    var temFilhos by remember { mutableStateOf("") }
    var relacionamentoConjuge by remember { mutableStateOf("") }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Estado civil:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        val estadosCivis = listOf(
            "Casado (a)",
            "Solteiro (a)",
            "União Estável",
            "Divorciado (a)",
            "Viúvo (a)"
        )
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            estadosCivis.forEach { estado ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = estadoCivilSelecionado == estado,
                        onCheckedChange = { checked ->
                            estadoCivilSelecionado = if (checked) estado else ""
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            uncheckedColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    Text(
                        text = estado,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
        
        Text(
            text = "Nome, idade do cônjuge e quanto tempo juntos? (se tiver):",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = nomeConjuge,
            onValueChange = { nomeConjuge = it },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 1
        )
        
        Text(
            text = "Tem filhos? (nome e idade):",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = temFilhos,
            onValueChange = { temFilhos = it },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 1
        )
        
        Text(
            text = "Descreva seu relacionamento com seu cônjuge (se houver), incluindo o máximo de detalhes possível.:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = relacionamentoConjuge,
            onValueChange = { relacionamentoConjuge = it },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

@Composable
fun CicloSocialSection() {
    var relacionamentosInterpessoais by remember { mutableStateOf("") }
    var lugaresAtividades by remember { mutableStateOf("") }
    var relacionamentosSignificativos by remember { mutableStateOf("") }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Como você descreveria seus relacionamentos interpessoais?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = relacionamentosInterpessoais,
            onValueChange = { relacionamentosInterpessoais = it },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Quais lugares você costuma frequentar? quais atividades sociais ou voluntárias você participa?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = lugaresAtividades,
            onValueChange = { lugaresAtividades = it },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Há algum relacionamento significativo que influenciou sua vida de maneira notável?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = relacionamentosSignificativos,
            onValueChange = { relacionamentosSignificativos = it },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

@Composable
fun HistoriaEscolarAdultaSection() {
    var experienciaEscolar by remember { mutableStateOf("") }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Você gostava da escola? quais foram seus principais sucessos ou dificuldades?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = experienciaEscolar,
            onValueChange = { experienciaEscolar = it },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

@Composable
fun QueixaPrincipalSection() {
    var queixasPrincipais by remember { mutableStateOf("") }
    var quandoComecou by remember { mutableStateOf("") }
    var evolucaoProblema by remember { mutableStateOf("") }
    var situacoesMelhoraPiora by remember { mutableStateOf("") }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Quais suas queixas considera principais?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = queixasPrincipais,
            onValueChange = { queixasPrincipais = it },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Desde quando essa queixa tem afetado a sua vida? por que acha que ela ocorre?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = quandoComecou,
            onValueChange = { quandoComecou = it },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "O problema se mantem igual, piorou ou melhorou nos últimos tempos?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = evolucaoProblema,
            onValueChange = { evolucaoProblema = it },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Há alguma situação que você pode associar à melhora ou piora do seu problema?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = situacoesMelhoraPiora,
            onValueChange = { situacoesMelhoraPiora = it },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

@Composable
fun HistoriaMedicaSection() {
    var ultimoCheckup by remember { mutableStateOf("") }
    var examesVitaminas by remember { mutableStateOf("") }
    var doencasAtuais by remember { mutableStateOf("") }
    var tratamentosPsicologicos by remember { mutableStateOf("") }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Qual foi a última vez que você fez um check-up?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = ultimoCheckup,
            onValueChange = { ultimoCheckup = it },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Qual foi a última vez que você fez exames de vitaminas? quanto está sua vitaminas b12 e d:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = examesVitaminas,
            onValueChange = { examesVitaminas = it },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Atualmente, você está tratando alguma doença ou enfermidade?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = doencasAtuais,
            onValueChange = { doencasAtuais = it },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Você já recebeu tratamento psiquiátrico ou psicológico? em caso positivo, especifique.:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = tratamentosPsicologicos,
            onValueChange = { tratamentosPsicologicos = it },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

@Composable
fun HistoriaFamiliarSection() {
    var relacionamentoPai by remember { mutableStateOf("") }
    var relacionamentoMae by remember { mutableStateOf("") }
    var relacionamentoIrmaos by remember { mutableStateOf("") }
    var outrosRelacionamentos by remember { mutableStateOf("") }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Descreva o relacionamento atual com seu pai e como foi durante a infância e adolescência.:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = relacionamentoPai,
            onValueChange = { relacionamentoPai = it },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Descreva o relacionamento atual com sua mãe e como foi durante a infância e adolescência.:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = relacionamentoMae,
            onValueChange = { relacionamentoMae = it },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Irmãos (relacionamento):",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = relacionamentoIrmaos,
            onValueChange = { relacionamentoIrmaos = it },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Outros (relevantes):",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = outrosRelacionamentos,
            onValueChange = { outrosRelacionamentos = it },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

@Composable
fun HistoriaSocialSection() {
    var relacionamentosInterpessoais by remember { mutableStateOf("") }
    var lugaresAtividades by remember { mutableStateOf("") }
    var relacionamentosSignificativos by remember { mutableStateOf("") }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Como você descreveria seus relacionamentos interpessoais?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = relacionamentosInterpessoais,
            onValueChange = { relacionamentosInterpessoais = it },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Quais lugares você costuma frequentar? quais atividades sociais ou voluntárias você participa?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = lugaresAtividades,
            onValueChange = { lugaresAtividades = it },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Há algum relacionamento significativo que influenciou sua vida de maneira notável?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = relacionamentosSignificativos,
            onValueChange = { relacionamentosSignificativos = it },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

@Composable
fun HistoriaPsicologicaSection() {
    var experienciaEscolar by remember { mutableStateOf("") }
    var infancia by remember { mutableStateOf("") }
    var vicios by remember { mutableStateOf("") }
    var hobbies by remember { mutableStateOf("") }
    var trabalho by remember { mutableStateOf("") }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Você gostava da escola? quais foram seus principais sucessos ou dificuldades?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = experienciaEscolar,
            onValueChange = { experienciaEscolar = it },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Infância:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = infancia,
            onValueChange = { infancia = it },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Vícios:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = vicios,
            onValueChange = { vicios = it },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Hobbies:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = hobbies,
            onValueChange = { hobbies = it },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Trabalho:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = trabalho,
            onValueChange = { trabalho = it },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

@Composable
fun ValoresHabilidadesSection() {
    var pessoasImportantes by remember { mutableStateOf("") }
    var tresCoisas by remember { mutableStateOf("") }
    var maioresSonhos by remember { mutableStateOf("") }
    var pontosFortes by remember { mutableStateOf("") }
    var gostos by remember { mutableStateOf("") }
    var habilidadesSelecionadas by remember { mutableStateOf(mutableSetOf<String>()) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Quem são as pessoas mais importantes para você?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = pessoasImportantes,
            onValueChange = { pessoasImportantes = it },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Escreva três coisas que você gostaria de fazer antes de morrer.:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = tresCoisas,
            onValueChange = { tresCoisas = it },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Quais são os seus maiores sonhos/aspirações?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = maioresSonhos,
            onValueChange = { maioresSonhos = it },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Quais são seus principais pontos fortes e recursos pessoais?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = pontosFortes,
            onValueChange = { pontosFortes = it },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Sobre suas habilidades:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        val habilidades = listOf(
            "Tocar Instrumento",
            "Praticar Esporte", 
            "Escrever",
            "Comunicação",
            "Ensino",
            "Criatividade",
            "Leitura",
            "Organização",
            "Tomar iniciativa",
            "Reflexão",
            "Liderança",
            "Resiliência",
            "Aconselhamento",
            "Ajudar pessoas necessitadas"
        )
        
        LazyColumn(
            modifier = Modifier.height(300.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(habilidades) { habilidade ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = habilidadesSelecionadas.contains(habilidade),
                        onCheckedChange = { checked ->
                            if (checked) {
                                habilidadesSelecionadas.add(habilidade)
                            } else {
                                habilidadesSelecionadas.remove(habilidade)
                            }
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            uncheckedColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    Text(
                        text = habilidade,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Quais são seus gostos de músicas, filmes, artistas, livros, estilos?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = gostos,
            onValueChange = { gostos = it },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

// ===== SEÇÕES PARA CRIANÇAS =====
@Composable
fun DadosCriancaSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Nome completo da criança"
        )
        
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Data de nascimento"
        )
        
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Idade"
        )
        
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Escola"
        )
        
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Série/Ano escolar"
        )
    }
}

@Composable
fun DadosPaisSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Nome da mãe"
        )
        
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Nome do pai"
        )
        
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Telefone dos pais"
        )
        
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Endereço"
        )
        
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Profissão dos pais"
        )
    }
}

@Composable
fun DesenvolvimentoCriancaSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Desenvolvimento motor (quando começou a sentar, engatinhar, andar):",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = "",
            onValueChange = { },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Desenvolvimento da linguagem (primeiras palavras, frases):",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = "",
            onValueChange = { },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Desenvolvimento social (interação com outras crianças):",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = "",
            onValueChange = { },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

@Composable
fun ComportamentoCriancaSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Descreva o comportamento da criança:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = "",
            onValueChange = { },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Principais queixas dos pais:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = "",
            onValueChange = { },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Como a criança se comporta em diferentes situações (casa, escola, social):",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = "",
            onValueChange = { },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

@Composable
fun HistoriaEscolarCriancaSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Histórico escolar"
        )
        
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Dificuldades de aprendizagem"
        )
        
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Relacionamento com professores"
        )
        
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Relacionamento com colegas"
        )
    }
}

@Composable
fun HistoriaMedicaCriancaSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Histórico médico da criança"
        )
        
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Medicamentos em uso"
        )
        
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Alergias"
        )
        
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Histórico de internações"
        )
    }
}

// ===== SEÇÕES PARA ADOLESCENTES =====
@Composable
fun DadosAdolescenteSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Nome completo do adolescente"
        )
        
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Data de nascimento"
        )
        
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Idade"
        )
        
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Escola"
        )
        
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Série/Ano escolar"
        )
    }
}

@Composable
fun DadosPaisAdolescenteSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Nome da mãe"
        )
        
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Nome do pai"
        )
        
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Telefone dos pais"
        )
        
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Endereço"
        )
        
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Profissão dos pais"
        )
    }
}

@Composable
fun QueixasAdolescenteSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Principais queixas do adolescente:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = "",
            onValueChange = { },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Principais queixas dos pais:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = "",
            onValueChange = { },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Como o adolescente vê o problema:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = "",
            onValueChange = { },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

@Composable
fun DesenvolvimentoAdolescenteSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Desenvolvimento psicossocial:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = "",
            onValueChange = { },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Identidade e autoestima:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = "",
            onValueChange = { },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Relacionamentos e amizades:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = "",
            onValueChange = { },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

@Composable
fun HistoriaEscolarSocialAdolescenteSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Histórico escolar"
        )
        
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Dificuldades de aprendizagem"
        )
        
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Relacionamento com professores"
        )
        
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Relacionamento com colegas"
        )
        
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Atividades extracurriculares"
        )
    }
}

// ===== SEÇÕES PARA IDOSOS =====
@Composable
fun DadosIdosoSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Nome completo"
        )
        
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Data de nascimento"
        )
        
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Idade"
        )
        
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Estado civil"
        )
        
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Profissão atual/aposentado"
        )
        
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Endereço"
        )
    }
}

@Composable
fun QueixasIdosoSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Principais queixas:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = "",
            onValueChange = { },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Como essas queixas afetam a vida diária:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = "",
            onValueChange = { },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Mudanças recentes na vida:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = "",
            onValueChange = { },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

@Composable
fun HistoriaMedicaIdosoSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Doenças crônicas"
        )
        
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Medicamentos em uso"
        )
        
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Cirurgias realizadas"
        )
        
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Internações recentes"
        )
        
        themedOutlinedTextField(
            value = "",
            onValueChange = { },
            label = "Alergias"
        )
    }
}

@Composable
fun FuncionalidadeIdosoSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Capacidade de realizar atividades da vida diária:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = "",
            onValueChange = { },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Mobilidade e locomoção:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = "",
            onValueChange = { },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Capacidade de autocuidado:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = "",
            onValueChange = { },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

@Composable
fun SuporteSocialIdosoSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Rede de apoio familiar:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = "",
            onValueChange = { },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Rede de amigos e vizinhos:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = "",
            onValueChange = { },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Serviços de apoio utilizados:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = "",
            onValueChange = { },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

@Composable
fun AspectosCognitivosIdosoSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Funções cognitivas (memória, atenção, orientação):",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = "",
            onValueChange = { },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Estado emocional:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = "",
            onValueChange = { },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Comportamento e personalidade:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = "",
            onValueChange = { },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
} 

@Composable
fun HistoriaPessoalSection() {
    var infancia by remember { mutableStateOf("") }
    var vicios by remember { mutableStateOf("") }
    var hobbies by remember { mutableStateOf("") }
    var trabalho by remember { mutableStateOf("") }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Infância:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = infancia,
            onValueChange = { infancia = it },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Vícios:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = vicios,
            onValueChange = { vicios = it },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Hobbies:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = hobbies,
            onValueChange = { hobbies = it },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Trabalho:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = trabalho,
            onValueChange = { trabalho = it },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

@Composable
fun SonoRotinaSection() {
    var horarioSono by remember { mutableStateOf("") }
    var tempoAdormecer by remember { mutableStateOf("") }
    var sensacaoAcordar by remember { mutableStateOf("") }
    var acordarDuranteSono by remember { mutableStateOf("") }
    var rotinaDiaria by remember { mutableStateOf("") }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Que horas você costuma ir dormir e acordar?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = horarioSono,
            onValueChange = { horarioSono = it },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Quanto tempo demora para iniciar o sono depois que vai dormir?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = tempoAdormecer,
            onValueChange = { tempoAdormecer = it },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Como se sente quando acorda? (disposto, cansado, etc.):",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = sensacaoAcordar,
            onValueChange = { sensacaoAcordar = it },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Costuma acordar durante o sono?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = acordarDuranteSono,
            onValueChange = { acordarDuranteSono = it },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Descreva sua rotina desde a hora de acordar até a hora de dormir:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = rotinaDiaria,
            onValueChange = { rotinaDiaria = it },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
} 



