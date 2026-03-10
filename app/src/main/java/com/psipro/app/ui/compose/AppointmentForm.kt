package com.psipro.app.ui.compose

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.psipro.app.R
import com.psipro.app.data.entities.Appointment
import com.psipro.app.data.entities.AppointmentStatus
import com.psipro.app.data.entities.AppointmentType
import com.psipro.app.data.entities.Patient
import com.psipro.app.viewmodel.AppointmentViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import com.psipro.app.ui.compose.BillingDialog
import com.psipro.app.ui.compose.BillingNotificationDialog

private data class StatusOption(val label: String, val color: Color)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentForm(
    onDismiss: () -> Unit,
    initialDate: LocalDate? = null,
    initialHour: Int? = null,
    patients: List<Patient> = emptyList(),
    viewModel: AppointmentViewModel,
    existingAppointment: Appointment? = null,
    preselectedPatient: Patient? = null,
    initialEventTypeIndex: Int = 0,
    onAppointmentCreated: ((LocalDate) -> Unit)? = null
) {
    val isEditing = existingAppointment != null

    var eventTypeIndex by remember { mutableStateOf(initialEventTypeIndex) }
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
    var selectedPatient by remember { mutableStateOf<Patient?>(preselectedPatient) }

    val statusOptions = listOf(
        StatusOption("Confirmado", Color(0xFF4CAF50)), // Verde
        StatusOption("Realizado", Color(0xFF2196F3)),  // Azul
        StatusOption("Faltou", Color(0xFFFF9800)),     // Laranja
        StatusOption("Cancelou", Color(0xFFF44336))    // Vermelho
    )

    val statusEnumMap = mapOf(
        0 to AppointmentStatus.CONFIRMADO,
        1 to AppointmentStatus.REALIZADO,
        2 to AppointmentStatus.FALTOU,
        3 to AppointmentStatus.CANCELOU
    )

    // Estados para integração financeira
    var showBillingDialog by remember { mutableStateOf(false) }
    var billingMessage by remember { mutableStateOf("") }
    var showConfirmBilling by remember { mutableStateOf(false) }
    
    // Observar mudanças no status para integração financeira
    LaunchedEffect(statusIndex) {
        val newStatus = statusEnumMap[statusIndex]
        if (newStatus != null && existingAppointment != null) {
            val oldStatus = existingAppointment.status
            if (newStatus != oldStatus) {
                when (newStatus) {
                    AppointmentStatus.CONFIRMADO -> {
                        billingMessage = "✅ Consulta/Reconsulta confirmada!\n💰 Valor a receber será gerado automaticamente."
                        showBillingDialog = true
                        showConfirmBilling = false
                    }
                    AppointmentStatus.REALIZADO -> {
                        billingMessage = "✅ Consulta realizada!\n💰 Cobrança será gerada automaticamente."
                        showBillingDialog = true
                        showConfirmBilling = false
                    }
                    AppointmentStatus.FALTOU -> {
                        billingMessage = "❌ Paciente faltou na consulta.\n💰 Deseja gerar cobrança pela falta?"
                        showBillingDialog = true
                        showConfirmBilling = true
                    }
                    AppointmentStatus.CANCELOU -> {
                        billingMessage = "❌ Consulta cancelada.\n💰 Deseja gerar cobrança pelo cancelamento?"
                        showBillingDialog = true
                        showConfirmBilling = true
                    }
                }
            }
        }
    }

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
            eventTypeIndex = when (existingAppointment.type) {
                AppointmentType.CONSULTA -> 0
                AppointmentType.RECONSULTA -> 1
                AppointmentType.PESSOAL -> 2
                else -> 0
            }
        }
    }

    val appointmentColors = listOf(
        "#2196F3", // Azul
        "#43A047", // Verde
        "#FBC02D", // Amarelo
        "#E53935", // Vermelho
        "#8E24AA", // Roxo
        "#F06292", // Rosa
        "#FF9800"  // Laranja
    )

    val context = LocalContext.current
    val bronzeGold = colorResource(id = R.color.bronze_gold)
    val bronzeDark = colorResource(id = R.color.bronze_gold_dark)
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
                            selectedDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
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
                    val isStartTime = showTimePicker == "start"
                    
                    // Criar lista de horários com intervalos de 30 minutos
                    val timeSlots = mutableListOf<String>()
                    for (hour in 8..21) {
                        timeSlots.add(String.format("%02d:00", hour))
                        timeSlots.add(String.format("%02d:30", hour))
                    }
                    
                    // Mostrar diálogo customizado para seleção de horário
                    TimeSlotPickerDialog(
                        timeSlots = timeSlots,
                        onTimeSelected = { selectedTime ->
                            if (isStartTime) {
                                startTime = selectedTime
                                // Auto-update end time (1 hora depois)
                                val timeParts = selectedTime.split(":")
                                val hour = timeParts[0].toInt()
                                val minute = timeParts[1].toInt()
                                
                                val endHour = if (minute == 30) hour + 1 else hour
                                val endMinute = if (minute == 30) 0 else 30
                                
                                endTime = String.format("%02d:%02d", endHour, endMinute)
                            } else {
                                endTime = selectedTime
                            }
                            showTimePicker = null
                        },
                        onDismiss = { showTimePicker = null }
                    )
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
                        if (selectedDate.isBlank()) {
                            android.widget.Toast.makeText(context, "Selecione uma data", android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (startTime.isBlank() || endTime.isBlank()) {
                            android.widget.Toast.makeText(context, "Selecione o horário de início e término", android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val parsedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(selectedDate)
                        if (parsedDate == null) {
                            android.widget.Toast.makeText(context, "Data inválida", android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val newOrUpdatedAppointment = Appointment(
                            id = if(isEditing) existingAppointment!!.id else 0,
                            title = if(title.isBlank() && selectedPatient != null) selectedPatient!!.name else title.ifBlank { "Consulta" },
                            description = description,
                            date = parsedDate,
                            startTime = startTime,
                            endTime = endTime,
                            patientId = selectedPatient?.id,
                            patientName = selectedPatient?.name ?: "Pessoal",

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
                            onSuccess = {
                                android.widget.Toast.makeText(context, "Agendamento confirmado!", android.widget.Toast.LENGTH_SHORT).show()
                                parsedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().let { date ->
                                    onAppointmentCreated?.invoke(date)
                                }
                                onDismiss()
                            },
                            onConflict = {
                                android.widget.Toast.makeText(context, "Já existe uma consulta neste horário", android.widget.Toast.LENGTH_LONG).show()
                            },
                            onError = { e ->
                                android.widget.Toast.makeText(context, "Erro: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                            }
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
    
    // Diálogo de cobrança
    if (showBillingDialog) {
        if (showConfirmBilling) {
            BillingDialog(
                message = billingMessage,
                onConfirm = {
                    showBillingDialog = false
                    // Aqui você pode adicionar lógica para gerar cobrança
                },
                onDismiss = {
                    showBillingDialog = false
                },
                showConfirmButton = true
            )
        } else {
            BillingNotificationDialog(
                message = billingMessage,
                onDismiss = {
                    showBillingDialog = false
                }
            )
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

@Composable
fun TimeSlotPickerDialog(
    timeSlots: List<String>,
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Selecionar Horário",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(timeSlots) { timeSlot ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onTimeSelected(timeSlot) },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = timeSlot,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(16.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                }
            }
        }
    }
}




