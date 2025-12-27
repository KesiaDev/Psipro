"use client";

import { useState } from "react";

export default function DadosCadastraisTab({ patientId }: { patientId: string }) {
  const [isEditing, setIsEditing] = useState(false);

  // Dados mockados
  const [formData, setFormData] = useState({
    name: "Maria Silva Santos",
    cpf: "123.456.789-00",
    birthDate: "15/05/1990",
    phone: "(11) 98765-4321",
    email: "maria.silva@email.com",
    address: "Rua das Flores, 123 - São Paulo, SP",
    emergencyContact: "João Silva - (11) 91234-5678",
    observations: "Paciente prefere contato por WhatsApp.",
  });

  const handleChange = (field: string, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
  };

  const handleSave = () => {
    // Aqui seria a chamada à API
    setIsEditing(false);
  };

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
              className="px-4 py-2 text-sm font-medium text-psipro-text bg-psipro-primary rounded-lg hover:bg-psipro-primary-dark transition-all"
            >
              Salvar
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
              <label className="block text-sm font-medium text-psipro-text-secondary mb-2">
                Nome completo
              </label>
              {isEditing ? (
                <input
                  type="text"
                  value={formData.name}
                  onChange={(e) => handleChange("name", e.target.value)}
                  className="w-full px-4 py-2 border border-psipro-border rounded-lg bg-psipro-background text-psipro-text focus:ring-2 focus:ring-psipro-primary focus:border-psipro-primary outline-none"
                />
              ) : (
                <p className="text-psipro-text">{formData.name}</p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium text-psipro-text-secondary mb-2">
                CPF
              </label>
              {isEditing ? (
                <input
                  type="text"
                  value={formData.cpf}
                  onChange={(e) => handleChange("cpf", e.target.value)}
                  className="w-full px-4 py-2 border border-psipro-border rounded-lg bg-psipro-background text-psipro-text focus:ring-2 focus:ring-psipro-primary focus:border-psipro-primary outline-none"
                />
              ) : (
                <p className="text-psipro-text">{formData.cpf}</p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium text-psipro-text-secondary mb-2">
                Data de nascimento
              </label>
              {isEditing ? (
                <input
                  type="text"
                  value={formData.birthDate}
                  onChange={(e) => handleChange("birthDate", e.target.value)}
                  className="w-full px-4 py-2 border border-psipro-border rounded-lg bg-psipro-background text-psipro-text focus:ring-2 focus:ring-psipro-primary focus:border-psipro-primary outline-none"
                />
              ) : (
                <p className="text-psipro-text">{formData.birthDate}</p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium text-psipro-text-secondary mb-2">
                Telefone
              </label>
              {isEditing ? (
                <input
                  type="text"
                  value={formData.phone}
                  onChange={(e) => handleChange("phone", e.target.value)}
                  className="w-full px-4 py-2 border border-psipro-border rounded-lg bg-psipro-background text-psipro-text focus:ring-2 focus:ring-psipro-primary focus:border-psipro-primary outline-none"
                />
              ) : (
                <p className="text-psipro-text">{formData.phone}</p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium text-psipro-text-secondary mb-2">
                E-mail
              </label>
              {isEditing ? (
                <input
                  type="email"
                  value={formData.email}
                  onChange={(e) => handleChange("email", e.target.value)}
                  className="w-full px-4 py-2 border border-psipro-border rounded-lg bg-psipro-background text-psipro-text focus:ring-2 focus:ring-psipro-primary focus:border-psipro-primary outline-none"
                />
              ) : (
                <p className="text-psipro-text">{formData.email}</p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium text-psipro-text-secondary mb-2">
                Endereço
              </label>
              {isEditing ? (
                <input
                  type="text"
                  value={formData.address}
                  onChange={(e) => handleChange("address", e.target.value)}
                  className="w-full px-4 py-2 border border-psipro-border rounded-lg bg-psipro-background text-psipro-text focus:ring-2 focus:ring-psipro-primary focus:border-psipro-primary outline-none"
                />
              ) : (
                <p className="text-psipro-text">{formData.address}</p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium text-psipro-text-secondary mb-2">
                Contato de emergência
              </label>
              {isEditing ? (
                <input
                  type="text"
                  value={formData.emergencyContact}
                  onChange={(e) => handleChange("emergencyContact", e.target.value)}
                  className="w-full px-4 py-2 border border-psipro-border rounded-lg bg-psipro-background text-psipro-text focus:ring-2 focus:ring-psipro-primary focus:border-psipro-primary outline-none"
                />
              ) : (
                <p className="text-psipro-text">{formData.emergencyContact}</p>
              )}
            </div>
          </div>

          <div className="mt-6">
            <label className="block text-sm font-medium text-psipro-text-secondary mb-2">
              Observações administrativas
            </label>
            {isEditing ? (
              <textarea
                value={formData.observations}
                onChange={(e) => handleChange("observations", e.target.value)}
                rows={4}
                className="w-full px-4 py-2 border border-psipro-border rounded-lg bg-psipro-background text-psipro-text focus:ring-2 focus:ring-psipro-primary focus:border-psipro-primary outline-none"
              />
            ) : (
              <p className="text-psipro-text leading-relaxed">{formData.observations}</p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

