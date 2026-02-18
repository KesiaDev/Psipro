# Auditoria Total — Diagnóstico Técnico PsiPro

**Data:** Fevereiro 2026

---

## ETAPA 1 — Estrutura do Frontend

| Verificação | Resultado | Arquivo / Evidência |
|-------------|-----------|---------------------|
| App Router | ✅ Confirmado | Não existe `web/pages/`; usa `web/app/` |
| `web/app/clinics/page.tsx` | ✅ Existe | Exporta `redirect("/clinica")` — linha 8 |
| `web/app/clinica/page.tsx` | ✅ Existe | Página principal de Clínicas |
| `web/next.config.ts` | ✅ Correto | `redirects()` com `source: "/clinics"` → `destination: "/clinica"` |
| Root Directory Railway | ⚠️ Verificar | Deve ser `web` (monorepo) — não verificável via código |

**Inconsistência:** Nenhuma no código. Root Directory é configuração do Railway.

---

## ETAPA 2 — Build e Produção

| Verificação | Resultado | Evidência |
|-------------|-----------|-----------|
| `NEXT_PUBLIC_API_URL` | ✅ Usado | `web/app/services/api.ts` linha 12 |
| `API_BASE_URL` termina com `/api` | ✅ Sim | Fallback `http://localhost:3001/api`; `.replace(/\/+$/, '')` remove só trailing slash |
| `window.location.origin` | ✅ Não usado | Grep: zero ocorrências em `web/app` |
| URL relativa incorreta | ✅ Não | Todas chamadas usam `getApiBaseUrl()` ou `api` (baseURL) |

**Log adicionado:** `web/app/services/api.ts` — `console.log("[api] API_BASE_URL:", API_BASE_URL)` (client-side).

---

## ETAPA 3 — Debug do Botão Criar Clínica

**Logs adicionados em `web/app/clinica/page.tsx` (handleCreateClinic):**

- `console.log("[handleCreateClinic] iniciado, nome:", name)`
- `console.log("[handleCreateClinic] Token presente:", !!token)`
- `console.log("[handleCreateClinic] Chamando POST /clinics...")`
- `console.log("[handleCreateClinic] Resposta createClinic:", data)`
- `console.error("[handleCreateClinic] Erro criar clínica:", err)`

**Fluxo verificado:**
- Botão "Criar Clínica" → `onClick={() => setShowCreateModal(true)}` (linha 109)
- Modal input + botão "Criar" → `onClick={handleCreateClinic}` (linha 137)
- `handleCreateClinic` chama `clinicService.createClinic({ name })` → `api.post('/clinics', data)`

---

## ETAPA 4 — Token

| Verificação | Resultado |
|-------------|-----------|
| Chave localStorage | `psipro_token` |
| Uso em api.ts | `localStorage.getItem('psipro_token')` → `Authorization: Bearer ${token}` |
| handleCreateClinic | Log adicionado: `Token presente`, `tamanho` |

---

## ETAPA 5 — Backend

| Verificação | Resultado | Arquivo | Linha |
|-------------|-----------|---------|-------|
| `setGlobalPrefix('api')` | ✅ | `backend/src/main.ts` | 28 |
| `@Controller('clinics')` | ✅ | `backend/src/clinics/clinics.controller.ts` | 19 |
| `JwtAuthGuard` | ✅ | `backend/src/clinics/clinics.controller.ts` | 20 |
| CORS `CORS_ORIGIN` | ⚠️ | `backend/src/main.ts` | 9-11 |

**CORS — configuração necessária:**

```
CORS_ORIGIN=https://triumphant-perception-production-8792.up.railway.app,http://localhost:3000
```

O fallback atual é só `['http://localhost:3001', 'http://localhost:3000']`. Em produção, se `CORS_ORIGIN` não incluir o domínio do frontend, o browser bloqueia as requisições.

---

## ETAPA 6 — Teste Automático

**Botão adicionado em `web/app/test/page.tsx`:** "Testar POST /clinics"

- Usa `getApiBaseUrl() + '/clinics'`
- Headers: `Content-Type`, `Authorization: Bearer ${token}`
- Body: `{ name: "TESTE CLINICA AUDIT" }`
- Console: `[AUDIT] Status`, `[AUDIT] Response`, `[AUDIT] Erro completo`

---

## ETAPA 7 — Causa Raiz

### 1. Causa do 404 em /clinics

**Diagnóstico:** O 404 acontece na requisição da **página** `/clinics` (documento HTML), não na API.

Possíveis causas:
1. Redirect em `next.config.ts` não aplicado no deploy (build antigo ou cache)
2. Root Directory do Railway diferente de `web`, então `next.config.ts` não entra no build
3. `app/clinics/page.tsx` fora do build (mesma hipótese de Root Directory)

**Não é:** chamada de API para o frontend — `clinicService` usa `api.post('/clinics')` com baseURL do backend.

**Correção exata:**
- Railway → triumphant-perception → Settings → Root Directory = `web`
- Redeploy com "Clear build cache" (se existir)
- Testar: `https://triumphant-perception.../clinics` — deve redirecionar 307 para `/clinica`

---

### 2. Causa do botão "Criar Clínica" não funcionar

**Diagnóstico:** O fluxo em código está correto. Se não funciona, as causas mais prováveis são:

1. **CORS** — Browser bloqueia a requisição; erro de rede no console (ex.: "CORS policy")
2. **Token inválido ou ausente** — 401; `showError` exibe mensagem
3. **NEXT_PUBLIC_API_URL incorreta** — Chamada vai para URL errada; `[api] API_BASE_URL` no console indica o valor em uso
4. **Erro no catch** — `showError` é chamada, mas usuário pode não notar; logs `[handleCreateClinic]` ajudam

**Correção exata:**
- Conferir console do browser ao clicar "Criar"
- Se erro CORS: adicionar `CORS_ORIGIN` no backend (Railway) com a URL do frontend
- Se 401: refazer login

---

### 3. Matriz de causas prováveis

| Causa | Sintoma | Como verificar | Correção |
|-------|---------|----------------|----------|
| CORS | Erro de rede / CORS no console | Network tab, erro CORS | `CORS_ORIGIN` no backend |
| Token inválido | 401 na resposta | Network tab, resposta 401 | Refazer login |
| Env incorreta | Chamada para localhost em produção | Console `[api] API_BASE_URL` | Definir `NEXT_PUBLIC_API_URL` no Railway (web) |
| Build antigo | Redirect /clinics não funciona | Redeploy e teste | Redeploy com cache limpo |
| Root Directory errado | 404, redirect não funciona | Settings do Railway | Root Directory = `web` |
| Erro silencioso no catch | Toast de erro | Console `[handleCreateClinic] Erro` | Ver mensagem e stack |
| Guard JWT falha | 401 | Resposta do backend | Token válido, JWT_SECRET consistente |
| Redirect App Router | 404 em /clinics | Acessar /clinics no browser | Depende de config correta no deploy |

---

## Alterações feitas no código

1. **web/app/services/api.ts** — Log: `console.log("[api] API_BASE_URL:", ...)` (client-side)
2. **web/app/clinica/page.tsx** — Logs em `handleCreateClinic`
3. **web/app/test/page.tsx** — Botão "Testar POST /clinics" com logs `[AUDIT]`

---

## Comandos / passos recomendados

### Railway — Backend (Psipro-backend)
1. Variables → adicionar ou editar: `CORS_ORIGIN=https://triumphant-perception-production-8792.up.railway.app,http://localhost:3000`
2. Redeploy (geralmente automático ao salvar variáveis)

### Railway — Frontend (triumphant-perception)
1. Settings → Root Directory = `web`
2. Variables → `NEXT_PUBLIC_API_URL=https://psipro-backend-production.up.railway.app/api`
3. Redeploy → usar "Clear build cache" se houver

### Teste em produção
1. Abrir DevTools (F12) → aba Console
2. Acessar `/clinica` (ou clicar em Clínicas)
3. Verificar `[api] API_BASE_URL:` no console
4. Fazer login
5. Clicar em "Criar Clínica", preencher nome e criar
6. Acompanhar logs `[handleCreateClinic]` e erros na aba Network

### Página de teste
1. Ir em `/test`
2. Fazer login
3. Clicar em "Testar POST /clinics"
4. Conferir `[AUDIT]` no console e status/response
