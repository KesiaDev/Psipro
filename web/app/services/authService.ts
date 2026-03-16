/**
 * Serviço de Autenticação
 * 
 * Gerencia login, registro e gerenciamento de tokens
 */

import { api, getApiBaseUrl } from './api';

export interface LoginDto {
  email: string;
  password: string;
}

export interface RegisterDto {
  email: string;
  password: string;
  fullName: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: User;
}

export interface User {
  id: string;
  email: string;
  fullName: string;
  clinicId?: string;
  createdAt?: string;
}

class AuthService {
  private readonly TOKEN_KEY = 'psipro_token';
  private readonly REFRESH_TOKEN_KEY = 'psipro_refresh_token';
  private readonly USER_KEY = 'psipro_user';

  /**
   * Realiza login
   */
  async login(credentials: LoginDto): Promise<AuthResponse> {
    try {
      const response = await api.post<AuthResponse>('/auth/login', credentials);

      this.setToken(response.accessToken);
      this.setRefreshToken(response.refreshToken);
      this.setUser(response.user);
      if (response.user.clinicId && typeof window !== 'undefined') {
        localStorage.setItem('active_clinic_id', response.user.clinicId);
      }

      return response;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Realiza registro
   */
  async register(data: RegisterDto): Promise<AuthResponse> {
    try {
      const response = await api.post<AuthResponse>('/auth/register', data);

      this.setToken(response.accessToken);
      this.setRefreshToken(response.refreshToken);
      this.setUser(response.user);
      if (response.user.clinicId && typeof window !== 'undefined') {
        localStorage.setItem('active_clinic_id', response.user.clinicId);
      }

      return response;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Limpa tokens e dados locais. AuthContext chama logout no backend antes.
   */
  logout(): void {
    if (typeof window !== 'undefined') {
      localStorage.removeItem(this.TOKEN_KEY);
      localStorage.removeItem(this.REFRESH_TOKEN_KEY);
      localStorage.removeItem(this.USER_KEY);
      localStorage.removeItem('active_clinic_id');
    }
  }

  /**
   * Atualiza o access token usando o refresh token
   * @throws Error se não houver refresh token ou se o refresh falhar
   */
  async refreshToken(): Promise<string> {
    const refreshToken = localStorage.getItem(this.REFRESH_TOKEN_KEY);
    if (!refreshToken) {
      throw new Error('No refresh token');
    }

    const baseUrl = getApiBaseUrl();
    const response = await fetch(`${baseUrl}/auth/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken }),
    });

    if (!response.ok) {
      throw new Error('Refresh failed');
    }

    const data = (await response.json()) as {
      accessToken: string;
      refreshToken: string;
    };

    this.setToken(data.accessToken);
    this.setRefreshToken(data.refreshToken);

    return data.accessToken;
  }

  /**
   * Obtém o refresh token (para logout no backend)
   */
  getRefreshToken(): string | null {
    if (typeof window === 'undefined') return null;
    return localStorage.getItem(this.REFRESH_TOKEN_KEY);
  }

  /**
   * Obtém o token atual
   */
  getToken(): string | null {
    if (typeof window === 'undefined') return null;
    return localStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Obtém o usuário atual
   */
  getUser(): User | null {
    if (typeof window === 'undefined') return null;
    const userStr = localStorage.getItem(this.USER_KEY);
    if (!userStr) return null;
    
    try {
      return JSON.parse(userStr);
    } catch {
      return null;
    }
  }

  /**
   * Verifica se está autenticado
   */
  isAuthenticated(): boolean {
    return this.getToken() !== null;
  }

  /**
   * Define o access token
   */
  private setToken(token: string): void {
    if (typeof window !== 'undefined') {
      localStorage.setItem(this.TOKEN_KEY, token);
    }
  }

  /**
   * Define o refresh token
   */
  private setRefreshToken(token: string): void {
    if (typeof window !== 'undefined') {
      localStorage.setItem(this.REFRESH_TOKEN_KEY, token);
    }
  }

  /**
   * Define o usuário
   */
  private setUser(user: User): void {
    if (typeof window !== 'undefined') {
      localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    }
  }

  /**
   * Troca a clínica ativa. Chama POST /auth/switch-clinic e atualiza token + active_clinic_id.
   */
  async switchClinic(clinicId: string): Promise<void> {
    const response = await api.post<{ accessToken: string; clinicId: string }>(
      '/auth/switch-clinic',
      { clinicId },
    );
    this.setToken(response.accessToken);
    if (typeof window !== 'undefined') {
      localStorage.setItem('active_clinic_id', response.clinicId);
    }
  }

  /**
   * Obtém informações do usuário autenticado (valida token)
   */
  async getCurrentUser(): Promise<User> {
    try {
      const user = await api.get<User>('/auth/me');
      this.setUser(user); // Atualizar cache
      if (user.clinicId && typeof window !== 'undefined') {
        localStorage.setItem('active_clinic_id', user.clinicId);
      }
      return user;
    } catch (error) {
      // Se falhar, limpar tokens inválidos
      this.logout();
      throw this.handleError(error);
    }
  }

  /**
   * Trata erros da API
   */
  private handleError(error: unknown): Error {
    if (error && typeof error === 'object' && 'status' in error) {
      const apiError = error as { message?: string; status: number };
      return new Error(apiError.message || 'Erro na autenticação');
    }
    return error instanceof Error ? error : new Error('Erro desconhecido');
  }
}

export const authService = new AuthService();
