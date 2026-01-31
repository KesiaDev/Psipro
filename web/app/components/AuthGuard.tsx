"use client";

import { useEffect } from "react";
import { useRouter, usePathname } from "next/navigation";
import { useAuth } from "../contexts/AuthContext";

interface AuthGuardProps {
  children: React.ReactNode;
}

/**
 * Componente para proteger rotas autenticadas
 * 
 * Redireciona para /login se o usuário não estiver autenticado
 */
export default function AuthGuard({ children }: AuthGuardProps) {
  const router = useRouter();
  const pathname = usePathname();
  const { isAuthenticated, loading } = useAuth();

  useEffect(() => {
    if (!loading && !isAuthenticated) {
      // Salvar a rota atual para redirecionar após login
      const returnUrl = pathname !== "/login" && pathname !== "/register" ? pathname : "/dashboard";
      router.push(`/login?returnUrl=${encodeURIComponent(returnUrl)}`);
    }
  }, [isAuthenticated, loading, router, pathname]);

  // Mostrar loading enquanto verifica autenticação
  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-psipro-background">
        <div className="text-psipro-text-secondary">Carregando...</div>
      </div>
    );
  }

  // Não renderizar se não estiver autenticado (será redirecionado)
  if (!isAuthenticated) {
    return null;
  }

  return <>{children}</>;
}
