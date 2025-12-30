# ✅ CORRIGIR BUILD COMMAND - Passo a Passo

## 🎯 O QUE FAZER AGORA (Você está na tela certa!)

### 1️⃣ CORRIGIR BUILD COMMAND

No campo **"Custom Build Command"** que você está vendo:

**Substitua por (completo):**
```
cd backend && npm install && npm run build && npm run prisma:generate
```

⚠️ **Importante**: Certifique-se que está completo, não cortado!

---

### 2️⃣ ENCONTRAR START COMMAND

1. No menu lateral direito, clique em **"Build"** ou **"Deploy"**
2. Procure por **"Start Command"** ou **"Custom Start Command"**
3. Configure como:

```
cd backend && npm run start:prod
```

---

### 3️⃣ ENCONTRAR ROOT DIRECTORY (Opcional, mas recomendado)

1. No menu lateral direito, clique em **"Source"**
2. Procure por **"Root Directory"** ou **"Working Directory"**
3. Se encontrar, configure como: `backend`

---

## 📋 CHECKLIST COMPLETO

### Build Command:
- [ ] Campo: "Custom Build Command"
- [ ] Valor: `cd backend && npm install && npm run build && npm run prisma:generate`
- [ ] Está completo (não cortado)

### Start Command:
- [ ] Encontrou "Start Command" ou "Custom Start Command"
- [ ] Valor: `cd backend && npm run start:prod`

### Root Directory (Opcional):
- [ ] Clicou em "Source" no menu lateral
- [ ] Configurou como: `backend`

### Variáveis de Ambiente:
- [ ] Vá em "Variables" (aba no topo)
- [ ] Verifique se tem:
  - `DATABASE_URL` (conectada ao PostgreSQL)
  - `JWT_SECRET`
  - `PORT=3001`
  - `NODE_ENV=production`

---

## 🚀 DEPOIS DE CONFIGURAR

1. Clique em **"Save"** ou **"Salvar"** (se houver)
2. Vá na aba **"Deployments"** (no topo)
3. Clique em **"Redeploy"** ou **"Deploy"**
4. Aguarde o build
5. Verifique os logs

---

## ✅ RESULTADO ESPERADO

Após corrigir, os logs devem mostrar:

```
cd backend
npm install
npm run build
npm run prisma:generate
npm run start:prod
```

**NÃO deve aparecer mais:**
```
./gradlew
gradle
```

---

## 💡 DICA

Se o Build Command já está com `cd backend`, você só precisa:
1. ✅ Verificar se está completo
2. ✅ Configurar Start Command
3. ✅ Fazer Redeploy

---

**Corrija o Build Command e me diga se funcionou! 🚀**

