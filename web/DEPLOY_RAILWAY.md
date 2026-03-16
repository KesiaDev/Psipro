# 🚀 Guia de Deploy no Railway - PsiPro

## 📁 Estrutura do Repositório (Monorepo)

```
psipro/
├── backend/     → NestJS API (porta 3001)
├── web/         → Next.js Frontend (porta 3000)
└── android/     → App Android (não deploya no Railway)
```

## 🎯 Estratégia de Deploy

O Railway precisa de **2 serviços separados**:
1. **Backend Service** → pasta `backend/`
2. **Web Service** → pasta `web/`

---

## 📋 PASSO A PASSO

### 1️⃣ Criar Projeto no Railway

1. Acesse [railway.app](https://railway.app)
2. Crie um **New Project**
3. Selecione **Deploy from GitHub repo**
4. Conecte o repositório `psipro`

### 2️⃣ Deploy do Backend (Primeiro)

1. No projeto Railway, clique em **+ New Service**
2. Selecione **GitHub Repo** → escolha `psipro`
3. Nas configurações:
   - **Root Directory**: `backend`
   - **Build Command**: `npm run build && npm run prisma:generate`
   - **Start Command**: `npm run start:prod`
4. Adicione variáveis de ambiente:
   ```
   DATABASE_URL=postgresql://... (Railway cria automaticamente)
   JWT_SECRET=sua_chave_secreta_aqui
   PORT=3001
   NODE_ENV=production
   CORS_ORIGIN=https://seu-web.railway.app,http://localhost:3000
   ```
   ⚠️ **CORS_ORIGIN**: Adicione o domínio do web depois que criar o serviço web
5. Adicione **PostgreSQL Database** (Railway cria automaticamente)
6. Conecte o DATABASE_URL ao serviço

### 3️⃣ Deploy do Web (Segundo)

1. No mesmo projeto Railway, clique em **+ New Service**
2. Selecione **GitHub Repo** → escolha `psipro`
3. Nas configurações:
   - **Root Directory**: `web`
   - **Build Command**: `npm run build`
   - **Start Command**: `npm start`
4. Adicione variáveis de ambiente:
   ```
   NEXT_PUBLIC_API_URL=https://seu-backend.railway.app/api
   PORT=3000
   NODE_ENV=production
   ```

### 4️⃣ Configurar Domínios

1. **Backend**: Gere um domínio público (ex: `psipro-api.railway.app`)
2. **Web**: Gere um domínio público (ex: `psipro.railway.app`)
3. Atualize `NEXT_PUBLIC_API_URL` no Web com o domínio do Backend

---

## ⚙️ Arquivos de Configuração

### `backend/railway.json` ✅
Já criado - configura build e start do backend

### `web/railway.json` ✅
Já criado - configura build e start do web

---

## 🔐 Variáveis de Ambiente

### Backend
```env
DATABASE_URL=postgresql://...
JWT_SECRET=chave_super_secreta_aleatoria
PORT=3001
NODE_ENV=production
```

### Web
```env
NEXT_PUBLIC_API_URL=https://psipro-api.railway.app/api
PORT=3000
NODE_ENV=production
```

---

## ⚠️ IMPORTANTE

1. **Ordem de Deploy**: Sempre deploy o backend primeiro
2. **DATABASE_URL**: Railway cria automaticamente quando você adiciona PostgreSQL
3. **CORS**: Certifique-se que o backend permite requisições do domínio do web
4. **Prisma Migrations**: Execute `npm run prisma:migrate` no backend após primeiro deploy

---

## 🐛 Troubleshooting

### Backend não inicia
- Verifique se `DATABASE_URL` está configurada
- Verifique se `prisma generate` rodou no build

### Web não conecta ao Backend
- Verifique `NEXT_PUBLIC_API_URL`
- Verifique CORS no backend
- Verifique se o backend está rodando

### Build falha
- Verifique se `Root Directory` está correto
- Verifique logs do Railway

---

## 📝 Checklist Final

- [ ] Backend deployado e rodando
- [ ] PostgreSQL conectado
- [ ] Web deployado e rodando
- [ ] Domínios configurados
- [ ] Variáveis de ambiente configuradas
- [ ] CORS ajustado no backend
- [ ] Teste de conexão Web → Backend

---

**Pronto para deploy! 🚀**

