package com.example.psipro.ui.compose

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
import androidx.compose.ui.res.colorResource
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
import com.example.psipro.data.entities.Appointment
import androidx.compose.material3.MaterialTheme
import com.example.psipro.R
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.OutlinedButton
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext


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
    val bronzeGold = colorResource(id = R.color.primary_bronze)
    val bronzeGoldTranslucent = colorResource(id = R.color.primary_bronze_translucent)
    val backgroundDark = colorResource(id = R.color.background_dark)
    val backgroundLight = colorResource(id = R.color.background_light)
    val textLight = colorResource(id = R.color.text_light)
    val textDark = colorResource(id = R.color.text_dark)
    val eventBackground = colorResource(id = R.color.event_background)
    val eventText = colorResource(id = R.color.event_text)
    val isLight = !isSystemInDarkTheme()
    val thinLine = 0.8.dp
    val thinLineColor = bronzeGold.copy(alpha = if (isLight) 0.18f else 0.28f)
    val hourBoxHeight = 70.dp
    val hourFontSize = 10.sp
    val nameFontSize = 10.sp
    val dayFontSize = 12.sp
    val headerFontSize = 18.sp
    val visibleHours = (7..13).toList() // 07:00 até 14:00 (8 horários visíveis)
    val extraHours = (21..22).toList() // Horários que aparecem só ao rolar
    val totalHours = (7..20).toList()
    val maxVisibleRows = visibleHours.size
    val maxGridHeight = (hourBoxHeight * maxVisibleRows) + 28.dp
    var currentMonth by remember { mutableStateOf(weekStart.withDayOfMonth(1)) }
    val hourLabelColor = bronzeGold.copy(alpha = 0.7f)
    val context = LocalContext.current
    var theme by remember { mutableStateOf(loadThemePreference(context)) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(theme) {
        saveThemePreference(context, theme)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        @OptIn(ExperimentalMaterial3Api::class)
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
                                    expanded = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = when(option) {
                                            "Mês" -> Icons.Filled.DateRange
                                            "Semana" -> Icons.Filled.Event
                                            "4 dias" -> Icons.Filled.Event
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

        // 1. Título principal da agenda
        // Text(
        //     text = "Agenda",
        //     fontSize = 26.sp,
        //     fontWeight = FontWeight.Bold,
        //     color = bronzeGold,
        //     textAlign = TextAlign.Center,
        //     modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
        // )

        // Adicione acima do calendário:
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Menu dropdown para seleção de visualização
            // var expanded by remember { mutableStateOf(false) }
            
            // Box {
            //     IconButton(
            //         onClick = { expanded = true },
            //         modifier = Modifier.padding(vertical = 0.dp)
            //     ) {
            //         Icon(
            //             Icons.Filled.DateRange,
            //             contentDescription = "Selecionar visualização",
            //             tint = bronzeGold
            //         )
            //     }
                
            //     DropdownMenu(
            //         expanded = expanded,
            //         onDismissRequest = { expanded = false }
            //     ) {
            //         menuOptions.forEach { option ->
            //             DropdownMenuItem(
            //                 text = { 
            //                     Text(
            //                         text = option,
            //                         color = if (viewMode == option) bronzeGold else MaterialTheme.colorScheme.onSurface
            //                     )
            //                 },
            //                 onClick = {
            //                     viewMode = option
            //                     expanded = false
            //                 },
            //                 leadingIcon = {
            //                     Icon(
            //                         imageVector = when(option) {
            //                             "Mês" -> Icons.Filled.DateRange
            //                             "Semana" -> Icons.Filled.Event
            //                             "4 dias" -> Icons.Filled.Event
            //                             else -> Icons.Filled.Event
            //                         },
            //                         contentDescription = null,
            //                         tint = if (viewMode == option) bronzeGold else MaterialTheme.colorScheme.onSurface
            //                     )
            //                 }
            //             )
            //         }
            //     }
            // }
        }

        // Cabeçalho com menu suspenso sempre visível
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (viewMode == "Semana") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                IconButton(onClick = { weekStart = weekStart.minusWeeks(1) }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Semana anterior", tint = bronzeGold)
            }
            Text(
                        text = "Semana",
                color = bronzeGold,
                fontWeight = FontWeight.Bold,
                        fontSize = headerFontSize,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
                    IconButton(onClick = { weekStart = weekStart.plusWeeks(1) }) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Próxima semana", tint = bronzeGold)
                    }
                }
            } else {
                Spacer(modifier = Modifier.width(40.dp))
            }
        }

        // 2. Linhas de grade bronze translúcido
        Divider(color = bronzeGold.copy(alpha = 0.2f), thickness = 1.dp)

        // Conteúdo de acordo com o modo de visualização
        when(viewMode) {
            "Semana" -> {
                // Cabeçalho dos dias da semana
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
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
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Nome do dia (SEG, TER, ...)
                            Text(
                                text = daysOfWeek[i],
                                color = if (isToday) MaterialTheme.colorScheme.background else bronzeGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = dayFontSize,
                                modifier = if (isToday) Modifier
                                    .background(bronzeGold, shape = CircleShape)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                else Modifier
                            )
                            // Número do dia do mês
                            Text(
                                text = date.dayOfMonth.toString(),
                                color = if (isToday) MaterialTheme.colorScheme.background else bronzeGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = dayFontSize
                            )
                        }
                    }
                }
                val horarios = (7..20).map { java.time.LocalTime.of(it, 0) }
                val alturaCelula = hourBoxHeight
                Row(Modifier.fillMaxWidth()) {
                    // Coluna de horários
                    Column(Modifier.width(44.dp)) {
                        Spacer(modifier = Modifier.height(28.dp))
                        horarios.forEach { horario ->
                            Box(
                                Modifier
                                    .height(alturaCelula)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = "%02d:00".format(horario.hour),
                                    color = bronzeGold,
                                    fontSize = hourFontSize,
                                    modifier = Modifier.padding(end = 12.dp),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                    // Colunas dos dias
                    for (i in 0..6) {
                        val date = weekStart.plusDays(i.toLong())
                        Box(Modifier.weight(1f)) {
                            // Grade de horários
                            Column {
                                Spacer(modifier = Modifier.height(28.dp))
                                horarios.forEach { _ ->
                                    Box(
                                        Modifier
                                            .height(alturaCelula)
                                            .fillMaxWidth()
                                            .border(thinLine, thinLineColor)
                                    )
                                }
                            }
                            // Eventos sobrepostos desenhados por cima
                            val eventosDoDia = appointments.filter { appt ->
                                val apptDate = appt.date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                                apptDate == date
                            }
                            eventosDoDia.sortedBy { parseTime(it.startTime) }.forEach { evento ->
                                val inicio = parseTime(evento.startTime)
                                val fim = parseTime(evento.endTime)
                                val minutosDesdeInicio = ((inicio.hour - 7) * 60 + inicio.minute)
                                val minutosDuracao = ((fim.hour - inicio.hour) * 60 + (fim.minute - inicio.minute)).coerceAtLeast(15)
                                val topOffset = alturaCelula * (minutosDesdeInicio / 60f)
                                val eventHeight = alturaCelula * (minutosDuracao / 60f)
                                // Empilhamento lateral para múltiplos eventos no mesmo horário
                                val eventosNoMesmoHorario = eventosDoDia.filter {
                                    val s = parseTime(it.startTime)
                                    val e = parseTime(it.endTime)
                                    (inicio < e && fim > s)
                                }
                                val largura = 1f / eventosNoMesmoHorario.size
                                val posicao = eventosNoMesmoHorario.indexOf(evento)
                                Box(
                                    Modifier
                                        .fillMaxWidth(largura)
                                        .offset(y = topOffset)
                                        .height(eventHeight)
                                        .padding(2.dp)
                                        .background(Color(android.graphics.Color.parseColor(evento.colorHex)), shape = RoundedCornerShape(8.dp))
                                        .clickable { /* abrir detalhes/editar */ }
                                ) {
                                    Text(
                                        text = evento.title,
                                        color = textDark,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            "Mês" -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = "Mês anterior", tint = bronzeGold)
                    }
                    Text(
                        text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("pt", "BR"))),
                        color = bronzeGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = headerFontSize,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                        Icon(Icons.Filled.ChevronRight, contentDescription = "Próximo mês", tint = bronzeGold)
                    }
                }
                MonthView(
                    appointments = appointments,
                    currentMonth = currentMonth,
                    bronzeGold = bronzeGold,
                    bronzeGoldTranslucent = bronzeGoldTranslucent,
                    backgroundDark = MaterialTheme.colorScheme.background,
                    onDayClick = { }
                )
            }
            "4 dias" -> {
                FourDaysView(
                    appointments = appointments,
                    startDate = weekStart,
                    bronzeGold = bronzeGold,
                    bronzeGoldTranslucent = bronzeGoldTranslucent,
                    backgroundDark = MaterialTheme.colorScheme.background,
                    onTimeSlotClick = { date, hour, _ -> onTimeSlotClick(date, hour) },
                    textLight = textLight,
                    textDark = textDark,
                    eventText = eventText
                )
            }
            "Dia" -> {
                DayView(
                    appointments = appointments,
                    date = weekStart,
                    bronzeGold = bronzeGold,
                    bronzeGoldTranslucent = bronzeGoldTranslucent,
                    backgroundDark = MaterialTheme.colorScheme.background,
                    onTimeSlotClick = { date, hour, _ -> onTimeSlotClick(date, hour) },
                    textLight = textLight,
                    textDark = textDark,
                    eventText = eventText
                )
            }
            else -> {
                // Placeholder para outros modos
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
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

        // 6. Espaçamento generoso
        Spacer(modifier = Modifier.height(16.dp))

        // Adicione o botão de seleção de tema na tela de configurações:
        OutlinedButton(
            onClick = { theme = if (theme == "light") "dark" else "light" },
            border = BorderStroke(2.dp, if (theme == "light") bronzeGold else Color.Transparent),
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = if (theme == "light") "Tema: Claro" else "Tema: Escuro",
                color = bronzeGold
            )
        }
    }
}

@Composable
fun MonthView(
    appointments: List<Appointment>,
    currentMonth: LocalDate,
    bronzeGold: Color,
    bronzeGoldTranslucent: Color,
    backgroundDark: Color,
    onDayClick: (LocalDate) -> Unit = {}
) {
    val firstDayOfMonth = currentMonth.withDayOfMonth(1)
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // 0=Domingo
    val weeks = ((daysInMonth + firstDayOfWeek - 1) / 7) + 1
    val daysOfWeek = listOf("DOM", "SEG", "TER", "QUA", "QUI", "SEX", "SÁB")
    Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)) {
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
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                    .background(bronzeGoldTranslucent, shape = RoundedCornerShape(16.dp))
                                    .padding(vertical = 8.dp, horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                            Text(
                                day.toString(),
                                    color = if (hasAppointment) MaterialTheme.colorScheme.background else bronzeGold,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                            }
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
    bronzeGoldTranslucent: Color,
    backgroundDark: Color,
    onTimeSlotClick: (date: LocalDate, hour: Int, appointment: Appointment?) -> Unit = { _, _, _ -> },
    textLight: Color,
    textDark: Color,
    eventText: Color
) {
    val thinLine = 0.2.dp
    val thinLineColor = bronzeGold.copy(alpha = 0.5f)
    val visibleHours = (8..13).toList() // 08:00 até 14:00 (6 horários)
    val extraHours = (21..22).toList()
    val totalHours = (8..22).toList()
    val hourBoxHeight = 56.dp
    val maxVisibleRows = visibleHours.size
    val maxGridHeight = (hourBoxHeight * maxVisibleRows) + 28.dp
    val daysOfWeek = listOf("SEG", "TER", "QUA", "QUI", "SEX", "SÁB", "DOM")
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
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
                    Column(modifier = Modifier.width(64.dp)) {
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
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(bronzeGold.copy(alpha = 0.85f), shape = RoundedCornerShape(16.dp))
                                                .padding(horizontal = 8.dp)
                                                .heightIn(min = 48.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                        Text(
                                            text = appointment.patientName,
                                                color = textDark,
                                                textAlign = TextAlign.Center,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.padding(vertical = 12.dp)
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
}

@Composable
fun DayView(
    appointments: List<Appointment>,
    date: LocalDate,
    bronzeGold: Color,
    bronzeGoldTranslucent: Color,
    backgroundDark: Color,
    onTimeSlotClick: (date: LocalDate, hour: Int, appointment: Appointment?) -> Unit = { _, _, _ -> },
    textLight: Color,
    textDark: Color,
    eventText: Color
) {
    val thinLine = 0.2.dp
    val thinLineColor = bronzeGold.copy(alpha = 0.5f)
    val visibleHours = (8..13).toList() // 08:00 até 14:00 (6 horários)
    val extraHours = (21..22).toList()
    val totalHours = (8..22).toList()
    val hourBoxHeight = 56.dp
    val maxVisibleRows = visibleHours.size
    val maxGridHeight = (hourBoxHeight * maxVisibleRows) + 28.dp
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
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
                        Text("%02d:00".format(hour), color = bronzeGold, fontSize = 10.sp, modifier = Modifier.width(64.dp).padding(end = 2.dp), textAlign = TextAlign.End)
                        if (appointment != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                    .background(bronzeGoldTranslucent, shape = RoundedCornerShape(16.dp))
                                    .padding(vertical = 8.dp, horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(bronzeGold.copy(alpha = 0.85f), shape = RoundedCornerShape(16.dp))
                                        .padding(horizontal = 8.dp)
                                        .heightIn(min = 48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                            Text(
                                text = appointment.patientName,
                                        color = textDark,
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(vertical = 12.dp)
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

fun saveThemePreference(context: Context, theme: String) {
    val prefs: SharedPreferences = context.getSharedPreferences("PsiproPrefs", Context.MODE_PRIVATE)
    prefs.edit().putString("theme", theme).apply()
}

fun loadThemePreference(context: Context): String {
    val prefs: SharedPreferences = context.getSharedPreferences("PsiproPrefs", Context.MODE_PRIVATE)
    return prefs.getString("theme", "light") ?: "light"
}

// Função utilitária para converter string de hora para LocalTime
fun parseTime(timeStr: String): java.time.LocalTime =
    java.time.LocalTime.parse(timeStr, java.time.format.DateTimeFormatter.ofPattern("HH:mm")) 