# Especificação para Integração PsiPro Web ↔ PsiPro App

Este documento descreve as APIs, fluxos de autenticação e pontos de integração entre o **PsiPro Web** e o **PsiPro App** (Android), para apoiar a implementação da funcionalidade "Integrações → PsiPro App" atualmente marcada como "Em breve".

---

## 1. Visão geral da arquitetura

- **Backend único**: App e Web utilizam o **mesmo backend** (NestJS).
- **Base URL da API (produção)**: `https://psipro-backend-production.up.railway.app/api`
- **Base URL do Web (produção)**: `https://psipro-dashboard-production.up.railway.app`
- O App já abre o Web com **token na URL** para SSO (handoff) e a página de login do Web trata esse fluxo.

---

## 2. Fluxo de handoff App → Web (já implementado)

### 2.1 Como o App abre o Web

O App redireciona para:

```
{PSIPRO_WEB_BASE_URL}/login?token={JWT}&returnUrl={path}
```

Exemplos:
- Dashboard: `/login?token=eyJhbG...&returnUrl=/dashboard`
- Paciente: `/login?token=eyJhbG...&returnUrl=/patients/123`
- Financeiro: `/login?token=eyJhbG...&returnUrl=/financial`

### 2.2 O que o Web deve fazer ao receber `token` na URL

1. **POST** `/api/auth/handoff` com body: `{ "token": "<JWT>" }`
2. Backend valida o JWT e retorna: `{ token, user }`
3. Web salva:
   - `psipro_token` no localStorage
   - `psipro_current_clinic_id` (se presente em `user.clinicId`)
   - `psipro_user` (id, email, fullName)
4. Redireciona para `returnUrl` ou `/dashboard`

### 2.3 Endpoint de handoff

| Método | URL              | Body                 | Resposta (200) |
|--------|------------------|----------------------|----------------|
| POST   | `/api/auth/handoff` | `{ "token": "JWT" }` | `{ token, user: { id, email, name, clinicId } }` |

O header `Authorization: Bearer <token>` também é aceito.

---

## 3. APIs de autenticação (App e Web)

| Método | Endpoint           | Uso                          |
|--------|--------------------|------------------------------|
| POST   | `/api/auth/login`  | Login email/senha            |
| POST   | `/api/auth/register` | Registro novo usuário      |
| POST   | `/api/auth/refresh` | Renovar access token        |
| POST   | `/api/auth/logout` | Logout (invalida refresh)    |
| GET    | `/api/auth/me`     | Perfil do usuário autenticado |
| POST   | `/api/auth/handoff` | SSO App → Web (valida JWT)  |
| POST   | `/api/auth/switch-clinic` | Trocar clínica ativa    |

---

## 4. APIs de sincronização (dados compartilhados)

Todas requerem **JWT** no header `Authorization: Bearer <token>` e **clinicId** como query param.

| Método | Endpoint                | Query params      | Uso                            |
|--------|-------------------------|-------------------|--------------------------------|
| GET    | `/api/sync/patients`    | `clinicId`, `updatedAfter?` | Listar pacientes           |
| POST   | `/api/sync/patients`    | `clinicId`        | Enviar pacientes do App       |
| GET    | `/api/sync/appointments`| `clinicId`, `updatedAfter?` | Listar agendamentos      |
| POST   | `/api/sync/appointments`| `clinicId`        | Enviar agendamentos do App     |
| GET    | `/api/sync/sessions`    | `clinicId`, `updatedAfter?` | Listar sessões            |
| POST   | `/api/sync/sessions`    | `clinicId`        | Enviar sessões do App          |
| GET    | `/api/sync/payments`    | `clinicId`, `updatedAfter?` | Listar pagamentos        |
| POST   | `/api/sync/payments`    | `clinicId`        | Enviar pagamentos do App       |

---

## 5. Modelos de dados (contratos)

### Login response
```json
{
  "accessToken": "eyJhbG...",
  "refreshToken": "uuid...",
  "user": {
    "id": "user-uuid",
    "email": "user@example.com",
    "name": "Nome Completo",
    "clinicId": "clinic-uuid"
  }
}
```

### /auth/me response
```json
{
  "id": "user-uuid",
  "email": "user@example.com",
  "role": "ADMIN",
  "clinicId": "clinic-uuid",
  "name": "Nome Completo"
}
```

### Patient (sync)
```json
{
  "id": "patient-uuid",
  "clinicId": "clinic-uuid",
  "name": "Nome",
  "birthDate": "1990-01-15",
  "cpf": "12345678900",
  "phone": "+5511999999999",
  "email": "paciente@email.com",
  "address": "...",
  "emergencyContact": "...",
  "observations": "...",
  "status": "ativo",
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

---

## 6. Fluxos sugeridos para "Integrações → PsiPro App"

### Opção A: QR code ou código de pareamento (recomendado)

1. **Web** gera um código curto (6–8 caracteres) ou QR code.
2. Código é vinculado à sessão do usuário logado no Web (temporary token).
3. **App** abre tela "Conectar ao Web", usuário digita código ou escaneia QR.
4. App envia código + JWT do App para o backend: `POST /api/auth/link-device` (novo endpoint).
5. Backend valida:
   - Código existe e não expirou
   - Usuário do código (Web) = usuário do JWT (App) ou mesma clínica
6. Backend associa o dispositivo e retorna sucesso.
7. Web e App passam a compartilhar a mesma conta (já usam o mesmo backend).

**Exemplo de endpoint novo no backend:**
```ts
POST /api/auth/link-device
Body: { code: "ABC123", deviceId?: "uuid", deviceName?: "Pixel 6" }
Headers: Authorization: Bearer <JWT do App>
```

### Opção B: Link “Abrir no app” (simplificado)

1. Web mostra botão "Abrir no app".
2. Ao clicar, Web abre **deeplink** ou **URL custom**: `psipro://open?token=...&returnPath=/dashboard`
3. App (se instalado) captura o deeplink e faz login com o token ou valida sessão.
4. Requer configuração de **App Links** (Android) / **Universal Links** (iOS).

### Opção C: Sincronização transparente (atual)

- App e Web já usam o mesmo backend e mesmos endpoints.
- Usuário faz login no App e no Web com as **mesmas credenciais**.
- Ao abrir "Plataforma Web" no App, o handoff via URL (`/login?token=...&returnUrl=...`) já realiza o SSO.
- Para a seção "Integrações", pode-se documentar esse fluxo e adicionar um botão "Abrir app" no Web que tenta abrir o deeplink `psipro://` (se o app estiver instalado).

---

## 7. O que o App já envia/espera

| Recurso        | App envia                         | Web precisa tratar               |
|----------------|-----------------------------------|----------------------------------|
| Token          | JWT na URL (?token=...)           | Já tratado em `/login`           |
| returnUrl      | Path para redirecionar            | Já usado após handoff             |
| clinicId       | Via user em /auth/me e handoff     | Já salvo em localStorage          |

---

## 8. Resumo para o time PsiPro Web

- O handoff **App → Web** já está implementado: o App abre `/login?token=...&returnUrl=...` e o Web trata em `(auth)/login/page.tsx`.
- App e Web compartilham o **mesmo backend** e **mesmas APIs** de auth e sync.
- Para a aba "Integrações → PsiPro App", as opções práticas são:
  1. **Documentar o fluxo atual** (abrir Web a partir do App) e adicionar um botão "Abrir no app" (deeplink).
  2. **Implementar pareamento por QR/código** para vincular explicitamente o app ao usuário logado no Web.
- Para definir o fluxo concreto (QR, deeplink, etc.), é necessário alinhar requisitos de produto e UX.

---

## 9. Referências no código

| Componente         | Caminho                                                  |
|--------------------|----------------------------------------------------------|
| App – WebNavigator | `app/src/main/java/com/psipro/app/utils/WebNavigator.kt` |
| App – BackendApiService | `app/src/main/java/com/psipro/app/sync/api/BackendApiService.kt` |
| App – BackendAuthManager | `app/src/main/java/com/psipro/app/sync/BackendAuthManager.kt` |
| Web – Login handoff | `web/app/(auth)/login/page.tsx`                         |
| Backend – Auth controller | `backend/src/auth/auth.controller.ts`                 |
| Backend – Handoff | `backend/src/auth/auth.service.ts` (método `handoff`)   |
