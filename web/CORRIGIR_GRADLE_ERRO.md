# ⚠️ ERRO: Railway Tentando Compilar Android (Gradle)

## 🔍 PROBLEMA IDENTIFICADO

O Railway está tentando executar:
```
./gradlew clean build -x check -x test -Pproduction
```

Isso significa que ele está detectando a pasta `android/` ao invés de `backend/` ou `web/`.

---

## ✅ SOLUÇÃO IMEDIATA

### 1️⃣ Verificar Qual Serviço Está Falhando

1. No Railway, veja qual serviço está com erro
2. Verifique o nome do serviço:
   - Se for "Backend" → deve apontar para `backend/`
   - Se for "Web" → deve apontar para `web/`
   - Se for outro nome → pode estar apontando para raiz ou `android/`

### 2️⃣ Configurar Root Directory Corretamente

1. Clique no serviço que está falhando
2. Vá em **"Settings"** (⚙️)
3. Role até **"Source"**
4. Em **"Root Directory"**, configure:
   
   **Para Backend:**
   ```
   backend
   ```
   
   **Para Web:**
   ```
   web
   ```
   
5. ⚠️ **NÃO deixe vazio** (vazio = raiz do projeto = detecta android)
6. ⚠️ **NÃO use** `android` (isso vai tentar compilar Android)
7. Clique em **"Save"**

### 3️⃣ Verificar Build Command

Após configurar Root Directory, verifique:

**Backend:**
- Build Command: `npm install && npm run build && npm run prisma:generate`
- Start Command: `npm run start:prod`

**Web:**
- Build Command: `npm install && npm run build`
- Start Command: `npm start`

### 4️⃣ Redeploy

1. Vá em **"Deployments"**
2. Clique em **"Redeploy"** ou **"Deploy"**
3. Agora deve compilar com Node.js, não Gradle

---

## 🎯 CHECKLIST DE CORREÇÃO

- [ ] Root Directory configurado como `backend` (para backend)
- [ ] Root Directory configurado como `web` (para web)
- [ ] Build Command usa `npm` (não `gradlew`)
- [ ] Start Command usa `npm` (não `gradlew`)
- [ ] Redeploy feito

---

## 🔄 SE O PROBLEMA PERSISTIR

### Opção 1: Deletar e Recriar Serviço

1. Delete o serviço atual
2. Crie novo serviço
3. **IMPORTANTE**: Configure Root Directory ANTES do primeiro deploy
4. Configure Build Command
5. Configure Start Command
6. Adicione variáveis
7. Deploy

### Opção 2: Verificar Estrutura do Repositório

Certifique-se que a estrutura está assim:
```
psipro/
├── backend/     ← Backend deve apontar aqui
├── web/        ← Web deve apontar aqui
└── android/    ← NÃO deve apontar aqui
```

---

## 💡 DICA IMPORTANTE

**Sempre configure Root Directory ANTES do primeiro deploy!**

Se você deixar vazio, o Railway vai:
1. Detectar automaticamente o tipo de projeto
2. Pode detectar `android/` primeiro
3. Tentar compilar com Gradle
4. ❌ Falhar

---

## ✅ RESULTADO ESPERADO

Após corrigir, os logs devem mostrar:
```
npm install
npm run build
npm run prisma:generate  (backend)
npm start
```

**NÃO deve aparecer:**
```
./gradlew
gradle
```

---

**Configure o Root Directory agora e faça redeploy! 🚀**

