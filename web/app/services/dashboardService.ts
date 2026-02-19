/**
 * Serviço de Dashboard
 * 
 * Busca dados para o dashboard (métricas, agenda, financeiro)
 * 
 * NOTA: Se os endpoints específicos não existirem no backend,
 * este serviço usa os services existentes (patientService, appointmentService)
 * para calcular os dados como fallback.
 */

import { api, ApiError } from './api';
import { patientService } from './patientService';
import { appointmentService } from './appointmentService';

export interface DashboardMetrics {
  activePatients: number;
  sessionsThisMonth: number;
  sessionsThisWeek: number;
  monthlyRevenue: number;
  pendingRevenue: number;
}

export interface AgendaSummary {
  totalSessionsThisWeek: number;
  busiestDays: string[];
  emptiestDays: string[];
  isEmpty: boolean;
}

export interface FinanceSummary {
  monthlyRevenue: number;
  averagePerSession: number;
  unpaidSessions: number;
  isEmpty: boolean;
}

class DashboardService {
  /**
   * Busca métricas do dashboard
   * 
   * Tenta usar GET /api/dashboard/metrics
   * Se não existir, calcula usando services existentes
   */
  async getMetrics(clinicId?: string): Promise<DashboardMetrics> {
    try {
      // Tentar endpoint específico
      const params = clinicId ? { clinicId } : undefined;
      return await api.get<DashboardMetrics>('/dashboard/metrics', params);
    } catch (error) {
      // Se endpoint não existir (404), calcular usando services
      if (error && typeof error === 'object' && 'status' in error && (error as ApiError).status === 404) {
        return this.calculateMetrics(clinicId);
      }
      throw this.handleError(error);
    }
  }

  /**
   * Busca resumo de agenda
   * 
   * Tenta usar GET /api/dashboard/agenda-summary
   * Se não existir, calcula usando appointmentService
   */
  async getAgendaSummary(clinicId?: string): Promise<AgendaSummary> {
    try {
      const params = clinicId ? { clinicId } : undefined;
      return await api.get<AgendaSummary>('/dashboard/agenda-summary', params);
    } catch (error) {
      if (error && typeof error === 'object' && 'status' in error && (error as ApiError).status === 404) {
        return this.calculateAgendaSummary(clinicId);
      }
      throw this.handleError(error);
    }
  }

  /**
   * Busca resumo financeiro
   * 
   * Tenta usar GET /api/dashboard/finance-summary
   * Se não existir, calcula usando appointmentService
   */
  async getFinanceSummary(clinicId?: string): Promise<FinanceSummary> {
    try {
      const params = clinicId ? { clinicId } : undefined;
      return await api.get<FinanceSummary>('/dashboard/finance-summary', params);
    } catch (error) {
      if (error && typeof error === 'object' && 'status' in error && (error as ApiError).status === 404) {
        return this.calculateFinanceSummary(clinicId);
      }
      throw this.handleError(error);
    }
  }

  /**
   * Calcula métricas usando services existentes (fallback)
   */
  private async calculateMetrics(clinicId?: string): Promise<DashboardMetrics> {
    try {
      const [patients, appointments] = await Promise.all([
        patientService.getPatients(),
        appointmentService.getAppointments(clinicId),
      ]);

      const now = new Date();
      const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);
      const startOfWeek = new Date(now);
      startOfWeek.setDate(now.getDate() - now.getDay()); // Domingo da semana
      startOfWeek.setHours(0, 0, 0, 0);

      // Filtrar pacientes ativos (sem critério específico, considerar todos como ativos)
      const activePatients = patients.length;

      // Sessões do mês (realizadas)
      const sessionsThisMonth = appointments.filter(
        (apt) => new Date(apt.scheduledAt) >= startOfMonth && apt.status === 'completed'
      ).length;

      // Sessões da semana (agendadas)
      const sessionsThisWeek = appointments.filter(
        (apt) => new Date(apt.scheduledAt) >= startOfWeek
      ).length;

      // Receita do mês (precisa de dados de pagamento, usar 0 como fallback)
      const monthlyRevenue = 0;

      // Valores pendentes (precisa de dados de pagamento, usar 0 como fallback)
      const pendingRevenue = 0;

      return {
        activePatients,
        sessionsThisMonth,
        sessionsThisWeek,
        monthlyRevenue,
        pendingRevenue,
      };
    } catch (error) {
      // Se houver erro, retornar valores zerados
      return {
        activePatients: 0,
        sessionsThisMonth: 0,
        sessionsThisWeek: 0,
        monthlyRevenue: 0,
        pendingRevenue: 0,
      };
    }
  }

  /**
   * Calcula resumo de agenda usando services existentes (fallback)
   */
  private async calculateAgendaSummary(clinicId?: string): Promise<AgendaSummary> {
    try {
      const appointments = await appointmentService.getAppointments(clinicId);

      const now = new Date();
      const startOfWeek = new Date(now);
      startOfWeek.setDate(now.getDate() - now.getDay());
      startOfWeek.setHours(0, 0, 0, 0);
      const endOfWeek = new Date(startOfWeek);
      endOfWeek.setDate(startOfWeek.getDate() + 7);

      const weekAppointments = appointments.filter(
        (apt) => {
          const aptDate = new Date(apt.scheduledAt);
          return aptDate >= startOfWeek && aptDate < endOfWeek;
        }
      );

      const totalSessionsThisWeek = weekAppointments.length;

      // Agrupar por dia da semana
      const dayCounts: Record<number, number> = {};
      weekAppointments.forEach((apt) => {
        const day = new Date(apt.scheduledAt).getDay();
        dayCounts[day] = (dayCounts[day] || 0) + 1;
      });

      const dayNames = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'];
      const sortedDays = Object.entries(dayCounts)
        .sort(([, a], [, b]) => b - a)
        .map(([day]) => parseInt(day));

      const busiestDays = sortedDays.slice(0, 2).map((day) => dayNames[day]);
      const emptiestDays = sortedDays.slice(-2).reverse().map((day) => dayNames[day]);

      return {
        totalSessionsThisWeek,
        busiestDays,
        emptiestDays,
        isEmpty: totalSessionsThisWeek === 0,
      };
    } catch (error) {
      return {
        totalSessionsThisWeek: 0,
        busiestDays: [],
        emptiestDays: [],
        isEmpty: true,
      };
    }
  }

  /**
   * Calcula resumo financeiro usando services existentes (fallback)
   */
  private async calculateFinanceSummary(clinicId?: string): Promise<FinanceSummary> {
    try {
      const appointments = await appointmentService.getAppointments(clinicId);

      const now = new Date();
      const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);

      const monthAppointments = appointments.filter(
        (apt) => new Date(apt.scheduledAt) >= startOfMonth && apt.status === 'completed'
      );

      // Sem dados de pagamento no modelo atual, retornar zeros
      const monthlyRevenue = 0;
      const averagePerSession = 0;
      const unpaidSessions = appointments.filter((apt) => apt.status !== 'paid').length;

      return {
        monthlyRevenue,
        averagePerSession,
        unpaidSessions,
        isEmpty: monthAppointments.length === 0,
      };
    } catch (error) {
      return {
        monthlyRevenue: 0,
        averagePerSession: 0,
        unpaidSessions: 0,
        isEmpty: true,
      };
    }
  }

  private handleError(error: unknown): ApiError {
    if (error && typeof error === 'object' && 'status' in error) {
      return error as ApiError;
    }
    return {
      message: 'Erro ao buscar dados do dashboard',
      status: 0,
    };
  }
}

export const dashboardService = new DashboardService();
