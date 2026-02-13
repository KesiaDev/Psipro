# Setup Rápido - PsiPro Backend

## Pré-requisitos

- Node.js 18+ instalado
- PostgreSQL instalado e rodando
- npm ou yarn

## Passo a Passo

### 1. Instalar dependências

```bash
cd backend
npm install
```

### 2. Configurar banco de dados

Crie um arquivo `.env` na raiz do backend:

```env
DATABASE_URL="postgresql://user:password@localhost:5432/psipro?schema=public"
JWT_SECRET="sua-chave-secreta-super-segura-aqui"
JWT_EXPIRES_IN="7d"
PORT=3001
CORS_ORIGIN="http://localhost:3000"
```

**Ou use Docker (recomendado):**

```bash
docker run --name psipro-db \
  -e POSTGRES_USER=psipro \
  -e POSTGRES_PASSWORD=psipro123 \
  -e POSTGRES_DB=psipro \
  -p 5432:5432 \
  -d postgres:15
```

E configure o `.env`:

```env
DATABASE_URL="postgresql://psipro:psipro123@localhost:5432/psipro?schema=public"
```

### 3. Executar migrações

```bash
npm run prisma:generate
npm run prisma:migrate
```

### 4. Popular banco (seed)

```bash
npm run prisma:seed
```

Isso criará:
- Usuário: `psicologo@psipro.com` / `senha123`
- 2 pacientes de exemplo

### 5. Iniciar servidor

```bash
# Desenvolvimento (com hot-reload)
npm run start:dev

# Produção
npm run build
npm run start:prod
```

A API estará disponível em: `http://localhost:3001/api`

## Testar a API

### Login

```bash
curl -X POST http://localhost:3001/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"psicologo@psipro.com","password":"senha123"}'
```

### Listar pacientes (com token)

```bash
curl http://localhost:3001/api/patients \
  -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

## Próximos Passos

1. ✅ Backend criado e funcionando
2. ⏳ Integrar com a web (substituir dados mockados)
3. ⏳ Criar SyncService no app Android
4. ⏳ Testar sincronização bidirecional
5. ⏳ Implementar geração de insights (IA)

## Estrutura Criada

```
backend/
├── src/
│   ├── auth/          ✅ Autenticação JWT
│   ├── patients/      ✅ CRUD de pacientes
│   ├── sessions/      ✅ Sessões realizadas
│   ├── payments/      ✅ Pagamentos
│   ├── financial/     ✅ Resumo financeiro
│   ├── appointments/  ✅ Agendamentos
│   ├── documents/     ✅ Documentos
│   └── insights/      ✅ Insights (estrutura pronta)
├── prisma/
│   └── schema.prisma  ✅ Schema completo
└── README.md          ✅ Documentação
```

## Notas Importantes

- O app Android **não depende** desta API para funcionar
- A API é uma camada de sincronização, não uma dependência
- Todos os dados são isolados por usuário (userId)
- Sincronização é bidirecional e resolve conflitos por timestamp




