# Resolver migração Prisma P3009 falha

O script `migrate:resolve-deploy` executa `prisma migrate resolve --rolled-back` antes de `migrate deploy`. O `start:prod` usa esse script.

Se o deploy ainda falhar, execute **manualmente** com a `DATABASE_URL` do Postgres no Railway:

```bash
cd backend
npx prisma migrate resolve --rolled-back 20260303100000_add_anamnese
npx prisma migrate deploy
```

**Via Railway CLI:**
```bash
railway run npx prisma migrate resolve --rolled-back 20260303100000_add_anamnese
railway run npx prisma migrate deploy
```

Documentação: https://pris.ly/d/migrate-resolve
