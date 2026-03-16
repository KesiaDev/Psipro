"use client";

import { useState, useEffect } from "react";
import InsightSection from "@/app/components/InsightSection";
import { Insight } from "@/app/components/InsightCard";
import { dashboardService } from "@/app/services/dashboardService";
import { useToast } from "@/app/contexts/ToastContext";
import Skeleton from "@/app/components/Skeleton";
import { useClinic } from "@/app/contexts/ClinicContext";

function formatCurrency(value: number) {
  return new Intl.NumberFormat("pt-BR", {
    style: "currency",
    currency: "BRL",
  }).format(value);
}

export default function FinanceiroPage() {
  const { currentClinic } = useClinic();
  const { showError } = useToast();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [summary, setSummary] = useState<{
    monthlyRevenue: number;
    averagePerSession: number;
    unpaidSessions: number;
    isEmpty: boolean;
  } | null>(null);

  const loadFinancialData = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await dashboardService.getFinanceSummary();
      setSummary(data);
    } catch (err) {
      const msg =
        err && typeof err === "object" && "message" in err
          ? String((err as { message: string }).message)
          : "Erro ao carregar dados financeiros";
      setError(msg);
      showError(msg);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadFinancialData();
  }, [currentClinic?.id]);

  if (loading) {
    return (
      <div>
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-psipro-text mb-2 tracking-tight">
            Financeiro
          </h1>
          <p className="text-psipro-text-secondary text-lg">
            Visão geral financeira do consultório
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
            Financeiro
          </h1>
          <p className="text-psipro-text-secondary text-lg">
            Visão geral financeira do consultório
          </p>
        </div>
        <div className="bg-psipro-error/10 border border-psipro-error/20 text-psipro-error rounded-lg p-6">
          <p className="font-medium mb-2">Não foi possível carregar os dados</p>
          <p className="text-sm mb-4">{error}</p>
          <button
            onClick={loadFinancialData}
            className="px-4 py-2 bg-psipro-primary text-white rounded-lg hover:bg-psipro-primary-dark transition-colors font-medium"
          >
            Tentar novamente
          </button>
        </div>
      </div>
    );
  }

  if (!summary) return null;

  const financialCards = [
    {
      title: "Receita do mês",
      value: formatCurrency(summary.monthlyRevenue),
      subtitle: "Mês atual",
      icon: "💰",
      color: "text-psipro-primary",
    },
    {
      title: "Ticket médio por sessão",
      value: formatCurrency(summary.averagePerSession),
      subtitle: "Média do mês",
      icon: "📊",
      color: "text-psipro-primary",
    },
    {
      title: "Sessões não pagas",
      value: String(summary.unpaidSessions),
      subtitle: "Pendências",
      icon: "⏳",
      color: "text-psipro-warning",
    },
  ];

  const hasData =
    summary.monthlyRevenue > 0 ||
    summary.averagePerSession > 0 ||
    summary.unpaidSessions > 0;

  const financialInsights: Insight[] = hasData
    ? [
        {
          id: "financeiro-1",
          type: "financeiro",
          message:
            summary.unpaidSessions > 0
              ? `${summary.unpaidSessions} sessão(ões) com pagamento pendente.`
              : "Todos os registros estão em dia.",
        },
      ]
    : [];

  return (
    <div>
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-psipro-text mb-2 tracking-tight">
          Financeiro
        </h1>
        <p className="text-psipro-text-secondary text-lg">
          Visão geral financeira do consultório
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-5 mb-8">
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

      <div className="mb-8">
        <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm p-6">
          <h2 className="text-lg font-semibold text-psipro-text mb-4">
            Resumo consolidado
          </h2>
          <p className="text-sm text-psipro-text-secondary">
            Os dados exibidos vêm diretamente da API. Para detalhamento por
            paciente e registro de pagamentos, utilize o app PsiPro no celular.
          </p>
        </div>
      </div>

      {financialInsights.length > 0 && (
        <div className="mb-8">
          <InsightSection insights={financialInsights} maxItems={3} />
        </div>
      )}

      <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm p-6">
        <div className="flex items-center gap-3">
          <div className="text-2xl opacity-60">📱</div>
          <div className="flex-1">
            <p className="text-sm text-psipro-text-secondary">
              <strong>
                O controle individual de pagamentos é feito no app PsiPro.
              </strong>{" "}
              Esta página mostra a visão consolidada do consultório.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
