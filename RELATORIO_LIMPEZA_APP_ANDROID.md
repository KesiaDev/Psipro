# Relatório de Limpeza Estratégica — App Android PsiPro

**Data:** 2025  
**Objetivo:** Transformar o app em companion app focado em uso diário do psicólogo.  
**Status:** Fases 1–2 concluídas | Fase 4 parcial

---

## Resumo executivo

A limpeza removeu módulos administrativos e de integração que passam a existir na versão Web (Lovable).  
O app está focado em: Login, Agenda, Pacientes, Sessões, Registro de evolução, Pagamentos e Notificações.

**Compilação:** ✅ BUILD SUCCESSFUL

---

## FASE 1 — REMOVIDO DO APP

### 1.1 Integrações WhatsApp

| Item | Status |
|------|--------|
| WhatsAppHistoryActivity | Deletado |
| WhatsAppDialogActivity | Deletado |
| WhatsAppHistoryScreen (Compose) | Deletado |
| WhatsAppMensagemDialog | Deletado |
| WhatsAppService | Deletado |
| WhatsAppConversationRepository | Deletado |
| WhatsAppConversationAdapter (adapter/ + ui/) | Deletado |
| WhatsAppReminderReceiver | Deletado |
| WhatsAppUtils | Deletado |
| LembreteCobrancaWorker | Simplificado (abre Financeiro, não WhatsApp) |
| Menu "WhatsApp" em DetalhePacienteActivity | Removido |
| Botão "Contato" em HomeScreen | Alterado para abrir discador (tel:) |

**Mantido no banco:** `WhatsAppConversation` (entity + DAO) — tabela não usada, sem migration.

### 1.2 Relatórios e exportações

| Item | Status |
|------|--------|
| ReportsActivity | Deletado |
| PatientReportsActivity | Deletado |
| AppointmentReportActivity | Deletado |
| AppointmentReportListActivity | Deletado |
| AppointmentReportListFragment | Deletado |
| ReportViewModel | Deletado |
| PatientReportViewModel | Deletado |
| AppointmentReportListViewModel | Deletado |
| AppointmentReportListViewModelFactory | Deletado |
| AppointmentReportRepository | Deletado |
| AppointmentReportGenerator | Deletado |
| PdfReportExporter | Deletado |
| PatientReportAdapter | Deletado |
| PatientReportDialog | Deletado |
| AppointmentReportAdapter | Deletado |
| AppointmentReportListAdapter | Deletado |
| AppointmentReportDialog | Deletado |
| Importação Excel (PatientsFragment) | Removida — toast redireciona para Web |
| Apache POI (poi-ooxml) | Removido de build.gradle |

**Mantido no banco:** `PatientReport`, `AppointmentReport` (entities + DAOs) — sem migration.

### 1.3 Audit Log

| Item | Status |
|------|--------|
| AuditLogActivity | Deletado |
| AuditLogViewModel (Kotlin) | Deletado |
| AuditLogViewModel.java | Deletado |

**Mantido no banco:** `AuditLog` (entity + DAO) — sem migration.

### 1.4 AndroidManifest

- Removido: `ReportsActivity`, `WhatsAppDialogActivity`, `WhatsAppHistoryActivity`, `WhatsAppReminderReceiver`.

---

## FASE 2 — SIMPLIFICAR DESIGN ✅ Concluída

| Item | Status |
|------|--------|
| Remoção do tema claro | Tema escuro único em App.kt e MainActivity |
| Toggle modo escuro em Configurações | Removido (switch oculto) |
| values/themes.xml | Alterado para usar cores escuras (único tema) |
| Theme.kt (Theme.kt) | Cores centralizadas: GoldPrimary, BackgroundDark, CardDark, BorderGoldSoft |
| PsiProDimens | Radius padrão 20.dp, RadiusSmall 12.dp, RadiusLarge 24.dp |
| PsiProCard | Componente reutilizável em PsiProComponents.kt |
| PsiProPrimaryButton | Botão primário dourado em PsiProComponents.kt |
| PsiProTopBar | TopBar reutilizável em PsiProComponents.kt |
| PsiproTheme (Compose) | Apenas esquema escuro; StatusColors preservado |

---

## FASE 3 — MANTIDO (core mobile)

| Módulo | Arquivos principais |
|--------|---------------------|
| Login | MainActivity, AuthViewModel, CreateAccountActivity |
| Seleção de clínica | BackendAuthManager (sync) |
| Dashboard | DashboardActivity, HomeInteligenteFragment, HomeScreen |
| Agenda | ScheduleFragment, AppointmentScheduleActivity, WeeklyAgendaScreen |
| Pacientes | PatientsFragment, PatientListActivity, CadastroPacienteActivity, DetalhePacienteActivity |
| Sessões | NovaSessaoActivity, AnotacoesSessaoActivity |
| Registro de evolução | NoteEditActivity, AnotacaoSessaoViewModel |
| Pagamento | FinanceiroFragment, FinanceiroDashboardActivity, CobrancaSessaoViewModel |
| Notificações | NotificacoesFragment, NotificationsActivity |
| Configurações | ConfiguracoesFragment (perfil, notificações, tema) |
| Suporte | SuporteFragment |
| Aniversariantes | BirthdayFragment |
| Autoavaliação | AutoavaliacaoFragment |

---

## FASE 4 — OTIMIZAÇÃO (parcial)

- Apache POI removido — redução de dependência.
- Verificar outras dependências não usadas.
- ProGuard mantido em release.

---

## Lista de arquivos deletados

```
app/src/main/java/com/psipro/app/ui/screens/WhatsAppHistoryActivity.kt
app/src/main/java/com/psipro/app/ui/screens/WhatsAppDialogActivity.kt
app/src/main/java/com/psipro/app/ui/screens/WhatsAppHistoryScreen.kt
app/src/main/java/com/psipro/app/ui/screens/WhatsAppMensagemDialog.kt
app/src/main/java/com/psipro/app/data/service/WhatsAppService.kt
app/src/main/java/com/psipro/app/notification/WhatsAppReminderReceiver.kt
app/src/main/java/com/psipro/app/utils/WhatsAppUtils.kt
app/src/main/java/com/psipro/app/adapter/WhatsAppConversationAdapter.kt
app/src/main/java/com/psipro/app/ui/WhatsAppConversationAdapter.kt
app/src/main/java/com/psipro/app/data/repository/WhatsAppConversationRepository.kt
app/src/main/java/com/psipro/app/ui/ReportsActivity.kt
app/src/main/java/com/psipro/app/ui/PatientReportsActivity.kt
app/src/main/java/com/psipro/app/ui/AuditLogActivity.kt
app/src/main/java/com/psipro/app/ui/AppointmentReportActivity.kt
app/src/main/java/com/psipro/app/ui/AppointmentReportListActivity.kt
app/src/main/java/com/psipro/app/ui/AppointmentReportListFragment.kt
app/src/main/java/com/psipro/app/ui/AppointmentReportListViewModel.kt
app/src/main/java/com/psipro/app/ui/AppointmentReportListViewModelFactory.kt
app/src/main/java/com/psipro/app/ui/AppointmentReportAdapter.kt
app/src/main/java/com/psipro/app/ui/AppointmentReportListAdapter.kt
app/src/main/java/com/psipro/app/viewmodel/ReportViewModel.kt
app/src/main/java/com/psipro/app/viewmodel/PatientReportViewModel.kt
app/src/main/java/com/psipro/app/reports/AppointmentReportGenerator.kt
app/src/main/java/com/psipro/app/reports/PdfReportExporter.kt
app/src/main/java/com/psipro/app/data/repository/AppointmentReportRepository.kt
app/src/main/java/com/psipro/app/adapter/PatientReportAdapter.kt
app/src/main/java/com/psipro/app/ui/PatientReportDialog.kt
app/src/main/java/com/psipro/app/ui/AppointmentReportDialog.kt
app/src/main/java/com/psipro/app/AuditLogViewModel.java
app/src/main/java/com/psipro/app/ui/AuditLogViewModel.kt
```

---

## Lista de dependências removidas

- `org.apache.poi:poi-ooxml:5.2.3`

---

## Arquivos novos criados

**Fase 1:**
- `app/src/main/java/com/psipro/app/utils/ClipboardUtils.kt` — utilitário de área de transferência

**Fase 2:**
- `app/src/main/java/com/psipro/app/ui/compose/Theme.kt` — PsiProColors, PsiProDimens, StatusColors
- `app/src/main/java/com/psipro/app/ui/compose/PsiProComponents.kt` — PsiProCard, PsiProPrimaryButton, PsiProTopBar

---

## Estrutura final do app

```
app/
├── MainActivity, DashboardActivity, SplashActivity
├── CadastroPacienteActivity, DetalhePacienteActivity, DadosPessoaisActivity
├── FinanceiroPacienteActivity
├── AnamneseActivity, ProntuarioListActivity, ProntuarioEditActivity, NoteEditActivity
├── ui/
│   ├── fragments/ (Home, Schedule, Patients, Financeiro, Notificacoes, etc.)
│   ├── screens/ (NovaSessao, AnotacoesSessao, Documentos, Arquivos, FinanceiroDashboard, etc.)
│   ├── compose/ (PsiproTheme, Theme, PsiProComponents, BillingDialog, WeeklyAgendaScreen)
│   └── viewmodels/
├── data/ (entities, dao, repository — PatientReport, AppointmentReport, AuditLog, WhatsAppConversation mantidos no DB)
├── viewmodel/
├── sync/
├── notification/
└── utils/ (ClipboardUtils, etc.)
```

---

## Próximos passos recomendados

1. Migration Room (27): remover tabelas `audit_logs`, `whatsapp_conversations`, `appointment_reports` quando seguro.
3. Remover `PatientReport` se não houver uso futuro.
4. Atualizar `menu_patient_list.xml` se `action_import_patients` e `action_test` forem obsoletos.
