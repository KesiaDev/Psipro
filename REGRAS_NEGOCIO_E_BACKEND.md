# Regras de Negócio do App Psipro

Lista de regras de negócio identificadas no código Android e indicação de quais deveriam estar no backend para garantir consistência, auditoria e uso multiplataforma.

---

## Legenda

- **Local** – Regra implementada apenas no app Android (Room, ViewModels, etc.)
- **Backend** – Recomendação: regra deveria ser centralizada no servidor

---

## 1. Agendamentos (Appointments)

| Regra | Onde está | Descrição | Backend? |
|-------|------------|-----------|----------|
| Conflito de horário | AppointmentViewModel, AppointmentRepository | Não permitir duas consultas no mesmo horário (data + startTime + endTime) | **Sim** – conflito deve ser validado no servidor para multi-profissional / web |
| Índice único (date, startTime, endTime) | Appointment entity | Constraint Room impede duplicatas no mesmo slot | **Sim** – equivalente no banco do backend |
| Duração mínima/máxima da consulta | ValidationUtils | 30–240 min por consulta | **Sim** – configurável por clínica |
| Horário comercial | ValidationUtils | Horário entre 07:00 e 20:00 | **Sim** – configurável por profissional |
| Status do agendamento | Appointment entity | CONFIRMADO, REALIZADO, FALTOU, CANCELOU | **Sim** – transições devem ser auditadas no backend |
| Transição de status → cobrança | AppointmentViewModel | REALIZADO → CobrancaSessao; FALTOU/CANCELOU → CobrancaAgendamento | **Sim** – criação de cobrança deve ser atômica no backend |
| Recorrência | Appointment entity, generateRecurrenceDates | DAILY, WEEKLY, BIWEEKLY, MONTHLY, CUSTOM; até ~100 ocorrências | **Sim** – geração de série e validações no backend |

---

## 2. Cobranças e Financeiro

| Regra | Onde está | Descrição | Backend? |
|-------|------------|-----------|----------|
| Valor da sessão | Patient.sessionValue, Appointment.sessionValue | Prioridade: Patient > Appointment | **Sim** – fonte única de verdade e auditoria |
| Número da sessão | AppointmentViewModel | `max(numeroSessao) + 1` por paciente | **Sim** – evitar colisões com múltiplos clientes |
| Vencimento padrão (sessão realizada) | AppointmentViewModel | dataVencimento = dataSessao + 7 dias | **Sim** – configurável por clínica/paciente |
| Vencimento (falta/cancelamento) | AppointmentViewModel.confirmBilling | dataVencimento = dataEvento + 7 dias | **Sim** |
| Status de pagamento | CobrancaSessao, CobrancaAgendamento | PAGO, A_RECEBER, VENCIDO, CANCELADO | **Sim** – transições auditadas |
| Cobrança vencida | CobrancaSessaoDao.getVencidas | Cobranças com dataVencimento < hoje e status A_RECEBER | **Sim** – regra de "vencido" centralizada |
| Dia de cobrança | Patient.diaCobranca | Dia do mês (1–31) para lembretes | **Sim** – sincronizar com backend |
| Lembrete de cobrança | Patient.lembreteCobranca, LembreteCobrancaWorker | Notificação no dia = diaCobranca | **Sim** – preferência do profissional |
| Motivo da cobrança (agendamento) | CobrancaAgendamento | FALTA ou CANCELAMENTO | **Sim** |

---

## 3. Pacientes

| Regra | Onde está | Descrição | Backend? |
|-------|------------|-----------|----------|
| CPF válido | ValidationUtils | Formato + dígitos verificadores | **Sim** – evitar CPFs duplicados entre clínicas |
| Telefone válido | ValidationUtils | Formato brasileiro (11) 9xxxx-xxxx | **Sim** – validação e normalização |
| E-mail válido | FirebaseAuthErrorHelper, ValidationUtils | Padrão RFC | **Sim** |
| Data de nascimento | ValidationUtils | Idade entre 0 e 120 anos | **Sim** |
| Valor da sessão obrigatório | CadastroPacienteActivity | valorSessao > 0 | **Sim** – pode ter exceções por clínica |
| Dia de cobrança (1–31) | CadastroPacienteActivity | diaCobranca in 1..31 | **Sim** |
| CPF obrigatório no cadastro | CadastroPacienteActivity | Não permite vazio | **Sim** – política da clínica |
| Grupo de anamnese | Patient.anamneseGroup | ADULTO, CRIANCAS, ADOLESCENTES, IDOSOS | **Sim** – usado no web |

---

## 4. Autenticação e Conta

| Regra | Onde está | Descrição | Backend? |
|-------|------------|-----------|----------|
| Senha mínima 6 caracteres | FirebaseAuthErrorHelper | Validação local antes do Firebase | **Sim** – Firebase já valida; redundante |
| Senha máx. 128 caracteres | FirebaseAuthErrorHelper | Limite de tamanho | **Sim** |
| Confirmação de senha | CreateAccountActivity | password == confirmPassword | Local (UX) – Firebase não exige |
| Nome mínimo 2 caracteres | CreateAccountActivity | Validação no cadastro | **Sim** |
| Campos obrigatórios | CreateAccountActivity, MainActivity | name, email, password | Local + Backend |

---

## 5. Sincronização

| Regra | Onde está | Descrição | Backend? |
|-------|------------|-----------|----------|
| Pacientes "dirty" primeiro | SyncPatientsManager | Push de pacientes locais alterados antes do pull | **Sim** – estratégia de sync definida no backend |
| Não sobrescrever dirty no pull | SyncPatientsManager | Se local dirty, não aplicar remoto | **Sim** – resolução de conflitos |
| Preservar sessionValue, diaCobranca no merge | SyncPatientsManager | Campos locais não vêm do backend; manter existentes | **Sim** – backend deve ter esses campos |
| clinicId obrigatório | BackendAuthManager | Sync só roda com clinicId | **Sim** – multi-tenant |
| Token backend obrigatório | SyncPatientsManager | Sync só com auth válida | **Sim** |

---

## 6. Documentos e Templates

| Regra | Onde está | Descrição | Backend? |
|-------|------------|-----------|----------|
| Tipos de documento | TipoDocumento enum | Atestado, laudo, encaminhamento, etc. | **Sim** – catálogo configurável |
| Preenchimento de templates | DocumentosScreen | Placeholders [Nome], [CRP], etc. | **Sim** – templates versionados e auditados |

---

## 7. Anamnese

| Regra | Onde está | Descrição | Backend? |
|-------|------------|-----------|----------|
| Modelos por grupo | AnamneseSection, AnamneseGroup | Adulto, Crianças, Adolescentes, Idosos | **Sim** – templates no backend |
| Campos obrigatórios | AnamneseViewModel | Respostas não vazias para salvar | **Sim** – regras por tipo de anamnese |
| Exportação de anamnese | AnamneseCompleteFlow | Exportar para PDF/outro formato | **Sim** – geração e armazenamento no backend |

---

## 8. Autoavaliação

| Regra | Onde está | Descrição | Backend? |
|-------|------------|-----------|----------|
| Armazenamento local | AutoavaliacaoRepository | Autoavaliação do profissional (não do paciente) | **Sim** – se for integrar com relatórios web |
| Histórico | AutoavaliacaoScreen | Dashboard, formulário, histórico | **Sim** – auditoria e análises |

---

## 9. Notificações

| Regra | Onde está | Descrição | Backend? |
|-------|------------|-----------|----------|
| Lembrete de consulta | AppointmentNotificationService | X minutos antes (reminderMinutes) | Local – push nativo |
| Lembrete de cobrança | LembreteCobrancaWorker | Dia = diaCobranca e lembreteCobranca = true | Local – pode ser complementado por backend (agendamento de jobs) |
| Relatório diário | FinanceiroNotificationService | Resumo financeiro do dia | Local – lógica pode ser duplicada no backend para web |

---

## 10. Backup e Segurança

| Regra | Onde está | Descrição | Backend? |
|-------|------------|-----------|----------|
| Criptografia do backup | BackupUtils, SecuritySettingsActivity | Backup criptografado | Local – dados sensíveis no dispositivo |
| Senha do backup | SecuritySettingsActivity | Atualmente hardcoded – em produção deve pedir ao usuário | Local – melhoria de implementação |

---

## 11. LGPD e Consentimento

| Regra | Onde está | Descrição | Backend? |
|-------|------------|-----------|----------|
| Termo LGPD no primeiro uso | MainActivity | SharedPreferences aceitou_lgpd | **Sim** – auditoria de consentimento no backend |
| Dados criptografados (clinicalHistory, etc.) | Patient entity, EncryptionConverter | Campos sensíveis criptografados | **Sim** – backend deve armazenar criptografado |

---

## Resumo – Regras que deveriam estar no Backend

| Área | Prioridade | Motivo |
|------|------------|--------|
| **Cobranças e transições de status** | Alta | Integridade financeira, auditoria, multi-plataforma |
| **Validações de paciente (CPF, etc.)** | Alta | Evitar duplicatas, consistência |
| **Conflito de agendamentos** | Alta | Compartilhamento de agenda (web + app) |
| **Criação de CobrancaSessao/CobrancaAgendamento** | Alta | Transação atômica, regras únicas |
| **Número da sessão** | Média | Sequência única por paciente |
| **Regras de vencimento** | Média | Configurável por clínica |
| **Sync (dirty, merge)** | Média | Já parcialmente no backend; reforçar |
| **Validações de consulta (duração, horário)** | Média | Configurável |
| **Anamnese (modelos, obrigatoriedade)** | Média | Templates versionados |
| **LGPD/consentimento** | Média | Auditoria e conformidade |
| **Recorrência** | Média | Regras complexas, série no servidor |

---

## Regras que podem permanecer no app (local)

| Regra | Motivo |
|-------|--------|
| Formatação de CPF, telefone, data | Apenas UX/local |
| Confirmação de senha | UX |
| Horário comercial (se fixo) | Pode ser config no backend e consumido pelo app |
| Notificações push | Recursos nativos do Android |
| Lembretes de cobrança no dia | Pode ser complementado por jobs no backend |
