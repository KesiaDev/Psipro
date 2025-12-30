/**
 * Cliente HTTP centralizado para comunicação com a API do PsiPro
 * 
 * Configurações:
 * - Base URL via NEXT_PUBLIC_API_URL
 * - Token JWT automático via localStorage
 * - Interceptação de erros (401, 403, 500)
 * - Retorno de erros normalizados
 */

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:3001/api';

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

  private async request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const token = this.getToken();
    const url = `${this.baseURL}${endpoint}`;

    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      ...(options.headers as Record<string, string> || {}),
    };

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
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

        // Interceptar 401 (não autenticado)
        if (response.status === 401) {
          // Limpar token inválido
          if (typeof window !== 'undefined') {
            localStorage.removeItem('psipro_token');
            // Redirecionar para login se necessário
            // window.location.href = '/login';
          }
        }

        throw error;
      }

      // Retornar dados
      const data = await response.json();
      return data;
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

