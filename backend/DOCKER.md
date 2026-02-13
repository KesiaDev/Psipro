# Docker Setup (Opcional)

Para facilitar o desenvolvimento, você pode usar Docker para o PostgreSQL:

```bash
# Criar container PostgreSQL
docker run --name psipro-db \
  -e POSTGRES_USER=psipro \
  -e POSTGRES_PASSWORD=psipro123 \
  -e POSTGRES_DB=psipro \
  -p 5432:5432 \
  -d postgres:15

# Conectar ao banco
docker exec -it psipro-db psql -U psipro -d psipro
```

Depois, configure o `.env`:

```env
DATABASE_URL="postgresql://psipro:psipro123@localhost:5432/psipro?schema=public"
```




