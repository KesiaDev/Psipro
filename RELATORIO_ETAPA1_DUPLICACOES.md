# 📋 RELATÓRIO ETAPA 1 — MAPEAMENTO DE DUPLICAÇÕES

**Projeto:** Psipro  
**Data:** Fevereiro 2026  
**Objetivo:** Identificar ViewModels, Repositories, DAOs, Entities e Services duplicados

---

## 1. VIEWMODELS DUPLICADOS

### 1.1 AppointmentViewModel

| Arquivo | Caminho | Classe | Onde está sendo usado |
|---------|---------|--------|------------------------|
| **Versão A** | `app/.../viewmodel/AppointmentViewModel.kt` | `com.psipro.app.viewmodel.AppointmentViewModel` | AppointmentScheduleActivity, WeeklyAgendaScreen, ScheduleFragment, HomeFragment, AppointmentFormActivity |
| **Versão B** | `app/.../ui/viewmodels/AppointmentViewModel.kt` | `com.psipro.app.ui.viewmodels.AppointmentViewModel` | AppointmentDetailActivity, HomeInteligenteFragment, AppointmentListActivity |

**Diferenças:**
- **A**: Tem `AppointmentNotificationService`, lógica completa de CRUD, notificações
- **B**: Tem `showBillingDialog`, `showPaymentDialog`, `updateAppointmentStatus(id, status, onSuccess, onError)`

**Recomendação:** Consolidar em `ui.viewmodels` e migrar dependências da versão A. A versão B precisa receber `AppointmentNotificationService` e a lógica de notificações.

---

### 1.2 PatientViewModel

| Arquivo | Caminho | Classe | Onde está sendo usado |
|---------|---------|--------|------------------------|
| **Versão A** | `app/.../viewmodel/PatientViewModel.kt` | `com.psipro.app.viewmodel.PatientViewModel` | CadastroPacienteActivity, AppointmentScheduleActivity, DetalhePacienteActivity, HomeFragment |
| **Versão B** | `app/.../ui/viewmodels/PatientViewModel.kt` | `com.psipro.app.ui.viewmodels.PatientViewModel` | FinanceiroPacienteScreen, PatientListActivity, ProntuarioListActivity, PatientSelectionActivity, PatientListScreen, PatientsFragment, BirthdayFragment |

**Diferenças:** Versão B tem `EncryptionManager` para dados sensíveis. Versão A é mais simples.

**Recomendação:** Manter `ui.viewmodels.PatientViewModel` (mais completa) e migrar consumidores da versão A.

---

### 1.3 PatientNoteViewModel

| Arquivo | Caminho | Classe | Onde está sendo usado |
|---------|---------|--------|------------------------|
| **Versão A** | `app/.../viewmodel/PatientNoteViewModel.kt` | `com.psipro.app.viewmodel.PatientNoteViewModel` | ProntuarioListActivity (via ViewModelProvider) |
| **Versão B** | `app/.../ui/viewmodels/PatientNoteViewModel.kt` | `com.psipro.app.ui.viewmodels.PatientNoteViewModel` | NoteEditActivity |

**Diferenças:**
- **A**: AndroidViewModel, cria repository manualmente via AppDatabase.getInstance(), callbacks (onSuccess, onError)
- **B**: HiltViewModel com @Inject, injeta `PatientNoteRepository`, usa Coroutines/Flow

**Recomendação:** Migrar ProntuarioListActivity e NoteEditActivity para usar `ui.viewmodels.PatientNoteViewModel` (Hilt). Remover viewmodel/PatientNoteViewModel.

---

### 1.4 AuditLogViewModel

| Arquivo | Caminho | Classe | Onde está sendo usado |
|---------|---------|--------|------------------------|
| **Versão A** | `app/.../AuditLogViewModel.java` | `com.psipro.app.AuditLogViewModel` | **NENHUM** (não referenciado) |
| **Versão B** | `app/.../ui/AuditLogViewModel.kt` | `com.psipro.app.ui.AuditLogViewModel` | AuditLogActivity |

**Recomendação:** Remover `AuditLogViewModel.java` (obsoleto, não utilizado).

---

## 2. REPOSITORIES DUPLICADOS

### 2.1 PatientNoteRepository

| Arquivo | Caminho | Classe | Onde está sendo usado |
|---------|---------|--------|------------------------|
| **Versão A** | `app/.../data/repository/PatientNoteRepository.kt` | `com.psipro.app.data.repository.PatientNoteRepository` | viewmodel/PatientNoteViewModel (cria manualmente) |
| **Versão B** | `app/.../data/repositories/PatientNoteRepository.kt` | `com.psipro.app.data.repositories.PatientNoteRepository` | ui/viewmodels/PatientNoteViewModel (Hilt @Inject) |

**Diferenças:** B tem `getNoteById`, `@Inject`, `@Singleton`. Funcionalidade equivalente.

**Recomendação:** Manter `data.repository.PatientNoteRepository` (padrão oficial). Mover método `getNoteById` de B para A. Remover `data/repositories/PatientNoteRepository.kt`. Registrar no AppModule se necessário. Atualizar ui/viewmodels/PatientNoteViewModel para usar `data.repository`.

---

## 3. PACOTE `data/repositories` (plural) — fora do padrão

| Arquivo | Caminho | Ação |
|---------|---------|------|
| PatientNoteRepository | `data/repositories/PatientNoteRepository.kt` | Mover para `data/repository/` e eliminar duplicata |
| PatientMessageRepository | `data/repositories/PatientMessageRepository.kt` | Mover para `data/repository/` (único, não duplicado) |

**Padrão oficial:** `com.psipro.app.data.repository` (singular)

---

## 4. DAOs DUPLICADOS

### 4.1 PatientDao

| Arquivo | Caminho | Classe | Onde está sendo usado |
|---------|---------|--------|------------------------|
| **Versão A** | `app/.../data/dao/PatientDao.kt` | `com.psipro.app.data.dao.PatientDao` | AppDatabase, DatabaseModule, PatientRepository, DocumentoViewModel, ScheduleViewModel, SyncPatientsManager |
| **Versão B** | `app/.../data/local/PatientDao.kt` | `com.psipro.app.data.local.PatientDao` | **NENHUM** |

**Recomendação:** Remover `data/local/PatientDao.kt` (não referenciado, duplicata obsoleta).

---

## 5. ENTITIES — SEM DUPLICAÇÃO

Todas as entities estão em `com.psipro.app.data.entities` e não há duplicatas.

---

## 6. SERVICES — SEM DUPLICAÇÃO CRÍTICA

- AuthManager (auth/ e security/) — verificar se são o mesmo arquivo ou duplicados
- NotificationService, AgendamentoNotificationService, etc. — únicos
- BackendAuthManager, BackendApiService, SyncPatientsManager — únicos

---

## 7. VIEWMODELS FORA DO PADRÃO DE PACOTE

| Classe | Localização atual | Padrão oficial |
|--------|-------------------|----------------|
| ScheduleViewModel | `ui/schedule/ScheduleViewModel.kt` | `ui.viewmodels.ScheduleViewModel` |
| AppointmentReportListViewModel | `ui/AppointmentReportListViewModel.kt` | `ui.viewmodels.AppointmentReportListViewModel` |
| AuditLogViewModel | `ui/AuditLogViewModel.kt` | `ui.viewmodels.AuditLogViewModel` |

---

## 8. RESUMO EXECUTIVO

| Tipo | Duplicados | Ação |
|------|------------|------|
| ViewModels | 4 (Appointment, Patient, PatientNote, AuditLog) | Consolidar, migrar consumidores, remover obsoletos |
| Repositories | 1 (PatientNote) + 1 pacote `repositories` | Unificar em `data.repository`, mover PatientMessageRepository |
| DAOs | 1 (PatientDao em data/local) | Remover data/local/PatientDao.kt |
| Entities | 0 | — |
| Services | 0 | — |

---

## 9. ORDEM DE EXECUÇÃO SUGERIDA (ETAPA 3)

1. **Remoções simples (sem migração):**
   - Remover `data/local/PatientDao.kt`
   - Remover `AuditLogViewModel.java`

2. **PatientNoteRepository:**
   - Adicionar `getNoteById` em `data/repository/PatientNoteRepository.kt`
   - Registrar no AppModule
   - Atualizar `ui/viewmodels/PatientNoteViewModel` para usar `data.repository.PatientNoteRepository`
   - Migrar `ProntuarioListActivity` e `NoteEditActivity` para Hilt + ui/viewmodels/PatientNoteViewModel
   - Remover `viewmodel/PatientNoteViewModel.kt` e `data/repositories/PatientNoteRepository.kt`

3. **PatientMessageRepository:**
   - Mover `data/repositories/PatientMessageRepository.kt` → `data/repository/PatientMessageRepository.kt`
   - Atualizar imports em AppModule

4. **AppointmentViewModel e PatientViewModel:**
   - Decisão complexa: unificar requer migração cuidadosa de muitas Activities/Fragments
   - Sugestão: fazer em commit separado após validação das etapas 1–2
