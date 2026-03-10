# Resolver migração Prisma P3009 falha

Se o deploy falhar com `P3009: migrate found failed migrations`, o script `start:prod` já tenta executar `prisma migrate resolve --rolled-back` automaticamente antes de `migrate deploy`.

Se ainda falhar, execute manualmente no seu ambiente (com `DATABASE_URL` do Railway):

```bash
npx prisma migrate resolve --rolled-back 20260303100000_add_anamnese
npx prisma migrate deploy
```

Documentação: https://pris.ly/d/migrate-resolve
