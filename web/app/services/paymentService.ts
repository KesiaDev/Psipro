/**
 * Serviço de Pagamentos
 *
 * GET /payments/patient/:patientId
 * POST /payments
 */

import { api, ApiError } from "./api";

export interface Payment {
  id: string;
  patientId: string;
  sessionId?: string;
  amount: number;
  date: string;
  status: string;
  source?: string;
  createdAt?: string;
  updatedAt?: string;
}

class PaymentService {
  async getByPatient(patientId: string): Promise<Payment[]> {
    try {
      return await api.get<Payment[]>(
        `/payments/patient/${encodeURIComponent(patientId)}`
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
      message: "Erro ao carregar pagamentos",
      status: 0,
    };
  }
}

export const paymentService = new PaymentService();
