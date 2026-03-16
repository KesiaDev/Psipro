package com.psipro.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.Composable

/**
 * Empty state: Nenhum paciente cadastrado.
 */
@Composable
fun EmptyPatients(
    onAction: () -> Unit
) {
    PsiproEmptyState(
        title = "Nenhum paciente cadastrado",
        subtitle = "Toque no botão + para cadastrar seu primeiro paciente",
        icon = Icons.Outlined.Person,
        action = { PsiproButton(text = "Cadastrar paciente", onClick = onAction) }
    )
}

/**
 * Empty state: Agenda vazia.
 */
@Composable
fun EmptyAgenda(
    onAction: () -> Unit
) {
    PsiproEmptyState(
        title = "Nenhuma consulta agendada",
        subtitle = "Toque no botão + para agendar sua primeira consulta",
        icon = Icons.Outlined.CalendarMonth,
        action = { PsiproButton(text = "Nova consulta", onClick = onAction) }
    )
}

/**
 * Empty state: Nenhuma sessão.
 */
@Composable
fun EmptySessions(
    onAction: () -> Unit
) {
    PsiproEmptyState(
        title = "Nenhuma sessão registrada",
        subtitle = "As sessões aparecerão aqui após serem realizadas",
        icon = Icons.Outlined.EventBusy,
        action = { PsiproButton(text = "Nova sessão", onClick = onAction) }
    )
}
