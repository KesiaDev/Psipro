# ⚡ EXECUTE AGORA - Passo a Passo Rápido

## 🎯 ORDEM DE EXECUÇÃO

### PASSO 1: Limpar e Commitar (2 minutos)

Abra o PowerShell e execute:

```powershell
cd C:\Users\User\Desktop\psipro

# Remover pastas duplicadas (se existirem)
if (Test-Path "web\backend") { Remove-Item -Recurse -Force "web\backend" }
if (Test-Path "web\web") { Remove-Item -Recurse -Force "web\web" }

# Commitar tudo
git add .
git commit -m "Preparar projeto para deploy no Railway - Configurações e documentação"
git push origin main
```

✅ **Aguarde o push completar**

---

### PASSO 2: Criar Conta no Railway (3 minutos)

1. Acesse: **https://railway.app**
2. Clique em **"Login"** ou **"Start a New Project"**
3. Faça login com **GitHub**
4. Clique em **"+ New Project"**
5. Selecione **"Deploy from GitHub repo"**
6. Autorize o Railway
7. Escolha o repositório **`psipro`**

✅ **Projeto criado no Railway!**

---

### PASSO 3: Deploy do Backend (10 minutos)

#### 3.1 Adicionar PostgreSQL
1. No projeto Railway, clique em **"+ New"**
2. Selecione **"Database"** → **"Add PostgreSQL"**
3. ✅ PostgreSQL criado automaticamente

#### 3.2 Adicionar Serviço Backend
1. Clique em **"+ New"** novamente
2. Selecione **"GitHub Repo"**
3. Escolha **`psipro`**
4. Clique no serviço criado
5. Vá em **"Settings"** (⚙️)
6. Em **"Root Directory"**, digite: `backend`
7. Clique em **"Save"**

#### 3.3 Configurar Variáveis
1. Vá em **"Variables"** (🔧)
2. Railway já criou `DATABASE_URL` automaticamente
3. Adicione estas variáveis:

```
JWT_SECRET=gerar_chave_aleatoria_longa_aqui
PORT=3001
NODE_ENV=production
```

⚠️ **JWT_SECRET**: Use um gerador online ou: `openssl rand -base64 32`

#### 3.4 Configurar Build
1. Em **"Settings"** → **"Build Command"**: 
   ```
   npm run build && npm run prisma:generate
   ```
2. **"Start Command"**:
   ```
   npm run start:prod
   ```
3. Clique em **"Save"**

#### 3.5 Obter URL do Backend
1. **"Settings"** → **"Networking"**
2. Clique em **"Generate Domain"**
3. ✅ **ANOTE A URL** (ex: `psipro-backend-production.up.railway.app`)
4. URL completa da API: `https://SUA_URL/api`

✅ **Backend deployando! Aguarde 2-5 minutos**

---

### PASSO 4: Deploy do Web (10 minutos)

#### 4.1 Adicionar Serviço Web
1. No mesmo projeto, clique em **"+ New"**
2. **"GitHub Repo"** → **`psipro`**
3. Clique no serviço
4. **"Settings"** → **"Root Directory"**: `web`
5. **"Save"**

#### 4.2 Configurar Variáveis
1. **"Variables"** (🔧)
2. Adicione:

```
NEXT_PUBLIC_API_URL=https://SUA_URL_DO_BACKEND/api
PORT=3000
NODE_ENV=production
```

⚠️ **Substitua** `SUA_URL_DO_BACKEND` pela URL que você anotou no Passo 3.5

#### 4.3 Configurar Build
1. **"Settings"** → **"Build Command"**: `npm run build`
2. **"Start Command"**: `npm start`
3. **"Save"**

#### 4.4 Obter URL do Web
1. **"Settings"** → **"Networking"** → **"Generate Domain"**
2. ✅ **ANOTE A URL** (ex: `psipro-web-production.up.railway.app`)

✅ **Web deployando! Aguarde 3-7 minutos**

---

### PASSO 5: Ajustar CORS (2 minutos)

1. Volte ao serviço **Backend**
2. **"Variables"** → Adicione:

```
CORS_ORIGIN=https://SUA_URL_DO_WEB,http://localhost:3000
```

⚠️ **Substitua** `SUA_URL_DO_WEB` pela URL do Passo 4.4

3. O Railway vai redeployar automaticamente

---

### PASSO 6: Executar Migrations (3 minutos)

1. No serviço **Backend**, vá em **"Deployments"**
2. Clique no deployment mais recente
3. Vá na aba **"Logs"** ou use o terminal (se disponível)
4. Execute:

```bash
npx prisma migrate deploy
```

Ou via Railway CLI (se instalado):

```bash
railway run --service backend npx prisma migrate deploy
```

---

### PASSO 7: TESTAR (2 minutos)

1. ✅ Acesse: `https://SUA_URL_DO_WEB`
2. ✅ A página deve carregar
3. ✅ Abra o console do navegador (F12)
4. ✅ Verifique se não há erros de CORS

---

## ✅ CHECKLIST FINAL

- [ ] Código commitado e pushado
- [ ] Projeto Railway criado
- [ ] PostgreSQL adicionado
- [ ] Backend deployado e rodando
- [ ] Web deployado e rodando
- [ ] URLs anotadas
- [ ] CORS configurado
- [ ] Migrations executadas
- [ ] Teste de acesso funcionando

---

## 🐛 SE ALGO DER ERRADO

### Backend não inicia
- Verifique logs do Railway
- Verifique se `DATABASE_URL` está configurada
- Verifique se todas as variáveis estão corretas

### Web não conecta
- Verifique `NEXT_PUBLIC_API_URL`
- Verifique se Backend está rodando
- Verifique CORS no Backend

### Erro de CORS
- Verifique formato: `https://url1,https://url2` (sem espaços)
- Verifique se URL do Web está em `CORS_ORIGIN`

---

## 📚 DOCUMENTAÇÃO COMPLETA

Para mais detalhes, consulte: **`PASSO_A_PASSO_RAILWAY.md`**

---

**Boa sorte! 🚀**

