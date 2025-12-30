"use client";

import { useState } from "react";
import { useToast } from "../contexts/ToastContext";
import Link from "next/link";

interface BetaAccessFormProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess?: () => void;
}

/**
 * Formulário de Solicitação de Acesso Beta
 * 
 * Coleta informações do profissional interessado em participar do beta.
 * Linguagem profissional, acolhedora e sem pressão.
 */
export default function BetaAccessForm({ isOpen, onClose, onSuccess }: BetaAccessFormProps) {
  const { showSuccess, showError } = useToast();
  const [formData, setFormData] = useState({
    fullName: "",
    email: "",
    city: "",
    state: "",
    practiceType: "",
    expectations: "", // Campo opcional: "O que você espera organizar melhor hoje?"
  });
  const [loading, setLoading] = useState(false);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      // Validação básica
      if (!formData.fullName || !formData.email) {
        showError("Nome completo e e-mail são obrigatórios.");
        setLoading(false);
        return;
      }

      // TODO: Integrar com API quando disponível
      // Por enquanto, salva no console e simula sucesso
      console.log("Solicitação de acesso beta:", formData);
      
      // Simula delay de API
      await new Promise((resolve) => setTimeout(resolve, 1500));

      showSuccess("Recebemos seu pedido. Em breve entraremos em contato com mais informações.");
      
      // Limpar formulário
      setFormData({
        fullName: "",
        email: "",
        city: "",
        state: "",
        practiceType: "",
        expectations: "",
      });

      // Callback de sucesso
      if (onSuccess) {
        onSuccess();
      } else {
        onClose();
      }
    } catch (error) {
      showError("Erro ao enviar solicitação. Tente novamente.");
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>
  ) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  return (
    <>
      {/* Overlay */}
      <div
        className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4"
        onClick={onClose}
      >
        {/* Modal */}
        <div
          className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto animate-in fade-in zoom-in-95 duration-300"
          onClick={(e) => e.stopPropagation()}
        >
          <div className="p-8">
            {/* Header */}
            <div className="flex items-start justify-between mb-6">
              <div>
                <h2 className="text-2xl font-bold text-psipro-text mb-2">
                  Solicitar acesso ao beta
                </h2>
                <p className="text-sm text-psipro-text-secondary">
                  Preencha o formulário e entraremos em contato em breve.
                </p>
              </div>
              <button
                onClick={onClose}
                className="text-psipro-text-secondary hover:text-psipro-text transition-colors text-xl"
                aria-label="Fechar"
              >
                ✕
              </button>
            </div>

            {/* Form */}
            <form onSubmit={handleSubmit} className="space-y-4">
              {/* Nome completo */}
              <div>
                <label htmlFor="fullName" className="block text-sm font-medium text-psipro-text mb-2">
                  Nome completo <span className="text-psipro-error">*</span>
                </label>
                <input
                  type="text"
                  id="fullName"
                  name="fullName"
                  value={formData.fullName}
                  onChange={handleChange}
                  required
                  className="w-full px-4 py-2 bg-psipro-background border border-psipro-border rounded-lg text-psipro-text focus:outline-none focus:ring-2 focus:ring-psipro-primary"
                  placeholder="Seu nome completo"
                />
              </div>

              {/* E-mail profissional */}
              <div>
                <label htmlFor="email" className="block text-sm font-medium text-psipro-text mb-2">
                  E-mail profissional <span className="text-psipro-error">*</span>
                </label>
                <input
                  type="email"
                  id="email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  required
                  className="w-full px-4 py-2 bg-psipro-background border border-psipro-border rounded-lg text-psipro-text focus:outline-none focus:ring-2 focus:ring-psipro-primary"
                  placeholder="seu@email.com"
                />
              </div>

              {/* Cidade / Estado */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label htmlFor="city" className="block text-sm font-medium text-psipro-text mb-2">
                    Cidade
                  </label>
                  <input
                    type="text"
                    id="city"
                    name="city"
                    value={formData.city}
                    onChange={handleChange}
                    className="w-full px-4 py-2 bg-psipro-background border border-psipro-border rounded-lg text-psipro-text focus:outline-none focus:ring-2 focus:ring-psipro-primary"
                    placeholder="Sua cidade"
                  />
                </div>
                <div>
                  <label htmlFor="state" className="block text-sm font-medium text-psipro-text mb-2">
                    Estado
                  </label>
                  <input
                    type="text"
                    id="state"
                    name="state"
                    value={formData.state}
                    onChange={handleChange}
                    className="w-full px-4 py-2 bg-psipro-background border border-psipro-border rounded-lg text-psipro-text focus:outline-none focus:ring-2 focus:ring-psipro-primary"
                    placeholder="Seu estado"
                  />
                </div>
              </div>

              {/* Atua como */}
              <div>
                <label htmlFor="practiceType" className="block text-sm font-medium text-psipro-text mb-2">
                  Atua como
                </label>
                <select
                  id="practiceType"
                  name="practiceType"
                  value={formData.practiceType}
                  onChange={handleChange}
                  className="w-full px-4 py-2 bg-psipro-background border border-psipro-border rounded-lg text-psipro-text focus:outline-none focus:ring-2 focus:ring-psipro-primary"
                >
                  <option value="">Selecione...</option>
                  <option value="autonomo">Psicólogo(a) autônomo(a)</option>
                  <option value="clinica">Clínica / consultório</option>
                </select>
              </div>

              {/* Campo opcional: O que você espera organizar melhor hoje? */}
              <div>
                <label htmlFor="expectations" className="block text-sm font-medium text-psipro-text mb-2">
                  O que você espera organizar melhor hoje? <span className="text-psipro-text-muted text-xs">(opcional)</span>
                </label>
                <textarea
                  id="expectations"
                  name="expectations"
                  value={formData.expectations}
                  onChange={handleChange}
                  rows={3}
                  className="w-full px-4 py-2 bg-psipro-background border border-psipro-border rounded-lg text-psipro-text focus:outline-none focus:ring-2 focus:ring-psipro-primary resize-none"
                  placeholder="Conte-nos o que você gostaria de organizar melhor na sua rotina..."
                />
              </div>

              {/* Footer */}
              <div className="flex items-center justify-between pt-4 border-t border-psipro-border">
                <p className="text-xs text-psipro-text-secondary">
                  <span className="text-psipro-error">*</span> Campos obrigatórios
                </p>
                <div className="flex gap-3">
                  <button
                    type="button"
                    onClick={onClose}
                    className="px-4 py-2 text-sm text-psipro-text-secondary hover:text-psipro-text transition-colors"
                  >
                    Cancelar
                  </button>
                  <button
                    type="submit"
                    disabled={loading}
                    className="px-6 py-2 bg-psipro-primary text-white rounded-lg hover:bg-psipro-primary-dark transition-colors font-medium disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    {loading ? "Enviando..." : "Enviar solicitação"}
                  </button>
                </div>
              </div>
            </form>
          </div>
        </div>
      </div>
    </>
  );
}


