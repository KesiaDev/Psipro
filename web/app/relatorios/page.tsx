"use client";

import { useState, useEffect } from "react";
import { useClinic } from "../contexts/ClinicContext";
import { reportsService, type ReportsData, type ReportsSummary } from "../services/reportsService";
import Skeleton from "../components/Skeleton";

function formatCurrency(value: number): string {
  return new Intl.NumberFormat("pt-BR", {
    style: "currency",
    currency: "BRL",
  }).format(value);
}

export default function RelatoriosPage() {
  const { currentClinic, isIndependent } = useClinic();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [data, setData] = useState<ReportsData | null>(null);
  const [summary, setSummary] = useState<ReportsSummary | null>(null);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);
    Promise.all([reportsService.findAll(), reportsService.getSummary()])
      .then(([reports, sum]) => {
        if (!cancelled) {
          setData(reports);
          setSummary(sum);
        }
      })
      .catch((e) => {
        if (!cancelled) {
          setError(
            e && typeof e === "object" && "message" in e
              ? String((e as { message: string }).message)
              : "Erro ao carregar relatórios"
          );
        }
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [currentClinic?.id]);

  if (loading) {
    return (
      <div className="space-y-6">
        <Skeleton className="h-10 w-48" />
        <Skeleton className="h-64" />
        <Skeleton className="h-48" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-psipro-error/10 border border-psipro-error/20 text-psipro-error rounded-lg p-6">
        <p className="font-medium">{error}</p>
      </div>
    );
  }

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-2xl font-bold text-psipro-text mb-1">Relatórios</h1>
        <p className="text-psipro-text-secondary text-sm">
          Visão consolidada de sessões, receita e pacientes dos últimos 6 meses
        </p>
      </div>

      {/* Resumo */}
      {summary && (
        <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
          <div className="bg-psipro-surface-elevated border border-psipro-border rounded-lg p-4">
            <p className="text-xs text-psipro-text-muted uppercase tracking-wide">Pacientes</p>
            <p className="text-xl font-bold text-psipro-text mt-1">{summary.totalPatients}</p>
          </div>
          <div className="bg-psipro-surface-elevated border border-psipro-border rounded-lg p-4">
            <p className="text-xs text-psipro-text-muted uppercase tracking-wide">Sessões</p>
            <p className="text-xl font-bold text-psipro-text mt-1">{summary.totalSessions}</p>
          </div>
          <div className="bg-psipro-surface-elevated border border-psipro-border rounded-lg p-4">
            <p className="text-xs text-psipro-text-muted uppercase tracking-wide">Receita</p>
            <p className="text-xl font-bold text-psipro-success mt-1">
              {formatCurrency(summary.totalRevenue)}
            </p>
          </div>
          <div className="bg-psipro-surface-elevated border border-psipro-border rounded-lg p-4">
            <p className="text-xs text-psipro-text-muted uppercase tracking-wide">Despesas</p>
            <p className="text-xl font-bold text-psipro-error mt-1">
              {formatCurrency(summary.totalExpenses)}
            </p>
          </div>
          <div className="bg-psipro-surface-elevated border border-psipro-border rounded-lg p-4">
            <p className="text-xs text-psipro-text-muted uppercase tracking-wide">Hoje</p>
            <p className="text-xl font-bold text-psipro-primary mt-1">{summary.todaySessions}</p>
          </div>
        </div>
      )}

      {/* Sessões por mês */}
      {data?.monthlySessions && data.monthlySessions.length > 0 && (
        <div className="bg-psipro-surface-elevated border border-psipro-border rounded-lg p-6">
          <h2 className="text-lg font-semibold text-psipro-text mb-4">Sessões por mês</h2>
          <div className="flex items-end gap-2 h-40">
            {data.monthlySessions.map((m, i) => {
              const max = Math.max(...data.monthlySessions.map((x) => x.sessions), 1);
              const h = (m.sessions / max) * 100;
              return (
                <div key={i} className="flex-1 flex flex-col items-center gap-2">
                  <div
                    className="w-full bg-psipro-primary/60 rounded-t min-h-[4px] transition-all"
                    style={{ height: `${Math.max(h, 4)}%` }}
                  />
                  <span className="text-xs text-psipro-text-muted">{m.month}</span>
                  <span className="text-xs font-medium text-psipro-text">{m.sessions}</span>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* Receita por mês */}
      {data?.revenue && data.revenue.length > 0 && (
        <div className="bg-psipro-surface-elevated border border-psipro-border rounded-lg p-6">
          <h2 className="text-lg font-semibold text-psipro-text mb-4">Receita por mês</h2>
          <div className="space-y-2">
            {data.revenue.map((r, i) => (
              <div key={i} className="flex justify-between items-center">
                <span className="text-psipro-text-secondary">{r.month}</span>
                <span className="font-semibold text-psipro-success">{formatCurrency(r.value)}</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Top pacientes */}
      {data?.topPatients && data.topPatients.length > 0 && (
        <div className="bg-psipro-surface-elevated border border-psipro-border rounded-lg p-6">
          <h2 className="text-lg font-semibold text-psipro-text mb-4">Pacientes com mais sessões</h2>
          <div className="space-y-3">
            {data.topPatients.slice(0, 10).map((p, i) => (
              <div key={i} className="flex items-center gap-3">
                <span className="text-sm text-psipro-text-muted w-6">{i + 1}.</span>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-psipro-text truncate">{p.name}</p>
                  <div className="h-1.5 bg-psipro-surface rounded-full overflow-hidden mt-1">
                    <div
                      className="h-full bg-psipro-primary/60 rounded-full"
                      style={{ width: `${p.percentage}%` }}
                    />
                  </div>
                </div>
                <span className="text-sm font-semibold text-psipro-text shrink-0">{p.sessions}</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Stats */}
      {data?.stats && (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="bg-psipro-surface-elevated border border-psipro-border rounded-lg p-4">
            <p className="text-sm text-psipro-text-muted">Taxa de retorno</p>
            <p className="text-2xl font-bold text-psipro-text mt-1">{data.stats.returnRate}%</p>
          </div>
          <div className="bg-psipro-surface-elevated border border-psipro-border rounded-lg p-4">
            <p className="text-sm text-psipro-text-muted">Média horas/semana (6 meses)</p>
            <p className="text-2xl font-bold text-psipro-text mt-1">{data.stats.avgHoursPerWeek}h</p>
          </div>
        </div>
      )}
    </div>
  );
}
