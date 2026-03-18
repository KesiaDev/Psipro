# PsiPro – Tudo que já está construído e implementado

> Documento para suporte a decisões inteligentes de IA (ChatGPT, Cursor, etc.). Atualizado em 18/03/2026.

---

## 1. Stack e infraestrutura

### App Android
- **Linguagem:** Kotlin
- **UI:** Jetpack Compose + Material3, XML (legado)
- **Arquitetura:** MVVM, Repository pattern
- **DI:** Hilt
- **Cache:** Room (app_pisc_database)
- **Rede:** Retrofit (BackendApiService)
- **Sync:** WorkManager (PatientsSyncWorker, AppointmentSyncWorker, SessionSyncWorker, PaymentSyncWorker)
- **Segurança:** EncryptedSharedPreferences, SQLCipher, biometria (SecureActivity)

### Backend
- **Framework:** NestJS (Node.js)
- **ORM:** Prisma
- **Auth:** JWT (access + refresh), bcrypt
- **Deploy:** Railway
- **Base URL produção:** `https://psipro-backend-production.up.railway.app/api`
- **Web produção:** `https://psipro-dashboard-production.up.railway.app`

---

## 2. Autenticação (implementado)

| Item | Status | Onde |
|------|--------|------|
| Login email/senha | ✅ | MainActivity, BackendAuthManager |
| Registro | ✅ | CreateAccountActivity |
| Refresh token | ✅ | RefreshInterceptor, AuthInterceptor |
| Logout | ✅ | BackendAuthManager |
| Handoff App→Web | ✅ | WebNavigator, /auth/handoff |
| Troca de clínica | ✅ | BackendAuthManager.ensureClinicId() |
| Headers obrigatórios | ✅ | Authorization: Bearer, x-clinic-id |
| Token seguro | ✅ | EncryptedSharedPreferences, BackendSessionStore |

---

## 3. Telas e fluxos principais

### Launch e navegação
- **SplashActivity** (LAUNCHER) → verifica BackendAuthManager → Main ou Dashboard
- **MainActivity** → login (API), termo LGPD
- **DashboardActivity** → Drawer + NavHostFragment
- **Fragmentos:** Home, Agenda, Pacientes, Financeiro, Notificações, Autoavaliação, Configurações, Suporte, Aniversariantes

### Pacientes
- Lista: PatientsFragment, PatientListActivity, PatientListScreen
- Detalhes: DetalhePacienteActivity
- Cadastro: CadastroPacienteActivity
- Repositório: PatientRepository (Room + sync)

### Agenda e consultas
- Agenda: ScheduleFragment, WeeklyAgendaScreen, AppointmentScheduleActivity
- Lista: AppointmentListActivity
- Sync: SyncAppointmentsManager, SyncAppointmentsController

### Sessões
- Nova sessão: NovaSessaoActivity, QuickSessionActivity
- Anotações: AnotacoesSessaoActivity, AnotacoesSessaoScreen
- Sync: SyncSessionsManager, POST /sessions/voice-note (transcrição)

### Anamnese
- Formulários: AnamneseActivity, AnamneseFormScreen, SimplifiedAnamneseScreen, FormularioAnamneseScreen
- Modelos: AnamneseModel, AnamneseCampo, AnamnesePreenchida
- DAOs: AnamneseModelDao, AnamneseCampoDao, AnamnesePreenchidaDao

### Documentos e arquivos
- Documentos: DocumentosActivity, DocumentosScreen, DocumentoViewModel
- Arquivos: ArquivosActivity, ArquivoViewModel
- Tipos: Termo consentimento, confidencialidade, encaminhamento, etc.
- Sync: SyncDocumentsManager, sync/documents

### Financeiro
- Cobranças: CobrancaSessaoViewModel, CobrancaAgendamentoViewModel
- Registros: FinancialRecordRepository
- Sync: SyncPaymentsManager, sync/payments

### Perfil e config
- Perfil: EditProfileActivity, DadosPessoaisActivity
- Segurança: SecuritySettingsActivity
- Política de privacidade: PrivacyPolicyActivity
- Ajuda: HelpActivity

### LGPD (integrado com backend)
- Termo de consentimento obrigatório antes do dashboard
- LgpdConsentActivity: tela bloqueante com checkbox + Aceito
- POST /auth/consent envia aceite ao backend (User.lgpdAcceptedAt)
- Persistência local: BackendSessionStore + settings aceitou_lgpd
- Splash/Main: verifica consent antes de liberar dashboard
- Offline: salva local, prossegue (evita bloquear sem rede)

---

## 4. Sincronização (offline-first)

### WorkManager
- **SyncScheduler.enqueueBoth()** → encadeia Patients + Appointments + Sessions + Payments
- Workers: PatientsSyncWorker, AppointmentSyncWorker, SessionSyncWorker, PaymentSyncWorker
- Gatilhos: login, foreground, manual, patient_created, agenda_screen, etc.

### Fluxo
1. Salvar local (Room) → dirty=1
2. WorkManager executa sync
3. Push: enviar itens dirty
4. Pull: buscar do backend (updatedAfter)
5. Marcar synced (dirty=0, lastSyncedAt)

### Endpoints de sync
| Método | Endpoint | Uso |
|--------|----------|-----|
| POST | sync/patients | Enviar pacientes |
| GET | sync/patients?clinicId=&updatedAfter= | Puxar pacientes |
| POST | sync/appointments | Enviar agendamentos |
| GET | sync/appointments?clinicId=&updatedAfter= | Puxar agendamentos |
| POST | sync/sessions | Enviar sessões |
| GET | sync/sessions?clinicId=&updatedAfter= | Puxar sessões |
| POST | sync/payments | Enviar pagamentos |
| GET | sync/payments?clinicId=&updatedAfter= | Puxar pagamentos |
| POST | sync/documents | Enviar documentos |
| GET | sync/documents?clinicId=&updatedAfter= | Puxar documentos |

### Headers sync
- Authorization: Bearer &lt;token&gt;
- x-clinic-id: &lt;clinicId&gt;

---

## 5. Entidades Room (cache local)

- Patient, User, Appointment, Notification
- PatientNote, PatientMessage, PatientReport
- AnotacaoSessao, CobrancaSessao, CobrancaAgendamento, TipoSessao
- Prontuario, Documento, Arquivo
- Autoavaliacao, VidaEmocional, HistoricoMedico, HistoricoFamiliar, ObservacoesClinicas
- FinancialRecord, AuditLog, AnamneseModel, AnamneseCampo, AnamnesePreenchida
- PatientCache, AppointmentCache (cache)

### Campos de sync nas entidades
- backendId, dirty, lastSyncedAt (patients, appointments, anotacoes_sessao, cobrancas_sessao, documentos)

---

## 6. Backend – Controllers e endpoints principais

### Auth
- POST /auth/login, register, refresh, logout, forgot-password
- GET /auth/me
- POST /auth/handoff, switch-clinic

### CRUD
- /patients, /appointments, /sessions, /payments
- /financial, /documents, /anamnese
- /clinics, /users, /professionals

### Sync
- /sync/patients, /sync/appointments, /sync/sessions, /sync/payments, /sync/documents

### Extras
- /dashboard, /insights, /reports
- /voice/transcribe, /voice/summarize, /voice/insights
- /patients/:id/patterns, /patients/:id/emotional-evolution

---

## 7. IA e análises (backend)

- **VoiceService:** transcrição (Whisper), insights (OpenAI)
- **Insights:** summary, themes, emotions, actionItems, riskFlags
- **Padrões:** PatientPatternsService (temas, emoções, alertas)
- **Evolução emocional:** EmotionalEvolutionService
- **Entradas da IA:** session.notes, patient.anamnesis, patient.observations

---

## 8. ViewModels e Repositories (Hilt)

### ViewModels
BackendLoginViewModel, PatientViewModel, AppointmentViewModel, AnotacaoSessaoViewModel, DocumentoViewModel, ArquivoViewModel, CobrancaSessaoViewModel, CobrancaAgendamentoViewModel, AnamneseViewModel, AutoavaliacaoViewModel, HistoricoMedicoViewModel, HistoricoFamiliarViewModel, VidaEmocionalViewModel, ObservacoesClinicasViewModel, ScheduleViewModel, HomeViewModel, NotificationViewModel, ProntuarioViewModel, QuickSessionViewModel, etc.

### Repositories
PatientRepository, AppointmentRepository, AnotacaoSessaoRepository, DocumentoRepository, ArquivoRepository, AutoavaliacaoRepository, etc.

---

## 9. Firebase (parcial)

- firebase-firestore, firebase-storage, firebase-messaging
- Auto-init desabilitado no AndroidManifest (evitar crash com config inválida)
- NoteEditActivity usa Firebase Storage/Firestore para upload de áudio
- google-services.json em .gitignore (não versionado)

---

## 10. Configurações de build

### BuildConfig
- PSIPRO_API_BASE_URL (produção)
- PSIPRO_WEB_BASE_URL (produção)
- Em emulador/local: usar http://10.0.2.2:3001/api

### Versões
- Room: 2.6.1
- Hilt: 2.48
- Compose: 1.5.10
- minSdk: 26, targetSdk: 34

---

## 11. Pendências e melhorias conhecidas

1. **Firebase:** Configurar corretamente ou remover dependência
2. **Testes:** Validar abertura, login, navegação (estrutura em androidTest)
3. **Retry automático:** Reforçar em falhas de rede
4. **Logs:** Padronizar erros de sync e API

---

## 12. Documentação de referência

- ARQUITETURA_APP.md – fluxos e camadas
- backend/API.md – endpoints
- docs/ESPECIFICACAO_INTEGRACAO_APP_WEB.md – handoff e sync
- .cursor/rules/psipro-android.mdc – regras técnicas
