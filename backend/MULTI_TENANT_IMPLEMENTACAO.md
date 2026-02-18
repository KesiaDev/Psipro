# Implementação Multi-Tenant SaaS — Psipro Backend

**Data:** Fevereiro 2026  
**Status:** Implementado (incremental, sem quebrar base existente)

---

## Resumo das Mudanças

### ETAPA 1 — Entidade Clinic
- ✅ Adicionado enum `PlanType` (INDIVIDUAL | CLINIC)
- ✅ Adicionado campo `planType` em Clinic (default: CLINIC)

### ETAPA 2 — Alteração em User
- ✅ Adicionado `clinicId` (FK → Clinic.id, nullable para migração)
- ✅ Adicionado `role` (enum UserRole: OWNER, PSYCHOLOGIST, ASSISTANT)
- ✅ Mantida compatibilidade com ClinicUser (many-to-many)

### ETAPA 3 — clinicId nas Tabelas
- ✅ `Patient` — já tinha clinicId (opcional)
- ✅ `Appointment` — já tinha clinicId (opcional)
- ✅ `FinancialRecord` — adicionado clinicId (nullable)
- ✅ `Payment` — adicionado clinicId (nullable)

### ETAPA 4 — Middleware / Contexto
- ✅ `getCurrentClinicId(ctx)` em `common/helpers/clinic-context.helper.ts`
- ✅ Decorator `@CurrentClinicId()` em `common/decorators/current-clinic.decorator.ts`
- ✅ JwtStrategy passa `clinicId` em `request.user` (de user.clinicId ou ClinicUser)

### ETAPA 5 — Registro de Usuário
- ✅ Se não vier clinic: cria Clinic com planType INDIVIDUAL e name = fullName
- ✅ Associa user à nova clinic com role OWNER

### ETAPA 6 — Tabela Subscription
- ✅ Criada tabela `subscriptions` com:
  - clinicId, status (ACTIVE | PAST_DUE | CANCELED)
  - stripeCustomerId, stripeSubscriptionId, currentPeriodEnd
- ✅ Sem integração Stripe ainda

### ETAPA 7 — Seed de Migração
- ✅ Script `prisma/migrate-existing-data.ts`:
  - Cria clinic "PSIPRO_DEFAULT"
  - Associa usuários sem clinicId a ela
  - Atualiza financial_records e payments com clinicId

### ETAPA 8 — Testes
- ⚠️ Validar manualmente após deploy

---

## Arquivos Alterados

### Schema / Migrations
| Arquivo | Mudança |
|---------|---------|
| `prisma/schema.prisma` | PlanType, UserRole, SubscriptionStatus enums; Clinic.planType; User.clinicId, role; FinancialRecord.clinicId; Payment.clinicId; model Subscription |
| `prisma/migrations/20260213000000_add_multi_tenant_.../migration.sql` | SQL da migration |
| `prisma/seed.ts` | planType em Clinic; clinicId/role em User; Clinic INDIVIDUAL para psicólogo independente |
| `prisma/migrate-existing-data.ts` | **NOVO** — script de migração para dados existentes |

### Auth
| Arquivo | Mudança |
|---------|---------|
| `src/auth/auth.service.ts` | register: auto-cria Clinic INDIVIDUAL; validateToken: usa user.clinicId, clinicUserRole |
| `src/auth/strategies/jwt.strategy.ts` | Inclui clinicId em request.user |

### Common
| Arquivo | Mudança |
|---------|---------|
| `src/common/helpers/clinic-context.helper.ts` | **NOVO** — getCurrentClinicId |
| `src/common/decorators/current-clinic.decorator.ts` | **NOVO** — @CurrentClinicId |

### Services
| Arquivo | Mudança |
|---------|---------|
| `src/patients/patients.service.ts` | getUserClinics inclui user.clinicId; create usa clinicId do user para paciente independente |
| `src/payments/payments.service.ts` | create recebe clinicId, grava em Payment; validação de acesso por clinic |
| `src/payments/payments.controller.ts` | Passa user.clinicId para create |

---

## Migrations Criadas

1. **20260213000000_add_multi_tenant_clinic_user_payment_subscription**
   - Cria enums PlanType, UserRole, SubscriptionStatus
   - Adiciona planType em clinics
   - Adiciona clinicId, role em users
   - Adiciona clinicId em financial_records, payments
   - Cria tabela subscriptions
   - Cria índices e FKs

---

## Ordem de Deploy

1. **Rodar migration:**
   ```bash
   npx prisma migrate deploy
   ```

2. **Rodar script de migração de dados (para dados existentes):**
   ```bash
   npx ts-node prisma/migrate-existing-data.ts
   # ou: npx tsx prisma/migrate-existing-data.ts
   ```

3. **Rodar seed (opcional, ambiente dev):**
   ```bash
   npx prisma db seed
   ```

---

## Checklist de Validação (ETAPA 8)

- [ ] Usuário A não enxerga dados da Clinic B
- [ ] Psicólogo individual continua funcionando igual
- [ ] Criação de paciente grava clinicId automaticamente
- [ ] Financeiro continua funcionando
- [ ] Dashboard não quebra
- [ ] GET /auth/me retorna clinicId
