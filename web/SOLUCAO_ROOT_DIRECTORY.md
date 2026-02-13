# ✅ SOLUÇÃO: Usar Root Directory (Mais Confiável)

## 🎯 PROBLEMA

O comando `cd backend` no Build Command não está funcionando. A solução mais confiável é usar **Root Directory**.

---

## 🚀 CONFIGURAÇÃO CORRETA

### 1️⃣ Configurar Root Directory

1. No Railway, vá em **Settings** → **Source** (menu lateral)
2. Procure por **"Root Directory"** ou **"Working Directory"**
3. Digite: `backend`
4. Salve

---

### 2️⃣ Ajustar Build Command

1. Vá em **Settings** → **Build** (ou onde está Custom Build Command)
2. **REMOVA** o `cd backend &&` do início
3. Deixe apenas:

```
npm install && npm run build && npm run prisma:generate
```

4. Salve

---

### 3️⃣ Ajustar Start Command

1. Em **Settings** → **Deploy** ou **Build**
2. Procure por **"Start Command"**
3. **REMOVA** o `cd backend &&` do início
4. Deixe apenas:

```
npm run start:prod
```

5. Salve

---

## ✅ CONFIGURAÇÃO FINAL

### Root Directory:
```
backend
```

### Build Command:
```
npm install && npm run build && npm run prisma:generate
```

### Start Command:
```
npm run start:prod
```

---

## 🎯 POR QUE ISSO FUNCIONA?

Quando você configura Root Directory = `backend`:
- O Railway **já entra** na pasta `backend/` antes de executar os comandos
- Não precisa usar `cd backend` nos comandos
- É mais confiável e direto

---

## 📋 CHECKLIST

- [ ] Root Directory = `backend`
- [ ] Build Command = `npm install && npm run build && npm run prisma:generate` (sem `cd backend`)
- [ ] Start Command = `npm run start:prod` (sem `cd backend`)
- [ ] Branch = `master`
- [ ] Repository = `KesiaDev/Psipro`
- [ ] Redeploy feito

---

## 🚀 DEPOIS DE CONFIGURAR

1. Vá em **Deployments**
2. Clique em **Redeploy**
3. Aguarde o build
4. Verifique os logs

---

## ✅ RESULTADO ESPERADO

Os logs devem mostrar:

```
npm install
npm run build
npm run prisma:generate
npm run start:prod
```

**NÃO deve aparecer mais:**
- ❌ "can't cd to backend"
- ❌ "cd backend"

---

**Configure Root Directory = `backend` e remova `cd backend` dos comandos! 🎯**

