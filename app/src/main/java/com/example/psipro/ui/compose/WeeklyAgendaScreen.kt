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
import androidx.compose.ui.draw.clip
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
import com.example.psipro.data.entities.AppointmentType
import com.example.psipro.data.entities.Patient
import com.example.psipro.ui.schedule.ScheduleViewModel
import com.example.psipro.viewmodel.AppointmentViewModel
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

val hourBoxHeight = 70.dp
val thinLine = 0.8.dp
val daysOfWeek = listOf("SEG", "TER", "QUA", "QUI", "SEX", "SÁB", "DOM")
val dayFontSize = 12.sp
val bronzeGold: Color @Composable get() = colorResource(id = R.color.bronze_gold)
val agendaColor: Color @Composable get() = colorResource(id = R.color.bronze_gold)
val thinLineColor: Color @Composable get() {
    return agendaColor.copy(alpha = 0.12f)
}

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
            title = { Text("Agenda") },
            actions = {
                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            Icons.Filled.DateRange,
                            contentDescription = "Selecionar visualização"
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
                                        tint = if (viewMode == option) agendaColor else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            )
                        }
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = agendaColor,
                actionIconContentColor = agendaColor
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

            IconButton(onClick = {
                if (viewMode == "Mês") currentMonth = prevDate else focusedDate = prevDate
            }) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "Anterior", tint = agendaColor)
            }
            Text(
                text = navText,
                color = agendaColor,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            IconButton(onClick = {
                if (viewMode == "Mês") currentMonth = nextDate else focusedDate = nextDate
            }) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "Próximo", tint = agendaColor)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            when (viewMode) {
                "Mês" -> MonthView(appointments, currentMonth, onAppointmentClicked)
                "Semana" -> WeekView(weekStart, hours, appointments, today, onTimeSlotClicked, onAppointmentClicked)
                "4 dias" -> FourDayView(focusedDate, hours, appointments, today, onTimeSlotClicked, onAppointmentClicked)
                "Dia" -> DayView(focusedDate, hours, appointments, today, onTimeSlotClicked, onAppointmentClicked)
            }
        }

        if (showAppointmentForm.value) {
            AppointmentForm(
                onDismiss = { showAppointmentForm.value = false },
                initialDate = selectedDateForForm.value,
                initialHour = selectedHourForForm.value,
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
    val firstHour = hours.minOrNull() ?: 8

    Box(modifier = Modifier.padding(horizontal = 8.dp)) {
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
                        color = agendaColor,
                        fontSize = 10.sp,
                        modifier = Modifier
                            .width(64.dp)
                            .padding(end = 2.dp),
                        textAlign = TextAlign.End
                    )
                }
            }
        }

        val appointmentsOnThisDay = appointments.filter {
            it.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() == date
        }

        appointmentsOnThisDay.forEach { appointment ->
            val cardModifier = Modifier.clickable { onAppointmentClick(appointment) }
            AppointmentCard(
                appointment = appointment,
                firstHour = firstHour,
                modifier = cardModifier,
                isDayView = true
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
    AgendaView(
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
fun WeekView(
    startDate: LocalDate,
    hours: List<Int>,
    appointments: List<Appointment>,
    today: LocalDate,
    onTimeSlotClick: (LocalDate, Int) -> Unit,
    onAppointmentClick: (Appointment) -> Unit
) {
    AgendaView(
        numDays = 7,
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
    horizontalOffset: Float = 0f
) {
    val startTime = parseTime(appointment.startTime)
    val endTime = parseTime(appointment.endTime)

    val minutesFromTop = ((startTime.hour - firstHour) * 60 + startTime.minute).toFloat()
    val durationInMinutes = ((endTime.hour * 60 + endTime.minute) - (startTime.hour * 60 + startTime.minute)).toFloat()
    val clampedDuration = durationInMinutes.coerceAtLeast(15f)

    val cardOffsetY = hourBoxHeight * (minutesFromTop / 60f)
    val cardHeight = hourBoxHeight * (clampedDuration / 60f) - 2.dp

    val cardModifier = if (isDayView) {
        modifier
            .fillMaxWidth()
            .height(cardHeight)
            .offset(x = 70.dp, y = cardOffsetY)
            .padding(horizontal = 2.dp)
    } else {
        modifier
            .fillMaxWidth(fraction = widthFraction)
            .height(cardHeight)
            .offset(y = cardOffsetY)
            .padding(start = (100 * horizontalOffset).dp * widthFraction)
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
        val title = if (appointment.type == AppointmentType.PESSOAL) {
            appointment.title
        } else {
            appointment.patientName
        }

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
                text = title,
                color = Color.Black,
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
private fun AgendaView(
    numDays: Int,
    startDate: LocalDate,
    hours: List<Int>,
    appointments: List<Appointment>,
    today: LocalDate,
    onTimeSlotClick: (LocalDate, Int) -> Unit,
    onAppointmentClick: (Appointment) -> Unit
) {
    val days = (0 until numDays).map { startDate.plusDays(it.toLong()) }
    val firstHour = hours.minOrNull() ?: 8

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        // Coluna das Horas
        Column {
            Spacer(modifier = Modifier.height(30.dp))
            hours.forEach { hour ->
                Text(
                    "%02d:00".format(hour),
                    color = agendaColor,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .height(hourBoxHeight)
                        .width(48.dp)
                        .padding(end = 4.dp),
                    textAlign = TextAlign.End
                )
            }
        }

        // Colunas dos dias
        days.forEach { date ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Cabeçalho do Dia
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = daysOfWeek[date.dayOfWeek.value - 1],
                        fontSize = dayFontSize,
                        color = if (date == today) MaterialTheme.colorScheme.primary else agendaColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                    )
                    Text(
                        text = date.dayOfMonth.toString(),
                        fontSize = 10.sp,
                        color = if (date == today) MaterialTheme.colorScheme.primary else agendaColor,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Corpo do Dia
                Box {
                    Column {
                        hours.forEach { hour ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(hourBoxHeight)
                                    .border(thinLine, thinLineColor)
                                    .clickable { onTimeSlotClick(date, hour) }
                            )
                        }
                    }

                    val appointmentsOnThisDay = appointments
                        .filter { it.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() == date }
                        .sortedBy { parseTime(it.startTime) }

                    val appointmentLayouts = calculateAppointmentLayout(appointmentsOnThisDay)

                    appointmentLayouts.forEach { layoutInfo ->
                        Box(modifier = Modifier.fillMaxWidth(layoutInfo.width).offset(x = (100 * layoutInfo.xOffset).dp * layoutInfo.width)) {
                            AppointmentCard(
                                appointment = layoutInfo.appointment,
                                firstHour = firstHour,
                                modifier = Modifier.clickable { onAppointmentClick(layoutInfo.appointment) }
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
    val layouts = mutableListOf<AppointmentLayoutInfo>()
    if (appointments.isEmpty()) return layouts

    val columns = mutableListOf<MutableList<Appointment>>()
    columns.add(mutableListOf())

    for (appointment in appointments) {
        var placed = false
        for (col in columns) {
            if (col.isEmpty() || !overlaps(appointment, col.last())) {
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

private fun overlaps(a: Appointment, b: Appointment): Boolean {
    val startA = parseTime(a.startTime)
    val endA = parseTime(a.endTime)
    val startB = parseTime(b.startTime)
    val endB = parseTime(b.endTime)
    return startA.isBefore(endB) && startB.isBefore(endA)
}

@Composable
fun MonthView(
    appointments: List<Appointment>,
    currentMonth: LocalDate,
    onAppointmentClick: (Appointment) -> Unit
) {
    val context = LocalContext.current
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.withDayOfMonth(1)
    val startDayOfWeek = firstDayOfMonth.dayOfWeek.value
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    val appointmentsForSelectedDay = remember(selectedDate) {
        appointments.filter {
            it.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() == selectedDate
        }.sortedBy { parseTime(it.startTime) }
    }

    Column(modifier = Modifier.padding(8.dp)) {
        // Calendário
        Column {
            // Header
            Row(modifier = Modifier.fillMaxWidth()) {
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                        color = agendaColor
                    )
                }
            }
            // Dias
            (0 until (daysInMonth + startDayOfWeek - 1) / 7 + 1).forEach { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    (0..6).forEach { day ->
                        val dayOfMonth = week * 7 + day - startDayOfWeek + 2
                        if (dayOfMonth > 0 && dayOfMonth <= daysInMonth) {
                            val date = currentMonth.withDayOfMonth(dayOfMonth)
                            val appointmentsOnDay = appointments.count { it.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() == date }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(
                                        width = 2.dp,
                                        color = when {
                                            date == selectedDate -> MaterialTheme.colorScheme.primary
                                            date == LocalDate.now() -> agendaColor
                                            else -> Color.Transparent
                                        },
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .clickable { selectedDate = date },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = dayOfMonth.toString())
                                if (appointmentsOnDay > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = 4.dp)
                                            .size(6.dp)
                                            .background(agendaColor, CircleShape)
                                    )
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Lista de agendamentos para o dia selecionado
        if (selectedDate != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Agendamentos para ${selectedDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp),
                color = agendaColor
            )
            if (appointmentsForSelectedDay.isEmpty()) {
                Text("Nenhum agendamento para este dia.")
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    items(appointmentsForSelectedDay) { appointment ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onAppointmentClick(appointment) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = appointment.patientName, fontWeight = FontWeight.Bold)
                                    Text(text = "Horário: ${appointment.startTime} - ${appointment.endTime}", fontSize = 14.sp)
                                }
                                Icon(Icons.Filled.ChevronRight, contentDescription = "Ver detalhes")
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