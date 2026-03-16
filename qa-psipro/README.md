# PsiPro QA Agent V3

Agente inteligente de validação da plataforma PsiPro — App Android + Dashboard Web + Backend.

## Quick Start

```bash
cd qa-psipro
npm install
npm run test:api    # Testa API (mais rápido)
# ou
npm run test        # Pipeline completo (infra + api + web + ai)
```

Configure `AUTH_USER` e `AUTH_PASS` via variáveis de ambiente ou GitHub Secrets (CI). Veja `.env.example` para referência.

| Versão | Função |
|--------|--------|
| **V1** | Testes básicos |
| **V2** | Cobertura completa (8 camadas) |
| **V3** | Exploração automática + descoberta de bugs |

## Arquitetura (8 Camadas + Explorer)

| Camada | Função | Detecta |
|--------|--------|---------|
| **1. Infra** | Production monitoring | Deploy quebrado |
| **2. API** | Backend NestJS | Endpoint quebrado |
| **3. Web** | Dashboard E2E | Bug UI |
| **4. Mobile** | Jetpack Compose | Erro mobile |
| **5. AI** | IA clínica | Erro IA |
| **6. Sync** | Mobile ↔ Backend | Falha de sincronização |
| **7. Security** | Auth, LGPD | Erro segurança |
| **8. Performance** | Load, stress | Lentidão |

## Estrutura

```
qa-psipro/
├── tests/
│   ├── infra/           # Layer 1 - Production Monitoring
│   │   ├── health.spec.ts
│   │   ├── database.spec.ts
│   │   ├── railway.spec.ts
│   │   ├── latency.spec.ts
│   │   └── system-health.spec.ts
│   ├── api/             # Layer 2 - Backend API
│   │   ├── auth.spec.ts
│   │   ├── pacientes.spec.ts
│   │   ├── agenda.spec.ts
│   │   ├── prontuario.spec.ts
│   │   ├── clinics.spec.ts
│   │   ├── sessions.spec.ts
│   │   ├── financial.spec.ts
│   │   ├── reports.spec.ts
│   │   └── integrations.spec.ts
│   ├── web/             # Layer 3 - Dashboard Web
│   │   ├── login.spec.ts
│   │   ├── auth-flow.spec.ts
│   │   ├── dashboard.spec.ts
│   │   ├── pacientes.spec.ts
│   │   ├── agenda.spec.ts
│   │   ├── prontuario.spec.ts
│   │   ├── relatorios.spec.ts
│   │   ├── clinics.spec.ts
│   │   ├── professionals.spec.ts
│   │   ├── financeiro.spec.ts
│   │   ├── settings.spec.ts
│   │   ├── system-health.spec.ts
│   │   └── voice-command.spec.ts
│   ├── mobile/          # Layer 4 - Android (Jetpack Compose)
│   │   ├── LoginTest.kt
│   │   ├── PacienteTest.kt
│   │   ├── AgendaTest.kt
│   │   ├── ProntuarioTest.kt
│   │   ├── DashboardTest.kt
│   │   ├── NotificationsTest.kt
│   │   ├── SyncTest.kt
│   │   ├── VoiceTest.kt
│   │   └── AccessibilityTest.kt
│   ├── ai/              # Layer 5 - IA Clínica
│   │   ├── voice-transcription.spec.ts
│   │   ├── insights.spec.ts
│   │   ├── emotion-analysis.spec.ts
│   │   └── patterns.spec.ts
│   ├── sync/            # Layer 6 - Sync Engine
│   │   ├── patients-sync.spec.ts
│   │   ├── appointments-sync.spec.ts
│   │   ├── sessions-sync.spec.ts
│   │   └── documents-sync.spec.ts
│   ├── security/        # Layer 7 - Security & LGPD
│   │   ├── auth.spec.ts
│   │   ├── token.spec.ts
│   │   ├── permissions.spec.ts
│   │   └── lgpd.spec.ts
│   └── performance/     # Layer 8 - Performance
│       ├── load.spec.ts
│       └── stress.spec.ts
│   └── explorer/        # V3 - Exploração automática
│       └── explorer.spec.ts
├── agents/              # V3 - Explorer Agent
│   └── explorer-agent.ts
├── helpers/
│   ├── login-web.ts
│   └── login-api.ts
├── playwright.config.ts
└── package.json
```

## Pré-requisitos

- Node.js 18+
- Credenciais válidas do PsiPro

## Variáveis de Ambiente

| Variável | Descrição | Padrão |
|----------|-----------|--------|
| `BASE_URL` | URL do dashboard web | https://psipro-dashboard-production.up.railway.app |
| `API_URL` | URL do backend API | https://psipro-backend-production.up.railway.app |
| `API_PATH_PREFIX` | Prefixo das rotas (ex: `api` para `/api/auth/login`) | api |
| `AUTH_USER` | Email para login | terapeutaclaudiacruz@gmail.com |
| `AUTH_PASS` | Senha | senha123 |
| `LATENCY_LOGIN_MS` | Limite latência login (ms) | 5000 |
| `LATENCY_ENDPOINT_MS` | Limite latência endpoints (ms) | 5000 |
| `LOGIN_DELAY_MS` | Delay antes do login (evita 429) | 1500 |

**404 no login?** O backend pode usar prefixo nas rotas. Tente:
```powershell
$env:API_PATH_PREFIX="api"
npm run test:api
```

## Pipeline de Execução

```
1. infra → 2. api → 3. web → 4. mobile → 5. sync → 6. ai → 7. security → 8. performance
```

## Comandos

```bash
# Pipeline principal (infra + api + web + ai)
npm run test

# V3: Testes + Explorer Agent (exploração automática)
npm run test:v3

# Explorer Agent apenas (login → navegar → detectar erros → relatório)
npm run explore

# Por camada
npm run test:infra
npm run test:api
npm run test:web
npm run test:ai
npm run test:sync
npm run test:security
npm run test:performance
npm run test:explorer

# Todos os testes
npm run test:all

# Relatório HTML
npm run report
```

## Explorer Agent (V3)

O agente explora automaticamente a plataforma e detecta:

- Páginas quebradas (status 4xx/5xx)
- Erros de JavaScript
- Rotas protegidas falhando
- Regressões de deploy

**Relatório:** `reports/explorer-report.json`

## Mobile (Android)

Os testes Kotlin em `tests/mobile/` integram ao projeto `psipro-app`:

```bash
cd psipro-app
./gradlew connectedAndroidTest
```

Veja `tests/mobile/README.md` para detalhes.

## Configuração CI/CD (GitHub Actions)

O workflow roda automaticamente:
- **Em push** para a branch `main`
- **Todo dia** às 9h UTC (6h Brasília)
- **Manual** — Actions → PsiPro QA → Run workflow

### Secrets (obrigatórios)

Em **Settings → Secrets and variables → Actions**:

| Secret | Descrição |
|--------|-----------|
| `AUTH_USER` | Email do usuário de teste |
| `AUTH_PASS` | Senha do usuário de teste |

### Variáveis (opcionais)

| Variável | Descrição | Padrão |
|----------|-----------|--------|
| `BASE_URL` | URL do dashboard | https://psipro-dashboard-production.up.railway.app |
| `API_URL` | URL da API | https://psipro-backend-production.up.railway.app |
| `API_PATH_PREFIX` | Prefixo das rotas | api |
| `LOGIN_DELAY_MS` | Delay entre logins (evita 429) | 2000 |

> **Primeira vez?** Crie os secrets e faça um push ou rode o workflow manualmente.

## Relatórios

- **playwright-report/** – HTML interativo com screenshots, falhas, tempo
- **reports/junit.xml** – JUnit para CI
- **reports/explorer-report.json** – Resultados da exploração V3
- **test-results/** – traces e vídeos de falhas
