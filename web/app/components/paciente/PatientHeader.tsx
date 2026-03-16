"use client";

import { useState, useEffect } from "react";
import { patientService, type Patient } from "@/app/services/patientService";

export default function PatientHeader({ patientId }: { patientId: string }) {
  const [patient, setPatient] = useState<Patient | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    patientService
      .getPatientById(patientId)
      .then((p) => {
        if (!cancelled) setPatient(p);
      })
      .catch((e) => {
        if (!cancelled)
          setError(
            e && typeof e === "object" && "message" in e
              ? String((e as { message: string }).message)
              : "Erro ao carregar paciente"
          );
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
      <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm p-6">
        <div className="animate-pulse flex items-center gap-6">
          <div className="h-20 w-20 rounded-full bg-psipro-surface" />
          <div className="flex-1">
            <div className="h-6 bg-psipro-surface rounded w-48 mb-3" />
            <div className="h-4 bg-psipro-surface rounded w-32" />
          </div>
        </div>
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

  if (!patient) return null;

  const displayName = patient.name || "Paciente";

  return (
    <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm">
      <div className="p-6">
        <div className="flex items-start gap-6">
          <div className="flex-shrink-0">
            <div className="h-20 w-20 rounded-full bg-psipro-primary border-2 border-psipro-primary-dark flex items-center justify-center shadow-sm">
              <span className="text-2xl font-semibold text-psipro-text">
                {displayName.charAt(0)}
              </span>
            </div>
          </div>

          <div className="flex-1 min-w-0">
            <h1 className="text-2xl font-bold text-psipro-text mb-3 tracking-tight">
              {displayName}
            </h1>

            <div className="flex items-center gap-4 text-sm mb-3">
              <span
                className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium ${
                  (patient.status || "").toLowerCase() === "ativo"
                    ? "bg-psipro-success/20 text-psipro-success"
                    : "bg-psipro-text-muted/20 text-psipro-text-muted"
                }`}
              >
                <span
                  className={`h-2 w-2 rounded-full ${
                    (patient.status || "").toLowerCase() === "ativo"
                      ? "bg-psipro-success"
                      : "bg-psipro-text-muted"
                  }`}
                />
                {patient.status || "—"}
              </span>
              {patient.age != null && (
                <span className="text-psipro-text-secondary">
                  {patient.age} anos
                </span>
              )}
              {patient.nextSession && (
                <span className="text-psipro-text-secondary">
                  Próxima: {patient.nextSession}
                </span>
              )}
            </div>

            <div className="flex items-center gap-6 text-sm">
              {patient.sessionsCount != null && (
                <div className="flex items-center gap-2">
                  <span className="text-psipro-text-muted">
                    Total de sessões:
                  </span>
                  <span className="font-semibold text-psipro-text">
                    {patient.sessionsCount}
                  </span>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
