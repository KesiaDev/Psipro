# 🧠 Sistema de Insights do PsiPro

## 📋 Visão Geral

O sistema de insights do PsiPro gera observações inteligentes sobre dados operacionais e administrativos, ajudando psicólogos a entender melhor sua prática clínica.

## ⚠️ Princípios de Segurança

- ❌ **NUNCA** diagnostica ou sugere condutas clínicas
- ❌ **NUNCA** analisa conteúdo de prontuário
- ❌ **NUNCA** interpreta texto sensível de pacientes
- ✅ **APENAS** dados operacionais e administrativos
- ✅ **SEMPRE** observações, nunca ordens
- ✅ **SEMPRE** linguagem cuidadosa e não invasiva

## 📁 Estrutura

```
app/insights/
├── types.ts              # Tipos e interfaces
├── InsightEngine.ts      # Motor de insights (regras)
├── InsightProvider.ts    # Interface para provedores
└── README.md            # Esta documentação
```

## 🔧 Como Funciona

### 1. Motor de Insights (`InsightEngine.ts`)

O motor analisa dados estruturados e gera insights baseados em regras:

- **Agenda**: Concentração de sessões, faltas, cancelamentos
- **Financeiro**: Receita, valores pendentes, tendências
- **Pacientes**: Base ativa, pacientes sem sessões (apenas administrativo)

### 2. Tipos de Insights

- `warning` - Requer atenção (ex: muitas faltas, receita baixa)
- `tip` - Dica útil (ex: poucas sessões agendadas)
- `success` - Boa notícia (ex: crescimento, receita alta)
- `info` - Informação geral (ex: distribuição da agenda)

### 3. Priorização

Insights são ordenados por prioridade (1-10):
- Maior prioridade = mais importante
- Dashboard mostra até 3 insights por vez
- Prioriza: warning → tip → success → info

## 💻 Uso no Dashboard

```typescript
import { generateInsights } from '@/app/insights/InsightEngine';
import type { InsightInput } from '@/app/insights/types';

// Preparar dados
const input: InsightInput = {
  sessionsThisMonth: 20,
  sessionsLastMonth: 15,
  monthlyRevenue: 5000,
  // ... outros dados
};

// Gerar insights
const insights = generateInsights(input);

// Renderizar (já implementado no dashboard)
```

## 🚀 Evolução Futura (IA Real)

O sistema está preparado para evoluir para IA real:

1. **Interface `InsightProvider`** - Permite trocar o provedor
2. **`RuleBasedInsightProvider`** - Implementação atual (regras)
3. **`AIInsightProvider`** - Futuro (OpenAI, Azure, etc.)

### Como adicionar IA real:

```typescript
// app/insights/AIInsightProvider.ts
export class AIInsightProvider implements InsightProvider {
  async generate(input: InsightInput): Promise<Insight[]> {
    // Chamar API de IA
    // Processar resposta
    // Retornar insights
  }
}

// Trocar no InsightProvider.ts
export function createInsightProvider(): InsightProvider {
  if (USE_AI) {
    return new AIInsightProvider();
  }
  return new RuleBasedInsightProvider();
}
```

## 📊 Exemplos de Insights

### Agenda
- "Você teve 5 faltas este mês (25% das sessões). Pode ser útil observar se há algum padrão."
- "Você tem 3 sessões agendadas para a próxima semana. Pode ser uma boa oportunidade para organizar sua agenda."

### Financeiro
- "Sua receita deste mês está 20% abaixo do mês anterior. Pode ser útil verificar se há padrões que explicam essa diferença."
- "Você tem 8 sessões ainda não pagas. Pode ser útil acompanhar esses valores."

### Pacientes
- "5 pacientes ativos não têm sessões agendadas. Pode ser útil verificar se há necessidade de reagendamento."
- "Você recebeu 3 novos pacientes este mês."

## ✅ Critérios de Qualidade

Todos os insights devem:

- ✅ Ser observacionais (nunca imperativos)
- ✅ Usar linguagem humana e acolhedora
- ✅ Não ser alarmistas
- ✅ Não ser técnicos demais
- ✅ Funcionar com dados vazios
- ✅ Ter prioridade adequada

## 🔒 Segurança e Ética

- Nenhum dado clínico é analisado
- Nenhum conteúdo de sessão é processado
- Apenas metadados operacionais (datas, valores, contagens)
- Insights são sempre sugestões, nunca prescrições

---

**Status**: ✅ Implementado e funcionando
**Próximo passo**: Integrar com dados reais da API quando disponível



