import { api, ApiError } from './api';

/** Mensagem amigável para conflito de agenda (409). Usar em toasts/feedback. */
export const APPOINTMENT_CONFLICT_MESSAGE =
  'Já existe uma consulta agendada neste horário. Escolha outro horário.';

/** Verifica se o erro é de conflito de agenda (409). */
export function isAppointmentConflictError(e: unknown): boolean {
  return e && typeof e === 'object' && 'status' in e && (e as ApiError).status === 409;
}

export interface Appointment {
  id: string;
  patientId: string;
  userId?: string;
  clinicId?: string;
  scheduledAt: string;
  duration?: number;
  type?: string;
  status?: string;
  notes?: string;
  patient?: {
    id: string;
    name: string;
  };
  user?: {
    id: string;
    name: string;
  };
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateAppointmentDto {
  patientId: string;
  scheduledAt: string;
  duration?: number;
  type?: string;
  status?: string;
  notes?: string;
  clinicId?: string;
}

export interface UpdateAppointmentDto {
  scheduledAt?: string;
  duration?: number;
  type?: string;
  status?: string;
  notes?: string;
}

class AppointmentService {
  /**
   * Lista consultas do usuário, opcionalmente filtrado por clínica
   */
  async getAppointments(clinicId?: string): Promise<Appointment[]> {
    try {
      const params = clinicId ? { clinicId } : undefined;
      return await api.get<Appointment[]>('/appointments', params);
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Busca detalhes de uma consulta específica
   */
  async getAppointmentById(id: string): Promise<Appointment> {
    try {
      return await api.get<Appointment>(`/appointments/${id}`);
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Cria uma nova consulta
   */
  async createAppointment(data: CreateAppointmentDto): Promise<Appointment> {
    try {
      return await api.post<Appointment>('/appointments', data);
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Atualiza dados de uma consulta
   */
  async updateAppointment(
    id: string,
    data: UpdateAppointmentDto
  ): Promise<Appointment> {
    try {
      return await api.put<Appointment>(`/appointments/${id}`, data);
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Remove uma consulta
   */
  async deleteAppointment(id: string): Promise<void> {
    try {
      return await api.delete(`/appointments/${id}`);
    } catch (error) {
      throw this.handleError(error);
    }
  }

  private handleError(error: unknown): ApiError {
    if (error && typeof error === 'object' && 'status' in error) {
      const apiError = error as ApiError;
      // B4: Tratar 409 Conflict - conflito de agenda
      if (apiError.status === 409) {
        return { message: APPOINTMENT_CONFLICT_MESSAGE, status: 409 };
      }
      return apiError;
    }
    return {
      message: 'Erro desconhecido',
      status: 0,
    };
  }
}

export const appointmentService = new AppointmentService();



