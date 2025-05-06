package com.example.apppisc.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.apppisc.data.entities.Patient
import com.example.apppisc.ui.viewmodels.PatientViewModel
import com.example.apppisc.ui.viewmodels.PatientUiState

@Composable
fun PatientListScreen(
    onAddPatient: () -> Unit,
    onPatientClick: (Long) -> Unit,
    viewModel: PatientViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val patients by viewModel.patients.collectAsState(initial = emptyList())
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkPatientLimit()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { 
                searchQuery = it
                viewModel.searchPatients(it)
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar pacientes...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Warning messages
        when (uiState) {
            is PatientUiState.Warning -> {
                AlertDialog(
                    onDismissRequest = { },
                    title = { Text("Aviso") },
                    text = { Text((uiState as PatientUiState.Warning).message) },
                    confirmButton = {
                        TextButton(onClick = { }) {
                            Text("OK")
                        }
                    }
                )
            }
            is PatientUiState.Error -> {
                AlertDialog(
                    onDismissRequest = { },
                    title = { Text("Erro") },
                    text = { Text((uiState as PatientUiState.Error).message) },
                    confirmButton = {
                        TextButton(onClick = { }) {
                            Text("OK")
                        }
                    }
                )
            }
            else -> {}
        }

        // Patient list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(patients) { patient ->
                PatientItem(
                    patient = patient,
                    onClick = { onPatientClick(patient.id) }
                )
            }
        }

        // Add patient button
        FloatingActionButton(
            onClick = onAddPatient,
            modifier = Modifier
                .align(Alignment.End)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Adicionar paciente")
        }
    }
}

@Composable
fun PatientItem(
    patient: Patient,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = patient.name,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "CPF: ${patient.cpf}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
} 