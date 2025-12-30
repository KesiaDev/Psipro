# PsiPro Backend API

API única e central para sincronização entre App Android e Web do PsiPro.

## Arquitetura

```
App Android (Room local) ↔ API Backend ↔ PostgreSQL ↔ Web (Next.js)
```

## Princípios

- **Offline-first**: App funciona sem internet
- **Sincronização bidirecional**: Dados fluem em ambas direções
- **Resolução de conflitos**: Por timestamp (última atualização vence)
- **Segurança**: JWT, HTTPS, escopo por usuário

## Tecnologias

- **Framework**: NestJS
- **Banco de Dados**: PostgreSQL
- **ORM**: Prisma
- **Autenticação**: JWT + Passport
- **Validação**: class-validator

## Setup

### 1. Instalar dependências

```bash
npm install
```

### 2. Configurar banco de dados

Copie `.env.example` para `.env` e configure:

```env
DATABASE_URL="postgresql://user:password@localhost:5432/psipro"
JWT_SECRET="your-secret-key"
```

### 3. Executar migrações

```bash
npm run prisma:generate
npm run prisma:migrate
```

### 4. Iniciar servidor

```bash
# Desenvolvimento
npm run start:dev

# Produção
npm run build
npm run start:prod
```

## Estrutura do Projeto

```
backend/
├── src/
│   ├── auth/          # Autenticação e autorização
│   ├── patients/      # CRUD de pacientes
│   ├── appointments/  # Agendamentos
│   ├── sessions/      # Sessões realizadas
│   ├── payments/      # Pagamentos
│   ├── financial/     # Resumo financeiro
│   ├── documents/     # Documentos
│   ├── insights/      # Insights (futuro)
│   └── common/        # Utilitários compartilhados
├── prisma/
│   └── schema.prisma  # Schema do banco
└── test/              # Testes
```

## Endpoints Principais

### Autenticação
- `POST /auth/login` - Login
- `POST /auth/refresh` - Refresh token

### Pacientes
- `GET /patients` - Listar pacientes
- `POST /patients` - Criar paciente
- `PUT /patients/:id` - Atualizar paciente
- `GET /patients/:id` - Detalhes do paciente

### Sessões
- `GET /sessions` - Listar sessões
- `POST /sessions` - Registrar sessão realizada
- `GET /sessions/patient/:patientId` - Sessões de um paciente

### Financeiro
- `GET /financial/summary` - Resumo financeiro
- `GET /financial/patient/:patientId` - Financeiro do paciente

## Sincronização

O app Android envia dados quando:
- Há conexão com internet
- Em background (não bloqueia uso)
- Automaticamente após ações importantes

A API:
- Recebe dados do app
- Persiste no banco central
- Disponibiliza para a web
- Resolve conflitos por timestamp

## Segurança

- Todos os endpoints (exceto login) requerem JWT
- Dados isolados por usuário (userId)
- HTTPS obrigatório em produção
- Refresh tokens para renovação segura

## Próximos Passos

1. ✅ Estrutura base criada
2. ⏳ Implementar módulos principais
3. ⏳ Testes de sincronização
4. ⏳ Integração com app Android
5. ⏳ Integração com web
6. ⏳ Geração de insights (IA)



