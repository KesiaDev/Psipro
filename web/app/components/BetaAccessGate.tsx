"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import BetaAccessForm from "./BetaAccessForm";

/**
 * Componente de Verificação de Acesso Beta
 * 
 * Exibe uma tela quando o usuário não tem acesso liberado ao beta.
 * Permite solicitar acesso ou verificar status.
 */
export default function BetaAccessGate({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const [hasAccess, setHasAccess] = useState<boolean | null>(null);
  const [showForm, setShowForm] = useState(false);

  useEffect(() => {
    // Verificar se o usuário tem acesso liberado
    // TODO: Substituir por verificação real via API quando disponível
    const checkAccess = async () => {
      const token = localStorage.getItem("psipro_token");
      
      if (!token) {
        // Sem token = sem acesso
        setHasAccess(false);
        return;
      }

      // TODO: Fazer requisição à API para verificar se o usuário tem acesso beta
      // Exemplo de implementação futura:
      // try {
      //   const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:3000';
      //   const response = await fetch(`${apiUrl}/api/auth/beta-status`, {
      //     headers: { 'Authorization': `Bearer ${token}` }
      //   });
      //   const data = await response.json();
      //   setHasAccess(data.hasAccess === true);
      // } catch (error) {
      //   setHasAccess(false);
      // }
      
      // Por enquanto, para desenvolvimento: se tem token, assume que tem acesso
      // Em produção, isso DEVE ser verificado no backend
      // O backend deve ter uma lista de usuários com acesso beta liberado
      setHasAccess(true);
    };

    checkAccess();
  }, []);

  // Mostrar loading enquanto verifica
  if (hasAccess === null) {
    return (
      <div className="min-h-screen bg-psipro-background flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-psipro-primary mx-auto mb-4"></div>
          <p className="text-psipro-text-secondary">Verificando acesso...</p>
        </div>
      </div>
    );
  }

  // Se não tem acesso, mostrar tela de bloqueio
  if (!hasAccess) {
    return (
      <div className="min-h-screen bg-psipro-background flex items-center justify-center p-4">
        <div className="max-w-md w-full bg-psipro-surface-elevated rounded-lg border border-psipro-border p-8 text-center">
          <div className="text-5xl mb-6">🔒</div>
          <h1 className="text-2xl font-bold text-psipro-text mb-4">
            O PsiPro está em beta fechado
          </h1>
          <p className="text-psipro-text-secondary mb-6 leading-relaxed">
            Seu acesso ainda não foi liberado. Avisaremos por e-mail assim que estiver disponível.
          </p>
          
          <div className="space-y-3">
            <button
              onClick={() => setShowForm(true)}
              className="w-full px-6 py-3 bg-psipro-primary text-white rounded-lg hover:bg-psipro-primary-dark transition-colors font-medium"
            >
              Solicitar acesso
            </button>
            <Link
              href="/beta"
              className="block w-full px-6 py-3 bg-psipro-surface border border-psipro-border text-psipro-text rounded-lg hover:bg-psipro-surface-elevated transition-colors font-medium"
            >
              Saiba mais sobre o beta
            </Link>
            <Link
              href="/"
              className="block text-sm text-psipro-text-secondary hover:text-psipro-text transition-colors mt-4"
            >
              Voltar à página inicial
            </Link>
          </div>
        </div>

        {/* Formulário de solicitação */}
        {showForm && (
          <BetaAccessForm
            isOpen={showForm}
            onClose={() => setShowForm(false)}
            onSuccess={() => {
              setShowForm(false);
              // Pode mostrar mensagem de sucesso
            }}
          />
        )}
      </div>
    );
  }

  // Se tem acesso, renderizar conteúdo normalmente
  return <>{children}</>;
}

