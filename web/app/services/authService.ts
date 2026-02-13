/**
 * Serviço de Autenticação
 * 
 * Gerencia login, registro e gerenciamento de tokens
 */

import { api } from './api';

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
  access_token: string;
  user: {
    id: string;
    email: string;
    fullName: string;
  };
}

export interface User {
  id: string;
  email: string;
  fullName: string;
  createdAt?: string;
}

class AuthService {
  private readonly TOKEN_KEY = 'psipro_token';
  private readonly USER_KEY = 'psipro_user';

  /**
   * Realiza login
   */
  async login(credentials: LoginDto): Promise<AuthResponse> {
    try {
      const response = await api.post<AuthResponse>('/auth/login', credentials);
      
      // Salvar token e usuário
      this.setToken(response.access_token);
      this.setUser(response.user);
      
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
      
      // Salvar token e usuário
      this.setToken(response.access_token);
      this.setUser(response.user);
      
      return response;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Realiza logout
   */
  logout(): void {
    if (typeof window !== 'undefined') {
      localStorage.removeItem(this.TOKEN_KEY);
      localStorage.removeItem(this.USER_KEY);
    }
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
   * Define o token
   */
  private setToken(token: string): void {
    if (typeof window !== 'undefined') {
      localStorage.setItem(this.TOKEN_KEY, token);
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
   * Obtém informações do usuário autenticado (valida token)
   */
  async getCurrentUser(): Promise<User> {
    try {
      const user = await api.get<User>('/auth/me');
      this.setUser(user); // Atualizar cache
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
