"use client";

import { useState, useEffect, useRef } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { useAuth } from "../../contexts/AuthContext";
import { useToast } from "../../contexts/ToastContext";
import { getApiBaseUrl } from "../../services/api";

const API_BASE_URL = getApiBaseUrl();

type HandoffResponse = {
  token?: string;
  user?: {
    id?: string;
    email?: string;
    fullName?: string;
    name?: string;
    clinicId?: string | null;
  };
};

export default function LoginPage() {
  const router = useRouter();
  const { login, isAuthenticated, loading: authLoading } = useAuth();
  const { showError } = useToast();
  const handoffRan = useRef(false);
  const [handoffLoading, setHandoffLoading] = useState(true);
  const [showLoginForm, setShowLoginForm] = useState(false);

  const [formData, setFormData] = useState({
    email: "",
    password: "",
  });
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<{ email?: string; password?: string; general?: string }>({});

  // Handoff: se houver token na URL (vindo do app), trocar por sessão e redirecionar
  useEffect(() => {
    if (typeof window === "undefined" || handoffRan.current) return;
    const params = new URLSearchParams(window.location.search);
    const token = params.get("token")?.trim();
    const returnUrl = params.get("returnUrl")?.trim();
    if (!token) {
      setHandoffLoading(false);
      setShowLoginForm(true);
      return;
    }
    handoffRan.current = true;
    const run = async () => {
      try {
        const response = await fetch(`${API_BASE_URL}/auth/handoff`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ token }),
        });
        if (!response.ok) {
          window.history.replaceState({}, "", "/login");
          setHandoffLoading(false);
          setShowLoginForm(true);
          return;
        }
        const result = (await response.json()) as HandoffResponse;
        const finalToken = result?.token ?? token;
        localStorage.setItem("psipro_token", finalToken);
        const clinicId = result?.user?.clinicId;
        if (clinicId != null && String(clinicId).length > 0) {
          localStorage.setItem("psipro_current_clinic_id", String(clinicId));
        }
        if (result?.user?.id && result?.user?.email) {
          const fullName = result.user.fullName || result.user.name || result.user.email;
          localStorage.setItem("psipro_user", JSON.stringify({
            id: result.user.id,
            email: result.user.email,
            fullName,
          }));
        }
        const safePath = returnUrl && returnUrl.startsWith("/") && !returnUrl.startsWith("//")
          ? returnUrl
          : "/dashboard";
        window.location.replace(safePath);
      } catch {
        window.history.replaceState({}, "", "/login");
        setHandoffLoading(false);
        setShowLoginForm(true);
      }
    };
    void run();
  }, []);

  // Redirecionar se já estiver autenticado (e não estiver em handoff)
  useEffect(() => {
    if (!authLoading && isAuthenticated && !handoffRan.current) {
      const params = new URLSearchParams(window.location.search);
      const returnUrl = params.get("returnUrl") || "/dashboard";
      router.push(returnUrl);
    }
  }, [isAuthenticated, authLoading, router]);

  const validateForm = (): boolean => {
    const newErrors: typeof errors = {};

    if (!formData.email) {
      newErrors.email = "Email é obrigatório";
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = "Email inválido";
    }

    if (!formData.password) {
      newErrors.password = "Senha é obrigatória";
    } else if (formData.password.length < 6) {
      newErrors.password = "Senha deve ter pelo menos 6 caracteres";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    setLoading(true);
    setErrors({});

    try {
      await login(formData);
      // Verificar se há returnUrl nos query params
      const params = new URLSearchParams(window.location.search);
      const returnUrl = params.get("returnUrl") || "/dashboard";
      router.push(returnUrl);
    } catch (error) {
      const message = error instanceof Error ? error.message : "Erro ao fazer login";
      setErrors({ general: message });
      showError(message);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
    // Limpar erro do campo ao digitar
    if (errors[e.target.name as keyof typeof errors]) {
      setErrors({
        ...errors,
        [e.target.name]: undefined,
      });
    }
  };

  if (authLoading || (handoffLoading && !showLoginForm)) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-psipro-background">
        <div className="text-psipro-text-secondary">
          {handoffLoading && !showLoginForm ? "Conectando sua sessão..." : "Carregando..."}
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-psipro-background px-4 py-6 sm:py-12">
      <div className="w-full max-w-md">
        {/* Logo/Header */}
        <div className="text-center mb-6 sm:mb-8">
          <h1 className="text-2xl sm:text-3xl font-bold text-psipro-text mb-2">PsiPro</h1>
          <p className="text-psipro-text-secondary text-sm sm:text-base">Entre na sua conta</p>
        </div>

        {/* Form Card */}
        <div className="bg-psipro-surface-elevated border border-psipro-border rounded-lg p-6 sm:p-8 shadow-lg">
          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Erro geral */}
            {errors.general && (
              <div className="bg-psipro-error/10 border border-psipro-error/20 text-psipro-error rounded-lg p-3 text-sm">
                {errors.general}
              </div>
            )}

            {/* Email */}
            <div>
              <label htmlFor="email" className="block text-sm font-medium text-psipro-text mb-2">
                Email <span className="text-psipro-error">*</span>
              </label>
              <input
                type="email"
                id="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                className={`w-full px-4 py-2 bg-psipro-background border rounded-lg text-psipro-text focus:outline-none focus:ring-2 transition-colors ${
                  errors.email
                    ? "border-psipro-error focus:ring-psipro-error"
                    : "border-psipro-border focus:ring-psipro-primary"
                }`}
                placeholder="seu@email.com"
                disabled={loading}
              />
              {errors.email && (
                <p className="mt-1 text-sm text-psipro-error">{errors.email}</p>
              )}
            </div>

            {/* Senha */}
            <div>
              <label htmlFor="password" className="block text-sm font-medium text-psipro-text mb-2">
                Senha <span className="text-psipro-error">*</span>
              </label>
              <input
                type="password"
                id="password"
                name="password"
                value={formData.password}
                onChange={handleChange}
                className={`w-full px-4 py-2 bg-psipro-background border rounded-lg text-psipro-text focus:outline-none focus:ring-2 transition-colors ${
                  errors.password
                    ? "border-psipro-error focus:ring-psipro-error"
                    : "border-psipro-border focus:ring-psipro-primary"
                }`}
                placeholder="••••••••"
                disabled={loading}
              />
              {errors.password && (
                <p className="mt-1 text-sm text-psipro-error">{errors.password}</p>
              )}
            </div>

            {/* Submit Button */}
            <button
              type="submit"
              disabled={loading}
              className="w-full px-4 py-2 bg-psipro-primary text-white rounded-lg hover:bg-psipro-primary-dark transition-colors font-medium disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? "Entrando..." : "Entrar"}
            </button>
          </form>

          {/* Footer */}
          <div className="mt-6 text-center text-sm">
            <p className="text-psipro-text-secondary">
              Não tem uma conta?{" "}
              <Link href="/register" className="text-psipro-primary hover:text-psipro-primary-dark font-medium">
                Cadastre-se
              </Link>
            </p>
          </div>
        </div>

        {/* Link para Landing */}
        <div className="mt-6 text-center">
          <Link href="/" className="text-sm text-psipro-text-secondary hover:text-psipro-text">
            ← Voltar para a página inicial
          </Link>
        </div>
      </div>
    </div>
  );
}
