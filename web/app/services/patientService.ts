import { api, ApiError } from './api';

export interface Patient {
  id: string;
  name: string;
  /**
   * Campos auxiliares que podem vir pré-calculados da API
   * (o Web também pode exibir quando presentes).
   */
  age?: number;
  nextSession?: string;
  sessionsCount?: number;
  cpf?: string;
  phone?: string;
  email?: string;
  birthDate?: string;
  address?: string;
  emergencyContact?: string;
  observations?: string;
  status?: string;
  type?: string;
  source?: string;
  clinicId?: string;
  userId?: string;
  sharedWith?: string[];
  createdAt?: string;
  updatedAt?: string;
  lastSyncedAt?: string;
}

export type PatientOrigin = 'ANDROID' | 'WEB';

export interface SyncPatientDto {
  id?: string;
  clinicId: string;
  name: string;
  birthDate?: string;
  cpf?: string;
  phone?: string;
  email?: string;
  address?: string;
  emergencyContact?: string;
  observations?: string;
  status?: string;
  type?: string;
  sharedWith?: string[];
  origin?: PatientOrigin;
  source?: 'app' | 'web';
  updatedAt: string;
  createdAt?: string;
}

export interface CreatePatientDto {
  name: string;
  cpf?: string;
  phone?: string;
  email?: string;
  birthDate?: string;
  address?: string;
  emergencyContact?: string;
  observations?: string;
  status?: string;
  type?: string;
  source?: string;
  clinicId?: string;
  sharedWith?: string[];
}

export interface UpdatePatientDto {
  name?: string;
  cpf?: string;
  phone?: string;
  email?: string;
  birthDate?: string;
  address?: string;
  emergencyContact?: string;
  observations?: string;
  status?: string;
  type?: string;
  sharedWith?: string[];
}

class PatientService {
  /**
   * Lista pacientes da clínica ativa.
   * Usa GET /api/patients — clinicId vem do header X-Clinic-Id automaticamente.
   */
  async getPatients(clinicId?: string): Promise<Patient[]> {
    return await api.get<Patient[]>('/patients');
  }

  /**
   * Busca detalhes de um paciente específico
   */
  async getPatientById(id: string): Promise<Patient> {
    try {
      return await api.get<Patient>(`/patients/${id}`);
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Cria um novo paciente.
   * POST /api/patients — clinicId vem do header X-Clinic-Id automaticamente.
   */
  async createPatient(data: CreatePatientDto): Promise<Patient> {
    try {
      return await api.post<Patient>('/patients', data);
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Upsert de pacientes para sync bidirecional (Android <-> Web).
   * POST /sync/patients?clinicId=...
   */
  async syncPatients(clinicId: string, patients: SyncPatientDto[]): Promise<Patient[]> {
    try {
      return await api.post<Patient[]>(`/sync/patients?clinicId=${encodeURIComponent(clinicId)}`, {
        patients,
      });
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Importa pacientes via Excel (multipart/form-data).
   * Retorna a lista criada pelo backend (persistida).
   * clinicId vem do header X-Clinic-Id automaticamente.
   */
  async importPatients(
    file: File,
    mapping: Record<string, string>,
  ): Promise<Patient[]> {
    try {
      const formData = new FormData();
      formData.append('file', file);
      formData.append('mapping', JSON.stringify(mapping));

      return await api.postFormData<Patient[]>('/patients/import', formData);
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Atualiza dados de um paciente
   */
  async updatePatient(id: string, data: UpdatePatientDto): Promise<Patient> {
    try {
      return await api.patch<Patient>(`/patients/${id}`, data);
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Remove um paciente
   */
  async deletePatient(id: string): Promise<void> {
    try {
      return await api.delete(`/patients/${id}`);
    } catch (error) {
      throw this.handleError(error);
    }
  }

  private handleError(error: unknown): ApiError {
    if (error && typeof error === 'object' && 'status' in error) {
      return error as ApiError;
    }
    return {
      message: 'Erro desconhecido',
      status: 0,
    };
  }
}

export const patientService = new PatientService();



