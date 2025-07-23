@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.psipro

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.psipro.ui.compose.PsiproTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.appcompat.app.AppCompatDelegate
import android.content.Context
import com.example.psipro.data.entities.AnamneseGroup

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class AnamneseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val patientId = intent.getLongExtra("PATIENT_ID", -1)
        val patientName = intent.getStringExtra("PATIENT_NAME") ?: ""
        val anamneseGroup = intent.getStringExtra("ANAMNESE_GROUP")?.let { 
            AnamneseGroup.valueOf(it) 
        } ?: AnamneseGroup.ADULTO
        
        Log.d("AnamneseActivity", "onCreate - Patient ID: $patientId, Name: $patientName, Group: $anamneseGroup")
        
        if (patientId == -1L) {
            Log.e("AnamneseActivity", "Invalid patient ID")
            finish()
            return
        }
        
        setContent {
            PsiproTheme(useDarkTheme = isDarkModeActive(this)) {
                AnamneseScreen(
                    patientName = patientName,
                    patientId = patientId,
                    anamneseGroup = anamneseGroup,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnamneseScreen(
    patientName: String,
    patientId: Long,
    anamneseGroup: AnamneseGroup,
    onBack: () -> Unit
) {
    var expandedSections by remember { mutableStateOf(setOf<String>()) }
    var formData by remember { mutableStateOf(mutableMapOf<String, String>()) }
    
    // Define seções baseadas no tipo de anamnese
    val secoes = when (anamneseGroup) {
        AnamneseGroup.ADULTO -> listOf(
            "Atendimento",
            "Identificação adulto", 
            "Profissão e escolaridade",
            "Cônjuge e filhos",
            "Ciclo social",
            "Histórico familiar (relacionamento)",
            "Histórico escolar",
            "Histórico pessoal",
            "Histórico médico / psiquiátrico",
            "Queixas principais"
        )
        AnamneseGroup.CRIANCAS -> listOf(
            "Identificação da criança",
            "Dados familiares",
            "Desenvolvimento da saúde",
            "Histórico escolar",
            "Histórico médico",
            "Comportamento e socialização",
            "Queixas principais"
        )
        AnamneseGroup.ADOLESCENTES -> listOf(
            "Identificação do adolescente",
            "Dados familiares",
            "Desenvolvimento psicossocial",
            "Histórico escolar",
            "Histórico médico",
            "Comportamento e relacionamentos",
            "Queixas principais"
        )
        AnamneseGroup.IDOSOS -> listOf(
            "Identificação idoso",
            "Preocupações físicas",
            "Preocupações sensoriais",
            "Preocupações intelectuais",
            "Humor/comportamento/personalidade",
            "Histórico médico",
            "História da família",
            "Histórico profissional",
            "Lazer",
            "Hipótese diagnóstica"
        )
    }
    
    val grupoText = when (anamneseGroup) {
        AnamneseGroup.ADULTO -> "Adulto"
        AnamneseGroup.CRIANCAS -> "Crianças"
        AnamneseGroup.ADOLESCENTES -> "Adolescentes"
        AnamneseGroup.IDOSOS -> "Idosos"
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // TopBar
        TopAppBar(
            title = { Text("Anamnese - $patientName") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Voltar",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )
        
        // Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Text(
                    text = "Grupo de anamnese: $grupoText",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            items(secoes) { secao ->
                AnamneseSection(
                    title = secao,
                    isExpanded = expandedSections.contains(secao),
                    formData = formData,
                    onFormDataChange = { newData ->
                        formData = formData.toMutableMap().apply { putAll(newData) }
                    },
                    onToggle = {
                        if (expandedSections.contains(secao)) {
                            expandedSections = expandedSections - secao
                        } else {
                            expandedSections = expandedSections + secao
                        }
                    },
                    onSave = {
                        // TODO: Implementar salvamento no banco de dados
                        Log.d("AnamneseActivity", "Salvando dados da seção: $secao")
                    },
                    onCancel = {
                        expandedSections = expandedSections - secao
                    },
                    anamneseGroup = anamneseGroup
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun AnamneseSection(
    title: String,
    isExpanded: Boolean,
    formData: Map<String, String>,
    onFormDataChange: (Map<String, String>) -> Unit,
    onToggle: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    anamneseGroup: AnamneseGroup
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Recolher" else "Expandir",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Renderiza conteúdo baseado no grupo de anamnese
                    when (anamneseGroup) {
                        AnamneseGroup.ADULTO -> renderAdultoContent(title, formData, onFormDataChange)
                        AnamneseGroup.CRIANCAS -> renderCriancasContent(title, formData, onFormDataChange)
                        AnamneseGroup.ADOLESCENTES -> renderAdolescentesContent(title, formData, onFormDataChange)
                        AnamneseGroup.IDOSOS -> renderIdososContent(title, formData, onFormDataChange)
                    }
                    
                    // Botões de ação
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onCancel,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar")
                        }
                        
                        Button(
                            onClick = onSave,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Salvar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun renderAdultoContent(title: String, formData: Map<String, String>, onFormDataChange: (Map<String, String>) -> Unit) {
    when (title) {
        "Atendimento" -> AtendimentoContent(formData, onFormDataChange)
        "Identificação adulto" -> IdentificacaoAdultoContent(formData, onFormDataChange)
        "Profissão e escolaridade" -> ProfissaoEscolaridadeContent(formData, onFormDataChange)
        "Cônjuge e filhos" -> ConjugeFilhosContent(formData, onFormDataChange)
        "Ciclo social" -> CicloSocialContent(formData, onFormDataChange)
        "Histórico familiar (relacionamento)" -> HistoricoFamiliarContent(formData, onFormDataChange)
        "Histórico escolar" -> HistoricoEscolarContent(formData, onFormDataChange)
        "Histórico pessoal" -> HistoricoPessoalContent(formData, onFormDataChange)
        "Histórico médico / psiquiátrico" -> HistoricoMedicoContent(formData, onFormDataChange)
        "Queixas principais" -> QueixasPrincipaisContent(formData, onFormDataChange)
    }
}

@Composable
fun renderCriancasContent(title: String, formData: Map<String, String>, onFormDataChange: (Map<String, String>) -> Unit) {
    when (title) {
        "Identificação da criança" -> IdentificacaoCriancaContent(formData, onFormDataChange)
        "Dados familiares" -> DadosFamiliaresCriancaContent(formData, onFormDataChange)
        "Desenvolvimento da saúde" -> DesenvolvimentoSaudeCriancaContent(formData, onFormDataChange)
        "Histórico escolar" -> HistoricoEscolarCriancaContent(formData, onFormDataChange)
        "Histórico médico" -> HistoricoMedicoCriancaContent(formData, onFormDataChange)
        "Comportamento e socialização" -> ComportamentoSocializacaoCriancaContent(formData, onFormDataChange)
        "Queixas principais" -> QueixasPrincipaisCriancaContent(formData, onFormDataChange)
    }
}

@Composable
fun renderAdolescentesContent(title: String, formData: Map<String, String>, onFormDataChange: (Map<String, String>) -> Unit) {
    when (title) {
        "Identificação do adolescente" -> IdentificacaoAdolescenteContent(formData, onFormDataChange)
        "Dados familiares" -> DadosFamiliaresAdolescenteContent(formData, onFormDataChange)
        "Desenvolvimento psicossocial" -> DesenvolvimentoPsicossocialAdolescenteContent(formData, onFormDataChange)
        "Histórico escolar" -> HistoricoEscolarAdolescenteContent(formData, onFormDataChange)
        "Histórico médico" -> HistoricoMedicoAdolescenteContent(formData, onFormDataChange)
        "Comportamento e relacionamentos" -> ComportamentoRelacionamentosAdolescenteContent(formData, onFormDataChange)
        "Queixas principais" -> QueixasPrincipaisAdolescenteContent(formData, onFormDataChange)
    }
}

@Composable
fun renderIdososContent(title: String, formData: Map<String, String>, onFormDataChange: (Map<String, String>) -> Unit) {
    when (title) {
        "Identificação idoso" -> IdentificacaoIdosoContent(formData, onFormDataChange)
        "Preocupações físicas" -> PreocupacoesFisicasIdosoContent(formData, onFormDataChange)
        "Preocupações sensoriais" -> PreocupacoesSensoriaisIdosoContent(formData, onFormDataChange)
        "Preocupações intelectuais" -> PreocupacoesIntelectuaisIdosoContent(formData, onFormDataChange)
        "Humor/comportamento/personalidade" -> HumorComportamentoPersonalidadeIdosoContent(formData, onFormDataChange)
        "Histórico médico" -> HistoricoMedicoIdosoContent(formData, onFormDataChange)
        "História da família" -> HistoriaFamiliaIdosoContent(formData, onFormDataChange)
        "Histórico profissional" -> HistoricoProfissionalIdosoContent(formData, onFormDataChange)
        "Lazer" -> LazerIdosoContent(formData, onFormDataChange)
        "Hipótese diagnóstica" -> HipotesDiagnosticaIdosoContent(formData, onFormDataChange)
    }
}

@Composable
fun AtendimentoContent(
    formData: Map<String, String>,
    onFormDataChange: (Map<String, String>) -> Unit
) {
    var motivos by remember { mutableStateOf(TextFieldValue(formData["motivos"] ?: "")) }
    var objetivos by remember { mutableStateOf(TextFieldValue(formData["objetivos"] ?: "")) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Descreva os motivos que o levaram a buscar atendimento psicológico:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = motivos,
            onValueChange = { 
                motivos = it
                onFormDataChange(mapOf("motivos" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Descreva os objetivos que você gostaria de alcançar com a terapia:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = objetivos,
            onValueChange = { 
                objetivos = it
                onFormDataChange(mapOf("objetivos" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

@Composable
fun IdentificacaoAdultoContent(
    formData: Map<String, String>,
    onFormDataChange: (Map<String, String>) -> Unit
) {
    val campos = listOf(
        "endereco" to "Endereço com número:",
        "bairro" to "Bairro:",
        "cep" to "Cep:",
        "cidade" to "Cidade:",
        "contatos_emergencia" to "Dois contatos de emergência:",
        "religiao" to "Religião (se tiver):"
    )
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        campos.forEach { (key, label) ->
            var value by remember { mutableStateOf(TextFieldValue(formData[key] ?: "")) }
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            OutlinedTextField(
                value = value,
                onValueChange = { 
                    value = it
                    onFormDataChange(mapOf(key to it.text))
                },
                placeholder = { Text("Digite aqui") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ProfissaoEscolaridadeContent(
    formData: Map<String, String>,
    onFormDataChange: (Map<String, String>) -> Unit
) {
    var profissao by remember { mutableStateOf(TextFieldValue(formData["profissao"] ?: "")) }
    var horario by remember { mutableStateOf(TextFieldValue(formData["horario"] ?: "")) }
    var formacao by remember { mutableStateOf(formData["formacao"] ?: "Ensino Fundamental Completo") }
    
    val formacoes = listOf(
        "Ensino Fundamental Incompleto",
        "Ensino Fundamental Completo",
        "Ensino Médio Incompleto",
        "Ensino Médio completo",
        "Ensino Superior Incompleto",
        "Ensino Superior Completo",
        "Outros"
    )
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Profissão atual:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = profissao,
            onValueChange = { 
                profissao = it
                onFormDataChange(mapOf("profissao" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Text(
            text = "Horário de trabalho- (exemplo 08 às 18:00 horas):",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = horario,
            onValueChange = { 
                horario = it
                onFormDataChange(mapOf("horario" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Text(
            text = "Qual sua formação? (ou grau de formação):",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        formacoes.forEach { opcao ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = formacao == opcao,
                    onClick = {
                        formacao = opcao
                        onFormDataChange(mapOf("formacao" to opcao))
                    }
                )
                Text(
                    text = opcao,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
fun ConjugeFilhosContent(
    formData: Map<String, String>,
    onFormDataChange: (Map<String, String>) -> Unit
) {
    var estadoCivil by remember { mutableStateOf(formData["estado_civil"] ?: "") }
    var conjuge by remember { mutableStateOf(TextFieldValue(formData["conjuge"] ?: "")) }
    var filhos by remember { mutableStateOf(TextFieldValue(formData["filhos"] ?: "")) }
    var relacionamento by remember { mutableStateOf(TextFieldValue(formData["relacionamento"] ?: "")) }
    
    val estadosCivis = listOf("Casado (a)", "Solteiro (a)", "União Estável", "Divorciado (a)", "Viúvo (a)")
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Estado civil:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        estadosCivis.forEach { estado ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = estadoCivil == estado,
                    onClick = {
                        estadoCivil = estado
                        onFormDataChange(mapOf("estado_civil" to estado))
                    }
                )
                Text(
                    text = estado,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
        
        Text(
            text = "Nome, idade do cônjuge e quanto tempo juntos? (se tiver):",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = conjuge,
            onValueChange = { 
                conjuge = it
                onFormDataChange(mapOf("conjuge" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Text(
            text = "Tem filhos? (nome e idade):",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = filhos,
            onValueChange = { 
                filhos = it
                onFormDataChange(mapOf("filhos" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Text(
            text = "Descreva seu relacionamento com seu cônjuge (se houver), incluindo o máximo de detalhes possível.:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = relacionamento,
            onValueChange = { 
                relacionamento = it
                onFormDataChange(mapOf("relacionamento" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

@Composable
fun CicloSocialContent(
    formData: Map<String, String>,
    onFormDataChange: (Map<String, String>) -> Unit
) {
    var relacionamentos by remember { mutableStateOf(TextFieldValue(formData["relacionamentos"] ?: "")) }
    var lugares by remember { mutableStateOf(TextFieldValue(formData["lugares"] ?: "")) }
    var relacionamentoSignificativo by remember { mutableStateOf(TextFieldValue(formData["relacionamento_significativo"] ?: "")) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Como você descreveria seus relacionamentos interpessoais?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = relacionamentos,
            onValueChange = { 
                relacionamentos = it
                onFormDataChange(mapOf("relacionamentos" to it.text))
            },
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
            value = lugares,
            onValueChange = { 
                lugares = it
                onFormDataChange(mapOf("lugares" to it.text))
            },
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
            value = relacionamentoSignificativo,
            onValueChange = { 
                relacionamentoSignificativo = it
                onFormDataChange(mapOf("relacionamento_significativo" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

@Composable
fun HistoricoFamiliarContent(
    formData: Map<String, String>,
    onFormDataChange: (Map<String, String>) -> Unit
) {
    var pai by remember { mutableStateOf(TextFieldValue(formData["pai"] ?: "")) }
    var mae by remember { mutableStateOf(TextFieldValue(formData["mae"] ?: "")) }
    var irmaos by remember { mutableStateOf(TextFieldValue(formData["irmaos"] ?: "")) }
    var outros by remember { mutableStateOf(TextFieldValue(formData["outros"] ?: "")) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Descreva o relacionamento atual com seu pai e como foi durante a infância e adolescência.:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = pai,
            onValueChange = { 
                pai = it
                onFormDataChange(mapOf("pai" to it.text))
            },
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
            value = mae,
            onValueChange = { 
                mae = it
                onFormDataChange(mapOf("mae" to it.text))
            },
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
            value = irmaos,
            onValueChange = { 
                irmaos = it
                onFormDataChange(mapOf("irmaos" to it.text))
            },
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
            value = outros,
            onValueChange = { 
                outros = it
                onFormDataChange(mapOf("outros" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

@Composable
fun HistoricoEscolarContent(
    formData: Map<String, String>,
    onFormDataChange: (Map<String, String>) -> Unit
) {
    var escolar by remember { mutableStateOf(TextFieldValue(formData["escolar"] ?: "")) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Você gostava da escola? quais foram seus principais sucessos ou dificuldades?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = escolar,
            onValueChange = { 
                escolar = it
                onFormDataChange(mapOf("escolar" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

@Composable
fun HistoricoPessoalContent(
    formData: Map<String, String>,
    onFormDataChange: (Map<String, String>) -> Unit
) {
    var infancia by remember { mutableStateOf(TextFieldValue(formData["infancia"] ?: "")) }
    var vicios by remember { mutableStateOf(TextFieldValue(formData["vicios"] ?: "")) }
    var hobbies by remember { mutableStateOf(TextFieldValue(formData["hobbies"] ?: "")) }
    var trabalho by remember { mutableStateOf(TextFieldValue(formData["trabalho"] ?: "")) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Infância:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = infancia,
            onValueChange = { 
                infancia = it
                onFormDataChange(mapOf("infancia" to it.text))
            },
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
            onValueChange = { 
                vicios = it
                onFormDataChange(mapOf("vicios" to it.text))
            },
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
            onValueChange = { 
                hobbies = it
                onFormDataChange(mapOf("hobbies" to it.text))
            },
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
            onValueChange = { 
                trabalho = it
                onFormDataChange(mapOf("trabalho" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

@Composable
fun HistoricoMedicoContent(
    formData: Map<String, String>,
    onFormDataChange: (Map<String, String>) -> Unit
) {
    var checkup by remember { mutableStateOf(TextFieldValue(formData["checkup"] ?: "")) }
    var vitaminas by remember { mutableStateOf(TextFieldValue(formData["vitaminas"] ?: "")) }
    var doencas by remember { mutableStateOf(TextFieldValue(formData["doencas"] ?: "")) }
    var tratamento by remember { mutableStateOf(TextFieldValue(formData["tratamento"] ?: "")) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Qual foi a última vez que você fez um check-up?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = checkup,
            onValueChange = { 
                checkup = it
                onFormDataChange(mapOf("checkup" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Text(
            text = "Qual foi a última vez que você fez exames de vitaminas? quanto está sua vitaminas b12 e d:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = vitaminas,
            onValueChange = { 
                vitaminas = it
                onFormDataChange(mapOf("vitaminas" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Text(
            text = "Atualmente, você está tratando alguma doença ou enfermidade?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = doencas,
            onValueChange = { 
                doencas = it
                onFormDataChange(mapOf("doencas" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Text(
            text = "Você já recebeu tratamento psiquiátrico ou psicológico? em caso positivo, especifique.:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = tratamento,
            onValueChange = { 
                tratamento = it
                onFormDataChange(mapOf("tratamento" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

@Composable
fun QueixasPrincipaisContent(
    formData: Map<String, String>,
    onFormDataChange: (Map<String, String>) -> Unit
) {
    var queixas by remember { mutableStateOf(TextFieldValue(formData["queixas"] ?: "")) }
    var duracao by remember { mutableStateOf(TextFieldValue(formData["duracao"] ?: "")) }
    var evolucao by remember { mutableStateOf(TextFieldValue(formData["evolucao"] ?: "")) }
    var situacao by remember { mutableStateOf(TextFieldValue(formData["situacao"] ?: "")) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Quais suas queixas considera principais?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = queixas,
            onValueChange = { 
                queixas = it
                onFormDataChange(mapOf("queixas" to it.text))
            },
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
            value = duracao,
            onValueChange = { 
                duracao = it
                onFormDataChange(mapOf("duracao" to it.text))
            },
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
            value = evolucao,
            onValueChange = { 
                evolucao = it
                onFormDataChange(mapOf("evolucao" to it.text))
            },
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
            value = situacao,
            onValueChange = { 
                situacao = it
                onFormDataChange(mapOf("situacao" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
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

// ===== CONTEÚDO PARA CRIANÇAS =====

@Composable
fun IdentificacaoCriancaContent(
    formData: Map<String, String>,
    onFormDataChange: (Map<String, String>) -> Unit
) {
    val campos = listOf(
        "idade_crianca" to "Idade da criança:",
        "escola_crianca" to "Escola que frequenta:",
        "serie_crianca" to "Série/ano escolar:",
        "responsavel_crianca" to "Responsável legal:",
        "contato_emergencia_crianca" to "Contato de emergência:"
    )
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        campos.forEach { (key, label) ->
            var value by remember { mutableStateOf(TextFieldValue(formData[key] ?: "")) }
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            OutlinedTextField(
                value = value,
                onValueChange = { 
                    value = it
                    onFormDataChange(mapOf(key to it.text))
                },
                placeholder = { Text("Digite aqui") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun DadosFamiliaresCriancaContent(
    formData: Map<String, String>,
    onFormDataChange: (Map<String, String>) -> Unit
) {
    var composicaoFamiliar by remember { mutableStateOf(TextFieldValue(formData["composicao_familiar"] ?: "")) }
    var relacionamentoPais by remember { mutableStateOf(TextFieldValue(formData["relacionamento_pais"] ?: "")) }
    var outrosCuidadores by remember { mutableStateOf(TextFieldValue(formData["outros_cuidadores"] ?: "")) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Composição familiar (pais, irmãos, outros):",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = composicaoFamiliar,
            onValueChange = { 
                composicaoFamiliar = it
                onFormDataChange(mapOf("composicao_familiar" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Relacionamento entre os pais:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = relacionamentoPais,
            onValueChange = { 
                relacionamentoPais = it
                onFormDataChange(mapOf("relacionamento_pais" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Outros cuidadores (avós, tios, etc.):",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = outrosCuidadores,
            onValueChange = { 
                outrosCuidadores = it
                onFormDataChange(mapOf("outros_cuidadores" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

@Composable
fun DesenvolvimentoSaudeCriancaContent(
    formData: Map<String, String>,
    onFormDataChange: (Map<String, String>) -> Unit
) {
    var gestacao by remember { mutableStateOf(TextFieldValue(formData["gestacao"] ?: "")) }
    var parto by remember { mutableStateOf(TextFieldValue(formData["parto"] ?: "")) }
    var desenvolvimento by remember { mutableStateOf(TextFieldValue(formData["desenvolvimento"] ?: "")) }
    var alimentacao by remember { mutableStateOf(TextFieldValue(formData["alimentacao"] ?: "")) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Como foi a gestação?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = gestacao,
            onValueChange = { 
                gestacao = it
                onFormDataChange(mapOf("gestacao" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Como foi o parto?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = parto,
            onValueChange = { 
                parto = it
                onFormDataChange(mapOf("parto" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Desenvolvimento motor e da linguagem:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = desenvolvimento,
            onValueChange = { 
                desenvolvimento = it
                onFormDataChange(mapOf("desenvolvimento" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Hábitos alimentares:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = alimentacao,
            onValueChange = { 
                alimentacao = it
                onFormDataChange(mapOf("alimentacao" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

@Composable
fun HistoricoEscolarCriancaContent(
    formData: Map<String, String>,
    onFormDataChange: (Map<String, String>) -> Unit
) {
    var adaptacaoEscolar by remember { mutableStateOf(TextFieldValue(formData["adaptacao_escolar"] ?: "")) }
    var dificuldadesEscolares by remember { mutableStateOf(TextFieldValue(formData["dificuldades_escolares"] ?: "")) }
    var relacionamentoColegas by remember { mutableStateOf(TextFieldValue(formData["relacionamento_colegas"] ?: "")) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Como foi a adaptação escolar?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = adaptacaoEscolar,
            onValueChange = { 
                adaptacaoEscolar = it
                onFormDataChange(mapOf("adaptacao_escolar" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Dificuldades escolares identificadas:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = dificuldadesEscolares,
            onValueChange = { 
                dificuldadesEscolares = it
                onFormDataChange(mapOf("dificuldades_escolares" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Relacionamento com colegas:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = relacionamentoColegas,
            onValueChange = { 
                relacionamentoColegas = it
                onFormDataChange(mapOf("relacionamento_colegas" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

@Composable
fun HistoricoMedicoCriancaContent(
    formData: Map<String, String>,
    onFormDataChange: (Map<String, String>) -> Unit
) {
    var doencas by remember { mutableStateOf(TextFieldValue(formData["doencas"] ?: "")) }
    var medicamentos by remember { mutableStateOf(TextFieldValue(formData["medicamentos"] ?: "")) }
    var alergias by remember { mutableStateOf(TextFieldValue(formData["alergias"] ?: "")) }
    var hospitalizacoes by remember { mutableStateOf(TextFieldValue(formData["hospitalizacoes"] ?: "")) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Doenças ou condições médicas:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = doencas,
            onValueChange = { 
                doencas = it
                onFormDataChange(mapOf("doencas" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Medicamentos em uso:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = medicamentos,
            onValueChange = { 
                medicamentos = it
                onFormDataChange(mapOf("medicamentos" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Alergias conhecidas:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = alergias,
            onValueChange = { 
                alergias = it
                onFormDataChange(mapOf("alergias" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Hospitalizações ou cirurgias:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = hospitalizacoes,
            onValueChange = { 
                hospitalizacoes = it
                onFormDataChange(mapOf("hospitalizacoes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

@Composable
fun ComportamentoSocializacaoCriancaContent(
    formData: Map<String, String>,
    onFormDataChange: (Map<String, String>) -> Unit
) {
    var comportamento by remember { mutableStateOf(TextFieldValue(formData["comportamento"] ?: "")) }
    var socializacao by remember { mutableStateOf(TextFieldValue(formData["socializacao"] ?: "")) }
    var brincadeiras by remember { mutableStateOf(TextFieldValue(formData["brincadeiras"] ?: "")) }
    var rotina by remember { mutableStateOf(TextFieldValue(formData["rotina"] ?: "")) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Comportamento geral da criança:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = comportamento,
            onValueChange = { 
                comportamento = it
                onFormDataChange(mapOf("comportamento" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Como é a socialização com outras crianças?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = socializacao,
            onValueChange = { 
                socializacao = it
                onFormDataChange(mapOf("socializacao" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Tipos de brincadeiras preferidas:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = brincadeiras,
            onValueChange = { 
                brincadeiras = it
                onFormDataChange(mapOf("brincadeiras" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Rotina diária da criança:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = rotina,
            onValueChange = { 
                rotina = it
                onFormDataChange(mapOf("rotina" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

@Composable
fun QueixasPrincipaisCriancaContent(
    formData: Map<String, String>,
    onFormDataChange: (Map<String, String>) -> Unit
) {
    var queixas by remember { mutableStateOf(TextFieldValue(formData["queixas"] ?: "")) }
    var quandoComecou by remember { mutableStateOf(TextFieldValue(formData["quando_comecou"] ?: "")) }
    var fatores by remember { mutableStateOf(TextFieldValue(formData["fatores"] ?: "")) }
    var expectativas by remember { mutableStateOf(TextFieldValue(formData["expectativas"] ?: "")) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Quais são as principais queixas sobre a criança?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = queixas,
            onValueChange = { 
                queixas = it
                onFormDataChange(mapOf("queixas" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Quando começaram essas dificuldades?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = quandoComecou,
            onValueChange = { 
                quandoComecou = it
                onFormDataChange(mapOf("quando_comecou" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Quais fatores podem estar relacionados?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = fatores,
            onValueChange = { 
                fatores = it
                onFormDataChange(mapOf("fatores" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "O que esperam da terapia?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = expectativas,
            onValueChange = { 
                expectativas = it
                onFormDataChange(mapOf("expectativas" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
} 

// ===== CONTEÚDO PARA ADOLESCENTES =====

@Composable
fun IdentificacaoAdolescenteContent(
    formData: Map<String, String>,
    onFormDataChange: (Map<String, String>) -> Unit
) {
    val campos = listOf(
        "idade_adolescente" to "Idade do adolescente:",
        "escola_adolescente" to "Escola que frequenta:",
        "serie_adolescente" to "Série/ano escolar:",
        "responsavel_adolescente" to "Responsável legal:",
        "contato_emergencia_adolescente" to "Contato de emergência:"
    )
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        campos.forEach { (key, label) ->
            var value by remember { mutableStateOf(TextFieldValue(formData[key] ?: "")) }
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            OutlinedTextField(
                value = value,
                onValueChange = { 
                    value = it
                    onFormDataChange(mapOf(key to it.text))
                },
                placeholder = { Text("Digite aqui") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun DadosFamiliaresAdolescenteContent(
    formData: Map<String, String>,
    onFormDataChange: (Map<String, String>) -> Unit
) {
    var composicaoFamiliar by remember { mutableStateOf(TextFieldValue(formData["composicao_familiar"] ?: "")) }
    var relacionamentoPais by remember { mutableStateOf(TextFieldValue(formData["relacionamento_pais"] ?: "")) }
    var outrosCuidadores by remember { mutableStateOf(TextFieldValue(formData["outros_cuidadores"] ?: "")) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Composição familiar (pais, irmãos, outros):",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = composicaoFamiliar,
            onValueChange = { 
                composicaoFamiliar = it
                onFormDataChange(mapOf("composicao_familiar" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Relacionamento entre os pais:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = relacionamentoPais,
            onValueChange = { 
                relacionamentoPais = it
                onFormDataChange(mapOf("relacionamento_pais" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Outros cuidadores (avós, tios, etc.):",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = outrosCuidadores,
            onValueChange = { 
                outrosCuidadores = it
                onFormDataChange(mapOf("outros_cuidadores" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

@Composable
fun DesenvolvimentoPsicossocialAdolescenteContent(
    formData: Map<String, String>,
    onFormDataChange: (Map<String, String>) -> Unit
) {
    var desenvolvimento by remember { mutableStateOf(TextFieldValue(formData["desenvolvimento"] ?: "")) }
    var socializacao by remember { mutableStateOf(TextFieldValue(formData["socializacao"] ?: "")) }
    var interesses by remember { mutableStateOf(TextFieldValue(formData["interesses"] ?: "")) }
    var rotina by remember { mutableStateOf(TextFieldValue(formData["rotina"] ?: "")) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Desenvolvimento psicossocial:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = desenvolvimento,
            onValueChange = { 
                desenvolvimento = it
                onFormDataChange(mapOf("desenvolvimento" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Como é a socialização com outros adolescentes?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = socializacao,
            onValueChange = { 
                socializacao = it
                onFormDataChange(mapOf("socializacao" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Interesses e hobbies:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = interesses,
            onValueChange = { 
                interesses = it
                onFormDataChange(mapOf("interesses" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Rotina diária do adolescente:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = rotina,
            onValueChange = { 
                rotina = it
                onFormDataChange(mapOf("rotina" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

@Composable
fun HistoricoEscolarAdolescenteContent(
    formData: Map<String, String>,
    onFormDataChange: (Map<String, String>) -> Unit
) {
    var adaptacaoEscolar by remember { mutableStateOf(TextFieldValue(formData["adaptacao_escolar"] ?: "")) }
    var dificuldadesEscolares by remember { mutableStateOf(TextFieldValue(formData["dificuldades_escolares"] ?: "")) }
    var relacionamentoColegas by remember { mutableStateOf(TextFieldValue(formData["relacionamento_colegas"] ?: "")) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Como é a adaptação escolar?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = adaptacaoEscolar,
            onValueChange = { 
                adaptacaoEscolar = it
                onFormDataChange(mapOf("adaptacao_escolar" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Dificuldades escolares identificadas:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = dificuldadesEscolares,
            onValueChange = { 
                dificuldadesEscolares = it
                onFormDataChange(mapOf("dificuldades_escolares" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Relacionamento com colegas:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = relacionamentoColegas,
            onValueChange = { 
                relacionamentoColegas = it
                onFormDataChange(mapOf("relacionamento_colegas" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

@Composable
fun HistoricoMedicoAdolescenteContent(
    formData: Map<String, String>,
    onFormDataChange: (Map<String, String>) -> Unit
) {
    var doencas by remember { mutableStateOf(TextFieldValue(formData["doencas"] ?: "")) }
    var medicamentos by remember { mutableStateOf(TextFieldValue(formData["medicamentos"] ?: "")) }
    var alergias by remember { mutableStateOf(TextFieldValue(formData["alergias"] ?: "")) }
    var hospitalizacoes by remember { mutableStateOf(TextFieldValue(formData["hospitalizacoes"] ?: "")) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Doenças ou condições médicas:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = doencas,
            onValueChange = { 
                doencas = it
                onFormDataChange(mapOf("doencas" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Medicamentos em uso:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = medicamentos,
            onValueChange = { 
                medicamentos = it
                onFormDataChange(mapOf("medicamentos" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Alergias conhecidas:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = alergias,
            onValueChange = { 
                alergias = it
                onFormDataChange(mapOf("alergias" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Hospitalizações ou cirurgias:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = hospitalizacoes,
            onValueChange = { 
                hospitalizacoes = it
                onFormDataChange(mapOf("hospitalizacoes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

@Composable
fun ComportamentoRelacionamentosAdolescenteContent(
    formData: Map<String, String>,
    onFormDataChange: (Map<String, String>) -> Unit
) {
    var comportamento by remember { mutableStateOf(TextFieldValue(formData["comportamento"] ?: "")) }
    var relacionamentos by remember { mutableStateOf(TextFieldValue(formData["relacionamentos"] ?: "")) }
    var conflitos by remember { mutableStateOf(TextFieldValue(formData["conflitos"] ?: "")) }
    var expectativas by remember { mutableStateOf(TextFieldValue(formData["expectativas"] ?: "")) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Comportamento geral do adolescente:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = comportamento,
            onValueChange = { 
                comportamento = it
                onFormDataChange(mapOf("comportamento" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Como são os relacionamentos interpessoais?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = relacionamentos,
            onValueChange = { 
                relacionamentos = it
                onFormDataChange(mapOf("relacionamentos" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Conflitos frequentes:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = conflitos,
            onValueChange = { 
                conflitos = it
                onFormDataChange(mapOf("conflitos" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Expectativas para o futuro:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = expectativas,
            onValueChange = { 
                expectativas = it
                onFormDataChange(mapOf("expectativas" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

@Composable
fun QueixasPrincipaisAdolescenteContent(
    formData: Map<String, String>,
    onFormDataChange: (Map<String, String>) -> Unit
) {
    var queixas by remember { mutableStateOf(TextFieldValue(formData["queixas"] ?: "")) }
    var quandoComecou by remember { mutableStateOf(TextFieldValue(formData["quando_comecou"] ?: "")) }
    var fatores by remember { mutableStateOf(TextFieldValue(formData["fatores"] ?: "")) }
    var expectativas by remember { mutableStateOf(TextFieldValue(formData["expectativas"] ?: "")) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Quais são as principais queixas sobre o adolescente?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = queixas,
            onValueChange = { 
                queixas = it
                onFormDataChange(mapOf("queixas" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Quando começaram essas dificuldades?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = quandoComecou,
            onValueChange = { 
                quandoComecou = it
                onFormDataChange(mapOf("quando_comecou" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Quais fatores podem estar relacionados?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = fatores,
            onValueChange = { 
                fatores = it
                onFormDataChange(mapOf("fatores" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "O que esperam da terapia?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = expectativas,
            onValueChange = { 
                expectativas = it
                onFormDataChange(mapOf("expectativas" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

// ===== CONTEÚDO PARA IDOSOS =====

@Composable
fun IdentificacaoIdosoContent(
    formData: Map<String, String>,
    onFormDataChange: (Map<String, String>) -> Unit
) {
    var nomeDepoente by remember { mutableStateOf(TextFieldValue(formData["nome_depoente"] ?: "")) }
    var relacaoPaciente by remember { mutableStateOf(TextFieldValue(formData["relacao_paciente"] ?: "")) }
    var diagnosticoMedico by remember { mutableStateOf(TextFieldValue(formData["diagnostico_medico"] ?: "")) }
    var queixaMotivo by remember { mutableStateOf(TextFieldValue(formData["queixa_motivo"] ?: "")) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Nome do depoente:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = nomeDepoente,
            onValueChange = { 
                nomeDepoente = it
                onFormDataChange(mapOf("nome_depoente" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Text(
            text = "Relação com paciente:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = relacaoPaciente,
            onValueChange = { 
                relacaoPaciente = it
                onFormDataChange(mapOf("relacao_paciente" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Text(
            text = "Diagnóstico médico (se houver):",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = diagnosticoMedico,
            onValueChange = { 
                diagnosticoMedico = it
                onFormDataChange(mapOf("diagnostico_medico" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Queixa ou motivo da consulta:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = queixaMotivo,
            onValueChange = { 
                queixaMotivo = it
                onFormDataChange(mapOf("queixa_motivo" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

@Composable
fun DadosFamiliaresIdosoContent(
    formData: Map<String, String>,
    onFormDataChange: (Map<String, String>) -> Unit
) {
    var composicaoFamiliar by remember { mutableStateOf(TextFieldValue(formData["composicao_familiar"] ?: "")) }
    var relacionamentoFamilia by remember { mutableStateOf(TextFieldValue(formData["relacionamento_familia"] ?: "")) }
    var suporteFamilia by remember { mutableStateOf(TextFieldValue(formData["suporte_familia"] ?: "")) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Composição familiar:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = composicaoFamiliar,
            onValueChange = { 
                composicaoFamiliar = it
                onFormDataChange(mapOf("composicao_familiar" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Relacionamento com a família:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = relacionamentoFamilia,
            onValueChange = { 
                relacionamentoFamilia = it
                onFormDataChange(mapOf("relacionamento_familia" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Suporte familiar disponível:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = suporteFamilia,
            onValueChange = { 
                suporteFamilia = it
                onFormDataChange(mapOf("suporte_familia" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

@Composable
fun HistoricoMedicoIdosoContent(
    formData: Map<String, String>,
    onFormDataChange: (Map<String, String>) -> Unit
) {
    var arteriosclerose by remember { mutableStateOf(formData["arteriosclerose"] ?: "Não") }
    var arterioscleroseDetalhes by remember { mutableStateOf(TextFieldValue(formData["arteriosclerose_detalhes"] ?: "")) }
    var demencia by remember { mutableStateOf(formData["demencia"] ?: "Não") }
    var demenciaDetalhes by remember { mutableStateOf(TextFieldValue(formData["demencia_detalhes"] ?: "")) }
    var outrasInfeccoes by remember { mutableStateOf(formData["outras_infeccoes"] ?: "Não") }
    var outrasInfeccoesDetalhes by remember { mutableStateOf(TextFieldValue(formData["outras_infeccoes_detalhes"] ?: "")) }
    var diabetes by remember { mutableStateOf(formData["diabetes"] ?: "Não") }
    var diabetesDetalhes by remember { mutableStateOf(TextFieldValue(formData["diabetes_detalhes"] ?: "")) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Arteriosclerose
        Text(
            text = "Arteriosclerose?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = arteriosclerose,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = arterioscleroseDetalhes,
            onValueChange = { 
                arterioscleroseDetalhes = it
                onFormDataChange(mapOf("arteriosclerose_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Demência
        Text(
            text = "Demência?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = demencia,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = demenciaDetalhes,
            onValueChange = { 
                demenciaDetalhes = it
                onFormDataChange(mapOf("demencia_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Outras infecções no cérebro
        Text(
            text = "Outras infecções no cérebro ou desordens (meningite, encefalite, privação de oxigênio, etc)?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = outrasInfeccoes,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = outrasInfeccoesDetalhes,
            onValueChange = { 
                outrasInfeccoesDetalhes = it
                onFormDataChange(mapOf("outras_infeccoes_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Diabetes
        Text(
            text = "Diabetes?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = diabetes,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = diabetesDetalhes,
            onValueChange = { 
                diabetesDetalhes = it
                onFormDataChange(mapOf("diabetes_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun CondicoesVidaIdosoContent(
    formData: Map<String, String>,
    onFormDataChange: (Map<String, String>) -> Unit
) {
    var moradia by remember { mutableStateOf(TextFieldValue(formData["moradia"] ?: "")) }
    var independencia by remember { mutableStateOf(TextFieldValue(formData["independencia"] ?: "")) }
    var atividades by remember { mutableStateOf(TextFieldValue(formData["atividades"] ?: "")) }
    var rotina by remember { mutableStateOf(TextFieldValue(formData["rotina"] ?: "")) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Condições de moradia:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = moradia,
            onValueChange = { 
                moradia = it
                onFormDataChange(mapOf("moradia" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Nível de independência:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = independencia,
            onValueChange = { 
                independencia = it
                onFormDataChange(mapOf("independencia" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Atividades diárias:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = atividades,
            onValueChange = { 
                atividades = it
                onFormDataChange(mapOf("atividades" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Rotina diária:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = rotina,
            onValueChange = { 
                rotina = it
                onFormDataChange(mapOf("rotina" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

@Composable
fun AspectosCognitivosIdosoContent(
    formData: Map<String, String>,
    onFormDataChange: (Map<String, String>) -> Unit
) {
    var memoria by remember { mutableStateOf(TextFieldValue(formData["memoria"] ?: "")) }
    var orientacao by remember { mutableStateOf(TextFieldValue(formData["orientacao"] ?: "")) }
    var linguagem by remember { mutableStateOf(TextFieldValue(formData["linguagem"] ?: "")) }
    var concentracao by remember { mutableStateOf(TextFieldValue(formData["concentracao"] ?: "")) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Avaliação da memória:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = memoria,
            onValueChange = { 
                memoria = it
                onFormDataChange(mapOf("memoria" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Orientação temporal e espacial:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = orientacao,
            onValueChange = { 
                orientacao = it
                onFormDataChange(mapOf("orientacao" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Linguagem e comunicação:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = linguagem,
            onValueChange = { 
                linguagem = it
                onFormDataChange(mapOf("linguagem" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Concentração e atenção:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = concentracao,
            onValueChange = { 
                concentracao = it
                onFormDataChange(mapOf("concentracao" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

@Composable
fun AspectosEmocionaisIdosoContent(
    formData: Map<String, String>,
    onFormDataChange: (Map<String, String>) -> Unit
) {
    var humor by remember { mutableStateOf(TextFieldValue(formData["humor"] ?: "")) }
    var ansiedade by remember { mutableStateOf(TextFieldValue(formData["ansiedade"] ?: "")) }
    var depressao by remember { mutableStateOf(TextFieldValue(formData["depressao"] ?: "")) }
    var motivacao by remember { mutableStateOf(TextFieldValue(formData["motivacao"] ?: "")) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Estado de humor:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = humor,
            onValueChange = { 
                humor = it
                onFormDataChange(mapOf("humor" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Sintomas de ansiedade:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = ansiedade,
            onValueChange = { 
                ansiedade = it
                onFormDataChange(mapOf("ansiedade" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Sintomas de depressão:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = depressao,
            onValueChange = { 
                depressao = it
                onFormDataChange(mapOf("depressao" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Motivação e interesse:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = motivacao,
            onValueChange = { 
                motivacao = it
                onFormDataChange(mapOf("motivacao" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

@Composable
fun QueixasPrincipaisIdosoContent(
    formData: Map<String, String>,
    onFormDataChange: (Map<String, String>) -> Unit
) {
    var queixas by remember { mutableStateOf(TextFieldValue(formData["queixas"] ?: "")) }
    var quandoComecou by remember { mutableStateOf(TextFieldValue(formData["quando_comecou"] ?: "")) }
    var fatores by remember { mutableStateOf(TextFieldValue(formData["fatores"] ?: "")) }
    var expectativas by remember { mutableStateOf(TextFieldValue(formData["expectativas"] ?: "")) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Quais são as principais queixas do idoso?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = queixas,
            onValueChange = { 
                queixas = it
                onFormDataChange(mapOf("queixas" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Quando começaram essas dificuldades?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = quandoComecou,
            onValueChange = { 
                quandoComecou = it
                onFormDataChange(mapOf("quando_comecou" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "Quais fatores podem estar relacionados?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = fatores,
            onValueChange = { 
                fatores = it
                onFormDataChange(mapOf("fatores" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Text(
            text = "O que esperam da terapia?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = expectativas,
            onValueChange = { 
                expectativas = it
                onFormDataChange(mapOf("expectativas" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
} 

@Composable
fun PreocupacoesFisicasIdosoContent(
    formData: Map<String, String>,
    onFormDataChange: (Map<String, String>) -> Unit
) {
    var doresCabeca by remember { mutableStateOf(formData["dores_cabeca"] ?: "Não") }
    var doresCabecaDetalhes by remember { mutableStateOf(TextFieldValue(formData["dores_cabeca_detalhes"] ?: "")) }
    var observacoes by remember { mutableStateOf(TextFieldValue(formData["observacoes"] ?: "")) }
    var tonturas by remember { mutableStateOf(formData["tonturas"] ?: "Não") }
    var tonturasDetalhes by remember { mutableStateOf(TextFieldValue(formData["tonturas_detalhes"] ?: "")) }
    var enjoosVomitos by remember { mutableStateOf(formData["enjoos_vomitos"] ?: "Não") }
    var enjoosVomitosDetalhes by remember { mutableStateOf(TextFieldValue(formData["enjoos_vomitos_detalhes"] ?: "")) }
    var fadigaExcessiva by remember { mutableStateOf(formData["fadiga_excessiva"] ?: "Não") }
    var fadigaExcessivaDetalhes by remember { mutableStateOf(TextFieldValue(formData["fadiga_excessiva_detalhes"] ?: "")) }
    var incontinencia by remember { mutableStateOf(formData["incontinencia"] ?: "Não") }
    var incontinenciaDetalhes by remember { mutableStateOf(TextFieldValue(formData["incontinencia_detalhes"] ?: "")) }
    var problemasIntestinais by remember { mutableStateOf(formData["problemas_intestinais"] ?: "Não") }
    var problemasIntestinaisDetalhes by remember { mutableStateOf(TextFieldValue(formData["problemas_intestinais_detalhes"] ?: "")) }
    var fraquezaCorpo by remember { mutableStateOf(formData["fraqueza_corpo"] ?: "Não") }
    var fraquezaCorpoDetalhes by remember { mutableStateOf(TextFieldValue(formData["fraqueza_corpo_detalhes"] ?: "")) }
    var problemasCoordenacao by remember { mutableStateOf(formData["problemas_coordenacao"] ?: "Não") }
    var problemasCoordenacaoDetalhes by remember { mutableStateOf(TextFieldValue(formData["problemas_coordenacao_detalhes"] ?: "")) }
    var tremores by remember { mutableStateOf(formData["tremores"] ?: "Não") }
    var tremoresDetalhes by remember { mutableStateOf(TextFieldValue(formData["tremores_detalhes"] ?: "")) }
    var tiquesMovimentos by remember { mutableStateOf(formData["tiques_movimentos"] ?: "Não") }
    var tiquesMovimentosDetalhes by remember { mutableStateOf(TextFieldValue(formData["tiques_movimentos_detalhes"] ?: "")) }
    var desmaios by remember { mutableStateOf(formData["desmaios"] ?: "Não") }
    var desmaiosDetalhes by remember { mutableStateOf(TextFieldValue(formData["desmaios_detalhes"] ?: "")) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Dores de cabeça
        Text(
            text = "Dores de cabeça?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = doresCabeca,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = doresCabecaDetalhes,
            onValueChange = { 
                doresCabecaDetalhes = it
                onFormDataChange(mapOf("dores_cabeca_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Text(
            text = "Observações:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = observacoes,
            onValueChange = { 
                observacoes = it
                onFormDataChange(mapOf("observacoes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        // Tonturas
        Text(
            text = "Tonturas:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = tonturas,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = tonturasDetalhes,
            onValueChange = { 
                tonturasDetalhes = it
                onFormDataChange(mapOf("tonturas_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Enjoos ou vômitos
        Text(
            text = "Enjoos ou vômitos:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = enjoosVomitos,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = enjoosVomitosDetalhes,
            onValueChange = { 
                enjoosVomitosDetalhes = it
                onFormDataChange(mapOf("enjoos_vomitos_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Fadiga excessiva
        Text(
            text = "Fadiga excessiva?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = fadigaExcessiva,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = fadigaExcessivaDetalhes,
            onValueChange = { 
                fadigaExcessivaDetalhes = it
                onFormDataChange(mapOf("fadiga_excessiva_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Incontinência
        Text(
            text = "Incontinência urinária/fecal:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = incontinencia,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = incontinenciaDetalhes,
            onValueChange = { 
                incontinenciaDetalhes = it
                onFormDataChange(mapOf("incontinencia_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Problemas intestinais
        Text(
            text = "Problemas intestinais?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = problemasIntestinais,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = problemasIntestinaisDetalhes,
            onValueChange = { 
                problemasIntestinaisDetalhes = it
                onFormDataChange(mapOf("problemas_intestinais_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Fraqueza de um lado do corpo
        Text(
            text = "Fraqueza de um lado do corpo? qual parte?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = fraquezaCorpo,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = fraquezaCorpoDetalhes,
            onValueChange = { 
                fraquezaCorpoDetalhes = it
                onFormDataChange(mapOf("fraqueza_corpo_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Problemas com coordenação
        Text(
            text = "Problemas com a coordenação?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = problemasCoordenacao,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = problemasCoordenacaoDetalhes,
            onValueChange = { 
                problemasCoordenacaoDetalhes = it
                onFormDataChange(mapOf("problemas_coordenacao_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Tremores
        Text(
            text = "Tremores?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = tremores,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = tremoresDetalhes,
            onValueChange = { 
                tremoresDetalhes = it
                onFormDataChange(mapOf("tremores_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Tiques ou movimentos estranhos
        Text(
            text = "Tiques ou movimentos estranhos?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = tiquesMovimentos,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = tiquesMovimentosDetalhes,
            onValueChange = { 
                tiquesMovimentosDetalhes = it
                onFormDataChange(mapOf("tiques_movimentos_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Desmaios
        Text(
            text = "Desmaios:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = desmaios,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = desmaiosDetalhes,
            onValueChange = { 
                desmaiosDetalhes = it
                onFormDataChange(mapOf("desmaios_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
    }
} 

@Composable
fun PreocupacoesSensoriaisIdosoContent(
    formData: Map<String, String>,
    onFormDataChange: (Map<String, String>) -> Unit
) {
    var perdaSensacoes by remember { mutableStateOf(formData["perda_sensacoes"] ?: "Não") }
    var perdaSensacoesDetalhes by remember { mutableStateOf(TextFieldValue(formData["perda_sensacoes_detalhes"] ?: "")) }
    var formigamentos by remember { mutableStateOf(formData["formigamentos"] ?: "Não") }
    var formigamentosDetalhes by remember { mutableStateOf(TextFieldValue(formData["formigamentos_detalhes"] ?: "")) }
    var dificuldadeQuenteFrio by remember { mutableStateOf(formData["dificuldade_quente_frio"] ?: "Não") }
    var dificuldadeQuenteFrioDetalhes by remember { mutableStateOf(TextFieldValue(formData["dificuldade_quente_frio_detalhes"] ?: "")) }
    var comprometimentoVisual by remember { mutableStateOf(formData["comprometimento_visual"] ?: "Não") }
    var comprometimentoVisualDetalhes by remember { mutableStateOf(TextFieldValue(formData["comprometimento_visual_detalhes"] ?: "")) }
    var veCoisas by remember { mutableStateOf(formData["ve_coisas"] ?: "Não") }
    var veCoisasDetalhes by remember { mutableStateOf(TextFieldValue(formData["ve_coisas_detalhes"] ?: "")) }
    var brevesCegueira by remember { mutableStateOf(formData["breves_cegueira"] ?: "Não") }
    var brevesCegueiraDetalhes by remember { mutableStateOf(TextFieldValue(formData["breves_cegueira_detalhes"] ?: "")) }
    var perdaAuditiva by remember { mutableStateOf(formData["perda_auditiva"] ?: "Não") }
    var perdaAuditivaDetalhes by remember { mutableStateOf(TextFieldValue(formData["perda_auditiva_detalhes"] ?: "")) }
    var zumbidos by remember { mutableStateOf(formData["zumbidos"] ?: "Não") }
    var zumbidosDetalhes by remember { mutableStateOf(TextFieldValue(formData["zumbidos_detalhes"] ?: "")) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Perda de sensações
        Text(
            text = "Perda de sensações / dormências. qual o local?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = perdaSensacoes,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = perdaSensacoesDetalhes,
            onValueChange = { 
                perdaSensacoesDetalhes = it
                onFormDataChange(mapOf("perda_sensacoes_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Formigamentos
        Text(
            text = "Formigamentos ou sensações estranhas na pele? em qual local?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = formigamentos,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = formigamentosDetalhes,
            onValueChange = { 
                formigamentosDetalhes = it
                onFormDataChange(mapOf("formigamentos_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Dificuldade diferenciar quente e frio
        Text(
            text = "Dificuldade de diferenciar quente e frio?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = dificuldadeQuenteFrio,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = dificuldadeQuenteFrioDetalhes,
            onValueChange = { 
                dificuldadeQuenteFrioDetalhes = it
                onFormDataChange(mapOf("dificuldade_quente_frio_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Comprometimento visual
        Text(
            text = "Comprometimento visual?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = comprometimentoVisual,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = comprometimentoVisualDetalhes,
            onValueChange = { 
                comprometimentoVisualDetalhes = it
                onFormDataChange(mapOf("comprometimento_visual_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Vê coisas que não estão lá
        Text(
            text = "Vê coisas que não estão lá?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = veCoisas,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = veCoisasDetalhes,
            onValueChange = { 
                veCoisasDetalhes = it
                onFormDataChange(mapOf("ve_coisas_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Breves períodos de cegueira
        Text(
            text = "Breves períodos de cegueira:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = brevesCegueira,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = brevesCegueiraDetalhes,
            onValueChange = { 
                brevesCegueiraDetalhes = it
                onFormDataChange(mapOf("breves_cegueira_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Perda auditiva
        Text(
            text = "Perda auditiva?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = perdaAuditiva,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = perdaAuditivaDetalhes,
            onValueChange = { 
                perdaAuditivaDetalhes = it
                onFormDataChange(mapOf("perda_auditiva_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Zumbidos nos ouvidos
        Text(
            text = "Zumbidos nos ouvidos?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = zumbidos,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = zumbidosDetalhes,
            onValueChange = { 
                zumbidosDetalhes = it
                onFormDataChange(mapOf("zumbidos_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun PreocupacoesIntelectuaisIdosoContent(
    formData: Map<String, String>,
    onFormDataChange: (Map<String, String>) -> Unit
) {
    var dificuldadeResolverProblemas by remember { mutableStateOf(formData["dificuldade_resolver_problemas"] ?: "Não") }
    var dificuldadeResolverProblemasDetalhes by remember { mutableStateOf(TextFieldValue(formData["dificuldade_resolver_problemas_detalhes"] ?: "")) }
    var dificuldadePensarRapidamente by remember { mutableStateOf(formData["dificuldade_pensar_rapidamente"] ?: "Não") }
    var dificuldadePensarRapidamenteDetalhes by remember { mutableStateOf(TextFieldValue(formData["dificuldade_pensar_rapidamente_detalhes"] ?: "")) }
    var dificuldadeCompletarAtividades by remember { mutableStateOf(formData["dificuldade_completar_atividades"] ?: "Não") }
    var dificuldadeCompletarAtividadesDetalhes by remember { mutableStateOf(TextFieldValue(formData["dificuldade_completar_atividades_detalhes"] ?: "")) }
    var dificuldadeSequencial by remember { mutableStateOf(formData["dificuldade_sequencial"] ?: "Não") }
    var dificuldadeSequencialDetalhes by remember { mutableStateOf(TextFieldValue(formData["dificuldade_sequencial_detalhes"] ?: "")) }
    var linguagem by remember { mutableStateOf(formData["linguagem"] ?: "Não") }
    var linguagemDetalhes by remember { mutableStateOf(TextFieldValue(formData["linguagem_detalhes"] ?: "")) }
    var problemasEncontrarCaminhos by remember { mutableStateOf(formData["problemas_encontrar_caminhos"] ?: "Não") }
    var problemasEncontrarCaminhosDetalhes by remember { mutableStateOf(TextFieldValue(formData["problemas_encontrar_caminhos_detalhes"] ?: "")) }
    var dificuldadeReconhecer by remember { mutableStateOf(formData["dificuldade_reconhecer"] ?: "Não") }
    var dificuldadeReconhecerDetalhes by remember { mutableStateOf(TextFieldValue(formData["dificuldade_reconhecer_detalhes"] ?: "")) }
    var dificuldadeReconhecerCorpo by remember { mutableStateOf(formData["dificuldade_reconhecer_corpo"] ?: "Não") }
    var dificuldadeReconhecerCorpoDetalhes by remember { mutableStateOf(TextFieldValue(formData["dificuldade_reconhecer_corpo_detalhes"] ?: "")) }
    var dificuldadeOrientacaoTempo by remember { mutableStateOf(formData["dificuldade_orientacao_tempo"] ?: "Não") }
    var dificuldadeOrientacaoTempoDetalhes by remember { mutableStateOf(TextFieldValue(formData["dificuldade_orientacao_tempo_detalhes"] ?: "")) }
    var outrosProblemasNaoVerbais by remember { mutableStateOf(formData["outros_problemas_nao_verbais"] ?: "Não") }
    var outrosProblemasNaoVerbaisDetalhes by remember { mutableStateOf(TextFieldValue(formData["outros_problemas_nao_verbais_detalhes"] ?: "")) }
    var memoria by remember { mutableStateOf(TextFieldValue(formData["memoria"] ?: "")) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Dificuldade resolver problemas
        Text(
            text = "Dificuldade de resolver problemas que a maioria consegue?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = dificuldadeResolverProblemas,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = dificuldadeResolverProblemasDetalhes,
            onValueChange = { 
                dificuldadeResolverProblemasDetalhes = it
                onFormDataChange(mapOf("dificuldade_resolver_problemas_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Dificuldade pensar rapidamente
        Text(
            text = "Dificuldade de pensar rapidamente quando necessário?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = dificuldadePensarRapidamente,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = dificuldadePensarRapidamenteDetalhes,
            onValueChange = { 
                dificuldadePensarRapidamenteDetalhes = it
                onFormDataChange(mapOf("dificuldade_pensar_rapidamente_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Dificuldade completar atividades
        Text(
            text = "Dificuldade de completar atividades em tempo razoável?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = dificuldadeCompletarAtividades,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = dificuldadeCompletarAtividadesDetalhes,
            onValueChange = { 
                dificuldadeCompletarAtividadesDetalhes = it
                onFormDataChange(mapOf("dificuldade_completar_atividades_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Dificuldade sequencial
        Text(
            text = "Dificuldade de fazer coisas seqüencialmente?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = dificuldadeSequencial,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = dificuldadeSequencialDetalhes,
            onValueChange = { 
                dificuldadeSequencialDetalhes = it
                onFormDataChange(mapOf("dificuldade_sequencial_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Linguagem
        Text(
            text = "Linguagem:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = linguagem,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = linguagemDetalhes,
            onValueChange = { 
                linguagemDetalhes = it
                onFormDataChange(mapOf("linguagem_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Problemas encontrar caminhos
        Text(
            text = "Problemas para encontrar caminhos em lugares familiares?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = problemasEncontrarCaminhos,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = problemasEncontrarCaminhosDetalhes,
            onValueChange = { 
                problemasEncontrarCaminhosDetalhes = it
                onFormDataChange(mapOf("problemas_encontrar_caminhos_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Dificuldade reconhecer objetos ou pessoas
        Text(
            text = "Dificuldade de reconhecer objetos ou pessoas?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = dificuldadeReconhecer,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = dificuldadeReconhecerDetalhes,
            onValueChange = { 
                dificuldadeReconhecerDetalhes = it
                onFormDataChange(mapOf("dificuldade_reconhecer_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Dificuldade reconhecer partes do corpo
        Text(
            text = "Dificuldade de reconhecer partes do próprio corpo?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = dificuldadeReconhecerCorpo,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = dificuldadeReconhecerCorpoDetalhes,
            onValueChange = { 
                dificuldadeReconhecerCorpoDetalhes = it
                onFormDataChange(mapOf("dificuldade_reconhecer_corpo_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Dificuldade orientação tempo
        Text(
            text = "Dificuldade de orientação do tempo (dias, meses, ano)?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = dificuldadeOrientacaoTempo,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = dificuldadeOrientacaoTempoDetalhes,
            onValueChange = { 
                dificuldadeOrientacaoTempoDetalhes = it
                onFormDataChange(mapOf("dificuldade_orientacao_tempo_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Outros problemas não verbais
        Text(
            text = "Outros problemas não verbais?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = outrosProblemasNaoVerbais,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = outrosProblemasNaoVerbaisDetalhes,
            onValueChange = { 
                outrosProblemasNaoVerbaisDetalhes = it
                onFormDataChange(mapOf("outros_problemas_nao_verbais_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Memória
        Text(
            text = "Memória:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = memoria,
            onValueChange = { 
                memoria = it
                onFormDataChange(mapOf("memoria" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
} 

@Composable
fun HumorComportamentoPersonalidadeIdosoContent(
    formData: Map<String, String>,
    onFormDataChange: (Map<String, String>) -> Unit
) {
    var tristezaDepressao by remember { mutableStateOf(TextFieldValue(formData["tristeza_depressao"] ?: "")) }
    var ansiedadeNervosismo by remember { mutableStateOf(TextFieldValue(formData["ansiedade_nervosismo"] ?: "")) }
    var estresse by remember { mutableStateOf(formData["estresse"] ?: "Não") }
    var estresseDetalhes by remember { mutableStateOf(TextFieldValue(formData["estresse_detalhes"] ?: "")) }
    var problemasSono by remember { mutableStateOf(formData["problemas_sono"] ?: "Não") }
    var problemasSonoDetalhes by remember { mutableStateOf(TextFieldValue(formData["problemas_sono_detalhes"] ?: "")) }
    var pesadelos by remember { mutableStateOf(formData["pesadelos"] ?: "Não") }
    var pesadelosDetalhes by remember { mutableStateOf(TextFieldValue(formData["pesadelos_detalhes"] ?: "")) }
    var irritadoFacilmente by remember { mutableStateOf(formData["irritado_facilmente"] ?: "Não") }
    var irritadoFacilmenteDetalhes by remember { mutableStateOf(TextFieldValue(formData["irritado_facilmente_detalhes"] ?: "")) }
    var euforia by remember { mutableStateOf(formData["euforia"] ?: "Não") }
    var euforiaDetalhes by remember { mutableStateOf(TextFieldValue(formData["euforia_detalhes"] ?: "")) }
    var muitoEmotivo by remember { mutableStateOf(formData["muito_emotivo"] ?: "Não") }
    var muitoEmotivoDetalhes by remember { mutableStateOf(TextFieldValue(formData["muito_emotivo_detalhes"] ?: "")) }
    var nadaImporta by remember { mutableStateOf(formData["nada_importa"] ?: "Não") }
    var nadaImportaDetalhes by remember { mutableStateOf(TextFieldValue(formData["nada_importa_detalhes"] ?: "")) }
    var facilmenteFrustrado by remember { mutableStateOf(formData["facilmente_frustrado"] ?: "Não") }
    var facilmenteFrustradoDetalhes by remember { mutableStateOf(TextFieldValue(formData["facilmente_frustrado_detalhes"] ?: "")) }
    var coisasAutomaticamente by remember { mutableStateOf(formData["coisas_automaticamente"] ?: "Não") }
    var coisasAutomaticamenteDetalhes by remember { mutableStateOf(TextFieldValue(formData["coisas_automaticamente_detalhes"] ?: "")) }
    var menosInibido by remember { mutableStateOf(formData["menos_inibido"] ?: "Não") }
    var menosInibidoDetalhes by remember { mutableStateOf(TextFieldValue(formData["menos_inibido_detalhes"] ?: "")) }
    var dificuldadeEspontaneo by remember { mutableStateOf(formData["dificuldade_espontaneo"] ?: "Não") }
    var dificuldadeEspontaneoDetalhes by remember { mutableStateOf(TextFieldValue(formData["dificuldade_espontaneo_detalhes"] ?: "")) }
    var mudancaEnergia by remember { mutableStateOf(formData["mudanca_energia"] ?: "Não") }
    var mudancaEnergiaDetalhes by remember { mutableStateOf(TextFieldValue(formData["mudanca_energia_detalhes"] ?: "")) }
    var mudancaApetite by remember { mutableStateOf(formData["mudanca_apetite"] ?: "Não") }
    var mudancaApetiteDetalhes by remember { mutableStateOf(TextFieldValue(formData["mudanca_apetite_detalhes"] ?: "")) }
    var mudancaPeso by remember { mutableStateOf(formData["mudanca_peso"] ?: "Não") }
    var mudancaPesoDetalhes by remember { mutableStateOf(TextFieldValue(formData["mudanca_peso_detalhes"] ?: "")) }
    var mudancaInteresseSexual by remember { mutableStateOf(formData["mudanca_interesse_sexual"] ?: "Não") }
    var mudancaInteresseSexualDetalhes by remember { mutableStateOf(TextFieldValue(formData["mudanca_interesse_sexual_detalhes"] ?: "")) }
    var faltaInteresseAtividades by remember { mutableStateOf(formData["falta_interesse_atividades"] ?: "Não") }
    var faltaInteresseAtividadesDetalhes by remember { mutableStateOf(TextFieldValue(formData["falta_interesse_atividades_detalhes"] ?: "")) }
    var aumentoIrritabilidade by remember { mutableStateOf(formData["aumento_irritabilidade"] ?: "Não") }
    var aumentoIrritabilidadeDetalhes by remember { mutableStateOf(TextFieldValue(formData["aumento_irritabilidade_detalhes"] ?: "")) }
    var outrasMudancasHumor by remember { mutableStateOf(formData["outras_mudancas_humor"] ?: "Não") }
    var outrasMudancasHumorDetalhes by remember { mutableStateOf(TextFieldValue(formData["outras_mudancas_humor_detalhes"] ?: "")) }
    var problemasMatrimoniais by remember { mutableStateOf(formData["problemas_matrimoniais"] ?: "Não") }
    var problemasMatrimoniaisDetalhes by remember { mutableStateOf(TextFieldValue(formData["problemas_matrimoniais_detalhes"] ?: "")) }
    var problemasFinanceiros by remember { mutableStateOf(formData["problemas_financeiros"] ?: "Não") }
    var problemasFinanceirosDetalhes by remember { mutableStateOf(TextFieldValue(formData["problemas_financeiros_detalhes"] ?: "")) }
    var problemasServicosDomesticos by remember { mutableStateOf(formData["problemas_servicos_domesticos"] ?: "Não") }
    var problemasServicosDomesticosDetalhes by remember { mutableStateOf(TextFieldValue(formData["problemas_servicos_domesticos_detalhes"] ?: "")) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Tristeza ou depressão
        Text(
            text = "Tristeza ou depressão?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = tristezaDepressao,
            onValueChange = { 
                tristezaDepressao = it
                onFormDataChange(mapOf("tristeza_depressao" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        // Ansiedade ou nervosismo
        Text(
            text = "Ansiedade ou nervosismo?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = ansiedadeNervosismo,
            onValueChange = { 
                ansiedadeNervosismo = it
                onFormDataChange(mapOf("ansiedade_nervosismo" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        // Estresse
        Text(
            text = "Estresse:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = estresse,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = estresseDetalhes,
            onValueChange = { 
                estresseDetalhes = it
                onFormDataChange(mapOf("estresse_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Problemas no sono
        Text(
            text = "Problemas no sono? (cochilo ou durmindo muito):",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = problemasSono,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = problemasSonoDetalhes,
            onValueChange = { 
                problemasSonoDetalhes = it
                onFormDataChange(mapOf("problemas_sono_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Pesadelos
        Text(
            text = "Tem pesadelos em uma base diária/semanal:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = pesadelos,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = pesadelosDetalhes,
            onValueChange = { 
                pesadelosDetalhes = it
                onFormDataChange(mapOf("pesadelos_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Fica irritado facilmente
        Text(
            text = "Fica irritado facilmente?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = irritadoFacilmente,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = irritadoFacilmenteDetalhes,
            onValueChange = { 
                irritadoFacilmenteDetalhes = it
                onFormDataChange(mapOf("irritado_facilmente_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Sente euforia
        Text(
            text = "Sente euforia (se sentindo no topo do mundo) ?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = euforia,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = euforiaDetalhes,
            onValueChange = { 
                euforiaDetalhes = it
                onFormDataChange(mapOf("euforia_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Se sente muito emotivo
        Text(
            text = "Se sente muito emotivo (chorando facilmente)?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = muitoEmotivo,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = muitoEmotivoDetalhes,
            onValueChange = { 
                muitoEmotivoDetalhes = it
                onFormDataChange(mapOf("muito_emotivo_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Se sente como se nada mais importasse
        Text(
            text = "Se sente como se nada mais importasse ?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = nadaImporta,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = nadaImportaDetalhes,
            onValueChange = { 
                nadaImportaDetalhes = it
                onFormDataChange(mapOf("nada_importa_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Fica facilmente frustrado
        Text(
            text = "Fica facilmente frustrado?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = facilmenteFrustrado,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = facilmenteFrustradoDetalhes,
            onValueChange = { 
                facilmenteFrustradoDetalhes = it
                onFormDataChange(mapOf("facilmente_frustrado_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Faz coisas automaticamente
        Text(
            text = "Faz coisas automaticamente (sem consciência)?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = coisasAutomaticamente,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = coisasAutomaticamenteDetalhes,
            onValueChange = { 
                coisasAutomaticamenteDetalhes = it
                onFormDataChange(mapOf("coisas_automaticamente_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Se sente menos inibido
        Text(
            text = "Se sente menos inibido (fazendo coisas que não fazia antes) ?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = menosInibido,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = menosInibidoDetalhes,
            onValueChange = { 
                menosInibidoDetalhes = it
                onFormDataChange(mapOf("menos_inibido_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Tem dificuldade em ser espontâneo
        Text(
            text = "Tem dificuldade em ser espontâneo?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = dificuldadeEspontaneo,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = dificuldadeEspontaneoDetalhes,
            onValueChange = { 
                dificuldadeEspontaneoDetalhes = it
                onFormDataChange(mapOf("dificuldade_espontaneo_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Houve mudança na energia
        Text(
            text = "Houve mudança na energia? (perda ou aumento):",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = mudancaEnergia,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = mudancaEnergiaDetalhes,
            onValueChange = { 
                mudancaEnergiaDetalhes = it
                onFormDataChange(mapOf("mudanca_energia_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Houve mudança no apetite
        Text(
            text = "Houve mudança no apetite? (perda ou aumento):",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = mudancaApetite,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = mudancaApetiteDetalhes,
            onValueChange = { 
                mudancaApetiteDetalhes = it
                onFormDataChange(mapOf("mudanca_apetite_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Houve mudança no peso
        Text(
            text = "Houve mudança no peso? (perda ou aumento):",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = mudancaPeso,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = mudancaPesoDetalhes,
            onValueChange = { 
                mudancaPesoDetalhes = it
                onFormDataChange(mapOf("mudanca_peso_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Houve mudança no interesse sexual
        Text(
            text = "Houve mudança no interesse sexual? (perda ou aumento):",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = mudancaInteresseSexual,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = mudancaInteresseSexualDetalhes,
            onValueChange = { 
                mudancaInteresseSexualDetalhes = it
                onFormDataChange(mapOf("mudanca_interesse_sexual_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Houve falta de interesse em atividades prazerosas
        Text(
            text = "Houve falta de interesse em atividades prazerosas ?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = faltaInteresseAtividades,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = faltaInteresseAtividadesDetalhes,
            onValueChange = { 
                faltaInteresseAtividadesDetalhes = it
                onFormDataChange(mapOf("falta_interesse_atividades_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Houve aumento de irritabilidade
        Text(
            text = "Houve aumento de irritabilidade?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = aumentoIrritabilidade,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = aumentoIrritabilidadeDetalhes,
            onValueChange = { 
                aumentoIrritabilidadeDetalhes = it
                onFormDataChange(mapOf("aumento_irritabilidade_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Outras mudanças no humor
        Text(
            text = "Outras mudanças no humor, personalidade ou em como lida com as pessoas?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = outrasMudancasHumor,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = outrasMudancasHumorDetalhes,
            onValueChange = { 
                outrasMudancasHumorDetalhes = it
                onFormDataChange(mapOf("outras_mudancas_humor_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Passando por algum problema matrimonial/familiar
        Text(
            text = "Passando por algum problema matrimonial/familiar?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = problemasMatrimoniais,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = problemasMatrimoniaisDetalhes,
            onValueChange = { 
                problemasMatrimoniaisDetalhes = it
                onFormDataChange(mapOf("problemas_matrimoniais_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Passando por algum problema financeiro/jurídico
        Text(
            text = "Passando por algum problema financeiro/jurídico?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = problemasFinanceiros,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = problemasFinanceirosDetalhes,
            onValueChange = { 
                problemasFinanceirosDetalhes = it
                onFormDataChange(mapOf("problemas_financeiros_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Passando por algum problema serviços domésticos
        Text(
            text = "Passando por algum problema serviços domésticos/ gerenciamento de dinheiro?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = problemasServicosDomesticos,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = problemasServicosDomesticosDetalhes,
            onValueChange = { 
                problemasServicosDomesticosDetalhes = it
                onFormDataChange(mapOf("problemas_servicos_domesticos_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun HistoriaFamiliaIdosoContent(
    formData: Map<String, String>,
    onFormDataChange: (Map<String, String>) -> Unit
) {
    var quantosIrmaos by remember { mutableStateOf(TextFieldValue(formData["quantos_irmaos"] ?: "")) }
    var problemasComuns by remember { mutableStateOf(formData["problemas_comuns"] ?: "Não") }
    var problemasComunsDetalhes by remember { mutableStateOf(TextFieldValue(formData["problemas_comuns_detalhes"] ?: "")) }
    var relacaoFamilia by remember { mutableStateOf(TextFieldValue(formData["relacao_familia"] ?: "")) }
    var estadoCivil by remember { mutableStateOf(TextFieldValue(formData["estado_civil"] ?: "")) }
    var anosCasado by remember { mutableStateOf(TextFieldValue(formData["anos_casado"] ?: "")) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Quantos irmãos
        Text(
            text = "Quantos irmãos o paciente tem?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = quantosIrmaos,
            onValueChange = { 
                quantosIrmaos = it
                onFormDataChange(mapOf("quantos_irmaos" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Tem algum problema em comum
        Text(
            text = "Tem algum problema em comum (físico, acadêmico, psicológico) associado com algum dos seus irmãos?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = problemasComuns,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = problemasComunsDetalhes,
            onValueChange = { 
                problemasComunsDetalhes = it
                onFormDataChange(mapOf("problemas_comuns_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Relação com a família
        Text(
            text = "Relação com a família:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = relacaoFamilia,
            onValueChange = { 
                relacaoFamilia = it
                onFormDataChange(mapOf("relacao_familia" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        // Estado civil
        Text(
            text = "Estado civil:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = estadoCivil,
            onValueChange = { 
                estadoCivil = it
                onFormDataChange(mapOf("estado_civil" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Quantos anos de casado
        Text(
            text = "Quantos anos de casado(a) tem?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = anosCasado,
            onValueChange = { 
                anosCasado = it
                onFormDataChange(mapOf("anos_casado" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun HistoricoProfissionalIdosoContent(
    formData: Map<String, String>,
    onFormDataChange: (Map<String, String>) -> Unit
) {
    var trabalhaAtualmente by remember { mutableStateOf(formData["trabalha_atualmente"] ?: "Não") }
    var trabalhaAtualmenteDetalhes by remember { mutableStateOf(TextFieldValue(formData["trabalha_atualmente_detalhes"] ?: "")) }
    var jaAposentou by remember { mutableStateOf(formData["ja_aposentou"] ?: "Não") }
    var jaAposentouDetalhes by remember { mutableStateOf(TextFieldValue(formData["ja_aposentou_detalhes"] ?: "")) }
    var cargoFuncao by remember { mutableStateOf(TextFieldValue(formData["cargo_funcao"] ?: "")) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // O paciente trabalha atualmente
        Text(
            text = "O paciente trabalha atualmente?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = trabalhaAtualmente,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = trabalhaAtualmenteDetalhes,
            onValueChange = { 
                trabalhaAtualmenteDetalhes = it
                onFormDataChange(mapOf("trabalha_atualmente_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // O paciente já se aposentou
        Text(
            text = "O paciente já se aposentou?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = jaAposentou,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = jaAposentouDetalhes,
            onValueChange = { 
                jaAposentouDetalhes = it
                onFormDataChange(mapOf("ja_aposentou_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Cargo ou função no trabalho
        Text(
            text = "Cargo ou função no trabalho:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = cargoFuncao,
            onValueChange = { 
                cargoFuncao = it
                onFormDataChange(mapOf("cargo_funcao" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun LazerIdosoContent(
    formData: Map<String, String>,
    onFormDataChange: (Map<String, String>) -> Unit
) {
    var tiposLazer by remember { mutableStateOf(TextFieldValue(formData["tipos_lazer"] ?: "")) }
    var aindaCapaz by remember { mutableStateOf(formData["ainda_capaz"] ?: "Não") }
    var aindaCapazDetalhes by remember { mutableStateOf(TextFieldValue(formData["ainda_capaz_detalhes"] ?: "")) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Resuma os tipos de lazer
        Text(
            text = "Resuma os tipos de lazer que o paciente gosta:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = tiposLazer,
            onValueChange = { 
                tiposLazer = it
                onFormDataChange(mapOf("tipos_lazer" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        // Ele ainda é capaz de realizar estas atividades
        Text(
            text = "Ele ainda é capaz de realizar estas atividades?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { },
        ) {
            OutlinedTextField(
                value = aindaCapaz,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                modifier = Modifier.menuAnchor()
            )
        }
        
        OutlinedTextField(
            value = aindaCapazDetalhes,
            onValueChange = { 
                aindaCapazDetalhes = it
                onFormDataChange(mapOf("ainda_capaz_detalhes" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun HipotesDiagnosticaIdosoContent(
    formData: Map<String, String>,
    onFormDataChange: (Map<String, String>) -> Unit
) {
    var descricao by remember { mutableStateOf(TextFieldValue(formData["descricao"] ?: "")) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Descrição:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedTextField(
            value = descricao,
            onValueChange = { 
                descricao = it
                onFormDataChange(mapOf("descricao" to it.text))
            },
            placeholder = { Text("Digite aqui") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 5
        )
    }
}