import { Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { whereNotDeleted } from '../prisma/soft-delete.helper';
import { InsightSummaryDTO } from './dto/insight-summary.dto';

interface InsightInput {
  // Dados de agenda
  sessionsThisMonth: number;
  sessionsLastMonth: number;
  sessionsThisWeek: number;
  scheduledSessionsNextWeek: number;
  missedSessions: number;
  cancelledSessions: number;
  busiestDay?: string;
  emptiestDay?: string;
  
  // Dados financeiros
  monthlyRevenue: number;
  lastMonthRevenue: number;
  averageRevenuePerSession: number;
  unpaidSessions: number;
  totalPendingRevenue: number;
  
  // Dados de pacientes
  activePatients: number;
  newPatientsThisMonth: number;
  patientsWithoutSessions: number;
  patientsWithManySessions: number;
}

@Injectable()
export class InsightsService {
  constructor(private prisma: PrismaService) {}

  async findAll(userId: string, _clinicId: string) {
    return this.prisma.insight.findMany({
      where: {
        userId,
        dismissed: false,
      },
      orderBy: { createdAt: 'desc' },
      take: 10,
    });
  }

  async dismiss(id: string, userId: string, _clinicId: string) {
    return this.prisma.insight.updateMany({
      where: {
        id,
        userId,
      },
      data: {
        dismissed: true,
      },
    });
  }

  /**
   * Gera insights resumidos (máximo 3) para consumo pelo App Android
   */
  async getSummary(userId: string, clinicId: string): Promise<InsightSummaryDTO[]> {
    const input = await this.collectInsightData(userId, clinicId);
    
    // Gerar insights
    const insights = this.generateInsights(input);
    
    // Retornar apenas os 3 mais prioritários
    return insights.slice(0, 3).map(insight => ({
      id: insight.id,
      title: insight.title,
      description: insight.description,
      priority: insight.priority,
      actionId: insight.actionId,
    }));
  }

  /**
   * Coleta dados do banco para gerar insights
   */
  private async collectInsightData(userId: string, clinicId: string): Promise<InsightInput> {
    const now = new Date();
    const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);
    const startOfLastMonth = new Date(now.getFullYear(), now.getMonth() - 1, 1);
    const endOfLastMonth = new Date(now.getFullYear(), now.getMonth(), 0);
    const startOfWeek = new Date(now);
    startOfWeek.setDate(now.getDate() - now.getDay());
    const endOfWeek = new Date(startOfWeek);
    endOfWeek.setDate(startOfWeek.getDate() + 6);
    const nextWeekStart = new Date(now);
    nextWeekStart.setDate(now.getDate() + (7 - now.getDay()));
    const nextWeekEnd = new Date(nextWeekStart);
    nextWeekEnd.setDate(nextWeekStart.getDate() + 6);

    const sessionClinicFilter = { userId, patient: { clinicId, deletedAt: null } };
    const baseAppointmentWhere = whereNotDeleted('appointment', { userId, clinicId });
    const basePaymentWhere = whereNotDeleted('payment', { userId, clinicId });
    const basePatientWhere = whereNotDeleted('patient', { clinicId, status: 'Ativo' as const });

    // Sessões deste mês
    const sessionsThisMonth = await this.prisma.session.count({
      where: whereNotDeleted('session', {
        ...sessionClinicFilter,
        date: { gte: startOfMonth },
        status: 'realizada',
      }),
    });

    // Sessões do mês anterior
    const sessionsLastMonth = await this.prisma.session.count({
      where: whereNotDeleted('session', {
        userId,
        patient: { clinicId, deletedAt: null },
        date: { gte: startOfLastMonth, lte: endOfLastMonth },
        status: 'realizada',
      }),
    });

    // Sessões desta semana
    const sessionsThisWeek = await this.prisma.session.count({
      where: whereNotDeleted('session', {
        userId,
        patient: { clinicId, deletedAt: null },
        date: { gte: startOfWeek, lte: endOfWeek },
        status: 'realizada',
      }),
    });

    // Sessões agendadas para próxima semana
    const scheduledSessionsNextWeek = await this.prisma.appointment.count({
      where: { ...baseAppointmentWhere, scheduledAt: { gte: nextWeekStart, lte: nextWeekEnd }, status: { in: ['agendada', 'confirmada'] } },
    });

    // Faltas (sessões com status 'falta')
    const missedSessions = await this.prisma.session.count({
      where: whereNotDeleted('session', {
        userId,
        patient: { clinicId, deletedAt: null },
        date: { gte: startOfMonth },
        status: 'falta',
      }),
    });

    // Cancelamentos (últimos 30 dias)
    const thirtyDaysAgo = new Date(now);
    thirtyDaysAgo.setDate(now.getDate() - 30);
    const cancelledSessions = await this.prisma.appointment.count({
      where: { ...baseAppointmentWhere, updatedAt: { gte: thirtyDaysAgo }, status: 'cancelada' },
    });

    // Receita do mês
    const monthlyRevenueResult = await this.prisma.payment.aggregate({
      where: { ...basePaymentWhere, date: { gte: startOfMonth }, status: 'pago' },
      _sum: { amount: true },
    });
    const monthlyRevenue = Number(monthlyRevenueResult._sum.amount || 0);

    // Receita do mês anterior
    const lastMonthRevenueResult = await this.prisma.payment.aggregate({
      where: { ...basePaymentWhere, date: { gte: startOfLastMonth, lte: endOfLastMonth }, status: 'pago' },
      _sum: { amount: true },
    });
    const lastMonthRevenue = Number(lastMonthRevenueResult._sum.amount || 0);

    // Receita média por sessão
    const averageRevenuePerSession = sessionsThisMonth > 0
      ? monthlyRevenue / sessionsThisMonth
      : 0;

    // Sessões não pagas
    const unpaidSessions = await this.prisma.session.count({
      where: whereNotDeleted('session', {
        userId,
        patient: { clinicId, deletedAt: null },
        date: { gte: startOfMonth },
        payment: null,
      }),
    });

    // Total pendente
    const pendingRevenueResult = await this.prisma.payment.aggregate({
      where: { ...basePaymentWhere, status: 'pendente' },
      _sum: { amount: true },
    });
    const totalPendingRevenue = Number(pendingRevenueResult._sum.amount || 0);

    // Pacientes ativos
    const activePatients = await this.prisma.patient.count({
      where: basePatientWhere,
    });

    // Novos pacientes este mês
    const newPatientsThisMonth = await this.prisma.patient.count({
      where: {
        ...basePatientWhere,
        createdAt: { gte: startOfMonth },
      },
    });

    // Pacientes sem sessões agendadas
    const patientsWithSessions = await this.prisma.patient.findMany({
      where: {
        ...basePatientWhere,
        appointments: {
          some: {
            deletedAt: null,
            scheduledAt: { gte: now },
            status: { in: ['agendada', 'confirmada'] },
          },
        },
      },
      select: { id: true },
    });
    const patientsWithoutSessions = activePatients - patientsWithSessions.length;

    // Pacientes com muitas sessões (mais de 10 este mês)
    const patientsWithManySessions = await this.prisma.patient.count({
      where: {
        ...basePatientWhere,
        sessions: {
          some: {
            deletedAt: null,
            date: { gte: startOfMonth },
            status: 'realizada',
          },
        },
      },
    });

    // Dia mais ocupado (simplificado)
    const sessionsThisMonthList = await this.prisma.session.findMany({
      where: whereNotDeleted('session', {
        userId,
        patient: { clinicId, deletedAt: null },
        date: { gte: startOfMonth },
        status: 'realizada',
      }),
      select: { date: true },
    });
    
    // Agrupar por dia da semana
    const dayCounts: Record<string, number> = {};
    sessionsThisMonthList.forEach(session => {
      const dayName = new Date(session.date).toLocaleDateString('pt-BR', { weekday: 'long' });
      dayCounts[dayName] = (dayCounts[dayName] || 0) + 1;
    });
    
    const busiestDay = Object.keys(dayCounts).length > 0
      ? Object.entries(dayCounts).sort((a, b) => b[1] - a[1])[0][0]
      : undefined;

    return {
      sessionsThisMonth,
      sessionsLastMonth,
      sessionsThisWeek,
      scheduledSessionsNextWeek,
      missedSessions,
      cancelledSessions,
      busiestDay,
      emptiestDay: undefined, // Pode ser implementado se necessário
      monthlyRevenue,
      lastMonthRevenue,
      averageRevenuePerSession,
      unpaidSessions,
      totalPendingRevenue,
      activePatients,
      newPatientsThisMonth,
      patientsWithoutSessions,
      patientsWithManySessions,
    };
  }

  /**
   * Gera insights baseados em dados operacionais
   * NUNCA inclui dados clínicos
   */
  private generateInsights(input: InsightInput): InsightSummaryDTO[] {
    const insights: InsightSummaryDTO[] = [];

    // Insights de agenda
    insights.push(...this.generateAgendaInsights(input));

    // Insights financeiros
    insights.push(...this.generateFinancialInsights(input));

    // Insights de pacientes (apenas administrativos)
    insights.push(...this.generatePatientInsights(input));

    // Ordenar por prioridade (maior primeiro)
    insights.sort((a, b) => b.priority - a.priority);

    return insights;
  }

  /**
   * Gera insights relacionados à agenda
   */
  private generateAgendaInsights(input: InsightInput): InsightSummaryDTO[] {
    const insights: InsightSummaryDTO[] = [];

    // Insight: Muitas faltas
    if (input.missedSessions > 0 && input.sessionsThisMonth > 0) {
      const missedRate = (input.missedSessions / input.sessionsThisMonth) * 100;
      if (missedRate > 20) {
        insights.push({
          id: 'agenda-missed-sessions',
          title: 'Taxa de faltas elevada',
          description: `Você teve ${input.missedSessions} faltas este mês (${Math.round(missedRate)}% das sessões). Pode ser útil observar se há algum padrão nos dias ou horários.`,
          priority: 7,
          actionId: 'view-agenda',
        });
      }
    }

    // Insight: Poucas sessões agendadas
    if (input.scheduledSessionsNextWeek < 5 && input.activePatients > 0) {
      insights.push({
        id: 'agenda-few-scheduled',
        title: 'Poucas sessões agendadas',
        description: `Você tem ${input.scheduledSessionsNextWeek} sessões agendadas para a próxima semana. Pode ser uma boa oportunidade para organizar sua agenda.`,
        priority: 5,
        actionId: 'view-agenda',
      });
    }

    // Insight: Muitos cancelamentos
    if (input.cancelledSessions > 3) {
      insights.push({
        id: 'agenda-cancellations',
        title: 'Cancelamentos recentes',
        description: `Houve ${input.cancelledSessions} cancelamentos recentemente. Vale observar se esse padrão se mantém.`,
        priority: 6,
        actionId: 'view-agenda',
      });
    }

    // Insight: Crescimento de sessões
    if (input.sessionsThisMonth > input.sessionsLastMonth * 1.2 && input.sessionsLastMonth > 0) {
      insights.push({
        id: 'agenda-growth',
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
  private generateFinancialInsights(input: InsightInput): InsightSummaryDTO[] {
    const insights: InsightSummaryDTO[] = [];

    // Insight: Receita abaixo da média
    if (input.lastMonthRevenue > 0 && input.monthlyRevenue < input.lastMonthRevenue * 0.8) {
      const decrease = ((input.lastMonthRevenue - input.monthlyRevenue) / input.lastMonthRevenue) * 100;
      insights.push({
        id: 'financial-revenue-decrease',
        title: 'Receita abaixo do esperado',
        description: `Sua receita deste mês está ${Math.round(decrease)}% abaixo do mês anterior. Pode ser útil verificar se há padrões que explicam essa diferença.`,
        priority: 8,
        actionId: 'view-financial',
      });
    }

    // Insight: Receita acima da média
    if (input.lastMonthRevenue > 0 && input.monthlyRevenue > input.lastMonthRevenue * 1.2) {
      const increase = ((input.monthlyRevenue - input.lastMonthRevenue) / input.lastMonthRevenue) * 100;
      insights.push({
        id: 'financial-revenue-increase',
        title: 'Receita em crescimento',
        description: `Sua receita deste mês está ${Math.round(increase)}% acima do mês anterior.`,
        priority: 7,
      });
    }

    // Insight: Muitos atendimentos não pagos
    if (input.unpaidSessions > 5) {
      insights.push({
        id: 'financial-unpaid-sessions',
        title: 'Atendimentos pendentes de pagamento',
        description: `Você tem ${input.unpaidSessions} sessões ainda não pagas. Pode ser útil acompanhar esses valores para manter o fluxo de caixa organizado.`,
        priority: 7,
        actionId: 'view-financial',
      });
    }

    // Insight: Valor pendente alto
    if (input.totalPendingRevenue > input.monthlyRevenue * 0.3 && input.monthlyRevenue > 0) {
      insights.push({
        id: 'financial-high-pending',
        title: 'Valores a receber',
        description: `Você tem R$ ${input.totalPendingRevenue.toFixed(2)} em valores pendentes. Acompanhar esses recebimentos pode ajudar no planejamento.`,
        priority: 6,
        actionId: 'view-financial',
      });
    }

    return insights;
  }

  /**
   * Gera insights relacionados a pacientes (apenas administrativos)
   */
  private generatePatientInsights(input: InsightInput): InsightSummaryDTO[] {
    const insights: InsightSummaryDTO[] = [];

    // Insight: Pacientes sem sessões agendadas
    if (input.patientsWithoutSessions > 0 && input.activePatients > 0) {
      const percentage = (input.patientsWithoutSessions / input.activePatients) * 100;
      if (percentage > 30) {
        insights.push({
          id: 'patients-no-sessions',
          title: 'Pacientes sem sessões futuras',
          description: `${input.patientsWithoutSessions} pacientes ativos não têm sessões agendadas. Pode ser útil verificar se há necessidade de reagendamento.`,
          priority: 5,
          actionId: 'view-patients',
        });
      }
    }

    // Insight: Muitos pacientes novos
    if (input.newPatientsThisMonth > input.activePatients * 0.3 && input.activePatients > 0) {
      insights.push({
        id: 'patients-many-new',
        title: 'Crescimento na base de pacientes',
        description: `Você recebeu ${input.newPatientsThisMonth} novos pacientes este mês.`,
        priority: 6,
      });
    }

    return insights;
  }
}



