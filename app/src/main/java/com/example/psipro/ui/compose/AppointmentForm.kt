package com.example.psipro.ui.compose

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.psipro.R
import com.example.psipro.data.entities.Appointment
import com.example.psipro.data.entities.AppointmentStatus
import com.example.psipro.data.entities.AppointmentType
import com.example.psipro.data.entities.Patient
import com.example.psipro.viewmodel.AppointmentViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

private data class StatusOption(val label: String, val color: Color)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentForm(
    onDismiss: () -> Unit,
    initialDate: LocalDate? = null,
    initialHour: Int? = null,
    patients: List<Patient> = emptyList(),
    viewModel: AppointmentViewModel,
    existingAppointment: Appointment? = null
) {
    val isEditing = existingAppointment != null

    var eventTypeIndex by remember { mutableStateOf(0) }
    val eventTypes = listOf("Consulta", "Reconsulta", "Pessoal")
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember {
        mutableStateOf(
            initialDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: ""
        )
    }
    var startTime by remember {
        mutableStateOf(
            initialHour?.let { String.format("%02d:00", it) } ?: ""
        )
    }
    var endTime by remember {
        mutableStateOf(
            initialHour?.let { String.format("%02d:00", it + 1) } ?: ""
        )
    }
    var statusIndex by remember { mutableStateOf(0) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf<String?>(null) } // "start" or "end"

    var patientDropdownExpanded by remember { mutableStateOf(false) }
    var selectedPatient by remember { mutableStateOf<Patient?>(null) }

    val statusOptions = listOf(
        StatusOption("Pendente", Color(0xFFD4AF37)),
        StatusOption("Confirmado", Color(0xFF4CAF50)),
        StatusOption("Cancelado", Color(0xFFF44336)),
        StatusOption("Realizado", Color(0xFF2196F3)),
        StatusOption("Faltou", Color(0xFF9E9E9E))
    )

    val statusEnumMap = mapOf(
        0 to AppointmentStatus.SCHEDULED,
        1 to AppointmentStatus.COMPLETED, // Usando COMPLETED para "Confirmado"
        2 to AppointmentStatus.CANCELLED,
        3 to AppointmentStatus.COMPLETED,
        4 to AppointmentStatus.NO_SHOW
    )

    LaunchedEffect(existingAppointment) {
        if (existingAppointment != null) {
            title = existingAppointment.title
            description = existingAppointment.description ?: ""
            val zonedDateTime = existingAppointment.date.toInstant().atZone(ZoneId.systemDefault())
            selectedDate = zonedDateTime.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            startTime = existingAppointment.startTime
            endTime = existingAppointment.endTime
            selectedPatient = patients.find { it.id == existingAppointment.patientId }
            statusIndex = statusEnumMap.entries.find { it.value == existingAppointment.status }?.key ?: 0
        }
    }

    val appointmentColors = listOf(
        "#FFAB91", "#FFCC80", "#FFE082", "#A5D6A7", "#80CBC4", "#81D4FA", "#CE93D8"
    )

    val bronzeGold = colorResource(id = R.color.primary_bronze)
    val bronzeDark = colorResource(id = R.color.bronze_dark)
    val customTextFieldColors = TextFieldDefaults.outlinedTextFieldColors(
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        cursorColor = bronzeGold,
        focusedBorderColor = bronzeDark,
        unfocusedBorderColor = bronzeDark.copy(alpha = 0.7f),
        focusedLabelColor = bronzeDark,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .padding(vertical = 32.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SegmentedButton(
                    items = eventTypes,
                    selectedIndex = eventTypeIndex,
                    onSelectedIndexChange = { eventTypeIndex = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título do evento") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = customTextFieldColors,
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = customTextFieldColors,
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (eventTypes[eventTypeIndex] != "Pessoal") {
                    ExposedDropdownMenuBox(
                        expanded = patientDropdownExpanded,
                        onExpandedChange = { patientDropdownExpanded = !patientDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedPatient?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Paciente") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = patientDropdownExpanded) },
                            colors = customTextFieldColors,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = patientDropdownExpanded,
                            onDismissRequest = { patientDropdownExpanded = false }
                        ) {
                            patients.forEach { patient ->
                                DropdownMenuItem(
                                    text = { Text(patient.name) },
                                    onClick = {
                                        selectedPatient = patient
                                        patientDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = {},
                    label = { Text("Data") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().clickable(onClick = { showDatePicker = true }),
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(imageVector = Icons.Default.DateRange, contentDescription = "Selecionar data")
                        }
                    },
                    colors = customTextFieldColors,
                    shape = RoundedCornerShape(8.dp)
                )

                if (showDatePicker) {
                    val context = LocalContext.current
                    val calendar = Calendar.getInstance()
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            selectedDate = "$dayOfMonth/${month + 1}/$year"
                            showDatePicker = false
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { },
                        label = { Text("Hora de início") },
                        readOnly = true,
                        modifier = Modifier.weight(1f).clickable { showTimePicker = "start" },
                        colors = customTextFieldColors,
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { },
                        label = { Text("Hora de término") },
                        readOnly = true,
                        modifier = Modifier.weight(1f).clickable { showTimePicker = "end" },
                        colors = customTextFieldColors,
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                if (showTimePicker != null) {
                    val context = LocalContext.current
                    val calendar = Calendar.getInstance()
                    val isStartTime = showTimePicker == "start"
                    TimePickerDialog(
                        context,
                        { _, hour, minute ->
                            val time = String.format("%02d:%02d", hour, minute)
                            if (isStartTime) {
                                startTime = time
                                // Auto-update end time
                                val endCalendar = Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, hour)
                                    set(Calendar.MINUTE, minute)
                                    add(Calendar.HOUR_OF_DAY, 1)
                                }
                                endTime = String.format("%02d:%02d", endCalendar.get(Calendar.HOUR_OF_DAY), endCalendar.get(Calendar.MINUTE))
                            } else {
                                endTime = time
                            }
                            showTimePicker = null
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                    ).show()
                }

                Spacer(modifier = Modifier.height(24.dp))

                StatusSelector(
                    statusOptions = statusOptions,
                    selectedIndex = statusIndex,
                    onSelectedIndexChange = { statusIndex = it }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val newOrUpdatedAppointment = Appointment(
                            id = if(isEditing) existingAppointment!!.id else 0,
                            title = if(title.isBlank() && selectedPatient != null) selectedPatient!!.name else title,
                            description = description,
                            date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(selectedDate) ?: Date(),
                            startTime = startTime,
                            endTime = endTime,
                            patientId = selectedPatient?.id,
                            patientName = selectedPatient?.name ?: "Pessoal",
                            status = statusEnumMap[statusIndex] ?: AppointmentStatus.SCHEDULED,
                            colorHex = existingAppointment?.colorHex ?: appointmentColors.random(),
                            patientPhone = selectedPatient?.phone ?: "",
                            type = when (eventTypes[eventTypeIndex]) {
                                "Consulta" -> AppointmentType.CONSULTA
                                "Reconsulta" -> AppointmentType.RECONSULTA
                                else -> AppointmentType.PESSOAL
                            }
                        )

                        viewModel.addAppointment(
                            appointment = newOrUpdatedAppointment,
                            onSuccess = { onDismiss() },
                            onConflict = { /* TODO: handle conflict */ },
                            onError = { /* TODO: handle error */ }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = bronzeGold)
                ) {
                    Text(text = if(isEditing) "Salvar Alterações" else "Agendar", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun StatusSelector(
    statusOptions: List<StatusOption>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Status: ", fontWeight = FontWeight.Bold)
            Text(
                text = statusOptions[selectedIndex].label,
                color = statusOptions[selectedIndex].color,
                fontWeight = FontWeight.Bold
            )
        }
        Row {
            statusOptions.forEachIndexed { index, option ->
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(option.color)
                        .clickable { onSelectedIndexChange(index) }
                        .then(
                            if (selectedIndex == index) {
                                Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            } else Modifier
                        )
                )
            }
        }
    }
}

@Composable
fun SegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .height(IntrinsicSize.Min)
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = selectedIndex == index
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelectedIndexChange(index) }
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun SegmentedButton(
    items: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .height(IntrinsicSize.Min)
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = selectedIndex == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelectedIndexChange(index) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
