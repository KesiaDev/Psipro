"use client";

import { useState } from "react";
import { api, getApiBaseUrl } from "../services/api";
import { clinicService } from "../services/clinicService";
import { useToast } from "../contexts/ToastContext";
import { useClinic } from "../contexts/ClinicContext";

export default function TestPage() {
  const [token, setToken] = useState("");
  const [email, setEmail] = useState("owner@psiclinic.com");
  const [password, setPassword] = useState("senha123");
  const [loading, setLoading] = useState(false);
  const { showSuccess, showError, showInfo } = useToast();
  const { clinics, loading: clinicsLoading, refreshClinics } = useClinic();

  const handleLogin = async () => {
    setLoading(true);
    try {
      // Verificar se backend está acessível
      const apiUrl = getApiBaseUrl();
      const response = await fetch(`${apiUrl}/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email, password }),
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({ message: response.statusText }));
        throw new Error(errorData.message || `Erro ${response.status}: ${response.statusText}`);
      }

      const data = await response.json();
      const tokenValue = data.access_token || data.token;
      
      if (tokenValue) {
        localStorage.setItem("psipro_token", tokenValue);
        setToken(tokenValue);
        showSuccess("Login realizado com sucesso!");
        await refreshClinics();
      } else {
        showError("Token não encontrado na resposta. Resposta: " + JSON.stringify(data));
      }
    } catch (error: any) {
      console.error("Erro no login:", error);
      if (error.message?.includes('Failed to fetch') || error.message?.includes('NetworkError')) {
        showError("Erro de conexão. Verifique NEXT_PUBLIC_API_URL e se o backend está acessível.");
      } else {
        showError(error.message || "Erro ao fazer login");
      }
    } finally {
      setLoading(false);
    }
  };

  const handleSetToken = () => {
    if (token) {
      localStorage.setItem("psipro_token", token);
      showSuccess("Token salvo!");
      refreshClinics();
    }
  };

  const handleTestConnection = async () => {
    setLoading(true);
    try {
      // Verificar token primeiro
      const token = localStorage.getItem("psipro_token");
      if (!token) {
        showError("Token JWT não encontrado. Faça login primeiro.");
        setLoading(false);
        return;
      }

      const clinics = await clinicService.getClinics();
      showSuccess(`Conexão OK! ${clinics.length} clínica(s) encontrada(s)`);
    } catch (error: any) {
      console.error("Erro no teste:", error);
      if (error.status === 401) {
        showError("Token inválido ou expirado. Faça login novamente.");
      } else       if (error.status === 0 || error.message?.includes('Failed to fetch')) {
        showError("Erro de conexão. Verifique NEXT_PUBLIC_API_URL e se o backend está acessível.");
      } else {
        showError(`Erro: ${error.message} (Status: ${error.status || 'N/A'})`);
      }
    } finally {
      setLoading(false);
    }
  };

  const currentToken = typeof window !== "undefined" 
    ? localStorage.getItem("psipro_token") 
    : null;

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold text-psipro-text mb-8">
        🧪 Página de Teste - PsiPro
      </h1>

      {/* Status */}
      <div className="bg-psipro-surface-elevated border border-psipro-border rounded-lg p-6 mb-6">
        <h2 className="text-xl font-semibold text-psipro-text mb-4">Status</h2>
        <div className="space-y-2">
          <div className="flex items-center gap-2">
            <span className="text-psipro-text-secondary">Token JWT:</span>
            <span className={currentToken ? "text-psipro-success" : "text-psipro-error"}>
              {currentToken ? "✓ Presente" : "✗ Ausente"}
            </span>
          </div>
          <div className="flex items-center gap-2">
            <span className="text-psipro-text-secondary">API URL:</span>
            <span className="text-psipro-text">
              {getApiBaseUrl()}
            </span>
          </div>
          <div className="flex items-center gap-2">
            <span className="text-psipro-text-secondary">Backend Status:</span>
            <button
              onClick={async () => {
                try {
                  const apiUrl = getApiBaseUrl();
                  const response = await fetch(`${apiUrl}/auth/me`, {
                    headers: {
                      'Authorization': `Bearer ${currentToken || ''}`,
                    },
                  });
                  if (response.ok || response.status === 401) {
                    showInfo("Backend está respondendo!");
                  } else {
                    showError("Backend não está respondendo corretamente");
                  }
                } catch (error) {
                  showError("Backend não está acessível. Verifique se está rodando.");
                }
              }}
              className="text-xs px-2 py-1 bg-psipro-primary text-white rounded hover:bg-psipro-primary-dark"
            >
              Testar Backend
            </button>
          </div>
          <div className="flex items-center gap-2">
            <span className="text-psipro-text-secondary">Clínicas carregadas:</span>
            <span className="text-psipro-text">
              {clinicsLoading ? "Carregando..." : `${clinics.length} clínica(s)`}
            </span>
          </div>
        </div>
      </div>

      {/* Login */}
      <div className="bg-psipro-surface-elevated border border-psipro-border rounded-lg p-6 mb-6">
        <h2 className="text-xl font-semibold text-psipro-text mb-4">Login</h2>
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-psipro-text mb-2">
              Email
            </label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full px-4 py-2 bg-psipro-background border border-psipro-border rounded-lg text-psipro-text focus:outline-none focus:ring-2 focus:ring-psipro-primary"
              placeholder="email@exemplo.com"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-psipro-text mb-2">
              Senha
            </label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full px-4 py-2 bg-psipro-background border border-psipro-border rounded-lg text-psipro-text focus:outline-none focus:ring-2 focus:ring-psipro-primary"
              placeholder="senha"
            />
          </div>
          <button
            onClick={handleLogin}
            disabled={loading}
            className="w-full px-4 py-2 bg-psipro-primary text-white rounded-lg hover:bg-psipro-primary-dark transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {loading ? "Fazendo login..." : "Fazer Login"}
          </button>
        </div>
      </div>

      {/* Token Manual */}
      <div className="bg-psipro-surface-elevated border border-psipro-border rounded-lg p-6 mb-6">
        <h2 className="text-xl font-semibold text-psipro-text mb-4">
          Token Manual (Alternativo)
        </h2>
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-psipro-text mb-2">
              Cole seu token JWT aqui
            </label>
            <textarea
              value={token}
              onChange={(e) => setToken(e.target.value)}
              className="w-full px-4 py-2 bg-psipro-background border border-psipro-border rounded-lg text-psipro-text focus:outline-none focus:ring-2 focus:ring-psipro-primary font-mono text-sm"
              rows={3}
              placeholder="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            />
          </div>
          <button
            onClick={handleSetToken}
            disabled={!token}
            className="w-full px-4 py-2 bg-psipro-surface border border-psipro-border rounded-lg hover:bg-psipro-surface-elevated transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Salvar Token
          </button>
        </div>
      </div>

      {/* Teste POST /clinics (auditoria) */}
      <div className="bg-psipro-surface-elevated border border-psipro-border rounded-lg p-6 mb-6">
        <h2 className="text-xl font-semibold text-psipro-text mb-4">
          🧪 Teste POST /clinics (Auditoria)
        </h2>
        <button
          onClick={async () => {
            const apiUrl = getApiBaseUrl();
            const token = localStorage.getItem("psipro_token");
            console.log("[AUDIT] API_BASE_URL:", apiUrl);
            console.log("[AUDIT] Token:", token ? `${token.substring(0, 30)}...` : "AUSENTE");
            if (!token) {
              showError("Faça login primeiro");
              return;
            }
            try {
              const url = `${apiUrl}/clinics`;
              const res = await fetch(url, {
                method: "POST",
                headers: {
                  "Content-Type": "application/json",
                  "Authorization": "Bearer " + token,
                },
                body: JSON.stringify({ name: "TESTE CLINICA AUDIT" }),
              });
              const data = await res.json().catch(() => ({}));
              console.log("[AUDIT] Status:", res.status, "| Response:", data);
              if (res.ok) {
                showSuccess(`POST /clinics OK! Status ${res.status}`);
              } else {
                showError(`Erro ${res.status}: ${JSON.stringify(data)}`);
              }
            } catch (e) {
              console.error("[AUDIT] Erro completo:", e);
              showError("Erro de rede: " + (e instanceof Error ? e.message : String(e)));
            }
          }}
          disabled={!currentToken}
          className="w-full px-4 py-2 bg-amber-600 text-white rounded-lg hover:bg-amber-700 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          Testar POST /clinics
        </button>
      </div>

      {/* Teste de Conexão */}
      <div className="bg-psipro-surface-elevated border border-psipro-border rounded-lg p-6 mb-6">
        <h2 className="text-xl font-semibold text-psipro-text mb-4">
          Teste de Conexão
        </h2>
        <button
          onClick={handleTestConnection}
          disabled={loading || !currentToken}
          className="w-full px-4 py-2 bg-psipro-success text-white rounded-lg hover:bg-psipro-success/90 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {loading ? "Testando..." : "Testar Conexão com API"}
        </button>
        <p className="text-sm text-psipro-text-secondary mt-2">
          Este teste verifica se a conexão com a API está funcionando e se o token é válido.
        </p>
      </div>

      {/* Lista de Clínicas */}
      {clinics.length > 0 && (
        <div className="bg-psipro-surface-elevated border border-psipro-border rounded-lg p-6">
          <h2 className="text-xl font-semibold text-psipro-text mb-4">
            Clínicas Encontradas
          </h2>
          <div className="space-y-2">
            {clinics.map((clinic) => (
              <div
                key={clinic.id}
                className="p-3 bg-psipro-background rounded-lg"
              >
                <div className="font-medium text-psipro-text">{clinic.name}</div>
                <div className="text-sm text-psipro-text-secondary">
                  Role: {clinic.role} | ID: {clinic.id}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Instruções */}
      <div className="mt-8 bg-psipro-warning/10 border border-psipro-warning/30 rounded-lg p-6">
        <h3 className="text-lg font-semibold text-psipro-text mb-2">
          📝 Instruções
        </h3>
        <ol className="list-decimal list-inside space-y-2 text-psipro-text-secondary">
          <li>Certifique-se de que o backend está rodando na porta 3001 (com prefixo /api)</li>
          <li>Use as credenciais do seed: owner@psiclinic.com / senha123</li>
          <li>Ou cole um token JWT válido manualmente</li>
          <li>Clique em "Testar Conexão" para verificar se tudo está funcionando</li>
          <li>Acesse /clinica para ver a página completa de clínicas</li>
        </ol>
      </div>
    </div>
  );
}

