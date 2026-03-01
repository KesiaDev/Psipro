/**
 * Serviço de Dashboard
 *
 * Consome exclusivamente a API real:
 * - GET /dashboard/metrics
 * - GET /dashboard/agenda-summary
 * - GET /dashboard/finance-summary
 *
 * Sem fallbacks. Sem dados mockados.
 */

import { api, ApiError } from './api';

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
   * GET /dashboard/metrics
   * X-Clinic-Id e Authorization via api client.
   */
  async getMetrics(): Promise<DashboardMetrics> {
    try {
      return await api.get<DashboardMetrics>('/dashboard/metrics');
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * GET /dashboard/agenda-summary
   */
  async getAgendaSummary(): Promise<AgendaSummary> {
    try {
      return await api.get<AgendaSummary>('/dashboard/agenda-summary');
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * GET /dashboard/finance-summary
   */
  async getFinanceSummary(): Promise<FinanceSummary> {
    try {
      return await api.get<FinanceSummary>('/dashboard/finance-summary');
    } catch (error) {
      throw this.handleError(error);
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
