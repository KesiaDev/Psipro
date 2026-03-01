/**
 * Serviço Financeiro
 *
 * Consome exclusivamente a API real (GET /financial/summary, GET /financial/patient/:id).
 * Sem fallbacks nem dados mockados.
 */

import { api, ApiError } from "./api";

export interface FinancialSummary {
  receitaHoje: number;
  receitaMes: number;
  totalRecebido: number;
  totalAReceber: number;
  ticketMedio: number;
}

export interface PatientFinancialSummary {
  totalFaturado: number;
  totalRecebido: number;
  totalAberto: number;
}

class FinancialService {
  /**
   * GET /financial/summary
   * Requer X-Clinic-Id e Authorization.
   */
  async getSummary(): Promise<FinancialSummary> {
    try {
      return await api.get<FinancialSummary>("/financial/summary");
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * GET /financial/patient/:patientId
   */
  async getPatientFinancial(patientId: string): Promise<PatientFinancialSummary> {
    try {
      return await api.get<PatientFinancialSummary>(
        `/financial/patient/${patientId}`
      );
    } catch (error) {
      throw this.handleError(error);
    }
  }

  private handleError(error: unknown): ApiError {
    if (error && typeof error === "object" && "status" in error) {
      return error as ApiError;
    }
    return {
      message: "Erro ao carregar dados financeiros",
      status: 0,
    };
  }
}

export const financialService = new FinancialService();
