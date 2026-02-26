"use client";

/**
 * Error Boundary global do Next.js App Router.
 * Captura erros não tratados na árvore de componentes (incluindo layout).
 * Renderiza fora do layout principal (substitui todo o body).
 */

export default function GlobalError({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  return (
    <html lang="pt-BR">
      <body className="min-h-screen flex items-center justify-center bg-neutral-100 dark:bg-neutral-900 p-4">
        <div className="max-w-md w-full bg-white dark:bg-neutral-800 border border-neutral-200 dark:border-neutral-700 rounded-xl p-6 shadow-lg">
          <div className="text-center">
            <span className="text-4xl mb-4 block" aria-hidden>
              ⚠️
            </span>
            <h1 className="text-xl font-semibold text-neutral-900 dark:text-neutral-100 mb-2">
              Erro inesperado
            </h1>
            <p className="text-neutral-600 dark:text-neutral-400 mb-6">
              Ocorreu um erro. Por favor, tente novamente ou recarregue a página.
            </p>
            <div className="flex gap-3 justify-center flex-wrap">
              <button
                onClick={reset}
                className="px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition-colors"
              >
                Tentar novamente
              </button>
              <button
                onClick={() => window.location.assign("/")}
                className="px-4 py-2 bg-neutral-200 dark:bg-neutral-700 text-neutral-900 dark:text-neutral-100 rounded-lg hover:bg-neutral-300 dark:hover:bg-neutral-600 transition-colors"
              >
                Ir para início
              </button>
              <button
                onClick={() => window.location.reload()}
                className="px-4 py-2 border border-neutral-300 dark:border-neutral-600 text-neutral-700 dark:text-neutral-300 rounded-lg hover:bg-neutral-100 dark:hover:bg-neutral-700 transition-colors"
              >
                Recarregar página
              </button>
            </div>
            {process.env.NODE_ENV === "development" && error && (
              <details className="mt-6 text-left">
                <summary className="text-sm text-neutral-500 cursor-pointer">
                  Detalhes do erro
                </summary>
                <pre className="mt-2 text-xs text-neutral-600 dark:text-neutral-400 bg-neutral-100 dark:bg-neutral-900 p-3 rounded overflow-auto max-h-32">
                  {error.message}
                  {error.stack}
                </pre>
              </details>
            )}
          </div>
        </div>
      </body>
    </html>
  );
}
