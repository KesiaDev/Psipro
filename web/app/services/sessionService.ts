/**
 * Serviço de Sessões
 *
 * GET /sessions?patientId=xxx
 */

import { api, ApiError } from "./api";

export interface Session {
  id: string;
  patientId: string;
  userId?: string;
  date: string;
  duration?: number;
  status?: string;
  notes?: string;
  patient?: { id: string; name: string };
  createdAt?: string;
  updatedAt?: string;
}

class SessionService {
  async getByPatient(patientId: string): Promise<Session[]> {
    try {
      return await api.get<Session[]>(
        `/sessions?patientId=${encodeURIComponent(patientId)}`
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
      message: "Erro ao carregar sessões",
      status: 0,
    };
  }
}

export const sessionService = new SessionService();
