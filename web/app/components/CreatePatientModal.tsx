"use client";

import { useEffect, useMemo, useState } from "react";
import { createPortal } from "react-dom";

export type CreatePatientFormValues = {
  name: string;
  phone: string;
  email: string;
  cpf: string;
  birthDate: string;
};

type Props = {
  isOpen: boolean;
  isSubmitting?: boolean;
  onClose: () => void;
  onSubmit: (values: CreatePatientFormValues) => void;
};

export default function CreatePatientModal({
  isOpen,
  isSubmitting = false,
  onClose,
  onSubmit,
}: Props) {
  const [name, setName] = useState("");
  const [phone, setPhone] = useState("");
  const [email, setEmail] = useState("");
  const [cpf, setCpf] = useState("");
  const [birthDate, setBirthDate] = useState("");

  useEffect(() => {
    if (!isOpen) return;
    // Resetar campos toda vez que abrir
    setName("");
    setPhone("");
    setEmail("");
    setCpf("");
    setBirthDate("");
  }, [isOpen]);

  const canSubmit = useMemo(() => {
    return name.trim().length > 0 && !isSubmitting;
  }, [name, isSubmitting]);

  if (!isOpen) return null;

  const modalContent = (
    <div
      className="fixed inset-0 z-[99999] flex items-center justify-center bg-black/60"
      role="dialog"
      aria-modal="true"
      aria-labelledby="create-patient-title"
    >
      <div
        className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-lg max-w-xl w-full mx-4"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="p-6 border-b border-psipro-divider flex items-center justify-between">
          <div>
            <h2 id="create-patient-title" className="text-xl font-semibold text-psipro-text">Novo paciente</h2>
            <p className="text-sm text-psipro-text-secondary mt-1">
              Cadastre manualmente e salve no backend.
            </p>
          </div>
          <button
            onClick={onClose}
            className="p-2 text-psipro-text-muted hover:text-psipro-text hover:bg-psipro-surface rounded-lg transition-colors"
            aria-label="Fechar"
          >
            ✕
          </button>
        </div>

        <div className="p-6 space-y-4">
          <div>
            <label
              htmlFor="create-patient-name"
              className="block text-sm font-medium text-psipro-text-secondary mb-1"
            >
              Nome <span className="text-psipro-error">*</span>
            </label>
            <input
              id="create-patient-name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full px-3 py-2 border border-psipro-border rounded-lg bg-psipro-background text-psipro-text focus:ring-2 focus:ring-psipro-primary focus:border-psipro-primary outline-none text-sm"
              placeholder="Ex.: Maria Silva"
              autoFocus
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label
                htmlFor="create-patient-phone"
                className="block text-sm font-medium text-psipro-text-secondary mb-1"
              >
                Telefone
              </label>
              <input
                id="create-patient-phone"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                className="w-full px-3 py-2 border border-psipro-border rounded-lg bg-psipro-background text-psipro-text focus:ring-2 focus:ring-psipro-primary focus:border-psipro-primary outline-none text-sm"
                placeholder="(11) 99999-9999"
              />
            </div>
            <div>
              <label
                htmlFor="create-patient-email"
                className="block text-sm font-medium text-psipro-text-secondary mb-1"
              >
                E-mail
              </label>
              <input
                id="create-patient-email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full px-3 py-2 border border-psipro-border rounded-lg bg-psipro-background text-psipro-text focus:ring-2 focus:ring-psipro-primary focus:border-psipro-primary outline-none text-sm"
                placeholder="email@exemplo.com"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label
                htmlFor="create-patient-cpf"
                className="block text-sm font-medium text-psipro-text-secondary mb-1"
              >
                CPF
              </label>
              <input
                id="create-patient-cpf"
                value={cpf}
                onChange={(e) => setCpf(e.target.value)}
                className="w-full px-3 py-2 border border-psipro-border rounded-lg bg-psipro-background text-psipro-text focus:ring-2 focus:ring-psipro-primary focus:border-psipro-primary outline-none text-sm"
                placeholder="000.000.000-00"
              />
            </div>
            <div>
              <label
                htmlFor="create-patient-birthDate"
                className="block text-sm font-medium text-psipro-text-secondary mb-1"
              >
                Data de nascimento
              </label>
              <input
                id="create-patient-birthDate"
                value={birthDate}
                onChange={(e) => setBirthDate(e.target.value)}
                type="date"
                className="w-full px-3 py-2 border border-psipro-border rounded-lg bg-psipro-background text-psipro-text focus:ring-2 focus:ring-psipro-primary focus:border-psipro-primary outline-none text-sm"
              />
            </div>
          </div>
        </div>

        <div className="p-6 border-t border-psipro-divider flex justify-end gap-3">
          <button
            onClick={onClose}
            className="px-4 py-2 text-sm font-medium text-psipro-text-secondary bg-psipro-surface border border-psipro-border rounded-lg hover:bg-psipro-surface-elevated transition-all"
            disabled={isSubmitting}
          >
            Cancelar
          </button>
          <button
            onClick={() =>
              onSubmit({
                name,
                phone,
                email,
                cpf,
                birthDate,
              })
            }
            className="px-4 py-2 text-sm font-medium text-psipro-text bg-psipro-primary rounded-lg hover:bg-psipro-primary-dark transition-all disabled:opacity-60 disabled:cursor-not-allowed"
            disabled={!canSubmit}
          >
            {isSubmitting ? "Salvando..." : "Salvar paciente"}
          </button>
        </div>
      </div>
    </div>
  );

  if (typeof document !== "undefined") {
    return createPortal(modalContent, document.body);
  }
  return modalContent;
}

