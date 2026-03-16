# Plano de Integrações — PsiPro

**Objetivo:** Implementar Google Calendar, WhatsApp Business e Gateway de Pagamento conforme tela "Integrações" nas Configurações.

---

## 1. Google Calendar

### Descrição
Sincronize sua agenda com o Google Calendar.

### Requisitos
- OAuth 2.0 (usuário autoriza acesso ao calendário dele)
- Token armazenado por usuário (ou por profissional/clínica)
- Sincronização bidirecional ou apenas PsiPro → Google

### Implementação
- **Backend:**
  - Modelo `UserIntegration` (provider: `google_calendar`, config: JSON com `refresh_token`)
  - `GET /integrations/google-calendar/auth-url` — retorna URL de OAuth
  - `GET /integrations/google-calendar/callback?code=...` — troca código por tokens, salva, redireciona
  - `POST /integrations/google-calendar/sync` — força sincronia dos appointments
  - Hook: ao criar/atualizar/remover `Appointment` → criar/atualizar/remover evento no Google Calendar
- **Pacote:** `googleapis`
- **Config:** `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `GOOGLE_CALENDAR_REDIRECT_URI`

### Google Cloud Console
1. Criar projeto → APIs & Services → Habilitar "Google Calendar API"
2. Credenciais → OAuth 2.0 Client ID (tipo "Web application")
3. Adicionar redirect URI: `https://seu-dominio/api/integrations/google-calendar/callback`

---

## 2. WhatsApp Business

### Descrição
Envie lembretes e mensagens automatizadas.

### Requisitos
- WhatsApp Business Cloud API (Meta)
- Número de telefone aprovado pela Meta
- Templates de mensagem aprovados (ex.: lembretes de consulta)
- Pacientes precisam ter `phone` cadastrado

### Implementação
- **Backend:**
  - Modelo: config WhatsApp por clínica (token, phone_number_id)
  - `POST /integrations/whatsapp/send-reminder` — envia lembrete para paciente (após aprovação do template)
  - Cron: consultas nas próximas 24h → enviar lembrete (se paciente tem phone e opt-in)
- **Pacote:** `whatsapp` (SDK oficial Meta) ou API REST direta
- **Config:** `WA_PHONE_NUMBER_ID`, `WA_ACCESS_TOKEN`, `WA_VERIFY_TOKEN` (webhook)

### Meta Business
1. Meta for Developers → WhatsApp → Cloud API
2. Criar app e adicionar produto WhatsApp
3. Número de teste ou solicitar número Business
4. Criar templates de mensagem (ex.: "Lembrete: sua consulta com Dr. X amanhã às 14h")

---

## 3. Gateway de Pagamento

### Descrição
Receba pagamentos online dos pacientes.

### Requisitos
- Gateway: Mercado Pago, Stripe ou PagSeguro (Brasil)
- Checkout: link ou modal de pagamento
- Webhook para confirmar pagamento
- Vincular pagamento à sessão/agendamento

### Implementação
- **Backend:**
  - Modelo: config gateway por clínica (chaves API, webhook secret)
  - `POST /payments/checkout` — cria intenção de pagamento, retorna link/modal
  - `POST /payments/webhook/:provider` — recebe notificação de pagamento confirmado
  - Atualizar `Payment` ou `FinancialRecord` ao confirmar
- **Opções:** Mercado Pago (mais comum no Brasil), Stripe (internacional)

### Config
- Mercado Pago: `MP_ACCESS_TOKEN`, `MP_WEBHOOK_SECRET`
- Stripe: `STRIPE_SECRET_KEY`, `STRIPE_WEBHOOK_SECRET`

---

## Variáveis de Ambiente — Google Calendar (implementado)

Adicione ao `.env` do backend:

```
GOOGLE_CLIENT_ID=seu-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=seu-client-secret
GOOGLE_CALENDAR_REDIRECT_URI=https://seu-backend.com/api/integrations/google-calendar/callback
DASHBOARD_URL=https://seu-dashboard.com
```

Para desenvolvimento local:
```
GOOGLE_CALENDAR_REDIRECT_URI=http://localhost:8080/api/integrations/google-calendar/callback
DASHBOARD_URL=http://localhost:5173
```

---

## 4. Modelo de Dados (Prisma)

```prisma
model UserIntegration {
  id          String   @id @default(uuid())
  userId      String
  clinicId    String?  // Algumas integrações são por clínica
  provider    String   // google_calendar | whatsapp | mercadopago | stripe
  config      Json     // Tokens, chaves (criptografar em produção)
  status      String   @default("connected") // connected | disconnected | error
  lastSyncAt  DateTime?
  createdAt   DateTime @default(now())
  updatedAt   DateTime @updatedAt

  user        User     @relation(fields: [userId], references: [id], onDelete: Cascade)
  clinic      Clinic?  @relation(fields: [clinicId], references: [id], onDelete: Cascade)

  @@unique([userId, clinicId, provider])
  @@index([userId])
  @@index([clinicId])
  @@map("user_integrations")
}
```

---

## 5. Ordem de Implementação Sugerida

| # | Integração           | Complexidade | Dependências externas          | Prioridade |
|---|----------------------|--------------|-------------------------------|------------|
| 1 | Google Calendar      | Média        | Google Cloud (grátis)         | Alta       |
| 2 | WhatsApp Business    | Alta         | Meta Business (aprovação)     | Média      |
| 3 | Gateway de Pagamento | Alta         | Mercado Pago / Stripe (conta) | Média      |

---

## 6. Frontend — Atualizações em Settings

- **Google Calendar:** Botão "Conectar" → abre URL OAuth → callback redireciona de volta → "Conectado" + "Desconectar"
- **WhatsApp:** Botão "Configurar" → modal com instruções e campos (quando backend tiver endpoint)
- **Gateway:** Botão "Configurar" → escolha de provedor + credenciais
