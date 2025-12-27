"use client";

import InsightSection from "@/app/components/InsightSection";
import { Insight } from "@/app/components/InsightCard";

export default function DashboardPage() {
  // Dados mockados - Insights
  const insights: Insight[] = [
    {
      id: "dashboard-1",
      type: "clinico",
      message: "Você tem maior concentração de atendimentos no início da semana.",
    },
    {
      id: "dashboard-2",
      type: "financeiro",
      message: "Sua receita deste mês está abaixo da média dos últimos 3 meses.",
    },
    {
      id: "dashboard-3",
      type: "agenda",
      message: "Há oportunidade para abrir novos horários nas quartas-feiras.",
    },
  ];

  // Cards de métricas (zerados)
  const metricsCards = [
    {
      title: "Pacientes ativos",
      value: "0",
      icon: "👥",
    },
    {
      title: "Consultas no mês",
      value: "0",
      icon: "📅",
    },
    {
      title: "Receita do mês",
      value: "R$ 0,00",
      icon: "💰",
    },
    {
      title: "A receber",
      value: "R$ 0,00",
      icon: "⏳",
    },
  ];

  return (
    <div>
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-psipro-text mb-2 tracking-tight">Dashboard</h1>
        <p className="text-psipro-text-secondary text-lg">Visão geral da sua prática clínica</p>
      </div>

      {/* Cards de métricas */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-5 mb-8">
        {metricsCards.map((card, index) => (
          <div
            key={index}
            className="bg-psipro-surface-elevated rounded-lg border border-psipro-border p-6 hover:shadow-md transition-all duration-200"
          >
            <div className="flex items-start justify-between">
              <div className="flex-1">
                <p className="text-sm text-psipro-text-secondary mb-2 font-medium">
                  {card.title}
                </p>
                <p className="text-3xl font-bold text-psipro-text">{card.value}</p>
              </div>
              <span className="text-3xl opacity-60">{card.icon}</span>
            </div>
          </div>
        ))}
      </div>

      {/* Grid de conteúdo */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
        {/* Bloco de agenda resumida */}
        <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm">
          <div className="p-6 border-b border-psipro-divider">
            <h2 className="text-lg font-semibold text-psipro-text">Próximas consultas</h2>
          </div>
          <div className="p-6">
            <div className="text-center py-12">
              <div className="text-5xl mb-4 opacity-40">📅</div>
              <p className="text-psipro-text-secondary text-sm mb-1">
                Nenhuma consulta encontrada.
              </p>
              <p className="text-psipro-text-muted text-xs">
                As consultas agendadas e realizadas no app aparecem aqui automaticamente.
              </p>
            </div>
          </div>
        </div>

        {/* Bloco financeiro resumido */}
        <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm">
          <div className="p-6 border-b border-psipro-divider">
            <h2 className="text-lg font-semibold text-psipro-text">Resumo financeiro</h2>
          </div>
          <div className="p-6">
            <p className="text-sm text-psipro-text-secondary leading-relaxed mb-4">
              Acompanhe entradas, valores a receber e desempenho mensal. Os dados são gerados a
              partir das sessões realizadas no app e sincronizados automaticamente para visualização na web.
            </p>
            <div className="space-y-3">
              <div className="flex items-center justify-between p-3 bg-psipro-surface rounded-lg border border-psipro-border">
                <span className="text-sm text-psipro-text-secondary">Receita do mês</span>
                <span className="text-base font-semibold text-psipro-text">R$ 0,00</span>
              </div>
              <div className="flex items-center justify-between p-3 bg-psipro-surface rounded-lg border border-psipro-border">
                <span className="text-sm text-psipro-text-secondary">A receber</span>
                <span className="text-base font-semibold text-psipro-text">R$ 0,00</span>
              </div>
              <div className="flex items-center justify-between p-3 bg-psipro-surface rounded-lg border border-psipro-border">
                <span className="text-sm text-psipro-text-secondary">Sessões realizadas no app</span>
                <span className="text-base font-semibold text-psipro-text">0</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Insights do PsiPro */}
      <div className="mb-8">
        <InsightSection insights={insights} maxItems={3} />
      </div>

      {/* Mensagem de orientação de produto */}
      <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm p-6">
        <div className="max-w-2xl">
            <h3 className="text-base font-semibold text-psipro-text mb-3">O PsiPro funciona assim:</h3>
            <ul className="space-y-2 text-sm text-psipro-text-secondary">
              <li className="flex items-start gap-2">
                <span className="text-psipro-primary mt-0.5">•</span>
                <span>
                  <strong>App Android:</strong> Agendar e realizar consultas, marcar sessão como realizada,
                  registrar pagamento e fazer anotações. Uso rápido e operacional no dia a dia.
                </span>
              </li>
              <li className="flex items-start gap-2">
                <span className="text-psipro-primary mt-0.5">•</span>
                <span>
                  <strong>Web:</strong> Importar e organizar pacientes, visualizar agenda geral, acompanhar
                  financeiro e ver indicadores. Gestão centralizada com mais conforto e visão ampla.
                </span>
              </li>
            </ul>
        </div>
      </div>
    </div>
  );
}
