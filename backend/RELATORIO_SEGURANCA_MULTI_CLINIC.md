# Relatório Final de Segurança — Implementação Multi-Clinic

**Data:** 12/02/2025  
**Projeto:** PsiPro Backend (NestJS)

---

## Resumo Executivo

Implementação completa da correção do sistema multi-clinic, incluindo:

- Contexto global de clínica via header `X-Clinic-Id`
- Validação de pertencimento à clínica
- Correção de vulnerabilidades em Payments, Sessions e Financial
- Novos campos `clinicId` no Prisma (Session, Payment, FinancialRecord, Document, Insight)
- Migration segura para compatibilidade com dados existentes

---

## FASE 1 — Contexto Global de Clínica

### 1. ClinicContextGuard

**Arquivo:** `src/common/guards/clinic-context.guard.ts`

- Lê header `X-Clinic-Id`
- Valida se o usuário pertence à clínica via `ClinicUser`
- Injeta `request.clinicId`
- Bloqueia quando:
  - Usuário pertence a 2+ clínicas e não envia o header (400)
  - Usuário envia `X-Clinic-Id` de clínica à qual não pertence (403)
- Usuário com 0 clínicas (independente): `clinicId = null`, segue normal
- Usuário com 1 clínica: auto-injeta quando header ausente

### 2. Decorators

- **`@ClinicId()`** — obtém `clinicId` do request
- **`@SkipClinicContext()`** — dispensa validação (Auth, Clinics)

### 3. Controllers Atualizados

| Controller    | Uso de `request.clinicId` |
|---------------|---------------------------|
| Patients      | create, import, findAll   |
| Appointments  | findAll                   |
| Sessions      | create, findAll           |
| Payments      | create, findByPatient     |
| Financial     | getSummary, getPatientFinancial |
| Documents     | findAll                   |
| Insights      | findAll, getSummary, dismiss |
| Sync          | getPatients, syncPatients |

### 4. Rotas sem obrigatoriedade de clinic

- `AuthController` — `@SkipClinicContext()`
- `ClinicsController` — `@SkipClinicContext()` (clinic vem da URL)

---

## FASE 2 — Correções de Segurança

### PaymentsService

- `clinicId` incluído no modelo e nas operações
- `PatientAccessHelper` para validar acesso ao paciente (próprio, clínica ou sharedWith)
- `create`: valida acesso, persiste `clinicId`, não restringe mais só a `patient.userId`
- `findByPatient`: valida acesso e filtra por `clinicId` quando informado

### SessionsService

- `PatientAccessHelper` para validação de acesso
- `create`: aceita sessões de pacientes da clínica e compartilhados
- Persiste `clinicId` nas novas sessões
- `findAll`: considera `clinicId` no filtro

### FinancialService

- `PatientAccessHelper` para `getPatientFinancial`
- `getSummary`: suporta filtro por `clinicId` em payments e sessions
- `getPatientFinancial`: valida acesso antes de retornar dados

### PatientAccessHelper

**Arquivo:** `src/common/helpers/patient-access.helper.ts`

- Lógica centralizada de acesso ao paciente
- Casos: próprio (`userId`), clínica (`ClinicUser` + `canViewAllPatients` ou `sharedWith`), compartilhado (`sharedWith`)

---

## FASE 3 — Prisma e Migration

### Novos campos (todos opcionais)

| Model          | Campo      | Tipo    |
|----------------|------------|---------|
| Session        | clinicId   | String? |
| Payment        | clinicId   | String? |
| FinancialRecord| clinicId   | String? |
| Document       | clinicId   | String? |
| Insight        | clinicId   | String? |

### Migration

**Arquivo:** `prisma/migrations/20250212000000_add_clinic_id_multi_tenant/migration.sql`

- Usa `ADD COLUMN IF NOT EXISTS` para evitar erro em re-execução
- Índices criados com `IF NOT EXISTS`
- Compatível com registros antigos (`clinicId` nullable)

**Comando:** `npm run migrate:deploy` (em produção)

---

## Arquivos Criados

| Arquivo |
|---------|
| `src/common/guards/clinic-context.guard.ts` |
| `src/common/decorators/clinic-id.decorator.ts` |
| `src/common/decorators/skip-clinic-context.decorator.ts` |
| `src/common/helpers/patient-access.helper.ts` |
| `src/common/common.module.ts` |
| `prisma/migrations/20250212000000_add_clinic_id_multi_tenant/migration.sql` |

---

## Arquivos Modificados

| Arquivo |
|---------|
| `prisma/schema.prisma` |
| `src/app.module.ts` |
| `src/auth/auth.controller.ts` |
| `src/clinics/clinics.controller.ts` |
| `src/patients/patients.controller.ts` |
| `src/patients/patients.service.ts` |
| `src/appointments/appointments.controller.ts` |
| `src/sessions/sessions.controller.ts` |
| `src/sessions/sessions.service.ts` |
| `src/payments/payments.controller.ts` |
| `src/payments/payments.service.ts` |
| `src/financial/financial.controller.ts` |
| `src/financial/financial.service.ts` |
| `src/documents/documents.controller.ts` |
| `src/documents/documents.service.ts` |
| `src/insights/insights.controller.ts` |
| `src/insights/insights.service.ts` |
| `src/sync/sync.controller.ts` |

---

## Nível de Segurança Multi-Tenant

**Antes:** 5/10  
**Depois:** **8/10**

### Motivos do incremento

1. Contexto de clínica centralizado via `X-Clinic-Id`
2. Validação de pertencimento à clínica antes de operar
3. Payments ajustado para suportar clínica
4. Sessions.create liberado para membros da clínica
5. Financial isolado por `clinicId`
6. Documents e Insights filtrados por `clinicId`
7. Uso de `clinicId` em Session, Payment, FinancialRecord, Document, Insight
8. `PatientAccessHelper` unifica a lógica de acesso ao paciente

### O que ainda pode evoluir

- Integração explícita do modelo `Clinic` com Session, Payment etc. (relações Prisma)
- Política de `X-Clinic-Id` obrigatório em alguns contextos (ex.: sync)
- Auditoria de acesso por clínica (logs, métricas)

---

## Compatibilidade

- Dados antigos preservados (`clinicId` opcional)
- Psicólogos independentes continuam funcionando (`clinicId = null`)
- Query `?clinicId=` mantida no Sync para compatibilidade, com prioridade do header quando presente

---

## Build Status

```
✅ npm run build — sucesso
✅ npx prisma generate — sucesso
```
