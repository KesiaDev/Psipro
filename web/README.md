# PsiPro Web

Plataforma web para gestão e análise de clínicas psicológicas.

## 🚀 Como Rodar Localmente

### Pré-requisitos

- Node.js 18+ 
- npm ou yarn

### Instalação

```bash
# Instalar dependências
npm install
```

### Variáveis de Ambiente

Crie um arquivo `.env.local` na raiz do projeto:

```env
NEXT_PUBLIC_API_URL=http://localhost:3001/api
```

**Variáveis disponíveis:**
- `NEXT_PUBLIC_API_URL` - URL base da API backend (padrão: `http://localhost:3001/api`)

### Executar

```bash
# Modo desenvolvimento
npm run dev

# Build de produção
npm run build

# Executar build de produção
npm start
```

A aplicação estará disponível em `http://localhost:3000`.

## 📁 Estrutura do Projeto

```
app/
├── (auth)/              # Páginas de autenticação
│   ├── login/
│   └── register/
├── components/          # Componentes reutilizáveis
│   ├── dashboard/       # Componentes do dashboard
│   ├── paciente/        # Componentes de pacientes
│   └── onboarding/      # Componentes de onboarding
├── contexts/            # Contexts React (Auth, Clinic, Toast, etc)
├── dashboard/           # Página do dashboard
├── insights/            # Sistema de insights
├── pacientes/           # Páginas de pacientes
├── services/            # Services de API
├── utils/               # Utilitários
└── layout.tsx           # Layout raiz

```

## 🔐 Fluxo de Autenticação

1. **Registro/Login**: Usuário se registra ou faz login em `/register` ou `/login`
2. **Token JWT**: Token é armazenado no `localStorage` como `psipro_token`
3. **Proteção de Rotas**: `AuthGuard` verifica autenticação antes de renderizar páginas protegidas
4. **API Requests**: Token é automaticamente incluído no header `Authorization: Bearer <token>` via `api.ts`

### Rotas Públicas

- `/` - Landing page
- `/beta` - Página de solicitação de acesso beta
- `/login` - Login
- `/register` - Registro

### Rotas Protegidas

Todas as outras rotas requerem autenticação:
- `/dashboard` - Dashboard principal
- `/pacientes` - Lista de pacientes
- `/pacientes/[id]` - Detalhes do paciente
- `/agenda` - Agenda
- `/financeiro` - Financeiro
- `/clinica` - Gestão de clínica

## 🔌 Integração com API

### Services

Os services estão em `app/services/`:

- **`api.ts`** - Cliente HTTP centralizado (intercepta tokens, trata erros)
- **`authService.ts`** - Autenticação (login, registro, logout)
- **`clinicService.ts`** - Gestão de clínicas
- **`patientService.ts`** - Gestão de pacientes
- **`appointmentService.ts`** - Gestão de consultas
- **`dashboardService.ts`** - Dados do dashboard

### Uso Básico

```typescript
import { patientService } from '@/app/services/patientService';

// Listar pacientes
const patients = await patientService.getPatients(clinicId);

// Criar paciente
const newPatient = await patientService.createPatient({
  name: "João Silva",
  email: "joao@example.com",
  // ...
});
```

### Tratamento de Erros

O `api.ts` intercepta automaticamente:
- **401**: Remove token e pode redirecionar para login
- **403**: Erro de permissão
- **500**: Erro do servidor

Erros são lançados como `ApiError` com `message`, `status` e `errors`.

## 📊 Dashboard

O dashboard consome dados de:

- `GET /api/dashboard/metrics` - Métricas (KPIs)
- `GET /api/dashboard/agenda-summary` - Resumo de agenda
- `GET /api/dashboard/finance-summary` - Resumo financeiro

**Fallback**: Se os endpoints não existirem, o `dashboardService` calcula dados usando `patientService` e `appointmentService`.

## 🧠 Sistema de Insights

O sistema de insights gera observações automáticas sobre:
- Agenda (distribuição, faltas, cancelamentos)
- Financeiro (receita, valores pendentes)
- Pacientes (administrativo apenas)

**Localização**: `app/insights/`

**Uso**: O dashboard usa `generateInsights()` do `InsightEngine` para gerar insights a partir dos dados carregados.

## 🎓 Onboarding

O onboarding guiado aparece automaticamente no primeiro acesso ao dashboard.

**Localização**: `app/components/onboarding/` e `app/contexts/OnboardingContext.tsx`

**Estado**: Armazenado no `localStorage` (`psipro-onboarding-completed`)

## 🚢 Deploy

### Railway

O projeto está configurado para deploy no Railway via `railway.json`.

**Variáveis de ambiente no Railway:**
- `NEXT_PUBLIC_API_URL` - URL do backend (ex: `https://backend.railway.app/api`)

**Build Command**: `npm run build`
**Start Command**: `npm start`

## 🔧 Scripts Disponíveis

- `npm run dev` - Inicia servidor de desenvolvimento
- `npm run build` - Build de produção
- `npm start` - Executa build de produção
- `npm run lint` - Executa ESLint

## 📝 Notas Importantes

- **Token JWT**: Armazenado no `localStorage` (não é HTTP-only, considere revisar em produção)
- **CORS**: Backend precisa configurar CORS para aceitar requisições do frontend
- **Error Boundary**: Integrado no layout raiz para capturar erros de renderização
- **Beta Access**: Formulário de solicitação tenta enviar para `/api/beta/request`, mas funciona mesmo se endpoint não existir

## 🤝 Contribuindo

1. Siga a estrutura de pastas existente
2. Use TypeScript para type safety
3. Mantenha componentes reutilizáveis em `app/components/`
4. Services de API em `app/services/`
5. Contexts para estado global em `app/contexts/`

---

**Desenvolvido para PsiPro** 🧠
