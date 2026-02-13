/**
 * Tipos e interfaces para o sistema de insights do PsiPro
 * 
 * Insights são observações inteligentes sobre dados operacionais e administrativos.
 * NUNCA incluem diagnóstico clínico ou recomendações terapêuticas.
 */

export type InsightType = 'info' | 'warning' | 'success' | 'tip';

export type InsightCategory = 'agenda' | 'financeiro' | 'pacientes' | 'geral';

export interface Insight {
  id: string;
  type: InsightType;
  category: InsightCategory;
  title: string;
  description: string;
  priority: number; // 1-10, maior = mais importante
  relatedTo?: string; // ID do recurso relacionado (opcional)
}

export interface InsightInput {
  // Dados de agenda
  sessionsThisMonth: number;
  sessionsLastMonth: number;
  sessionsThisWeek: number;
  scheduledSessionsNextWeek: number;
  missedSessions: number;
  cancelledSessions: number;
  busiestDay?: string;
  emptiestDay?: string;
  
  // Dados financeiros
  monthlyRevenue: number;
  lastMonthRevenue: number;
  averageRevenuePerSession: number;
  unpaidSessions: number;
  totalPendingRevenue: number;
  
  // Dados de pacientes
  activePatients: number;
  newPatientsThisMonth: number;
  patientsWithoutSessions: number;
  patientsWithManySessions: number;
  
  // Configurações
  averageSessionsPerWeek?: number;
  targetMonthlyRevenue?: number;
}



