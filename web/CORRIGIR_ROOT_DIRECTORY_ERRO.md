# ⚠️ ERRO: "Could not find root directory: backend"

## 🔍 PROBLEMA IDENTIFICADO

O Railway está dizendo: **"Could not find root directory: backend"**

Isso significa que:
- ❌ O Railway está procurando a pasta `backend` no repositório GitHub
- ❌ Mas não está encontrando

---

## ✅ SOLUÇÃO: Verificar e Commitar Código

### PASSO 1: Verificar se backend está no Git

```powershell
cd C:\Users\User\Desktop\psipro
git status
```

Se aparecer `backend/` como modificado ou não rastreado, precisa commitar.

---

### PASSO 2: Commitar TUDO

```powershell
cd C:\Users\User\Desktop\psipro

# Adicionar tudo
git add .

# Commitar
git commit -m "Adicionar backend e web para deploy Railway"

# Push para GitHub
git push origin main
```

⚠️ **IMPORTANTE**: Aguarde o push completar!

---

### PASSO 3: Verificar no GitHub

1. Acesse seu repositório no GitHub
2. Verifique se a pasta `backend/` existe
3. Verifique se a pasta `web/` existe
4. Se não existir, o push não completou

---

### PASSO 4: No Railway - Verificar Repositório

1. Vá em **Settings** → **Source** (menu lateral)
2. Verifique:
   - **Repository**: Deve ser seu repositório `psipro`
   - **Branch**: Deve ser `main` ou `master`
   - **Root Directory**: Deve estar **VAZIO** ou **não configurado**

---

### PASSO 5: Configurar Root Directory Corretamente

**OPÇÃO A - Se Root Directory estiver como `backend`:**
1. Deixe **VAZIO** (sem nada)
2. Salve
3. Use Build Command com `cd backend` (já está configurado)

**OPÇÃO B - Se Root Directory estiver vazio:**
1. Deixe vazio mesmo
2. O Build Command com `cd backend` já resolve

---

## 🎯 SOLUÇÃO ALTERNATIVA: Remover Root Directory

Se você configurou Root Directory como `backend` nas Settings:

1. Vá em **Settings** → **Source**
2. Em **Root Directory**, **DELETE** o valor `backend`
3. Deixe **VAZIO**
4. Salve
5. O Build Command `cd backend && ...` já faz o trabalho

---

## ✅ CHECKLIST DE CORREÇÃO

- [ ] Código commitado localmente
- [ ] Código pushado para GitHub
- [ ] Pasta `backend/` existe no GitHub
- [ ] Pasta `web/` existe no GitHub
- [ ] Root Directory está **VAZIO** (não `backend`)
- [ ] Build Command tem `cd backend && ...`
- [ ] Redeploy feito

---

## 🚀 DEPOIS DE CORRIGIR

1. No Railway, vá em **Deployments**
2. Clique em **Redeploy**
3. Aguarde o build
4. Verifique os logs

---

## 💡 POR QUE ISSO ACONTECEU?

O Railway procura a pasta no repositório GitHub. Se:
- O código não foi pushado → não encontra
- Root Directory está como `backend` mas não existe no repo → não encontra
- Repositório errado conectado → não encontra

---

**Execute os comandos git acima e me diga o resultado! 🚀**

