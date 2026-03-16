# Resumo: Área de Clínicas e Criar Clínica

Este documento resume tudo o que foi feito para que a área de clínicas e o fluxo de criação de clínica funcionem corretamente no PsiPro (web + backend).

---

## Contexto

- **Frontend (Next.js):** `https://triumphant-perception-production-8792.up.railway.app`
- **Backend (NestJS):** `https://psipro-backend-production.up.railway.app`
- **API base:** `https://psipro-backend-production.up.railway.app/api`

---

## 1. Backend — Multi-tenant e módulo Clinics

### Schema Prisma
- Modelo `Clinic` com `planType` (INDIVIDUAL | CLINIC), `status`, etc.
- `User` com `clinicId`, `role` (OWNER | PSYCHOLOGIST | ASSISTANT)
- `FinancialRecord`, `Payment` e `Patient` com `clinicId`
- Tabela `Subscription` (preparada para futura cobrança)

### Módulo Clinics
- **POST /api/clinics** — Cria clínica (planType INDIVIDUAL), associa usuário como OWNER e retorna `{ clinic, accessToken }` (novo JWT com `clinicId` e `role`)
- **GET /api/clinics** — Lista clínicas do usuário
- **GET /api/clinics/:id** — Detalhes da clínica
- **PUT /api/clinics/:id** — Atualiza clínica
- **POST /api/clinics/:id/invite** — Convidar usuário
- **PUT/DELETE** — Gerenciar membros

### Auth
- Registro cria Clinic INDIVIDUAL e associa usuário como OWNER
- JWT inclui `clinicId` e `role`
- `JwtStrategy` passa `clinicId` e `role` em `request.user`

### Migrations
- `prisma migrate deploy` — Aplica schema no banco
- `migrate-existing-data.ts` — Migra dados antigos (cria PSIPRO_DEFAULT, associa usuários)

---

## 2. Frontend — API centralizada

### Cliente HTTP (`app/services/api.ts`)
- Base URL via `NEXT_PUBLIC_API_URL` (obrigatório em produção)
- Token JWT em `localStorage` (`psipro_token`)
- Função `getApiBaseUrl()` exportada para handoff e página de teste

### Variáveis de ambiente
- **`.env.local`:** `NEXT_PUBLIC_API_URL=https://psipro-backend-production.up.railway.app/api`
- **Railway (serviço Web):** mesma variável nas Variables do serviço `triumphant-perception`

Todas as chamadas (login, clínicas, pacientes, etc.) usam o cliente centralizado; nenhuma usa URL relativa ou `window.location.origin`.

---

## 3. Redirect /clinics → /clinica

- **next.config.ts:** `redirects()` com `{ source: "/clinics", destination: "/clinica", permanent: false }`
- **app/clinics/page.tsx:** Página que faz `redirect("/clinica")` (redundante ao next.config)
- **Sidebar:** Link "Clínicas" aponta para `/clinica`

Se alguém acessar `/clinics`, deve redirecionar para `/clinica`. Caso o redirect não funcione em produção, usar direto `/clinica` ou o link do menu.

---

## 4. Criar Clínica — Fluxo completo

### Backend
- **POST /api/clinics** com `{ name: string }` e JWT
- Retorna `{ clinic, accessToken }`
- `accessToken` inclui `clinicId` e `role` do novo dono da clínica

### Frontend (`app/clinica/page.tsx`)
- Estado vazio: "Você ainda não faz parte de nenhuma clínica" + botão **Criar Clínica**
- Botão abre modal com campo "Nome da clínica"
- Ao confirmar:
  1. `clinicService.createClinic({ name })`
  2. Se a resposta trouxer `accessToken` → `localStorage.setItem("psipro_token", accessToken)`
  3. `refreshClinics()`
  4. Mensagem de sucesso e `router.push("/dashboard")`

### clinicService
- `createClinic(data)` retorna `Promise<CreateClinicResponse>` (`{ clinic, accessToken }`)
- Usa `api.post('/clinics', data)` — cliente centralizado com baseURL do backend

---

## 5. Railway — Configuração

### Psipro-backend
- **Variables:** `DATABASE_URL`, `JWT_SECRET`, `CORS_ORIGIN`, `NODE_ENV`
- **Custom Start Command:** `npx prisma generate && npx prisma migrate deploy && npx ts-node prisma/migrate-existing-data.ts && node dist/main` (opcional remover o script de migração de dados após a primeira execução)
- Root Directory: `backend` (se monorepo)

### triumphant-perception (Web)
- **Variables:** `NEXT_PUBLIC_API_URL=https://psipro-backend-production.up.railway.app/api`
- Root Directory: `web` (se monorepo)

---

## 6. Checklist de validação

- [ ] `NEXT_PUBLIC_API_URL` definida no Railway (serviço Web)
- [ ] Migrations rodadas no banco de produção (`prisma migrate deploy`)
- [ ] Migração de dados antigos rodada (se o banco já tinha dados)
- [ ] Acesso a `/clinica` funciona (ou `/clinics` redireciona)
- [ ] Login funciona
- [ ] "Criar Clínica" abre modal, envia POST ao backend e retorna 201
- [ ] Token é atualizado após criar clínica
- [ ] Dashboard carrega dados isolados por `clinicId`

---

## Arquivos principais alterados/criados

| Arquivo | Descrição |
|---------|-----------|
| `backend/prisma/schema.prisma` | Modelo Clinic, User.clinicId/role, clinicId em tabelas |
| `backend/src/clinics/*` | Módulo Clinics (controller, service, DTOs) |
| `backend/src/auth/*` | Registro cria clinic, JWT com clinicId/role |
| `backend/prisma/migrate-existing-data.ts` | Migração de dados para multi-tenant |
| `web/app/services/api.ts` | Base URL centralizada, getApiBaseUrl() |
| `web/app/services/clinicService.ts` | createClinic retorna CreateClinicResponse |
| `web/app/clinica/page.tsx` | Modal "Criar Clínica", handleCreateClinic, atualiza token |
| `web/next.config.ts` | Redirect /clinics → /clinica |
| `web/.env.local` | NEXT_PUBLIC_API_URL |
| `web/.env.example` | Modelo para variáveis |
| `web/RAILWAY_WEB_VARIABLES.md` | Instruções para variáveis no Railway |
| `PASSO_A_PASSO_RAILWAY_E_TESTES.md` | Passo a passo de deploy e testes |
