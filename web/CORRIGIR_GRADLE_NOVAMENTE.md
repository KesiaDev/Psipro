# ⚠️ ERRO: Railway Tentando Compilar Android Novamente

## 🔍 PROBLEMA

O Railway está executando:
```
./gradlew clean build
```

Isso significa que está detectando a pasta `android/` ao invés de `backend/`.

**Causa:** Root Directory não está configurado ou foi resetado.

---

## ✅ SOLUÇÃO: Verificar Root Directory

### PASSO 1: Ir para Settings → Source

1. No serviço **Backend** (Psipro)
2. Vá em **Settings** (aba no topo)
3. No menu lateral direito, clique em **"Source"**

---

### PASSO 2: Verificar Root Directory

1. Procure por **"Root Directory"**
2. **DEVE estar como:** `backend`
3. Se estiver vazio ou com outro valor:
   - **APAGUE** o que estiver
   - Digite: `backend`
   - Clique em **Salvar**

---

### PASSO 3: Verificar Branch

1. Ainda em **Source**
2. Verifique **"Branch"**
3. **DEVE estar como:** `master`
4. Se estiver como `main`, mude para `master`
5. Salve

---

### PASSO 4: Verificar Build Command

1. Vá em **Settings** → **Build** (menu lateral)
2. Verifique **"Custom Build Command"**
3. **DEVE estar:**
   ```
   npm install && npm run build && npm run prisma:generate
   ```
4. Se tiver `gradlew` ou `gradle`, **APAGUE** e coloque o comando acima
5. Salve

---

### PASSO 5: Redeploy

1. Vá em **Deployments**
2. Clique em **Redeploy**
3. Aguarde

---

## ✅ CHECKLIST

- [ ] Root Directory = `backend` (não vazio!)
- [ ] Branch = `master` (não `main`)
- [ ] Build Command = `npm install && npm run build && npm run prisma:generate` (sem gradle)
- [ ] Start Command = `npm start` (ou `npm run start:prod`)
- [ ] Redeploy feito

---

## 💡 IMPORTANTE

**Root Directory DEVE estar como `backend`!**

Se estiver vazio, o Railway detecta automaticamente e pode pegar `android/` primeiro.

---

**Vá em Settings → Source e verifique se Root Directory está como `backend`! 🚀**

