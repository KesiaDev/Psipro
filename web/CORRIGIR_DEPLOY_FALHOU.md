# ⚠️ CORRIGIR DEPLOY QUE FALHOU - Guia Rápido

## 🎯 AÇÃO IMEDIATA

### 1️⃣ VER OS LOGS DO ERRO

1. No Railway, clique no serviço **"Psipro"** que falhou
2. Vá em **"Deployments"**
3. Clique no deployment mais recente (com erro)
4. Abra a aba **"Logs"**
5. **Leia a última mensagem de erro**

---

## 🔍 PROBLEMAS MAIS COMUNS

### ❌ ERRO: "package.json not found"

**Causa**: Root Directory não configurado

**Solução**:
1. Vá em **"Settings"** do serviço
2. Role até **"Source"**
3. Em **"Root Directory"**, digite: `backend` (se for backend) ou `web` (se for web)
4. Clique em **"Save"**
5. Vá em **"Deployments"** → **"Redeploy"**

---

### ❌ ERRO: "DATABASE_URL" ou "Cannot connect to database"

**Causa**: PostgreSQL não conectado ou variável faltando

**Solução**:
1. Verifique se criou o PostgreSQL no projeto
2. No serviço Backend, vá em **"Variables"**
3. Verifique se existe `DATABASE_URL`
4. Se não existir, clique em **"Connect"** e selecione o PostgreSQL
5. Faça **"Redeploy"**

---

### ❌ ERRO: "Prisma Client" ou "schema.prisma not found"

**Causa**: Prisma não está sendo gerado

**Solução**:
1. Vá em **"Settings"** → **"Build Command"**
2. Certifique-se que está:
   ```
   npm install && npm run build && npm run prisma:generate
   ```
3. Ou:
   ```
   npm install && npm run build && npx prisma generate
   ```
4. **"Save"** e **"Redeploy"**

---

### ❌ ERRO: "Module not found" ou dependências

**Causa**: npm install não está rodando

**Solução**:
1. Vá em **"Settings"** → **"Build Command"**
2. Adicione `npm install &&` no início:
   ```
   npm install && npm run build && npm run prisma:generate
   ```
3. **"Save"** e **"Redeploy"**

---

### ❌ ERRO: "Port already in use" ou não inicia

**Causa**: Porta não configurada ou conflito

**Solução**:
1. Vá em **"Variables"**
2. Adicione:
   - Backend: `PORT=3001`
   - Web: `PORT=3000`
3. **"Save"** e **"Redeploy"**

---

## ✅ CHECKLIST RÁPIDO DE CORREÇÃO

Verifique se está tudo configurado:

### Backend:
- [ ] **Root Directory**: `backend`
- [ ] **Build Command**: `npm install && npm run build && npm run prisma:generate`
- [ ] **Start Command**: `npm run start:prod`
- [ ] **Variáveis**:
  - [ ] `DATABASE_URL` (conectada ao PostgreSQL)
  - [ ] `JWT_SECRET` (chave aleatória)
  - [ ] `PORT=3001`
  - [ ] `NODE_ENV=production`

### Web:
- [ ] **Root Directory**: `web`
- [ ] **Build Command**: `npm install && npm run build`
- [ ] **Start Command**: `npm start`
- [ ] **Variáveis**:
  - [ ] `NEXT_PUBLIC_API_URL` (URL do backend)
  - [ ] `PORT=3000`
  - [ ] `NODE_ENV=production`

---

## 🚀 PASSOS PARA CORRIGIR AGORA

### Passo 1: Ver Logs
```
1. Clique no serviço "Psipro"
2. "Deployments" → deployment mais recente
3. Abra "Logs"
4. Copie a última mensagem de erro
```

### Passo 2: Identificar Erro
- Compare com os erros acima
- Identifique qual é o problema

### Passo 3: Corrigir
- Siga a solução do erro identificado
- Salve as configurações

### Passo 4: Redeploy
```
1. "Deployments"
2. "Redeploy" ou "Deploy"
3. Aguarde build completar
4. Verifique logs novamente
```

---

## 🔄 SE NADA FUNCIONAR - RECRIAR SERVIÇO

1. **Delete o serviço atual**:
   - Settings → Delete Service

2. **Crie novo serviço**:
   - + New → GitHub Repo
   - Escolha `psipro`
   - Configure Root Directory: `backend` ou `web`
   - Configure Build Command
   - Configure Start Command
   - Adicione variáveis
   - Deploy

---

## 💡 DICA IMPORTANTE

**Sempre configure nesta ordem**:
1. Root Directory
2. Variáveis de ambiente
3. Build Command
4. Start Command
5. Deploy

---

**Qual erro apareceu nos logs? Me mostre e eu ajudo a corrigir! 🔧**

