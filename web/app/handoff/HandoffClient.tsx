"use client";

import { useEffect, useRef } from "react";
import { useSearchParams } from "next/navigation";
import { api } from "../services/api";

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

export default function HandoffClient() {
  const searchParams = useSearchParams();
  const didRun = useRef(false);

  useEffect(() => {
    if (didRun.current) return;
    didRun.current = true;

    const run = async () => {
      const token = searchParams.get("token");

      if (!token) {
        window.location.replace("/login");
        return;
      }

      try {
        const result = await api.post<HandoffResponse>("/auth/handoff", { token });

        const finalToken = result?.token || token;
        localStorage.setItem("psipro_token", finalToken);

        const clinicId = result?.user?.clinicId;
        if (clinicId) {
          localStorage.setItem("psipro_current_clinic_id", clinicId);
        }

        // Cache do usuário (opcional, mas ajuda a UI a iniciar mais rápido)
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

        // Recarrega o app para o AuthProvider inicializar a sessão corretamente
        window.location.replace("/dashboard");
      } catch {
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

