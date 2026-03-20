# Análise: Requisitos do Backend PsiPro para o Dashboard

Este documento compara os requisitos do Dashboard com o estado atual do backend.

---

## 1. Headers obrigatórios

| Requisito | Status | Observação |
|-----------|--------|------------|
| `Authorization: Bearer {token}` | ✅ | JwtAuthGuard em todas as rotas protegidas |
| `X-Clinic-Id` | ✅ | ClinicGuard exige o header; retorna 400 se ausente |

---

## 2. Pacientes

### POST `/patients` — Criar paciente

| Campo | Requisito | Status | Observação |
|-------|-----------|--------|-------------|
| name / full_name | Obrigatório | ✅ | Aceita ambos; Transform mapeia |
| email, phone, birthDate, cpf, observations, status | Opcionais | ✅ | Implementado |
| **gender** | Opcional (novo) | ❌ | **Falta:** adicionar ao DTO e schema |
| **profession** | Opcional (novo) | ❌ | **Falta:** adicionar ao DTO e schema |

### PATCH `/patients/:id`

| Campo | Requisito | Status | Observação |
|-------|-----------|--------|------------|
| anamnesis | Aceitar e persistir | ✅ | Implementado |
| **progress** | "improving" \| "stable" \| "attention" | ❌ | **Falta:** adicionar ao DTO e schema |

### GET `/patients/:id`

| Campo | Requisito | Status | Observação |
|-------|-----------|--------|------------|
| anamnesis / anamnesis_data / anamnesisData | Retornar quando existir | ✅ | Aliases implementados |
| progress | Retornar quando existir | ❌ | **Falta:** campo não existe no schema |

### DELETE `/patients/:id`

| Requisito | Status | Observação |
|-----------|--------|------------|
| Implementar endpoint | ✅ | Soft delete (deletedAt); 404 se não existir |

---

## 3. Sessões

### GET `/sessions/:id` — Detalhe da sessão

| Requisito | Status | Observação |
|-----------|--------|------------|
| Endpoint existe | ✅ | `findOne` retorna sessão |
| Formato da resposta | ⚠️ Parcial | Backend retorna formato Prisma (camelCase). Dashboard espera snake_case e campos específicos |

**Resposta atual (Prisma):**
```json
{
  "id": "uuid",
  "patientId": "uuid",
  "userId": "uuid",
  "date": "2026-03-06T17:00:00.000Z",
  "duration": 60,
  "type": "Consulta",
  "status": "realizada",
  "notes": "...",
  "clinicalData": { "emotional_state": 7, ... },
  "patient": { "id": "...", "name": "..." },
  "summary": "...",
  "themes": [],
  "emotions": [],
  "actionItems": [],
  "riskFlags": []
}
```

**Resposta esperada pelo Dashboard:**
```json
{
  "patient_id": "uuid",
  "patient_name": "...",
  "professional_id": "uuid",
  "scheduled_at": "...",
  "start_at": "...",
  "duration_minutes": 50,
  "clinical": { ... },
  "aiAnalysis": { "summary": "...", "themes": [], ... }
}
```

**Gap:** Session usa `date` (não `scheduled_at`/`start_at`). Falta mapear para o formato esperado ou o frontend adaptar.

### PATCH `/sessions/:id`

| Campo | Requisito | Status |
|-------|-----------|--------|
| patientId, date, professionalId, duration, notes | Aceitar | ✅ |
| type | Aceitar | ✅ |
| clinical | Aceitar (emotional_state, evolution_notes, etc.) | ✅ |

### DELETE `/sessions/:id`

| Requisito | Status |
|-----------|--------|
| Implementar endpoint | ✅ |

---

## 4. Financeiro

### POST `/financial/records`

| Requisito | Status | Observação |
|-----------|--------|------------|
| Respeitar `status` enviado | ⚠️ Parcial | Dashboard envia `"pending"`, `"paid"`, `"overdue"`, `"cancelled"`. Backend usa `"pendente"`, `"pago"`. **Falta mapear** |
| amount: 0 | ✅ | Aceito |
| session_id | ✅ | Aceito e persistido |

### PATCH `/financial/records/:id`

| Requisito | Status |
|-----------|--------|
| Respeitar status, amount, payment_method, paid_at | ✅ |

---

## 5. Anamnese

| Requisito | Status |
|-----------|--------|
| GET `/patients/:id` retorna anamnesis | ✅ |
| PATCH `/patients/:id` aceita anamnesis | ✅ |
| Estrutura `{ items: [...], updatedAt }` | ✅ |

---

## 6. Outros endpoints

| Método | Rota | Status | Observação |
|--------|------|--------|------------|
| POST | /auth/login | ✅ | |
| GET | /clinics | ✅ | |
| GET | /clinics/:id/professionals | ✅ | Rota: `:clinicId/professionals` |
| GET | /patients | ✅ | |
| GET | /patients/count | ✅ | |
| GET | /patients/recent | ✅ | |
| GET | /appointments?start=&end= | ⚠️ | `findAll` não aceita start/end; retorna todos |
| GET | /appointments/today | ✅ | |
| POST | /appointments | ✅ | |
| GET | /sessions | ✅ | |
| POST | /sessions | ✅ | |
| GET | /financial/records | ✅ | |
| GET | /financial/summary | ✅ | |
| GET | /reports | ✅ | Várias subrotas (findAll, getSummary, etc.) |

---

## 7. Checklist — O que falta implementar

| Item | Prioridade | Status |
|------|------------|--------|
| **Pacientes: gender e profession** | Alta | ✅ Implementado |
| **Pacientes: progress** | Média | ✅ Implementado |
| **Financeiro: mapear status** | Alta | ✅ Implementado (pending↔pendente, paid↔pago, etc.) |
| **GET /sessions/:id formato** | Média | ✅ Implementado (formatForDashboard) |
| **GET /appointments?start=&end=** | Baixa | ⏳ Pendente |

---

## 8. URL base

- Backend usa prefixo global `/api`.
- URLs finais: `https://psipro-backend-production.up.railway.app/api/patients`, etc.
- Dashboard deve usar `VITE_API_URL` ou `NEXT_PUBLIC_API_URL` = `https://...railway.app/api` (com `/api`).
