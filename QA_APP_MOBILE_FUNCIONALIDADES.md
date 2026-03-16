# PsiPro App Mobile — Funcionalidades para QA

Documento completo das funcionalidades do app Android para uso por um agente de QA.

---

## 1. AUTENTICAÇÃO E ONBOARDING

| # | Funcionalidade | Descrição | Como testar |
|---|----------------|-----------|-------------|
| 1.1 | **Login** | Login com e-mail e senha | MainActivity → preencher credenciais → Dashboard |
| 1.2 | **Criar conta** | Cadastro de novo usuário | MainActivity → "Criar conta" → CreateAccountActivity |
| 1.3 | **Recuperar senha** | Esqueci minha senha | MainActivity → link → abre plataforma web |
| 1.4 | **Consentimento LGPD** | Aceite de termos ao first-run | Primeiro acesso → modal LGPD → Aceito |
| 1.5 | **Login Backend** | Login no backend NestJS (para sync) | BackendLoginActivity (se houver fluxo) |
| 1.6 | **Splash** | Tela de carregamento inicial | SplashActivity |
| 1.7 | **Persistência de sessão** | Usuário logado permanece | Fechar e reabrir app → deve ir direto ao Dashboard |

---

## 2. NAVEGAÇÃO PRINCIPAL

### 2.1 Bottom Navigation
- **Dashboard (Início)** — `HomeInteligenteFragment`
- **Agenda** — `ScheduleFragment` / `WeeklyAgendaScreen`
- **Pacientes** — `PatientsFragment` / `PatientsScreen`

### 2.2 Menu Lateral (Drawer)
| Item | Ação |
|------|------|
| Dashboard | Navega para Home |
| Agenda | Navega para Agenda |
| Pacientes | Navega para Pacientes |
| **Notificações** | Abre NotificationsActivity |
| **Plataforma Web** | Abre dashboard web (Custom Tabs) |
| **Relatórios Web** | Abre relatórios na web |
| **Sincronizar** | Dispara sync manual (só se logado no backend) |
| **Trocar clínica** | Abre ClinicSwitchDialog (só se logado no backend) |
| **Configurações** | Navega para ConfiguracoesFragment |
| **Suporte** | Navega para SuporteFragment |
| **Sair** | Logout → volta para MainActivity |

---

## 3. DASHBOARD (HOME)

| # | Funcionalidade | Descrição | Como testar |
|---|----------------|-----------|-------------|
| 3.1 | **Resumo do dia** | Consultas de hoje | Ver cards de appointments |
| 3.2 | **Consultas de hoje** | Lista de agendamentos | Clicar em consulta → AppointmentDetailActivity |
| 3.3 | **Confirmar sessão realizada** | Marcar como realizada | Botão na consulta → diálogo de pagamento |
| 3.4 | **Anotação de sessão** | Abrir NovaSessaoActivity | Clicar em anotação |
| 3.5 | **Ligar/WhatsApp** | Contato do paciente | Botão de telefone |
| 3.6 | **Ir para Agenda** | Navega para ScheduleFragment | Botão/área |
| 3.7 | **Chip Trocar clínica** | Seleção de clínica (se backend logado) | Chip na Home |
| 3.8 | **Pacientes recentes** | Lista de pacientes recentes | Cards na Home |

---

## 4. AGENDA

| # | Funcionalidade | Descrição | Como testar |
|---|----------------|-----------|-------------|
| 4.1 | **Visualização semanal** | WeeklyAgendaScreen | Ver grade de dias/horas |
| 4.2 | **Navegar semanas** | Anterior / Hoje / Próxima | Botões de navegação |
| 4.3 | **Novo agendamento** | AppointmentScheduleActivity | Botão FAB ou "Nova consulta" |
| 4.4 | **Tipos de consulta** | Consulta, Reconsulta (sem Pessoal) | Formulário de agendamento |
| 4.5 | **Seleção de paciente** | PatientSelectionActivity | Buscar/selecionar paciente |
| 4.6 | **Conflito de horário** | Validação | Tentar agendar em horário ocupado |
| 4.7 | **Lembrete de consulta** | Notificação antes do horário | Agendar e aguardar lembrete |
| 4.8 | **Confirmar via WhatsApp** | Enviar mensagem ao paciente | Botão no diálogo de confirmação |
| 4.9 | **Editar agendamento** | AppointmentFormActivity | Clicar em slot existente |
| 4.10 | **Excluir agendamento** | Cancelar consulta | Opção no detalhe |
| 4.11 | **Detalhe do agendamento** | AppointmentDetailActivity | Clicar em slot |

---

## 5. PACIENTES

| # | Funcionalidade | Descrição | Como testar |
|---|----------------|-----------|-------------|
| 5.1 | **Listar pacientes** | PatientsScreen / PatientListActivity | Ver lista |
| 5.2 | **Buscar pacientes** | Campo de busca | Digitar nome |
| 5.3 | **Cadastrar paciente** | CadastroPacienteActivity | Novo paciente |
| 5.4 | **Dados pessoais** | Nome, CPF, data nascimento, telefone, etc. | Formulário de cadastro |
| 5.5 | **Informações financeiras** | Valor sessão, dia cobrança, lembrete | Cadastro/campos |
| 5.6 | **Detalhe do paciente** | DetalhePacienteActivity | Clicar em paciente |
| 5.7 | **Ver sessões** | AnotacoesSessaoActivity | Menu paciente → Ver sessões |
| 5.8 | **Nova anotação rápida** | QuickSessionActivity | Menu paciente |
| 5.9 | **Agendar consulta** | Navega para Agenda com paciente pré-selecionado | Menu paciente |
| 5.10 | **Abrir no Web** | Abre ficha do paciente na plataforma web | Menu paciente |

---

## 6. SESSÕES E ANOTAÇÕES

| # | Funcionalidade | Descrição | Como testar |
|---|----------------|-----------|-------------|
| 6.1 | **Nova sessão (anotação)** | NovaSessaoActivity | A partir de consulta ou menu |
| 6.2 | **Sessão rápida (Quick)** | QuickSessionActivity | Anotação rápida com gravação |
| 6.3 | **Gravar sessão** | VoiceRecorder → backend Whisper | Botão gravar → parar → transcrever |
| 6.4 | **Transcrição de áudio** | POST /voice/transcribe | Gravação convertida em texto |
| 6.5 | **Insights IA** | Resumo/insights da sessão (voice-note) | Após transcrever em QuickSession |
| 6.6 | **Leitura por voz (TTS)** | VoiceReader para insights | Ouvir insights em voz alta |
| 6.7 | **Anotações de sessão** | AnotacoesSessaoActivity | Lista de sessões do paciente |
| 6.8 | **Editar nota** | NoteEditActivity | Editar anotação existente |
| 6.9 | **Transcrever áudio em nota** | NoteEditActivity → escolher arquivo ou gravar | Botão transcrever |

---

## 7. PRONTUÁRIO E ANAMNESE

| # | Funcionalidade | Descrição | Como testar |
|---|----------------|-----------|-------------|
| 7.1 | **Lista de prontuários** | ProntuarioListActivity | Menu principal |
| 7.2 | **Editar prontuário** | ProntuarioEditActivity | Abrir prontuário |
| 7.3 | **Modelos de anamnese** | ModelosAnamneseScreen | CRUD de modelos |
| 7.4 | **Formulário de anamnese** | FormularioAnamneseScreen / SimplifiedAnamneseActivity | Preencher anamnese |
| 7.5 | **Histórico médico** | HistoricoMedicoScreen | Seção no paciente |
| 7.6 | **Histórico familiar** | HistoricoFamiliarScreen | Seção no paciente |
| 7.7 | **Vida emocional** | VidaEmocionalScreen | Seção no paciente |
| 7.8 | **Observações clínicas** | ObservacoesClinicasScreen | Seção no paciente |
| 7.9 | **Menu anamnese** | MenuAnamneseScreen | Acesso às seções |
| 7.10 | **Anamnese simplificada** | SimplifiedAnamneseActivity | Fluxo simplificado |

---

## 8. DOCUMENTOS E ARQUIVOS

| # | Funcionalidade | Descrição | Como testar |
|---|----------------|-----------|-------------|
| 8.1 | **Documentos do paciente** | DocumentosScreen / DocumentosActivity | Lista de documentos |
| 8.2 | **Arquivos** | ArquivosScreen / ArquivosActivity | Lista de arquivos |
| 8.3 | **Anexar arquivo** | Upload de arquivo | Adicionar documento |
| 8.4 | **Anexar áudio** | Gravação ou seleção de áudio | Em nota ou documento |

---

## 9. FINANCEIRO E COBRANÇAS

| # | Funcionalidade | Descrição | Como testar |
|---|----------------|-----------|-------------|
| 9.1 | **Financeiro na Web** | WebNavigator.openFinancialOnWeb | Menu → abrir financeiro |
| 9.2 | **Cobrança de sessão** | Cobrança ao marcar sessão como realizada | Confirmar sessão → diálogo cobrança |
| 9.3 | **Detalhe da cobrança** | DetalheCobrancaScreen | Ver cobrança |
| 9.4 | **Mensagem WhatsApp (cobrança)** | Gerar link WhatsApp para cobrança | Botão WhatsApp na cobrança |
| 9.5 | **Lembrete de cobrança** | CobrancaNotificationService / LembreteCobrancaWorker | Notificação mensal |

---

## 10. AUTOAVALIAÇÃO

| # | Funcionalidade | Descrição | Como testar |
|---|----------------|-----------|-------------|
| 10.1 | **Autoavaliação** | AutoavaliacaoFragment / AutoavaliacaoScreen | Responder questionário |
| 10.2 | **Notificações motivacionais** | AutoavaliacaoMotivacionalReceiver | Lembretes diários |
| 10.3 | **Histórico de autoavaliações** | Lista de respostas | Ver histórico |

---

## 11. CONFIGURAÇÕES

| # | Funcionalidade | Descrição | Como testar |
|---|----------------|-----------|-------------|
| 11.1 | **Editar perfil** | EditProfileActivity | Nome, CRP, e-mail, WhatsApp, foto |
| 11.2 | **Modo escuro/claro** | Tema | Switch modo escuro |
| 11.3 | **Notificações** | Liga/desliga | Switch notificações |
| 11.4 | **Sincronizar agora** | Sync manual (backend) | Botão Sincronizar |
| 11.5 | **Política de privacidade** | Dialog | Botão política |
| 11.6 | **Termos de uso** | Dialog | Botão termos |
| 11.7 | **Acessibilidade** | Fonte ampliada, alto contraste, botões maiores | Switches |

---

## 12. SUPORTE

| # | Funcionalidade | Descrição | Como testar |
|---|----------------|-----------|-------------|
| 12.1 | **E-mail suporte** | mailto:apppsipro@gmail.com | Botão E-mail |
| 12.2 | **WhatsApp suporte** | wa.me/54992448888 | Botão WhatsApp |
| 12.3 | **FAQ** | Perguntas frequentes | Botão FAQ → dialog |
| 12.4 | **Feedback** | mailto com assunto Feedback | Botão Feedback |

---

## 13. COMANDO DE VOZ

| # | Funcionalidade | Descrição | Como testar |
|---|----------------|-----------|-------------|
| 13.1 | **Abrir comando de voz** | Ícone no menu da action bar | Clicar no ícone |
| 13.2 | **Permissão microfone** | RECORD_AUDIO | Conceder se solicitado |
| 13.3 | **Nova sessão** | "nova sessão", "agendar", "novo agendamento" | Falar comando |
| 13.4 | **Buscar paciente** | "buscar paciente", "pacientes" | Falar comando |
| 13.5 | **Agenda de hoje** | "agenda", "agenda de hoje", "hoje" | Falar comando |
| 13.6 | **Dashboard** | "dashboard", "início", "home" | Falar comando |
| 13.7 | **Comando não reconhecido** | Feedback de erro | Falar algo inválido |

---

## 14. NOTIFICAÇÕES

| # | Funcionalidade | Descrição | Como testar |
|---|----------------|-----------|-------------|
| 14.1 | **Tela de notificações** | NotificationsActivity | Drawer → Notificações |
| 14.2 | **Badge de contador** | Número no item Notificações | Quando há não lidas |
| 14.3 | **Lembrete de consulta** | AppointmentReminderReceiver | Agendar e aguardar |
| 14.4 | **Permissão POST_NOTIFICATIONS** | Android 13+ | Conceder ao abrir |

---

## 15. PLATAFORMA WEB

| # | Funcionalidade | Descrição | Como testar |
|---|----------------|-----------|-------------|
| 15.1 | **Abrir dashboard web** | Custom Tabs com SSO (token) | Drawer → Plataforma Web |
| 15.2 | **Abrir paciente no web** | /patients/:id | Detalhe paciente → Abrir no Web |
| 15.3 | **Abrir financeiro web** | /financeiro | Via WebNavigator |
| 15.4 | **Abrir relatórios web** | /relatorios | Drawer → Relatórios Web |

---

## 16. SINCRONIZAÇÃO

| # | Funcionalidade | Descrição | Como testar |
|---|----------------|-----------|-------------|
| 16.1 | **Sincronizar** | Sync manual (pacientes, appointments, sessões) | Configurações ou Drawer |
| 16.2 | **Visibilidade** | Só aparece se logado no backend | Ver botão Sincronizar |
| 16.3 | **Header X-Clinic-Id** | Enviado em requisições | Verificar requests ao backend |

---

## 17. TROCAR CLÍNICA

| # | Funcionalidade | Descrição | Como testar |
|---|----------------|-----------|-------------|
| 17.1 | **ClinicSwitchDialog** | Troca de clínica ativa | Drawer → Trocar clínica |
| 17.2 | **Visibilidade** | Só quando logado no backend | Ver item no drawer |
| 17.3 | **Chip na Home** | Trocar clínica no dashboard | Chip "Trocar clínica" |

---

## 18. ACESSIBILIDADE

| # | Funcionalidade | Descrição | Como testar |
|---|----------------|-----------|-------------|
| 18.1 | **Fonte ampliada** | AccessibilityPreferences | Configurações → Fonte ampliada |
| 18.2 | **Alto contraste** | Tema high contrast | Configurações → Alto contraste |
| 18.3 | **Botões maiores** | AccessibilityPreferences | Configurações |
| 18.4 | **Espaçamento aumentado** | Text spacing | Configurações |

---

## 19. ANIVERSARIANTES

| # | Funcionalidade | Descrição | Como testar |
|---|----------------|-----------|-------------|
| 19.1 | **BirthdayFragment** | Lista de aniversariantes do mês | Seção na Home ou menu |
| 19.2 | **WhatsApp aniversário** | Enviar mensagem para paciente | Botão no card de aniversariante |

---

## 20. OUTRAS TELAS

| # | Tela | Descrição |
|---|-----|-----------|
| 20.1 | **BackendLoginActivity** | Login no backend |
| 20.2 | **EditProfileActivity** | Editar perfil completo |
| 20.3 | **HelpActivity** | Ajuda |
| 20.4 | **PrivacyPolicyActivity** | Política de privacidade |
| 20.5 | **PasswordRecoveryActivity** | Recuperar senha |
| 20.6 | **DadosPessoaisActivity** | Dados pessoais do usuário |

---

## CENÁRIOS DE TESTE SUGERIDOS

1. **Fluxo completo de agendamento**: Login → Agenda → Novo agendamento → Selecionar paciente → Confirmar → Ver na lista.
2. **Fluxo de sessão com gravação**: Paciente → Nova anotação rápida → Gravar → Parar → Transcrever → Ver insights.
3. **Comando de voz**: Abrir comando → Falar "nova sessão" → Verificar abertura da tela de agendamento.
4. **Sincronização**: Login backend → Sincronizar → Verificar dados atualizados.
5. **Trocar clínica**: Trocar clínica → Verificar dados filtrados pela nova clínica.
6. **Plataforma Web**: Clicar em "Plataforma Web" → Verificar abertura com SSO.
7. **Offline/Online**: Desligar rede → Usar app → Ligar rede → Sincronizar.

---

## DEPENDÊNCIAS EXTERNAS

- **Backend NestJS**: API base (ex: psipro-backend-production.up.railway.app)
- **PostgreSQL**: Banco (via backend)
- **Whisper** (backend): Transcrição de áudio
- **Chrome Custom Tabs**: Abertura da plataforma web
