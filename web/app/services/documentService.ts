/**
 * Serviço de Documentos
 *
 * GET /documents?patientId=xxx
 * Consome exclusivamente a API real. Sem fallbacks.
 */

import { api, ApiError } from "./api";

export interface Document {
  id: string;
  name: string;
  type: string;
  fileUrl?: string;
  fileSize?: number;
  mimeType?: string;
  status: string;
  createdAt?: string;
  patientId?: string;
  patient?: { id: string; name: string };
}

class DocumentService {
  async getByPatient(patientId: string): Promise<Document[]> {
    try {
      return await api.get<Document[]>(
        `/documents?patientId=${encodeURIComponent(patientId)}`
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
      message: "Erro ao carregar documentos",
      status: 0,
    };
  }
}

export const documentService = new DocumentService();
