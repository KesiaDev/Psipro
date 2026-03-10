# Relatório: Refatoração Arquitetura App/Web PsiPro

## Objetivo

Separar funcionalidades entre o app Android (uso rápido no consultório) e o dashboard web (gestão administrativa completa).

---

## Funcionalidade | App | Web

| Funcionalidade | App Android | PsiPro Web |
|----------------|-------------|------------|
| **Dashboard** | ✅ Resumo do dia, próximas sessões, ações rápidas | ✅ Dashboard completo com métricas |
| **Agenda** | ✅ Visualização semanal, novo agendamento | ✅ Agenda completa, gestão de horários |
| **Pacientes** | ✅ Lista, ficha simplificada, contadores | ✅ CRUD completo, histórico |
| **Financeiro** | 🔗 Link para web | ✅ Gestão completa, cobranças, relatórios |
| **Sessão Rápida** | ✅ Anotação rápida, marcar como realizada | — |
| **Anotação completa** | ✅ NovaSessao/AnotacoesSessao | ✅ Histórico e gestão |
| **Prontuário** | ❌ Removido | ✅ Completo |
| **Anamnese** | ❌ Removido | ✅ Completo |
| **Documentos** | ❌ Removido | ✅ Gestão de documentos |
| **Arquivos** | ❌ Removido | ✅ Gestão de arquivos |
| **Notificações** | ✅ | ✅ |
| **Sincronização** | ✅ patients, appointments, sessions, payments | ✅ Backend principal |
| **Configurações** | ✅ | ✅ |
| **Abrir no Web** | ✅ Botão na ficha do paciente | — |

---

## Alterações Realizadas no App

### 1. Removido do App
- Prontuário completo (telas, activities, navegação)
- Anamnese completa (todas as variantes)
- Documentos
- Arquivos
- Menu items correspondentes na ficha do paciente
- Autoavaliação e Aniversariantes do drawer (foco no essencial)

### 2. Ficha do Paciente Simplificada
- **Resumo:** contador de sessões, atendidas, faltas (dados reais)
- **Ações:** Ver sessões, Nova anotação rápida, Agendar consulta, Ver financeiro, Abrir no Web

### 3. Nova Tela: QuickSessionScreen
- Campos: observações/notas, duração (minutos)
- Botão: "Marcar como realizada e salvar"
- Cria AnotacaoSessao e atualiza appointment do dia para REALIZADO

### 4. Navegação Atualizada
- **Drawer:** Dashboard, Agenda, Pacientes, Financeiro, Notificações, Plataforma Web, Sincronizar, Configurações, Suporte, Sair
- **Bottom Nav:** Início, Agenda, Pacientes, Financeiro
- **Financeiro:** abre plataforma web (`/financial`)

### 5. WebNavigator
- `openPatientOnWeb(context, patientId)` → `/patients/{id}`
- `openFinancialOnWeb(context)` → `/financial`
- `openDashboard(context)` → `/dashboard`

---

## Sincronização Mantida

- patients
- appointments
- sessions (cobranças/pagamentos)
- payments

---

## Resultado

O app Android passou a funcionar como **ferramenta rápida de consultório** (agenda, sessões rápidas, visualização de pacientes). A **gestão avançada** (prontuário, anamnese, documentos, arquivos, financeiro completo) fica concentrada no **PsiPro Web**.
