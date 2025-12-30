/**
 * Motor de Insights do PsiPro
 * 
 * Gera insights inteligentes baseados em dados operacionais e administrativos.
 * 
 * PRINCÍPIOS:
 * - Apenas dados operacionais (nunca clínicos)
 * - Observações, não ordens
 * - Linguagem humana e ética
 * - Preparado para evoluir para IA real
 */

import { Insight, InsightInput, InsightType } from './types';

class InsightEngine {
  /**
   * Gera todos os insights disponíveis a partir dos dados fornecidos
   */
  generateInsights(input: InsightInput): Insight[] {
    const insights: Insight[] = [];

    // Gerar insights de cada categoria
    insights.push(...this.generateAgendaInsights(input));
    insights.push(...this.generateFinancialInsights(input));
    insights.push(...this.generatePatientInsights(input));

    // Ordenar por prioridade (maior primeiro)
    insights.sort((a, b) => b.priority - a.priority);

    return insights;
  }

  /**
   * Gera insights relacionados à agenda
   */
  private generateAgendaInsights(input: InsightInput): Insight[] {
    const insights: Insight[] = [];

    // Insight: Muitas faltas
    if (input.missedSessions > 0 && input.sessionsThisMonth > 0) {
      const missedRate = (input.missedSessions / input.sessionsThisMonth) * 100;
      if (missedRate > 20) {
        insights.push({
          id: 'agenda-missed-sessions',
          type: 'warning',
          category: 'agenda',
          title: 'Taxa de faltas elevada',
          description: `Você teve ${input.missedSessions} faltas este mês (${Math.round(missedRate)}% das sessões). Pode ser útil observar se há algum padrão nos dias ou horários.`,
          priority: 7,
        });
      }
    }

    // Insight: Poucas sessões agendadas
    if (input.scheduledSessionsNextWeek < 5 && input.activePatients > 0) {
      insights.push({
        id: 'agenda-few-scheduled',
        type: 'tip',
        category: 'agenda',
        title: 'Poucas sessões agendadas',
        description: `Você tem ${input.scheduledSessionsNextWeek} sessões agendadas para a próxima semana. Pode ser uma boa oportunidade para organizar sua agenda.`,
        priority: 5,
      });
    }

    // Insight: Agenda concentrada
    if (input.busiestDay && input.emptiestDay) {
      insights.push({
        id: 'agenda-concentration',
        type: 'info',
        category: 'agenda',
        title: 'Distribuição da agenda',
        description: `Sua agenda parece estar mais concentrada em ${input.busiestDay}. Pode ser interessante observar como isso impacta sua rotina.`,
        priority: 4,
      });
    }

    // Insight: Muitos cancelamentos
    if (input.cancelledSessions > 3) {
      insights.push({
        id: 'agenda-cancellations',
        type: 'warning',
        category: 'agenda',
        title: 'Cancelamentos recentes',
        description: `Houve ${input.cancelledSessions} cancelamentos recentemente. Vale observar se esse padrão se mantém.`,
        priority: 6,
      });
    }

    // Insight: Crescimento de sessões
    if (input.sessionsThisMonth > input.sessionsLastMonth * 1.2) {
      insights.push({
        id: 'agenda-growth',
        type: 'success',
        category: 'agenda',
        title: 'Crescimento na agenda',
        description: `Você teve ${input.sessionsThisMonth} sessões este mês, um aumento em relação ao mês anterior.`,
        priority: 6,
      });
    }

    return insights;
  }

  /**
   * Gera insights relacionados ao financeiro
   */
  private generateFinancialInsights(input: InsightInput): Insight[] {
    const insights: Insight[] = [];

    // Insight: Receita abaixo da média
    if (input.lastMonthRevenue > 0 && input.monthlyRevenue < input.lastMonthRevenue * 0.8) {
      const decrease = ((input.lastMonthRevenue - input.monthlyRevenue) / input.lastMonthRevenue) * 100;
      insights.push({
        id: 'financial-revenue-decrease',
        type: 'warning',
        category: 'financeiro',
        title: 'Receita abaixo do esperado',
        description: `Sua receita deste mês está ${Math.round(decrease)}% abaixo do mês anterior. Pode ser útil verificar se há padrões que explicam essa diferença.`,
        priority: 8,
      });
    }

    // Insight: Receita acima da média
    if (input.lastMonthRevenue > 0 && input.monthlyRevenue > input.lastMonthRevenue * 1.2) {
      const increase = ((input.monthlyRevenue - input.lastMonthRevenue) / input.lastMonthRevenue) * 100;
      insights.push({
        id: 'financial-revenue-increase',
        type: 'success',
        category: 'financeiro',
        title: 'Receita em crescimento',
        description: `Sua receita deste mês está ${Math.round(increase)}% acima do mês anterior.`,
        priority: 7,
      });
    }

    // Insight: Muitos atendimentos não pagos
    if (input.unpaidSessions > 5) {
      insights.push({
        id: 'financial-unpaid-sessions',
        type: 'warning',
        category: 'financeiro',
        title: 'Atendimentos pendentes de pagamento',
        description: `Você tem ${input.unpaidSessions} sessões ainda não pagas. Pode ser útil acompanhar esses valores para manter o fluxo de caixa organizado.`,
        priority: 7,
      });
    }

    // Insight: Valor pendente alto
    if (input.totalPendingRevenue > input.monthlyRevenue * 0.3) {
      insights.push({
        id: 'financial-high-pending',
        type: 'tip',
        category: 'financeiro',
        title: 'Valores a receber',
        description: `Você tem R$ ${input.totalPendingRevenue.toFixed(2)} em valores pendentes. Acompanhar esses recebimentos pode ajudar no planejamento.`,
        priority: 6,
      });
    }

    // Insight: Receita média por sessão
    if (input.averageRevenuePerSession > 0 && input.sessionsThisMonth > 0) {
      const avg = input.averageRevenuePerSession;
      insights.push({
        id: 'financial-average-session',
        type: 'info',
        category: 'financeiro',
        title: 'Receita média por sessão',
        description: `Sua receita média por sessão este mês foi de R$ ${avg.toFixed(2)}.`,
        priority: 4,
      });
    }

    return insights;
  }

  /**
   * Gera insights relacionados a pacientes (apenas administrativos)
   */
  private generatePatientInsights(input: InsightInput): Insight[] {
    const insights: Insight[] = [];

    // Insight: Pacientes sem sessões agendadas
    if (input.patientsWithoutSessions > 0 && input.activePatients > 0) {
      const percentage = (input.patientsWithoutSessions / input.activePatients) * 100;
      if (percentage > 30) {
        insights.push({
          id: 'patients-no-sessions',
          type: 'tip',
          category: 'pacientes',
          title: 'Pacientes sem sessões futuras',
          description: `${input.patientsWithoutSessions} pacientes ativos não têm sessões agendadas. Pode ser útil verificar se há necessidade de reagendamento.`,
          priority: 5,
        });
      }
    }

    // Insight: Muitos pacientes novos
    if (input.newPatientsThisMonth > input.activePatients * 0.3) {
      insights.push({
        id: 'patients-many-new',
        type: 'success',
        category: 'pacientes',
        title: 'Crescimento na base de pacientes',
        description: `Você recebeu ${input.newPatientsThisMonth} novos pacientes este mês.`,
        priority: 6,
      });
    }

    // Insight: Poucos pacientes ativos
    if (input.activePatients < 5 && input.sessionsThisMonth > 0) {
      insights.push({
        id: 'patients-few-active',
        type: 'info',
        category: 'pacientes',
        title: 'Base de pacientes',
        description: `Você tem ${input.activePatients} pacientes ativos. Conforme sua prática crescer, você poderá acompanhar padrões mais claros aqui.`,
        priority: 3,
      });
    }

    return insights;
  }
}

// Instância singleton
export const insightEngine = new InsightEngine();

/**
 * Função principal para gerar insights
 * Esta é a interface pública que será usada no dashboard
 */
export function generateInsights(input: InsightInput): Insight[] {
  return insightEngine.generateInsights(input);
}


