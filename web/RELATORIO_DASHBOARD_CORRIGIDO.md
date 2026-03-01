# Relatório — Correção Definitiva do Dashboard Principal

**Data**: 2025  
**Objetivo**: Remover mocks, conectar exclusivamente à API real e garantir atualização automática.

---

## 1. Objetos e Mocks Removidos

| Local | Removido |
|-------|----------|
| `dashboard/page.tsx` | Estado inicial mockado de métricas, agenda e financeiro |
| `dashboard/page.tsx` | Chamadas a `dashboardService.getMetrics()`, `getAgendaSummary()`, `getFinanceSummary()` (endpoints antigos) |
| `dashboardService.ts` | Métodos `getMetrics()`, `getAgendaSummary()`, `getFinanceSummary()` e consumo de `/dashboard/metrics`, `/dashboard/agenda-summary`, `/dashboard/finance-summary` |
| Dashboard | Fallback com dados vazios quando API falha — substituído por exibição de erro + botão "Tentar novamente" |

---

## 2. Novo dashboardService.ts

**Arquivo**: `web/app/services/dashboardService.ts`

Consome exclusivamente `api.ts` (baseURL, Authorization, X-Clinic-Id automáticos).

| Método | Endpoint | Retorno |
|--------|----------|---------|
| `getPatientsCount()` | `GET /patients/count` | `number` |
| `getAppointmentsToday()` | `GET /appointments/today` | `{ count, items }` |
| `getSessionsStats()` | `GET /sessions/stats` | `{ sessionsThisMonth, sessionsThisWeek }` |
| `getFinancialSummary()` | `GET /financial/summary` | `{ receitaHoje, receitaMes, totalRecebido, totalAReceber, ticketMedio }` |

**Sem fallbacks.** Em caso de falha, o erro é propagado para a página.

---

## 3. Endpoints Criados no Backend

| Endpoint | Arquivo | Descrição |
|----------|---------|-----------|
| `GET /patients/count` | `patients.controller.ts`, `patients.service.ts` | Total de pacientes da clínica |
| `GET /appointments/today` | `appointments.controller.ts`, `appointments.service.ts` | Consultas agendadas para hoje (`{ count, items }`) |
| `GET /sessions/stats` | `sessions.controller.ts`, `sessions.service.ts` | `sessionsThisMonth`, `sessionsThisWeek` (sessões realizadas) |

**Alteração**: `GET /financial/summary` — permissão estendida de `@Roles('admin')` para `@Roles('admin', 'psychologist')`.

---

## 4. Implementação no Dashboard

- **useEffect** para carregar os 4 endpoints em paralelo quando a clínica muda
- **Estado de erro**: se a API falhar, exibe mensagem e botão "Tentar novamente"
- **Sem fallback**: nenhum dado mockado; em caso de erro, a tela mostra apenas o erro
- **Authorization e X-Clinic-Id** enviados em todas as requisições via `api.ts`

---

## 5. Atualização Após Criação de Paciente

- Constante `DASHBOARD_REFRESH_EVENT` em `web/app/constants/events.ts`
- Na criação de paciente (`handleCreatePatient`), dispara `window.dispatchEvent(new CustomEvent(DASHBOARD_REFRESH_EVENT))`
- Na importação de pacientes (`handleImportPatients`), também dispara o mesmo evento
- O Dashboard escuta o evento e chama `loadDashboardData()` para atualizar os dados

---

## 6. Arquivos Alterados

### Backend
- `backend/src/patients/patients.controller.ts` — rota `GET /patients/count`
- `backend/src/patients/patients.service.ts` — método `getCount()`
- `backend/src/appointments/appointments.controller.ts` — rota `GET /appointments/today`
- `backend/src/appointments/appointments.service.ts` — método `getToday()`
- `backend/src/sessions/sessions.controller.ts` — rota `GET /sessions/stats`
- `backend/src/sessions/sessions.service.ts` — método `getStats()`
- `backend/src/financial/financial.controller.ts` — `@Roles` ampliado para `psychologist`

### Web
- `web/app/services/dashboardService.ts` — reescrito com os 4 endpoints acima
- `web/app/dashboard/page.tsx` — usa o novo `dashboardService`, exibe erro sem fallback, escuta evento de refresh
- `web/app/pacientes/page.tsx` — dispara `DASHBOARD_REFRESH_EVENT` ao criar/importar pacientes
- `web/app/constants/events.ts` — definição do evento

---

## 7. Métricas Exibidas

| Card | Fonte |
|------|-------|
| Pacientes ativos | `GET /patients/count` |
| Sessões realizadas (este mês) | `GET /sessions/stats` → `sessionsThisMonth` |
| Consultas hoje | `GET /appointments/today` → `count` |
| Receita do mês | `GET /financial/summary` → `receitaMes` |
| Valores a receber | `GET /financial/summary` → `totalAReceber` |

---

## 8. Blocos de Conteúdo

- **Agenda da clínica**: Lista de consultas de hoje via `GET /appointments/today` → `items`
- **Resumo financeiro**: Receita do mês, ticket médio e valores a receber via `GET /financial/summary`
- **Insights**: Gerados com base nos dados reais (sem mocks)

---

## 9. Confirmações

- [x] Nenhum objeto mockado de métricas, pacientes ou agenda
- [x] `dashboardService.ts` criado e usando `api.ts`
- [x] Endpoints `GET /patients/count`, `GET /appointments/today`, `GET /sessions/stats`, `GET /financial/summary` implementados e utilizados
- [x] useEffect carregando dados reais
- [x] Em falha da API: exibição de erro, sem fallback mock
- [x] Authorization e X-Clinic-Id enviados (via `api.ts`)
- [x] Atualização automática após criação ou importação de pacientes
