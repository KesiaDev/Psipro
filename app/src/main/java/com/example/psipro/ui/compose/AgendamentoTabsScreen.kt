package com.example.psipro.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight

@Composable
fun AgendamentoTabsScreen() {
    // Usar cores do tema Material3
    val selectedTabColor = MaterialTheme.colorScheme.primary
    val unselectedTabColor = Color.Transparent
    val selectedTextColor = MaterialTheme.colorScheme.onPrimary
    val unselectedTextColor = MaterialTheme.colorScheme.primary

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Sessão Única", "Sessões Recorrentes", "Compromisso Pessoal")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Botões horizontais
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            tabs.forEachIndexed { index, label ->
                Button(
                    onClick = { selectedTab = index },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == index) selectedTabColor else unselectedTabColor,
                        contentColor = if (selectedTab == index) selectedTextColor else unselectedTextColor
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                ) {
                    Text(label, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Conteúdo de acordo com a aba selecionada
        when (selectedTab) {
            0 -> SessaoUnicaForm()
            1 -> SessoesRecorrentesForm()
            2 -> CompromissoPessoalForm()
        }
    }
}

@Composable
fun SessaoUnicaForm() {
    // Coloque aqui os campos do formulário de sessão única
    Text("Formulário Sessão Única", color = MaterialTheme.colorScheme.onBackground)
}

@Composable
fun SessoesRecorrentesForm() {
    // Coloque aqui os campos do formulário de sessões recorrentes
    Text("Formulário Sessões Recorrentes", color = MaterialTheme.colorScheme.onBackground)
}

@Composable
fun CompromissoPessoalForm() {
    // Coloque aqui os campos do formulário de compromisso pessoal
    Text("Formulário Compromisso Pessoal", color = MaterialTheme.colorScheme.onBackground)
} 