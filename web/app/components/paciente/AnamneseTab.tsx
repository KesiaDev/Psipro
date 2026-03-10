"use client";

import { useState, useEffect } from "react";
import { anamneseService, type AnamnesePreenchida } from "@/app/services/anamneseService";
import Skeleton from "@/app/components/Skeleton";

interface SectionProps {
  title: string;
  children: React.ReactNode;
  defaultOpen?: boolean;
}

function CollapsibleSection({ title, children, defaultOpen = false }: SectionProps) {
  const [isOpen, setIsOpen] = useState(defaultOpen);

  return (
    <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border mb-4">
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="w-full flex items-center justify-between p-4 text-left hover:bg-psipro-surface transition-colors"
      >
        <h3 className="text-base font-semibold text-psipro-text">{title}</h3>
        <span className="text-psipro-text-muted">{isOpen ? "▼" : "▶"}</span>
      </button>
      {isOpen && <div className="p-4 pt-0 border-t border-psipro-divider">{children}</div>}
    </div>
  );
}

function formatDate(dateStr: string): string {
  try {
    const d = new Date(dateStr);
    return d.toLocaleDateString("pt-BR", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
    });
  } catch {
    return dateStr;
  }
}

interface AnamneseTabProps {
  patientId: string;
}

export default function AnamneseTab({ patientId }: AnamneseTabProps) {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [preenchidas, setPreenchidas] = useState<AnamnesePreenchida[]>([]);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);
    anamneseService
      .getPreenchidas(patientId)
      .then((data) => {
        if (!cancelled) setPreenchidas(data);
      })
      .catch((e) => {
        if (!cancelled) {
          setError(
            e && typeof e === "object" && "message" in e
              ? String((e as { message: string }).message)
              : "Erro ao carregar anamnese"
          );
        }
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [patientId]);

  if (loading) {
    return (
      <div className="space-y-4">
        <Skeleton className="h-10 w-48" />
        <Skeleton className="h-32" />
        <Skeleton className="h-32" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-psipro-error/10 border border-psipro-error/20 text-psipro-error rounded-lg p-6">
        <p className="font-medium">{error}</p>
      </div>
    );
  }

  if (preenchidas.length === 0) {
    return (
      <div>
        <p className="text-psipro-text-secondary text-sm leading-relaxed mb-6">
          Nenhuma anamnese preenchida para este paciente. As anamneses preenchidas
          no app ou na plataforma web aparecerão aqui.
        </p>
        <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border p-8 text-center">
          <p className="text-psipro-text-muted">Nenhum registro encontrado</p>
        </div>
      </div>
    );
  }

  return (
    <div>
      <div className="mb-6">
        <p className="text-psipro-text-secondary text-sm leading-relaxed">
          Informações completas da anamnese do paciente. Você pode expandir e
          colapsar cada seção conforme necessário.
        </p>
      </div>

      <div className="space-y-4">
        {preenchidas.map((p) => {
          const modelo = p.modelo;
          const campos = modelo?.campos || [];
          const respostas = (p.respostas as Record<string, unknown>) || {};
          const labelMap = new Map(campos.map((c) => [c.id, c.label]));

          return (
            <CollapsibleSection
              key={p.id}
              title={`${modelo?.nome ?? "Anamnese"} — ${formatDate(p.data)}`}
              defaultOpen={true}
            >
              <div className="space-y-4">
                {Object.entries(respostas).map(([campoId, valor]) => {
                  const label = labelMap.get(campoId) ?? campoId;
                  const valorStr =
                    valor == null
                      ? ""
                      : typeof valor === "string"
                        ? valor
                        : Array.isArray(valor)
                          ? valor.join(", ")
                          : String(valor);
                  if (!valorStr.trim()) return null;
                  return (
                    <div key={campoId}>
                      <label className="block text-sm font-medium text-psipro-text-secondary mb-1">
                        {label}
                      </label>
                      <p className="text-psipro-text whitespace-pre-wrap">{valorStr}</p>
                    </div>
                  );
                })}
                {Object.keys(respostas).length === 0 && (
                  <p className="text-psipro-text-muted text-sm">
                    Nenhuma resposta registrada
                  </p>
                )}
              </div>
            </CollapsibleSection>
          );
        })}
      </div>
    </div>
  );
}
