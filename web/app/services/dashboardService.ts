/**
 * Serviço de Dashboard
 *
 * Consome exclusivamente a API real via api.ts.
 * Sem fallbacks. Sem dados mockados.
 *
 * Endpoints:
 * - GET /patients/count
 * - GET /appointments/today
 * - GET /sessions/stats
 * - GET /financial/summary
 *
 * Authorization e X-Clinic-Id enviados automaticamente pelo api.ts.
 */

import { api, ApiError } from "./api";

export interface PatientsCount {
  /** Número retornado diretamente quando o backend retorna number */
  count?: number;
}

export interface AppointmentsToday {
  count: number;
  items: Array<{
    id: string;
    patientId: string;
    scheduledAt: string;
    duration?: number;
    status?: string;
    patient?: { id: string; name: string };
  }>;
}

export interface SessionsStats {
  sessionsThisMonth: number;
  sessionsThisWeek: number;
}

export interface FinancialSummary {
  receitaHoje: number;
  receitaMes: number;
  totalRecebido: number;
  totalAReceber: number;
  ticketMedio: number;
}

class DashboardService {
  /**
   * GET /patients/count
   * Retorna o total de pacientes ativos da clínica.
   */
  async getPatientsCount(): Promise<number> {
    try {
      const res = await api.get<number | { count: number }>("/patients/count");
      return typeof res === "number" ? res : (res?.count ?? 0);
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * GET /appointments/today
   * Retorna consultas agendadas para hoje.
   */
  async getAppointmentsToday(): Promise<AppointmentsToday> {
    try {
      return await api.get<AppointmentsToday>("/appointments/today");
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * GET /sessions/stats
   * Retorna estatísticas de sessões realizadas.
   */
  async getSessionsStats(): Promise<SessionsStats> {
    try {
      return await api.get<SessionsStats>("/sessions/stats");
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * GET /financial/summary
   * Retorna resumo financeiro (receitaHoje, receitaMes, etc). Requer admin ou psychologist.
   */
  async getFinancialSummary(): Promise<FinancialSummary> {
    try {
      return await api.get<FinancialSummary>("/financial/summary");
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * GET /dashboard/finance-summary
   * Retorna resumo financeiro da página Financeiro (monthlyRevenue, averagePerSession, etc).
   */
  async getFinanceSummary(): Promise<{
    monthlyRevenue: number;
    averagePerSession: number;
    unpaidSessions: number;
    isEmpty: boolean;
  }> {
    try {
      return await api.get("/dashboard/finance-summary");
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * GET /dashboard/metrics
   * Retorna métricas do dashboard (activePatients, sessionsThisMonth, etc).
   */
  async getMetrics(): Promise<{
    activePatients: number;
    sessionsThisMonth: number;
    sessionsThisWeek: number;
    monthlyRevenue: number;
    pendingRevenue: number;
  }> {
    try {
      return await api.get("/dashboard/metrics");
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * GET /dashboard/agenda-summary
   * Retorna resumo da agenda (totalSessionsThisWeek, busiestDays, etc).
   */
  async getAgendaSummary(): Promise<{
    totalSessionsThisWeek: number;
    busiestDays: string[];
    emptiestDays: string[];
    isEmpty: boolean;
  }> {
    try {
      return await api.get("/dashboard/agenda-summary");
    } catch (error) {
      throw this.handleError(error);
    }
  }

  private handleError(error: unknown): ApiError {
    if (error && typeof error === "object" && "status" in error) {
      return error as ApiError;
    }
    return {
      message: "Erro ao buscar dados do dashboard",
      status: 0,
    };
  }
}

export const dashboardService = new DashboardService();
