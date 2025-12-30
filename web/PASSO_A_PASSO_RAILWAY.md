# 🚀 PASSO A PASSO COMPLETO - Deploy PsiPro no Railway

## 📋 ANTES DE COMEÇAR

Você precisa ter:
- ✅ Conta no GitHub (repositório `psipro` criado)
- ✅ Conta no Railway (criar em [railway.app](https://railway.app))
- ✅ Código local commitado e pushado

---

## ETAPA 1: PREPARAR O CÓDIGO LOCAL

### 1.1 Verificar se tudo está commitado

```powershell
cd C:\Users\User\Desktop\psipro
git status
```

Se houver arquivos não commitados, faça:

```powershell
git add .
git commit -m "Preparar projeto para deploy no Railway"
git push origin main
```

---

## ETAPA 2: CRIAR CONTA E PROJETO NO RAILWAY

### 2.1 Acessar Railway

1. Acesse: https://railway.app
2. Clique em **"Login"** ou **"Start a New Project"**
3. Faça login com GitHub (recomendado)

### 2.2 Criar Novo Projeto

1. No dashboard, clique em **"+ New Project"**
2. Selecione **"Deploy from GitHub repo"**
3. Autorize o Railway a acessar seus repositórios GitHub
4. Selecione o repositório **`psipro`**

✅ Agora você tem um projeto vazio no Railway conectado ao GitHub.

---

## ETAPA 3: DEPLOY DO BACKEND (PRIMEIRO)

### 3.1 Adicionar Serviço Backend

1. No projeto Railway, clique em **"+ New"**
2. Selecione **"GitHub Repo"**
3. Escolha o repositório **`psipro`** novamente
4. Railway vai detectar automaticamente

### 3.2 Configurar Root Directory

1. Clique no serviço que acabou de criar
2. Vá em **"Settings"** (⚙️)
3. Role até **"Source"**
4. Em **"Root Directory"**, digite: `backend`
5. Clique em **"Save"**

### 3.3 Adicionar Banco de Dados PostgreSQL

1. No mesmo projeto Railway, clique em **"+ New"**
2. Selecione **"Database"** → **"Add PostgreSQL"**
3. Railway vai criar automaticamente um PostgreSQL
4. Anote o nome do serviço (ex: "Postgres")

### 3.4 Conectar Database ao Backend

1. Clique no serviço **Backend**
2. Vá em **"Variables"** (🔧)
3. Railway já deve ter criado `DATABASE_URL` automaticamente
4. Se não tiver, clique em **"+ New Variable"**:
   - Nome: `DATABASE_URL`
   - Valor: Clique em **"Connect"** e selecione o PostgreSQL

### 3.5 Adicionar Outras Variáveis do Backend

No mesmo painel **"Variables"**, adicione:

```
JWT_SECRET=coloque_uma_chave_secreta_aleatoria_aqui_muito_longa
PORT=3001
NODE_ENV=production
```

⚠️ **JWT_SECRET**: Gere uma chave aleatória longa (ex: use um gerador online)

### 3.6 Configurar Build e Start

1. Ainda em **"Settings"** do Backend
2. Role até **"Build Command"**
3. Digite: `npm run build && npm run prisma:generate`
4. Em **"Start Command"**, digite: `npm run start:prod`
5. Clique em **"Save"**

### 3.7 Fazer Deploy

1. Vá em **"Deployments"** (📦)
2. Clique em **"Deploy"** ou aguarde o deploy automático
3. Aguarde o build completar (pode levar 2-5 minutos)
4. Verifique os logs para ver se deu certo

✅ **Backend deve estar rodando!**

### 3.8 Obter URL do Backend

1. No serviço Backend, vá em **"Settings"**
2. Role até **"Networking"**
3. Clique em **"Generate Domain"**
4. Anote a URL gerada (ex: `psipro-backend-production.up.railway.app`)
5. A URL completa da API será: `https://psipro-backend-production.up.railway.app/api`

---

## ETAPA 4: DEPLOY DO WEB (SEGUNDO)

### 4.1 Adicionar Serviço Web

1. No mesmo projeto Railway, clique em **"+ New"**
2. Selecione **"GitHub Repo"**
3. Escolha o repositório **`psipro`**

### 4.2 Configurar Root Directory

1. Clique no novo serviço
2. Vá em **"Settings"** (⚙️)
3. Em **"Root Directory"**, digite: `web`
4. Clique em **"Save"**

### 4.3 Adicionar Variáveis do Web

1. Vá em **"Variables"** (🔧)
2. Adicione:

```
NEXT_PUBLIC_API_URL=https://SUA_URL_DO_BACKEND/api
PORT=3000
NODE_ENV=production
```

⚠️ **NEXT_PUBLIC_API_URL**: Substitua `SUA_URL_DO_BACKEND` pela URL que você anotou na etapa 3.8

### 4.4 Configurar Build e Start

1. Em **"Settings"** do Web
2. **"Build Command"**: `npm run build`
3. **"Start Command"**: `npm start`
4. Clique em **"Save"**

### 4.5 Fazer Deploy

1. Vá em **"Deployments"**
2. Aguarde o deploy automático ou clique em **"Deploy"**
3. Aguarde o build (pode levar 3-7 minutos)

✅ **Web deve estar rodando!**

### 4.6 Obter URL do Web

1. No serviço Web, vá em **"Settings"**
2. **"Networking"** → **"Generate Domain"**
3. Anote a URL (ex: `psipro-web-production.up.railway.app`)

---

## ETAPA 5: AJUSTAR CORS NO BACKEND

### 5.1 Atualizar CORS_ORIGIN

1. Volte ao serviço **Backend**
2. Vá em **"Variables"**
3. Adicione ou edite:

```
CORS_ORIGIN=https://SUA_URL_DO_WEB,http://localhost:3000
```

⚠️ Substitua `SUA_URL_DO_WEB` pela URL que você anotou na etapa 4.6

### 5.2 Redeploy do Backend

1. Após adicionar `CORS_ORIGIN`, o Railway vai redeployar automaticamente
2. Ou vá em **"Deployments"** → **"Redeploy"**

---

## ETAPA 6: EXECUTAR MIGRATIONS DO PRISMA

### 6.1 Abrir Terminal do Backend

1. No serviço Backend, vá em **"Deployments"**
2. Clique no deployment mais recente
3. Vá na aba **"Logs"**
4. Ou use o terminal do Railway (se disponível)

### 6.2 Executar Migration

No terminal do Railway (ou via SSH se disponível):

```bash
cd backend
npm run prisma:migrate deploy
```

Ou se Railway tiver terminal integrado:
```bash
npx prisma migrate deploy
```

---

## ETAPA 7: TESTAR

### 7.1 Testar Backend

1. Acesse: `https://SUA_URL_DO_BACKEND/api`
2. Deve retornar algo (mesmo que erro 404, significa que está rodando)

### 7.2 Testar Web

1. Acesse: `https://SUA_URL_DO_WEB`
2. A página deve carregar
3. Tente fazer login (se tiver)

### 7.3 Verificar Conexão Web → Backend

1. Abra o console do navegador (F12)
2. Veja se há erros de CORS ou conexão
3. Se houver erro de CORS, verifique se `CORS_ORIGIN` está correto

---

## ✅ CHECKLIST FINAL

- [ ] Backend deployado e rodando
- [ ] PostgreSQL conectado
- [ ] Variáveis de ambiente do Backend configuradas
- [ ] Web deployado e rodando
- [ ] `NEXT_PUBLIC_API_URL` apontando para o Backend
- [ ] `CORS_ORIGIN` configurado no Backend
- [ ] Migrations do Prisma executadas
- [ ] URLs anotadas
- [ ] Teste de acesso funcionando

---

## 🐛 PROBLEMAS COMUNS

### Backend não inicia
- ✅ Verifique se `DATABASE_URL` está configurada
- ✅ Verifique logs do Railway
- ✅ Verifique se `prisma generate` rodou

### Web não conecta ao Backend
- ✅ Verifique `NEXT_PUBLIC_API_URL`
- ✅ Verifique `CORS_ORIGIN` no Backend
- ✅ Verifique se Backend está rodando

### Erro de CORS
- ✅ Adicione a URL do Web em `CORS_ORIGIN`
- ✅ Formato: `https://url1,https://url2` (sem espaços)

### Build falha
- ✅ Verifique se `Root Directory` está correto
- ✅ Verifique logs do build
- ✅ Verifique se todas as dependências estão no `package.json`

---

## 📞 PRÓXIMOS PASSOS

Depois que tudo estiver funcionando:

1. **Configurar domínios customizados** (opcional)
2. **Configurar SSL** (Railway faz automaticamente)
3. **Monitorar logs** e performance
4. **Configurar alertas** (opcional)

---

**Boa sorte com o deploy! 🚀**

