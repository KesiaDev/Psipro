"use client";

import React, { createContext, useContext, useState, useEffect, useCallback } from "react";
import { authService, type User, type LoginDto, type RegisterDto } from "../services/authService";

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  loading: boolean;
  error: string | null;
  login: (credentials: LoginDto) => Promise<void>;
  register: (data: RegisterDto) => Promise<void>;
  logout: () => void;
  refreshUser: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Carregar usuário ao inicializar
  useEffect(() => {
    loadUser();
  }, []);

  const loadUser = async () => {
    setLoading(true);
    setError(null);

    // Verificar se há token
    if (!authService.isAuthenticated()) {
      setUser(null);
      setLoading(false);
      return;
    }

    // Tentar obter usuário do cache
    const cachedUser = authService.getUser();
    if (cachedUser) {
      setUser(cachedUser);
      setLoading(false);
    }

    // Validar token e obter dados atualizados
    try {
      const userData = await authService.getCurrentUser();
      setUser(userData);
    } catch (err) {
      // Token inválido ou expirado
      setUser(null);
      authService.logout();
    } finally {
      setLoading(false);
    }
  };

  const login = async (credentials: LoginDto) => {
    setLoading(true);
    setError(null);

    try {
      const response = await authService.login(credentials);
      setUser(response.user);
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Erro ao fazer login';
      setError(message);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const register = async (data: RegisterDto) => {
    setLoading(true);
    setError(null);

    try {
      const response = await authService.register(data);
      setUser(response.user);
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Erro ao fazer registro';
      setError(message);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const logout = useCallback(async () => {
    const refreshToken = authService.getRefreshToken();
    if (refreshToken) {
      try {
        const baseUrl = (await import("../services/api")).getApiBaseUrl();
        await fetch(`${baseUrl}/auth/logout`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ refreshToken }),
        });
      } catch {
        // Ignorar erro de rede; limpar local de qualquer forma
      }
    }
    authService.logout();
    setUser(null);
    setError(null);
  }, []);

  const refreshUser = useCallback(async () => {
    if (!authService.isAuthenticated()) {
      setUser(null);
      return;
    }

    try {
      const userData = await authService.getCurrentUser();
      setUser(userData);
    } catch (err) {
      setUser(null);
      authService.logout();
    }
  }, []);

  const value: AuthContextType = {
    user,
    isAuthenticated: !!user,
    loading,
    error,
    login,
    register,
    logout,
    refreshUser,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth deve ser usado dentro de AuthProvider");
  }
  return context;
}
