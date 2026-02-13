package com.psipro.app.ui.screens.home

import com.psipro.app.data.entities.AppointmentStatus
import com.psipro.app.data.entities.StatusPagamento
import java.util.Date

/**
 * Estado principal da Home Inteligente
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val greeting: String = "",
    val currentDate: String = "",
    val summary: HomeSummary = HomeSummary(),
    val todayAppointments: List<AppointmentUi> = emptyList(),
    val nextAppointment: AppointmentUi? = null, // Próxima sessão agendada
    val pendingItems: List<PendingItem> = emptyList(),
    val insights: List<HomeInsight> = emptyList(),
    val error: String? = null
)

/**
 * Resumo de informações da Home
 * UX: Informações que o psicólogo precisa ver rapidamente
 */
data class HomeSummary(
    val todaySessionsCount: Int = 0,
    val sessionsWithoutNoteCount: Int = 0,
    val pendingPaymentsCount: Int = 0,
    val recentAbsencesCount: Int = 0,
    val todayReceivedValue: Double = 0.0, // Valor recebido hoje
    val todayRealizedCount: Int = 0 // Sessões realizadas hoje
)

/**
 * Representação de agendamento para UI
 */
data class AppointmentUi(
    val id: Long,
    val patientId: Long?,
    val patientName: String,
    val patientPhone: String,
    val time: String, // Formato "HH:mm"
    val status: AppointmentStatus,
    val hasNote: Boolean = false,
    val sessionValue: Double = 0.0
)

/**
 * Item de pendência crítica
 */
data class PendingItem(
    val id: String,
    val title: String,
    val description: String,
    val priority: Priority,
    val type: PendingType,
    val relatedId: Long? = null, // ID do agendamento, cobrança, etc.
    val actionLabel: String = "Ver detalhes"
)

enum class Priority {
    HIGH,    // Crítico - precisa atenção imediata
    MEDIUM,  // Importante - deve ser resolvido em breve
    LOW      // Informativo - pode ser resolvido depois
}

enum class PendingType {
    SESSION_WITHOUT_NOTE,  // Sessão realizada sem anotação
    OVERDUE_PAYMENT,       // Pagamento vencido
    MISSED_APPOINTMENT,    // Falta recente
    UPCOMING_APPOINTMENT   // Próximo agendamento
}

/**
 * Insight inteligente (preparado para futura IA)
 */
data class HomeInsight(
    val id: String,
    val title: String,
    val description: String,
    val type: InsightType,
    val icon: String = "", // Nome do ícone
    val actionLabel: String? = null,
    val actionId: String? = null
)

enum class InsightType {
    PATIENT_PROGRESS,      // Progresso do paciente
    PAYMENT_TREND,         // Tendência de pagamentos
    ATTENDANCE_PATTERN,    // Padrão de atendimento
    RECOMMENDATION         // Recomendação geral
}

