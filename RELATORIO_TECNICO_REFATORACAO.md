# Relatório Técnico de Refatoração - Projeto Psipro

**Data:** 2025  
**Objetivo:** Identificar arquivos não utilizados, código duplicado, classes mortas e oportunidades de refatoração.

---

## 1. Arquivos não utilizados

### 1.1 Classes e arquivos completamente órfãos

| Arquivo | Motivo | Recomendação |
|---------|--------|--------------|
| `app/FinancialRecordDialogFragment.java` | Única classe Java do app; não referenciada em nenhum outro arquivo; usa `R.layout.dialog_financial_record`; possui código incompleto (comentário sobre `insert` ser suspending na linha 58) | **Remover** – O app usa `FinanceiroRegistroDialog.kt` e telas Compose para finanças |
| `app/sync/SyncService.kt` | Injeta `@Singleton` mas nunca é chamado em nenhum lugar; contém apenas TODOs de sincronização | **Remover** ou manter como esqueleto se houver plano de implementação futuro |
| `app/ui/FinanceiroRegistroDialog.kt` | `DialogFragment` nunca instanciado ou referenciado em nenhum arquivo | **Remover** – Funcionalidade existe em telas Compose (ex.: `FinanceiroPacienteScreen`) |
| `app/ui/compose/AgendamentoTabsScreen.kt` | Função `AgendamentoTabsScreen()` nunca chamada; apenas citada em documentação | **Remover** ou integrar à navegação se fizer parte do design planejado |

### 1.2 Layouts órfãos (apenas referenciados por arquivos mortos)

| Layout | Usado por | Recomendação |
|--------|-----------|--------------|
| `dialog_financial_record.xml` | `FinancialRecordDialogFragment.java` (morto) | **Remover** junto com o Fragment |
| `dialog_financeiro_registro.xml` | `FinanceiroRegistroDialog.kt` (morto) | **Remover** junto com o Dialog |

### 1.3 Arquivo com package inconsistente

| Arquivo | Problema | Recomendação |
|---------|----------|--------------|
| `app/ui/FinanceiroRegistro.kt` | Declara `package com.psipro.app.ui.fragments` mas está em `ui/`; mistura imports de `ArrayAdapter` não usados com `data class FinanceiroRegistro` | **Mover** para `ui/fragments/` e remover imports desnecessários; ou extrair apenas o data class para `ui/fragments/FinanceiroRegistro.kt` |

---

## 2. Funções duplicadas

### 2.1 `createNotificationChannel()`

**Ocorrências em 6 serviços de notificação:**

- `AppointmentNotificationService.kt`
- `AutoavaliacaoNotificationService.kt`
- `AgendamentoNotificationService.kt`
- `CobrancaNotificationService.kt`
- `FinanceiroNotificationService.kt`
- `NotificationService.kt` (base)

**Padrão repetido em cada um:**

```kotlin
private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Nome do Canal",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Descrição do canal"
        }
        notificationManager.createNotificationChannel(channel)
    }
}
```

**Refatoração sugerida:** Extrair para um `NotificationChannelHelper` ou classe base `BaseNotificationService` com método `createChannel(channelId, name, description)`.

### 2.2 Flags de PendingIntent (SDK version check)

**Padrão repetido em múltiplos serviços:**

```kotlin
val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
} else {
    PendingIntent.FLAG_UPDATE_CURRENT
}
```

**Refatoração sugerida:** Centralizar em um utilitário como `PendingIntentUtils.getDefaultFlags()` ou `NotificationUtils.pendingIntentFlags()`.

### 2.3 Parse de valor monetário (`toDoubleOrZero` / similar)

**Ocorrência:** Padrões semelhantes de parse de `String` para `Double` em várias telas financeiras.

**Refatoração sugerida:** Extrair extensão `String.toDoubleOrZero()` em um módulo de utilidades compartilhado.

---

## 3. Código comentado antigo e TODOs

### 3.1 Blocos de código comentado

Foram identificados blocos comentados em vários arquivos, incluindo:

- `HomeScreen.kt`
- `DetalheCobrancaScreen.kt`
- `App.kt`
- Outros arquivos com comentários de 3+ linhas

**Recomendação:** Revisar cada bloco: se for histórico, remover; se for pendência, converter em TODO ou task.

### 3.2 Inventário de TODOs relevantes

| Arquivo | Linha | Descrição |
|---------|-------|-----------|
| `AuthManager.kt` | 123 | Implementar lógica real de autenticação |
| `FinanceiroDashboardActivity.kt` | 48 | Implementar navegação para detalhes da cobrança |
| `FinanceiroPacienteScreen.kt` | 297 | Abrir dialog de edição |
| `SimplifiedAnamneseScreen.kt` | 93 | Implementar salvamento |
| `PatientViewModel.kt` (viewmodel) | 72 | Implementar StateFlow para lista de pacientes |
| `SessionManager.kt` | 64 | Implementar carregamento do usuário |
| `AppointmentListActivity.kt` | 31–33 | Ações de click, long click, recorrência |
| `PasswordRecoveryService.kt` | 30, 32, 43, 45, 61 | Fluxo de recuperação de senha |
| `BackupService.kt` | 116 | Gerar chave de forma segura |
| `PatientListActivity.kt` | 138 | Iniciar fluxo de importação de pacientes |
| `SyncService.kt` | 27, 31, 35 | Implementar sincronização com servidor |
| `AnamneseActivity.kt` | 192 | Implementar salvamento no banco |
| `NotificationService.kt` | 43 | Enviar token para o servidor |
| `AppointmentForm.kt` | 387–388 | Tratar conflito e erro |
| `FinanceiroUnificadoViewModel.kt` | 384 | Implementar query complexa quando possível |
| `FormularioAnamneseScreen.kt` | 352 | Gravar áudio |

---

## 4. Classes duplicadas / ViewModels duplicados

### 4.1 `AppointmentViewModel` (2 versões)

| Pacote | Uso | Diferenças principais |
|--------|-----|------------------------|
| `com.psipro.app.viewmodel.AppointmentViewModel` | `AppointmentScheduleActivity`, `AppointmentFormActivity`, `HomeFragment`, `ScheduleFragment`, `AppointmentForm`, `WeeklyAgendaScreen` | Flow direto, `AppointmentNotificationService`, `generateRecurrenceDates` |
| `com.psipro.app.ui.viewmodels.AppointmentViewModel` | `AppointmentListActivity`, `HomeInteligenteFragment`, `AppointmentDetailActivity` | StateFlow, LiveData, sem notificationService |

**Problema:** Duas implementações diferentes para a mesma entidade, com APIs distintas.

**Refatoração sugerida:** Unificar em um único `AppointmentViewModel` em `ui.viewmodels` e migrar todos os consumidores; deprecar ou remover o `viewmodel.AppointmentViewModel`.

### 4.2 `PatientViewModel` (2 versões)

| Pacote | Uso | Diferenças principais |
|--------|-----|------------------------|
| `com.psipro.app.viewmodel.PatientViewModel` | `CadastroPacienteActivity`, `HomeFragment`, `DetalhePacienteActivity` | Implementação específica daquele pacote |
| `com.psipro.app.ui.viewmodels.PatientViewModel` | `PatientSelectionActivity`, `ProntuarioListActivity`, `PatientListScreen`, `PatientListActivity`, `PatientsFragment`, `BirthdayFragment`, `AppointmentScheduleActivity`, `FinanceiroPacienteScreen` | Mais amplamente usado |

**Refatoração sugerida:** Unificar em `ui.viewmodels.PatientViewModel` e migrar `CadastroPacienteActivity`, `HomeFragment` e `DetalhePacienteActivity`.

---

## 5. Possíveis refatorações priorizadas

### Prioridade alta

1. **Remover arquivos mortos**
   - `FinancialRecordDialogFragment.java`
   - `FinanceiroRegistroDialog.kt`
   - Layouts `dialog_financial_record.xml`, `dialog_financeiro_registro.xml`
   - Avaliar remoção de `SyncService.kt` e `AgendamentoTabsScreen.kt`

2. **Unificar ViewModels**
   - `AppointmentViewModel` → manter apenas `ui.viewmodels`
   - `PatientViewModel` → manter apenas `ui.viewmodels`

### Prioridade média

3. **Extrair helpers de notificação**
   - `NotificationChannelHelper.createChannel(...)`
   - `NotificationUtils.pendingIntentFlags()`

4. **Corrigir package de `FinanceiroRegistro.kt`**
   - Mover para `ui/fragments/` ou ajustar estrutura de pacotes e remover imports não usados

### Prioridade baixa

5. **Extensões de utilidade**
   - `String.toDoubleOrZero()` para parsing monetário
   - Componente Compose compartilhado para `OutlinedTextField` (substituir `themedOutlinedTextField` duplicado)

6. **Tratar TODOs**
   - Priorizar: `PasswordRecoveryService`, fluxo de login, `SessionManager`
   - Secundário: navegação financeira, gravação de áudio na anamnese

---

## 6. Plano de ação sugerido

### Fase 1 – Limpeza (estimada: 1–2 dias)

1. Remover `FinancialRecordDialogFragment.java` e `dialog_financial_record.xml`
2. Remover `FinanceiroRegistroDialog.kt` e `dialog_financeiro_registro.xml`
3. Remover `AgendamentoTabsScreen.kt` se não houver uso planejado
4. Decidir sobre `SyncService.kt`: remover ou manter como esqueleto documentado

### Fase 2 – Unificação de ViewModels (estimada: 2–3 dias)

1. Consolidar `AppointmentViewModel` em `ui.viewmodels` e migrar consumidores
2. Consolidar `PatientViewModel` em `ui.viewmodels` e migrar consumidores
3. Remover pacote `viewmodel` (singular) se ficar vazio
4. Reexecutar testes após cada migração

### Fase 3 – Extração de helpers (estimada: 1 dia)

1. Criar `NotificationChannelHelper` e migrar serviços
2. Criar `PendingIntentUtils` ou similar
3. Atualizar todos os serviços de notificação

### Fase 4 – Ajustes e TODOs (contínuo)

1. Corrigir package de `FinanceiroRegistro.kt`
2. Revisar blocos de código comentado
3. Atualizar ou implementar TODOs conforme prioridade do produto

---

## 7. Resumo executivo

| Categoria | Quantidade | Impacto |
|-----------|------------|---------|
| Arquivos mortos | 4–6 | Reduz complexidade e confusão |
| Layouts órfãos | 2 | Limpeza de recursos |
| Funções duplicadas | 2 padrões (6+ ocorrências cada) | Manutenção mais simples |
| ViewModels duplicados | 2 pares | Consistência e manutenção |
| TODOs relevantes | 17+ | Produtividade e completude |

**Benefícios esperados:**
- Código-base mais limpo e fácil de entender
- Menos risco de bugs por uso incorreto de ViewModels duplicados
- Manutenção concentrada em helpers compartilhados
- Redução de confusão sobre qual versão de classe usar

---

*Relatório gerado com base em análise estática do código-fonte.*
