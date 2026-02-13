"use client";

import React from "react";

interface ErrorBoundaryState {
  hasError: boolean;
  error?: Error;
}

interface ErrorBoundaryProps {
  children: React.ReactNode;
  fallback?: React.ComponentType<{ error?: Error; resetError: () => void }>;
}

/**
 * Error Boundary para capturar erros de renderização
 * 
 * Usado para prevenir que erros quebrem toda a aplicação
 */
export class ErrorBoundary extends React.Component<ErrorBoundaryProps, ErrorBoundaryState> {
  constructor(props: ErrorBoundaryProps) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError(error: Error): ErrorBoundaryState {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    // Log do erro (em produção, enviar para serviço de monitoramento)
    console.error("ErrorBoundary capturou um erro:", error, errorInfo);
  }

  resetError = () => {
    this.setState({ hasError: false, error: undefined });
  };

  render() {
    if (this.state.hasError) {
      if (this.props.fallback) {
        const FallbackComponent = this.props.fallback;
        return <FallbackComponent error={this.state.error} resetError={this.resetError} />;
      }

      // Fallback padrão
      return (
        <div className="min-h-screen flex items-center justify-center bg-psipro-background p-4">
          <div className="max-w-md w-full bg-psipro-surface-elevated border border-psipro-border rounded-lg p-6">
            <div className="text-center">
              <div className="text-4xl mb-4">⚠️</div>
              <h2 className="text-xl font-semibold text-psipro-text mb-2">
                Algo deu errado
              </h2>
              <p className="text-psipro-text-secondary mb-6">
                Ocorreu um erro inesperado. Por favor, tente recarregar a página.
              </p>
              <div className="flex gap-3 justify-center">
                <button
                  onClick={this.resetError}
                  className="px-4 py-2 bg-psipro-primary text-white rounded-lg hover:bg-psipro-primary-dark transition-colors"
                >
                  Tentar novamente
                </button>
                <button
                  onClick={() => window.location.reload()}
                  className="px-4 py-2 bg-psipro-surface border border-psipro-border text-psipro-text rounded-lg hover:bg-psipro-surface-elevated transition-colors"
                >
                  Recarregar página
                </button>
              </div>
              {process.env.NODE_ENV === "development" && this.state.error && (
                <details className="mt-4 text-left">
                  <summary className="text-sm text-psipro-text-secondary cursor-pointer">
                    Detalhes do erro (desenvolvimento)
                  </summary>
                  <pre className="mt-2 text-xs text-psipro-text-secondary bg-psipro-surface p-3 rounded overflow-auto">
                    {this.state.error.toString()}
                    {this.state.error.stack}
                  </pre>
                </details>
              )}
            </div>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}
