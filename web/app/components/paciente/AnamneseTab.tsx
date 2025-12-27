"use client";

import { useState } from "react";

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

export default function AnamneseTab() {
  return (
    <div>
      <div className="mb-6">
        <p className="text-psipro-text-secondary text-sm leading-relaxed">
          Informações completas da anamnese do paciente. Você pode expandir e
          colapsar cada seção conforme necessário.
        </p>
      </div>

      <div className="space-y-4">
        <CollapsibleSection title="Identificação" defaultOpen={true}>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-psipro-text-secondary mb-1">
                Nome completo
              </label>
              <p className="text-psipro-text">Maria Silva Santos</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Data de nascimento
              </label>
              <p className="text-gray-900">15/08/1990</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                CPF
              </label>
              <p className="text-gray-900">123.456.789-00</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Telefone
              </label>
              <p className="text-gray-900">(11) 98765-4321</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                E-mail
              </label>
              <p className="text-gray-900">maria.silva@email.com</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Endereço
              </label>
              <p className="text-gray-900">
                Rua das Flores, 123 - São Paulo, SP
              </p>
            </div>
          </div>
        </CollapsibleSection>

        <CollapsibleSection title="Histórico familiar">
          <div className="space-y-3">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Composição familiar
              </label>
              <p className="text-gray-900">
                Casada, 2 filhos. Relacionamento estável há 8 anos.
              </p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Histórico de saúde mental na família
              </label>
              <p className="text-gray-900">
                Mãe com histórico de ansiedade. Sem outros casos conhecidos.
              </p>
            </div>
          </div>
        </CollapsibleSection>

        <CollapsibleSection title="Escolaridade e Profissão">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Escolaridade
              </label>
              <p className="text-gray-900">Ensino Superior Completo</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Profissão
              </label>
              <p className="text-gray-900">Arquiteta</p>
            </div>
            <div className="md:col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Situação profissional
              </label>
              <p className="text-gray-900">
                Empregada em escritório de arquitetura. Relata estresse relacionado
                ao trabalho.
              </p>
            </div>
          </div>
        </CollapsibleSection>

        <CollapsibleSection title="Queixas principais">
          <div className="space-y-3">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Motivo da busca por terapia
              </label>
              <p className="text-gray-900 leading-relaxed">
                Ansiedade generalizada, dificuldades para dormir, sensação de
                sobrecarga no trabalho e nos cuidados com os filhos. Busca
                estratégias para melhorar qualidade de vida e gerenciamento do
                estresse.
              </p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Quando começou
              </label>
              <p className="text-gray-900">Há aproximadamente 6 meses</p>
            </div>
          </div>
        </CollapsibleSection>

        <CollapsibleSection title="Histórico médico e psiquiátrico">
          <div className="space-y-3">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Medicações em uso
              </label>
              <p className="text-gray-900">Nenhuma medicação psiquiátrica</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Tratamentos anteriores
              </label>
              <p className="text-gray-900">
                Primeira experiência com terapia psicológica.
              </p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Condições médicas
              </label>
              <p className="text-gray-900">Nenhuma condição médica relevante</p>
            </div>
          </div>
        </CollapsibleSection>
      </div>
    </div>
  );
}

