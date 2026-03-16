# 🎯 SOLUÇÃO DEFINITIVA - Erro "Could not find root directory: backend"

## ⚠️ PROBLEMA

O Railway está procurando a pasta `backend` mas não encontra porque:
1. O código pode não estar no GitHub
2. O branch pode estar errado (master vs main)
3. O Root Directory pode estar configurado incorretamente

---

## ✅ SOLUÇÃO COMPLETA

### PASSO 1: Commitar e Pushar TUDO

```powershell
cd C:\Users\User\Desktop\psipro

# Verificar status
git status

# Adicionar tudo
git add .

# Commitar
git commit -m "Preparar backend e web para deploy Railway"

# Pushar para GitHub
git push origin master
```

⚠️ **Aguarde o push completar!**

---

### PASSO 2: No Railway - Verificar Branch

1. Vá em **Settings** → **Source** (menu lateral direito)
2. Verifique o campo **"Branch"**:
   - Deve ser: `master` (não `main`)
   - Se estiver como `main`, mude para `master`
3. Salve

---

### PASSO 3: No Railway - Remover Root Directory

1. Ainda em **Settings** → **Source**
2. Procure por **"Root Directory"** ou **"Working Directory"**
3. Se estiver preenchido com `backend`, **DELETE** o valor
4. Deixe **VAZIO** (sem nada)
5. Salve

**Por quê?** O Build Command já tem `cd backend`, então não precisa de Root Directory.

---

### PASSO 4: Verificar Build Command

1. Vá em **Settings** → **Build** (ou onde está o Custom Build Command)
2. Verifique se está:

```
cd backend && npm install && npm run build && npm run prisma:generate
```

3. Se não estiver, corrija e salve

---

### PASSO 5: Verificar Start Command

1. Em **Settings** → **Deploy** ou **Build**
2. Procure por **"Start Command"**
3. Configure como:

```
cd backend && npm run start:prod
```

---

### PASSO 6: Redeploy

1. Vá em **Deployments**
2. Clique em **Redeploy** ou **Deploy**
3. Aguarde o build
4. Verifique os logs

---

## 📋 CHECKLIST FINAL

- [ ] Código pushado para GitHub (`git push origin master`)
- [ ] Branch no Railway = `master` (não `main`)
- [ ] Root Directory = **VAZIO** (não `backend`)
- [ ] Build Command = `cd backend && npm install && npm run build && npm run prisma:generate`
- [ ] Start Command = `cd backend && npm run start:prod`
- [ ] Redeploy feito

---

## 🔍 VERIFICAR NO GITHUB

1. Acesse seu repositório no GitHub
2. Verifique se está no branch `master`
3. Verifique se a pasta `backend/` existe
4. Verifique se a pasta `web/` existe
5. Se não existir, o push não completou

---

## 💡 POR QUE DEIXAR ROOT DIRECTORY VAZIO?

Quando você usa `cd backend` no Build Command:
- O Railway começa na raiz do repositório
- O comando `cd backend` entra na pasta
- Executa os comandos npm lá
- Funciona perfeitamente!

Se você configurar Root Directory como `backend`:
- O Railway tenta entrar em `backend/` antes de executar
- Mas se não encontrar, dá erro
- Por isso é melhor deixar vazio e usar `cd` no comando

---

## 🚀 EXECUTE AGORA

1. Execute os comandos git acima
2. No Railway, verifique branch = `master`
3. Remova Root Directory (deixe vazio)
4. Faça Redeploy

**Me diga o resultado! 🎯**

