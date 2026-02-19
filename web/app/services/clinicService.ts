import { api, ApiError } from './api';

export interface Clinic {
  id: string;
  name: string;
  cnpj?: string;
  address?: string;
  phone?: string;
  email?: string;
  plan?: string;
  status?: string;
  role?: string;
  permissions?: {
    canViewAllPatients: boolean;
    canEditAllPatients: boolean;
    canViewFinancial: boolean;
    canManageUsers: boolean;
  };
  members?: Array<{
    id: string;
    name: string;
    email: string;
    role: string;
    status: string;
    joinedAt?: string;
  }>;
}

export interface CreateClinicDto {
  name: string;
  cnpj?: string;
  address?: string;
  phone?: string;
  email?: string;
}

export interface UpdateClinicDto {
  name?: string;
  cnpj?: string;
  address?: string;
  phone?: string;
  email?: string;
  plan?: string;
  status?: string;
}

export interface InviteUserDto {
  email: string;
  role?: string;
  canViewAllPatients?: boolean;
  canEditAllPatients?: boolean;
  canViewFinancial?: boolean;
  canManageUsers?: boolean;
}

export interface UpdateClinicUserDto {
  role?: string;
  status?: string;
  canViewAllPatients?: boolean;
  canEditAllPatients?: boolean;
  canViewFinancial?: boolean;
  canManageUsers?: boolean;
}

export interface ClinicStats {
  patients: number;
  appointments: number;
  sessions: number;
  revenue: number;
}

/** Resposta do POST /clinics: retorna apenas clinic (arquitetura multi-clinic). */
export interface CreateClinicResponse {
  clinic: Clinic;
}

class ClinicService {
  /**
   * Lista todas as clínicas do usuário autenticado
   */
  async getClinics(): Promise<Clinic[]> {
    try {
      return await api.get<Clinic[]>('/clinics');
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Busca detalhes de uma clínica específica
   */
  async getClinicById(id: string): Promise<Clinic> {
    try {
      return await api.get<Clinic>(`/clinics/${id}`);
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Cria uma nova clínica. Retorna clinic + accessToken (atualizar token no front).
   */
  async createClinic(data: CreateClinicDto): Promise<CreateClinicResponse> {
    try {
      return await api.post<CreateClinicResponse>('/clinics', data);
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Atualiza dados de uma clínica
   */
  async updateClinic(id: string, data: UpdateClinicDto): Promise<Clinic> {
    try {
      return await api.put<Clinic>(`/clinics/${id}`, data);
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Exclui uma clínica (apenas owner/admin)
   */
  async deleteClinic(id: string): Promise<void> {
    try {
      await api.delete(`/clinics/${id}`);
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Convidar usuário para a clínica
   */
  async inviteUser(clinicId: string, data: InviteUserDto): Promise<any> {
    try {
      return await api.post(`/clinics/${clinicId}/invite`, data);
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Atualizar membro da clínica (permissões, role, etc)
   */
  async updateClinicUser(
    clinicId: string,
    userId: string,
    data: UpdateClinicUserDto
  ): Promise<any> {
    try {
      return await api.put(`/clinics/${clinicId}/users/${userId}`, data);
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Remover membro da clínica
   */
  async removeClinicUser(clinicId: string, userId: string): Promise<void> {
    try {
      return await api.delete(`/clinics/${clinicId}/users/${userId}`);
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Buscar estatísticas da clínica
   */
  async getClinicStats(clinicId: string): Promise<ClinicStats> {
    try {
      return await api.get<ClinicStats>(`/clinics/${clinicId}/stats`);
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

export const clinicService = new ClinicService();



