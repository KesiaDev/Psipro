# Relatório Final — Refatoração Backend NestJS para Produção (Railway)

**Data:** 12/02/2025  
**Objetivo:** Preparar o backend NestJS para deploy em produção no Railway.

---

## 1. Alterações no `package.json`

### Scripts ajustados:

| Script | Antes | Depois |
|--------|-------|--------|
| `build` | `nest build` | `nest build` (mantido) |
| `start` | `nest start` | **`node dist/src/main`** |
| `start:dev` | `nest start --watch` | `nest start --watch` (mantido) |
| `start:prod` | `node dist/main` | **Removido** (start já é produção) |
| `migrate:deploy` | — | **`prisma migrate deploy`** (novo) |

### Resultado

- `npm run build` gera a pasta `dist/` via `nest build`
- `npm start` executa `node dist/src/main` (produção)
- **Nota:** NestJS preserva a estrutura de pastas (`src/main.ts` → `dist/src/main.js`). O ponto de entrada é `dist/src/main.js`.
- `prisma migrate deploy` não roda no start; deve ser executado manualmente ou no build/release do Railway

---

## 2. Alterações no `main.ts`

| Item | Antes | Depois |
|------|-------|--------|
| Porta | `process.env.PORT \|\| 3001` | **`process.env.PORT \|\| 3000`** |
| Bind | `app.listen(port)` (localhost) | **`app.listen(port, '0.0.0.0')`** |
| CORS | `origin: corsOrigins` (lista) | **`origin: true`** |
| CORS credentials | `credentials: true` | `credentials: true` (mantido) |

### CORS

```ts
app.enableCors({
  origin: true,      // aceita qualquer origem
  credentials: true
});
```

### Bind `0.0.0.0`

- Necessário no Railway para aceitar conexões externas
- Sem isso, o app escuta só em localhost e não recebe requisições externas

---

## 3. `tsconfig.json`

- `outDir` permanece `./dist`
- `nest build` usa essa configuração e gera `dist/main.js` e demais arquivos compilados

---

## 4. `.gitignore`

- Removido `/dist` do `.gitignore`
- A pasta `dist/` passa a ser versionada conforme solicitado

---

## 5. Fluxo de deploy no Railway

1. **Build:** `npm run build` → gera `dist/`
2. **Start:** `npm start` → `node dist/src/main`
3. **Migrações:** executar `npm run migrate:deploy` em etapa separada (ex.: pre-deploy) ou manualmente

---

## 6. Checklist final

- [x] `npm run build` gera `dist/` corretamente
- [x] Script `start` é `node dist/src/main`
- [x] Script `build` é `nest build`
- [x] `prisma migrate deploy` não roda automaticamente no start
- [x] `tsconfig.json` aponta para `dist` (`outDir: "./dist"`)
- [x] `main.ts` usa `PORT` do ambiente e `listen(port, '0.0.0.0')`
- [x] CORS configurado com `origin: true` e `credentials: true`
- [x] `dist` não está no `.gitignore`

---

## 7. Variáveis de ambiente recomendadas no Railway

- `PORT` — definido automaticamente pelo Railway
- `DATABASE_URL` — connection string PostgreSQL
- `JWT_SECRET` — secret para tokens JWT
- `CORS_ORIGIN` — não é mais obrigatório (CORS está em `origin: true`)
