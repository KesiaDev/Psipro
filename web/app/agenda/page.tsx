"use client";

import { useState, useEffect } from "react";
import InsightSection from "@/app/components/InsightSection";
import { Insight } from "@/app/components/InsightCard";
import { appointmentService } from "@/app/services/appointmentService";
import { dashboardService } from "@/app/services/dashboardService";
import type { Appointment } from "@/app/services/appointmentService";
import { useClinic } from "@/app/contexts/ClinicContext";
import { useToast } from "@/app/contexts/ToastContext";
import Skeleton from "@/app/components/Skeleton";

const DAY_NAMES = ["Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"];

function getWeekDates(): { date: string; day: string; dayOfWeek: number }[] {
  const today = new Date();
  const dayOfWeek = today.getDay();
  const mondayOffset = dayOfWeek === 0 ? -6 : 1 - dayOfWeek;
  const monday = new Date(today);
  monday.setDate(today.getDate() + mondayOffset);
  monday.setHours(0, 0, 0, 0);

  const result: { date: string; day: string; dayOfWeek: number }[] = [];
  for (let i = 0; i < 7; i++) {
    const d = new Date(monday);
    d.setDate(monday.getDate() + i);
    result.push({
      date: d.toISOString().split("T")[0],
      day: DAY_NAMES[d.getDay()],
      dayOfWeek: d.getDay(),
    });
  }
  return result;
}

export default function AgendaPage() {
  const { currentClinic } = useClinic();
  const { showError } = useToast();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [metrics, setMetrics] = useState({
    activePatients: 0,
    sessionsThisMonth: 0,
    sessionsThisWeek: 0,
    monthlyRevenue: 0,
    pendingRevenue: 0,
  });
  const [agendaSummary, setAgendaSummary] = useState({
    totalSessionsThisWeek: 0,
    busiestDays: [] as string[],
    emptiestDays: [] as string[],
    isEmpty: true,
  });
  const [viewMode, setViewMode] = useState<"week" | "month">("week");
  const [selectedDay, setSelectedDay] = useState<string | null>(null);

  const weekDays = getWeekDates();

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [apts, m, agenda] = await Promise.all([
        appointmentService.getAppointments(),
        dashboardService.getMetrics(),
        dashboardService.getAgendaSummary(),
      ]);
      setAppointments(apts);
      setMetrics(m);
      setAgendaSummary(agenda);
    } catch (err) {
      const msg =
        err && typeof err === "object" && "message" in err
          ? String((err as { message: string }).message)
          : "Erro ao carregar agenda";
      setError(msg);
      showError(msg);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [currentClinic?.id]);

  const appointmentsByDate = appointments.reduce<Record<string, Appointment[]>>(
    (acc, apt) => {
      const d = new Date(apt.scheduledAt).toISOString().split("T")[0];
      if (!acc[d]) acc[d] = [];
      acc[d].push(apt);
      return acc;
    },
    {}
  );

  const daySummary = selectedDay
    ? (() => {
        const apts = appointmentsByDate[selectedDay] || [];
        const dayInfo = weekDays.find((d) => d.date === selectedDay);
        if (!dayInfo) return null;
        const maxInWeek = Math.max(
          ...weekDays.map((d) => (appointmentsByDate[d.date] || []).length),
          1
        );
        const count = apts.length;
        let status = "normal";
        if (count >= maxInWeek && count > 0) status = "busy";
        else if (count === 0) status = "free";
        return {
          date: selectedDay,
          day: dayInfo.day,
          sessions: count,
          patients: apts.map((a) => a.patient?.name || "Paciente"),
          status:
            status === "busy"
              ? "Sobrecarregado"
              : status === "free"
                ? "Ocioso"
                : "Normal",
          statusColor:
            status === "busy"
              ? "text-psipro-warning"
              : status === "free"
                ? "text-psipro-text-muted"
                : "text-psipro-primary",
        };
      })()
    : null;

  const agendaInsights: Insight[] = agendaSummary.isEmpty
    ? [
        {
          id: "agenda-empty",
          type: "agenda",
          message:
            "Nenhuma sessão agendada esta semana. Use o app para agendar.",
        },
      ]
    : agendaSummary.busiestDays.length > 0
      ? [
          {
            id: "agenda-busy",
            type: "agenda",
            message: `Dias mais cheios: ${agendaSummary.busiestDays.join(", ")}.`,
          },
        ]
      : [];

  if (loading) {
    return (
      <div>
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-psipro-text mb-2 tracking-tight">
            Agenda
          </h1>
          <p className="text-psipro-text-secondary text-lg">
            Visão geral e planejamento dos atendimentos
          </p>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-5 mb-8">
          {[1, 2, 3, 4, 5].map((i) => (
            <Skeleton key={i} className="h-32" />
          ))}
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div>
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-psipro-text mb-2 tracking-tight">
            Agenda
          </h1>
          <p className="text-psipro-text-secondary text-lg">
            Visão geral e planejamento dos atendimentos
          </p>
        </div>
        <div className="bg-psipro-error/10 border border-psipro-error/20 text-psipro-error rounded-lg p-6">
          <p className="font-medium mb-2">Não foi possível carregar os dados</p>
          <p className="text-sm mb-4">{error}</p>
          <button
            onClick={loadData}
            className="px-4 py-2 bg-psipro-primary text-white rounded-lg hover:bg-psipro-primary-dark transition-colors font-medium"
          >
            Tentar novamente
          </button>
        </div>
      </div>
    );
  }

  const kpiCards = [
    {
      title: "Sessões realizadas",
      value: String(metrics.sessionsThisMonth),
      subtitle: "Este mês",
      icon: "📅",
      color: "text-psipro-primary",
    },
    {
      title: "Sessões esta semana",
      value: String(agendaSummary.totalSessionsThisWeek),
      subtitle: "Agendadas",
      icon: "📆",
      color: "text-psipro-primary",
    },
    {
      title: "Pacientes ativos",
      value: String(metrics.activePatients),
      subtitle: "Cadastrados",
      icon: "👥",
      color: "text-psipro-primary",
    },
  ];

  return (
    <div>
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-psipro-text mb-2 tracking-tight">
          Agenda
        </h1>
        <p className="text-psipro-text-secondary text-lg">
          Visão geral e planejamento dos atendimentos
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5 mb-8">
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
                <p className={`text-3xl font-bold ${card.color} mb-1`}>
                  {card.value}
                </p>
                <p className="text-xs text-psipro-text-muted">{card.subtitle}</p>
              </div>
              <span className="text-3xl opacity-60">{card.icon}</span>
            </div>
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
        <div className="lg:col-span-2">
          <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm">
            <div className="p-6 border-b border-psipro-divider flex items-center justify-between">
              <h2 className="text-lg font-semibold text-psipro-text">
                Visão da Agenda
              </h2>
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
                    const sessions =
                      appointmentsByDate[day.date] || [];
                    const count = sessions.length;
                    const maxInWeek = Math.max(
                      ...weekDays.map(
                        (d) => (appointmentsByDate[d.date] || []).length
                      ),
                      1
                    );
                    const status =
                      count >= maxInWeek && count > 0
                        ? "busy"
                        : count === 0
                          ? "free"
                          : "normal";
                    const isSelected = selectedDay === day.date;
                    const bgColor =
                      status === "busy"
                        ? "bg-psipro-warning/20 border-psipro-warning/50"
                        : status === "free"
                          ? "bg-psipro-text-muted/10 border-psipro-text-muted/30"
                          : "bg-psipro-surface border-psipro-border";
                    const textColor =
                      status === "busy"
                        ? "text-psipro-warning"
                        : status === "free"
                          ? "text-psipro-text-muted"
                          : "text-psipro-text";

                    return (
                      <button
                        key={day.date}
                        onClick={() =>
                          setSelectedDay(
                            selectedDay === day.date ? null : day.date
                          )
                        }
                        className={`p-4 rounded-lg border-2 transition-all text-left ${
                          isSelected
                            ? "border-psipro-primary bg-psipro-primary/10"
                            : `${bgColor} hover:border-psipro-primary/30`
                        }`}
                      >
                        <div className="text-xs font-medium text-psipro-text-secondary mb-1">
                          {day.day}
                        </div>
                        <div className="text-lg font-bold mb-2">
                          {day.date.split("-")[2]}
                        </div>
                        <div className={`text-sm font-semibold ${textColor}`}>
                          {count}{" "}
                          {count === 1 ? "sessão" : "sessões"}
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

        <div>
          {daySummary ? (
            <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm">
              <div className="p-6 border-b border-psipro-divider flex items-center justify-between">
                <h2 className="text-lg font-semibold text-psipro-text">
                  Resumo do Dia
                </h2>
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
                    {daySummary.day},{" "}
                    {daySummary.date.split("-")[2]}/
                    {daySummary.date.split("-")[1]}
                  </div>
                  <div className="text-2xl font-bold text-psipro-text mb-2">
                    {daySummary.sessions}{" "}
                    {daySummary.sessions === 1 ? "sessão" : "sessões"}
                  </div>
                  <div
                    className={`text-sm font-medium ${daySummary.statusColor}`}
                  >
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
            <InsightSection
              insights={agendaInsights}
              maxItems={3}
              showDisclaimer={false}
            />
          )}
        </div>
      </div>

      <div className="mb-8">
        <InsightSection insights={agendaInsights} maxItems={3} />
      </div>

      <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm p-6">
        <div className="flex items-center gap-3">
          <div className="text-2xl opacity-60">📱</div>
          <div className="flex-1">
            <p className="text-sm text-psipro-text-secondary">
              <strong>
                Para criar ou gerenciar consultas, utilize o app PsiPro no
                celular.
              </strong>{" "}
              Esta página é para visualização, planejamento e análise da sua
              agenda.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
