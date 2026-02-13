"use client";

import { useState } from "react";
import InsightSection from "@/app/components/InsightSection";
import { Insight } from "@/app/components/InsightCard";

export default function AgendaPage() {
  const [viewMode, setViewMode] = useState<"week" | "month">("week");
  const [selectedDay, setSelectedDay] = useState<string | null>(null);

  // Dados mockados - KPIs
  const kpiCards = [
    {
      title: "Sessões hoje",
      value: "3",
      subtitle: "2 realizadas, 1 agendada",
      icon: "📅",
      color: "text-psipro-primary",
    },
    {
      title: "Sessões esta semana",
      value: "12",
      subtitle: "8 realizadas",
      icon: "📆",
      color: "text-psipro-primary",
    },
    {
      title: "Sessões este mês",
      value: "48",
      subtitle: "Março 2025",
      icon: "🗓️",
      color: "text-psipro-primary",
    },
    {
      title: "Horários livres esta semana",
      value: "8",
      subtitle: "Disponíveis para agendamento",
      icon: "⏰",
      color: "text-psipro-warning",
    },
    {
      title: "Taxa de comparecimento",
      value: "87%",
      subtitle: "Últimos 30 dias",
      icon: "✅",
      color: "text-psipro-success",
    },
  ];

  // Dados mockados - Insights
  const agendaInsights: Insight[] = [
    {
      id: "agenda-1",
      type: "agenda",
      message: "Você tem muitos horários vagos às quartas-feiras.",
    },
    {
      id: "agenda-2",
      type: "agenda",
      message: "Segundas-feiras concentram 40% dos atendimentos.",
    },
    {
      id: "agenda-3",
      type: "clinico",
      message: "Taxa de comparecimento acima da média este mês.",
    },
  ];

  // Dados mockados - Calendário semanal
  const weekDays = [
    { date: "2025-03-24", day: "Seg", sessions: 5, status: "busy" },
    { date: "2025-03-25", day: "Ter", sessions: 3, status: "normal" },
    { date: "2025-03-26", day: "Qua", sessions: 1, status: "free" },
    { date: "2025-03-27", day: "Qui", sessions: 4, status: "normal" },
    { date: "2025-03-28", day: "Sex", sessions: 2, status: "normal" },
    { date: "2025-03-29", day: "Sáb", sessions: 0, status: "free" },
    { date: "2025-03-30", day: "Dom", sessions: 0, status: "free" },
  ];

  // Dados mockados - Resumo do dia selecionado
  const getDaySummary = (date: string) => {
    const day = weekDays.find((d) => d.date === date);
    if (!day) return null;

    const patients = [
      "Maria Silva Santos",
      "João Pedro Oliveira",
      "Ana Carolina Costa",
    ].slice(0, day.sessions);

    let statusLabel = "Normal";
    let statusColor = "text-psipro-primary";
    if (day.status === "busy") {
      statusLabel = "Sobrecarregado";
      statusColor = "text-psipro-warning";
    } else if (day.status === "free") {
      statusLabel = "Ocioso";
      statusColor = "text-psipro-text-muted";
    }

    return {
      date: day.date,
      day: day.day,
      sessions: day.sessions,
      patients,
      status: statusLabel,
      statusColor,
    };
  };

  const daySummary = selectedDay ? getDaySummary(selectedDay) : null;

  return (
    <div>
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-psipro-text mb-2 tracking-tight">Agenda</h1>
        <p className="text-psipro-text-secondary text-lg">
          Visão geral e planejamento dos atendimentos
        </p>
      </div>

      {/* Cards de KPIs */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-5 mb-8">
        {kpiCards.map((card, index) => (
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
        {/* Calendário */}
        <div className="lg:col-span-2">
          <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm">
            <div className="p-6 border-b border-psipro-divider flex items-center justify-between">
              <h2 className="text-lg font-semibold text-psipro-text">Visão da Agenda</h2>
              <div className="flex gap-2">
                <button
                  onClick={() => setViewMode("week")}
                  className={`px-3 py-1.5 text-xs font-medium rounded-lg transition-all ${
                    viewMode === "week"
                      ? "bg-psipro-primary text-psipro-text"
                      : "bg-psipro-surface text-psipro-text-secondary border border-psipro-border hover:bg-psipro-surface-elevated"
                  }`}
                >
                  Semanal
                </button>
                <button
                  onClick={() => setViewMode("month")}
                  className={`px-3 py-1.5 text-xs font-medium rounded-lg transition-all ${
                    viewMode === "month"
                      ? "bg-psipro-primary text-psipro-text"
                      : "bg-psipro-surface text-psipro-text-secondary border border-psipro-border hover:bg-psipro-surface-elevated"
                  }`}
                >
                  Mensal
                </button>
              </div>
            </div>
            <div className="p-6">
              {viewMode === "week" ? (
                <div className="grid grid-cols-7 gap-3">
                  {weekDays.map((day) => {
                    const isSelected = selectedDay === day.date;
                    const bgColor =
                      day.status === "busy"
                        ? "bg-psipro-warning/20 border-psipro-warning/50"
                        : day.status === "free"
                          ? "bg-psipro-text-muted/10 border-psipro-text-muted/30"
                          : "bg-psipro-surface border-psipro-border";
                    const textColor =
                      day.status === "busy"
                        ? "text-psipro-warning"
                        : day.status === "free"
                          ? "text-psipro-text-muted"
                          : "text-psipro-text";

                    return (
                      <button
                        key={day.date}
                        onClick={() => setSelectedDay(day.date)}
                        className={`p-4 rounded-lg border-2 transition-all text-left ${
                          isSelected
                            ? "border-psipro-primary bg-psipro-primary/10"
                            : `${bgColor} hover:border-psipro-primary/30`
                        }`}
                      >
                        <div className="text-xs font-medium text-psipro-text-secondary mb-1">
                          {day.day}
                        </div>
                        <div className="text-lg font-bold mb-2">{day.date.split("-")[2]}</div>
                        <div className={`text-sm font-semibold ${textColor}`}>
                          {day.sessions} {day.sessions === 1 ? "sessão" : "sessões"}
                        </div>
                      </button>
                    );
                  })}
                </div>
              ) : (
                <div className="text-center py-12">
                  <div className="text-5xl mb-4 opacity-40">📅</div>
                  <p className="text-psipro-text-secondary text-sm mb-1">
                    Visão mensal em desenvolvimento
                  </p>
                  <p className="text-psipro-text-muted text-xs">
                    Use a visão semanal para análise detalhada.
                  </p>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Resumo do dia selecionado ou Insights */}
        <div>
          {daySummary ? (
            <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm">
              <div className="p-6 border-b border-psipro-divider flex items-center justify-between">
                <h2 className="text-lg font-semibold text-psipro-text">Resumo do Dia</h2>
                <button
                  onClick={() => setSelectedDay(null)}
                  className="text-psipro-text-muted hover:text-psipro-text transition-colors"
                >
                  ✕
                </button>
              </div>
              <div className="p-6">
                <div className="mb-4">
                  <div className="text-sm text-psipro-text-secondary mb-1">
                    {daySummary.day}, {daySummary.date.split("-")[2]}/{daySummary.date.split("-")[1]}
                  </div>
                  <div className="text-2xl font-bold text-psipro-text mb-2">
                    {daySummary.sessions} {daySummary.sessions === 1 ? "sessão" : "sessões"}
                  </div>
                  <div className={`text-sm font-medium ${daySummary.statusColor}`}>
                    Status: {daySummary.status}
                  </div>
                </div>
                {daySummary.patients.length > 0 && (
                  <div className="mt-4">
                    <div className="text-xs font-medium text-psipro-text-secondary mb-2 uppercase">
                      Pacientes do dia
                    </div>
                    <div className="space-y-2">
                      {daySummary.patients.map((patient, index) => (
                        <div
                          key={index}
                          className="p-2 bg-psipro-surface rounded border border-psipro-border text-sm text-psipro-text-secondary"
                        >
                          {patient}
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            </div>
          ) : (
            <InsightSection insights={agendaInsights} maxItems={3} showDisclaimer={false} />
          )}
        </div>
      </div>

      {/* Insights do PsiPro */}
      <div className="mb-8">
        <InsightSection insights={agendaInsights} maxItems={3} />
      </div>

      {/* CTA de orientação */}
      <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm p-6">
        <div className="flex items-center gap-3">
          <div className="text-2xl opacity-60">📱</div>
          <div className="flex-1">
            <p className="text-sm text-psipro-text-secondary">
              <strong>Para criar ou gerenciar consultas, utilize o app PsiPro no celular.</strong>{" "}
              Esta página é para visualização, planejamento e análise da sua agenda.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
