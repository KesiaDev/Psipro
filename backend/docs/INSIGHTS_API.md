# 📊 API de Insights Resumidos - PsiPro

## 📋 Visão Geral

Endpoint para consumo de insights resumidos pelo App Android. Retorna no máximo 3 insights priorizados baseados em dados operacionais e administrativos.

**⚠️ IMPORTANTE**: Este endpoint **NUNCA** inclui dados clínicos ou diagnósticos. Apenas observações sobre agenda, financeiro e gestão de pacientes.

---

## 🔐 Autenticação

Todos os endpoints requerem autenticação via JWT token no header:

```
Authorization: Bearer {token}
```

---

## 📡 Endpoint

### `GET /insights/summary`

Retorna no máximo 3 insights priorizados para o usuário autenticado.

#### Request

```http
GET /insights/summary
Authorization: Bearer {jwt_token}
```

#### Response

**Status:** `200 OK`

**Body:**

```json
[
  {
    "id": "financial-revenue-decrease",
    "title": "Receita abaixo do esperado",
    "description": "Sua receita deste mês está 25% abaixo do mês anterior. Pode ser útil verificar se há padrões que explicam essa diferença.",
    "priority": 8,
    "actionId": "view-financial"
  },
  {
    "id": "agenda-missed-sessions",
    "title": "Taxa de faltas elevada",
    "description": "Você teve 5 faltas este mês (22% das sessões). Pode ser útil observar se há algum padrão nos dias ou horários.",
    "priority": 7,
    "actionId": "view-agenda"
  },
  {
    "id": "patients-no-sessions",
    "title": "Pacientes sem sessões futuras",
    "description": "3 pacientes ativos não têm sessões agendadas. Pode ser útil verificar se há necessidade de reagendamento.",
    "priority": 5,
    "actionId": "view-patients"
  }
]
```

#### Campos do Response

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `id` | `string` | Identificador único do insight (ex: `agenda-missed-sessions`) |
| `title` | `string` | Título curto do insight |
| `description` | `string` | Descrição detalhada (linguagem observacional) |
| `priority` | `number` | Prioridade (1-10, maior = mais importante) |
| `actionId` | `string?` | ID da ação relacionada (opcional, ex: `view-agenda`, `view-financial`, `view-patients`) |

#### Erros

**401 Unauthorized**
```json
{
  "statusCode": 401,
  "message": "Unauthorized"
}
```

**500 Internal Server Error**
```json
{
  "statusCode": 500,
  "message": "Internal server error"
}
```

---

## 🧠 Tipos de Insights

### Agenda
- Taxa de faltas elevada
- Poucas sessões agendadas
- Cancelamentos recentes
- Crescimento na agenda

### Financeiro
- Receita abaixo/acima do esperado
- Atendimentos pendentes de pagamento
- Valores a receber

### Pacientes (Administrativo)
- Pacientes sem sessões futuras
- Crescimento na base de pacientes

---

## 📐 Regras de Priorização

1. **Prioridade 8-10**: Insights críticos (ex: receita muito abaixo)
2. **Prioridade 6-7**: Insights importantes (ex: muitas faltas, receita em crescimento)
3. **Prioridade 4-5**: Insights informativos (ex: poucas sessões agendadas)
4. **Prioridade 1-3**: Insights de baixa prioridade

Os insights são ordenados por prioridade (maior primeiro) e apenas os 3 mais prioritários são retornados.

---

## 🔒 Segurança e Privacidade

### Princípios

- ✅ **Apenas dados operacionais**: Agenda, financeiro, contagem de pacientes
- ✅ **Linguagem observacional**: "Pode ser útil observar..." (nunca imperativo)
- ✅ **Sem dados clínicos**: Nenhum conteúdo de prontuário ou diagnóstico
- ✅ **Sem recomendações terapêuticas**: Apenas observações administrativas

### O que NÃO é incluído

- ❌ Conteúdo de sessões ou anotações
- ❌ Diagnósticos ou hipóteses clínicas
- ❌ Recomendações de tratamento
- ❌ Dados sensíveis de pacientes (apenas contagens e estatísticas)

---

## 🚀 Exemplo de Uso (Android)

```kotlin
// Exemplo em Kotlin
suspend fun fetchInsights(): List<InsightSummary> {
    val response = httpClient.get("https://api.psipro.com.br/insights/summary") {
        header("Authorization", "Bearer $token")
    }
    return response.body()
}

data class InsightSummary(
    val id: String,
    val title: String,
    val description: String,
    val priority: Int,
    val actionId: String?
)
```

---

## 📝 Notas de Implementação

### Coleta de Dados

O endpoint coleta dados em tempo real do banco de dados:
- Sessões realizadas (este mês, mês anterior, esta semana)
- Agendamentos futuros
- Faltas e cancelamentos
- Receita e pagamentos pendentes
- Contagem de pacientes ativos

### Performance

- O endpoint faz múltiplas queries ao banco
- Considerar cache para produção (ex: Redis)
- Timeout recomendado: 5 segundos

### Escalabilidade

O motor de insights atual é baseado em regras. Está preparado para evoluir para IA (OpenAI, Azure, etc.) sem quebrar o contrato da API.

---

## 🔄 Versionamento

**Versão atual:** `v1`

O endpoint está em `/insights/summary` (sem prefixo de versão). Se houver mudanças que quebrem compatibilidade, será criado `/v2/insights/summary`.

---

## ✅ Checklist de Implementação

- [x] DTO `InsightSummaryDTO` criado
- [x] Endpoint `GET /insights/summary` implementado
- [x] Autenticação via JWT
- [x] Motor de insights baseado em regras
- [x] Coleta de dados do banco
- [x] Priorização e limite de 3 insights
- [x] Linguagem observacional
- [x] Sem dados clínicos
- [x] Documentação completa

---

## 📞 Suporte

Para dúvidas ou problemas, consulte:
- `API.md` - Documentação geral da API
- `README.md` - Documentação do projeto

