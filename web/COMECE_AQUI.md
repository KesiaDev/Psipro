# 🚀 COMECE AQUI - Deploy Railway (Resumo Rápido)

## ⚡ AÇÃO IMEDIATA - 3 PASSOS

### 1️⃣ COMMITAR TUDO (AGORA)

```powershell
cd C:\Users\User\Desktop\psipro
git add .
git commit -m "Preparar projeto para deploy no Railway"
git push origin main
```

### 2️⃣ CRIAR CONTA NO RAILWAY

1. Acesse: https://railway.app
2. Login com GitHub
3. Clique em **"+ New Project"**
4. Selecione **"Deploy from GitHub repo"**
5. Escolha o repositório **`psipro`**

### 3️⃣ SEGUIR O GUIA COMPLETO

Abra o arquivo: **`PASSO_A_PASSO_RAILWAY.md`**

Ele tem TODOS os detalhes passo a passo.

---

## 📊 ESTRUTURA DO DEPLOY

```
Railway Project: psipro
│
├── Service 1: Backend
│   ├── Root: backend/
│   ├── Build: npm run build && npm run prisma:generate
│   └── Start: npm run start:prod
│
└── Service 2: Web
    ├── Root: web/
    ├── Build: npm run build
    └── Start: npm start
```

---

## ✅ CHECKLIST RÁPIDO

- [ ] Commit e push feito
- [ ] Conta Railway criada
- [ ] Projeto Railway criado
- [ ] Backend deployado
- [ ] PostgreSQL adicionado
- [ ] Web deployado
- [ ] CORS configurado
- [ ] Testado

---

**Próximo passo: Execute o comando git acima! 🎯**

