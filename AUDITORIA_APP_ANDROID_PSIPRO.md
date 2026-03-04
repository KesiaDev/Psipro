# Auditoria Completa do Aplicativo Android PsiPro

**Data:** Março 2025  
**Objetivo:** Verificar sincronização entre app Android, backend e dashboard Web.

---

## 1. ARQUITETURA

### ViewModels

| Package | ViewModels |
|---------|------------|
| viewmodel | AuthViewModel, BackendLoginViewModel, BaseViewModel, DadosPessoaisViewModel, PatientNoteViewModel, ProntuarioViewModel, AppointmentViewModel |
| ui.viewmodels | PatientViewModel, AppointmentViewModel, AnotacaoSessaoViewModel, AutoavaliacaoViewModel, CobrancaAgendamentoViewModel, CobrancaSessaoViewModel, FinanceiroUnificadoViewModel, HistoricoFamiliarViewModel, HistoricoMedicoViewModel, ObservacoesClinicasViewModel, PatientNoteViewModel, PatientMessageViewModel, VidaEmocionalViewModel, DocumentoViewModel, ArquivoViewModel, TipoSessaoViewModel, AnamneseViewModel, PacienteSessoesViewModel, NotificationViewModel, AudioTranscriptionViewModel |
| ui.viewmodels.home | HomeViewModel |
| ui.schedule | ScheduleViewModel |

### Repositories

| Repository | Responsabilidade |
|------------|------------------|
| PatientRepository | CRUD pacientes local (Room) |
| AppointmentRepository | CRUD agendamentos local |
| AnotacaoSessaoRepository | Anotações de sessão |
| CobrancaSessaoRepository | Cobranças de sessão |
| CobrancaAgendamentoRepository | Cobranças de agendamento |
| DocumentoRepository | Documentos |
| FinancialRecordRepository | Registros financeiros |
| UserRepository | Usuário local |
| HistoricoFamiliarRepository, HistoricoMedicoRepository | Anamnese |
| ObservacoesClinicasRepository, VidaEmocionalRepository | Prontuário |
| NotificationRepository | Notificações |
| AuditLogRepository | Auditoria |
| Outros | Anamnese, Autoavaliacao, etc. |

### API Service

- **BackendApiService** (Retrofit) — `app/src/main/java/com/psipro/app/sync/api/BackendApiService.kt`
- Base URL debug: `http://10.0.2.2:3001/api`
- Base URL release: `https://psipro-production.up.railway.app/api` (ou NEXT_PUBLIC_API_URL equivalente)

### Room Database

- **AppDatabase** (v26) — `app/src/main/java/com/psipro/app/data/AppDatabase.kt`
- SQLite local: `psipro_database`
- **25+ entidades:** User, Patient, Appointment, PatientNote, PatientMessage, PatientReport, FinancialRecord, Prontuario, AuditLog, Anamnese*, Historico*, AnotacaoSessao, CobrancaSessao, CobrancaAgendamento, Documento, Arquivo, Notification, TipoSessao, Autoavaliacao, etc.
- **TypeConverters:** Date, EncryptionConverter (campos sensíveis)
- **SQLCipher** disponível no build.gradle (criptografia opcional)

---

## 2. APIs UTILIZADAS

| Método | Endpoint | Uso |
|--------|----------|-----|
| POST | /api/auth/login | Login |
| POST | /api/auth/register | Registro |
| POST | /api/auth/refresh | Renovar token |
| POST | /api/auth/logout | Logout (revogar refresh) |
| POST | /api/auth/switch-clinic | Trocar clínica ativa |
| GET | /api/auth/me | Dados do usuário, clinicId |
| GET | /api/sync/patients | Pull pacientes (clinicId, updatedAfter) |
| POST | /api/sync/patients | Push pacientes para backend |

**Total:** 8 endpoints.

**NÃO utilizados pelo app:**
- /api/patients (CRUD direto)
- /api/appointments
- /api/sessions
- /api/payments
- /api/financial
- /api/documents
- /api/clinics
- /api/dashboard
- /api/reports

---

## 3. SINCRONIZAÇÃO

### Pacientes (único fluxo sincronizado)

| Fluxo | Status | Observação |
|-------|--------|------------|
| Paciente criado no app → Web | ✅ | POST sync/patients → backend → Web lista via GET /patients |
| Paciente criado no Web → App | ✅ | App faz GET sync/patients (pull) |
| Paciente editado no app → Web | ✅ | POST sync/patients com updatedAt |
| Paciente editado no Web → App | ✅ | Pull com updatedAfter (watermark) |

**Triggers de sync:**
- App em foreground (ProcessLifecycleOwner)
- Após login
- Botão manual "Sincronizar pacientes" no menu

### Agendamentos

| Fluxo | Status | Observação |
|-------|--------|------------|
| Agendamento criado no app → Web | ❌ | **Não sincronizado** — só local |
| Agendamento criado no Web → App | ❌ | **Não sincronizado** |

### Sessões

| Fluxo | Status | Observação |
|-------|--------|------------|
| Sessão (AnotacaoSessao) no app → Web | ❌ | **Não sincronizado** |
| Sessão no Web → App | ❌ | **Não sincronizado** |

### Pagamentos / Cobranças

| Fluxo | Status | Observação |
|-------|--------|------------|
| CobrancaSessao/CobrancaAgendamento no app → Web | ❌ | **Não sincronizado** |
| Payment no Web → App | ❌ | **Não sincronizado** |

---

## 4. OFFLINE MODE

| Recurso | Implementado |
|---------|--------------|
| Room Database | ✅ 25+ entidades |
| Pacientes offline | ✅ dirty flag, lastSyncedAt |
| Sync ao voltar online | ✅ WorkManager (PatientsSyncWorker) |
| Conflitos | Backend é single source of truth; pull não sobrescreve dirty |
| Agendamentos offline | ✅ Só local — não sincroniza |
| Sessões offline | ✅ Só local — não sincroniza |

**Fluxo offline pacientes:**
1. Criar/editar paciente → `dirty=true`
2. Ao ter rede → WorkManager executa push (POST sync/patients)
3. Sucesso → `dirty=false`, `lastSyncedAt` atualizado
4. Pull (GET sync/patients) — atualiza locais que não estão dirty

---

## 5. LOGIN

| Item | App | Web | Backend |
|------|-----|-----|---------|
| Endpoint | POST /api/auth/login | POST /api/auth/login | ✅ |
| Retorno | accessToken, refreshToken, user | idem | ✅ |
| JWT | Bearer no header | Bearer no header | ✅ |
| X-Clinic-Id | AuthInterceptor adiciona | api.ts adiciona | ClinicGuard valida |
| Refresh | POST /api/auth/refresh | POST /api/auth/refresh | ✅ |
| Logout | POST /api/auth/logout | Não chama backend | ✅ |

**Sistema de login:** Idêntico ao Web (mesmo backend, mesmos endpoints).

**Fluxo no app:**
1. SplashActivity → se token → GET auth/me → Dashboard
2. MainActivity → formulário → BackendAuthManager.login() → POST auth/login
3. TokenManager salva tokens (EncryptedSharedPreferences)
4. ensureClinicId() via GET auth/me
5. PatientsSyncScheduler.enqueue("login")

---

## 6. MODELOS DE DADOS

### Patient (App vs Backend)

| Campo App (Patient.kt) | Campo Backend (Prisma) | Observação |
|------------------------|------------------------|------------|
| id (Long, PK local) | - | App only |
| uuid | id (String UUID) | Mapeado no sync |
| name | name | ✅ |
| cpf | cpf | ✅ |
| birthDate | birthDate | ✅ |
| phone | phone | ✅ |
| email | email | ✅ |
| endereco, cep, numero, bairro, cidade, estado, complemento | address (string única) | App concatena para address no payload |
| notes | observations | ✅ |
| status | status | ✅ |
| origin | origin | ✅ |
| sharedWith | sharedWith | ✅ |
| dirty, lastSyncedAt | - | Só app (controle de sync) |
| sessionValue, diaCobranca, lembreteCobranca | - | App only |
| clinicalHistory, medications, allergies | - | App only (criptografado local) |

### Appointment (App vs Backend)

| Campo App | Campo Backend | Observação |
|-----------|---------------|------------|
| date, startTime, endTime | scheduledAt, duration | Modelo diferente |
| patientId (Long) | patientId (String UUID) | FK diferente |
| recurrence, valorCobranca, etc. | - | App only |
| **Sync** | **Nenhum** | Agendamentos não sincronizam |

### Session

| App | Backend | Observação |
|-----|---------|------------|
| AnotacaoSessao (notas) | Session (date, duration, status, notes) | Conceito similar, estrutura diferente |
| CobrancaSessao (cobrança) | Payment | Conceito similar, sem sync |

### Professional / User

| App (User.kt local) | Backend (Prisma User) | Observação |
|---------------------|------------------------|------------|
| id (Long), email, name, crp | id (UUID), email, name, license | Profissional vem de auth/me, não de modelo local de "Professional" |
| - | ClinicUser (role) | Backend tem profissionais via ClinicUser |

**Resumo:** Patient está alinhado para sync. Appointment, Session e Payment têm modelos locais diferentes e **não sincronizam**.

---

## 7. TESTE DE SINCRONIZAÇÃO (Simulação)

| Fluxo | Resultado esperado |
|-------|-------------------|
| Criar paciente no app | ✅ Aparece no Web após sync (POST → backend → Web lista) |
| Criar paciente no Web | ✅ Aparece no app no próximo pull |
| Criar agendamento no Web | ❌ **Não aparece no app** — sem sync |
| Criar agendamento no app | ❌ **Não aparece no Web** — sem sync |
| Registrar sessão no app | ❌ **Não aparece no Web** — sem sync |
| Registrar sessão no Web | ❌ **Não aparece no app** — sem sync |
| Cobrança no app | ❌ **Não aparece no Web** — sem sync |
| Pagamento no Web | ❌ **Não aparece no app** — sem sync |

---

## 8. PROBLEMAS

### Críticos
1. **Agendamentos não sincronizam** — App e Web usam modelos diferentes; backend tem endpoint, app não chama.
2. **Sessões não sincronizam** — AnotacaoSessao/CobrancaSessao vs Session/Payment; sem integração.
3. **Pagamentos não sincronizam** — CobrancaSessao, CobrancaAgendamento vs Payment.

### Médios
4. **SyncPatientsRequest.clinicId** — App envia clinicId no payload de cada paciente; backend usa header X-Clinic-Id (compatível).
5. **Roles sync** — Backend SyncController exige `@Roles('admin', 'psychologist')`; assistant pode não conseguir sincronizar.
6. **AuthManager legado** — Existe AuthManager com login hardcoded (admin@teste.com); pode causar confusão se usado em algum fluxo.

### Menores
7. **BackendLoginActivity** — Existe mas não está no AndroidManifest; fluxo usa MainActivity.
8. **Endereço** — App usa campos separados; backend usa string; mapeamento na concatenação pode perder dados.
9. **status/type em SyncPatientPayload** — Incluídos; alinhado com backend.

---

## 9. RELATÓRIO FINAL (Tabela)

| Funcionalidade | App | Backend | Web | Status |
|----------------|-----|---------|-----|--------|
| Login | ✅ | ✅ | ✅ | OK |
| Registro | ✅ | ✅ | ✅ | OK |
| Refresh Token | ✅ | ✅ | ✅ | OK |
| Logout | ✅ | ✅ | ❌ (não chama) | Parcial |
| Trocar clínica | ✅ | ✅ | ✅ | OK |
| Auth/me | ✅ | ✅ | ✅ | OK |
| **Pacientes – criar** | ✅ | ✅ (sync) | ✅ | **Sincronizado** |
| **Pacientes – listar** | ✅ | ✅ (sync) | ✅ | **Sincronizado** |
| **Pacientes – editar** | ✅ | ✅ (sync) | ✅ | **Sincronizado** |
| **Pacientes – import Excel** | ❌ | ✅ | ✅ | Só Web |
| **Agendamentos – criar** | ✅ local | ✅ | ✅ | **Não sincronizado** |
| **Agendamentos – listar** | ✅ local | ✅ | ✅ | **Não sincronizado** |
| **Sessões – registrar** | ✅ local | ✅ | ✅ | **Não sincronizado** |
| **Pagamentos/cobranças** | ✅ local | ✅ | ✅ | **Não sincronizado** |
| Documentos | ✅ local | ✅ | ✅ | App não sincroniza |
| Financeiro | ✅ local | ✅ | ✅ | App não sincroniza |
| Relatórios | ❌ | ✅ | ✅ | Só Web |
| Dashboard | ❌ | ✅ | ✅ | Só Web |
| Clínicas CRUD | ❌ | ✅ | ✅ | Só Web |
| Profissionais | ❌ | ✅ | ✅ | Só Web |

---

## 10. AÇÕES RECOMENDADAS

### Prioridade alta
1. **Implementar sync de Appointments** — Endpoints existem no backend; app precisa:
   - Adicionar POST/GET sync/appointments ou usar /api/appointments
   - Mapear modelo local (date, startTime, endTime) ↔ backend (scheduledAt, duration)

2. **Implementar sync de Sessions** — Mapear AnotacaoSessao ↔ Session e CobrancaSessao ↔ Payment

3. **Implementar sync de Payments** — CobrancaSessao/CobrancaAgendamento ↔ Payment

### Prioridade média
4. **Incluir assistant no sync** — Revisar `@Roles('admin', 'psychologist')` no SyncController se assistants precisarem sync

5. **Remover ou unificar AuthManager** — Evitar dois fluxos de login (legado vs backend)

6. **Documentar BackendLoginActivity** — Remover ou ativar se for necessário

### Prioridade baixa
7. **Web chamar POST /api/auth/logout** — Para invalidar tokens no servidor
8. **Melhorar mapeamento de endereço** — Evitar perda de dados na concatenação

---

**Relatório gerado automaticamente. Revisar conforme prioridades do projeto.**
