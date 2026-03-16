"use client";

import { useState, useEffect } from "react";
import InsightSection from "@/app/components/InsightSection";
import { Insight } from "@/app/components/InsightCard";
import { sessionService, type Session } from "@/app/services/sessionService";
import { financialService } from "@/app/services/financialService";
import { appointmentService } from "@/app/services/appointmentService";
import Skeleton from "@/app/components/Skeleton";

function formatDate(iso: string) {
  try {
    const d = new Date(iso);
    return d.toLocaleDateString("pt-BR", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
    });
  } catch {
    return iso;
  }
}

function formatCurrency(value: number) {
  return new Intl.NumberFormat("pt-BR", {
    style: "currency",
    currency: "BRL",
  }).format(value);
}

export default function VisaoGeralTab({ patientId }: { patientId: string }) {
  const [sessions, setSessions] = useState<Session[]>([]);
  const [financial, setFinancial] = useState<{
    totalFaturado: number;
    totalRecebido: number;
    totalAberto: number;
  } | null>(null);
  const [nextAppointment, setNextAppointment] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;

    Promise.all([
      sessionService.getByPatient(patientId),
      financialService.getPatientFinancial(patientId).catch(() => null),
      appointmentService
        .getAppointments()
        .then((list) => {
          const now = new Date();
          const future = list
            .filter(
              (a) =>
                a.patientId === patientId &&
                new Date(a.scheduledAt) >= now &&
                (a.status !== "cancelado" && a.status !== "cancelada")
            )
            .sort(
              (a, b) =>
                new Date(a.scheduledAt).getTime() -
                new Date(b.scheduledAt).getTime()
            );
          return future[0]?.scheduledAt ?? null;
        })
        .catch(() => null),
    ])
      .then(([s, f, next]) => {
        if (!cancelled) {
          setSessions(s);
          setFinancial(f);
          setNextAppointment(next);
        }
      })
      .catch((e) => {
        if (!cancelled) {
          setError(
            e && typeof e === "object" && "message" in e
              ? String((e as { message: string }).message)
              : "Erro ao carregar dados"
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

  const lastSession = sessions[0] ? sessions[0] : null;
  const realizadas = sessions.filter((s) => s.status === "realizada");
  const faltas = sessions.filter((s) => s.status === "falta");

  const insights: Insight[] = [];
  if (faltas.length > 0 && realizadas.length > 0) {
    const taxaFalta = (faltas.length / (realizadas.length + faltas.length)) * 100;
    if (taxaFalta > 20) {
      insights.push({
        id: `patient-${patientId}-faltas`,
        type: "clinico",
        message: `Este paciente apresenta ${faltas.length} falta(s) nas sessões registradas (${Math.round(taxaFalta)}%). Pode ser útil observar padrões.`,
      });
    }
  }
  if (financial && financial.totalAberto > 0) {
    insights.push({
      id: `patient-${patientId}-financeiro`,
      type: "financeiro",
      message: `Pendência financeira: ${formatCurrency(financial.totalAberto)} em aberto.`,
    });
  }
  if (financial && financial.totalAberto === 0 && financial.totalRecebido > 0) {
    insights.push({
      id: `patient-${patientId}-ok`,
      type: "financeiro",
      message: "Situação financeira em dia. Sem pendências.",
    });
  }

  if (loading) {
    return (
      <div className="space-y-6">
        <Skeleton className="h-48" />
        <Skeleton className="h-32" />
        <Skeleton className="h-64" />
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

  return (
    <div className="space-y-6">
      {/* Card Resumo Clínico */}
      <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm">
        <div className="p-6 border-b border-psipro-divider">
          <h2 className="text-lg font-semibold text-psipro-text">Resumo Clínico</h2>
        </div>
        <div className="p-6">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-4">
            <div>
              <p className="text-sm text-psipro-text-secondary mb-1">Última sessão</p>
              <p className="text-base font-semibold text-psipro-text">
                {lastSession ? formatDate(lastSession.date) : "—"}
              </p>
            </div>
            <div>
              <p className="text-sm text-psipro-text-secondary mb-1">Próxima consulta</p>
              <p className="text-base font-semibold text-psipro-text">
                {nextAppointment ? formatDate(nextAppointment) : "—"}
              </p>
            </div>
            <div>
              <p className="text-sm text-psipro-text-secondary mb-1">Sessões registradas</p>
              <p className="text-base font-semibold text-psipro-text">
                {sessions.length}
              </p>
            </div>
            <div>
              <p className="text-sm text-psipro-text-secondary mb-1">Status</p>
              <span className="inline-flex px-2.5 py-1 text-xs font-medium rounded-full bg-psipro-success/20 text-psipro-success">
                {sessions.length > 0 ? "Ativo" : "Sem sessões"}
              </span>
            </div>
          </div>
          <div>
            <p className="text-sm text-psipro-text-secondary mb-2">Observações gerais</p>
            <p className="text-sm text-psipro-text leading-relaxed">
              {lastSession?.notes || "Nenhuma observação registrada nas sessões."}
            </p>
          </div>
        </div>
      </div>

      {/* Card Resumo Financeiro */}
      <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm">
        <div className="p-6 border-b border-psipro-divider">
          <h2 className="text-lg font-semibold text-psipro-text">Resumo Financeiro</h2>
        </div>
        <div className="p-6">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-4">
            <div>
              <p className="text-sm text-psipro-text-secondary mb-1">Total faturado</p>
              <p className="text-lg font-semibold text-psipro-text">
                {financial ? formatCurrency(financial.totalFaturado) : "—"}
              </p>
            </div>
            <div>
              <p className="text-sm text-psipro-text-secondary mb-1">Total recebido</p>
              <p className="text-lg font-semibold text-psipro-success">
                {financial ? formatCurrency(financial.totalRecebido) : "—"}
              </p>
            </div>
            <div>
              <p className="text-sm text-psipro-text-secondary mb-1">Total em aberto</p>
              <p className="text-lg font-semibold text-psipro-warning">
                {financial ? formatCurrency(financial.totalAberto) : "—"}
              </p>
            </div>
            <div>
              <p className="text-sm text-psipro-text-secondary mb-1">Status financeiro</p>
              <span
                className={`inline-flex px-2.5 py-1 text-xs font-medium rounded-full ${
                  financial && financial.totalAberto === 0
                    ? "bg-psipro-success/20 text-psipro-success"
                    : "bg-psipro-warning/20 text-psipro-warning"
                }`}
              >
                {financial && financial.totalAberto === 0 ? "Em dia" : "Pendência"}
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* Card Linha do Tempo */}
      <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm">
        <div className="p-6 border-b border-psipro-divider">
          <h2 className="text-lg font-semibold text-psipro-text">Linha do Tempo</h2>
          <p className="text-xs text-psipro-text-muted mt-1">Últimas sessões e anotações</p>
        </div>
        <div className="p-6">
          <div className="space-y-4">
            {sessions.length === 0 ? (
              <p className="text-sm text-psipro-text-muted">Nenhuma sessão registrada.</p>
            ) : (
              sessions.slice(0, 5).map((item, index) => (
                <div key={item.id} className="flex gap-4">
                  <div className="shrink-0">
                    <div className="w-2 h-2 rounded-full bg-psipro-primary mt-2" />
                    {index < Math.min(5, sessions.length) - 1 && (
                      <div className="w-0.5 h-full bg-psipro-border ml-0.5 mt-1" />
                    )}
                  </div>
                  <div className="flex-1 pb-4">
                    <div className="flex items-center gap-2 mb-1">
                      <span className="text-sm font-medium text-psipro-text">
                        {formatDate(item.date)}
                      </span>
                      <span
                        className={`inline-flex px-2 py-0.5 text-xs font-medium rounded-full ${
                          item.status === "realizada"
                            ? "bg-psipro-success/20 text-psipro-success"
                            : item.status === "falta"
                              ? "bg-psipro-warning/20 text-psipro-warning"
                              : "bg-psipro-text-muted/20 text-psipro-text-muted"
                        }`}
                      >
                        {item.status === "realizada" ? "Realizada" : item.status === "falta" ? "Falta" : "Cancelada"}
                      </span>
                    </div>
                    {item.notes && (
                      <p className="text-sm text-psipro-text-secondary leading-relaxed">
                        {item.notes}
                      </p>
                    )}
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </div>

      {insights.length > 0 && (
        <div className="mt-6">
          <InsightSection insights={insights} maxItems={2} title="Insights do Paciente" />
        </div>
      )}
    </div>
  );
}
