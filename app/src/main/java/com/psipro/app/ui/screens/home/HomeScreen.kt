package com.psipro.app.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.psipro.app.data.entities.AppointmentStatus
import com.psipro.app.ui.components.PsiproCard
import com.psipro.app.ui.theme.psipro.PsiproSpacing
import com.psipro.app.ui.viewmodels.home.HomeViewModel


/* =========================================================
   HOME SCREEN
   ========================================================= */

@Composable
fun HomeScreen(
    onNavigateToAppointment: (Long) -> Unit,
    onNavigateToSessionNote: (Long) -> Unit,
    onNavigateToWhatsApp: (String) -> Unit,
    onNavigateToSchedule: () -> Unit,
    onConfirmAppointmentRealized: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(PsiproSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(PsiproSpacing.md)
    ) {
        /* HEADER */
        item {
            HomeHeader(
                greeting = uiState.greeting,
                currentDate = uiState.currentDate
            )
        }

        /* CARD PRINCIPAL */
        if (!uiState.isLoading) {

            uiState.nextAppointment?.let { appointment ->
                item {
                    NextAppointmentCard(
                        appointment = appointment,
                        onNavigate = { onNavigateToAppointment(appointment.id) }
                    )
                }
            }

            if (uiState.nextAppointment == null && uiState.todayAppointments.isEmpty()) {
                item {
                    MainStateCard(
                        title = "Você não tem atendimentos hoje",
                        message = "Aproveite o dia!"
                    )
                }
            }
        }

        /* RESUMO */
        if (!uiState.isLoading) {
            item {
                SummaryCardsRow(uiState.summary)
            }
        }

        /* LOADING */
        if (uiState.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        /* ERRO */
        uiState.error?.let {
            item { ErrorCard(it) }
        }

        /* ATENDIMENTOS */
        items(uiState.todayAppointments) { appointment ->
            AppointmentCard(
                appointment = appointment,
                onConfirmRealized = { onConfirmAppointmentRealized(appointment.id) }
            )
        }

        /* AÇÕES RÁPIDAS */
        item {
            QuickActionsRow(onNewAppointment = onNavigateToSchedule)
        }
    }
}

/* =========================================================
   COMPONENTES
   ========================================================= */

@Composable
fun HomeHeader(greeting: String, currentDate: String) {
    Column(modifier = Modifier.padding(vertical = PsiproSpacing.sm)) {
        Text(
            greeting,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            currentDate,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MainStateCard(title: String, message: String) {
    PsiproCard {
        Column {
            Text(
                title,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(PsiproSpacing.xs))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun NextAppointmentCard(
    appointment: AppointmentUi,
    onNavigate: () -> Unit
) {
    PsiproCard(onClick = onNavigate) {
        Column {
            Text(
                "Próxima sessão",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(PsiproSpacing.xs))
            Text(
                appointment.patientName,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                appointment.time,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AppointmentCard(
    appointment: AppointmentUi,
    onConfirmRealized: () -> Unit
) {
    PsiproCard(
        modifier = Modifier.semantics {
            contentDescription = "Sessão do paciente ${appointment.patientName} às ${appointment.time}. ${if (appointment.status == AppointmentStatus.CONFIRMADO) "Confirmada. Botão para marcar como realizada." else "Status: ${appointment.status}"}"
        }
    ) {
        Column {
            Text(
                appointment.patientName,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                appointment.time,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (appointment.status == AppointmentStatus.CONFIRMADO) {
                Spacer(Modifier.height(PsiproSpacing.md))
                Button(
                    onClick = onConfirmRealized,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Marcar como realizada")
                }
            }
        }
    }
}

@Composable
fun SummaryCardsRow(summary: HomeSummary) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(PsiproSpacing.sm)) {
        item { SummaryCard("Hoje", summary.todaySessionsCount.toString(), Icons.Default.Event) }
        item { SummaryCard("Pendentes", summary.sessionsWithoutNoteCount.toString(), Icons.Default.Note) }
    }
}

@Composable
fun SummaryCard(title: String, value: String, icon: ImageVector) {
    PsiproCard(modifier = Modifier.width(130.dp)) {
        Column {
            Icon(
                icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(PsiproSpacing.xs))
            Text(
                value,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun QuickActionsRow(onNewAppointment: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(PsiproSpacing.sm),
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(
            onClick = onNewAppointment,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Event, contentDescription = "Nova sessão", modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Nova sessão")
        }
    }
}

@Composable
fun ErrorCard(message: String) {
    PsiproCard {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
    }
}
