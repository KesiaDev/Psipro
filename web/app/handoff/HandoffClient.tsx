"use client";

import { useEffect, useRef } from "react";
import { useSearchParams } from "next/navigation";

const API_BASE_URL = (process.env.NEXT_PUBLIC_API_URL || "http://localhost:3001/api").replace(/\/+$/, "");

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

export default function HandoffClient() {
  const searchParams = useSearchParams();
  const didRun = useRef(false);

  useEffect(() => {
    if (didRun.current) return;
    didRun.current = true;

    const run = async () => {
      const token = searchParams.get("token");
      const returnUrl = searchParams.get("returnUrl");

      // DEBUG: token recebido
      console.log("[handoff] token recebido:", token ? `${token.substring(0, 20)}...` : "null");
      console.log("[handoff] returnUrl:", returnUrl ?? "null");

      if (!token || typeof token !== "string" || token.trim().length === 0) {
        console.log("[handoff] token inválido ou vazio, redirecionando para /login");
        clearAuthStorage();
        window.location.replace("/login");
        return;
      }

      try {
        const url = `${API_BASE_URL}/auth/handoff`;
        const response = await fetch(url, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ token: token.trim() }),
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

        const finalToken = result?.token || token.trim();
        localStorage.setItem("psipro_token", finalToken);

        // DEBUG: confirmação de token salvo
        console.log("[handoff] token salvo no localStorage, clinicId:", result?.user?.clinicId ?? "não definido");

        const clinicId = result?.user?.clinicId;
        if (clinicId) {
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

