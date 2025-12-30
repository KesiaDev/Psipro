# 🔗 CONECTAR REPOSITÓRIO AO GITHUB

## ⚠️ PROBLEMA IDENTIFICADO

O repositório local **não está conectado ao GitHub**!

Por isso o Railway não encontra a pasta `backend` - ela não está no GitHub ainda.

---

## ✅ SOLUÇÃO: Conectar ao GitHub

### OPÇÃO 1: Se você JÁ TEM um repositório no GitHub

1. **Copie a URL do seu repositório GitHub**
   - Exemplo: `https://github.com/seu-usuario/psipro.git`
   - Ou: `git@github.com:seu-usuario/psipro.git`

2. **Conecte o repositório local:**

```powershell
cd C:\Users\User\Desktop\psipro

# Adicionar remote
git remote add origin https://github.com/SEU-USUARIO/psipro.git

# Verificar
git remote -v

# Pushar código
git add .
git commit -m "Preparar projeto para deploy Railway"
git push -u origin master
```

---

### OPÇÃO 2: Se você NÃO TEM repositório no GitHub ainda

1. **Crie um novo repositório no GitHub:**
   - Acesse: https://github.com/new
   - Nome: `psipro`
   - **NÃO** marque "Initialize with README"
   - Clique em "Create repository"

2. **Copie a URL do repositório criado**

3. **Conecte e push:**

```powershell
cd C:\Users\User\Desktop\psipro

# Adicionar remote
git remote add origin https://github.com/SEU-USUARIO/psipro.git

# Verificar
git remote -v

# Pushar código
git add .
git commit -m "Preparar projeto para deploy Railway"
git push -u origin master
```

---

## 🎯 DEPOIS DE CONECTAR

### 1. Verificar no GitHub

1. Acesse seu repositório no GitHub
2. Verifique se a pasta `backend/` existe
3. Verifique se a pasta `web/` existe

### 2. No Railway

1. Vá em **Settings** → **Source**
2. Verifique:
   - **Repository**: Deve ser seu repositório `psipro`
   - **Branch**: Deve ser `master`
   - **Root Directory**: Deve estar **VAZIO**

3. Vá em **Deployments** → **Redeploy**

---

## ✅ CHECKLIST

- [ ] Repositório criado no GitHub (se não tinha)
- [ ] Remote `origin` adicionado
- [ ] Código pushado (`git push -u origin master`)
- [ ] Pasta `backend/` visível no GitHub
- [ ] Pasta `web/` visível no GitHub
- [ ] Railway configurado com branch `master`
- [ ] Root Directory vazio no Railway
- [ ] Redeploy feito

---

## 💡 IMPORTANTE

**Substitua `SEU-USUARIO` pela sua conta do GitHub!**

Exemplo:
- Se seu usuário é `joaosilva`, use:
  ```
  git remote add origin https://github.com/joaosilva/psipro.git
  ```

---

**Você já tem um repositório no GitHub ou precisa criar? Me diga e eu ajudo! 🚀**

