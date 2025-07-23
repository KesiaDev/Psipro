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
            PsiproTheme(useDarkTheme = isDarkModeActive(this)) {
                AnamneseScreen(
                    patientName = patientName,
                    patientId = patientId,
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
    onBack: () -> Unit
) {
    var expandedSections by remember { mutableStateOf(setOf<String>()) }
    var formData by remember { mutableStateOf(mutableMapOf<String, String>()) }
    
    val secoes = listOf(
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
                    text = "Grupo de anamnese: Adulto",
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
                    }
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
    onCancel: () -> Unit
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