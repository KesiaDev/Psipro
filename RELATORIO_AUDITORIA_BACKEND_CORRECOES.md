# Relatório de Auditoria e Correções do Backend PsiPro

**Data:** Março 2025

---

## 1. Correções Aplicadas

### 1.1 Inconsistência getFinanceSummary → getFinancialSummary

| Local | Antes | Depois |
|-------|-------|--------|
| web/app/financeiro/page.tsx | `getFinanceSummary()` (não existia) | `getFinanceSummary()` agora existe e chama GET /dashboard/finance-summary |
| web/app/services/dashboardService.ts | Apenas `getFinancialSummary()` (chama /financial/summary) | Adicionado `getFinanceSummary()` (chama /dashboard/finance-summary) |

**Observação:** Mantidos ambos os métodos pois servem propósitos diferentes:
- `getFinancialSummary()` → GET /financial/summary — estrutura: receitaHoje, receitaMes, totalRecebido, totalAReceber, ticketMedio
- `getFinanceSummary()` → GET /dashboard/finance-summary — estrutura: monthlyRevenue, averagePerSession, unpaidSessions, isEmpty (usado na página Financeiro)

### 1.2 Métodos faltantes no dashboardService

| Método | Endpoint | Status |
|--------|----------|--------|
| getMetrics() | GET /api/dashboard/metrics | ✅ Criado |
| getAgendaSummary() | GET /api/dashboard/agenda-summary | ✅ Criado |
| getFinanceSummary() | GET /api/dashboard/finance-summary | ✅ Criado |

**Arquivos alterados:**
- `web/app/services/dashboardService.ts` — adicionados getMetrics(), getAgendaSummary(), getFinanceSummary()
- `web/app/financeiro/page.tsx` — já chamava getFinanceSummary(); método implementado
- `web/app/agenda/page.tsx` — chama getMetrics() e getAgendaSummary(); métodos implementados

### 1.3 Endpoint Beta Request

| Item | Implementação |
|------|---------------|
| Controller | BetaController (POST /api/beta/request) |
| Service | BetaService |
| DTO | CreateBetaRequestDto |
| Prisma model | BetaRequest |
| Migration | 20260303000000_add_beta_request |

**Arquivos criados:**
- `backend/src/beta/beta.module.ts`
- `backend/src/beta/beta.controller.ts`
- `backend/src/beta/beta.service.ts`
- `backend/src/beta/dto/create-beta-request.dto.ts`
- `backend/prisma/migrations/20260303000000_add_beta_request/migration.sql`

**Payload aceito:**
```json
{
  "name": "string",
  "email": "string",
  "clinicName": "string?",
  "message": "string?"
}
```

Compatível também com o formulário Web que envia: fullName, email, city, state, practiceType, expectations.

---

## 2. Validação dos Endpoints do Dashboard

| Endpoint | Status | Controller | Observação |
|----------|--------|------------|------------|
| GET /api/clinics | ✅ Existe | ClinicsController | - |
| POST /api/clinics | ✅ Existe | ClinicsController | - |
| PUT /api/clinics/:id | ✅ Existe | ClinicsController | - |
| GET /api/patients | ✅ Existe | PatientsController | - |
| POST /api/patients | ✅ Existe | PatientsController | - |
| PUT /api/patients/:id | ⚠️ PATCH | PatientsController | Backend usa PATCH (padrão REST para atualização parcial) |
| DELETE /api/patients/:id | ✅ Existe | PatientsController | - |
| GET /api/appointments | ✅ Existe | AppointmentsController | - |
| POST /api/appointments | ✅ Existe | AppointmentsController | - |
| GET /api/sessions | ✅ Existe | SessionsController | - |
| POST /api/sessions | ✅ Existe | SessionsController | - |
| GET /api/financial/records | ✅ Existe | FinancialController | - |
| POST /api/financial/records | ✅ Existe | FinancialController | - |
| GET /api/reports | ✅ Existe | ReportsController | - |
| GET /api/dashboard/metrics | ✅ Existe | DashboardController | - |
| GET /api/dashboard/agenda-summary | ✅ Existe | DashboardController | - |
| GET /api/dashboard/finance-summary | ✅ Existe | DashboardController | - |
| GET /api/dashboard/count | ✅ Existe | DashboardController | - |
| GET /api/dashboard/stats | ✅ Existe | DashboardController | - |
| GET /api/dashboard/summary | ✅ Existe | DashboardController | - |
| POST /api/beta/request | ✅ Criado | BetaController | **Novo** |

---

## 3. Tabela Final: Endpoint | Status | Controller | Correção Aplicada

| Endpoint | Status | Controller | Correção Aplicada |
|----------|--------|------------|-------------------|
| GET /api/clinics | OK | ClinicsController | - |
| POST /api/clinics | OK | ClinicsController | - |
| PUT /api/clinics/:id | OK | ClinicsController | - |
| GET /api/patients | OK | PatientsController | - |
| POST /api/patients | OK | PatientsController | - |
| PATCH /api/patients/:id | OK | PatientsController | - |
| DELETE /api/patients/:id | OK | PatientsController | - |
| GET /api/appointments | OK | AppointmentsController | - |
| POST /api/appointments | OK | AppointmentsController | - |
| GET /api/sessions | OK | SessionsController | - |
| POST /api/sessions | OK | SessionsController | - |
| GET /api/financial/records | OK | FinancialController | - |
| POST /api/financial/records | OK | FinancialController | - |
| GET /api/reports | OK | ReportsController | - |
| GET /api/dashboard/metrics | OK | DashboardController | Web: getMetrics() adicionado |
| GET /api/dashboard/agenda-summary | OK | DashboardController | Web: getAgendaSummary() adicionado |
| GET /api/dashboard/finance-summary | OK | DashboardController | Web: getFinanceSummary() adicionado |
| POST /api/beta/request | OK | BetaController | **Criado** (módulo, service, DTO, migration) |

---

## 4. Próximos Passos

1. **Executar migration** em produção: `npx prisma migrate deploy`
2. **Deploy** — fazer push das alterações para o repositório
3. **Testar** — validar formulário Beta e páginas Financeiro/Agenda no dashboard
