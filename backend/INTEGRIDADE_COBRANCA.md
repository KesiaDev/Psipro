# Integridade Financeira — Geração de Cobrança

## Objetivo
Garantir integridade financeira sob concorrência ao confirmar Appointment como REALIZADO.

---

## Alterações Feitas

### 1. Schema Prisma
**Arquivo:** `prisma/schema.prisma`

- Adicionado `@@unique([patientId, sessionNumber])` no model `Payment`.
- Garante que não haja duas cobranças com o mesmo `(patientId, sessionNumber)`.

### 2. Migration
**Arquivo:** `prisma/migrations/20260226000000_add_payment_patient_session_unique/migration.sql`

```sql
CREATE UNIQUE INDEX "payments_patientId_sessionNumber_key" ON "payments"("patientId", "sessionNumber");
```

**Nota:** Em PostgreSQL, valores NULL em colunas com unique constraint são tratados como distintos (padrão SQL), então múltiplos registros com `sessionNumber = null` são permitidos para o mesmo paciente.

### 3. AppointmentsService — Lógica de Cobrança
**Arquivo:** `src/appointments/appointments.service.ts`

#### 3.1 Idempotência — Evitar duplicação por appointment
- Antes de criar Session/Payment, checa se já existe `Session` para `appointmentId`.
- Se existir → retorna o appointment atualizado e **não cria nova cobrança**.
- Cobranças criadas por requisições concorrentes são ignoradas.

#### 3.2 sessionNumber dentro da transação
- O cálculo de `max(sessionNumber) + 1` é feito dentro de `prisma.$transaction`.
- Toda a criação de Session e Payment ocorre na mesma transação.

#### 3.3 Retry em caso de P2002 (unique constraint)
- Se `payment.create` falhar com código `P2002`:
  1. Recalcula `max(sessionNumber)` dentro da mesma transação.
  2. Tenta criar o Payment novamente (apenas **1** retry).
- Se falhar de novo, a exceção é propagada.

#### 3.4 Fluxo completo na transação
1. `appointment.update`
2. Se `becomesRealizada`:
   - Checa `Session` por `appointmentId` → se existir, retorna
   - Cria `Session`
   - `createPaymentWithRetry()`:
     - aggregate `_max(sessionNumber)`
     - `payment.create` com `sessionNumber = max + 1`
     - Em caso de P2002: recalcula e tenta novamente (1 vez)
3. Se `becomesCancelado`: atualiza Payment vinculado para `status: 'cancelado'`
4. Retorna `updated` appointment

---

## Garantias

| Garantia | Como é garantido |
|----------|------------------|
| **Não gerar duas cobranças para o mesmo appointment** | Checagem de `Session` com `appointmentId` antes de criar; se existir, não cria. |
| **sessionNumber único por paciente** | Unique constraint `(patientId, sessionNumber)` no banco. |
| **Concorrência em sessionNumber** | Retry controlado com recálculo de `max` em caso de P2002. |
| **Atomicidade** | Toda a operação dentro de `prisma.$transaction`. |

---

## Execução da Migration

```bash
cd backend
npx prisma migrate deploy
```

**Atenção:** Se houver registros duplicados `(patientId, sessionNumber)` em `payments`, a migration falhará. Nesse caso, é necessário corrigir os dados antes de rodar a migration.
