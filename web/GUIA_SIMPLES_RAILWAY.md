# 🎯 GUIA SUPER SIMPLES - Railway (Passo a Passo)

## ⚡ FAÇA EXATAMENTE ISSO (NA ORDEM)

---

## PASSO 1: No Railway - Abrir Settings

1. Clique no serviço que está falhando (ex: "impartial-smile")
2. No topo, clique na aba **"Settings"**
3. No menu lateral DIREITO, clique em **"Source"**

---

## PASSO 2: Configurar Source

Na tela que abrir, configure:

### A) Repository
- Deve estar: `KesiaDev/Psipro`
- Se não estiver, corrija

### B) Branch
- Deve estar: `master`
- Se estiver `main`, mude para `master`

### C) Root Directory
- **APAGUE** qualquer coisa que estiver escrito
- Digite: `backend`
- Clique em **Salvar** (Save)

---

## PASSO 3: Configurar Build Command

1. No menu lateral DIREITO, clique em **"Build"**
2. Procure por **"Custom Build Command"**
3. **APAGUE** tudo que estiver escrito
4. Digite exatamente isso:

```
npm install && npm run build && npm run prisma:generate
```

5. Clique em **Salvar**

---

## PASSO 4: Configurar Start Command

1. Ainda em **"Build"** ou procure por **"Deploy"**
2. Procure por **"Start Command"** ou **"Custom Start Command"**
3. **APAGUE** tudo que estiver escrito
4. Digite exatamente isso:

```
npm run start:prod
```

5. Clique em **Salvar**

---

## PASSO 5: Verificar Variáveis

1. No topo, clique na aba **"Variables"**
2. Verifique se tem estas variáveis:

```
DATABASE_URL=postgresql://... (Railway cria automaticamente)
JWT_SECRET=qualquer_chave_aleatoria_longa
PORT=3001
NODE_ENV=production
```

3. Se faltar alguma, clique em **"+ New Variable"** e adicione

---

## PASSO 6: Fazer Deploy

1. No topo, clique na aba **"Deployments"**
2. Clique no botão **"Redeploy"** ou **"Deploy"**
3. Aguarde (pode levar 2-5 minutos)
4. Veja os logs para ver se funcionou

---

## ✅ RESUMO DO QUE CONFIGURAR

| Onde | O Que | Valor |
|------|-------|-------|
| Settings → Source → Root Directory | `backend` | `backend` |
| Settings → Build → Build Command | `npm install && npm run build && npm run prisma:generate` | (sem `cd backend`) |
| Settings → Build → Start Command | `npm run start:prod` | (sem `cd backend`) |
| Settings → Source → Branch | `master` | `master` |

---

## 🎯 SE AINDA NÃO FUNCIONAR

### Recriar Serviço (Última Opção)

1. **Anote as variáveis** que você configurou
2. Delete o serviço atual:
   - Settings → Final da página → Delete Service
3. Crie novo:
   - + New → GitHub Repo
   - Escolha `KesiaDev/Psipro`
   - **IMEDIATAMENTE** configure:
     - Root Directory = `backend`
     - Build Command = `npm install && npm run build && npm run prisma:generate`
     - Start Command = `npm run start:prod`
   - Adicione variáveis
   - Deploy

---

## 💡 DICA IMPORTANTE

**NÃO use `cd backend` nos comandos!**

O Root Directory já faz isso automaticamente.

---

**Siga os passos acima na ordem. Me diga em qual passo você está! 🚀**

