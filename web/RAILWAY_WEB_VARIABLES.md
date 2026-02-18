# Variáveis de ambiente — Web (Railway)

Para o frontend Next.js chamar o backend correto em produção, configure no Railway:

## Serviço Web (triumphant-perception ou equivalente)

1. Abra o projeto no Railway → serviço **Web** (frontend Next.js).
2. Vá em **Variables**.
3. Adicione ou edite:

| Variável | Valor | Obrigatório |
|----------|--------|-------------|
| `NEXT_PUBLIC_API_URL` | `https://psipro-backend-production.up.railway.app/api` | Sim |

**Importante:** a URL deve terminar em `/api` (o backend expõe as rotas em `/api`).

4. Salve. Um novo deploy será disparado e as chamadas de API passarão a usar o backend no Railway.

## Verificação

- Todas as chamadas (login, clínicas, pacientes, etc.) usam o cliente centralizado em `app/services/api.ts`, que lê `NEXT_PUBLIC_API_URL`.
- Se a variável não estiver definida em produção, o frontend usará o fallback de desenvolvimento (`http://localhost:3001/api`) e as requisições falharão (404 ou CORS).
