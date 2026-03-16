package com.psipro.app.ui.screens.patients

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.psipro.app.data.entities.Patient
import com.psipro.app.ui.components.PsiproButton
import com.psipro.app.ui.components.PsiproCard
import com.psipro.app.ui.components.PsiproEmptyState
import com.psipro.app.ui.components.PsiproFloatingButton
import com.psipro.app.ui.components.PsiproInput
import com.psipro.app.ui.theme.psipro.LocalSpacingScale
import com.psipro.app.ui.theme.psipro.PsiproColors
import com.psipro.app.ui.theme.psipro.PsiproSpacing
import java.text.SimpleDateFormat
import java.util.Locale

private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

@Composable
fun PatientsScreen(
    patients: List<Patient>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onPatientClick: (Patient) -> Unit,
    onScheduleClick: (Patient) -> Unit,
    onAddPatient: () -> Unit
) {
    val spacingScale = LocalSpacingScale.current
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(PsiproSpacing.screenPadding * spacingScale)
        ) {
            PsiproInput(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = "Buscar pacientes..."
            )

            if (patients.isEmpty()) {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
                PsiproEmptyState(
                    title = if (searchQuery.isNotEmpty()) "Nenhum paciente encontrado" else "Nenhum paciente cadastrado",
                    subtitle = if (searchQuery.isNotEmpty())
                        "Tente buscar com outro termo"
                    else
                        "Toque no botão + para cadastrar seu primeiro paciente",
                    icon = Icons.Outlined.PersonAdd,
                    action = {
                        if (searchQuery.isEmpty()) {
                            PsiproButton(
                                text = "Cadastrar paciente",
                                onClick = onAddPatient
                            )
                        }
                    }
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = PsiproSpacing.md * spacingScale,
                        bottom = PsiproSpacing.xxl * spacingScale
                    ),
                    verticalArrangement = Arrangement.spacedBy(PsiproSpacing.listItemSpacing * spacingScale)
                ) {
                    items(
                        items = patients,
                        key = { it.id }
                    ) { patient ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically(),
                            exit = fadeOut() + slideOutVertically()
                        ) {
                            PatientCard(
                                patient = patient,
                                onClick = { onPatientClick(patient) },
                                onScheduleClick = { onScheduleClick(patient) }
                            )
                        }
                    }
                }
            }
        }

        PsiproFloatingButton(
            onClick = onAddPatient,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(PsiproSpacing.md * spacingScale)
        )
    }
}

@Composable
private fun PatientCard(
    patient: Patient,
    onClick: () -> Unit,
    onScheduleClick: () -> Unit
) {
    PsiproCard(onClick = onClick) {
        Column {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            PsiproColors.Primary.copy(alpha = 0.2f),
                            MaterialTheme.shapes.medium
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = patient.name.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = PsiproSpacing.md)
                ) {
                    Text(
                        text = patient.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (patient.phone.isNotEmpty() || patient.email.isNotEmpty()) {
                        Text(
                            text = listOfNotNull(
                                patient.phone.takeIf { it.isNotEmpty() },
                                patient.email.takeIf { it.isNotEmpty() }
                            ).joinToString(" • "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                androidx.compose.material3.IconButton(onClick = onScheduleClick) {
                    Icon(
                        imageVector = Icons.Filled.Event,
                        contentDescription = "Agendar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
