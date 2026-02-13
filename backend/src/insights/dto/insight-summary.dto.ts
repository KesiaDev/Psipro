/**
 * DTO para insights resumidos (consumo pelo App Android)
 * 
 * Retorna no máximo 3 insights priorizados para exibição no app.
 */
export class InsightSummaryDTO {
  id: string;
  title: string;
  description: string;
  priority: number; // 1-10, maior = mais importante
  actionId?: string; // ID da ação relacionada (opcional)
}


