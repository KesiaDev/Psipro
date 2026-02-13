package com.psipro.app.ui.viewmodels.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psipro.app.data.dao.AppointmentDao
import com.psipro.app.data.dao.AnotacaoSessaoDao
import com.psipro.app.data.dao.CobrancaSessaoDao
import com.psipro.app.data.entities.AppointmentStatus
import com.psipro.app.data.entities.StatusPagamento
import com.psipro.app.ui.screens.home.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val appointmentDao: AppointmentDao,
    private val anotacaoSessaoDao: AnotacaoSessaoDao,
    private val cobrancaSessaoDao: CobrancaSessaoDao,
    private val insightProvider: InsightProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Calcular início e fim do dia
                val startOfDay = getStartOfDay(Date())
                val endOfDay = getEndOfDay(Date())
                
                // Buscar dados adicionais
                val todayReceived = cobrancaSessaoDao.getTotalRecebidoHoje(startOfDay, endOfDay) ?: 0.0
                val upcomingAppointments = appointmentDao.getUpcomingAppointments().first()
                
                // Combinar todos os flows
                combine(
                    appointmentDao.getTodayAppointments(),
                    appointmentDao.getAppointmentsByStatus(AppointmentStatus.REALIZADO),
                    cobrancaSessaoDao.getByStatus(StatusPagamento.A_RECEBER),
                    cobrancaSessaoDao.getByStatus(StatusPagamento.VENCIDO),
                    appointmentDao.getAppointmentsByStatus(AppointmentStatus.FALTOU)
                ) { todayAppointments, realizedAppointments, pendingPayments, overduePayments, missedAppointments ->
                    
                    // Sessões realizadas HOJE (para resumo do dia)
                    val today = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.time
                    val endOfToday = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }.time
                    
                    val todayRealized = realizedAppointments.filter { 
                        it.date >= today && it.date <= endOfToday 
                    }
                    
                    // Simplificar: não verificar anotações dentro do combine (será feito depois se necessário)
                    // Por enquanto, assumir que sessões realizadas podem não ter anotação
                    val sessionsWithoutNote = realizedAppointments

                    // Verificar quais agendamentos de hoje têm anotação (simplificado)
                    val todayAppointmentsUi = todayAppointments.map { appointment ->
                        AppointmentUi(
                            id = appointment.id,
                            patientId = appointment.patientId,
                            patientName = appointment.patientName,
                            patientPhone = appointment.patientPhone,
                            time = appointment.startTime,
                            status = appointment.status,
                            hasNote = false, // Será verificado depois se necessário
                            sessionValue = appointment.sessionValue
                        )
                    }
                    
                    // Próxima sessão (primeira agendada futura)
                    val nextAppointmentUi = upcomingAppointments
                        .filter { it.status == AppointmentStatus.CONFIRMADO }
                        .minByOrNull { 
                            val appointmentDate = Calendar.getInstance().apply {
                                time = it.date
                                set(Calendar.HOUR_OF_DAY, it.startTime.split(":")[0].toInt())
                                set(Calendar.MINUTE, it.startTime.split(":")[1].toInt())
                            }
                            appointmentDate.timeInMillis
                        }?.let { appointment ->
                            AppointmentUi(
                                id = appointment.id,
                                patientId = appointment.patientId,
                                patientName = appointment.patientName,
                                patientPhone = appointment.patientPhone,
                                time = appointment.startTime,
                                status = appointment.status,
                                hasNote = false,
                                sessionValue = appointment.sessionValue
                            )
                        }

                    // Criar resumo melhorado
                    val summary = HomeSummary(
                        todaySessionsCount = todayAppointments.size,
                        sessionsWithoutNoteCount = sessionsWithoutNote.size,
                        pendingPaymentsCount = (pendingPayments.size + overduePayments.size),
                        recentAbsencesCount = missedAppointments.filter { 
                            // Faltas dos últimos 7 dias
                            val daysDiff = (Date().time - it.date.time) / (1000 * 60 * 60 * 24)
                            daysDiff <= 7
                        }.size,
                        todayReceivedValue = todayReceived,
                        todayRealizedCount = todayRealized.size
                    )

                    // Criar lista de pendências
                    val pendingItems = buildPendingItems(
                        sessionsWithoutNote,
                        pendingPayments + overduePayments,
                        missedAppointments
                    )

                    // Gerar insights
                    val insights = insightProvider.generateInsights(
                        todayAppointmentsUi,
                        pendingItems,
                        summary
                    )

                    // Atualizar estado
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            greeting = getGreeting(),
                            currentDate = formatDate(Date()),
                            summary = summary,
                            todayAppointments = todayAppointmentsUi.sortedBy { it.time },
                            nextAppointment = nextAppointmentUi,
                            pendingItems = pendingItems.sortedByDescending { 
                                when (it.priority) {
                                    Priority.HIGH -> 3
                                    Priority.MEDIUM -> 2
                                    Priority.LOW -> 1
                                }
                            },
                            insights = insights
                        )
                    }
                }.catch { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Erro ao carregar dados"
                        )
                    }
                }.collect()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Erro ao carregar dados"
                    )
                }
            }
        }
    }

    private fun buildPendingItems(
        sessionsWithoutNote: List<com.psipro.app.data.entities.Appointment>,
        pendingPayments: List<com.psipro.app.data.entities.CobrancaSessao>,
        missedAppointments: List<com.psipro.app.data.entities.Appointment>
    ): List<PendingItem> {
        val items = mutableListOf<PendingItem>()

        // Sessões sem anotação
        sessionsWithoutNote.take(5).forEach { appointment ->
            items.add(
                PendingItem(
                    id = "session_note_${appointment.id}",
                    title = "Sessão sem anotação",
                    description = "${appointment.patientName} - ${formatDate(appointment.date)}",
                    priority = Priority.HIGH,
                    type = PendingType.SESSION_WITHOUT_NOTE,
                    relatedId = appointment.id,
                    actionLabel = "Anotar sessão"
                )
            )
        }

        // Pagamentos vencidos
        pendingPayments.filter { it.status == StatusPagamento.VENCIDO }
            .take(5)
            .forEach { payment ->
                items.add(
                    PendingItem(
                        id = "payment_${payment.id}",
                        title = "Pagamento vencido",
                        description = "R$ ${String.format("%.2f", payment.valor)} - Vencido em ${formatDate(payment.dataVencimento)}",
                        priority = Priority.HIGH,
                        type = PendingType.OVERDUE_PAYMENT,
                        relatedId = payment.id,
                        actionLabel = "Ver cobrança"
                    )
                )
            }

        // Pagamentos a receber
        pendingPayments.filter { it.status == StatusPagamento.A_RECEBER }
            .take(3)
            .forEach { payment ->
                items.add(
                    PendingItem(
                        id = "payment_pending_${payment.id}",
                        title = "Pagamento pendente",
                        description = "R$ ${String.format("%.2f", payment.valor)} - Vence em ${formatDate(payment.dataVencimento)}",
                        priority = Priority.MEDIUM,
                        type = PendingType.OVERDUE_PAYMENT,
                        relatedId = payment.id,
                        actionLabel = "Ver cobrança"
                    )
                )
            }

        // Faltas recentes
        missedAppointments.filter { 
            val daysDiff = (Date().time - it.date.time) / (1000 * 60 * 60 * 24)
            daysDiff <= 7
        }.take(3).forEach { appointment ->
            items.add(
                PendingItem(
                    id = "missed_${appointment.id}",
                    title = "Falta recente",
                    description = "${appointment.patientName} - ${formatDate(appointment.date)}",
                    priority = Priority.MEDIUM,
                    type = PendingType.MISSED_APPOINTMENT,
                    relatedId = appointment.id,
                    actionLabel = "Ver detalhes"
                )
            )
        }

        return items
    }

    private fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "Bom dia"
            hour < 18 -> "Boa tarde"
            else -> "Boa noite"
        }
    }

    private fun formatDate(date: Date): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
        return sdf.format(date)
    }

    private fun getStartOfDay(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    private fun getEndOfDay(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.time
    }

    fun refresh() {
        loadHomeData()
    }
}

