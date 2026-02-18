"use client";

import { useEffect, useRef } from "react";
import { useSearchParams } from "next/navigation";
import { getApiBaseUrl } from "../services/api";

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

function clearAuthStorage() {
  if (typeof window === "undefined") return;
  localStorage.removeItem("psipro_token");
  localStorage.removeItem("psipro_user");
  localStorage.removeItem("psipro_current_clinic_id");
}

/** Lê token da URL: searchParams (React) e fallback em window.location (evita atraso de hidratação). */
function getTokenFromUrl(searchParams: URLSearchParams): string | null {
  let token = searchParams.get("token");
  if (token !== null && typeof token === "string" && token.trim().length > 0) return token.trim();
  if (typeof window === "undefined") return null;
  const params = new URLSearchParams(window.location.search);
  token = params.get("token");
  if (token !== null && typeof token === "string" && token.trim().length > 0) return token.trim();
  return null;
}

/** Lê returnUrl da URL. */
function getReturnUrlFromUrl(searchParams: URLSearchParams): string | null {
  let url = searchParams.get("returnUrl");
  if (url !== null && typeof url === "string" && url.trim().length > 0) return url.trim();
  if (typeof window === "undefined") return null;
  const params = new URLSearchParams(window.location.search);
  url = params.get("returnUrl");
  if (url !== null && typeof url === "string" && url.trim().length > 0) return url.trim();
  return null;
}

export default function HandoffClient() {
  const searchParams = useSearchParams();
  const didRun = useRef(false);

  useEffect(() => {
    if (didRun.current) return;

    const token = getTokenFromUrl(searchParams);
    const returnUrl = getReturnUrlFromUrl(searchParams);

    // DEBUG: token recebido
    console.log("[handoff] token recebido:", token ? `${token.substring(0, 20)}...` : "null");
    console.log("[handoff] returnUrl:", returnUrl ?? "null");

    if (token === null || token.length === 0) {
      console.log("[handoff] token inválido ou vazio, redirecionando para /login");
      clearAuthStorage();
      window.location.replace("/login");
      return;
    }

    didRun.current = true;

    const run = async () => {
      try {
        const url = `${API_BASE_URL}/auth/handoff`;
        const response = await fetch(url, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ token }),
        });

        // DEBUG: status da resposta
        console.log("[handoff] POST /auth/handoff status:", response.status);

        if (!response.ok) {
          const errData = await response.json().catch(() => ({}));
          console.log("[handoff] erro da API:", response.status, errData);
          clearAuthStorage();
          window.location.replace("/login");
          return;
        }

        const result = (await response.json()) as HandoffResponse;

        // Garantir 200 antes de qualquer redirect; só então persistir e redirecionar
        const finalToken = result?.token ?? token;
        localStorage.setItem("psipro_token", finalToken);

        // DEBUG: confirmação de token salvo
        console.log("[handoff] resposta 200 recebida; token salvo no localStorage");
        console.log("[handoff] clinicId:", result?.user?.clinicId ?? "não definido");

        const clinicId = result?.user?.clinicId;
        if (clinicId != null && String(clinicId).length > 0) {
          localStorage.setItem("psipro_current_clinic_id", String(clinicId));
        }

        if (result?.user?.id && result?.user?.email) {
          const fullName = result.user.fullName || result.user.name || result.user.email;
          localStorage.setItem(
            "psipro_user",
            JSON.stringify({
              id: result.user.id,
              email: result.user.email,
              fullName,
            })
          );
        }

        const safePath =
          returnUrl &&
          typeof returnUrl === "string" &&
          returnUrl.startsWith("/") &&
          !returnUrl.startsWith("//")
            ? returnUrl
            : "/dashboard";

        console.log("[handoff] sucesso, redirecionando para:", safePath);
        window.location.replace(safePath);
      } catch (err) {
        console.log("[handoff] exceção:", err);
        clearAuthStorage();
        window.location.replace("/login");
      }
    };

    void run();
  }, [searchParams]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-psipro-background">
      <div className="text-psipro-text-secondary">Conectando sua sessão...</div>
    </div>
  );
}

