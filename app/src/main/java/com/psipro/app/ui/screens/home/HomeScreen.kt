package com.psipro.app.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.psipro.app.data.entities.AppointmentStatus
import com.psipro.app.ui.viewmodels.home.HomeViewModel


/* =========================================================
   HOME SCREEN
   ========================================================= */

@Composable
fun HomeScreen(
    onNavigateToAppointment: (Long) -> Unit,
    onNavigateToSessionNote: (Long) -> Unit,
    onNavigateToWhatsApp: (String) -> Unit,
    onNavigateToFinancial: () -> Unit,
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
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
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
            QuickActionsRow(
                onNewAppointment = onNavigateToSchedule,
                onNewPatient = {},
                onFinancial = onNavigateToFinancial
            )
        }
    }
}

/* =========================================================
   COMPONENTES
   ========================================================= */

@Composable
fun HomeHeader(greeting: String, currentDate: String) {
    Column {
        Text(greeting, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(currentDate, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun MainStateCard(title: String, message: String) {
    Card {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(message)
        }
    }
}

@Composable
fun NextAppointmentCard(
    appointment: AppointmentUi,
    onNavigate: () -> Unit
) {
    Card(
        modifier = Modifier.clickable(onClick = onNavigate)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Próxima sessão", style = MaterialTheme.typography.labelMedium)
            Text(appointment.patientName, fontWeight = FontWeight.Bold)
            Text(appointment.time)
        }
    }
}

@Composable
fun AppointmentCard(
    appointment: AppointmentUi,
    onConfirmRealized: () -> Unit
) {
    Card {
        Column(Modifier.padding(16.dp)) {
            Text(appointment.patientName, fontWeight = FontWeight.Bold)
            Text(appointment.time)

            if (appointment.status == AppointmentStatus.CONFIRMADO) {
                Spacer(Modifier.height(8.dp))
                Button(onClick = onConfirmRealized) {
                    Text("Marcar como realizada")
                }
            }
        }
    }
}

@Composable
fun SummaryCardsRow(summary: HomeSummary) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SummaryCard("Hoje", summary.todaySessionsCount.toString(), Icons.Default.Event) }
        item { SummaryCard("Pendentes", summary.sessionsWithoutNoteCount.toString(), Icons.Default.Note) }
        item { SummaryCard("A receber", summary.pendingPaymentsCount.toString(), Icons.Default.AttachMoney) }
    }
}

@Composable
fun SummaryCard(title: String, value: String, icon: ImageVector) {
    Card(modifier = Modifier.width(120.dp)) {
        Column(Modifier.padding(12.dp)) {
            Icon(icon, contentDescription = title)
            Text(value, fontWeight = FontWeight.Bold)
            Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun QuickActionsRow(
    onNewAppointment: () -> Unit,
    onNewPatient: () -> Unit,
    onFinancial: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = onNewAppointment) { Text("Nova sessão") }
        Button(onClick = onFinancial) { Text("Financeiro") }
    }
}

@Composable
fun ErrorCard(message: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f))) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            color = Color.Red
        )
    }
}
