package com.psipro.app.ui.screens.home

/**
 * Interface para provedor de insights inteligentes
 * 
 * PREPARAÇÃO PARA IA FUTURA:
 * Esta interface permite substituir a implementação local por uma integração com IA
 * sem alterar o código que consome os insights.
 * 
 * Pontos de entrada para IA:
 * - generateInsights: Pode usar análise de padrões, ML, ou API externa
 * - generateSessionSummary: (futuro) Resumos automáticos de sessões
 * - detectPatterns: (futuro) Detecção de padrões (faltas, progresso, etc.)
 * 
 * Por enquanto, usa apenas lógica local baseada em regras simples.
 */
interface InsightProvider {
    suspend fun generateInsights(
        appointments: List<AppointmentUi>,
        pendingItems: List<PendingItem>,
        summary: HomeSummary
    ): List<HomeInsight>
    
    // Pontos de entrada futuros para IA (comentados para não quebrar implementação atual)
    // suspend fun generateSessionSummary(sessionId: Long): String?
    // suspend fun detectPatterns(patientId: Long): List<Pattern>
    // suspend fun suggestActions(context: HomeContext): List<ActionSuggestion>
}

/**
 * Implementação local baseada em regras simples
 * Não usa API externa - apenas lógica local
 */
class LocalInsightProvider : InsightProvider {
    override suspend fun generateInsights(
        appointments: List<AppointmentUi>,
        pendingItems: List<PendingItem>,
        summary: HomeSummary
    ): List<HomeInsight> {
        val insights = mutableListOf<HomeInsight>()

        // PROMPT 3: Textos reescritos para tom mais humano e acolhedor
        // Insight 1: Sessões sem anotação
        if (summary.sessionsWithoutNoteCount > 0) {
            insights.add(
                HomeInsight(
                    id = "insight_1",
                    title = "Anotações pendentes",
                    description = "Você tem ${summary.sessionsWithoutNoteCount} sessão${if (summary.sessionsWithoutNoteCount > 1) "ões" else ""} realizada${if (summary.sessionsWithoutNoteCount > 1) "s" else ""} sem anotação. Que tal completar agora?",
                    type = InsightType.RECOMMENDATION,
                    icon = "ic_note",
                    actionLabel = "Ver sessões",
                    actionId = "view_sessions"
                )
            )
        }

        // Insight 2: Pagamentos pendentes
        if (summary.pendingPaymentsCount > 0) {
            insights.add(
                HomeInsight(
                    id = "insight_2",
                    title = "Pagamentos pendentes",
                    description = "${summary.pendingPaymentsCount} pagamento${if (summary.pendingPaymentsCount > 1) "s" else ""} aguardando. Vale a pena revisar.",
                    type = InsightType.PAYMENT_TREND,
                    icon = "ic_attach_money",
                    actionLabel = "Ver financeiro",
                    actionId = "view_financial"
                )
            )
        }

        // Insight 3: Agenda do dia
        if (summary.todaySessionsCount > 0) {
            insights.add(
                HomeInsight(
                    id = "insight_3",
                    title = "Seus atendimentos de hoje",
                    description = "${summary.todaySessionsCount} sessão${if (summary.todaySessionsCount > 1) "ões" else ""} agendada${if (summary.todaySessionsCount > 1) "s" else ""} para hoje.",
                    type = InsightType.ATTENDANCE_PATTERN,
                    icon = "ic_calendar",
                    actionLabel = "Ver agenda",
                    actionId = "view_schedule"
                )
            )
        }

        // Insight 4: Faltas recentes
        if (summary.recentAbsencesCount > 0) {
            insights.add(
                HomeInsight(
                    id = "insight_4",
                    title = "Faltas recentes",
                    description = "${summary.recentAbsencesCount} falta${if (summary.recentAbsencesCount > 1) "s" else ""} registrada${if (summary.recentAbsencesCount > 1) "s" else ""} recentemente. Talvez seja bom entrar em contato.",
                    type = InsightType.RECOMMENDATION,
                    icon = "ic_warning",
                    actionLabel = "Ver faltas",
                    actionId = "view_absences"
                )
            )
        }

        return insights
    }
}

