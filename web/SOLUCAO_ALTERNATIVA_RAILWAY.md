# 🔧 SOLUÇÃO ALTERNATIVA - Se Não Encontrar Root Directory

## ⚡ MÉTODO RÁPIDO (SEM ROOT DIRECTORY)

Se você não conseguir encontrar "Root Directory", use esta solução:

---

## 🎯 SOLUÇÃO: Modificar Build Command

### Para BACKEND:

1. Vá em **Settings** → **Build Command**
2. **Substitua** o comando atual por:

```
cd backend && npm install && npm run build && npm run prisma:generate
```

3. Em **Start Command**, coloque:

```
cd backend && npm run start:prod
```

4. **Save** e **Redeploy**

---

### Para WEB:

1. Vá em **Settings** → **Build Command**
2. **Substitua** o comando atual por:

```
cd web && npm install && npm run build
```

3. Em **Start Command**, coloque:

```
cd web && npm start
```

4. **Save** e **Redeploy**

---

## 📋 PASSOS COMPLETOS

### 1. Abrir Settings do Serviço

1. Clique no serviço que está falhando
2. Procure por **"Settings"** ou **"Configurações"**
   - Pode estar no topo (aba)
   - Pode estar no menu lateral (⚙️)
   - Pode estar nos 3 pontinhos (...) do card

### 2. Encontrar Build Command

1. Role a página para baixo
2. Procure por:
   - "Build Command"
   - "Comando de Compilação"
   - "Build"
   - "Compile"

### 3. Modificar Build Command

**Backend:**
```
cd backend && npm install && npm run build && npm run prisma:generate
```

**Web:**
```
cd web && npm install && npm run build
```

### 4. Modificar Start Command

**Backend:**
```
cd backend && npm run start:prod
```

**Web:**
```
cd web && npm start
```

### 5. Salvar e Redeploy

1. Clique em **"Save"** ou **"Salvar"**
2. Vá em **"Deployments"**
3. Clique em **"Redeploy"** ou **"Deploy"**

---

## ✅ RESULTADO ESPERADO

Após fazer isso, os logs devem mostrar:

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

## 🎯 SE AINDA NÃO ENCONTRAR BUILD COMMAND

### Método 3: Recriar Serviço

1. **Anote as variáveis de ambiente** do serviço atual
2. **Delete o serviço**:
   - Settings → Final da página → Delete Service
3. **Crie novo serviço**:
   - + New → GitHub Repo
   - Escolha `psipro`
   - **IMEDIATAMENTE** após criar, procure por qualquer campo de configuração
   - Configure Build Command com `cd backend` ou `cd web`
   - Adicione variáveis
   - Deploy

---

## 💡 POR QUE ISSO FUNCIONA?

O comando `cd backend` ou `cd web` no início do Build Command força o Railway a:
1. Entrar na pasta correta
2. Executar os comandos npm lá
3. Não detectar a pasta `android/`
4. Compilar com Node.js, não Gradle

---

## 📞 PRÓXIMO PASSO

1. Tente encontrar **Settings** → **Build Command**
2. Modifique conforme acima
3. Salve e faça Redeploy
4. Me diga se funcionou!

---

**Consegue ver a opção "Build Command" nas Settings? 🚀**

