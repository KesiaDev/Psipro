/**
 * ⚠️ ARQUIVO CRÍTICO - INTEGRAÇÃO BACKEND
 *
 * Este arquivo contém lógica essencial de integração com API,
 * autenticação ou variáveis de ambiente.
 *
 * NÃO alterar estrutura, headers, interceptors ou contratos de API
 * durante modernização visual.
 *
 * Qualquer alteração pode quebrar produção.
 */

/**
 * Cliente HTTP centralizado para comunicação com a API do PsiPro
 *
 * - Base URL via NEXT_PUBLIC_API_URL (lib/env.ts)
 * - Axios com interceptor JWT automático (Bearer token do localStorage)
 * - Interceptação de erros (401, 403, 500)
 * - Retorno de erros normalizados
 */

import axios, { AxiosInstance, AxiosError } from 'axios';
import { getApiUrl } from '@/lib/env';

export interface ApiError {
  message: string;
  status: number;
  errors?: Record<string, string[]>;
}

const TOKEN_KEY = 'psipro_token';

function getToken(): string | null {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem(TOKEN_KEY);
}

function buildApiClient(): AxiosInstance {
  const client = axios.create({
    baseURL: getApiUrl(),
    headers: {
      'Content-Type': 'application/json',
    },
    timeout: 30000,
  });

  // Interceptor de REQUEST: injeta JWT em todas as requisições
  client.interceptors.request.use(
    (config) => {
      const token = getToken();
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      // FormData: remover Content-Type para o browser definir boundary
      if (typeof FormData !== 'undefined' && config.data instanceof FormData) {
        delete config.headers['Content-Type'];
      }
      return config;
    },
    (error) => Promise.reject(error)
  );

  // Interceptor de RESPONSE: trata 401 e normaliza erros
  client.interceptors.response.use(
    (response) => response,
    (error: AxiosError<{ message?: string; errors?: Record<string, string[]> }>) => {
      const status = error.response?.status ?? 0;
      const data = error.response?.data;

      // 401: token inválido ou expirado - limpar localStorage
      if (status === 401 && typeof window !== 'undefined') {
        localStorage.removeItem(TOKEN_KEY);
      }

      const apiError: ApiError = {
        message: data?.message || error.message || 'Erro na requisição',
        status,
        errors: data?.errors,
      };

      return Promise.reject(apiError);
    }
  );

  return client;
}

const axiosClient = buildApiClient();

class ApiClient {
  async get<T>(endpoint: string, params?: Record<string, string>): Promise<T> {
    const res = await axiosClient.get<T>(endpoint, { params });
    return res.data;
  }

  async post<T>(endpoint: string, data?: unknown): Promise<T> {
    const res = await axiosClient.post<T>(endpoint, data);
    return res.data;
  }

  async postFormData<T>(endpoint: string, formData: FormData): Promise<T> {
    const res = await axiosClient.post<T>(endpoint, formData);
    return res.data;
  }

  async put<T>(endpoint: string, data?: unknown): Promise<T> {
    const res = await axiosClient.put<T>(endpoint, data);
    return res.data;
  }

  async patch<T>(endpoint: string, data?: unknown): Promise<T> {
    const res = await axiosClient.patch<T>(endpoint, data);
    return res.data;
  }

  async delete<T>(endpoint: string): Promise<T> {
    const res = await axiosClient.delete<T>(endpoint);
    return res.data;
  }
}

export const api = new ApiClient();
