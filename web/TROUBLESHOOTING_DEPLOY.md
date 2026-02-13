# 🔧 Troubleshooting - Deploy Falhou no Railway

## ⚠️ PROBLEMA IDENTIFICADO

O serviço "Psipro" está com **"Deployment failed"** e marcado como **"Removed"**.

---

## 🔍 CAUSAS COMUNS E SOLUÇÕES

### 1️⃣ Root Directory Não Configurado

**Sintoma**: Build falha com "package.json not found"

**Solução**:
1. No serviço Railway, vá em **"Settings"**
2. Role até **"Source"**
3. Em **"Root Directory"**, configure:
   - Para Backend: `backend`
   - Para Web: `web`
4. Clique em **"Save"**
5. Faça **"Redeploy"**

---

### 2️⃣ Variáveis de Ambiente Faltando

**Sintoma**: Erro ao iniciar (DATABASE_URL, JWT_SECRET, etc.)

**Solução - Backend**:
1. Vá em **"Variables"** do serviço Backend
2. Verifique se tem:
   ```
   DATABASE_URL=postgresql://... (Railway cria automaticamente)
   JWT_SECRET=chave_aleatoria_longa
   PORT=3001
   NODE_ENV=production
   ```
3. Se faltar, adicione e faça **"Redeploy"**

**Solução - Web**:
1. Vá em **"Variables"** do serviço Web
2. Verifique se tem:
   ```
   NEXT_PUBLIC_API_URL=https://sua-url-backend.railway.app/api
   PORT=3000
   NODE_ENV=production
   ```
3. Se faltar, adicione e faça **"Redeploy"**

---

### 3️⃣ Prisma Generate Falhando

**Sintoma**: Erro "Prisma Client not generated" ou "schema.prisma not found"

**Solução**:
1. Verifique se o **Root Directory** está como `backend`
2. Verifique se o arquivo `backend/prisma/schema.prisma` existe
3. No **Build Command**, certifique-se que está:
   ```
   npm run build && npm run prisma:generate
   ```
4. Ou tente:
   ```
   npm install && npm run build && npx prisma generate
   ```

---

### 4️⃣ Build Command Incorreto

**Sintoma**: Build falha imediatamente

**Solução - Backend**:
```
npm install && npm run build && npm run prisma:generate
```

**Solução - Web**:
```
npm install && npm run build
```

---

### 5️⃣ Start Command Incorreto

**Sintoma**: Deploy completa mas serviço não inicia

**Solução - Backend**:
```
npm run start:prod
```

**Solução - Web**:
```
npm start
```

---

### 6️⃣ PostgreSQL Não Conectado

**Sintoma**: Erro de conexão com banco de dados

**Solução**:
1. Verifique se o PostgreSQL foi criado no projeto
2. No serviço Backend, vá em **"Variables"**
3. Verifique se `DATABASE_URL` está conectada ao PostgreSQL
4. Se não estiver, clique em **"Connect"** e selecione o PostgreSQL

---

### 7️⃣ Porta Não Configurada

**Sintoma**: Serviço inicia mas não responde

**Solução**:
- Backend: Adicione `PORT=3001` nas variáveis
- Web: Adicione `PORT=3000` nas variáveis
- Railway usa a variável `PORT` automaticamente

---

## 🚀 SOLUÇÃO RÁPIDA (PASSO A PASSO)

### Passo 1: Verificar Logs

1. No serviço que falhou, clique em **"Deployments"**
2. Clique no deployment mais recente
3. Vá na aba **"Logs"**
4. Leia a mensagem de erro completa
5. Anote o erro

### Passo 2: Corrigir Baseado no Erro

**Se erro for "package.json not found"**:
- ✅ Configure **Root Directory** como `backend` ou `web`

**Se erro for "DATABASE_URL"**:
- ✅ Adicione variável `DATABASE_URL` conectada ao PostgreSQL

**Se erro for "Prisma"**:
- ✅ Verifique se `prisma:generate` está no Build Command
- ✅ Verifique se `backend/prisma/schema.prisma` existe

**Se erro for "module not found"**:
- ✅ Adicione `npm install` no início do Build Command

### Passo 3: Redeploy

1. Após corrigir, vá em **"Deployments"**
2. Clique em **"Redeploy"** ou **"Deploy"**
3. Aguarde o build completar
4. Verifique os logs novamente

---

## 📋 CHECKLIST DE VERIFICAÇÃO

Antes de fazer deploy, verifique:

- [ ] **Root Directory** configurado corretamente
- [ ] **Build Command** correto
- [ ] **Start Command** correto
- [ ] **Variáveis de ambiente** configuradas
- [ ] **PostgreSQL** criado e conectado (Backend)
- [ ] **NEXT_PUBLIC_API_URL** apontando para Backend (Web)
- [ ] Arquivo `railway.json` existe (opcional, mas ajuda)

---

## 🔄 RECRIAR SERVIÇO (ÚLTIMA OPÇÃO)

Se nada funcionar, recrie o serviço:

1. **Remover serviço atual**:
   - Clique no serviço
   - Settings → Delete Service

2. **Criar novo serviço**:
   - + New → GitHub Repo
   - Escolha `psipro`
   - Configure Root Directory
   - Configure variáveis
   - Deploy

---

## 💡 DICAS IMPORTANTES

1. **Sempre configure Root Directory primeiro**
2. **Sempre adicione variáveis antes do primeiro deploy**
3. **Leia os logs completos** - eles mostram exatamente o problema
4. **Backend deve ser deployado antes do Web**
5. **PostgreSQL deve ser criado antes do Backend**

---

## 📞 PRÓXIMOS PASSOS

1. Abra os **logs do deploy** que falhou
2. Identifique o erro específico
3. Use este guia para corrigir
4. Faça **Redeploy**

---

**Boa sorte! 🚀**

