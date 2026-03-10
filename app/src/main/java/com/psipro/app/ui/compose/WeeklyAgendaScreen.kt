package com.psipro.app.ui.compose

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.psipro.app.R
import com.psipro.app.data.entities.Appointment
import com.psipro.app.data.entities.AppointmentType
import com.psipro.app.data.entities.Patient
import com.psipro.app.ui.schedule.ScheduleViewModel
import com.psipro.app.viewmodel.AppointmentViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.max
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.zIndex
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset

val hourBoxHeight = 70.dp
val thinLine = 0.8.dp
val daysOfWeek = listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom")
val dayFontSize = 12.sp
val agendaColor: Color @Composable get() = MaterialTheme.colorScheme.primary
val thinLineColor: Color @Composable get() {
    return MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyAgendaScreen(
    appointments: List<Appointment>,
    appointmentViewModel: AppointmentViewModel,
    preselectedPatient: Patient? = null,
    tipoConsulta: String? = null
) {
    val scheduleViewModel: ScheduleViewModel = viewModel()
    val patients by scheduleViewModel.patients.collectAsState()

    val showAppointmentForm = remember { mutableStateOf(false) }
    val selectedDateForForm = remember { mutableStateOf<LocalDate?>(null) }
    val selectedHourForForm = remember { mutableStateOf<Int?>(null) }
    val editingAppointment = remember { mutableStateOf<Appointment?>(null) }
    
    // Se há um paciente pré-selecionado, abrir automaticamente o formulário
    LaunchedEffect(preselectedPatient) {
        preselectedPatient?.let { patient ->
            selectedDateForForm.value = LocalDate.now()
            selectedHourForForm.value = 9 // Hora padrão
            showAppointmentForm.value = true
        }
    }

    val onTimeSlotClicked = { date: LocalDate, hour: Int ->
        editingAppointment.value = null // Garante que não estamos editando
        selectedDateForForm.value = date
        selectedHourForForm.value = hour
        showAppointmentForm.value = true
    }

    val onAppointmentClicked = { appointment: Appointment ->
        editingAppointment.value = appointment
        showAppointmentForm.value = true
    }

    var focusedDate by remember { mutableStateOf(LocalDate.now()) }
    var viewMode by remember { mutableStateOf("Semana") }
    val hours = (8..20).toList()
    val today = LocalDate.now()
    val menuOptions = listOf("Mês", "Semana", "4 dias", "Dia")

    var currentMonth by remember { mutableStateOf(focusedDate.withDayOfMonth(1)) }
    var expanded by remember { mutableStateOf(false) }

    val weekStart = focusedDate.with(DayOfWeek.MONDAY)
    val weekDaysCount = when (viewMode) {
        "Semana" -> 7  // Seg-Dom: inclui sábado e domingo para agendamentos aparecerem
        "4 dias" -> 4
        else -> 7
    }
    val weekEnd = weekStart.plusDays((weekDaysCount - 1).toLong())
    val rangeStart = when (viewMode) {
        "Semana", "4 dias" -> if (viewMode == "4 dias") focusedDate else weekStart
        else -> focusedDate
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header estilo web: título + subtitle
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.schedule_professional),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when (viewMode) {
                            "Semana" -> "Semana de ${rangeStart.dayOfMonth} a ${weekEnd.dayOfMonth} de ${rangeStart.month.getDisplayName(java.time.format.TextStyle.FULL_STANDALONE, Locale("pt", "BR"))}, ${rangeStart.year}"
                            "4 dias" -> "${rangeStart.format(DateTimeFormatter.ofPattern("d"))} a ${weekEnd.format(DateTimeFormatter.ofPattern("d MMM", Locale("pt", "BR")))}"
                            "Dia" -> focusedDate.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("pt", "BR"))).replaceFirstChar { it.uppercase() }
                            "Mês" -> currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("pt", "BR"))).replaceFirstChar { it.uppercase() }
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            Icons.Filled.DateRange,
                            contentDescription = "Visualização",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        menuOptions.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = option,
                                        color = if (viewMode == option) agendaColor else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    viewMode = option
                                    focusedDate = LocalDate.now()
                                    expanded = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = when(option) {
                                            "Mês" -> Icons.Filled.DateRange
                                            else -> Icons.Filled.Event
                                        },
                                        contentDescription = null,
                                        tint = if (viewMode == option) agendaColor else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        // Navegação + botão Novo Agendamento (estilo web)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val (navText, prevDate, nextDate) = when (viewMode) {
                "Semana" -> Triple(
                    "Semana",
                    focusedDate.minusWeeks(1),
                    focusedDate.plusWeeks(1)
                )
                "4 dias" -> Triple(
                    "${focusedDate.format(DateTimeFormatter.ofPattern("dd/MM"))} - ${focusedDate.plusDays(3).format(DateTimeFormatter.ofPattern("dd/MM"))}",
                    focusedDate.minusDays(4),
                    focusedDate.plusDays(4)
                )
                "Dia" -> Triple(
                    focusedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("pt", "BR"))).replaceFirstChar { it.uppercase() },
                    focusedDate.minusDays(1),
                    focusedDate.plusDays(1)
                )
                "Mês" -> Triple(
                    currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("pt", "BR"))).replaceFirstChar { it.uppercase() },
                    currentMonth.minusMonths(1),
                    currentMonth.plusMonths(1)
                )
                else -> Triple(viewMode, focusedDate, focusedDate)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(onClick = {
                    if (viewMode == "Mês") currentMonth = prevDate else focusedDate = prevDate
                }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Anterior", tint = MaterialTheme.colorScheme.primary)
                }
                TextButton(
                    onClick = {
                        focusedDate = LocalDate.now()
                        if (viewMode == "Mês") currentMonth = LocalDate.now().withDayOfMonth(1)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Hoje", fontWeight = FontWeight.Medium)
                }
                IconButton(onClick = {
                    if (viewMode == "Mês") currentMonth = nextDate else focusedDate = nextDate
                }) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Próximo", tint = MaterialTheme.colorScheme.primary)
                }
            }
            FilledTonalButton(
                onClick = {
                    selectedDateForForm.value = LocalDate.now()
                    selectedHourForForm.value = 9
                    editingAppointment.value = null
                    showAppointmentForm.value = true
                },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Novo agendamento", modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Novo Agendamento", style = MaterialTheme.typography.labelLarge)
            }
        }

        // Estrutura com cabeçalho fixo e conteúdo rolável
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Grid em Card (estilo web)
            if (viewMode in listOf("Semana", "4 dias")) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 20.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        WeekHeader(
                            startDate = when (viewMode) {
                                "Semana" -> weekStart
                                else -> focusedDate
                            },
                            numDays = weekDaysCount,
                            today = today
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            when (viewMode) {
                                "Semana" -> WeekViewContent(weekStart, weekDaysCount, hours, appointments, today, onTimeSlotClicked, onAppointmentClicked)
                                "4 dias" -> FourDayViewContent(focusedDate, hours, appointments, today, onTimeSlotClicked, onAppointmentClicked)
                                else -> {}
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 20.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        when (viewMode) {
                            "Mês" -> MonthView(appointments, currentMonth, onAppointmentClicked)
                            "Dia" -> DayView(focusedDate, hours, appointments, today, onTimeSlotClicked, onAppointmentClicked)
                            else -> {}
                        }
                    }
                }
            }
        }

        if (showAppointmentForm.value) {
            android.util.Log.d("AgendaDebug", "Exibindo AppointmentForm")
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .zIndex(10f)
            ) {
                AppointmentForm(
                    onDismiss = { showAppointmentForm.value = false },
                    initialDate = selectedDateForForm.value,
                    initialHour = selectedHourForForm.value,
                    patients = patients,
                    viewModel = appointmentViewModel,
                    existingAppointment = editingAppointment.value,
                    preselectedPatient = preselectedPatient,
                    initialEventTypeIndex = when (tipoConsulta) {
                        "Reconsulta" -> 1
                        "Pessoal" -> 2
                        else -> 0 // "Consulta" ou null
                    },
                    onAppointmentCreated = { date -> focusedDate = date }
                )
            }
        }
    }
}

@Composable
fun DayView(
    date: LocalDate,
    hours: List<Int>,
    appointments: List<Appointment>,
    today: LocalDate,
    onTimeSlotClick: (LocalDate, Int) -> Unit,
    onAppointmentClick: (Appointment) -> Unit
) {
    val firstHour = hours.minOrNull() ?: 8
    val lineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
    val isToday = date == today

    Column(modifier = Modifier.fillMaxWidth()) {
        // Header do dia (estilo WeekHeader)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp)
                .zIndex(1f)
        ) {
            Spacer(modifier = Modifier.width(48.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isToday) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                    .padding(vertical = 12.dp, horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = daysOfWeek[date.dayOfWeek.value - 1],
                    fontSize = 12.sp,
                    color = if (isToday) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Medium
                )
                Text(
                    text = date.dayOfMonth.toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isToday) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = date.format(DateTimeFormatter.ofPattern("MMMM", Locale("pt", "BR"))).replaceFirstChar { it.uppercase() },
                    fontSize = 11.sp,
                    color = if (isToday) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                hours.forEach { hour ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(hourBoxHeight)
                            .drawBehind {
                                drawLine(
                                    color = lineColor,
                                    start = Offset(0f, size.height - 1.dp.toPx()),
                                    end = Offset(size.width, size.height - 1.dp.toPx()),
                                    strokeWidth = 1f
                                )
                            }
                            .clickable { onTimeSlotClick(date, hour) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "%02d:00".format(hour),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            modifier = Modifier
                                .width(48.dp)
                                .padding(end = 2.dp),
                            textAlign = TextAlign.End
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            val appointmentsOnThisDay = appointments.filter {
                it.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() == date
            }

            appointmentsOnThisDay.forEach { appointment ->
                val cardModifier = Modifier
                    .zIndex(1f)
                    .clickable {
                        android.util.Log.d("AgendaDebug", "Clicou no agendamento: ${appointment.title}")
                        onAppointmentClick(appointment)
                    }
                AppointmentCard(
                    appointment = appointment,
                    firstHour = firstHour,
                    modifier = cardModifier,
                    isDayView = true
                )
            }
        }
    }
}

@Composable
fun FourDayView(
    startDate: LocalDate,
    hours: List<Int>,
    appointments: List<Appointment>,
    today: LocalDate,
    onTimeSlotClick: (LocalDate, Int) -> Unit,
    onAppointmentClick: (Appointment) -> Unit
) {
    AgendaViewContent(
        numDays = 4,
        startDate = startDate,
        hours = hours,
        appointments = appointments,
        today = today,
        onTimeSlotClick = onTimeSlotClick,
        onAppointmentClick = onAppointmentClick
    )
}

@Composable
fun WeekHeader(
    startDate: LocalDate,
    numDays: Int = 5,
    today: LocalDate = LocalDate.now()
) {
    val days = remember(startDate, numDays) { 
        (0 until numDays).map { startDate.plusDays(it.toLong()) } 
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp)
            .zIndex(1f)
    ) {
        // Espaço para alinhar com a coluna das horas
        Spacer(modifier = Modifier.width(48.dp))
        
        days.forEach { date ->
            val isToday = date == today
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isToday) MaterialTheme.colorScheme.primary
                        else Color.Transparent
                    )
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = daysOfWeek[date.dayOfWeek.value - 1],
                    fontSize = 11.sp,
                    color = if (isToday) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Medium
                )
                Text(
                    text = date.dayOfMonth.toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isToday) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
fun WeekViewContent(
    startDate: LocalDate,
    numDays: Int,
    hours: List<Int>,
    appointments: List<Appointment>,
    today: LocalDate,
    onTimeSlotClick: (LocalDate, Int) -> Unit,
    onAppointmentClick: (Appointment) -> Unit
) {
    AgendaViewContent(
        numDays = numDays,
        startDate = startDate,
        hours = hours,
        appointments = appointments,
        today = today,
        onTimeSlotClick = onTimeSlotClick,
        onAppointmentClick = onAppointmentClick
    )
}

@Composable
fun FourDayViewContent(
    startDate: LocalDate,
    hours: List<Int>,
    appointments: List<Appointment>,
    today: LocalDate,
    onTimeSlotClick: (LocalDate, Int) -> Unit,
    onAppointmentClick: (Appointment) -> Unit
) {
    AgendaViewContent(
        numDays = 4,
        startDate = startDate,
        hours = hours,
        appointments = appointments,
        today = today,
        onTimeSlotClick = onTimeSlotClick,
        onAppointmentClick = onAppointmentClick
    )
}

@Composable
fun AppointmentCard(
    appointment: Appointment,
    firstHour: Int,
    modifier: Modifier,
    isDayView: Boolean = false,
    widthFraction: Float = 1f,
    horizontalOffset: Float = 0f,
    /** Hora da linha atual (usado em Semana/4 dias). Quando informado, o offset é relativo à linha. */
    rowStartHour: Int? = null
) {
    val startTime = parseTime(appointment.startTime)
    val endTime = parseTime(appointment.endTime)

    val baseHour = rowStartHour ?: firstHour
    val minutesFromTop = ((startTime.hour - baseHour) * 60 + startTime.minute).toFloat()
    val durationInMinutes = ((endTime.hour * 60 + endTime.minute) - (startTime.hour * 60 + startTime.minute)).toFloat()
    val clampedDuration = durationInMinutes.coerceAtLeast(15f)

    val cardOffsetY = hourBoxHeight * (minutesFromTop / 60f)
    val cardHeight = hourBoxHeight * (clampedDuration / 60f) - 2.dp

    val titleText = if (appointment.type == AppointmentType.PESSOAL) {
        appointment.title ?: "Compromisso Pessoal"
    } else {
        appointment.patientName ?: "Paciente"
    }
    val appointmentDesc = "Agendamento: $titleText das ${appointment.startTime} às ${appointment.endTime}. Toque para ver detalhes."
    val cardModifier = if (isDayView) {
        modifier
            .fillMaxWidth()
            .height(cardHeight)
            .offset(x = 70.dp, y = cardOffsetY)
            .padding(horizontal = 2.dp)
            .semantics { contentDescription = appointmentDesc }
    } else {
        modifier
            .fillMaxWidth(fraction = widthFraction)
            .height(cardHeight)
            .offset(y = cardOffsetY)
            .padding(start = (100 * horizontalOffset).dp * widthFraction)
            .semantics { contentDescription = appointmentDesc }
    }

    // Usar sempre a cor salva em colorHex
    val cardColor = Color(android.graphics.Color.parseColor(appointment.colorHex)).copy(alpha = 0.6f)

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
    ) {
        // Ajusta o tamanho da fonte e maxLines com base na visualização e duração
        val (dynamicFontSize, maxLines) = if (isDayView) {
            Pair(
                (14f + (clampedDuration / 60f)).coerceAtMost(18f).sp, // Fonte maior para DayView
                (clampedDuration / 20).toInt().coerceAtLeast(2)      // Mais linhas para DayView
            )
        } else {
            Pair(
                (10f + (clampedDuration / 45f)).coerceAtMost(14f).sp,
                (clampedDuration / 30).toInt().coerceAtLeast(1)
            )
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = titleText,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = dynamicFontSize,
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}

private fun parseTime(time: String): LocalTime {
    return try {
        LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
    } catch (e: Exception) {
        LocalTime.of(8, 0)
    }
}

@Composable
private fun AgendaViewContent(
    numDays: Int,
    startDate: LocalDate,
    hours: List<Int>,
    appointments: List<Appointment>,
    today: LocalDate,
    onTimeSlotClick: (LocalDate, Int) -> Unit,
    onAppointmentClick: (Appointment) -> Unit
) {
    val days = remember(numDays, startDate) { 
        (0 until numDays).map { startDate.plusDays(it.toLong()) } 
    }
    val firstHour = remember(hours) { hours.minOrNull() ?: 8 }
    val lineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)

    // Otimizar: pré-calcular appointments por data
    // Filtrar appointments apenas para os dias visíveis e converter corretamente
    val appointmentsByDate = remember(appointments, days) {
        val daysSet = days.toSet()
        appointments
            .filter { appointment ->
                // Converter Date para LocalDate de forma segura
                val appointmentDate = try {
                    appointment.date.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                } catch (e: Exception) {
                    android.util.Log.e("AgendaDebug", "Erro ao converter data: ${appointment.date}", e)
                    null
                }
                appointmentDate != null && appointmentDate in daysSet
            }
            .groupBy { appointment ->
                try {
                    appointment.date.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                } catch (e: Exception) {
                    android.util.Log.e("AgendaDebug", "Erro ao converter data no groupBy: ${appointment.date}", e)
                    LocalDate.now() // Fallback
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 0.dp)
    ) {
        hours.forEach { hour ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(hourBoxHeight)
                    .drawBehind {
                        drawLine(
                            color = lineColor,
                            start = Offset(0f, size.height - 1.dp.toPx()),
                            end = Offset(size.width, size.height - 1.dp.toPx()),
                            strokeWidth = 1f
                        )
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Coluna das horas (cinza suave como na web)
                Text(
                    "%02d:00".format(hour),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    modifier = Modifier
                        .width(48.dp)
                        .padding(end = 2.dp),
                    textAlign = TextAlign.End
                )
                
                // Colunas dos dias
                days.forEach { date ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onTimeSlotClick(date, hour) }
                    ) {
                        // Mostrar appointments para este horário e dia
                        appointmentsByDate[date]?.filter { appointment ->
                            val appointmentHour = parseTime(appointment.startTime).hour
                            appointmentHour == hour
                        }?.forEach { appointment ->
                            AppointmentCard(
                                appointment = appointment,
                                firstHour = firstHour,
                                modifier = Modifier.zIndex(1f),
                                widthFraction = 1f / numDays,
                                horizontalOffset = 0f, // Uma célula por dia; offset só para sobreposição no mesmo dia
                                rowStartHour = hour
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class AppointmentLayoutInfo(
    val appointment: Appointment,
    val width: Float,
    val xOffset: Float
)

private fun calculateAppointmentLayout(appointments: List<Appointment>): List<AppointmentLayoutInfo> {
    if (appointments.isEmpty()) return emptyList()
    
    val layouts = mutableListOf<AppointmentLayoutInfo>()
    val columns = mutableListOf<MutableList<Appointment>>()
    columns.add(mutableListOf())

    // Cache para parseTime para evitar recálculos
    val timeCache = mutableMapOf<String, LocalTime>()
    fun getCachedTime(time: String): LocalTime {
        return timeCache.getOrPut(time) { parseTime(time) }
    }

    for (appointment in appointments) {
        var placed = false
        for (col in columns) {
            if (col.isEmpty() || !overlapsWithCache(appointment, col.last()) { time -> getCachedTime(time) }) {
                col.add(appointment)
                placed = true
                break
            }
        }
        if (!placed) {
            columns.add(mutableListOf(appointment))
        }
    }

    val totalColumns = columns.size
    for ((colIndex, col) in columns.withIndex()) {
        for (appointment in col) {
            layouts.add(
                AppointmentLayoutInfo(
                    appointment = appointment,
                    width = 1f / totalColumns,
                    xOffset = colIndex.toFloat() / totalColumns
                )
            )
        }
    }

    return layouts
}

private fun overlapsWithCache(a: Appointment, b: Appointment, getTime: (String) -> LocalTime): Boolean {
    val startA = getTime(a.startTime)
    val endA = getTime(a.endTime)
    val startB = getTime(b.startTime)
    val endB = getTime(b.endTime)
    return startA.isBefore(endB) && startB.isBefore(endA)
}

@Composable
fun MonthView(
    appointments: List<Appointment>,
    currentMonth: LocalDate,
    onAppointmentClick: (Appointment) -> Unit
) {
    val today = LocalDate.now()
    val daysInMonth = remember(currentMonth) { currentMonth.lengthOfMonth() }
    val firstDayOfMonth = remember(currentMonth) { currentMonth.withDayOfMonth(1) }
    val startDayOfWeek = remember(firstDayOfMonth) { firstDayOfMonth.dayOfWeek.value }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    val appointmentsByDate = remember(appointments) {
        appointments.groupBy { 
            it.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() 
        }
    }

    val appointmentsForSelectedDay = remember(selectedDate, appointmentsByDate) {
        selectedDate?.let { date ->
            appointmentsByDate[date]?.sortedBy { parseTime(it.startTime) }
        } ?: emptyList()
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Header dos dias da semana (estilo WeekHeader)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Grid do calendário
        (0 until (daysInMonth + startDayOfWeek - 1) / 7 + 1).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                (0..6).forEach { day ->
                    val dayOfMonth = week * 7 + day - startDayOfWeek + 2
                    if (dayOfMonth > 0 && dayOfMonth <= daysInMonth) {
                        val date = currentMonth.withDayOfMonth(dayOfMonth)
                        val appointmentsOnDay = remember(date, appointmentsByDate) {
                            appointmentsByDate[date]?.size ?: 0
                        }
                        val isToday = date == today
                        val isSelected = date == selectedDate
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    }
                                )
                                .clickable { selectedDate = date },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = dayOfMonth.toString(),
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                if (appointmentsOnDay > 0) {
                                    Row(
                                        modifier = Modifier.padding(top = 2.dp),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        repeat(minOf(appointmentsOnDay, 3)) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(horizontal = 1.dp)
                                                    .size(4.dp)
                                                    .background(
                                                        if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                        else agendaColor,
                                                        CircleShape
                                                    )
                                            )
                                        }
                                        if (appointmentsOnDay > 3) {
                                            Text(
                                                text = "+${appointmentsOnDay - 3}",
                                                fontSize = 8.sp,
                                                color = when {
                                                    isSelected -> MaterialTheme.colorScheme.onPrimary
                                                    isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Lista de agendamentos do dia selecionado (cards modernos)
        if (selectedDate != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Agendamentos de ${selectedDate?.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("pt", "BR")))?.replaceFirstChar { it.uppercase() } ?: ""}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            if (appointmentsForSelectedDay.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "Nenhum agendamento para este dia.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(appointmentsForSelectedDay) { appointment ->
                        val cardColor = Color(android.graphics.Color.parseColor(appointment.colorHex)).copy(alpha = 0.25f)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAppointmentClick(appointment) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = appointment.patientName ?: "Paciente",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${appointment.startTime} - ${appointment.endTime}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    Icons.Filled.ChevronRight,
                                    contentDescription = "Ver detalhes",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Dummy function to avoid breaking changes if it was used somewhere else.
private fun saveThemePreference(context: Context, theme: String) {
    // No-op
}

private fun loadThemePreference(context: Context): String {
    return "light" // or "dark"
} 



