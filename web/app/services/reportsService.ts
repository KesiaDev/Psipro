/**
 * Serviço de Relatórios
 * Consome GET /reports do backend
 */

import { api } from "./api";

export interface ReportsSummary {
  totalPatients: number;
  totalSessions: number;
  totalRevenue: number;
  totalExpenses: number;
  todaySessions: number;
}

export interface ReportsData {
  monthlySessions: Array<{ month: string; sessions: number }>;
  revenue: Array<{ month: string; value: number }>;
  revenueData: Array<{ month: string; value: number }>;
  sessionTypes: Array<{ name: string; value: number }>;
  typeData: Array<{ name: string; value: number }>;
  topPatients: Array<{ name: string; sessions: number; percentage: number }>;
  stats: {
    totalSessions: number;
    activePatients: number;
    returnRate: number;
    avgHoursPerWeek: number;
  };
}

class ReportsService {
  async findAll(): Promise<ReportsData> {
    return api.get<ReportsData>("/reports");
  }

  async getSummary(): Promise<ReportsSummary> {
    return api.get<ReportsSummary>("/reports/summary");
  }

  async getToday(): Promise<{ todaySessions: number; sessions: unknown[] }> {
    return api.get("/reports/today");
  }

  async getStats(): Promise<ReportsSummary> {
    return api.get<ReportsSummary>("/reports/stats");
  }
}

export const reportsService = new ReportsService();
