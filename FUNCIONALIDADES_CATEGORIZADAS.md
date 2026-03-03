# Funcionalidades do App Psipro – Categorizadas e Sugestões

**Legenda das sugestões:**
- **Manter no app** – Uso frequente no atendimento ou depende de recursos nativos (câmera, notificações push, etc.)
- **Migrar para web** – Melhor em desktop, relatórios complexos ou configurações avançadas
- **Remover** – Obsoleto, não utilizado ou redundante

---

## 1. Operacional (Uso no Atendimento)

Funcionalidades usadas diretamente durante ou em suporte ao atendimento clínico.

| Funcionalidade | Descrição | Onde está | Sugestão |
|----------------|-----------|------------|----------|
| Cadastro de paciente | Criar novo paciente com dados pessoais | CadastroPacienteActivity | Manter no app |
| Detalhe do paciente | Menu central do paciente (dados, prontuário, anamnese, etc.) | DetalhePacienteActivity | Manter no app |
| Dados pessoais | Editar dados cadastrais do paciente | DadosPessoaisActivity | Manter no app |
| Prontuário | Lista de anotações/registros do paciente | ProntuarioListActivity | Manter no app |
| Editar anotação | Criar/editar anotação no prontuário | NoteEditActivity | Manter no app |
| Anamnese | Formulário de anamnese (3 variantes: legada, dinâmica, simplificada) | AnamneseActivity, ui.screens.AnamneseActivity, SimplifiedAnamneseActivity | Manter no app |
| Anotações da sessão | Registro de conteúdo da sessão, evolução | AnotacoesSessaoActivity / AnotacoesSessaoScreen | Manter no app |
| Nova sessão | Registrar nova sessão com anotações | NovaSessaoActivity | Manter no app |
| Documentos do paciente | Templates (atestado, laudo, encaminhamento) | DocumentosActivity / DocumentosScreen | Manter no app |
| Arquivos do paciente | Arquivos anexados (imagens, etc.) | ArquivosActivity | Manter no app |
| Agenda semanal | Visualizar e gerenciar agendamentos | ScheduleFragment, WeeklyAgendaScreen | Manter no app |
| Detalhe do agendamento | Ver/editar consulta, status, cobrança | AppointmentDetailActivity | Manter no app |
| Agendar consulta | Nova consulta com paciente, data, recorrência | AppointmentScheduleActivity, AppointmentForm, BillingDialog | Manter no app |
| Home com ações rápidas | Próximas consultas, confirmar sessão, WhatsApp, financeiro | HomeInteligenteFragment, HomeScreen | Manter no app |
| Confirmar sessão como realizada | Marcar sessão realizada e gerar cobrança | HomeScreen, AppointmentDetailActivity, SessionPaymentDialog | Manter no app |
| Financeiro do paciente | Valor sessão, dia cobrança, lembretes, cobranças | FinanceiroPacienteActivity, FinanceiroPacienteScreen | Manter no app |
| Contato (WhatsApp/telefone) | Ligar ou enviar WhatsApp | HomeScreen, BirthdayFragment | Manter no app |
| Aniversariantes | Lista + mensagens personalizadas para envio | BirthdayFragment | Manter no app |

---

## 2. Administrativo

Gestão de consultório e fluxos administrativos.

| Funcionalidade | Descrição | Onde está | Sugestão |
|----------------|-----------|------------|----------|
| Lista de pacientes | Buscar, filtrar, acessar paciente | PatientsFragment, PatientListActivity | Manter no app |
| Dashboard financeiro | Resumo receitas/pendentes, cobranças | FinanceiroDashboardActivity | Manter no app |
| Cobranças (sessões) | Registrar pagamento, status | CobrancaSessaoViewModel, FinanceiroPacienteScreen | Manter no app |
| Cobranças (agendamentos) | Cobrança por falta/cancelamento | CobrancaAgendamentoViewModel, BillingDialog | Manter no app |
| Valor por sessão / dia cobrança | Configuração por paciente | FinanceiroPacienteScreen, Patient entity | Manter no app |
| Lembretes de cobrança | Notificações de cobrança pendente | CobrancaNotificationService | Manter no app |
| Notificações de consulta | Lembretes de agendamento | AppointmentNotificationService, AgendamentoNotificationService | Manter no app |
| Lista de agendamentos | Ver agenda completa | AppointmentListActivity | Manter no app (ou migrar para web se pouco usado) |

---

## 3. Relatórios

Geração e visualização de relatórios.

| Funcionalidade | Descrição | Onde está | Sugestão |
|----------------|-----------|------------|----------|
| Relatório diário | Pagamentos do dia (resumo) | RelatorioDiarioScreen | Migrar para web – tela órfã (não há Activity), melhor em dashboard web |
| Exportar PDF (anotações) | Exportar anotações em PDF | AnotacoesSessaoScreen | Manter no app |
| Exportar anamnese | Exportar anamnese preenchida | AnamneseCompleteFlow | Manter no app |
| Notificação relatório diário | Push com resumo financeiro do dia | FinanceiroNotificationService | Manter no app – mantém no app o resumo diário via notificação |

---

## 4. Configuração

Ajustes do aplicativo e do perfil.

| Funcionalidade | Descrição | Onde está | Sugestão |
|----------------|-----------|------------|----------|
| Editar perfil | Nome, CRP, especialidades, foto | EditProfileActivity, EditProfileDialog | Manter no app |
| Preferências | Notificações on/off | ConfiguracoesFragment | Manter no app |
| Política de privacidade / Termos | Textos legais | ConfiguracoesFragment (dialogs) | Manter no app |
| SHA-1 (Firebase) | Exibir/copiar SHA-1 para config. Firebase | ConfiguracoesFragment | Manter no app |
| Backup / Restore | Exportar e importar backup criptografado | SecuritySettingsActivity | Manter no app – SecuritySettingsActivity não está linkado; adicionar na Configurações |
| Ajuda / FAQ | Perguntas frequentes | HelpActivity, SuporteFragment | Manter no app |
| Suporte | E-mail, WhatsApp, FAQ | SuporteFragment | Manter no app |
| Sair / Logout | Encerrar sessão | DashboardActivity (nav_sair) | Manter no app |

---

## 5. Integração

Sincronização com backend e serviços externos.

| Funcionalidade | Descrição | Onde está | Sugestão |
|----------------|-----------|------------|----------|
| Sincronizar pacientes | Sincronizar pacientes com backend | PatientsSyncWorker, PatientsSyncScheduler | Manter no app |
| Sync no login | Enfileirar sync ao fazer login | MainActivity (startBackendSessionAndEnqueueSync) | Manter no app |
| Sync manual (drawer) | Botão "Sincronizar" no menu | DashboardActivity (nav_sync_patients) | Manter no app |
| Plataforma web | Abrir dashboard web via SSO | WebNavigator, nav_plataforma_web | Manter no app |
| Login Firebase | E-mail/senha e Google | MainActivity | Manter no app |
| Criar conta | Cadastro de nova conta | CreateAccountActivity | Manter no app |
| Recuperar senha | Fluxo de recuperação de senha | PasswordRecoveryActivity | Manter no app |

---

## 6. Infraestrutura

Serviços internos, autenticação e dados.

| Funcionalidade | Descrição | Onde está | Sugestão |
|----------------|-----------|------------|----------|
| Splash | Tela inicial | SplashActivity | Manter no app |
| Login | Tela de login | MainActivity | Manter no app |
| Dashboard principal | Drawer + fragmentos | DashboardActivity | Manter no app |
| Room (banco local) | Persistência SQLite | AppDatabase, DAOs | Manter no app |
| Firebase Auth | Autenticação | MainActivity, AuthManager | Manter no app |
| WorkManager (sync) | Sincronização em segundo plano | PatientsSyncWorker | Manter no app |
| Notificações push | Canais e serviços de notificação | Vários *NotificationService | Manter no app |
| Autoavaliação (motivacional) | Lembretes de autoavaliação para paciente | AutoavaliacaoNotificationService | Manter no app |
| Tema escuro fixo | Modo escuro | App.kt, MainActivity | Manter no app |

---

## Resumo de Sugestões

| Sugestão | Quantidade |
|----------|------------|
| **Manter no app** | 45+ |
| **Migrar para web** | 1 (Relatório diário completo) |
| **Remover** | 0 |

---

## Itens Pendentes / Observações

1. **SecuritySettingsActivity** – Existe e está no Manifest, mas não há link nas telas. Recomendação: adicionar botão "Backup / Restaurar" em Configurações.
2. **RelatorioDiarioScreen** – Tela de relatório diário não tem Activity host; só há notificação. Recomendação: migrar relatório completo para web ou criar Activity que hoste essa tela.
3. **HelpActivity** – Está no Manifest; verificar se há navegação para ela (drawer, botão em Suporte, etc.).
4. **Anamnese em 3 variantes** – Há AnamneseActivity (legada), AnamneseActivity (Compose) e SimplifiedAnamneseActivity. Considerar unificar em um único fluxo.
5. **Configurações de segurança** – Dark mode switch está oculto no layout (visibility GONE). Se não for usado, remover do XML.
6. **Backup** – SecuritySettingsActivity usa senha fixa (`promptPassword()` retorna valor hardcoded). Em produção, pedir senha ao usuário.
