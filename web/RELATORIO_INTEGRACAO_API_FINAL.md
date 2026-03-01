# Relatório Final — Integração Web 100% com Backend Real

**Data**: 2025  
**Objetivo**: Aplicação web PsiPro conectada exclusivamente à API NestJS, sem dados mockados, fallbacks estáticos ou Supabase.

---

## 1. Arquivos Alterados (Nesta Sessão e Anterior)

### Serviços
| Arquivo | Alterações |
|---------|------------|
| `web/app/services/api.ts` | Cliente centralizado com baseURL, Authorization Bearer, X-Clinic-Id. Sem debug. |
| `web/app/services/dashboardService.ts` | Removidos fallbacks; chamadas reais a `/dashboard/metrics`, `/dashboard/agenda-summary`, `/dashboard/finance-summary` |
| `web/app/services/financialService.ts` | Criado. `getSummary()`, `getPatientFinancial(patientId)` |
| `web/app/services/sessionService.ts` | Criado. `getByPatient(patientId)` → `GET /sessions?patientId=` |
| `web/app/services/documentService.ts` | **Criado**. `getByPatient(patientId)` → `GET /documents?patientId=` |
| `web/app/services/paymentService.ts` | Criado. `getByPatient(patientId)` → `GET /payments/patient/:patientId` |

### Páginas
| Arquivo | Alterações |
|---------|------------|
| `web/app/dashboard/page.tsx` | Métricas, agenda e financeiro via `dashboardService` (API real) |
| `web/app/agenda/page.tsx` | Dados de `appointmentService` e `dashboardService` |
| `web/app/financeiro/page.tsx` | Dados de `dashboardService.getFinanceSummary()` |
| `web/app/pacientes/page.tsx` | `patientService.getPatients()` |
| `web/app/page.tsx` | Comentário ajustado (preview visual, sem dados de negócio) |

### Componentes de Paciente
| Arquivo | Alterações |
|---------|------------|
| `web/app/components/paciente/PatientHeader.tsx` | `patientService.getPatientById(patientId)` |
| `web/app/components/paciente/FinanceiroPacienteTab.tsx` | `financialService.getPatientFinancial()` e `paymentService.getByPatient()` |
| `web/app/components/paciente/HistoricoClinicoTab.tsx` | **Conectado**. `sessionService.getByPatient(patientId)` |
| `web/app/components/paciente/VisaoGeralTab.tsx` | **Conectado**. `sessionService`, `financialService`, `appointmentService` |
| `web/app/components/paciente/DocumentosArquivosTab.tsx` | **Conectado**. `documentService.getByPatient(patientId)` |
| `web/app/components/paciente/DadosCadastraisTab.tsx` | **Conectado**. `patientService.getPatientById` + `updatePatient` |

---

## 2. Endpoints Conectados

| Endpoint | Serviço | Uso |
|----------|---------|-----|
| `GET /dashboard/metrics` | dashboardService | Dashboard KPIs |
| `GET /dashboard/agenda-summary` | dashboardService | Agenda resumida |
| `GET /dashboard/finance-summary` | dashboardService | Resumo financeiro |
| `GET /patients` | patientService | Lista de pacientes |
| `GET /patients/:id` | patientService | Detalhe do paciente |
| `POST /patients` | patientService | Criar paciente |
| `PATCH /patients/:id` | patientService | Atualizar paciente |
| `DELETE /patients/:id` | patientService | Remover paciente |
| `GET /appointments` | appointmentService | Lista de consultas |
| `POST /appointments` | appointmentService | Criar consulta |
| `PUT /appointments/:id` | appointmentService | Atualizar consulta |
| `DELETE /appointments/:id` | appointmentService | Remover consulta |
| `GET /financial/summary` | financialService | Resumo financeiro (admin) |
| `GET /financial/patient/:patientId` | financialService | Financeiro por paciente |
| `GET /sessions?patientId=` | sessionService | Sessões do paciente |
| `GET /documents?patientId=` | documentService | Documentos do paciente |
| `GET /payments/patient/:patientId` | paymentService | Pagamentos do paciente |

---

## 3. Remoção de Mocks — Confirmação

- **Dashboard**: Sem fallbacks; dados exclusivamente da API.
- **Agenda**: Sem `kpiCards`, `weekDays`, `agendaInsights` hardcoded; calendário com dados reais.
- **Financeiro**: Sem `financialCards`, `monthlyRevenue`, `topPatients` mockados.
- **Paciente - Visão Geral**: Sem `patientInsights`, `clinicalSummary`, `financialSummary`, `timeline` hardcoded.
- **Paciente - Histórico Clínico**: Sem array `sessions` mockado.
- **Paciente - Documentos**: Sem array `documents` mockado.
- **Paciente - Dados Cadastrais**: Sem `formData` mockado; dados do paciente via API e salvamento real.
- **FinanceiroPacienteTab**: Sem mocks; dados de `financialService` e `paymentService`.

---

## 4. Loading e Error States

- Todas as páginas e tabs usam `loading` com `Skeleton` ou equivalente.
- Erros exibidos com mensagem real (sem fallback fake).
- Sem dados exibidos quando o backend está offline ou retorna erro.

---

## 5. Multi-Tenant

- Todas as requisições passam por `api.ts`.
- `Authorization: Bearer <token>` (localStorage `psipro_token`).
- `X-Clinic-Id` (localStorage `active_clinic_id`).
- Sem esses headers, o backend responde 400/401 conforme configuração.

---

## 6. Pontos Críticos

1. **FinancialController** (`/financial/*`) tem `@Roles('admin')`:
   - Psicólogos recebem 403 em `/financial/summary` e `/financial/patient/:id`.
   - Dashboard usa `/dashboard/finance-summary` (permite psychologist).
   - Aba financeiro do paciente pode retornar 403 para psicólogos; tratamos exibindo "—" ou mensagem de erro.

2. **ClinicContext**:
   - `active_clinic_id` deve estar em `localStorage` para o header `X-Clinic-Id`.
   - Sem clínica ativa, requisições podem falhar com 400.

3. **Documentos**:
   - `Document` sem `fileUrl` mostra botões Visualizar/Download desabilitados.

4. **Componentes não utilizados** (não aparecem na navegação da página do paciente):
   - `DadosPessoaisTab`, `SummaryTab`, `ProntuarioTab`, `SessoesTab`, `ArquivosTab`, `DocumentosTab` — ainda com dados mockados quando existem; não impactam o fluxo atual.

---

## 7. Validação Final

- **Backend offline**: Dashboard, agenda e demais telas mostram loading → erro, sem dados.
- **Dashboard**: Reflete dados do banco.
- **Criação de paciente no banco**: Aparece na listagem após refresh (ou refetch).
- **Criação de consulta**: Aparece na agenda e em "Próxima consulta" do paciente após refresh.

---

## 8. Status

A aplicação web está integrada ao backend NestJS, sem dados mockados nas funcionalidades principais.  
O sistema está pronto para uso clínico real com as ressalvas indicadas acima.
