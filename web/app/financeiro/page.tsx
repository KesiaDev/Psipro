"use client";

import { useState } from "react";
import InsightSection from "@/app/components/InsightSection";
import { Insight } from "@/app/components/InsightCard";

export default function FinanceiroPage() {
  const [periodFilter, setPeriodFilter] = useState<"month" | "quarter" | "year">("month");

  // Dados mockados - Cards principais
  const financialCards = [
    {
      title: "Receita hoje",
      value: "R$ 450,00",
      subtitle: "3 sessões realizadas",
      icon: "💰",
      color: "text-psipro-primary",
    },
    {
      title: "Receita do mês",
      value: "R$ 6.300,00",
      subtitle: "Março 2025",
      icon: "📊",
      color: "text-psipro-primary",
    },
    {
      title: "Total recebido",
      value: "R$ 18.900,00",
      subtitle: "Últimos 3 meses",
      icon: "✅",
      color: "text-psipro-success",
    },
    {
      title: "Total a receber",
      value: "R$ 1.200,00",
      subtitle: "5 pendências",
      icon: "⏳",
      color: "text-psipro-warning",
    },
    {
      title: "Ticket médio por sessão",
      value: "R$ 150,00",
      subtitle: "Média dos últimos 30 dias",
      icon: "📈",
      color: "text-psipro-primary",
    },
  ];

  // Dados mockados - Receita por mês (últimos 6 meses)
  const monthlyRevenue = [
    { month: "Out", value: 5200 },
    { month: "Nov", value: 5800 },
    { month: "Dez", value: 6100 },
    { month: "Jan", value: 5900 },
    { month: "Fev", value: 6200 },
    { month: "Mar", value: 6300 },
  ];

  // Dados mockados - Top 5 pacientes por receita
  const topPatients = [
    { name: "Maria Silva Santos", revenue: 1800, sessions: 12 },
    { name: "João Pedro Oliveira", revenue: 1200, sessions: 8 },
    { name: "Ana Carolina Costa", revenue: 1050, sessions: 7 },
    { name: "Carlos Eduardo Lima", revenue: 900, sessions: 6 },
    { name: "Fernanda Rodrigues", revenue: 750, sessions: 5 },
  ];

  // Dados mockados - Alertas financeiros
  const financialAlerts = [
    {
      type: "warning",
      message: "3 pacientes estão com pagamentos em atraso.",
      details: "Total de R$ 450,00 pendente há mais de 30 dias.",
      action: "Ver detalhes",
    },
    {
      type: "info",
      message: "Receita deste mês está 15% abaixo da média.",
      details: "Média dos últimos 3 meses: R$ 7.400,00",
      action: "Analisar",
    },
    {
      type: "success",
      message: "Paciente Maria Silva Santos concentra alto faturamento.",
      details: "R$ 1.800,00 este mês (28% da receita total)",
      action: "Ver histórico",
    },
  ];

  // Dados mockados - Insights
  const financialInsights: Insight[] = [
    {
      id: "financeiro-1",
      type: "financeiro",
      message: "Sua receita deste mês está abaixo da média dos últimos 3 meses.",
    },
    {
      id: "financeiro-2",
      type: "financeiro",
      message: "Pacientes semanais representam maior previsibilidade financeira.",
    },
    {
      id: "financeiro-3",
      type: "financeiro",
      message: "Sessões canceladas impactaram R$ 600,00 neste período.",
    },
    {
      id: "financeiro-4",
      type: "financeiro",
      message: "Terças-feiras são seus dias mais rentáveis.",
    },
  ];

  const insights = [
    {
      type: "success",
      message: "Seu melhor dia financeiro é terça-feira.",
      details: "Concentra 25% da receita semanal.",
    },
    {
      type: "info",
      message: "Pacientes semanais geram maior previsibilidade.",
      details: "80% da receita vem de pacientes com frequência regular.",
    },
    {
      type: "warning",
      message: "Sessões canceladas impactaram R$ 600,00 este mês.",
      details: "8 cancelamentos sem reagendamento.",
    },
  ];

  // Calcular altura máxima para gráfico
  const maxRevenue = Math.max(...monthlyRevenue.map((m) => m.value));

  return (
    <div>
      {/* Header */}
      <div className="mb-8">
        <div className="flex items-center justify-between mb-4">
          <div>
            <h1 className="text-3xl font-bold text-psipro-text mb-2 tracking-tight">Financeiro</h1>
            <p className="text-psipro-text-secondary text-lg">
              Visão geral financeira do consultório
            </p>
          </div>
          <div className="flex gap-2">
            <button
              onClick={() => setPeriodFilter("month")}
              className={`px-3 py-1.5 text-xs font-medium rounded-lg transition-all ${
                periodFilter === "month"
                  ? "bg-psipro-primary text-psipro-text"
                  : "bg-psipro-surface text-psipro-text-secondary border border-psipro-border hover:bg-psipro-surface-elevated"
              }`}
            >
              Mês
            </button>
            <button
              onClick={() => setPeriodFilter("quarter")}
              className={`px-3 py-1.5 text-xs font-medium rounded-lg transition-all ${
                periodFilter === "quarter"
                  ? "bg-psipro-primary text-psipro-text"
                  : "bg-psipro-surface text-psipro-text-secondary border border-psipro-border hover:bg-psipro-surface-elevated"
              }`}
            >
              Trimestre
            </button>
            <button
              onClick={() => setPeriodFilter("year")}
              className={`px-3 py-1.5 text-xs font-medium rounded-lg transition-all ${
                periodFilter === "year"
                  ? "bg-psipro-primary text-psipro-text"
                  : "bg-psipro-surface text-psipro-text-secondary border border-psipro-border hover:bg-psipro-surface-elevated"
              }`}
            >
              Ano
            </button>
          </div>
        </div>
      </div>

      {/* Cards principais */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-5 mb-8">
        {financialCards.map((card, index) => (
          <div
            key={index}
            className="bg-psipro-surface-elevated rounded-lg border border-psipro-border p-6 hover:shadow-md transition-all duration-200"
          >
            <div className="flex items-start justify-between">
              <div className="flex-1">
                <p className="text-sm text-psipro-text-secondary mb-2 font-medium">
                  {card.title}
                </p>
                <p className={`text-3xl font-bold ${card.color} mb-1`}>{card.value}</p>
                <p className="text-xs text-psipro-text-muted">{card.subtitle}</p>
              </div>
              <span className="text-3xl opacity-60">{card.icon}</span>
            </div>
          </div>
        ))}
      </div>

      {/* Grid principal */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
        {/* Gráfico de receita por período */}
        <div className="lg:col-span-2">
          <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm">
            <div className="p-6 border-b border-psipro-divider">
              <h2 className="text-lg font-semibold text-psipro-text">Receita por Período</h2>
              <p className="text-xs text-psipro-text-muted mt-1">Últimos 6 meses</p>
            </div>
            <div className="p-6">
              <div className="flex items-end justify-between gap-2 h-64">
                {monthlyRevenue.map((month, index) => {
                  const height = (month.value / maxRevenue) * 100;
                  return (
                    <div key={index} className="flex-1 flex flex-col items-center gap-2">
                      <div className="relative w-full h-full flex items-end">
                        <div
                          className="w-full bg-psipro-primary rounded-t-lg transition-all hover:bg-psipro-primary-dark"
                          style={{ height: `${height}%`, minHeight: "20px" }}
                          title={`${month.month}: R$ ${month.value.toLocaleString("pt-BR")}`}
                        />
                      </div>
                      <div className="text-xs text-psipro-text-secondary font-medium">
                        {month.month}
                      </div>
                      <div className="text-xs text-psipro-text-muted">
                        R$ {(month.value / 1000).toFixed(1)}k
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          </div>
        </div>

        {/* Top 5 pacientes */}
        <div>
          <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm">
            <div className="p-6 border-b border-psipro-divider">
              <h2 className="text-lg font-semibold text-psipro-text">Top 5 por Receita</h2>
              <p className="text-xs text-psipro-text-muted mt-1">Este mês</p>
            </div>
            <div className="p-6">
              <div className="space-y-3">
                {topPatients.map((patient, index) => {
                  const maxRevenue = Math.max(...topPatients.map((p) => p.revenue));
                  const width = (patient.revenue / maxRevenue) * 100;
                  return (
                    <div key={index}>
                      <div className="flex items-center justify-between mb-1">
                        <span className="text-sm font-medium text-psipro-text truncate flex-1">
                          {patient.name}
                        </span>
                        <span className="text-sm font-semibold text-psipro-primary ml-2">
                          R$ {patient.revenue.toLocaleString("pt-BR")}
                        </span>
                      </div>
                      <div className="w-full bg-psipro-surface rounded-full h-2 overflow-hidden">
                        <div
                          className="bg-psipro-primary h-full rounded-full transition-all"
                          style={{ width: `${width}%` }}
                        />
                      </div>
                      <div className="text-xs text-psipro-text-muted mt-1">
                        {patient.sessions} sessões
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Grid secundário */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
        {/* Alertas financeiros */}
        <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm">
          <div className="p-6 border-b border-psipro-divider">
            <h2 className="text-lg font-semibold text-psipro-text">Alertas Financeiros</h2>
          </div>
          <div className="p-6">
            <div className="space-y-4">
              {financialAlerts.map((alert, index) => {
                const bgColor =
                  alert.type === "warning"
                    ? "bg-psipro-warning/10 border-psipro-warning/30"
                    : alert.type === "success"
                      ? "bg-psipro-success/10 border-psipro-success/30"
                      : "bg-psipro-primary/10 border-psipro-primary/30";
                const textColor =
                  alert.type === "warning"
                    ? "text-psipro-warning"
                    : alert.type === "success"
                      ? "text-psipro-success"
                      : "text-psipro-primary";

                return (
                  <div key={index} className={`p-4 rounded-lg border ${bgColor}`}>
                    <p className={`text-sm font-medium ${textColor} mb-1`}>{alert.message}</p>
                    <p className="text-xs text-psipro-text-secondary mb-2">{alert.details}</p>
                    <button className="text-xs font-medium text-psipro-primary hover:text-psipro-primary-dark transition-colors">
                      {alert.action} →
                    </button>
                  </div>
                );
              })}
            </div>
          </div>
        </div>

        <InsightSection insights={financialInsights} maxItems={3} showDisclaimer={false} />
      </div>

      {/* Insights do PsiPro */}
      <div className="mb-8">
        <InsightSection insights={financialInsights} maxItems={3} />
      </div>

      {/* CTA de orientação */}
      <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm p-6">
        <div className="flex items-center gap-3">
          <div className="text-2xl opacity-60">📱</div>
          <div className="flex-1">
            <p className="text-sm text-psipro-text-secondary">
              <strong>O controle individual de pagamentos é feito no app PsiPro.</strong> Esta
              página é para análise financeira consolidada, planejamento e tomada de decisão.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
