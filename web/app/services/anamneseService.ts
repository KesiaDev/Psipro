/**
 * Serviço de Anamnese
 * Consome a API /anamnese do backend
 */

import { api } from "./api";

export interface AnamneseModel {
  id: string;
  nome: string;
  isDefault: boolean;
  clinicId?: string;
  campos?: AnamneseCampo[];
}

export interface AnamneseCampo {
  id: string;
  modeloId: string;
  tipo: string;
  label: string;
  opcoes?: string | null;
  obrigatorio: boolean;
  ordem: number;
}

export interface AnamnesePreenchida {
  id: string;
  patientId: string;
  modeloId: string;
  respostas: Record<string, unknown>;
  assinaturaPath?: string | null;
  data: string;
  versao: number;
  modelo?: { id: string; nome: string; campos?: AnamneseCampo[] };
}

class AnamneseService {
  async getModels(): Promise<AnamneseModel[]> {
    return api.get<AnamneseModel[]>("/anamnese/models");
  }

  async getPreenchidas(patientId: string): Promise<AnamnesePreenchida[]> {
    return api.get<AnamnesePreenchida[]>(`/anamnese/patients/${patientId}/preenchidas`);
  }

  async createPreenchida(data: {
    patientId: string;
    modeloId: string;
    respostas: Record<string, unknown>;
    assinaturaPath?: string;
  }): Promise<AnamnesePreenchida> {
    return api.post<AnamnesePreenchida>("/anamnese/preenchidas", data);
  }
}

export const anamneseService = new AnamneseService();
