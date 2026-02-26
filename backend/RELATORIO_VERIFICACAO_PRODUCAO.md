# Relatório — Verificação e Ajustes de Produção

**Data:** 12/02/2025  
**Projeto:** PsiPro Backend (NestJS)

---

## 1️⃣ HELMET

### Encontrado
- `app.use(helmet())` sem configuração específica.

### Alterado
- Configuração adicionada com `crossOriginResourcePolicy: { policy: 'cross-origin' }` para evitar conflito com frontend em outro domínio (Railway/Web).

```ts
app.use(
  helmet({
    crossOriginResourcePolicy: { policy: 'cross-origin' },
  }),
);
```

---

## 2️⃣ RATE LIMIT

### Encontrado
- `ttl: 1000`, `limit: 10` (10 req/segundo por IP).

### Alterado
- `ttl: 60`, `limit: 100` (100 requisições por minuto por IP).
- `@SkipThrottle()` em `/health` mantido.

---

## 3️⃣ GLOBAL EXCEPTION FILTER

### Encontrado
- Resposta poderia incluir campos extras vindos de `HttpException.getResponse()`.
- Stack trace não era tratado de forma explícita por ambiente.

### Alterado
- Em produção: resposta contém apenas `statusCode`, `message`, `timestamp`, `path`.
- Stack trace nunca é enviado ao cliente.
- Stack só é logado quando `NODE_ENV !== 'production'`.
- Resposta padronizada para todos os erros.

---

## 4️⃣ SWAGGER

### Encontrado
- Condição `process.env.NODE_ENV === 'production'` com `!isProduction`.

### Alterado
- Condição simplificada para `process.env.NODE_ENV !== 'production'`.
- Swagger continua desativado em produção.

---

## 5️⃣ HEALTH ENDPOINT

### Encontrado
- `@Controller()` com `@Get('health')` e método `check()`.

### Alterado
- `@Controller('health')` com `@Get()` e método `getHealth()`.
- Mantido fora do prefixo `/api` via `exclude: ['health']`.
- Sem autenticação.
- `@SkipThrottle()` mantido.

---

## 6️⃣ VERIFICAÇÕES FINAIS

| Item                    | Status |
|-------------------------|--------|
| NODE_ENV lido corretamente | ✅ |
| PORT (process.env.PORT \|\| 3000) | ✅ |
| app.listen(port, '0.0.0.0') | ✅ |
| npm run build sem erros | ✅ |

---

## Arquivos modificados

| Arquivo | Alterações |
|---------|------------|
| `src/main.ts` | Helmet com `crossOriginResourcePolicy`, condição do Swagger simplificada |
| `src/app.module.ts` | Throttler: ttl 60, limit 100 |
| `src/common/filters/http-exception.filter.ts` | Resposta padronizada, stack só em dev, sem stack no cliente |
| `src/health/health.controller.ts` | `@Controller('health')`, método `getHealth()` |

---

## Build status

```
✅ npm run build — sucesso
```
