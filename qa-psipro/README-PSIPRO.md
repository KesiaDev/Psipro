# QA PsiPro - Integrado ao repositório Psipro

Este repositório (**KesiaDev/Psipro**) contém:
- `android/` — App Android (Jetpack Compose)
- `backend/` — API NestJS
- `web/` — Dashboard Web
- `qa-psipro/` — Suite de testes automatizados

## Testes em CI (GitHub Actions)

O workflow **PsiPro QA** roda automaticamente:
- Em todo **push** para `main`
- **Todo dia** às 9h UTC (6h Brasília)
- **Manual** — Actions → PsiPro QA → Run workflow

### Configuração obrigatória

Em **Settings → Secrets and variables → Actions** deste repositório (Psipro):

| Secret | Descrição |
|--------|-----------|
| `AUTH_USER` | Email do usuário de teste |
| `AUTH_PASS` | Senha do usuário de teste |

As URLs padrão apontam para produção no Railway.

---

## Testes locais

### API + Web (Playwright)

```bash
cd qa-psipro
npm install
npm run test:api    # Só API
npm run test        # infra + api + web + ai
```

### Android (emulador/dispositivo)

```bash
cd android
./gradlew connectedAndroidTest
```

Requer emulador rodando ou dispositivo conectado. Os testes de login usam `BackendLoginScreen` e a API de produção (ou local, conforme build type).

---

## Estrutura dos testes

| Camada | Onde | O que testa |
|--------|------|-------------|
| API | qa-psipro/tests/api/ | Endpoints do backend |
| Web | qa-psipro/tests/web/ | Dashboard no browser |
| Android | android/app/src/androidTest/ | App no dispositivo/emulador |
