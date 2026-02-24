# Baseline migration (P3005)

A migration inicial `20260224160141_init` é um **baseline**: o banco de produção já tinha o schema; ela existe para o Prisma reconhecer o estado atual sem recriar tabelas.

**Se no primeiro deploy no Railway ainda aparecer P3005** (schema not empty), marque esta migration como já aplicada **uma vez** no banco de produção:

```bash
npx prisma migrate resolve --applied "20260224160141_init"
```

Depois disso, `prisma migrate deploy` não tentará aplicar essa migration de novo.
