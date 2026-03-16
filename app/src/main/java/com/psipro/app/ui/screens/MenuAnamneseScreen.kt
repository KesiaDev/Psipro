package com.psipro.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Note
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

// Enum para as seções da anamnese
enum class SecaoAnamneseType(val titulo: String) {
    DADOS_PESSOAIS("Dados Pessoais"),
    HISTORICO_FAMILIAR("Histórico Familiar"),
    HISTORICO_MEDICO("Histórico Médico"),
    VIDA_EMOCIONAL("Vida Emocional"),
    OBSERVACOES_CLINICAS("Observações Clínicas"),
    ANOTACOES_SESSAO("Anotações da Sessão")
}

// Dados do paciente
data class PacienteInfo(
    val nome: String,
    val idade: Int
)

// Seção da anamnese
data class SecaoAnamnese(
    val tipo: SecaoAnamneseType,
    val icone: @Composable () -> Unit,
    val preenchido: Boolean
)

@Composable
fun MenuAnamneseScreen(
    paciente: PacienteInfo,
    secoes: List<SecaoAnamnese>,
    onSecaoClick: (SecaoAnamnese) -> Unit,
    onBack: () -> Unit
) {
    val bronze = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val surface = MaterialTheme.colorScheme.surface

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // TopBar
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.semantics { 
                    contentDescription = "Voltar para a tela anterior" 
                }
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = bronze)
            }
            Spacer(Modifier.width(8.dp))
            Text("Anamnese", style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp), color = bronze)
        }
        Spacer(Modifier.height(8.dp))
        // Card Dados do Paciente
        Card(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(paciente.nome, style = MaterialTheme.typography.titleMedium)
                Text("${paciente.idade} anos", style = MaterialTheme.typography.bodySmall, color = onSurface.copy(alpha = 0.7f))
            }
        }
        Spacer(Modifier.height(16.dp))
        // Lista de seções
        Column(Modifier.padding(horizontal = 16.dp)) {
            secoes.forEach { secao ->
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onSecaoClick(secao) }
                        .semantics { 
                            contentDescription = "Seção ${secao.tipo.titulo}${if (secao.preenchido) ", preenchida" else ", não preenchida"}" 
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = surface)
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        secao.icone()
                        Spacer(Modifier.width(12.dp))
                        Text(secao.tipo.titulo, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                        if (secao.preenchido) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Seção preenchida",
                                tint = bronze,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Exemplo de uso dos ícones (pode ser customizado com SVGs ou drawables próprios)
fun getSecoesExemplo(): List<SecaoAnamnese> = listOf(
    SecaoAnamnese(SecaoAnamneseType.DADOS_PESSOAIS, { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }, true),
    SecaoAnamnese(SecaoAnamneseType.HISTORICO_FAMILIAR, { Icon(Icons.Default.FamilyRestroom, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }, true),
    SecaoAnamnese(SecaoAnamneseType.HISTORICO_MEDICO, { Icon(Icons.Default.MedicalServices, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }, true),
    SecaoAnamnese(SecaoAnamneseType.VIDA_EMOCIONAL, { Icon(Icons.Default.SelfImprovement, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }, false),
    SecaoAnamnese(SecaoAnamneseType.OBSERVACOES_CLINICAS, { Icon(Icons.Default.Assignment, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }, false),
    SecaoAnamnese(SecaoAnamneseType.ANOTACOES_SESSAO, { Icon(Icons.Default.Note, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }, false)
)

@Preview(showBackground = true)
@Composable
fun PreviewMenuAnamneseScreen() {
    val paciente = PacienteInfo(nome = "João da Silva", idade = 30)
    val secoes = getSecoesExemplo()
    MenuAnamneseScreen(
        paciente = paciente,
        secoes = secoes,
        onSecaoClick = { secao -> /* Navegar para a seção */ },
        onBack = { /* Voltar */ }
    )
} 



