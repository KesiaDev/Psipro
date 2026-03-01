"use client";

import { useState, useEffect } from "react";
import { patientService, type Patient } from "@/app/services/patientService";
import Skeleton from "@/app/components/Skeleton";

function formatDateForInput(iso?: string) {
  if (!iso) return "";
  try {
    const d = new Date(iso);
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, "0");
    const day = String(d.getDate()).padStart(2, "0");
    return `${y}-${m}-${day}`;
  } catch {
    return "";
  }
}

function formatDateFromInput(val: string): string {
  if (!val) return "";
  try {
    const d = new Date(val + "T12:00:00");
    return d.toISOString();
  } catch {
    return "";
  }
}

export default function DadosCadastraisTab({ patientId }: { patientId: string }) {
  const [isEditing, setIsEditing] = useState(false);
  const [saving, setSaving] = useState(false);
  const [patient, setPatient] = useState<Patient | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [formData, setFormData] = useState({
    name: "",
    cpf: "",
    birthDate: "",
    phone: "",
    email: "",
    address: "",
    emergencyContact: "",
    observations: "",
  });

  useEffect(() => {
    let cancelled = false;
    patientService
      .getPatientById(patientId)
      .then((p) => {
        if (!cancelled) {
          setPatient(p);
          setFormData({
            name: p.name ?? "",
            cpf: p.cpf ?? "",
            birthDate: formatDateForInput(p.birthDate),
            phone: p.phone ?? "",
            email: p.email ?? "",
            address: p.address ?? "",
            emergencyContact: p.emergencyContact ?? "",
            observations: p.observations ?? "",
          });
        }
      })
      .catch((e) => {
        if (!cancelled) {
          setError(
            e && typeof e === "object" && "message" in e
              ? String((e as { message: string }).message)
              : "Erro ao carregar paciente"
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

  const handleChange = (field: string, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
  };

  const handleSave = () => {
    if (!patientId) return;
    setSaving(true);
    patientService
      .updatePatient(patientId, {
        name: formData.name || undefined,
        cpf: formData.cpf || undefined,
        birthDate: formData.birthDate ? formatDateFromInput(formData.birthDate) : undefined,
        phone: formData.phone || undefined,
        email: formData.email || undefined,
        address: formData.address || undefined,
        emergencyContact: formData.emergencyContact || undefined,
        observations: formData.observations || undefined,
      })
      .then((updated) => {
        setPatient(updated);
        setIsEditing(false);
      })
      .catch((e) => {
        setError(
          e && typeof e === "object" && "message" in e
            ? String((e as { message: string }).message)
            : "Erro ao salvar"
        );
      })
      .finally(() => setSaving(false));
  };

  if (loading) {
    return (
      <div className="space-y-4">
        <Skeleton className="h-64" />
      </div>
    );
  }

  if (error && !patient) {
    return (
      <div className="bg-psipro-error/10 border border-psipro-error/20 text-psipro-error rounded-lg p-6">
        <p className="font-medium">{error}</p>
      </div>
    );
  }

  return (
    <div>
      <div className="mb-4 flex items-center justify-between">
        <p className="text-sm text-psipro-text-secondary">
          Dados pessoais e informações de contato. Esta é a única seção editável na web.
        </p>
        {!isEditing ? (
          <button
            onClick={() => setIsEditing(true)}
            className="px-4 py-2 text-sm font-medium text-psipro-text bg-psipro-primary rounded-lg hover:bg-psipro-primary-dark transition-all"
          >
            Editar
          </button>
        ) : (
          <div className="flex gap-2">
            <button
              onClick={() => setIsEditing(false)}
              className="px-4 py-2 text-sm font-medium text-psipro-text-secondary bg-psipro-surface border border-psipro-border rounded-lg hover:bg-psipro-surface-elevated transition-all"
            >
              Cancelar
            </button>
            <button
              onClick={handleSave}
              disabled={saving}
              className="px-4 py-2 text-sm font-medium text-psipro-text bg-psipro-primary rounded-lg hover:bg-psipro-primary-dark transition-all disabled:opacity-50"
            >
              {saving ? "Salvando..." : "Salvar"}
            </button>
          </div>
        )}
      </div>

      <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm">
        <div className="p-6 border-b border-psipro-divider">
          <h2 className="text-lg font-semibold text-psipro-text">Dados Pessoais</h2>
        </div>
        <div className="p-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label htmlFor="dados-name" className="block text-sm font-medium text-psipro-text-secondary mb-2">
                Nome completo
              </label>
              {isEditing ? (
                <input
                  id="dados-name"
                  type="text"
                  value={formData.name}
                  aria-label="Nome completo"
                  onChange={(e) => handleChange("name", e.target.value)}
                  className="w-full px-4 py-2 border border-psipro-border rounded-lg bg-psipro-background text-psipro-text focus:ring-2 focus:ring-psipro-primary focus:border-psipro-primary outline-none"
                />
              ) : (
                <p className="text-psipro-text">{formData.name || "—"}</p>
              )}
            </div>

            <div>
              <label htmlFor="dados-cpf" className="block text-sm font-medium text-psipro-text-secondary mb-2">
                CPF
              </label>
              {isEditing ? (
                <input
                  id="dados-cpf"
                  type="text"
                  value={formData.cpf}
                  aria-label="CPF"
                  onChange={(e) => handleChange("cpf", e.target.value)}
                  className="w-full px-4 py-2 border border-psipro-border rounded-lg bg-psipro-background text-psipro-text focus:ring-2 focus:ring-psipro-primary focus:border-psipro-primary outline-none"
                />
              ) : (
                <p className="text-psipro-text">{formData.cpf || "—"}</p>
              )}
            </div>

            <div>
              <label htmlFor="dados-birthDate" className="block text-sm font-medium text-psipro-text-secondary mb-2">
                Data de nascimento
              </label>
              {isEditing ? (
                <input
                  id="dados-birthDate"
                  type="date"
                  value={formData.birthDate}
                  aria-label="Data de nascimento"
                  onChange={(e) => handleChange("birthDate", e.target.value)}
                  className="w-full px-4 py-2 border border-psipro-border rounded-lg bg-psipro-background text-psipro-text focus:ring-2 focus:ring-psipro-primary focus:border-psipro-primary outline-none"
                />
              ) : (
                <p className="text-psipro-text">
                  {formData.birthDate
                    ? new Date(formData.birthDate + "T12:00:00").toLocaleDateString("pt-BR")
                    : "—"}
                </p>
              )}
            </div>

            <div>
              <label htmlFor="dados-phone" className="block text-sm font-medium text-psipro-text-secondary mb-2">
                Telefone
              </label>
              {isEditing ? (
                <input
                  id="dados-phone"
                  type="text"
                  value={formData.phone}
                  aria-label="Telefone"
                  onChange={(e) => handleChange("phone", e.target.value)}
                  className="w-full px-4 py-2 border border-psipro-border rounded-lg bg-psipro-background text-psipro-text focus:ring-2 focus:ring-psipro-primary focus:border-psipro-primary outline-none"
                />
              ) : (
                <p className="text-psipro-text">{formData.phone || "—"}</p>
              )}
            </div>

            <div>
              <label htmlFor="dados-email" className="block text-sm font-medium text-psipro-text-secondary mb-2">
                E-mail
              </label>
              {isEditing ? (
                <input
                  id="dados-email"
                  type="email"
                  value={formData.email}
                  aria-label="E-mail"
                  onChange={(e) => handleChange("email", e.target.value)}
                  className="w-full px-4 py-2 border border-psipro-border rounded-lg bg-psipro-background text-psipro-text focus:ring-2 focus:ring-psipro-primary focus:border-psipro-primary outline-none"
                />
              ) : (
                <p className="text-psipro-text">{formData.email || "—"}</p>
              )}
            </div>

            <div>
              <label htmlFor="dados-address" className="block text-sm font-medium text-psipro-text-secondary mb-2">
                Endereço
              </label>
              {isEditing ? (
                <input
                  id="dados-address"
                  type="text"
                  value={formData.address}
                  aria-label="Endereço"
                  onChange={(e) => handleChange("address", e.target.value)}
                  className="w-full px-4 py-2 border border-psipro-border rounded-lg bg-psipro-background text-psipro-text focus:ring-2 focus:ring-psipro-primary focus:border-psipro-primary outline-none"
                />
              ) : (
                <p className="text-psipro-text">{formData.address || "—"}</p>
              )}
            </div>

            <div>
              <label htmlFor="dados-emergencyContact" className="block text-sm font-medium text-psipro-text-secondary mb-2">
                Contato de emergência
              </label>
              {isEditing ? (
                <input
                  id="dados-emergencyContact"
                  type="text"
                  value={formData.emergencyContact}
                  aria-label="Contato de emergência"
                  onChange={(e) => handleChange("emergencyContact", e.target.value)}
                  className="w-full px-4 py-2 border border-psipro-border rounded-lg bg-psipro-background text-psipro-text focus:ring-2 focus:ring-psipro-primary focus:border-psipro-primary outline-none"
                />
              ) : (
                <p className="text-psipro-text">{formData.emergencyContact || "—"}</p>
              )}
            </div>
          </div>

          <div className="mt-6">
            <label htmlFor="dados-observations" className="block text-sm font-medium text-psipro-text-secondary mb-2">
              Observações administrativas
            </label>
            {isEditing ? (
              <textarea
                id="dados-observations"
                value={formData.observations}
                aria-label="Observações administrativas"
                onChange={(e) => handleChange("observations", e.target.value)}
                rows={4}
                className="w-full px-4 py-2 border border-psipro-border rounded-lg bg-psipro-background text-psipro-text focus:ring-2 focus:ring-psipro-primary focus:border-psipro-primary outline-none"
              />
            ) : (
              <p className="text-psipro-text leading-relaxed">
                {formData.observations || "Nenhuma observação."}
              </p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
