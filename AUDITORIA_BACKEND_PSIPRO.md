# Auditoria Completa do Backend PsiPro

**Data:** Março 2025  
**Objetivo:** Verificar conectividade entre backend, dashboard Web e app Android.

---

## 1. ESTRUTURA DO PROJETO

### Módulos (backend/src)

| Módulo | Responsabilidade |
|--------|------------------|
| LoggerModule | Logging estruturado |
| AppThrottlerModule | Rate limiting |
| ConfigModule | Variáveis de ambiente |
| ScheduleModule | Tarefas agendadas (cron) |
| PrismaModule | ORM Prisma (PostgreSQL) |
| AuditModule | Auditoria de ações críticas |
| CommonModule | Guards (JwtAuth, Clinic, Roles), decorators |
| AuthModule | Login, registro, JWT, refresh token |
| ClinicsModule | CRUD clínicas, convites, profissionais |
| PatientsModule | CRUD pacientes, importação Excel |
| AppointmentsModule | CRUD agendamentos |
| SessionsModule | Sessões realizadas |
| PaymentsModule | Pagamentos/cobranças |
| FinancialModule | Registros financeiros, summary |
| DocumentsModule | Documentos |
| InsightsModule | Insights gerados |
| DashboardModule | Métricas, resumos dashboard |
| ReportsModule | Relatórios |
| SyncModule | Sincronização Android ↔ Web |

### Controllers

- `AuthController` — /api/auth
- `ClinicsController` — /api/clinics
- `PatientsController` — /api/patients
- `AppointmentsController` — /api/appointments
- `SessionsController` — /api/sessions
- `PaymentsController` — /api/payments
- `FinancialController` — /api/financial
- `DocumentsController` — /api/documents
- `InsightsController` — /api/insights
- `DashboardController` — /api/dashboard
- `ReportsController` — /api/reports
- `SyncController` — /api/sync

### Services principais

- auth.service, refresh-token.service
- clinics.service
- patients.service, patients-import.service
- appointments.service
- sessions.service
- payments.service
- financial.service
- documents.service
- insights.service
- dashboard.service
- reports.service
- sync.service

---

## 2. ENDPOINTS EXISTENTES

**Prefixo global:** `/api`

### Auth
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | /api/auth/login | Login |
| POST | /api/auth/register | Registro |
| POST | /api/auth/logout | Logout |
| POST | /api/auth/refresh | Refresh token |
| POST | /api/auth/handoff | Handoff Android → Web |
| POST | /api/auth/switch-clinic | Trocar clínica ativa |
| GET | /api/auth/me | Dados do usuário autenticado |

### Clinics
| Método | Endpoint |
|--------|----------|
| POST | /api/clinics |
| GET | /api/clinics |
| GET | /api/clinics/:id |
| PUT | /api/clinics/:id |
| DELETE | /api/clinics/:id |
| POST | /api/clinics/:id/invite |
| PUT | /api/clinics/:id/users/:userId |
| DELETE | /api/clinics/:id/users/:userId |
| GET | /api/clinics/:clinicId/professionals |
| GET | /api/clinics/:id/stats |

### Patients
| Método | Endpoint |
|--------|----------|
| GET | /api/patients |
| GET | /api/patients/count |
| GET | /api/patients/recent |
| GET | /api/patients/:id |
| POST | /api/patients |
| POST | /api/patients/import |
| PATCH | /api/patients/:id |
| DELETE | /api/patients/:id |

### Appointments
| Método | Endpoint |
|--------|----------|
| GET | /api/appointments |
| GET | /api/appointments/today |
| GET | /api/appointments/recent |
| GET | /api/appointments/:id |
| POST | /api/appointments |
| PUT | /api/appointments/:id |
| DELETE | /api/appointments/:id |

### Sessions
| Método | Endpoint |
|--------|----------|
| POST | /api/sessions |
| GET | /api/sessions/stats |
| GET | /api/sessions |

### Payments
| Método | Endpoint |
|--------|----------|
| POST | /api/payments |
| GET | /api/payments/patient/:patientId |

### Financial
| Método | Endpoint |
|--------|----------|
| GET | /api/financial/summary |
| GET | /api/financial/records |
| POST | /api/financial/records |
| PATCH | /api/financial/records/:id |
| DELETE | /api/financial/records/:id |
| GET | /api/financial/patient/:patientId |

### Documents
| Método | Endpoint |
|--------|----------|
| GET | /api/documents |

### Insights
| Método | Endpoint |
|--------|----------|
| GET | /api/insights |
| GET | /api/insights/summary |
| PATCH | /api/insights/:id/dismiss |

### Dashboard
| Método | Endpoint |
|--------|----------|
| GET | /api/dashboard/metrics |
| GET | /api/dashboard/agenda-summary |
| GET | /api/dashboard/finance-summary |
| GET | /api/dashboard/count |
| GET | /api/dashboard/stats |
| GET | /api/dashboard/summary |

### Reports
| Método | Endpoint |
|--------|----------|
| GET | /api/reports |
| GET | /api/reports/summary |
| GET | /api/reports/today |
| GET | /api/reports/stats |
| GET | /api/reports/count |

### Sync (Android ↔ Web)
| Método | Endpoint |
|--------|----------|
| GET | /api/sync/patients |
| POST | /api/sync/patients |

---

## 3. BANCO DE DADOS (Prisma)

### Tabelas

| Tabela Prisma | Tabela SQL | Descrição |
|---------------|------------|-----------|
| Clinic | clinics | Clínicas/organizações |
| ClinicUser | clinic_users | User ↔ Clinic (N:N) + role |
| User | users | Usuários profissionais |
| RefreshToken | refresh_tokens | Tokens de sessão |
| Patient | patients | Pacientes |
| Appointment | appointments | Agendamentos |
| Session | sessions | Sessões realizadas |
| Payment | payments | Pagamentos/cobranças |
| FinancialRecord | financial_records | Registros financeiros |
| Document | documents | Documentos/arquivos |
| Insight | insights | Insights gerados |
| Subscription | subscriptions | Assinaturas (Stripe) |
| AuditLog | audit_logs | Auditoria |

**Nota:** Não existe tabela `professionals` separada. Profissionais são `User` associados via `ClinicUser`.

### Relacionamentos principais

- `clinic_id` — Patient, Appointment, Payment, FinancialRecord, AuditLog
- `user_id` — Appointment, Session, Payment, Document, etc.
- `patient_id` — Appointment, Session, Payment, Document
- `clinic_users` — clinicId + userId (N:N)

---

## 4. CONSISTÊNCIA DAS ROTAS (Web)

### Endpoints chamados pelo Web

| Endpoint | Usado em | Existe? |
|----------|----------|---------|
| POST /api/auth/login | authService | ✅ |
| POST /api/auth/register | authService | ✅ |
| POST /api/auth/refresh | authService | ✅ |
| POST /api/auth/switch-clinic | authService | ✅ |
| GET /api/auth/me | authService | ✅ |
| GET/POST/PUT/DELETE /api/clinics | clinicService | ✅ |
| GET /api/clinics/:id/stats | clinicService | ✅ |
| GET /api/patients, /count, /:id | patientService | ✅ |
| POST /api/patients, /import | patientService | ✅ |
| PATCH/DELETE /api/patients/:id | patientService | ✅ |
| GET/POST/PUT/DELETE /api/appointments | appointmentService | ✅ |
| GET /api/sessions | sessionService | ✅ |
| GET /api/payments/patient/:id | paymentService | ✅ |
| GET /api/financial/summary | dashboardService, financialService | ✅ |
| GET /api/financial/patient/:id | financialService | ✅ |
| GET /api/documents | documentService | ✅ |
| GET /api/patients/count | dashboardService | ✅ |
| GET /api/appointments/today | dashboardService | ✅ |
| GET /api/sessions/stats | dashboardService | ✅ |
| GET /api/sync/patients | patientService | ✅ |
| POST /api/beta/request | BetaAccessForm | ❌ **NÃO EXISTE** |
| GET /api/auth/beta-status | BetaAccessGate (comentado) | ❌ **NÃO EXISTE** |

### Problemas detectados no Web

1. **dashboardService incompleto**
   - `financeiro/page.tsx` chama `getFinanceSummary()` — método **não existe** (o correto é `getFinancialSummary()`)
   - `agenda/page.tsx` chama `getMetrics()` e `getAgendaSummary()` — **não existem** no dashboardService
   - O dashboardService atual só implementa: getPatientsCount, getAppointmentsToday, getSessionsStats, getFinancialSummary

2. **Dashboard usa endpoints diretos**
   - Em vez de `/api/dashboard/*`, o dashboardService usa `/patients/count`, `/appointments/today`, `/sessions/stats`, `/financial/summary`
   - Os endpoints `/api/dashboard/metrics`, `/agenda-summary`, `/finance-summary` existem no backend mas **não são usados** pelo Web

3. **GET /api/clinics/:clinicId/professionals**
   - Endpoint existe; Web **não consome** (página Profissionais pode precisar)

---

## 5. VALIDAÇÕES

| Item | Status |
|------|--------|
| ValidationPipe global | ✅ whitelist, forbidNonWhitelisted, transform |
| DTOs com class-validator | ✅ Login, Register, Patient, Appointment, etc. |
| Dados obrigatórios | ✅ Via decorators @IsString, @IsOptional, etc. |
| Tratamento de erros | ✅ BadRequest, NotFound, Forbidden |
| Status HTTP | ✅ 200, 201, 400, 401, 403, 404, 500 |

---

## 6. AUTENTICAÇÃO

| Endpoint | Existe | Usa JWT |
|----------|--------|---------|
| POST /api/auth/login | ✅ | ✅ (retorna accessToken) |
| POST /api/auth/register | ✅ | ✅ |
| GET /api/auth/me | ✅ | ✅ (JwtAuthGuard) |

- **Mecanismo:** JWT (Bearer token)
- **Refresh:** POST /api/auth/refresh
- **Estratégia:** passport-jwt, JwtStrategy
- Web **não** chama POST /api/auth/logout no backend (apenas limpa localStorage)

---

## 7. MULTI-CLÍNICA

| Recurso | Implementado |
|---------|--------------|
| Múltiplas clínicas | ✅ Clinic, ClinicUser |
| Múltiplos profissionais | ✅ ClinicUser com roles (owner, admin, psychologist, assistant) |
| Pacientes vinculados à clínica | ✅ Patient.clinicId |
| Troca de clínica ativa | ✅ POST /api/auth/switch-clinic |
| Header x-clinic-id | ✅ Obrigatório em rotas protegidas (ClinicGuard) |
| Isolamento por clínica | ✅ Filtros clinicId em queries |

---

## 8. INTEGRAÇÃO COM APP ANDROID

### Endpoints usados pelo App

| Endpoint | Uso |
|----------|-----|
| POST /api/auth/login | Login |
| POST /api/auth/register | Registro |
| POST /api/auth/refresh | Renovar token |
| POST /api/auth/logout | Logout |
| POST /api/auth/switch-clinic | Trocar clínica |
| GET /api/auth/me | Dados do usuário |
| GET /api/sync/patients | Baixar pacientes |
| POST /api/sync/patients | Enviar/sincronizar pacientes |

### Endpoints NÃO usados pelo App

- CRUD pacientes direto (usa sync)
- Appointments
- Sessions
- Payments
- Financial
- Documents
- Clinics (gestão)
- Dashboard
- Reports

O App opera em modo **sync**: envia e recebe pacientes via `/api/sync/patients`. O restante do CRUD é feito pelo Web ou futuramente pelo App.

**Sugestão:** Se o App precisar de agendamentos, sessões ou pagamentos, criar endpoints no Sync ou expor os existentes com autenticação adequada.

---

## 9. LOGS E ERROS

| Item | Status |
|------|--------|
| Logging middleware | ✅ loggingMiddleware |
| Exception filters | ✅ LoggingExceptionFilter |
| Throttler | ✅ Rate limiting em auth |
| Helmet | ✅ Segurança headers |
| Prisma soft-delete | ✅ deletedAt em Patient, Appointment, Session, Payment |

---

## 10. RELATÓRIO FINAL (Tabela Resumo)

| Endpoint | Status | Web | App | Problema |
|----------|--------|-----|-----|----------|
| POST /api/auth/login | OK | ✅ | ✅ | - |
| POST /api/auth/register | OK | ✅ | ✅ | - |
| POST /api/auth/logout | OK | ❌ | ✅ | Web não chama backend |
| POST /api/auth/refresh | OK | ✅ | ✅ | - |
| POST /api/auth/handoff | OK | ❌ | ❌ | Para SSO |
| POST /api/auth/switch-clinic | OK | ✅ | ✅ | - |
| GET /api/auth/me | OK | ✅ | ✅ | - |
| POST /api/clinics | OK | ✅ | ❌ | - |
| GET /api/clinics | OK | ✅ | ❌ | - |
| GET /api/clinics/:id | OK | ✅ | ❌ | - |
| PUT /api/clinics/:id | OK | ✅ | ❌ | - |
| DELETE /api/clinics/:id | OK | ✅ | ❌ | - |
| POST /api/clinics/:id/invite | OK | ✅ | ❌ | - |
| PUT/DELETE /api/clinics/:id/users/:userId | OK | ✅ | ❌ | - |
| GET /api/clinics/:clinicId/professionals | OK | ❌ | ❌ | Não usado |
| GET /api/clinics/:id/stats | OK | ✅ | ❌ | - |
| GET /api/patients | OK | ✅ | via sync | - |
| GET /api/patients/count | OK | ✅ | ❌ | - |
| GET /api/patients/recent | OK | ❌ | ❌ | Não usado |
| GET /api/patients/:id | OK | ✅ | ❌ | - |
| POST /api/patients | OK | ✅ | ❌ | - |
| POST /api/patients/import | OK | ✅ | ❌ | - |
| PATCH /api/patients/:id | OK | ✅ | ❌ | - |
| DELETE /api/patients/:id | OK | ✅ | ❌ | - |
| GET /api/appointments | OK | ✅ | ❌ | - |
| GET /api/appointments/today | OK | ✅ | ❌ | - |
| GET /api/appointments/recent | OK | ❌ | ❌ | Não usado |
| GET /api/appointments/:id | OK | ✅ | ❌ | - |
| POST /api/appointments | OK | ✅ | ❌ | - |
| PUT /api/appointments/:id | OK | ✅ | ❌ | - |
| DELETE /api/appointments/:id | OK | ✅ | ❌ | - |
| POST /api/sessions | OK | ❌ | ❌ | Web só lê via paciente |
| GET /api/sessions | OK | ✅ | ❌ | - |
| GET /api/sessions/stats | OK | ✅ | ❌ | - |
| POST /api/payments | OK | ❌ | ❌ | Não usado no Web |
| GET /api/payments/patient/:id | OK | ✅ | ❌ | - |
| GET /api/financial/summary | OK | ✅ | ❌ | - |
| GET /api/financial/records | OK | ❌ | ❌ | Não usado |
| POST /api/financial/records | OK | ❌ | ❌ | Não usado |
| PATCH /api/financial/records/:id | OK | ❌ | ❌ | Não usado |
| DELETE /api/financial/records/:id | OK | ❌ | ❌ | Não usado |
| GET /api/financial/patient/:id | OK | ✅ | ❌ | - |
| GET /api/documents | OK | ✅ | ❌ | - |
| GET /api/insights | OK | ❌ | ❌ | Não usado |
| GET /api/insights/summary | OK | ❌ | ❌ | Não usado |
| PATCH /api/insights/:id/dismiss | OK | ❌ | ❌ | Não usado |
| GET /api/dashboard/metrics | OK | ❌ | ❌ | Web usa endpoints diretos |
| GET /api/dashboard/agenda-summary | OK | ❌ | ❌ | - |
| GET /api/dashboard/finance-summary | OK | ❌ | ❌ | - |
| GET /api/dashboard/count | OK | ❌ | ❌ | - |
| GET /api/dashboard/stats | OK | ❌ | ❌ | - |
| GET /api/dashboard/summary | OK | ❌ | ❌ | - |
| GET /api/reports | OK | ❌ | ❌ | Não usado |
| GET /api/reports/summary | OK | ❌ | ❌ | - |
| GET /api/reports/today | OK | ❌ | ❌ | - |
| GET /api/reports/stats | OK | ❌ | ❌ | - |
| GET /api/reports/count | OK | ❌ | ❌ | - |
| GET /api/sync/patients | OK | ✅ | ✅ | - |
| POST /api/sync/patients | OK | ✅ | ✅ | - |
| POST /api/beta/request | **FALTANDO** | ✅ | ❌ | **Backend não implementado** |
| GET /api/auth/beta-status | **FALTANDO** | (comentado) | ❌ | **Backend não implementado** |

---

## 11. AÇÕES RECOMENDADAS

### Críticas (afetam funcionamento)

1. **Corrigir dashboardService no Web**
   - Adicionar `getMetrics()` → GET /api/dashboard/metrics
   - Adicionar `getAgendaSummary()` → GET /api/dashboard/agenda-summary
   - Corrigir `getFinanceSummary()` em financeiro/page → usar `getFinancialSummary()` OU adicionar alias no service

2. **Implementar ou remover Beta**
   - Criar `POST /api/beta/request` e `GET /api/auth/beta-status` se o fluxo for necessário
   - Ou remover chamadas do BetaAccessForm

### Melhorias

3. **Conectar Web a /api/dashboard/***
   - Unificar uso: ou dashboardService usa /dashboard/*, ou manter endpoints granulares atuais de forma consistente

4. **Usar /api/clinics/:id/professionals**
   - Se houver página Profissionais no Web, consumir este endpoint

5. **Web chamar POST /api/auth/logout**
   - Para invalidar refresh tokens no servidor

6. **Página Financeiro: registros**
   - Se houver UI para CRUD de registros financeiros, conectar a `/api/financial/records`

---

**Relatório gerado automaticamente. Revisar e ajustar conforme prioridades do projeto.**
