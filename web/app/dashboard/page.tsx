"use client";

import { useEffect, useMemo, useState } from "react";
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
import { dashboardService } from "../services/dashboardService";
import Skeleton from "../components/Skeleton";
import { useToast } from "../contexts/ToastContext";

export default function DashboardPage() {
  const { currentClinic, isIndependent } = useClinic();
  const { openOnboarding } = useOnboarding();
  const { showError } = useToast();
  
  const [loading, setLoading] = useState(true);
  const [metrics, setMetrics] = useState({
    activePatients: 0,
    sessionsThisMonth: 0,
    sessionsThisWeek: 0,
    monthlyRevenue: 0,
    pendingRevenue: 0,
  });
  const [agenda, setAgenda] = useState({
    totalSessionsThisWeek: 0,
    busiestDays: [] as string[],
    emptiestDays: [] as string[],
    isEmpty: true,
  });
  const [financial, setFinancial] = useState({
    monthlyRevenue: 0,
    averagePerSession: 0,
    unpaidSessions: 0,
    isEmpty: true,
  });

  // Carregar dados do dashboard
  useEffect(() => {
    loadDashboardData();
  }, [currentClinic?.id]);

  const loadDashboardData = async () => {
    setLoading(true);
    try {
      const [metricsData, agendaData, financialData] = await Promise.all([
        dashboardService.getMetrics(),
        dashboardService.getAgendaSummary(),
        dashboardService.getFinanceSummary(),
      ]);

      setMetrics(metricsData);
      setAgenda(agendaData);
      setFinancial(financialData);
    } catch (error) {
      showError("Erro ao carregar dados do dashboard");
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

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

  // Gerar insights a partir dos dados reais
  const insights = useMemo(() => {
    // Usar dados reais do dashboard para gerar insights
    const insightData: InsightInput = {
      sessionsThisMonth: metrics.sessionsThisMonth,
      sessionsLastMonth: 0, // TODO: Calcular se necessário
      sessionsThisWeek: metrics.sessionsThisWeek,
      scheduledSessionsNextWeek: 0, // TODO: Calcular se necessário
      missedSessions: 0, // TODO: Calcular se necessário
      cancelledSessions: 0, // TODO: Calcular se necessário
      monthlyRevenue: metrics.monthlyRevenue,
      lastMonthRevenue: 0, // TODO: Calcular se necessário
      averageRevenuePerSession: financial.averagePerSession,
      unpaidSessions: financial.unpaidSessions,
      totalPendingRevenue: metrics.pendingRevenue,
      activePatients: metrics.activePatients,
      newPatientsThisMonth: 0, // TODO: Calcular se necessário
      patientsWithoutSessions: 0, // TODO: Calcular se necessário
      patientsWithManySessions: 0, // TODO: Calcular se necessário
    };
    
    const generatedInsights = generateInsights(insightData);
    
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
  }, [metrics, financial]);

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat("pt-BR", {
      style: "currency",
      currency: "BRL",
    }).format(value);
  };

  return (
    <>
      <OnboardingModal />
      <div className="w-full min-w-0 py-6 sm:py-8">
      {/* Cabeçalho do Dashboard */}
      <div className="mb-6 sm:mb-8">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between mb-4">
          <div className="min-w-0">
            <h1 className="text-2xl sm:text-3xl font-bold text-psipro-text mb-2 tracking-tight">
              Dashboard
            </h1>
            <p className="text-psipro-text-secondary text-base sm:text-lg">
              Visão geral da sua clínica
            </p>
          </div>
          {currentClinic && (
            <div className="text-left sm:text-right shrink-0">
              <p className="text-sm text-psipro-text-secondary mb-1 truncate">
                {currentClinic.name}
              </p>
              <RoleBadge role={currentClinic.role || ""} />
            </div>
          )}
          {isIndependent && (
            <div className="text-left sm:text-right shrink-0">
              <p className="text-sm text-psipro-text-secondary">Modo Independente</p>
            </div>
          )}
        </div>
      </div>

      {/* Cards de Métricas (KPIs) */}
      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-5 mb-8">
          {[1, 2, 3, 4, 5].map((i) => (
            <Skeleton key={i} className="h-32" />
          ))}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-5 mb-8">
          <MetricCard
            title="Pacientes ativos"
            value={metrics.activePatients}
            icon="👥"
          />
          <MetricCard
            title="Sessões realizadas"
            value={metrics.sessionsThisMonth}
            icon="📅"
            subtitle="Este mês"
          />
          <MetricCard
            title="Sessões agendadas"
            value={metrics.sessionsThisWeek}
            icon="📋"
            subtitle="Esta semana"
          />
          <MetricCard
            title="Receita do mês"
            value={formatCurrency(metrics.monthlyRevenue)}
            icon="💰"
          />
          <MetricCard
            title="Valores a receber"
            value={formatCurrency(metrics.pendingRevenue)}
            icon="⏳"
          />
        </div>
      )}

      {/* Grid de Conteúdo Principal */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
        {/* Bloco - Agenda (Visão Estratégica) */}
        {loading ? (
          <SectionCard title="Agenda da clínica" isEmpty={false}>
            <Skeleton className="h-48" />
          </SectionCard>
        ) : (
          <SectionCard
            title="Agenda da clínica"
            isEmpty={agenda.isEmpty}
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
                      {agenda.totalSessionsThisWeek}
                    </span>
                  </div>
                  {agenda.busiestDays.length > 0 && (
                    <div className="p-3 bg-psipro-surface rounded-lg">
                      <p className="text-xs text-psipro-text-secondary mb-1">
                        Dias mais cheios
                      </p>
                      <p className="text-sm text-psipro-text">
                        {agenda.busiestDays.join(", ")}
                      </p>
                    </div>
                  )}
                  {agenda.emptiestDays.length > 0 && (
                    <div className="p-3 bg-psipro-surface rounded-lg">
                      <p className="text-xs text-psipro-text-secondary mb-1">
                        Dias mais vazios
                      </p>
                      <p className="text-sm text-psipro-text">
                        {agenda.emptiestDays.join(", ")}
                      </p>
                    </div>
                  )}
                </div>
              </div>
            </div>
          </SectionCard>
        )}

        {/* Bloco - Financeiro (Visão Executiva) */}
        {loading ? (
          <SectionCard title="Resumo financeiro" isEmpty={false}>
            <Skeleton className="h-48" />
          </SectionCard>
        ) : (
          <SectionCard
            title="Resumo financeiro"
            isEmpty={financial.isEmpty}
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
                      {formatCurrency(financial.monthlyRevenue)}
                    </p>
                  </div>
                </div>
                <div className="flex items-center justify-between p-4 bg-psipro-surface rounded-lg border border-psipro-border">
                  <div>
                    <p className="text-sm text-psipro-text-secondary mb-1">
                      Receita média por sessão
                    </p>
                    <p className="text-xl font-semibold text-psipro-text">
                      {formatCurrency(financial.averagePerSession)}
                    </p>
                  </div>
                </div>
                <div className="flex items-center justify-between p-4 bg-psipro-surface rounded-lg border border-psipro-border">
                  <div>
                    <p className="text-sm text-psipro-text-secondary mb-1">
                      Sessões não pagas
                    </p>
                    <p className="text-xl font-semibold text-psipro-text">
                      {financial.unpaidSessions}
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </SectionCard>
        )}
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
