import { api, ApiError } from './api';

export interface Patient {
  id: string;
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
  userId?: string;
  sharedWith?: string[];
  createdAt?: string;
  updatedAt?: string;
  lastSyncedAt?: string;
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
   * Lista pacientes do usuário, opcionalmente filtrado por clínica
   */
  async getPatients(clinicId?: string): Promise<Patient[]> {
    try {
      const params = clinicId ? { clinicId } : undefined;
      return await api.get<Patient[]>('/patients', params);
    } catch (error) {
      throw this.handleError(error);
    }
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
   * Cria um novo paciente
   */
  async createPatient(data: CreatePatientDto): Promise<Patient> {
    try {
      return await api.post<Patient>('/patients', data);
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


