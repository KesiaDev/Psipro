# Backend - Status do Módulo Financeiro

## Requisitos implementados (Dashboard PsiPro)

### POST /financial/records

- **Aceita `amount: 0`** em registros pendentes (sessão/agendamento a preencher depois)
- **Aceita e persiste `session_id`** — vínculo com a sessão para cobranças automáticas
- **Aceita `patient_id`** — paciente associado ao registro
- **Aceita `status`** — `pendente` | `pago` (default: `pendente` para novos)
- **Aceita `payment_method`** — PIX, Dinheiro, Cartão, etc.

### Campos do FinancialRecord (Prisma)

| Campo         | Tipo    | Descrição                          |
|---------------|---------|------------------------------------|
| id            | String  | UUID                               |
| userId        | String  | Profissional                       |
| clinicId      | String? | Clínica                            |
| date          | DateTime| Data do registro                    |
| type          | String  | receita \| despesa                 |
| amount        | Decimal | Valor (aceita 0 para pendentes)    |
| description   | String? | Ex: "Sessão - [Nome] - [Data]"      |
| category      | String? | Ex: Sessão, Agendamento             |
| patientId     | String? | Paciente (opcional)                 |
| sessionId     | String? | Vínculo com sessão (único)         |
| status        | String? | pendente \| pago (default: pendente) |
| paymentMethod | String? | Forma de pagamento                 |

### PATCH /financial/records/:id

- Atualiza `amount`, `status`, `payment_method` para "Registrar pagamento"
- Permite marcar como pago e informar valor/forma de pagamento

### GET /financial/records

- Retorna `patient_id`, `session_id`, `patient_name`, `status`, `payment_method`
- Compatível com lista unificada (pendentes + pagos)

## Fluxo automático (sessões/agendamentos)

O backend também cria **Payment** automaticamente ao registrar sessão (POST /sessions, sync). O Dashboard pode usar **FinancialRecord** com `session_id` para a lista unificada no financeiro.
