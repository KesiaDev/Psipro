# Auditoria Técnica Completa — PsiPro (Clínicas e Criar Clínica)

**Data:** Fevereiro 2026  
**Objetivo:** Identificar inconsistências que impedem /clinics e fluxo de criação de clínica.

---

## ETAPA 1 — Auditoria de Rotas Next.js

### 1.1 Estrutura em `web/app/`

| Arquivo | Existe? | Descrição |
|---------|---------|-----------|
| `web/app/clinics/page.tsx` | ✅ Sim | Faz `redirect("/clinica")` — Server Component |
| `web/app/clinica/page.tsx` | ✅ Sim | Página principal de Clínicas (Client Component) |

**Análise:** Ambas existem **por design**. `/clinics` redireciona para `/clinica`. Não é erro.

### 1.2 next.config.ts

- **Localização:** `web/next.config.ts`
- **redirects():** ✅ Existe e está exportado
- **Conteúdo:**
  ```ts
  async redirects() {
    return [
      { source: "/clinics", destination: "/clinica", permanent: false },
    ];
  }
  ```
- **Root:** O arquivo está em `web/next.config.ts` — root correto do projeto Next.js

**Possível causa do 404 em /clinics:** Se o redirect não está funcionando em produção, pode ser:
1. Build cache no Railway (deploy antigo sem o redirect)
2. Root Directory do Railway incorreto (deve ser `web` para monorepo)

### 1.3 Sidebar

- **Arquivo:** `web/app/components/Sidebar.tsx`
- **Link Clínicas:** `{ name: "Clínicas", href: "/clinica", icon: "🏥" }`

✅ Sidebar aponta para `/clinica` (rota correta).

### 1.4 Ocorrências de /clinica e /clinics

| Arquivo | Tipo | Uso |
|---------|------|-----|
| `web/app/clinics/page.tsx` | redirect | `redirect("/clinica")` |
| `web/app/clinica/page.tsx` | rota | Página de clínicas |
| `web/app/components/Sidebar.tsx` | href | `href: "/clinica"` |
| `web/app/dashboard/page.tsx` | href | `href: "/clinica"` |
| `web/app/services/clinicService.ts` | API | `api.get('/clinics')`, `api.post('/clinics', …)` (backend) |
| `web/next.config.ts` | redirect | `source: "/clinics", destination: "/clinica"` |

**Conclusão:** Consistente. `/clinics` no clinicService são chamadas à **API** (backend), não à página. A API usa baseURL do backend.

### 1.5 App Router

- Não existe pasta `web/pages/`. Projeto usa **App Router** (`web/app/`).

---

## ETAPA 2 — Auditoria do Cliente API

### 2.1 web/app/services/api.ts

| Item | Status |
|------|--------|
| `getApiBaseUrl()` | ✅ Usa `process.env.NEXT_PUBLIC_API_URL` |
| Fallback | `'http://localhost:3001/api'` (sem `window.location.origin`) |
| baseURL | `API_BASE_URL` normalizada (remove trailing slash) |
| Token | `localStorage.getItem('psipro_token')` → `Authorization: Bearer ${token}` |
| Montagem da URL | `baseURL + path` (path com `/` inicial) |

✅ Cliente API correto.

### 2.2 Chamadas fora do cliente centralizado

| Arquivo | Chamada | Base URL |
|---------|---------|----------|
| `web/app/test/page.tsx` | `fetch(apiUrl + '/auth/login')` | `getApiBaseUrl()` |
| `web/app/test/page.tsx` | `fetch(apiUrl + '/auth/me')` | `getApiBaseUrl()` |
| `web/app/handoff/HandoffClient.tsx` | `fetch(API_BASE_URL + '/auth/handoff')` | `getApiBaseUrl()` (module-level) |
| `web/app/services/api.ts` | `fetch(url, …)` | `API_BASE_URL` interno |

✅ Todas as chamadas usam a URL base do backend.

---

## ETAPA 3 — Auditoria do Fluxo "Criar Clínica"

### 3.1 Botão "Criar Clínica"

- **Arquivo:** `web/app/clinica/page.tsx`
- **Linha:** 107-112
- **onClick:** `setShowCreateModal(true)` ✅

### 3.2 handleCreateClinic

- **Linhas:** 62-84
- **Fluxo:**
  1. Valida `name`
  2. `clinicService.createClinic({ name })`
  3. Salva `accessToken` em `localStorage`
  4. Fecha modal, `refreshClinics()`, sucesso, `router.push("/dashboard")`
  5. Trata erro com `showError`

✅ Implementação correta. **Não há console.log** — pode ser adicionado para debug.

### 3.3 clinicService.createClinic

- **Arquivo:** `web/app/services/clinicService.ts`
- **Método:** `api.post<CreateClinicResponse>('/clinics', data)` ✅
- **URL resultante:** `baseURL + '/clinics'` = `https://psipro-backend-production.up.railway.app/api/clinics` (se `NEXT_PUBLIC_API_URL` estiver correto)

### 3.4 Modal

- Input "Nome da clínica"
- Botões "Criar" e "Cancelar"
- Estado `creating` desabilita botões

✅ Fluxo completo implementado.

---

## ETAPA 4 — Auditoria do Backend

### 4.1 Controller

- **Arquivo:** `backend/src/clinics/clinics.controller.ts`
- **Decorator:** `@Controller('clinics')` ✅
- **Guard:** `@UseGuards(JwtAuthGuard)` ✅
- **Rota POST:** `create(@Request() req, @Body() createClinicDto: CreateClinicDto)` ✅

### 4.2 Prefixo global

- **Arquivo:** `backend/src/main.ts`
- **Linha:** `app.setGlobalPrefix('api');` ✅

**Endpoint real:** `POST /api/clinics` ✅

### 4.3 CORS

- **Variável:** `CORS_ORIGIN` (split por `,`)
- **Fallback:** `['http://localhost:3001', 'http://localhost:3000']`

⚠️ **CRÍTICO:** Para produção, `CORS_ORIGIN` no Railway (Psipro-backend) **deve incluir**:
```
https://triumphant-perception-production-8792.up.railway.app
```
Caso contrário, o browser bloqueia as chamadas com erro de CORS.

---

## ETAPA 5 — Teste Direto de API

Para testar POST /api/clinics sem o fluxo completo, adicione temporariamente na página de teste (`web/app/test/page.tsx`) um botão que executa:

```ts
const testCreateClinic = async () => {
  const token = localStorage.getItem("psipro_token");
  if (!token) {
    showError("Faça login primeiro");
    return;
  }
  try {
    const res = await fetch(
      "https://psipro-backend-production.up.railway.app/api/clinics",
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": "Bearer " + token,
        },
        body: JSON.stringify({ name: "TESTE CLINICA" }),
      }
    );
    const data = await res.json();
    console.log("Status:", res.status, "Resposta:", data);
  } catch (e) {
    console.error(e);
  }
};
```

---

## ETAPA 6 — Railway

### Frontend (triumphant-perception)

| Item | O que verificar |
|------|-----------------|
| Root Directory | Deve ser `web` (monorepo) |
| NEXT_PUBLIC_API_URL | `https://psipro-backend-production.up.railway.app/api` |
| Redeploy | Após alterar variáveis ou código, novo deploy |

### Backend (Psipro-backend)

| Item | O que verificar |
|------|-----------------|
| Root Directory | Deve ser `backend` (monorepo) |
| CORS_ORIGIN | Incluir `https://triumphant-perception-production-8792.up.railway.app` |
| Migrations | `prisma migrate deploy` no start |
| DATABASE_URL | Configurada |

---

## ETAPA 7 — Produção vs Local

- **Local:** `NEXT_PUBLIC_API_URL` no `.env.local` → `http://localhost:3001/api`
- **Produção:** `NEXT_PUBLIC_API_URL` nas Variables do Railway → `https://psipro-backend-production.up.railway.app/api`
- **CORS local:** Fallback inclui `localhost:3000` e `localhost:3001`
- **CORS produção:** Depende de `CORS_ORIGIN` no backend

---

## RELATÓRIO FINAL

### Problemas encontrados

| # | Problema | Nível | Arquivo / Local |
|---|----------|-------|-----------------|
| 1 | CORS pode bloquear requisições em produção | **ALTO** | `backend/src/main.ts` — variável `CORS_ORIGIN` deve incluir URL do frontend no Railway |
| 2 | Redirect /clinics → /clinica pode não funcionar em produção (404) | **MÉDIO** | Railway build cache ou Root Directory incorreto |
| 3 | Falta de logs de debug no fluxo Criar Clínica | **BAIXO** | `web/app/clinica/page.tsx` — `handleCreateClinic` |

### Correções necessárias

#### 1. CORS (ALTO)

No Railway → Psipro-backend → Variables, adicionar ou editar:

```
CORS_ORIGIN=https://triumphant-perception-production-8792.up.railway.app,http://localhost:3000
```

#### 2. Redirect /clinics (MÉDIO)

- Fazer **Redeploy** do frontend com **Clear build cache** (se disponível no Railway)
- Confirmar **Root Directory** = `web` no serviço triumphant-perception
- Enquanto isso, usar sempre `/clinica` ou o link "Clínicas" do menu

#### 3. Logs de debug (BAIXO)

Em `web/app/clinica/page.tsx`, em `handleCreateClinic`:

```ts
console.log("Criando clínica...", name);
const data = await clinicService.createClinic({ name });
console.log("Resposta:", data);
```

### Ordem de correção recomendada

1. **CORS** — Garantir `CORS_ORIGIN` no backend com a URL do frontend
2. **Redeploy** — Redeploy do frontend com cache limpo
3. **Teste** — Acessar `/clinica`, fazer login, clicar em "Criar Clínica" e criar
4. **Logs** — Adicionar logs se ainda falhar para inspecionar resposta/erro

### Caminhos exatos dos arquivos

```
web/app/clinics/page.tsx
web/app/clinica/page.tsx
web/app/services/api.ts
web/app/services/clinicService.ts
web/app/contexts/ClinicContext.tsx
web/app/components/Sidebar.tsx
web/next.config.ts
backend/src/main.ts
backend/src/clinics/clinics.controller.ts
backend/src/clinics/clinics.service.ts
```

### Resumo

O código está **estruturalmente correto**. Os problemas prováveis são:

1. **CORS** — Backend não autorizando o domínio do frontend em produção
2. **Deploy** — Build antigo ou configuração de Root Directory no Railway
3. **Redirect** — next.config pode não estar sendo aplicado no deploy atual

Após corrigir CORS e conferir o deploy, o fluxo deve funcionar.
