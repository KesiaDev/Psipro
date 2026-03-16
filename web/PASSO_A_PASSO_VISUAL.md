# 📸 PASSO A PASSO VISUAL - Railway

## 🎯 O QUE FAZER (MUITO SIMPLES)

---

### 1️⃣ CLIQUE EM "SETTINGS"

- No topo da página, tem abas
- Clique em **"Settings"** (Configurações)

---

### 2️⃣ NO MENU LATERAL DIREITO, CLIQUE EM "SOURCE"

- Do lado direito da tela, tem um menu
- Clique em **"Source"**

---

### 3️⃣ CONFIGURE ROOT DIRECTORY

- Procure por um campo chamado **"Root Directory"**
- Se tiver algo escrito, **APAGUE**
- Digite: `backend`
- Clique em **Salvar**

---

### 4️⃣ NO MENU LATERAL, CLIQUE EM "BUILD"

- Volte ao menu lateral direito
- Clique em **"Build"**

---

### 5️⃣ CONFIGURE BUILD COMMAND

- Procure por **"Custom Build Command"**
- Se tiver algo escrito, **APAGUE TUDO**
- Digite exatamente:

```
npm install && npm run build && npm run prisma:generate
```

- Clique em **Salvar**

---

### 6️⃣ CONFIGURE START COMMAND

- Ainda na mesma tela, procure por **"Start Command"**
- Se tiver algo escrito, **APAGUE TUDO**
- Digite exatamente:

```
npm run start:prod
```

- Clique em **Salvar**

---

### 7️⃣ VÁ EM "DEPLOYMENTS" E CLIQUE EM "REDEPLOY"

- No topo, clique na aba **"Deployments"**
- Clique no botão **"Redeploy"**
- Aguarde

---

## ✅ PRONTO!

Se seguir esses 7 passos, deve funcionar!

---

## 🆘 SE NÃO CONSEGUIR ENCONTRAR

Me diga:
1. Você consegue ver a aba "Settings"?
2. Você consegue ver o menu lateral direito?
3. O que aparece na tela quando você clica em Settings?

**Me mostre ou descreva o que você vê! 👀**

