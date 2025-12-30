"use client";

import { useEffect, useMemo } from "react";
import { useClinic } from "../contexts/ClinicContext";
import { useOnboarding } from "../contexts/OnboardingContext";
import { isFirstAccess } from "../utils/onboarding";
import { generateInsights } from "../insights/InsightEngine";
import type { InsightInput } from "../insights/types";
import MetricCard from "../components/dashboard/MetricCard";
import SectionCard from "../components/dashboard/SectionCard";
import InsightCard from "../components/dashboard/InsightCard";
import ActionCard from "../components/dashboard/ActionCard";
import RoleBadge from "../components/RoleBadge";
import OnboardingModal from "../components/onboarding/OnboardingModal";

// Dados mockados estruturados (preparados para API futura)
const MOCK_METRICS = {
  activePatients: 0,
  sessionsThisMonth: 0,
  sessionsThisWeek: 0,
  monthlyRevenue: 0,
  pendingRevenue: 0,
};

const MOCK_AGENDA = {
  totalSessionsThisWeek: 0,
  busiestDays: [] as string[],
  emptiestDays: [] as string[],
  isEmpty: true,
};

const MOCK_FINANCIAL = {
  monthlyRevenue: 0,
  averagePerSession: 0,
  unpaidSessions: 0,
  isEmpty: true,
};

// Dados mockados para o motor de insights
const MOCK_INSIGHT_DATA: InsightInput = {
  sessionsThisMonth: 0,
  sessionsLastMonth: 0,
  sessionsThisWeek: 0,
  scheduledSessionsNextWeek: 0,
  missedSessions: 0,
  cancelledSessions: 0,
  monthlyRevenue: 0,
  lastMonthRevenue: 0,
  averageRevenuePerSession: 0,
  unpaidSessions: 0,
  totalPendingRevenue: 0,
  activePatients: 0,
  newPatientsThisMonth: 0,
  patientsWithoutSessions: 0,
  patientsWithManySessions: 0,
};

export default function DashboardPage() {
  const { currentClinic, isIndependent } = useClinic();
  const { openOnboarding } = useOnboarding();

  // Verificar primeiro acesso e abrir onboarding
  useEffect(() => {
    if (isFirstAccess()) {
      // Pequeno delay para garantir que a página carregou
      const timer = setTimeout(() => {
        openOnboarding();
      }, 500);
      return () => clearTimeout(timer);
    }
  }, [openOnboarding]);

  // Gerar insights a partir dos dados mockados
  const insights = useMemo(() => {
    // TODO: Substituir MOCK_INSIGHT_DATA por dados reais da API
    const generatedInsights = generateInsights(MOCK_INSIGHT_DATA);
    
    // Priorizar: warning → tip → success → info
    // Limitar a 3 insights para não sobrecarregar
    const prioritized = generatedInsights
      .sort((a, b) => {
        const priorityOrder: Record<string, number> = {
          warning: 4,
          tip: 3,
          success: 2,
          info: 1,
        };
        return priorityOrder[b.type] - priorityOrder[a.type];
      })
      .slice(0, 3);

    // Se não houver insights gerados, mostrar mensagem padrão
    if (prioritized.length === 0) {
      return [
        {
          id: 'default-tip',
          type: 'tip' as const,
          category: 'geral' as const,
          title: 'Começando a usar o PsiPro',
          description: 'Conforme você usar o app e registrar sessões, insights personalizados aparecerão aqui automaticamente.',
          priority: 1,
        },
      ];
    }

    return prioritized;
  }, []);

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat("pt-BR", {
      style: "currency",
      currency: "BRL",
    }).format(value);
  };

  return (
    <>
      <OnboardingModal />
      <div className="max-w-7xl mx-auto px-4 py-8">
      {/* Cabeçalho do Dashboard */}
      <div className="mb-8">
        <div className="flex items-start justify-between mb-4">
          <div>
            <h1 className="text-3xl font-bold text-psipro-text mb-2 tracking-tight">
              Dashboard
            </h1>
            <p className="text-psipro-text-secondary text-lg">
              Visão geral da sua clínica
            </p>
          </div>
          {currentClinic && (
            <div className="text-right">
              <p className="text-sm text-psipro-text-secondary mb-1">
                {currentClinic.name}
              </p>
              <RoleBadge role={currentClinic.role || ""} />
            </div>
          )}
          {isIndependent && (
            <div className="text-right">
              <p className="text-sm text-psipro-text-secondary">Modo Independente</p>
            </div>
          )}
        </div>
      </div>

      {/* Cards de Métricas (KPIs) */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-5 mb-8">
        <MetricCard
          title="Pacientes ativos"
          value={MOCK_METRICS.activePatients}
          icon="👥"
        />
        <MetricCard
          title="Sessões realizadas"
          value={MOCK_METRICS.sessionsThisMonth}
          icon="📅"
          subtitle="Este mês"
        />
        <MetricCard
          title="Sessões agendadas"
          value={MOCK_METRICS.sessionsThisWeek}
          icon="📋"
          subtitle="Esta semana"
        />
        <MetricCard
          title="Receita do mês"
          value={formatCurrency(MOCK_METRICS.monthlyRevenue)}
          icon="💰"
        />
        <MetricCard
          title="Valores a receber"
          value={formatCurrency(MOCK_METRICS.pendingRevenue)}
          icon="⏳"
        />
      </div>

      {/* Grid de Conteúdo Principal */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
        {/* Bloco - Agenda (Visão Estratégica) */}
        <SectionCard
          title="Agenda da clínica"
          isEmpty={MOCK_AGENDA.isEmpty}
          emptyState={{
            icon: "📅",
            title: "Nenhuma sessão agendada ainda",
            description:
              "Quando você começar a usar a agenda no app, a visão geral aparecerá aqui automaticamente.",
            hint: "A criação e confirmação de sessões é feita no app PsiPro.",
          }}
        >
          <div className="space-y-4">
            <div>
              <p className="text-sm text-psipro-text-secondary mb-2">
                Resumo da semana
              </p>
              <div className="space-y-2">
                <div className="flex items-center justify-between p-3 bg-psipro-surface rounded-lg">
                  <span className="text-sm text-psipro-text-secondary">
                    Total de sessões
                  </span>
                  <span className="text-base font-semibold text-psipro-text">
                    {MOCK_AGENDA.totalSessionsThisWeek}
                  </span>
                </div>
                {MOCK_AGENDA.busiestDays.length > 0 && (
                  <div className="p-3 bg-psipro-surface rounded-lg">
                    <p className="text-xs text-psipro-text-secondary mb-1">
                      Dias mais cheios
                    </p>
                    <p className="text-sm text-psipro-text">
                      {MOCK_AGENDA.busiestDays.join(", ")}
                    </p>
                  </div>
                )}
                {MOCK_AGENDA.emptiestDays.length > 0 && (
                  <div className="p-3 bg-psipro-surface rounded-lg">
                    <p className="text-xs text-psipro-text-secondary mb-1">
                      Dias mais vazios
                    </p>
                    <p className="text-sm text-psipro-text">
                      {MOCK_AGENDA.emptiestDays.join(", ")}
                    </p>
                  </div>
                )}
              </div>
            </div>
          </div>
        </SectionCard>

        {/* Bloco - Financeiro (Visão Executiva) */}
        <SectionCard
          title="Resumo financeiro"
          isEmpty={MOCK_FINANCIAL.isEmpty}
          emptyState={{
            icon: "💰",
            title: "Aguardando dados financeiros",
            description:
              "Os dados financeiros são gerados a partir das sessões realizadas no app.",
            hint: "A web é usada para acompanhar e analisar.",
          }}
        >
          <div className="space-y-4">
            <div className="space-y-3">
              <div className="flex items-center justify-between p-4 bg-psipro-surface rounded-lg border border-psipro-border">
                <div>
                  <p className="text-sm text-psipro-text-secondary mb-1">
                    Receita total do mês
                  </p>
                  <p className="text-2xl font-bold text-psipro-text">
                    {formatCurrency(MOCK_FINANCIAL.monthlyRevenue)}
                  </p>
                </div>
              </div>
              <div className="flex items-center justify-between p-4 bg-psipro-surface rounded-lg border border-psipro-border">
                <div>
                  <p className="text-sm text-psipro-text-secondary mb-1">
                    Receita média por sessão
                  </p>
                  <p className="text-xl font-semibold text-psipro-text">
                    {formatCurrency(MOCK_FINANCIAL.averagePerSession)}
                  </p>
                </div>
              </div>
              <div className="flex items-center justify-between p-4 bg-psipro-surface rounded-lg border border-psipro-border">
                <div>
                  <p className="text-sm text-psipro-text-secondary mb-1">
                    Sessões não pagas
                  </p>
                  <p className="text-xl font-semibold text-psipro-text">
                    {MOCK_FINANCIAL.unpaidSessions}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </SectionCard>
      </div>

      {/* Bloco - Alertas e Insights */}
      <div className="mb-8">
        <div className="mb-4">
          <h2 className="text-xl font-semibold text-psipro-text mb-2">
            Insights do PsiPro
          </h2>
          <p className="text-sm text-psipro-text-secondary">
            Observações automáticas sobre sua prática clínica
          </p>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {insights.map((insight) => (
            <div key={insight.id} className="bg-psipro-surface-elevated rounded-lg border border-psipro-border p-4">
              <div className="flex items-start gap-3">
                <span className="text-xl flex-shrink-0">
                  {insight.type === 'warning' ? '⚠️' : 
                   insight.type === 'success' ? '✅' : 
                   insight.type === 'tip' ? '💡' : 'ℹ️'}
                </span>
                <div className="flex-1">
                  <h3 className="font-semibold text-psipro-text mb-1 text-sm">
                    {insight.title}
                  </h3>
                  <p className="text-sm text-psipro-text-secondary leading-relaxed">
                    {insight.description}
                  </p>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Bloco - Ações Recomendadas */}
      <ActionCard
        title="O que fazer agora"
        description="Ações sugeridas para começar a usar o PsiPro"
        actions={[
          {
            label: "Importar pacientes",
            href: "/pacientes",
            variant: "primary",
          },
          {
            label: "Gerenciar clínica",
            href: "/clinica",
            variant: "secondary",
          },
        ]}
      />
      </div>
    </>
  );
}
