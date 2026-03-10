/**
 * Cliente HTTP centralizado para comunicação com a API do PsiPro
 *
 * Todas as chamadas usam a base URL do backend (NEXT_PUBLIC_API_URL).
 * Em produção (Railway): NEXT_PUBLIC_API_URL=https://psipro-backend-production.up.railway.app/api
 *
 * - Base URL via NEXT_PUBLIC_API_URL (obrigatório em produção)
 * - Token JWT automático via localStorage (psipro_token)
 * - Interceptação de 401: tenta refresh token, retry único, ou redireciona para /login
 * - Interceptação de erros (403, 500)
 */

const API_BASE_URL = (process.env.NEXT_PUBLIC_API_URL || 'http://localhost:3001/api').replace(/\/+$/, '');

/** URL base da API (para uso em handoff/test que não usam o cliente api). */
export function getApiBaseUrl(): string {
  return (process.env.NEXT_PUBLIC_API_URL || 'http://localhost:3001/api').replace(/\/+$/, '');
}

export interface ApiError {
  message: string;
  status: number;
  errors?: Record<string, string[]>;
}

class ApiClient {
  private baseURL: string;

  constructor(baseURL: string) {
    this.baseURL = baseURL;
  }

  private getToken(): string | null {
    if (typeof window === 'undefined') return null;
    return localStorage.getItem('psipro_token');
  }

  private getActiveClinicId(): string | null {
    if (typeof window === 'undefined') return null;
    // active_clinic_id = login normal; psipro_current_clinic_id = handoff do app
    return (
      localStorage.getItem('active_clinic_id') ||
      localStorage.getItem('psipro_current_clinic_id')
    );
  }

  private async request<T>(
    endpoint: string,
    options: RequestInit = {},
    isRetry = false
  ): Promise<T> {
    const token = this.getToken();
    const path = endpoint.startsWith('/') ? endpoint : `/${endpoint}`;
    const url = `${this.baseURL.replace(/\/+$/, '')}${path}`;

    const isFormData =
      typeof FormData !== 'undefined' && options.body instanceof FormData;

    const headers: Record<string, string> = {
      ...((options.headers as Record<string, string>) || {}),
    };

    // Só define Content-Type JSON quando NÃO é multipart/form-data.
    // (Para FormData, o browser injeta boundary automaticamente)
    if (!isFormData && !('Content-Type' in headers)) {
      headers['Content-Type'] = 'application/json';
    }

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    // X-Clinic-Id enviado em toda requisição (incluindo retry pós-refresh)
    const activeClinicId = this.getActiveClinicId();
    if (activeClinicId) {
      headers['X-Clinic-Id'] = activeClinicId;
    }

    try {
      const response = await fetch(url, {
        ...options,
        headers,
      });

      // Tratar erros HTTP
      if (!response.ok) {
        const errorData = await response.json().catch(() => ({
          message: response.statusText,
        }));

        const error: ApiError = {
          message: errorData.message || 'Erro na requisição',
          status: response.status,
          errors: errorData.errors,
        };

        // Interceptar 401 (não autenticado): tentar refresh ou redirecionar
        if (response.status === 401) {
          if (!isRetry && typeof window !== 'undefined') {
            try {
              const { authService } = await import('./authService');
              await authService.refreshToken();
              return this.request<T>(endpoint, options, true);
            } catch {
              const { authService } = await import('./authService');
              authService.logout();
              if (typeof window !== 'undefined') window.location.href = '/login';
            }
          } else {
            if (typeof window !== 'undefined') {
              const { authService } = await import('./authService');
              authService.logout();
              window.location.href = '/login';
            }
          }
        }

        throw error;
      }

      // Retornar dados
      // Algumas respostas podem ser 204 ou sem corpo.
      const text = await response.text();
      if (!text) return undefined as unknown as T;
      return JSON.parse(text) as T;
    } catch (error) {
      // Re-throw erros da API
      if (error && typeof error === 'object' && 'status' in error) {
        throw error;
      }

      // Erro de rede ou outro erro
      throw {
        message: 'Erro de conexão. Verifique sua internet.',
        status: 0,
      } as ApiError;
    }
  }

  async get<T>(endpoint: string, params?: Record<string, string>): Promise<T> {
    const queryString = params
      ? '?' + new URLSearchParams(params).toString()
      : '';
    return this.request<T>(endpoint + queryString, { method: 'GET' });
  }

  async post<T>(endpoint: string, data?: any): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'POST',
      body: data ? JSON.stringify(data) : undefined,
    });
  }

  async postFormData<T>(endpoint: string, formData: FormData): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'POST',
      body: formData,
    });
  }

  async put<T>(endpoint: string, data?: any): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'PUT',
      body: data ? JSON.stringify(data) : undefined,
    });
  }

  async patch<T>(endpoint: string, data?: any): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'PATCH',
      body: data ? JSON.stringify(data) : undefined,
    });
  }

  async delete<T>(endpoint: string): Promise<T> {
    return this.request<T>(endpoint, { method: 'DELETE' });
  }
}

export const api = new ApiClient(API_BASE_URL);

