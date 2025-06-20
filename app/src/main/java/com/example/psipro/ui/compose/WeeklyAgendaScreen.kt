package com.example.psipro.ui.compose

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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.psipro.R
import com.example.psipro.data.entities.Appointment
import com.example.psipro.data.entities.Patient
import com.example.psipro.ui.schedule.ScheduleViewModel
import com.example.psipro.viewmodel.AppointmentViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

val hourBoxHeight = 70.dp
val thinLine = 0.8.dp
val daysOfWeek = listOf("SEG", "TER", "QUA", "QUI", "SEX", "SÁB", "DOM")
val dayFontSize = 12.sp
val bronzeGold: Color @Composable get() = colorResource(id = R.color.primary_bronze)
val textDark: Color @Composable get() = colorResource(id = R.color.text_dark)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyAgendaScreen(
    appointments: List<Appointment>
) {
    val scheduleViewModel: ScheduleViewModel = viewModel()
    val appointmentViewModel: AppointmentViewModel = viewModel()
    val patients by scheduleViewModel.patients.collectAsState()

    val showAppointmentForm = remember { mutableStateOf(false) }
    val selectedDateForForm = remember { mutableStateOf<LocalDate?>(null) }
    val selectedHourForForm = remember { mutableStateOf<Int?>(null) }
    val editingAppointment = remember { mutableStateOf<Appointment?>(null) }

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
    var menuExpanded by remember { mutableStateOf(false) }
    var viewMode by remember { mutableStateOf("Semana") }
    val hours = (8..22).toList()
    val today = LocalDate.now()
    val menuOptions = listOf("Mês", "Semana", "4 dias", "Dia")

    var currentMonth by remember { mutableStateOf(focusedDate.withDayOfMonth(1)) }
    var expanded by remember { mutableStateOf(false) }

    val weekStart = focusedDate.with(DayOfWeek.MONDAY)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("Agenda", color = bronzeGold) },
            actions = {
                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            Icons.Filled.DateRange,
                            contentDescription = "Selecionar visualização",
                            tint = bronzeGold
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
                                        color = if (viewMode == option) bronzeGold else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    viewMode = option
                                    focusedDate = LocalDate.now() // Reset focus to today when changing view
                                    expanded = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = when(option) {
                                            "Mês" -> Icons.Filled.DateRange
                                            else -> Icons.Filled.Event
                                        },
                                        contentDescription = null,
                                        tint = if (viewMode == option) bronzeGold else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            )
                        }
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val (navText, prevDate, nextDate) = when (viewMode) {
                "Semana" -> Triple(
                    "Semana de ${weekStart.format(DateTimeFormatter.ofPattern("dd/MM"))}",
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

            IconButton(onClick = {
                if (viewMode == "Mês") currentMonth = prevDate else focusedDate = prevDate
            }) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "Anterior", tint = bronzeGold)
            }
            Text(
                text = navText,
                color = bronzeGold,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            IconButton(onClick = {
                if (viewMode == "Mês") currentMonth = nextDate else focusedDate = nextDate
            }) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "Próximo", tint = bronzeGold)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            when (viewMode) {
                "Mês" -> MonthView(appointments, currentMonth)
                "Semana" -> WeekView(weekStart, hours, appointments, today, onTimeSlotClicked, onAppointmentClicked)
                "4 dias" -> FourDayView(focusedDate, hours, appointments, today, onTimeSlotClicked, onAppointmentClicked)
                "Dia" -> DayView(focusedDate, hours, appointments, today, onTimeSlotClicked, onAppointmentClicked)
            }
        }

        if (showAppointmentForm.value) {
            val initialHour = selectedHourForForm.value
            AppointmentForm(
                onDismiss = { showAppointmentForm.value = false },
                initialDate = selectedDateForForm.value,
                initialHour = initialHour,
                patients = patients,
                viewModel = appointmentViewModel,
                existingAppointment = editingAppointment.value
            )
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
    val thinLineColor = bronzeGold.copy(alpha = if (!isSystemInDarkTheme()) 0.18f else 0.28f)
    val firstHour = hours.minOrNull() ?: 8

    Box(modifier = Modifier.padding(horizontal = 8.dp)) {
        // 1. Desenha a grade de fundo clicável
        Column {
            hours.forEach { hour ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(hourBoxHeight)
                        .border(thinLine, thinLineColor)
                        .clickable { onTimeSlotClick(date, hour) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "%02d:00".format(hour),
                        color = bronzeGold,
                        fontSize = 10.sp,
                        modifier = Modifier
                            .width(64.dp)
                            .padding(end = 2.dp),
                        textAlign = TextAlign.End
                    )
                }
            }
        }

        // 2. Desenha os agendamentos por cima
        val appointmentsOnThisDay = appointments.filter {
            it.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() == date
        }

        appointmentsOnThisDay.forEach { appointment ->
            AppointmentCard(
                appointment = appointment,
                firstHour = firstHour,
                modifier = Modifier.clickable { onAppointmentClick(appointment) }
            )
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
    val thinLineColor = bronzeGold.copy(alpha = if (!isSystemInDarkTheme()) 0.18f else 0.28f)
    val firstHour = hours.minOrNull() ?: 8

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 2.dp)
        ) {
            Spacer(modifier = Modifier.width(44.dp))
            (0..3).forEach { dayIndex ->
                val currentDate = startDate.plusDays(dayIndex.toLong())
                val isToday = currentDate == today
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = daysOfWeek[currentDate.dayOfWeek.value - 1],
                        color = if (isToday) MaterialTheme.colorScheme.onSurface else bronzeGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = dayFontSize
                    )
                }
            }
        }
        Row(Modifier.fillMaxWidth()) {
            Column(Modifier.width(44.dp)) {
                hours.forEach { hour ->
                    Box(
                        Modifier.height(hourBoxHeight), contentAlignment = Alignment.CenterEnd) {
                        Text("%02d:00".format(hour), color = bronzeGold, fontSize = 10.sp, modifier = Modifier.padding(end = 4.dp))
                    }
                }
            }
            (0..3).forEach { dayIndex ->
                val currentDate = startDate.plusDays(dayIndex.toLong())
                Box(modifier = Modifier.weight(1f)) {
                    // 1. Desenha a grade de fundo clicável
                    Column(Modifier.fillMaxSize()) {
                        hours.forEach { hour ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(hourBoxHeight)
                                    .border(thinLine, thinLineColor)
                                    .clickable { onTimeSlotClick(currentDate, hour) }
                            )
                        }
                    }

                    // 2. Desenha os agendamentos por cima da grade
                    val appointmentsOnThisDay = appointments.filter {
                        it.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() == currentDate
                    }

                    appointmentsOnThisDay.forEach { appointment ->
                        AppointmentCard(
                            appointment = appointment,
                            firstHour = firstHour,
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { onAppointmentClick(appointment) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeekView(
    startDate: LocalDate,
    hours: List<Int>,
    appointments: List<Appointment>,
    today: LocalDate,
    onTimeSlotClick: (LocalDate, Int) -> Unit,
    onAppointmentClick: (Appointment) -> Unit
) {
    val thinLineColor = bronzeGold.copy(alpha = if (!isSystemInDarkTheme()) 0.18f else 0.28f)
    val firstHour = hours.minOrNull() ?: 8

    Column(modifier = Modifier.fillMaxSize()) {
        // Linha com cabeçalho dos dias da semana
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 2.dp)
        ) {
            Spacer(modifier = Modifier.width(44.dp))
            (0..6).forEach { dayIndex ->
                val currentDate = startDate.plusDays(dayIndex.toLong())
                val isToday = currentDate == today
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = daysOfWeek[dayIndex],
                        color = if (isToday) Color.White else bronzeGold,
                        modifier = if (isToday) Modifier
                            .background(bronzeGold, CircleShape)
                            .padding(horizontal = 4.dp) else Modifier
                    )
                }
            }
        }

        // Linha principal da grade
        Row(Modifier.fillMaxWidth()) {
            // Coluna de horários (8:00, 9:00, etc)
            Column(Modifier.width(44.dp)) {
                hours.forEach { hour ->
                    Box(
                        Modifier.height(hourBoxHeight), contentAlignment = Alignment.CenterEnd) {
                        Text("%02d:00".format(hour), color = bronzeGold, fontSize = 10.sp, modifier = Modifier.padding(end = 4.dp))
                    }
                }
            }

            // Colunas dos dias
            (0..6).forEach { dayIndex ->
                val currentDate = startDate.plusDays(dayIndex.toLong())
                Box(modifier = Modifier.weight(1f)) {
                    // 1. Desenha a grade de fundo clicável
                    Column(Modifier.fillMaxSize()) {
                        hours.forEach { hour ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(hourBoxHeight)
                                    .border(thinLine, thinLineColor)
                                    .clickable { onTimeSlotClick(currentDate, hour) }
                            )
                        }
                    }

                    // 2. Desenha os agendamentos por cima da grade
                    val appointmentsOnThisDay = appointments.filter {
                        it.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() == currentDate
                    }

                    appointmentsOnThisDay.forEach { appointment ->
                        AppointmentCard(
                            appointment = appointment,
                            firstHour = firstHour,
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { onAppointmentClick(appointment) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthView(
    appointments: List<Appointment>,
    currentMonth: LocalDate
) {
    // Placeholder for month view implementation
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Visualização de Mês em breve.")
    }
}

@Composable
fun AppointmentCard(
    appointment: Appointment,
    firstHour: Int,
    modifier: Modifier
) {
    val startTime = parseTime(appointment.startTime)
    val endTime = parseTime(appointment.endTime)

    val minutesFromTop = ((startTime.hour - firstHour) * 60 + startTime.minute).toFloat()
    val durationInMinutes = ((endTime.hour * 60 + endTime.minute) - (startTime.hour * 60 + startTime.minute)).toFloat()
    val clampedDuration = durationInMinutes.coerceAtLeast(15f)

    val cardOffsetY = hourBoxHeight * (minutesFromTop / 60f)
    val cardHeight = hourBoxHeight * (clampedDuration / 60f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight)
            .offset(x = 70.dp, y = cardOffsetY) // Offset para não sobrepor os horários
            .padding(horizontal = 2.dp),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(android.graphics.Color.parseColor(appointment.colorHex))
        ),
    ) {
        Text(
            text = appointment.title,
            color = Color.Black,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(4.dp)
                .align(Alignment.CenterHorizontally)
        )
    }
}

// Dummy function to avoid breaking changes if it was used somewhere else.
private fun saveThemePreference(context: Context, theme: String) {
    // No-op
}

private fun loadThemePreference(context: Context): String {
    return "light" // or "dark"
}

private fun parseTime(time: String): LocalTime {
    return try {
        LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
    } catch (e: Exception) {
        LocalTime.MIDNIGHT // Retorna um valor padrão em caso de erro
    }
} 