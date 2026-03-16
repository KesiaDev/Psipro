# Sistema de Auditoria

## Objetivo
Registrar eventos críticos para rastreabilidade. O log de auditoria nunca interrompe o fluxo principal (try/catch silencioso).

---

## Model AuditLog (Prisma)

```
id        String   @id @default(uuid())
userId    String
clinicId  String?  // Opcional: login/logout podem não ter clínica ativa
action    String
entity    String
entityId  String?
metadata  Json?
createdAt DateTime @default(now())
```

---

## AuditService

```typescript
log({ userId, clinicId?, action, entity, entityId?, metadata? })
```

- **Try/catch silencioso:** se falhar, não propaga exceção.
- Usa `prisma.auditLog.create` diretamente (não dentro da transação de negócio).

---

## Endpoints que geram log de auditoria

| Endpoint | Action | Entity | Observação |
|----------|--------|--------|------------|
| **POST /auth/login** | `login` | User | Após validação e geração de tokens |
| **POST /auth/logout** | `logout` | User | Após revogar refresh token (userId do token) |
| **POST /auth/switch-clinic** | `switch_clinic` | Clinic | Após validar pertencimento |
| **PUT /appointments/:id** | `appointment_status_change` | Appointment | Quando status muda (metadata: from, to) |
| **PUT /appointments/:id** | `payment_creation` | Payment | Quando status → REALIZADO e Payment é criado |
| **PUT /appointments/:id** | `payment_cancellation` | Payment | Quando status → CANCELADO e Payment vinculado cancelado |
| **POST /payments** | `payment_creation` | Payment | Criação manual de pagamento |
| **POST /patients** | `patient_creation` | Patient | Criação de paciente (inclui import) |
| **PATCH /patients/:id** | `patient_update` | Patient | Atualização de paciente |
| **DELETE /patients/:id** | `patient_deletion` | Patient | Exclusão de paciente |

---

## Migration

```bash
cd backend
npx prisma migrate deploy
```

Migração: `20260227000000_add_audit_log`
