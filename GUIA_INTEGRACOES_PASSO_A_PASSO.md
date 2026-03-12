# Guia completo: Como configurar as integrações do PsiPro

**Para quem não é técnico** — siga cada passo com calma. Se travar em algum lugar, anote o que apareceu na tela e peça ajuda.

---

# 1. Google Calendar

Permite que suas consultas do PsiPro apareçam automaticamente no seu Google Calendar.

---

## Passo 1.1 — Criar um projeto no Google Cloud

1. Abra o navegador e acesse: **https://console.cloud.google.com/**
2. Faça login com a conta Google onde você usa o Gmail e o Google Calendar.
3. No topo da página, clique no **nome do projeto** (ou em "Selecionar projeto").
4. Clique em **"Novo projeto"**.
5. Em **"Nome do projeto"**, digite: `PsiPro` (ou outro nome).
6. Clique em **"Criar"**.
7. Espere o projeto ser criado. Depois, clique para abri-lo.

---

## Passo 1.2 — Habilitar a API do Google Calendar

1. No menu lateral, vá em **"APIs e serviços"** → **"Biblioteca"**.
2. Na caixa de busca, digite: **Google Calendar API**.
3. Clique no resultado **"Google Calendar API"**.
4. Clique no botão **"Ativar"**.
5. Aguarde alguns segundos até a mensagem de sucesso.

---

## Passo 1.3 — Configurar a tela de consentimento OAuth

1. No menu lateral, vá em **"APIs e serviços"** → **"Tela de consentimento OAuth"**.
2. Se perguntar o tipo de usuário, escolha **"Externo"** e clique em **"Criar"**.
3. Preencha:
   - **Nome do aplicativo:** `PsiPro`
   - **E-mail de suporte do usuário:** seu e-mail
   - **Logo do aplicativo:** pode deixar em branco por enquanto
4. Clique em **"Salvar e continuar"**.
5. Em **"Escopos"** → clique em **"Adicionar ou remover escopos"**.
6. Na busca, digite **"Calendar"**.
7. Marque:
   - `https://www.googleapis.com/auth/calendar`
   - `https://www.googleapis.com/auth/calendar.events`
8. Clique em **"Atualizar"** e depois em **"Salvar e continuar"**.
9. Em **"Usuários de teste"**, clique em **"Adicionar usuários"** e adicione seu e-mail (obrigatório em modo teste).
10. Clique em **"Salvar e continuar"**.

---

## Passo 1.4 — Criar as credenciais (Client ID e Secret)

1. No menu lateral, vá em **"APIs e serviços"** → **"Credenciais"**.
2. Clique em **"+ Criar credenciais"** → **"ID do cliente OAuth"**.
3. Em **"Tipo de aplicativo"**, escolha **"Aplicativo da Web"**.
4. Em **"Nome"**, digite: `PsiPro Web`.
5. Em **"URIs de redirecionamento autorizados"**, clique em **"+ Adicionar URI"** e adicione:

   **Para desenvolvimento local (testar no seu PC):**
   ```
   http://localhost:8080/api/integrations/google-calendar/callback
   ```

   **Para produção (quando estiver na internet):**
   ```
   https://SEU-BACKEND.COM/api/integrations/google-calendar/callback
   ```
   *(Substitua `SEU-BACKEND.COM` pela URL real do seu backend. Ex.: `api.psipro.com.br`.)*

6. Clique em **"Criar"**.
7. Na janela que abrir, você verá:
   - **ID do cliente:** algo como `123456789-abcdef.apps.googleusercontent.com`
   - **Segredo do cliente:** uma senha longa  
   **Guarde os dois.** Clique em **"Copiar"** para cada um e salve em um arquivo de texto seguro.

---

## Passo 1.5 — Configurar o backend do PsiPro

1. Abra a pasta do seu projeto: `Psipro/backend`.
2. Procure o arquivo **`.env`** (se não existir, crie um copiando o `.env.example`).
3. Adicione ou edite estas linhas (use os valores que você copiou antes):

```
GOOGLE_CLIENT_ID=COLE_AQUI_O_ID_DO_CLIENTE
GOOGLE_CLIENT_SECRET=COLE_AQUI_O_SEGREDO_DO_CLIENTE
GOOGLE_CALENDAR_REDIRECT_URI=http://localhost:8080/api/integrations/google-calendar/callback
DASHBOARD_URL=http://localhost:5173
```

**Em produção**, use as URLs reais:
```
GOOGLE_CALENDAR_REDIRECT_URI=https://api.psipro.com.br/api/integrations/google-calendar/callback
DASHBOARD_URL=https://seu-dashboard.com.br
```

4. Salve o arquivo `.env`.

---

## Passo 1.6 — Reiniciar o backend e testar

1. Pare o backend (Ctrl+C no terminal onde está rodando).
2. Inicie de novo: `npm run start:dev` (ou o comando que você usa).
3. Abra o **dashboard** do PsiPro no navegador.
4. Vá em **Configurações** → aba **Integrações**.
5. Ao lado de **Google Calendar**, clique em **Conectar**.
6. Você será redirecionado para o Google. Escolha a conta e autorize o acesso.
7. Deve voltar ao dashboard em **Configurações → Integrações** (`/settings/integrations`) com a mensagem de **Conectado**.

Agora, quando você criar um novo agendamento no PsiPro, ele aparecerá no seu Google Calendar.

---

# 2. WhatsApp Business (Em breve)

Para enviar lembretes e mensagens automáticas para os pacientes.

---

## O que você precisa antes

1. **Conta Meta Business** (antigo Facebook Business)
2. **Número de telefone** dedicado ao WhatsApp Business (pode ser um número novo)
3. **Aprovação da Meta** para usar a API (pode levar alguns dias)
4. **Templates de mensagem** aprovados pela Meta (ex.: "Lembrete: sua consulta amanhã às 14h")

---

## Passo 2.1 — Acessar o Meta for Developers

1. Acesse: **https://developers.facebook.com/**
2. Faça login com sua conta Facebook.
3. Clique em **"Meus aplicativos"** → **"Criar aplicativo"**.
4. Escolha **"Outro"** → **"Consumir"** → **"Próximo"**.
5. Preencha o nome (ex.: `PsiPro`) e clique em **"Criar aplicativo"**.

---

## Passo 2.2 — Adicionar o produto WhatsApp

1. No painel do seu app, procure **"WhatsApp"**.
2. Clique em **"Configurar"** ao lado de **WhatsApp**.
3. Escolha **"API do WhatsApp em nuvem"**.
4. Você precisará de:
   - **Número de teste** (temporário, para desenvolvimento) **ou**
   - **Número de produção** (solicitado à Meta, para uso real)
5. No modo teste, a Meta fornece um número e um **Token de acesso temporário**.
6. Anote o **ID do número de telefone** e o **Token de acesso**.

---

## Passo 2.3 — Criar template de mensagem (produção)

1. Em **WhatsApp** → **Configuração da API**.
2. Vá em **"Gerenciar templates de mensagem"**.
3. Clique em **"Criar template"**.
4. Exemplo de lembrete:
   - **Nome:** `lembrete_consulta`
   - **Categoria:** Utilitário
   - **Idioma:** Português (Brasil)
   - **Corpo:** `Olá! Lembrete: sua consulta com {{1}} está agendada para {{2}} às {{3}}. Qualquer dúvida, entre em contato.`
5. Envie para aprovação. A Meta pode levar até 24–48 horas para aprovar.

---

## Passo 2.4 — Variáveis para o backend (quando implementado)

Quando a integração estiver pronta no código, você adicionará no `.env`:

```
WA_PHONE_NUMBER_ID=seu_id_do_numero
WA_ACCESS_TOKEN=seu_token_de_acesso
```

**Importante:** O token de teste expira. Para produção, use um token permanente (System User).

---

# 3. Gateway de Pagamento (Em breve)

Para receber pagamentos online dos pacientes (PIX, cartão).

---

## Opção A — Mercado Pago (comum no Brasil)

### Passo 3A.1 — Criar conta

1. Acesse: **https://www.mercadopago.com.br/developers**
2. Clique em **"Entrar"** e faça login (ou crie uma conta).
3. Vá em **"Suas integrações"** → **"Criar aplicação"**.
4. Nome: `PsiPro`
5. Escolha **"Pagamentos online"**.

### Passo 3A.2 — Obter credenciais

1. No painel do app, vá em **"Credenciais"**.
2. Você verá:
   - **Chave pública** (Public Key)
   - **Token de acesso** (Access Token)
   - Modo: **Produção** ou **Teste**
3. Para testes, use as credenciais de **Teste**.
4. Copie a **Chave pública** e o **Token de acesso** e guarde.

### Passo 3A.3 — Variáveis para o backend (quando implementado)

```
MP_ACCESS_TOKEN=seu_token_de_acesso
MP_PUBLIC_KEY=sua_chave_publica
```

---

## Opção B — Stripe (internacional)

### Passo 3B.1 — Criar conta

1. Acesse: **https://dashboard.stripe.com/register**
2. Crie uma conta.
3. Acesse o **Dashboard**.

### Passo 3B.2 — Obter chaves

1. No canto superior direito, clique em **"Desenvolvedores"** → **"Chaves da API"**.
2. Você verá:
   - **Chave pública** (publishable key) — começa com `pk_`
   - **Chave secreta** (secret key) — começa com `sk_`
3. Copie as duas e guarde em local seguro.

### Passo 3B.3 — Variáveis para o backend (quando implementado)

```
STRIPE_SECRET_KEY=sk_...
STRIPE_PUBLISHABLE_KEY=pk_...
```

---

# Resumo rápido

| Integração         | Status   | Onde configurar                            | Dificuldade |
|--------------------|---------|--------------------------------------------|------------|
| Google Calendar    | Pronta  | Google Cloud Console + `.env` do backend    | Média      |
| WhatsApp Business  | Em breve| Meta for Developers + templates aprovados   | Alta       |
| Gateway Pagamento  | Em breve| Mercado Pago ou Stripe + `.env`            | Média      |

---

# Dicas gerais

1. **Nunca compartilhe** suas chaves ou tokens com ninguém.
2. **Guarde as credenciais** em arquivos separados (ex.: `credenciais.txt`) e **nunca** faça commit no Git.
3. Para **teste**, use sempre ambiente de sandbox/teste antes de produção.
4. Se algo der errado, anote a mensagem de erro exata e procure na documentação oficial ou peça ajuda a alguém técnico.

---

# Links úteis

- Google Cloud Console: https://console.cloud.google.com/
- Meta for Developers: https://developers.facebook.com/
- Mercado Pago Developers: https://www.mercadopago.com.br/developers
- Stripe Dashboard: https://dashboard.stripe.com/
