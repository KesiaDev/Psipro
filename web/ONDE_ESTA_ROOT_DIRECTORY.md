# 📍 ONDE ENCONTRAR ROOT DIRECTORY NO RAILWAY

## 🎯 PASSO A PASSO VISUAL

### PASSO 1: Encontrar o Serviço

1. No dashboard do Railway, você verá os serviços (cards)
2. Clique no serviço que está falhando (ex: "Psipro" ou "sorriso-imparcial")

---

### PASSO 2: Abrir Settings

**OPÇÃO A - Menu Superior:**
1. No topo da página, você verá abas: "Arquitetura", "Observabilidade", "Registros", **"Configurações"**
2. Clique em **"Configurações"** (Settings)

**OPÇÃO B - Menu Lateral:**
1. No lado esquerdo, procure por um ícone de ⚙️ (engrenagem)
2. Clique nele

**OPÇÃO C - Menu do Serviço:**
1. Clique nos **3 pontinhos** (...) no card do serviço
2. Procure por "Settings" ou "Configurações"

---

### PASSO 3: Encontrar "Source" ou "Root Directory"

Dentro de Settings, role a página para baixo e procure por:

**Seções onde pode estar:**

1. **"Source"** ou **"Fonte"**
   - Geralmente no topo das configurações
   - Mostra informações do repositório GitHub
   - Procure por um campo chamado:
     - "Root Directory"
     - "Diretório Raiz"
     - "Working Directory"
     - "Pasta Raiz"

2. **"Build"** ou **"Compilação"**
   - Pode estar junto com Build Command
   - Procure por "Root Directory"

3. **"Deploy"** ou **"Implantação"**
   - Pode estar nas configurações de deploy

---

### PASSO 4: Se NÃO Encontrar "Root Directory"

**ALTERNATIVA 1: Usar railway.json**

O Railway pode usar o arquivo `railway.json` que já criamos. Verifique se os arquivos existem:

- `backend/railway.json` ✅ (já criado)
- `web/railway.json` ✅ (já criado)

**ALTERNATIVA 2: Configurar via Build Command**

Se não encontrar Root Directory, você pode especificar no Build Command:

**Backend:**
```
cd backend && npm install && npm run build && npm run prisma:generate
```

**Web:**
```
cd web && npm install && npm run build
```

---

## 🔍 ONDE PROCURAR (TODAS AS POSSIBILIDADES)

### 1. Aba "Configurações" (Settings)
- Clique na aba no topo
- Role para baixo
- Procure por "Source", "Build", ou "Deploy"

### 2. Menu Lateral Esquerdo
- Ícone de ⚙️ (engrenagem)
- Ou menu com 3 linhas (☰)

### 3. Card do Serviço
- Clique nos 3 pontinhos (...)
- Menu dropdown com opções

### 4. Página do Deploy
- Vá em "Deployments"
- Clique no deployment
- Procure por "Settings" ou "Configure"

---

## 💡 DICA: Se Ainda Não Encontrar

### Método Alternativo - Via Build Command

1. Vá em **Settings** → **Build Command**
2. Em vez de:
   ```
   npm install && npm run build
   ```
   
   Use:
   ```
   cd backend && npm install && npm run build && npm run prisma:generate
   ```
   
   Ou para Web:
   ```
   cd web && npm install && npm run build
   ```

3. Isso força o Railway a executar na pasta correta

---

## 🚀 SOLUÇÃO RÁPIDA (SE NADA FUNCIONAR)

### Recriar Serviço com Configuração Correta

1. **Delete o serviço atual**:
   - Settings → Delete Service (no final da página)

2. **Criar novo serviço**:
   - + New → GitHub Repo
   - Escolha `psipro`
   - **IMEDIATAMENTE** procure por "Root Directory" ou "Working Directory"
   - Configure ANTES de fazer deploy

3. **Se aparecer opção de "Detect" ou "Auto-detect"**:
   - NÃO use
   - Configure manualmente

---

## 📸 O QUE PROCURAR (Descrição Visual)

Procure por um campo de texto que diz:
- "Root Directory"
- "Working Directory"  
- "Source Directory"
- "Base Directory"
- "Diretório Raiz" (em português)

Geralmente está:
- Logo abaixo de "Repository" ou "Repositório"
- Junto com "Branch" (ramo)
- Antes de "Build Command"

---

## ✅ CHECKLIST

- [ ] Cliquei no serviço que está falhando
- [ ] Abri "Settings" ou "Configurações"
- [ ] Rolei a página para baixo
- [ ] Procurei por "Root Directory" ou "Source"
- [ ] Se não encontrei, tentei o método alternativo (cd no Build Command)

---

**Me diga: você conseguiu abrir a página de Settings? O que você vê lá? 🤔**

