# 📁 Estrutura do Projeto PsiPro

## Organização do Repositório (Monorepo)

```
psipro/
│
├── 📱 android/          → App Android (não deploya no Railway)
│
├── 🔧 backend/          → NestJS API
│   ├── src/
│   ├── prisma/
│   ├── package.json
│   └── railway.json     ✅ Configurado
│
├── 🌐 web/              → Next.js Frontend
│   ├── app/
│   ├── package.json
│   └── railway.json     ✅ Configurado
│
└── 📄 .gitignore
```

---

## 🚂 Deploy no Railway

### **2 Serviços Separados**

1. **Backend Service**
   - Root: `backend/`
   - Porta: `3001`
   - Database: PostgreSQL (Railway cria)

2. **Web Service**
   - Root: `web/`
   - Porta: `3000`
   - Conecta ao Backend via API

---

## ✅ Arquivos Criados para Railway

- ✅ `backend/railway.json` → Configuração do backend
- ✅ `web/railway.json` → Configuração do web
- ✅ `DEPLOY_RAILWAY.md` → Guia completo de deploy

---

## 🔗 Fluxo de Deploy

```
GitHub Repo (psipro)
    │
    ├─── Service 1: Backend
    │    └── Root: backend/
    │    └── Build: npm run build && npm run prisma:generate
    │    └── Start: npm run start:prod
    │
    └─── Service 2: Web
         └── Root: web/
         └── Build: npm run build
         └── Start: npm start
```

---

**Pronto para deploy! 🚀**

