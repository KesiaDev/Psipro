/**
 * Interface para provedores de insights
 * 
 * Permite evoluir de regras baseadas (RuleBasedInsightProvider)
 * para IA real (AIInsightProvider) sem quebrar o código existente.
 */

import { Insight, InsightInput } from './types';

export interface InsightProvider {
  /**
   * Gera insights a partir dos dados fornecidos
   */
  generate(input: InsightInput): Promise<Insight[]>;
}

/**
 * Provedor baseado em regras (implementação atual)
 * 
 * Este é o provedor padrão que usa regras lógicas simples.
 * No futuro, pode ser substituído por um AIInsightProvider.
 */
export class RuleBasedInsightProvider implements InsightProvider {
  async generate(input: InsightInput): Promise<Insight[]> {
    // Importação dinâmica para evitar dependências circulares
    const { generateInsights } = await import('./InsightEngine');
    return generateInsights(input);
  }
}

/**
 * Factory para criar o provedor de insights
 * 
 * No futuro, pode retornar AIInsightProvider baseado em configuração
 */
export function createInsightProvider(): InsightProvider {
  return new RuleBasedInsightProvider();
}



