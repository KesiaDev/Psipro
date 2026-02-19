# Resumo Técnico — Refatoração Multi-Clinic

**Data:** Fevereiro 2026

---

## Objetivo

Arquitetura: **1 usuário → várias clínicas** (N:N via ClinicUser).  
Isolamento por header `X-Clinic-Id` em vez de `clinicId` no JWT.

---

## Alterações no Backend

### 1. Schema Prisma

- `user.clinicId` marcado como `@deprecated` (mantido para migração).
- `ClinicUser` permanece como fonte de verdade para a relação N:N.

### 2. JWT Strategy

- `clinicId` removido do retorno de `validate()`.
- Payload do JWT: apenas `{ id, sub, email }`.

### 3. CommonModule

- **ClinicContextHelper** (Injectable): valida acesso via ClinicUser.
- **ClinicGuard**: lê header `x-clinic-id`, valida com ClinicContextHelper, define `request.clinicId`.
- **@CurrentClinicId()**: lê `request.clinicId` após o ClinicGuard.

### 4. Endpoints com ClinicGuard

- **Dashboard:** `@UseGuards(JwtAuthGuard, ClinicGuard)` — todas as rotas.
- **Sync:** `@UseGuards(JwtAuthGuard, ClinicGuard)` — GET e POST `/sync/patients`.

### 5. ClinicsService.create

- Remove atualização de `user.clinicId` e `user.role`.
- Remove geração de novo JWT.
- Passa a retornar só `{ clinic }`.
- Mantém criação de ClinicUser (role OWNER).

### 6. Script de migração

- `prisma/migrate-to-clinic-user.ts` — cria ClinicUser OWNER para cada User com `clinicId`.
- Uso: `npm run prisma:migrate-to-clinic-user`

---

## Alterações no Frontend

### 1. API Client (`api.ts`)

- Header automático: `X-Clinic-Id: localStorage.getItem("active_clinic_id")`.
- Usado em todas as requisições autenticadas.

### 2. ClinicContext

- Usa `active_clinic_id` em vez de `psipro_current_clinic_id`.
- Se não houver salvo, define a primeira clínica como ativa.
- `setCurrentClinic` grava em `active_clinic_id`.

### 3. handleCreateClinic

- Não atualiza mais o token (API não retorna).
- Grava `active_clinic_id` com o ID da clínica criada.
- `CreateClinicResponse` passa a ter apenas `{ clinic }`.

---

## Fluxo Atual

1. Login → JWT sem `clinicId`.
2. `GET /clinics` → lista clínicas do usuário (sem header).
3. Define `active_clinic_id` como a primeira clínica (ou a salva).
4. Requisições seguintes incluem `X-Clinic-Id`.
5. Endpoints com ClinicGuard validam o header e isolam por clínica.

---

## Garantias

- 1 usuário pode ter várias clínicas.
- Troca de clínica alterando `active_clinic_id` e/ou seleção no frontend.
- Isolamento por clínica nos endpoints que usam ClinicGuard.
- `user.clinicId` mantido no schema para migração; remoção futura após validação.

---

## Módulos ainda sem ClinicGuard

- Patients, Appointments, Financial, Payments, Sessions, Documents, Insights.  
- Continuam com a lógica atual (ex.: query params).  
- Podem ser migrados para ClinicGuard em etapas seguintes.

---

## Comandos

```bash
# Migração de dados
npm run prisma:migrate-to-clinic-user

# Build
npm run build
```
