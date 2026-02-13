# ⚠️ ERRO: "can't cd to backend"

## 🔍 PROBLEMA IDENTIFICADO

O Railway está executando:
```
cd backend && npm install && npm run build && npm run prisma:generate
```

Mas está dando erro:
```
sh: 1: cd: can't cd to backend
```

Isso significa que a pasta `backend/` **não existe** no repositório que o Railway está vendo.

---

## ✅ SOLUÇÕES POSSÍVEIS

### SOLUÇÃO 1: Verificar se backend está no GitHub

1. Acesse: **https://github.com/KesiaDev/Psipro**
2. Verifique se a pasta `backend/` existe
3. Clique nela e veja se tem arquivos dentro
4. Se **NÃO existir**, o push não funcionou corretamente

---

### SOLUÇÃO 2: Fazer Push Novamente

Se a pasta não estiver no GitHub:

```powershell
cd C:\Users\User\Desktop\psipro

# Verificar se backend existe localmente
dir backend

# Adicionar tudo
git add backend/
git add web/

# Commitar
git commit -m "Adicionar pastas backend e web"

# Push forçado (se necessário)
git push origin master --force
```

⚠️ **Cuidado com --force**: Só use se tiver certeza!

---

### SOLUÇÃO 3: Verificar Branch no Railway

1. No Railway, vá em **Settings** → **Source**
2. Verifique:
   - **Branch**: Deve ser `master`
   - **Repository**: Deve ser `KesiaDev/Psipro`
3. Se estiver diferente, corrija

---

### SOLUÇÃO 4: Usar Root Directory (Alternativa)

Se o `cd backend` não funcionar, use Root Directory:

1. No Railway, vá em **Settings** → **Source**
2. Em **Root Directory**, digite: `backend`
3. Em **Build Command**, **REMOVA** o `cd backend &&`:
   ```
   npm install && npm run build && npm run prisma:generate
   ```
4. Em **Start Command**, **REMOVA** o `cd backend &&`:
   ```
   npm run start:prod
   ```
5. Salve e Redeploy

---

### SOLUÇÃO 5: Verificar Estrutura do Repositório

O Railway pode estar vendo uma estrutura diferente. Verifique:

1. No GitHub, acesse: https://github.com/KesiaDev/Psipro/tree/master
2. Veja se a estrutura é:
   ```
   Psipro/
   ├── backend/
   ├── web/
   └── android/
   ```
3. Se for diferente, pode ser o problema

---

## 🎯 SOLUÇÃO RECOMENDADA (Tente nesta ordem)

### Passo 1: Verificar no GitHub
- Acesse https://github.com/KesiaDev/Psipro
- Veja se `backend/` existe
- Se não existir → Solução 2

### Passo 2: Se existir no GitHub
- Verifique branch no Railway = `master`
- Tente Solução 4 (usar Root Directory)

### Passo 3: Se ainda não funcionar
- Recrie o serviço no Railway
- Configure Root Directory = `backend` desde o início

---

## ✅ CHECKLIST

- [ ] Pasta `backend/` existe no GitHub?
- [ ] Branch no Railway = `master`?
- [ ] Repository = `KesiaDev/Psipro`?
- [ ] Tentou usar Root Directory = `backend`?
- [ ] Build Command sem `cd backend`?

---

## 💡 DICA

**A forma mais confiável é usar Root Directory:**

1. Root Directory = `backend`
2. Build Command = `npm install && npm run build && npm run prisma:generate` (sem `cd backend`)
3. Start Command = `npm run start:prod` (sem `cd backend`)

Isso é mais confiável que usar `cd` no comando.

---

**Verifique no GitHub se a pasta backend existe e me diga o resultado! 🔍**

