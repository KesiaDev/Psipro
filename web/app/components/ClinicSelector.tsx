"use client";

import { useClinic } from "../contexts/ClinicContext";
import { useState } from "react";

const getRoleLabel = (role: string) => {
  const labels: Record<string, string> = {
    owner: "Proprietário",
    admin: "Administrador",
    psychologist: "Psicólogo",
    assistant: "Assistente",
  };
  return labels[role] || role;
};

const getRoleBadgeColor = (role: string) => {
  const colors: Record<string, string> = {
    owner: "bg-psipro-primary/20 text-psipro-primary",
    admin: "bg-psipro-warning/20 text-psipro-warning",
    psychologist: "bg-psipro-success/20 text-psipro-success",
    assistant: "bg-psipro-text-secondary/20 text-psipro-text-secondary",
  };
  return colors[role] || "bg-psipro-surface text-psipro-text-secondary";
};

export default function ClinicSelector() {
  const { currentClinic, clinics, isIndependent, setCurrentClinic, loading } = useClinic();
  const [isOpen, setIsOpen] = useState(false);

  if (loading) {
    return (
      <div className="h-10 w-32 bg-psipro-surface-elevated border border-psipro-border rounded-lg animate-pulse" />
    );
  }

  if (clinics.length === 0 && isIndependent) {
    return null; // Não mostrar se não tem clínicas
  }

  return (
    <div className="relative">
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center gap-2 px-3 sm:px-4 py-2 bg-psipro-surface-elevated border border-psipro-border rounded-lg hover:bg-psipro-surface transition-colors min-w-0 max-w-[180px] sm:max-w-none"
      >
        <span className="text-sm text-psipro-text-secondary truncate">
          {currentClinic ? currentClinic.name : "Modo Independente"}
        </span>
        <svg
          className={`w-4 h-4 text-psipro-text-secondary transition-transform ${
            isOpen ? "rotate-180" : ""
          }`}
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M19 9l-7 7-7-7"
          />
        </svg>
      </button>

      {isOpen && (
        <>
          <div
            className="fixed inset-0 z-10"
            onClick={() => setIsOpen(false)}
          />
          <div className="absolute top-full mt-2 right-0 z-20 bg-psipro-surface-elevated border border-psipro-border rounded-lg shadow-lg min-w-[200px]">
            {!isIndependent && (
              <button
                onClick={() => {
                  setCurrentClinic(null);
                  setIsOpen(false);
                }}
                className="w-full text-left px-4 py-2 text-sm text-psipro-text hover:bg-psipro-surface transition-colors"
              >
                Modo Independente
              </button>
            )}
            {clinics.map((clinic) => (
              <button
                key={clinic.id}
                onClick={() => {
                  setCurrentClinic(clinic);
                  setIsOpen(false);
                }}
                className={`w-full text-left px-4 py-2 text-sm transition-colors ${
                  currentClinic?.id === clinic.id
                    ? "bg-psipro-primary/10 text-psipro-primary"
                    : "text-psipro-text hover:bg-psipro-surface"
                }`}
              >
                <div className="font-medium">{clinic.name}</div>
                <div className="flex items-center gap-2 mt-1">
                  <span className={`text-xs px-2 py-0.5 rounded ${getRoleBadgeColor(clinic.role || "")}`}>
                    {getRoleLabel(clinic.role || "")}
                  </span>
                </div>
              </button>
            ))}
          </div>
        </>
      )}
    </div>
  );
}


