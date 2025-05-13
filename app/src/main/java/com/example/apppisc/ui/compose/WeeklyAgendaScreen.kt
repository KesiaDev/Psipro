package com.example.apppisc.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import com.example.apppisc.data.entities.Appointment

@Composable
fun WeeklyAgendaScreen(
    appointments: List<Appointment>,
    initialWeekStart: LocalDate = LocalDate.now().with(DayOfWeek.MONDAY),
    onAddEvent: () -> Unit = {},
    onTimeSlotClick: (date: LocalDate, hour: Int) -> Unit = { _, _ -> }
) {
    var weekStart by remember { mutableStateOf(initialWeekStart) }
    var menuExpanded by remember { mutableStateOf(false) }
    var viewMode by remember { mutableStateOf("Semana") }
    val hours = (8..22).toList()
    val daysOfWeek = listOf("SEG", "TER", "QUA", "QUI", "SEX", "SÁB", "DOM")
    val today = LocalDate.now()
    val menuOptions = listOf("Mês", "Semana", "4 dias", "Dia")
    val bronzeGold = Color(0xFFB08D57)
    val backgroundDark = Color(0xFF181818)
    val thinLine = 0.2.dp
    val thinLineColor = bronzeGold.copy(alpha = 0.5f)
    val hourBoxHeight = 36.dp
    val hourFontSize = 10.sp
    val nameFontSize = 10.sp
    val dayFontSize = 12.sp
    val headerFontSize = 18.sp
    val visibleHours = (8..20).toList() // Horários visíveis sem rolagem
    val extraHours = (21..22).toList() // Horários que aparecem só ao rolar
    val totalHours = (8..22).toList()
    val maxVisibleRows = visibleHours.size
    val maxGridHeight = (hourBoxHeight * maxVisibleRows) + 28.dp // 28dp para o header
    var currentMonth by remember { mutableStateOf(weekStart.withDayOfMonth(1)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundDark)
    ) {
        // Cabeçalho com menu suspenso sempre visível
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundDark)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (viewMode == "Semana") {
                IconButton(onClick = { weekStart = weekStart.minusWeeks(1) }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Semana anterior", tint = bronzeGold)
                }
            } else {
                Spacer(modifier = Modifier.width(40.dp))
            }
            Text(
                text = when(viewMode) {
                    "Semana" -> weekStart.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("pt", "BR"))).replaceFirstChar { it.uppercase() }
                    "Mês" -> "Mês"
                    else -> viewMode
                },
                color = bronzeGold,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            // Botão do menu suspenso
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Selecionar visualização", tint = bronzeGold)
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(Color(0xFF232323))
                ) {
                    menuOptions.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(option, color = if (viewMode == option) bronzeGold else Color.White)
                            },
                            onClick = {
                                viewMode = option
                                menuExpanded = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = when(option) {
                                        "Mês" -> Icons.Filled.Event
                                        "Semana" -> Icons.Filled.DateRange
                                        "4 dias" -> Icons.Filled.Event
                                        "Dia" -> Icons.Filled.Event
                                        else -> Icons.Filled.Event
                                    },
                                    contentDescription = null,
                                    tint = if (viewMode == option) bronzeGold else Color.White
                                )
                            }
                        )
                    }
                }
            }
            if (viewMode == "Semana") {
                IconButton(onClick = { weekStart = weekStart.plusWeeks(1) }) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Próxima semana", tint = bronzeGold)
                }
            } else {
                Spacer(modifier = Modifier.width(40.dp))
            }
        }

        // Conteúdo de acordo com o modo de visualização
        when(viewMode) {
            "Semana" -> {
                // Dias da semana
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(backgroundDark)
                        .padding(bottom = 2.dp)
                ) {
                    Spacer(modifier = Modifier.width(44.dp)) // Alinha com coluna de horários
                    for (i in 0..6) {
                        val date = weekStart.plusDays(i.toLong())
                        val isToday = date == today
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = daysOfWeek[i],
                                color = if (isToday) backgroundDark else bronzeGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = dayFontSize,
                                modifier = if (isToday) Modifier
                                    .background(bronzeGold, shape = CircleShape)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                else Modifier
                            )
                            Text(
                                text = date.dayOfMonth.toString(),
                                color = if (isToday) backgroundDark else bronzeGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = dayFontSize
                            )
                        }
                    }
                }
                // Grade de horários
                Box(
                    modifier = Modifier
                        .heightIn(max = maxGridHeight)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        // Coluna de horários
                        Column(
                            modifier = Modifier.width(44.dp)
                        ) {
                            Spacer(modifier = Modifier.height(28.dp))
                            for (hour in totalHours) {
                                Text(
                                    text = "%02d:00".format(hour),
                                    color = bronzeGold,
                                    fontSize = hourFontSize,
                                    modifier = Modifier
                                        .height(hourBoxHeight)
                                        .padding(end = 2.dp),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                        // Colunas dos dias
                        for (i in 0..6) {
                            val date = weekStart.plusDays(i.toLong())
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Spacer(modifier = Modifier.height(28.dp))
                                for (hour in totalHours) {
                                    val appointment = appointments.find { appt ->
                                        val apptDate = appt.date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                                        val apptHour = appt.startTime.split(":")[0].toIntOrNull() ?: -1
                                        apptDate == date && apptHour == hour
                                    }
                                    Box(
                                        modifier = Modifier
                                            .height(hourBoxHeight)
                                            .fillMaxWidth()
                                            .border(thinLine, thinLineColor)
                                            .clickable { onTimeSlotClick(date, hour) }
                                    ) {
                                        if (appointment != null) {
                                            Text(
                                                text = appointment.patientName,
                                                color = Color.White,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = nameFontSize,
                                                modifier = Modifier.align(Alignment.CenterStart).padding(start = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "Mês" -> {
                // Topo com navegação de mês
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = "Mês anterior", tint = bronzeGold)
                    }
                    Text(
                        text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("pt", "BR"))).replaceFirstChar { it.uppercase() },
                        color = bronzeGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                        Icon(Icons.Filled.ChevronRight, contentDescription = "Próximo mês", tint = bronzeGold)
                    }
                }
                MonthView(
                    appointments = appointments,
                    currentMonth = currentMonth,
                    bronzeGold = bronzeGold,
                    backgroundDark = backgroundDark,
                    onDayClick = { }
                )
            }
            "4 dias" -> {
                FourDaysView(
                    appointments = appointments,
                    startDate = weekStart,
                    bronzeGold = bronzeGold,
                    backgroundDark = backgroundDark,
                    onTimeSlotClick = { date, hour, _ -> onTimeSlotClick(date, hour) }
                )
            }
            "Dia" -> {
                DayView(
                    appointments = appointments,
                    date = weekStart,
                    bronzeGold = bronzeGold,
                    backgroundDark = backgroundDark,
                    onTimeSlotClick = { date, hour, _ -> onTimeSlotClick(date, hour) }
                )
            }
            else -> {
                // Placeholder para outros modos
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundDark),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Visualização '$viewMode' em breve",
                        color = bronzeGold,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun MonthView(
    appointments: List<Appointment>,
    currentMonth: LocalDate,
    bronzeGold: Color,
    backgroundDark: Color,
    onDayClick: (LocalDate) -> Unit = {}
) {
    val firstDayOfMonth = currentMonth.withDayOfMonth(1)
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // 0=Domingo
    val weeks = ((daysInMonth + firstDayOfWeek - 1) / 7) + 1
    val daysOfWeek = listOf("DOM", "SEG", "TER", "QUA", "QUI", "SEX", "SÁB")
    Column(modifier = Modifier.fillMaxWidth().background(backgroundDark)) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            daysOfWeek.forEach { d ->
                Text(d, color = bronzeGold, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            }
        }
        var day = 1
        for (week in 0 until weeks) {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                for (dow in 0..6) {
                    if ((week == 0 && dow < firstDayOfWeek) || day > daysInMonth) {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f)) {}
                    } else {
                        val thisDate = currentMonth.withDayOfMonth(day)
                        val hasAppointment = appointments.any { appt ->
                            val apptDate = appt.date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                            apptDate == thisDate
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clickable { onDayClick(thisDate) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (hasAppointment) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(bronzeGold.copy(alpha = 0.7f), shape = CircleShape)
                                )
                            }
                            Text(
                                day.toString(),
                                color = if (hasAppointment) backgroundDark else bronzeGold,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                        }
                        day++
                    }
                }
            }
        }
    }
}

@Composable
fun FourDaysView(
    appointments: List<Appointment>,
    startDate: LocalDate,
    bronzeGold: Color,
    backgroundDark: Color,
    onTimeSlotClick: (date: LocalDate, hour: Int, appointment: Appointment?) -> Unit = { _, _, _ -> }
) {
    val thinLine = 0.2.dp
    val thinLineColor = bronzeGold.copy(alpha = 0.5f)
    val visibleHours = (8..20).toList()
    val extraHours = (21..22).toList()
    val totalHours = (8..22).toList()
    val hourBoxHeight = 36.dp
    val maxVisibleRows = visibleHours.size
    val maxGridHeight = (hourBoxHeight * maxVisibleRows) + 28.dp
    val daysOfWeek = listOf("SEG", "TER", "QUA", "QUI", "SEX", "SÁB", "DOM")
    Box(modifier = Modifier.fillMaxSize().background(backgroundDark)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Linha com nomes dos dias da semana
            Row(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.width(44.dp)) // espaço da coluna de horários
                for (i in 0..3) {
                    val date = startDate.plusDays(i.toLong())
                    val dow = daysOfWeek[date.dayOfWeek.value % 7]
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(dow, color = bronzeGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(date.dayOfMonth.toString(), color = bronzeGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
            Box(
                modifier = Modifier
                    .heightIn(max = maxGridHeight)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Coluna de horários
                    Column(modifier = Modifier.width(44.dp)) {
                        Spacer(modifier = Modifier.height(28.dp))
                        for (hour in totalHours) {
                            Text("%02d:00".format(hour), color = bronzeGold, fontSize = 10.sp, modifier = Modifier.height(hourBoxHeight).padding(end = 2.dp), textAlign = TextAlign.End)
                        }
                    }
                    // 4 dias
                    for (i in 0..3) {
                        val date = startDate.plusDays(i.toLong())
                        Column(modifier = Modifier.weight(1f)) {
                            Spacer(modifier = Modifier.height(28.dp))
                            for (hour in totalHours) {
                                val appointment = appointments.find { appt ->
                                    val apptDate = appt.date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                                    val apptHour = appt.startTime.split(":")[0].toIntOrNull() ?: -1
                                    apptDate == date && apptHour == hour
                                }
                                Box(modifier = Modifier.height(hourBoxHeight).fillMaxWidth().border(thinLine, thinLineColor).clickable { onTimeSlotClick(date, hour, appointment) }) {
                                    if (appointment != null) {
                                        Text(
                                            text = appointment.patientName,
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 10.sp,
                                            modifier = Modifier.align(Alignment.CenterStart).padding(start = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DayView(
    appointments: List<Appointment>,
    date: LocalDate,
    bronzeGold: Color,
    backgroundDark: Color,
    onTimeSlotClick: (date: LocalDate, hour: Int, appointment: Appointment?) -> Unit = { _, _, _ -> }
) {
    val thinLine = 0.2.dp
    val thinLineColor = bronzeGold.copy(alpha = 0.5f)
    val visibleHours = (8..20).toList()
    val extraHours = (21..22).toList()
    val totalHours = (8..22).toList()
    val hourBoxHeight = 36.dp
    val maxVisibleRows = visibleHours.size
    val maxGridHeight = (hourBoxHeight * maxVisibleRows) + 28.dp
    Column(modifier = Modifier.fillMaxSize().background(backgroundDark)) {
        Text(date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("pt", "BR"))).replaceFirstChar { it.uppercase() }, color = bronzeGold, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.align(Alignment.CenterHorizontally).padding(8.dp))
        Box(
            modifier = Modifier
                .heightIn(max = maxGridHeight)
                .verticalScroll(rememberScrollState())
        ) {
            Column {
                for (hour in totalHours) {
                    val appointment = appointments.find { appt ->
                        val apptDate = appt.date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        val apptHour = appt.startTime.split(":")[0].toIntOrNull() ?: -1
                        apptDate == date && apptHour == hour
                    }
                    Row(modifier = Modifier.fillMaxWidth().height(hourBoxHeight).border(thinLine, thinLineColor).clickable { onTimeSlotClick(date, hour, appointment) }, verticalAlignment = Alignment.CenterVertically) {
                        Text("%02d:00".format(hour), color = bronzeGold, fontSize = 10.sp, modifier = Modifier.width(44.dp).padding(end = 2.dp), textAlign = TextAlign.End)
                        if (appointment != null) {
                            Text(
                                text = appointment.patientName,
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(start = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
} 