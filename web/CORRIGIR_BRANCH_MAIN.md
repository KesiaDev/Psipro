# ⚠️ PROBLEMA: Branch Errado!

## 🔍 PROBLEMA IDENTIFICADO

O Railway está tentando fazer deploy do branch **`main`**, mas o código foi pushado para **`master`**!

Por isso ele não encontra a pasta `backend` - ela não está no branch `main`.

---

## ✅ SOLUÇÃO: Mudar Branch no Railway

### PASSO 1: Ir para Settings → Source

1. No Railway, clique na aba **"Settings"** (no topo)
2. No menu lateral direito, clique em **"Source"**

---

### PASSO 2: Mudar Branch

1. Procure por **"Branch"** ou **"Git Branch"**
2. Se estiver como `main`, mude para: `master`
3. Clique em **Salvar**

---

### PASSO 3: Verificar Root Directory

1. Ainda em **Source**, verifique se **Root Directory** está como: `backend`
2. Se não estiver, configure como `backend`
3. Salve

---

### PASSO 4: Fazer Redeploy

1. Vá em **"Deployments"**
2. Clique em **"Redeploy"**
3. Aguarde

---

## 🎯 ALTERNATIVA: Push para Branch main

Se preferir usar o branch `main`:

```powershell
cd C:\Users\User\Desktop\psipro

# Criar branch main a partir de master
git checkout -b main

# Push para main
git push origin main
```

Depois, no Railway, configure branch = `main`.

---

## ✅ SOLUÇÃO RECOMENDADA

**Mudar Railway para usar branch `master`** (mais simples)

1. Settings → Source → Branch = `master`
2. Root Directory = `backend`
3. Redeploy

---

**Vá em Settings → Source e mude o Branch para `master`! 🚀**

