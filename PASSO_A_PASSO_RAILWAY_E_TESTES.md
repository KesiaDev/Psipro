# Passo a passo: Migrations em produção e testes

## Parte 1 — Rodar migrations no banco de produção (Railway)

### 1.1 Variável DATABASE_URL no Railway

1. No Railway, abra o projeto e o serviço **Psipro-backend**.
2. Vá em **Variables** (ou **Settings** → **Variables**).
3. Confirme que existe **DATABASE_URL** apontando para o PostgreSQL de produção (Railway costuma criar isso ao adicionar o banco ao projeto).
4. Se não existir, adicione: `DATABASE_URL` = a URL do seu banco (ex.: `postgresql://postgres:senha@host:porta/railway?schema=public`).

### 1.2 Rodar migrations (deploy do schema)

**Opção A — Pelo Railway (recomendado)**

1. No serviço **Psipro-backend**, abra a aba **Settings**.
2. Em **Build** ou **Deploy**, veja se há **Build Command** e **Start Command**.
3. Para rodar migrations **uma vez** antes do start, você pode:
   - **Opção 1:** Em **Deploy** → **Custom Start Command**, usar:
     ```bash
     npx prisma migrate deploy && node dist/main
     ```
     (Assim toda vez que o serviço subir, as migrations rodam antes.)
   - **Opção 2:** Rodar manualmente uma vez (ver Opção B) e deixar o Start Command normal: `node dist/main` (ou `npm run start:prod`).

**Opção B — Rodar manualmente (uma vez)**

1. No Railway, no serviço **Psipro-backend**, abra **Settings**.
2. Procure **One-off command** / **Run command** / **Shell** (ou use a aba que permite executar comando no container).
3. Se houver terminal/shell do deploy:
   - Root directory: **backend** (se o projeto monorepo estiver na raiz).
   - Comando:
     ```bash
     npx prisma migrate deploy
     ```
4. Se o Railway não tiver “run command”, use o **Custom Start Command** da Opção A (migrations + start) pelo menos no próximo deploy; depois pode voltar só para `node dist/main` se quiser.

### 1.3 Migração de dados existentes (se já tinha dados antes do multi-tenant)

Só faça isso se o banco de **produção** já tinha usuários/pagamentos antes da migration.

1. No Railway, serviço **Psipro-backend** → aba **Settings**.
2. No campo onde você colocou o comando de start (ex.: **Custom Build Command** ou **Start Command** — o que roda quando o container sobe), use este comando completo (em uma linha):
   ```bash
   npx prisma generate && npx prisma migrate deploy && npx ts-node prisma/migrate-existing-data.ts && node dist/main
   ```
   Ordem: gera o Prisma Client → aplica migrations do schema → roda a migração de dados antigos → sobe a API.
3. Salve e faça um **novo deploy** (Redeploy ou push no GitHub). Na primeira subida o script cria a clínica `PSIPRO_DEFAULT`, associa usuários e preenche `clinicId` em financial_records/payments.
4. **(Opcional)** Depois do deploy ok, você pode remover a parte do script de dados e deixar só: `npx prisma generate && npx prisma migrate deploy && node dist/main`. O script de dados é idempotente, então também pode deixar o comando completo para sempre.

**Nota:** O `ts-node` foi colocado em `dependencies` no backend para estar disponível em produção.

Se o banco de produção for **novo** (sem dados antigos), pule o passo 1.3.

---

## Parte 2 — Testes na prática

### 2.1 URLs

- **Frontend (Next.js):** `https://triumphant-perception-production-8792.up.railway.app`
- **Backend (API):** `https://psipro-backend-production.up.railway.app`
- **API base:** `https://psipro-backend-production.up.railway.app/api`

### 2.2 Teste 1 — Redirect /clinics → /clinica

1. Abra no navegador:
   `https://triumphant-perception-production-8792.up.railway.app/clinics`
2. **Esperado:** a URL deve mudar para `/clinica` (redirect) e a página de clínica deve carregar, sem 404.

### 2.3 Teste 2 — Registro (cria usuário + clinic INDIVIDUAL)

1. No frontend, vá para a tela de **registro** (cadastro de usuário).
2. Preencha e registre um usuário novo.
3. **Esperado:** registro com sucesso; no backend, deve ter sido criada uma **Clinic** com `planType` INDIVIDUAL e o usuário associado como OWNER.

**Conferir na API (opcional):**

- Login para obter o token (POST `/api/auth/login`).
- GET `/api/auth/me` com o token: a resposta deve incluir `clinicId` e `role`.

### 2.4 Teste 3 — Login

1. Faça logout (se estiver logado) e faça **login** com um usuário existente.
2. **Esperado:** login ok; o token JWT deve incluir `clinicId` e `role` (o app e a API usam isso para multi-tenant).

### 2.5 Teste 4 — Criar clínica (POST /api/clinics)

1. Faça login e pegue o **accessToken** (pela interface ou pela resposta do login).
2. Envie um POST para criar clínica:
   - **URL:** `POST https://psipro-backend-production.up.railway.app/api/clinics`
   - **Headers:** `Authorization: Bearer <accessToken>`, `Content-Type: application/json`
   - **Body (JSON):** `{ "name": "Minha Clínica Teste" }`
3. **Esperado:** status 201; resposta com dados da clínica e um **novo** `accessToken` (com o novo `clinicId`). Use esse token nas próximas requisições.

**Exemplo com curl:**

```bash
curl -X POST "https://psipro-backend-production.up.railway.app/api/clinics" \
  -H "Authorization: Bearer SEU_TOKEN_AQUI" \
  -H "Content-Type: application/json" \
  -d "{\"name\": \"Minha Clínica Teste\"}"
```

### 2.6 Resumo rápido

| Passo | O que fazer | O que conferir |
|-------|-------------|----------------|
| 1     | Rodar `npx prisma migrate deploy` no backend (Railway) | Sem erros; tabelas/enums multi-tenant existem |
| 2     | (Opcional) Rodar `migrate-existing-data.ts` se havia dados antigos | Usuários com clinicId; financial_records/payments com clinicId |
| 3     | Abrir `/clinics` no front | Redirect para `/clinica`, sem 404 |
| 4     | Registrar novo usuário | Clinic INDIVIDUAL criada; usuário OWNER |
| 5     | Login | Token com clinicId e role |
| 6     | POST `/api/clinics` com JWT | 201; novo token com novo clinicId |

---

## Script no package.json (backend)

Foi adicionado o script de migration para produção:

- `npm run prisma:deploy` → executa `prisma migrate deploy` (uso em produção / Railway).

Em produção, use sempre `prisma migrate deploy`; não use `prisma migrate dev` (é só para desenvolvimento).
