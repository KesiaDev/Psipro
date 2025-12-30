"use client";

import { useState } from "react";

export default function DadosPessoaisTab() {
  const [isEditing, setIsEditing] = useState(false);

  // Dados mockados - serão substituídos por dados reais depois
  const [formData, setFormData] = useState({
    nome: "Maria Silva Santos",
    cpf: "123.456.789-00",
    rg: "12.345.678-9",
    telefone: "(11) 98765-4321",
    email: "maria.silva@email.com",
    endereco: "Rua das Flores, 123",
    complemento: "Apto 45",
    bairro: "Centro",
    cidade: "São Paulo",
    estado: "SP",
    cep: "01234-567",
    tipoAtendimento: "Adulto",
    observacoes: "Paciente prefere atendimento no período da tarde. Sem restrições de horário.",
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSave = () => {
    // Aqui será implementada a lógica de salvamento
    setIsEditing(false);
    // Simular salvamento automático
    console.log("Dados salvos:", formData);
  };

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h2 className="text-xl font-semibold text-psipro-text mb-1">Dados Pessoais</h2>
          <p className="text-sm text-psipro-text-secondary">
            Informações básicas e administrativas do paciente
          </p>
        </div>
        {!isEditing ? (
          <button
            onClick={() => setIsEditing(true)}
            className="px-4 py-2 text-sm font-medium text-psipro-primary bg-psipro-primary/10 border border-psipro-primary/30 rounded-lg hover:bg-psipro-primary/20 transition-all"
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
        <div className="p-6 space-y-6">
          {/* Identificação */}
          <div>
            <h3 className="text-base font-semibold text-psipro-text mb-4 pb-2 border-b border-psipro-divider">
              Identificação
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-psipro-text-secondary mb-1.5">
                  Nome completo *
                </label>
                {isEditing ? (
                  <input
                    type="text"
                    name="nome"
                    value={formData.nome}
                    onChange={handleChange}
                    className="w-full px-3 py-2 border border-psipro-border rounded-lg bg-psipro-background text-psipro-text focus:ring-2 focus:ring-psipro-primary focus:border-psipro-primary outline-none"
                  />
                ) : (
                  <p className="text-psipro-text">{formData.nome}</p>
                )}
              </div>
              <div>
                <label className="block text-sm font-medium text-psipro-text-secondary mb-1.5">
                  CPF *
                </label>
                {isEditing ? (
                  <input
                    type="text"
                    name="cpf"
                    value={formData.cpf}
                    onChange={handleChange}
                    className="w-full px-3 py-2 border border-psipro-border rounded-lg bg-psipro-background text-psipro-text focus:ring-2 focus:ring-psipro-primary focus:border-psipro-primary outline-none"
                  />
                ) : (
                  <p className="text-psipro-text">{formData.cpf}</p>
                )}
              </div>
              <div>
                <label className="block text-sm font-medium text-psipro-text-secondary mb-1.5">
                  RG
                </label>
                {isEditing ? (
                  <input
                    type="text"
                    name="rg"
                    value={formData.rg}
                    onChange={handleChange}
                    className="w-full px-3 py-2 border border-psipro-border rounded-lg bg-psipro-background text-psipro-text focus:ring-2 focus:ring-psipro-primary focus:border-psipro-primary outline-none"
                  />
                ) : (
                  <p className="text-psipro-text">{formData.rg}</p>
                )}
              </div>
            </div>
          </div>

          {/* Contato */}
          <div>
            <h3 className="text-base font-semibold text-psipro-text mb-4 pb-2 border-b border-psipro-divider">
              Contato
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-psipro-text-secondary mb-1.5">
                  Telefone *
                </label>
                {isEditing ? (
                  <input
                    type="tel"
                    name="telefone"
                    value={formData.telefone}
                    onChange={handleChange}
                    className="w-full px-3 py-2 border border-psipro-border rounded-lg bg-psipro-background text-psipro-text focus:ring-2 focus:ring-psipro-primary focus:border-psipro-primary outline-none"
                  />
                ) : (
                  <p className="text-psipro-text">{formData.telefone}</p>
                )}
              </div>
              <div>
                <label className="block text-sm font-medium text-psipro-text-secondary mb-1.5">
                  E-mail
                </label>
                {isEditing ? (
                  <input
                    type="email"
                    name="email"
                    value={formData.email}
                    onChange={handleChange}
                    className="w-full px-3 py-2 border border-psipro-border rounded-lg bg-psipro-background text-psipro-text focus:ring-2 focus:ring-psipro-primary focus:border-psipro-primary outline-none"
                  />
                ) : (
                  <p className="text-psipro-text">{formData.email}</p>
                )}
              </div>
            </div>
          </div>

          {/* Endereço */}
          <div>
            <h3 className="text-base font-semibold text-psipro-text mb-4 pb-2 border-b border-psipro-divider">
              Endereço
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="md:col-span-2">
                <label className="block text-sm font-medium text-psipro-text-secondary mb-1.5">
                  Logradouro
                </label>
                {isEditing ? (
                  <input
                    type="text"
                    name="endereco"
                    value={formData.endereco}
                    onChange={handleChange}
                    className="w-full px-3 py-2 border border-psipro-border rounded-lg bg-psipro-background text-psipro-text focus:ring-2 focus:ring-psipro-primary focus:border-psipro-primary outline-none"
                  />
                ) : (
                  <p className="text-psipro-text">{formData.endereco}</p>
                )}
              </div>
              <div>
                <label className="block text-sm font-medium text-psipro-text-secondary mb-1.5">
                  Complemento
                </label>
                {isEditing ? (
                  <input
                    type="text"
                    name="complemento"
                    value={formData.complemento}
                    onChange={handleChange}
                    className="w-full px-3 py-2 border border-psipro-border rounded-lg bg-psipro-background text-psipro-text focus:ring-2 focus:ring-psipro-primary focus:border-psipro-primary outline-none"
                  />
                ) : (
                  <p className="text-psipro-text">{formData.complemento}</p>
                )}
              </div>
              <div>
                <label className="block text-sm font-medium text-psipro-text-secondary mb-1.5">
                  Bairro
                </label>
                {isEditing ? (
                  <input
                    type="text"
                    name="bairro"
                    value={formData.bairro}
                    onChange={handleChange}
                    className="w-full px-3 py-2 border border-psipro-border rounded-lg bg-psipro-background text-psipro-text focus:ring-2 focus:ring-psipro-primary focus:border-psipro-primary outline-none"
                  />
                ) : (
                  <p className="text-psipro-text">{formData.bairro}</p>
                )}
              </div>
              <div>
                <label className="block text-sm font-medium text-psipro-text-secondary mb-1.5">
                  Cidade
                </label>
                {isEditing ? (
                  <input
                    type="text"
                    name="cidade"
                    value={formData.cidade}
                    onChange={handleChange}
                    className="w-full px-3 py-2 border border-psipro-border rounded-lg bg-psipro-background text-psipro-text focus:ring-2 focus:ring-psipro-primary focus:border-psipro-primary outline-none"
                  />
                ) : (
                  <p className="text-psipro-text">{formData.cidade}</p>
                )}
              </div>
              <div>
                <label className="block text-sm font-medium text-psipro-text-secondary mb-1.5">
                  Estado
                </label>
                {isEditing ? (
                  <input
                    type="text"
                    name="estado"
                    value={formData.estado}
                    onChange={handleChange}
                    className="w-full px-3 py-2 border border-psipro-border rounded-lg bg-psipro-background text-psipro-text focus:ring-2 focus:ring-psipro-primary focus:border-psipro-primary outline-none"
                  />
                ) : (
                  <p className="text-psipro-text">{formData.estado}</p>
                )}
              </div>
              <div>
                <label className="block text-sm font-medium text-psipro-text-secondary mb-1.5">
                  CEP
                </label>
                {isEditing ? (
                  <input
                    type="text"
                    name="cep"
                    value={formData.cep}
                    onChange={handleChange}
                    className="w-full px-3 py-2 border border-psipro-border rounded-lg bg-psipro-background text-psipro-text focus:ring-2 focus:ring-psipro-primary focus:border-psipro-primary outline-none"
                  />
                ) : (
                  <p className="text-psipro-text">{formData.cep}</p>
                )}
              </div>
            </div>
          </div>

          {/* Informações de Atendimento */}
          <div>
            <h3 className="text-base font-semibold text-psipro-text mb-4 pb-2 border-b border-psipro-divider">
              Informações de Atendimento
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-psipro-text-secondary mb-1.5">
                  Tipo de atendimento *
                </label>
                {isEditing ? (
                  <select
                    name="tipoAtendimento"
                    value={formData.tipoAtendimento}
                    onChange={handleChange}
                    className="w-full px-3 py-2 border border-psipro-border rounded-lg bg-psipro-background text-psipro-text focus:ring-2 focus:ring-psipro-primary focus:border-psipro-primary outline-none"
                  >
                    <option value="Adulto">Adulto</option>
                    <option value="Infantil">Infantil</option>
                    <option value="Casal">Casal</option>
                  </select>
                ) : (
                  <p className="text-psipro-text">{formData.tipoAtendimento}</p>
                )}
              </div>
            </div>
          </div>

          {/* Observações Administrativas */}
          <div>
            <h3 className="text-base font-semibold text-psipro-text mb-4 pb-2 border-b border-psipro-divider">
              Observações Administrativas
            </h3>
            <div>
              <label className="block text-sm font-medium text-psipro-text-secondary mb-1.5">
                Observações
              </label>
              {isEditing ? (
                <textarea
                  name="observacoes"
                  value={formData.observacoes}
                  onChange={handleChange}
                  rows={4}
                  className="w-full px-3 py-2 border border-psipro-border rounded-lg bg-psipro-background text-psipro-text focus:ring-2 focus:ring-psipro-primary focus:border-psipro-primary outline-none resize-none"
                  placeholder="Anotações administrativas sobre o paciente..."
                />
              ) : (
                <p className="text-psipro-text whitespace-pre-wrap leading-relaxed">
                  {formData.observacoes || "Nenhuma observação registrada."}
                </p>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}




