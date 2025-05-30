package com.example.psipro.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.psipro.R
import androidx.compose.foundation.isSystemInDarkTheme
import android.app.DatePickerDialog
import android.app.TimePickerDialog as AndroidTimePickerDialog
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar
import androidx.compose.foundation.border
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.filled.Search

import java.text.NumberFormat
import java.util.Locale
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import com.example.psipro.ui.viewmodels.PatientViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentForm(
    initialPatientName: String = "",
    initialPatientPhone: String = "",
    initialDate: String = "",
    initialStartTime: String = "",
    initialEndTime: String = "",
    onSave: (
        notificacaoAtiva: Boolean,
        tempoAntecedencia: String,
        tipoAntecedencia: String,
        titulo: String,
        descricao: String,
        paciente: String,
        telefone: String,
        valor: String,
        data: String,
        horaInicio: String,
        horaFim: String
    ) -> Unit = { _, _, _, _, _, _, _, _, _, _, _ -> }
) {
    android.util.Log.d("AppointmentForm", "Dados recebidos no formulário: Nome=$initialPatientName, Telefone=$initialPatientPhone")
    
    val bronzeGold = colorResource(id = R.color.primary_bronze)
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF222222)
    val hintColor = if (isDark) Color(0xFFBBBBBB) else Color(0xFF999999)
    val cardColor = if (isDark) Color(0xFF232323) else Color.White
    val buttonTextColor = if (isDark) Color.Black else Color.White
    val colorOptions = listOf(
        Color(0xFFD4AF37), // bronze
        Color(0xFFE0C066), // amarelo
        Color(0xFFB0B0B0), // cinza
        Color(0xFFF5E9DA), // bege claro
        Color(0xFFE57373)  // vermelho claro
    )
    val context = LocalContext.current

    
    var titulo by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var paciente by remember { mutableStateOf(initialPatientName) }
    var telefone by remember { mutableStateOf(initialPatientPhone) }
    var valorRaw by remember { mutableStateOf("") }
    var valor by remember { mutableStateOf("") }
    var data by remember { mutableStateOf(initialDate) }
    var horaInicio by remember { mutableStateOf(initialStartTime) }
    var horaFim by remember { mutableStateOf(initialEndTime) }
    var recorrencia by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Não respondido") }
    var selectedColor by remember { mutableStateOf(colorOptions[0]) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePickerInicio by remember { mutableStateOf(false) }
    var showTimePickerFim by remember { mutableStateOf(false) }
    var showPatientDialog by remember { mutableStateOf(false) }
    var selectedPatientId by remember { mutableStateOf<Long?>(null) }
    var notificacaoAtiva by remember { mutableStateOf(false) }
    var tempoAntecedencia by remember { mutableStateOf("") }
    var tipoAntecedencia by remember { mutableStateOf("minutos") }

    val patientViewModel: PatientViewModel = viewModel()
    val patients by patientViewModel.patients.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            onDateChange = { year, month, day ->
                data = "%02d/%02d/%04d".format(day, month + 1, year)
                showDatePicker = false
            }
        )
    }
    if (showTimePickerInicio) {
        TimePickerDialog(
            onDismissRequest = { showTimePickerInicio = false },
            onTimeChange = { hour, minute ->
                horaInicio = "%02d:%02d".format(hour, minute)
                showTimePickerInicio = false
            }
        )
    }
    if (showTimePickerFim) {
        TimePickerDialog(
            onDismissRequest = { showTimePickerFim = false },
            onTimeChange = { hour, minute ->
                horaFim = "%02d:%02d".format(hour, minute)
                showTimePickerFim = false
            }
        )
    }

    if (showPatientDialog) {
        AlertDialog(
            onDismissRequest = { showPatientDialog = false },
            title = { Text("Selecionar paciente") },
            text = {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            patientViewModel.setSearchQuery(it)
                        },
                        label = { Text("Buscar...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Column {
                        patients.forEach { patient ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        paciente = patient.name
                                        telefone = patient.phone
                                        showPatientDialog = false
                                    }
                                    .padding(8.dp)
                            ) {
                                Text(patient.name, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(8.dp))
                                Text(patient.phone, color = Color.Gray)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPatientDialog = false }) {
                    Text("Fechar")
                }
            }
        )
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .padding(20.dp)
                .verticalScroll(scrollState)
        ) {
            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título da consulta", color = hintColor) },
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = bronzeGold,
                    unfocusedBorderColor = bronzeGold,
                    cursorColor = bronzeGold,
                    focusedLabelColor = hintColor,
                    unfocusedLabelColor = hintColor
                ),
                textStyle = TextStyle(color = textColor),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = descricao,
                onValueChange = { descricao = it },
                label = { Text("Descrição", color = hintColor) },
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = bronzeGold,
                    unfocusedBorderColor = bronzeGold,
                    cursorColor = bronzeGold,
                    focusedLabelColor = hintColor,
                    unfocusedLabelColor = hintColor
                ),
                textStyle = TextStyle(color = textColor),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = paciente,
                onValueChange = { },
                label = { Text("Nome do paciente", color = hintColor) },
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = bronzeGold,
                    unfocusedBorderColor = bronzeGold,
                    cursorColor = bronzeGold,
                    focusedLabelColor = hintColor,
                    unfocusedLabelColor = hintColor
                ),
                textStyle = TextStyle(color = textColor),
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showPatientDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Buscar paciente",
                            tint = bronzeGold
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = telefone,
                onValueChange = { },
                label = { Text("Telefone do paciente", color = hintColor) },
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = bronzeGold,
                    unfocusedBorderColor = bronzeGold,
                    cursorColor = bronzeGold,
                    focusedLabelColor = hintColor,
                    unfocusedLabelColor = hintColor
                ),
                textStyle = TextStyle(color = textColor),
                modifier = Modifier.fillMaxWidth(),
                readOnly = true
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = valor,
                    onValueChange = { newValue ->
                        val clean = newValue.replace("[^\\d]".toRegex(), "")
                        valorRaw = clean
                        valor = if (clean.isNotEmpty()) formatCurrencyBR(clean) else ""
                    },
                    label = { Text("Valor da sessão", color = hintColor) },
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = bronzeGold,
                        unfocusedBorderColor = bronzeGold,
                        cursorColor = bronzeGold,
                        focusedLabelColor = hintColor,
                        unfocusedLabelColor = hintColor
                    ),
                    textStyle = TextStyle(color = textColor),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    placeholder = { Text("R$ 0,00") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = data,
                    onValueChange = {},
                    label = { Text("Data", color = hintColor) },
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = bronzeGold,
                        unfocusedBorderColor = bronzeGold,
                        cursorColor = bronzeGold,
                        focusedLabelColor = hintColor,
                        unfocusedLabelColor = hintColor
                    ),
                    textStyle = TextStyle(color = textColor),
                    modifier = Modifier.weight(1f).clickable { showDatePicker = true },
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.AccessTime,
                            contentDescription = "Selecionar data",
                            tint = bronzeGold
                        )
                    }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = horaInicio,
                    onValueChange = {},
                    label = { Text("Hora de início", color = hintColor) },
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = bronzeGold,
                        unfocusedBorderColor = bronzeGold,
                        cursorColor = bronzeGold,
                        focusedLabelColor = hintColor,
                        unfocusedLabelColor = hintColor
                    ),
                    textStyle = TextStyle(color = textColor),
                    modifier = Modifier.weight(1f).clickable { showTimePickerInicio = true },
                    readOnly = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = horaFim,
                    onValueChange = {},
                    label = { Text("Hora de término", color = hintColor) },
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = bronzeGold,
                        unfocusedBorderColor = bronzeGold,
                        cursorColor = bronzeGold,
                        focusedLabelColor = hintColor,
                        unfocusedLabelColor = hintColor
                    ),
                    textStyle = TextStyle(color = textColor),
                    modifier = Modifier.weight(1f).clickable { showTimePickerFim = true },
                    readOnly = true
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            // Recorrência Dropdown
            var expanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = recorrencia,
                    onValueChange = {},
                    label = { Text("Recorrência", color = hintColor) },
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = bronzeGold,
                        unfocusedBorderColor = bronzeGold,
                        cursorColor = bronzeGold,
                        focusedLabelColor = hintColor,
                        unfocusedLabelColor = hintColor
                    ),
                    textStyle = TextStyle(color = textColor),
                    modifier = Modifier.fillMaxWidth().clickable { expanded = true },
                    readOnly = true,
                    trailingIcon = {
                        Icon(Icons.Filled.AccessTime, contentDescription = "Abrir opções", tint = bronzeGold)
                    }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    listOf("Não repetir", "Diária", "Semanal", "Mensal").forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                recorrencia = option
                                expanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            // Status e seleção de cor
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Status: ", color = textColor, fontWeight = FontWeight.Bold)
                Text(status, color = textColor)
                Spacer(modifier = Modifier.width(16.dp))
                colorOptions.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .padding(2.dp)
                            .background(color, CircleShape)
                            .clickable {
                                selectedColor = color
                                status = when (color) {
                                    colorOptions[0] -> "Não respondido"
                                    colorOptions[1] -> "Confirmado"
                                    colorOptions[2] -> "Remarcado"
                                    colorOptions[3] -> "Cancelado"
                                    colorOptions[4] -> "Faltou"
                                    else -> "Não respondido"
                                }
                            }
                            .border(
                                width = if (selectedColor == color) 2.dp else 0.dp,
                                color = if (selectedColor == color) bronzeGold else Color.Transparent,
                                shape = CircleShape
                            )
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            // Notificação de lembrete
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Aviso de notificação", color = textColor, modifier = Modifier.weight(1f))
                Switch(
                    checked = notificacaoAtiva,
                    onCheckedChange = { notificacaoAtiva = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = bronzeGold,
                        checkedTrackColor = bronzeGold.copy(alpha = 0.5f)
                    )
                )
            }
            if (notificacaoAtiva) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = tempoAntecedencia,
                        onValueChange = { novo ->
                            // Permite apenas números
                            if (novo.all { it.isDigit() }) tempoAntecedencia = novo
                        },
                        label = { Text("Tempo de antecedência", color = hintColor) },
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = bronzeGold,
                            unfocusedBorderColor = bronzeGold,
                            cursorColor = bronzeGold,
                            focusedLabelColor = hintColor,
                            unfocusedLabelColor = hintColor
                        ),
                        textStyle = TextStyle(color = textColor),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text("Ex: 30") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Dropdown para minutos/horas
                    var expandedTipo by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(onClick = { expandedTipo = true }) {
                            Text(tipoAntecedencia)
                        }
                        DropdownMenu(
                            expanded = expandedTipo,
                            onDismissRequest = { expandedTipo = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("minutos") },
                                onClick = {
                                    tipoAntecedencia = "minutos"
                                    expandedTipo = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("horas") },
                                onClick = {
                                    tipoAntecedencia = "horas"
                                    expandedTipo = false
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = {
                    onSave(
                        notificacaoAtiva,
                        tempoAntecedencia,
                        tipoAntecedencia,
                        titulo,
                        descricao,
                        paciente,
                        telefone,
                        valor,
                        data,
                        horaInicio,
                        horaFim
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = bronzeGold, contentColor = buttonTextColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("SALVAR", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateChange: (Int, Int, Int) -> Unit
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    DisposableEffect(Unit) {
        val dialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                onDateChange(year, month, dayOfMonth)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        dialog.setOnDismissListener { onDismissRequest() }
        dialog.show()
        onDispose { dialog.dismiss() }
    }
}

@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onTimeChange: (Int, Int) -> Unit
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    DisposableEffect(Unit) {
        val dialog = AndroidTimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                onTimeChange(hourOfDay, minute)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        dialog.setOnDismissListener { onDismissRequest() }
        dialog.show()
        onDispose { dialog.dismiss() }
    }
}

fun formatCurrencyBR(input: String): String {
    val cleanString = input.replace("[R$,.\\s]".toRegex(), "")
    if (cleanString.isEmpty()) return ""
    val parsed = cleanString.toDouble() / 100
    return NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(parsed)
} 