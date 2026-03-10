"use client";

import { useState } from "react";
import Link from "next/link";
import { getApiBaseUrl } from "../../services/api";
import { useToast } from "../../contexts/ToastContext";

const API_BASE_URL = getApiBaseUrl();

export default function ForgotPasswordPage() {
  const { showSuccess, showError } = useToast();
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [sent, setSent] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email.trim()) {
      setError("Informe seu e-mail");
      return;
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      setError("E-mail inválido");
      return;
    }
    setError(null);
    setLoading(true);
    try {
      const res = await fetch(`${API_BASE_URL}/auth/forgot-password`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email: email.trim().toLowerCase() }),
      });
      if (res.ok) {
        setSent(true);
        showSuccess("Se o e-mail existir, você receberá instruções para redefinir a senha.");
      } else {
        const data = await res.json().catch(() => ({}));
        const msg = data?.message || "Erro ao enviar. Tente novamente.";
        setError(msg);
        showError(msg);
      }
    } catch {
      setError("Erro de conexão. Tente novamente.");
      showError("Erro de conexão. Tente novamente.");
    } finally {
      setLoading(false);
    }
  };

  if (sent) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-psipro-background px-4 py-6">
        <div className="w-full max-w-md bg-psipro-surface-elevated border border-psipro-border rounded-lg p-6 shadow-lg text-center">
          <h1 className="text-xl font-bold text-psipro-text mb-2">E-mail enviado</h1>
          <p className="text-psipro-text-secondary text-sm mb-6">
            Se o endereço {email} estiver cadastrado, você receberá um link para redefinir sua senha.
            Verifique também a pasta de spam.
          </p>
          <Link
            href="/login"
            className="text-psipro-primary hover:text-psipro-primary-dark font-medium"
          >
            Voltar ao login
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-psipro-background px-4 py-6">
      <div className="w-full max-w-md">
        <div className="text-center mb-6">
          <h1 className="text-2xl font-bold text-psipro-text mb-2">Recuperar senha</h1>
          <p className="text-psipro-text-secondary text-sm">
            Informe seu e-mail e enviaremos um link para redefinir sua senha.
          </p>
        </div>
        <div className="bg-psipro-surface-elevated border border-psipro-border rounded-lg p-6 shadow-lg">
          <form onSubmit={handleSubmit} className="space-y-4">
            {error && (
              <div className="bg-psipro-error/10 border border-psipro-error/20 text-psipro-error rounded-lg p-3 text-sm">
                {error}
              </div>
            )}
            <div>
              <label htmlFor="email" className="block text-sm font-medium text-psipro-text mb-2">
                E-mail <span className="text-psipro-error">*</span>
              </label>
              <input
                type="email"
                id="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full px-4 py-2 bg-psipro-background border border-psipro-border rounded-lg text-psipro-text focus:outline-none focus:ring-2 focus:ring-psipro-primary"
                placeholder="seu@email.com"
                disabled={loading}
              />
            </div>
            <button
              type="submit"
              disabled={loading}
              className="w-full px-4 py-2 bg-psipro-primary text-white rounded-lg hover:bg-psipro-primary-dark transition-colors font-medium disabled:opacity-50"
            >
              {loading ? "Enviando..." : "Enviar"}
            </button>
          </form>
        </div>
        <div className="mt-6 text-center">
          <Link href="/login" className="text-sm text-psipro-text-secondary hover:text-psipro-text">
            ← Voltar ao login
          </Link>
        </div>
      </div>
    </div>
  );
}
