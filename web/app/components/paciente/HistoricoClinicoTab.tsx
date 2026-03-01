"use client";

import { useState, useEffect } from "react";
import { sessionService, type Session } from "@/app/services/sessionService";
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

export default function HistoricoClinicoTab({
  patientId,
}: {
  patientId: string;
}) {
  const [selectedSession, setSelectedSession] = useState<string | null>(null);
  const [sessions, setSessions] = useState<Session[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    sessionService
      .getByPatient(patientId)
      .then((s) => {
        if (!cancelled) setSessions(s);
      })
      .catch((e) => {
        if (!cancelled) {
          setError(
            e && typeof e === "object" && "message" in e
              ? String((e as { message: string }).message)
              : "Erro ao carregar histórico"
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

  const selectedSessionData = sessions.find((s) => s.id === selectedSession);

  if (loading) {
    return (
      <div className="space-y-4">
        <Skeleton className="h-48" />
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
    <div>
      <div className="mb-4">
        <p className="text-sm text-psipro-text-secondary">
          Histórico cronológico de sessões. Clique em uma sessão para visualizar
          detalhes.
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2">
          <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm">
            <div className="p-6 border-b border-psipro-divider">
              <h2 className="text-lg font-semibold text-psipro-text">Sessões</h2>
            </div>
            <div className="divide-y divide-psipro-divider">
              {sessions.length === 0 ? (
                <div className="p-6 text-center text-psipro-text-secondary text-sm">
                  Nenhuma sessão registrada ainda.
                </div>
              ) : (
                sessions.map((session) => (
                  <button
                    key={session.id}
                    onClick={() =>
                      setSelectedSession(
                        selectedSession === session.id ? null : session.id
                      )
                    }
                    className={`w-full p-4 text-left hover:bg-psipro-surface transition-colors ${
                      selectedSession === session.id ? "bg-psipro-primary/5" : ""
                    }`}
                  >
                    <div className="flex items-center justify-between">
                      <div className="flex-1">
                        <div className="flex items-center gap-3 mb-1">
                          <span className="text-sm font-medium text-psipro-text">
                            {formatDate(session.date)}
                          </span>
                          <span
                            className={`inline-flex px-2 py-0.5 text-xs font-medium rounded-full ${
                              session.status === "realizada"
                                ? "bg-psipro-success/20 text-psipro-success"
                                : session.status === "falta"
                                  ? "bg-psipro-warning/20 text-psipro-warning"
                                  : "bg-psipro-text-muted/20 text-psipro-text-muted"
                            }`}
                          >
                            {session.status === "realizada"
                              ? "Realizada"
                              : session.status === "falta"
                                ? "Falta"
                                : "Cancelada"}
                          </span>
                        </div>
                        {session.notes && (
                          <p className="text-xs text-psipro-text-muted flex items-center gap-1">
                            <span>📝</span>
                            <span>Possui anotação</span>
                          </p>
                        )}
                      </div>
                      <span className="text-psipro-text-muted">→</span>
                    </div>
                  </button>
                ))
              )}
            </div>
          </div>
        </div>

        <div>
          {selectedSessionData ? (
            <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm sticky top-6">
              <div className="p-6 border-b border-psipro-divider flex items-center justify-between">
                <h3 className="text-lg font-semibold text-psipro-text">
                  Detalhes da Sessão
                </h3>
                <button
                  onClick={() => setSelectedSession(null)}
                  className="text-psipro-text-muted hover:text-psipro-text transition-colors"
                >
                  ✕
                </button>
              </div>
              <div className="p-6">
                <div className="mb-4">
                  <p className="text-sm text-psipro-text-secondary mb-1">
                    Data
                  </p>
                  <p className="text-base font-semibold text-psipro-text">
                    {formatDate(selectedSessionData.date)}
                  </p>
                </div>
                <div className="mb-4">
                  <p className="text-sm text-psipro-text-secondary mb-1">
                    Status
                  </p>
                  <span
                    className={`inline-flex px-2.5 py-1 text-xs font-medium rounded-full ${
                      selectedSessionData.status === "realizada"
                        ? "bg-psipro-success/20 text-psipro-success"
                        : selectedSessionData.status === "falta"
                          ? "bg-psipro-warning/20 text-psipro-warning"
                          : "bg-psipro-text-muted/20 text-psipro-text-muted"
                    }`}
                  >
                    {selectedSessionData.status === "realizada"
                      ? "Realizada"
                      : selectedSessionData.status === "falta"
                        ? "Falta"
                        : "Cancelada"}
                  </span>
                </div>
                {selectedSessionData.notes ? (
                  <div>
                    <p className="text-sm text-psipro-text-secondary mb-2">
                      Anotação
                    </p>
                    <div className="bg-psipro-surface rounded-lg p-4 border border-psipro-border">
                      <p className="text-sm text-psipro-text-secondary leading-relaxed">
                        {selectedSessionData.notes}
                      </p>
                    </div>
                  </div>
                ) : (
                  <div className="bg-psipro-surface rounded-lg p-4 border border-psipro-border text-center">
                    <p className="text-sm text-psipro-text-muted">
                      Nenhuma anotação registrada
                    </p>
                  </div>
                )}
              </div>
            </div>
          ) : (
            <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm p-6 text-center">
              <div className="text-4xl mb-3 opacity-40">📝</div>
              <p className="text-sm text-psipro-text-secondary">
                Selecione uma sessão para visualizar os detalhes
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
