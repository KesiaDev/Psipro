# Relatório de Auditoria – Integração PsiPro

**Data:** 03/03/2025  
**Objetivo:** Garantir que PsiPro Mobile, Backend API e Dashboard Web funcionem como um único sistema sincronizado.

---

## 1. Mapeamento de Endpoints do Backend

Todos os endpoints abaixo estão sob o prefixo global `/api`.

### Auth (`/api/auth/*`)

| Método | Caminho | Descrição |
|--------|---------|-----------|
| POST | `/auth/login` | Login |
| POST | `/auth/logout` | Logout |
| POST | `/auth/refresh` | Refresh token |
| POST | `/auth/register` | Registro |
| POST | `/auth/forgot-password` | Recuperação de senha |
| POST | `/auth/handoff` | SSO Android → Web |
| POST | `/auth/switch-clinic` | Troca de clínica ativa |
| GET | `/auth/me` | Perfil do usuário autenticado |

### Pacientes (`/api/patients`)

| Método | Caminho | Descrição |
|--------|---------|-----------|
| GET | `/patients` | Listar |
| GET | `/patients/count` | Contagem |
| GET | `/patients/recent` | Recentes |
| GET | `/patients/:id` | Buscar por ID |
| GET | `/patients/:id/patterns` | Padrões do paciente |
| POST | `/patients` | Criar |
| POST | `/patients/import` | Importar Excel |
| PATCH | `/patients/:id` | Atualizar |
| DELETE | `/patients/:id` | Excluir |

### Agendamentos (`/api/appointments`)

| Método | Caminho | Descrição |
|--------|---------|-----------|
| GET | `/appointments` | Listar |
| GET | `/appointments/today` | Consultas de hoje |
| GET | `/appointments/recent` | Recentes |
| GET | `/appointments/:id` | Buscar por ID |
| POST | `/appointments` | Criar |
| PUT | `/appointments/:id` | Atualizar |
| DELETE | `/appointments/:id` | Excluir |

### Sessões (`/api/sessions`)

| Método | Caminho | Descrição |
|--------|---------|-----------|
| POST | `/sessions/voice-note` | Atualizar transcrição |
| POST | `/sessions` | Criar |
| GET | `/sessions/stats` | Estatísticas |
| GET | `/sessions` | Listar (com ?patientId) |
| GET | `/sessions/:id` | Buscar por ID |
| PATCH | `/sessions/:id` | Atualizar |
| DELETE | `/sessions/:id` | Excluir |

### Financeiro (`/api/financial`)

| Método | Caminho | Descrição |
|--------|---------|-----------|
| GET | `/financial/summary` | Resumo geral |
| GET | `/financial/records` | Listar registros |
| POST | `/financial/records` | Criar registro |
| PATCH | `/financial/records/:id` | Atualizar registro |
| DELETE | `/financial/records/:id` | Excluir registro |
| GET | `/financial/patient/:patientId` | Financeiro por paciente |

### Clínicas (`/api/clinics`)

| Método | Caminho | Descrição |
|--------|---------|-----------|
| POST | `/clinics` | Criar |
| GET | `/clinics` | Listar |
| GET | `/clinics/:id` | Buscar por ID |
| PUT | `/clinics/:id` | Atualizar |
| PATCH | `/clinics/:id/status` | Atualizar status |
| DELETE | `/clinics/:id` | Excluir |
| POST | `/clinics/:id/invite` | Convidar usuário |
| PUT | `/clinics/:id/users/:userId` | Atualizar membro |
| DELETE | `/clinics/:id/users/:userId` | Remover membro |
| GET | `/clinics/:clinicId/professionals` | Profissionais |
| GET | `/clinics/:id/stats` | Estatísticas |

### Relatórios (`/api/reports`)

| Método | Caminho | Descrição |
|--------|---------|-----------|
| GET | `/reports` | Dados completos |
| GET | `/reports/summary` | Resumo |
| GET | `/reports/today` | Sessões de hoje |
| GET | `/reports/stats` | Estatísticas |
| GET | `/reports/count` | Contagem |

### Dashboard (`/api/dashboard`)

| Método | Caminho | Descrição |
|--------|---------|-----------|
| GET | `/dashboard/metrics` | Métricas |
| GET | `/dashboard/agenda-summary` | Resumo da agenda |
| GET | `/dashboard/finance-summary` | Resumo financeiro |
| GET | `/dashboard/count` | Contagem |
| GET | `/dashboard/stats` | Estatísticas |
| GET | `/dashboard/summary` | Resumo geral |

### Sync (`/api/sync/*`) – Usado principalmente pelo App

| Método | Caminho | Descrição |
|--------|---------|-----------|
| GET | `/sync/patients` | Pull pacientes |
| POST | `/sync/patients` | Push pacientes |
| GET | `/sync/appointments` | Pull agendamentos |
| POST | `/sync/appointments` | Push agendamentos |
| GET | `/sync/sessions` | Pull sessões |
| POST | `/sync/sessions` | Push sessões |
| GET | `/sync/payments` | Pull pagamentos |
| POST | `/sync/payments` | Push pagamentos |
| GET | `/sync/documents` | Pull documentos |
| POST | `/sync/documents` | Push documentos |

### Outros

| Método | Caminho | Descrição |
|--------|---------|-----------|
| GET | `/clinics` | App usa para listar clínicas |
| POST | `/voice/transcribe` | Transcrição de voz |
| GET | `/documents` | Documentos |
| GET | `/insights` | Insights |
| GET | `/insights/summary` | Resumo insights |
| PATCH | `/insights/:id/dismiss` | Dispensar insight |
| POST | `/beta/request` | Solicitar acesso beta |
| POST | `/payments` | Criar pagamento |
| GET | `/payments/patient/:patientId` | Pagamentos do paciente |

---

## 2. Chamadas de API no Dashboard Web

### Serviços e Endpoints Utilizados

| Serviço | Endpoints |
|---------|-----------|
| **authService** | POST /auth/login, POST /auth/register, POST /auth/refresh, POST /auth/switch-clinic, GET /auth/me |
| **patientService** | GET /patients, GET /patients/:id, POST /patients, PATCH /patients/:id, DELETE /patients/:id, POST /sync/patients, POST /patients/import |
| **appointmentService** | GET /appointments, GET /appointments/:id, POST /appointments, PUT /appointments/:id, DELETE /appointments/:id |
| **sessionService** | GET /sessions?patientId= |
| **financialService** | GET /financial/summary, GET /financial/patient/:patientId |
| **clinicService** | GET /clinics, GET /clinics/:id, POST /clinics, PUT /clinics/:id, DELETE /clinics/:id, POST /clinics/:id/invite, PUT /clinics/:id/users/:userId, DELETE /clinics/:id/users/:userId, GET /clinics/:id/stats |
| **dashboardService** | GET /patients/count, GET /appointments/today, GET /sessions/stats, GET /financial/summary, GET /dashboard/finance-summary, GET /dashboard/metrics, GET /dashboard/agenda-summary |
| **reportsService** | GET /reports, GET /reports/summary, GET /reports/today, GET /reports/stats |
| **anamneseService** | GET /anamnese/patients/:patientId/preenchidas |
| **BetaAccessForm** | POST /beta/request |
| **login/handoff** | POST /auth/handoff (fetch direto) |
| **forgot-password** | POST /auth/forgot-password (fetch direto) |

### Web **não** utiliza

- `/financial/records` (CRUD de registros financeiros)
- `/sync/appointments`, `/sync/sessions`, `/sync/payments`, `/sync/documents` (usa CRUD principal)
- `/auth/beta-status` (não existe no backend; referência em doc)

---

## 3. Chamadas de API no App Android

### BackendApiService.kt

| Método | Endpoint | Uso |
|--------|----------|-----|
| POST | auth/login | Login |
| POST | auth/register | Registro |
| POST | auth/refresh | Refresh token |
| POST | auth/logout | Logout |
| POST | auth/switch-clinic | Troca de clínica |
| GET | auth/me | Identidade e clinicId |
| GET | clinics | Listar clínicas |
| GET | sync/patients | Pull pacientes |
| POST | sync/patients | Push pacientes |
| GET | sync/appointments | Pull agendamentos |
| POST | sync/appointments | Push agendamentos |
| GET | sync/sessions | Pull sessões |
| POST | sync/sessions | Push sessões |
| GET | sync/payments | Pull pagamentos |
| POST | sync/payments | Push pagamentos |
| GET | sync/documents | Pull documentos |
| POST | sync/documents | Push documentos |
| POST | voice/transcribe | Transcrição |
| POST | sessions | Criar sessão |
| POST | sessions/voice-note | Atualizar transcrição |

### App **não** utiliza

- `/patients`, `/appointments`, `/sessions` (CRUD principal) – usa somente sync
- `/financial/*`
- `/reports/*`
- `/dashboard/*`
- `/clinics/:id` (somente GET /clinics)

---

## 4. Tabela Comparativa de Integração

| Endpoint | Backend | Web | App |
|----------|---------|-----|-----|
| POST /auth/login | ✅ | ✅ | ✅ |
| POST /auth/register | ✅ | ✅ | ✅ |
| POST /auth/refresh | ✅ | ✅ | ✅ |
| POST /auth/logout | ✅ | ❌* | ✅ |
| POST /auth/handoff | ✅ | ✅ | ✅ |
| POST /auth/switch-clinic | ✅ | ✅ | ✅ |
| POST /auth/forgot-password | ✅ | ✅ | ❌ |
| GET /auth/me | ✅ | ✅ | ✅ |
| GET /clinics | ✅ | ✅ | ✅ |
| GET /clinics/:id | ✅ | ✅ | ❌ |
| POST /clinics | ✅ | ✅ | ❌ |
| PUT /clinics/:id | ✅ | ✅ | ❌ |
| DELETE /clinics/:id | ✅ | ✅ | ❌ |
| GET /clinics/:id/stats | ✅ | ✅ | ❌ |
| GET /patients | ✅ | ✅ | ❌ |
| GET /patients/:id | ✅ | ✅ | ❌ |
| GET /patients/count | ✅ | ✅ | ❌ |
| POST /patients | ✅ | ✅ | ❌ |
| PATCH /patients/:id | ✅ | ✅ | ❌ |
| DELETE /patients/:id | ✅ | ✅ | ❌ |
| POST /patients/import | ✅ | ✅ | ❌ |
| GET /sync/patients | ✅ | ✅ | ✅ |
| POST /sync/patients | ✅ | ✅ | ✅ |
| GET /appointments | ✅ | ✅ | ❌ |
| GET /appointments/:id | ✅ | ✅ | ❌ |
| GET /appointments/today | ✅ | ✅ | ❌ |
| POST /appointments | ✅ | ✅ | ❌ |
| PUT /appointments/:id | ✅ | ✅ | ❌ |
| DELETE /appointments/:id | ✅ | ✅ | ❌ |
| GET /sync/appointments | ✅ | ❌ | ✅ |
| POST /sync/appointments | ✅ | ❌ | ✅ |
| GET /sessions | ✅ | ✅ | ❌ |
| GET /sessions/stats | ✅ | ✅ | ❌ |
| POST /sessions | ✅ | ❌ | ✅ |
| POST /sessions/voice-note | ✅ | ❌ | ✅ |
| GET /sync/sessions | ✅ | ❌ | ✅ |
| POST /sync/sessions | ✅ | ❌ | ✅ |
| GET /financial/summary | ✅ | ✅ | ❌ |
| GET /financial/patient/:id | ✅ | ✅ | ❌ |
| GET /financial/records | ✅ | ❌ | ❌ |
| POST /financial/records | ✅ | ❌ | ❌ |
| GET /dashboard/* | ✅ | ✅ | ❌ |
| GET /reports/* | ✅ | ✅ | ❌ |
| GET /sync/payments | ✅ | ❌ | ✅ |
| POST /sync/payments | ✅ | ❌ | ✅ |
| GET /sync/documents | ✅ | ❌ | ✅ |
| POST /sync/documents | ✅ | ❌ | ✅ |
| POST /voice/transcribe | ✅ | ❌ | ✅ |
| POST /beta/request | ✅ | ✅ | ❌ |

\* Web chama logout via `fetch` direto em AuthContext, não via `api`.

---

## 5. Sincronização entre Sistemas

### Fluxo de dados

| Recurso | Web cria → App vê | App cria → Web vê |
|---------|-------------------|-------------------|
| **Pacientes** | ✅ via sync pull | ✅ via sync push + web GET /patients |
| **Agendamentos** | ✅ via sync pull | ✅ via sync push + web GET /appointments |
| **Sessões** | ✅ via sync pull | ✅ via sync push |
| **Pagamentos** | ✅ via sync pull | ✅ via sync push |
| **Documentos** | ✅ via sync pull | ✅ via sync push |

### Observações

- Web usa CRUD `/patients`, `/appointments`, etc. (escrita direta no backend).
- App usa sync: Room como cache, push de dados locais e pull periódico.
- Backend é a fonte única de verdade; Web e App leem/escrevem nas mesmas tabelas.

### Sincronização mobile

- App envia dados via `POST /sync/*` (patients, appointments, sessions, payments, documents).
- App busca dados via `GET /sync/*`.
- Room é cache local; o backend é a fonte autoritativa.
- `AuthInterceptor` envia `Authorization: Bearer` e `X-Clinic-Id`.

---

## 6. Headers obrigatórios

### Backend

- `ClinicGuard`: exige header `x-clinic-id` em rotas que o usam.
- Rotas com `ClinicGuard`: patients, appointments, sessions, financial, dashboard, reports, sync.

### App (AuthInterceptor)

- `Authorization: Bearer <token>` em todos os endpoints exceto login, register, refresh, logout.
- `X-Clinic-Id` quando `clinicId` está no `BackendSessionStore`.

### Web (api.ts)

- `Authorization: Bearer <token>` quando token existe.
- `X-Clinic-Id` quando `active_clinic_id` ou `psipro_current_clinic_id` existem no localStorage.

### Observação

O refresh token no web (`authService.refreshToken`) usa `fetch` direto e não passa `X-Clinic-Id`; o endpoint `/auth/refresh` não usa `ClinicGuard`, então está correto.

---

## 7. Modelos de dados

### Patient

| Backend (Prisma) | Web (patientService) | App (RemotePatient) |
|------------------|----------------------|--------------------|
| id (uuid) | id (string) | id (string) |
| name | name | name |
| birthDate | birthDate | birthDate |
| cpf, phone, email | cpf, phone, email | cpf, phone, email |
| clinicId | clinicId | clinicId |
| status, type | status, type | status, type |
| updatedAt | updatedAt | updatedAt |

Alinhados.

### Appointment

| Backend | Web | App (RemoteAppointment) |
|---------|-----|-------------------------|
| id | id | id |
| patientId | patientId | patientId |
| userId | userId | userId |
| scheduledAt | scheduledAt | scheduledAt |
| duration | duration | duration |
| status, type, notes | status, type, notes | status, type, notes |
| patient (embed) | patient (embed) | patient (embed) |

Alinhados; app mapeia `professionalId` ↔ `userId`.

### Session

| Backend | Web | App |
|---------|-----|-----|
| id, patientId, date | id, patientId, date | id, patientId, date |
| duration, status, notes | duration, status, notes | duration, status, notes |

Alinhados.

### FinancialRecord / Payment

- Web usa `FinancialSummary` e `PatientFinancialSummary` (agregados).
- App usa `RemotePayment` no sync.
- Backend tem `FinancialRecord` e `Payment`; CRUD de registros financeiros existe, mas não é usado pelo web no serviço atual.

---

## 8. Erros e inconsistências

### Críticos

- Nenhum endpoint crítico inexistente ou 404 identificado.

### Menores

1. **GET /auth/beta-status** – Documentado em FLUXO_AUTORIZACAO_BETA.md, mas não implementado no backend. BetaAccessGate usa código comentado; impacto baixo.
2. **Web e financial records** – Endpoints `/financial/records` (POST, PATCH, DELETE) existem, mas o `financialService` do web não os usa. Se houver UI de CRUD financeiro no web, ela precisa ser conectada a esses endpoints.

### IDs

- Backend: Patient, Appointment, Session, Clinic usam `String` (uuid).
- Web: trata ids como string.
- App: usa `Long` localmente no Room e mapeia para `backendId` (string/uuid) no sync.

---

## 9. Resumo das verificações

| Item | Status |
|------|--------|
| Endpoints mapeados | ✅ Concluído |
| Web usa API corretamente | ✅ |
| App usa sync (push + pull) | ✅ |
| Room como cache | ✅ |
| Authorization Bearer | ✅ App e Web |
| X-Clinic-Id | ✅ App e Web (quando aplicável) |
| Modelos alinhados | ✅ Patient, Appointment, Session |
| APP cria → WEB vê | ✅ Via backend |
| WEB cria → APP vê | ✅ Via sync pull |

---

## 10. Conclusão e Recomendações

### Estado atual

APP, WEB e BACKEND estão integrados e sincronizados nos fluxos principais:

- Autenticação compartilhada (login, refresh, handoff).
- Pacientes e agendamentos sincronizados entre Web e App via backend.
- Sessões e pagamentos sincronizados via sync.
- Headers obrigatórios enviados corretamente.

### Recomendações

1. **Implementar GET /auth/beta-status** – Se o fluxo beta for retomado, criar o endpoint para manter consistência com a documentação.
2. **Conectar Web ao CRUD financeiro** – Se existir ou for planejada interface de registros financeiros, integrar aos endpoints `/financial/records`.
3. **Testes E2E** – Criar cenários que cubram: criar paciente no App → ver no Web; criar agendamento no Web → ver no App; trocar de clínica e verificar dados corretos.

---

*Relatório gerado em 03/03/2025.*
