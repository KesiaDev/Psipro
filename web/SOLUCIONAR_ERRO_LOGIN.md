# 🔧 Solucionar Erro "Internal server error" no Login

## ✅ O Que Sabemos

- ✅ Frontend está funcionando
- ✅ Backend está online (responde requisições)
- ❌ Backend retorna erro 500 ao tentar fazer login

## 🔍 Causas Prováveis

### 1. Migrations do Prisma não foram executadas
O banco pode não ter as tabelas criadas.

### 2. Usuários do seed não existem
O usuário `owner@psiclinic.com` pode não estar no banco.

### 3. Erro no backend (ver logs)

---

## ✅ SOLUÇÕES

### Solução 1: Criar Usuário via /register (MAIS FÁCIL) 🎯

Se o seed não foi executado, você pode criar um usuário:

1. Acesse: `http://localhost:3000/register`
2. Preencha:
   - Nome: Qualquer nome
   - Email: Qualquer email (ex: `teste@teste.com`)
   - Senha: Mínimo 6 caracteres
3. Clique em "Criar conta"
4. Faça login com esse usuário

---

### Solução 2: Verificar Logs do Backend no Railway

1. No Railway, clique no serviço **"Psipro"**
2. Vá em **"Deployments"**
3. Clique no deployment mais recente
4. Vá na aba **"Logs"**
5. Procure por erros relacionados a:
   - Prisma
   - Database
   - Login
   - Auth

**Me diga o que aparece nos logs!**

---

### Solução 3: Executar Migrations no Railway

Se as migrations não foram executadas:

1. No Railway, no serviço Backend
2. Vá em **"Deployments"**
3. Clique no deployment mais recente
4. Use o terminal (se disponível) ou execute via Railway CLI:
   ```bash
   npx prisma migrate deploy
   ```

---

### Solução 4: Verificar Console do Navegador

Para ver mais detalhes do erro:

1. Abra o navegador (pressione **F12**)
2. Vá na aba **Console**
3. Tente fazer login
4. Veja se aparece algum erro específico

---

## 🎯 RECOMENDAÇÃO

**Tente primeiro criar um usuário via `/register`!**

É mais rápido e não precisa acessar o Railway.

---

## 📋 Checklist

- [ ] Tentou criar usuário via `/register`?
- [ ] Verificou logs do backend no Railway?
- [ ] Viu o console do navegador (F12)?
- [ ] Backend está online no Railway?

---

**Tente criar um usuário via /register primeiro! 🚀**
