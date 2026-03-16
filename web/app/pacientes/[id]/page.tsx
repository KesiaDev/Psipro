"use client";

import { useParams } from "next/navigation";
import { useState } from "react";
import PatientHeader from "@/app/components/paciente/PatientHeader";
import VisaoGeralTab from "@/app/components/paciente/VisaoGeralTab";
import HistoricoClinicoTab from "@/app/components/paciente/HistoricoClinicoTab";
import FinanceiroPacienteTab from "@/app/components/paciente/FinanceiroPacienteTab";
import DocumentosArquivosTab from "@/app/components/paciente/DocumentosArquivosTab";
import DadosCadastraisTab from "@/app/components/paciente/DadosCadastraisTab";
import AnamneseTab from "@/app/components/paciente/AnamneseTab";

const tabs = [
  { id: "visao-geral", label: "Visão Geral", icon: "📊", component: VisaoGeralTab },
  { id: "historico", label: "Histórico Clínico", icon: "📝", component: HistoricoClinicoTab },
  { id: "anamnese", label: "Anamnese", icon: "📋", component: AnamneseTab },
  { id: "financeiro", label: "Financeiro", icon: "💰", component: FinanceiroPacienteTab },
  { id: "documentos", label: "Documentos & Arquivos", icon: "📄", component: DocumentosArquivosTab },
  { id: "dados", label: "Dados Cadastrais", icon: "👤", component: DadosCadastraisTab },
];

export default function PatientPage() {
  const params = useParams();
  const patientId = params.id as string;
  const [activeTab, setActiveTab] = useState("visao-geral");

  const ActiveComponent = tabs.find((tab) => tab.id === activeTab)?.component || VisaoGeralTab;

  return (
    <div>
      {/* Header do paciente (fixo) */}
      <div className="mb-8">
        <PatientHeader patientId={patientId} />
      </div>

      {/* Sistema de abas */}
      <div>
        {/* Tabs Navigation */}
        <div className="border-b border-psipro-border mb-8">
          <nav className="flex gap-1 overflow-x-auto -mb-px">
            {tabs.map((tab) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`
                  flex items-center gap-2 px-5 py-3.5 text-sm font-medium border-b-2 transition-all whitespace-nowrap
                  ${
                    activeTab === tab.id
                      ? "border-psipro-primary text-psipro-primary bg-psipro-primary/10"
                      : "border-transparent text-psipro-text-secondary hover:text-psipro-text hover:border-psipro-border"
                  }
                `}
              >
                <span className="text-base">{tab.icon}</span>
                <span>{tab.label}</span>
              </button>
            ))}
          </nav>
        </div>

        {/* Tab Content */}
        <div className="min-h-[500px] pb-8">
          <ActiveComponent patientId={patientId} />
        </div>
      </div>
    </div>
  );
}
