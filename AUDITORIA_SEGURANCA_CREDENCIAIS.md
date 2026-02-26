# Auditoria de Segurança — Credenciais e Segredos

**Data:** 12/02/2025  
**Escopo:** Projeto PsiPro (web, backend, android)

---

## 1. RESUMO EXECUTIVO

| Categoria | Status | Prioridade |
|-----------|--------|------------|
| Segredos versionados | ⚠️ Encontrados | ALTA |
| JWT_SECRET hardcoded | ✅ Não encontrado | — |
| DATABASE_URL exposta | ⚠️ Em documentação | MÉDIA |
| Keystore versionado | ✅ Não encontrado | — |
| Senhas de teste versionadas | ⚠️ Encontradas | ALTA |
| API key Firebase versionada | ⚠️ Encontrada | MÉDIA |

---

## 2. ITENS ENCONTRADOS

### 2.1 Senha de teste hardcoded (`senha123`)

**Severidade:** ALTA  
**Arquivos afetados:**

| Arquivo | Linha | Descrição |
|---------|-------|-----------|
| `backend/prisma/seed.ts` | 9, 18, 31, 43, 168-170 | Senha usada para criar usuários de teste; impressa no console |
| `web/app/test/page.tsx` | 13, 267 | Valor padrão do campo senha na página de teste |
| `backend/SETUP.md` | 61, 84 | Documentação com credenciais de exemplo |
| `backend/API.md` | 30, 252 | Exemplos de requisição com senha |
| `backend/DOCKER.md` | — | Referência indireta |
| `RESUMO_IMPLEMENTACAO.md` | 93 | Credenciais documentadas |
| `CORRECAO_URGENTE.md` | 70 | Credenciais documentadas |

**Impacto:** Qualquer pessoa com acesso ao repositório conhece credenciais de teste. Em produção, seed não deve usar senhas fixas ou previsíveis.

---

### 2.2 DATABASE_URL com credenciais expostas

**Severidade:** MÉDIA  
**Arquivos afetados:**

| Arquivo | Linha | Conteúdo exposto |
|---------|-------|------------------|
| `backend/SETUP.md` | 23, 44 | `postgresql://user:password@...` e `postgresql://psipro:psipro123@...` |
| `backend/README.md` | 39 | `postgresql://user:password@localhost:5432/psipro` |
| `backend/DOCKER.md` | 9, 21 | `POSTGRES_PASSWORD=psipro123` e `psipro:psipro123` em exemplos |

**Observação:** O schema Prisma usa `env("DATABASE_URL")` corretamente; a exposição está apenas na documentação, mas a senha `psipro123` é reutilizada em vários contextos.

---

### 2.3 JWT_SECRET

**Status:** ✅ Nenhum valor hardcoded no código

- `backend/src/auth/auth.module.ts` — usa `config.get<string>('JWT_SECRET')`
- `backend/src/auth/strategies/jwt.strategy.ts` — usa `configService.get<string>('JWT_SECRET')`
- Documentação (`README.md`, `SETUP.md`) contém apenas placeholders: `your-secret-key`, `sua-chave-secreta-super-segura-aqui`

**Recomendação:** Garantir que `JWT_SECRET` nunca seja commitado em `.env`. Manter `.env` no `.gitignore` (já está).

---

### 2.4 Firebase API Key versionada

**Severidade:** MÉDIA  
**Arquivo:** `android/app/google-services (3).json`

```json
"current_key": "AIzaSyAZ_Iy6K3rVU0_i3gw9r_6KV7ZSLfAQOHo"
```

**Observações:**
- Arquivo com nome anômalo `google-services (3).json` (possível duplicata)
- Firebase API keys para Android costumam ser consideradas públicas (ficam no APK)
- Boa prática: restringir no Firebase Console (package name, APIs permitidas) e rotacionar se houver suspeita de vazamento

---

### 2.5 Keystore Android

**Status:** ✅ Nenhum arquivo `.jks` ou `keystore` versionado

- `.gitignore` cobre `android/app/release/` e `android/app/debug/`
- Nenhum arquivo de keystore encontrado no repositório
- Documentação cita Android Keystore apenas em termos conceituais

---

### 2.6 Arquivos de ambiente (.env)

**Status:** ✅ Adequado

- `.gitignore` inclui `.env`, `.env.local`, etc.
- Nenhum `.env` real encontrado no repositório
- Não existe `.env.example` versionado (mencionado em docs, mas ausente)

---

## 3. PLANO DE ROTAÇÃO DE CREDENCIAIS

### Fase 1 — Imediato (24–48h)

#### 1.1 Senhas de teste em código e docs

| Ação | Detalhes |
|------|----------|
| Remover senha fixa do seed | Usar `process.env.SEED_PASSWORD` ou gerar senha aleatória e imprimir uma única vez no console |
| Atualizar página de teste | Remover valor padrão `senha123`; usar string vazia ou placeholder |
| Atualizar documentação | Substituir senhas reais por placeholders, ex: `[SENHA_DO_SEED]` |
| Rotação no banco | Após alterar seed: rodar migração/reset em dev, recriar usuários com novas senhas |

#### 1.2 Credenciais PostgreSQL (se `psipro123` estiver em uso)

| Ação | Detalhes |
|------|----------|
| Gerar nova senha | Mín. 32 caracteres, alfanumérica + símbolos |
| Atualizar `DATABASE_URL` | Em `.env` (local e produção) |
| Atualizar PostgreSQL | `ALTER USER psipro PASSWORD 'nova_senha';` |
| Atualizar documentação | Remover exemplos com senhas reais; usar `[SUA_SENHA]` |

---

### Fase 2 — Curto prazo (1 semana)

#### 2.1 JWT_SECRET

| Ação | Detalhes |
|------|----------|
| Gerar novo secret | `openssl rand -base64 48` |
| Atualizar produção | Substituir em variáveis de ambiente (Railway, etc.) |
| Invalidar sessões | Usuários precisarão fazer login novamente |
| Comunicar usuários | Aviso prévio de manutenção / logout forçado |

#### 2.2 Firebase (Android)

| Ação | Detalhes |
|------|----------|
| Conferir restrições | Firebase Console → API key → restringir por app/package e APIs |
| Padronizar arquivo | Renomear para `google-services.json` se for o oficial |
| Rotação (opcional) | Firebase Console → APIs & Services → Credentials → criar nova API key, atualizar app, desativar antiga |
| Remover duplicatas | Manter apenas um `google-services.json` por build variant |

---

### Fase 3 — Reforço preventivo

#### 3.1 `.gitignore`

Adicionar (se ainda não estiver):

```
# Credenciais e segredos
*.jks
*.keystore
*.p12
*.pfx
**/google-services*.json
!**/google-services.json.example
```

**Nota:** `google-services.json` costuma ser versionado em projetos Android. Se não quiser versionar, adicione ao `.gitignore` e crie um `google-services.json.example` sem chaves reais.

#### 3.2 `.env.example`

Criar `backend/.env.example`:

```env
DATABASE_URL="postgresql://user:password@localhost:5432/psipro?schema=public"
JWT_SECRET="gere-com-openssl-rand-base64-48"
JWT_EXPIRES_IN="7d"
PORT=3001
CORS_ORIGIN="http://localhost:3000"
SEED_PASSWORD="senha-temporaria-dev-apenas"
```

**Não** versionar valores reais.

#### 3.3 Seed em produção

- Não executar `prisma seed` em produção com senhas fixas.
- Usuários iniciais criados via script separado ou fluxo de onboarding, com senhas escolhidas pelos usuários.

#### 3.4 Ferramentas de detecção

- **GitHub Secret Scanning** — ativar no repositório.
- **gitleaks** ou **truffleHog** — rodar em CI para evitar commits com segredos.
- **pre-commit** — hook para verificar antes do push.

---

## 4. CHECKLIST DE EXECUÇÃO

- [ ] Alterar `backend/prisma/seed.ts` para não usar `senha123` hardcoded
- [ ] Atualizar `web/app/test/page.tsx` (remover senha padrão)
- [ ] Substituir credenciais reais por placeholders em `.md`
- [ ] Rotar senha PostgreSQL se `psipro123` estiver em uso
- [ ] Rotar JWT_SECRET em produção
- [ ] Revisar restrições da API key do Firebase
- [ ] Padronizar/renomear `google-services (3).json`
- [ ] Adicionar regras extras ao `.gitignore`
- [ ] Criar `backend/.env.example`
- [ ] Configurar detecção de segredos (gitleaks, etc.)

---

## 5. REFERÊNCIAS

- [OWASP - Secrets Management](https://cheatsheetseries.owasp.org/cheatsheets/Secrets_Management_Cheat_Sheet.html)
- [GitHub - Removing sensitive data](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/removing-sensitive-data-from-a-repository)
- [Firebase - Restrict API keys](https://firebase.google.com/docs/projects/api-keys)
