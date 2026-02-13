# 🔍 Debug: Erro "Internal server error" no Login

## ❌ Problema

Ao tentar fazer login, aparece: **"Internal server error"**

Isso significa:
- ✅ Frontend está funcionando
- ✅ Frontend está conectando ao backend
- ❌ Backend está retornando erro 500

---

## 🔍 Possíveis Causas

### 1. Backend não está rodando
- Verifique no Railway se o serviço "Psipro" está **online**

### 2. Banco de dados não tem os usuários do seed
- O backend precisa ter executado o seed
- Usuários: `owner@psiclinic.com`, `psicologo@psipro.com`, etc.

### 3. Migrations do Prisma não foram executadas
- O banco pode não ter as tabelas criadas

### 4. JWT_SECRET não configurado
- Backend precisa de JWT_SECRET no Railway

---

## ✅ Como Debugar

### 1. Verificar Console do Navegador

1. Abra o navegador (F12)
2. Vá na aba **Console**
3. Tente fazer login
4. Veja qual erro aparece (pode mostrar mais detalhes)

### 2. Verificar Network/Tab

1. Abra o navegador (F12)
2. Vá na aba **Network** (Rede)
3. Tente fazer login
4. Clique na requisição `POST /auth/login`
5. Veja a resposta do servidor (Response)

### 3. Verificar Backend no Railway

1. Vá no Railway
2. Clique no serviço **"Psipro"**
3. Vá em **"Deployments"**
4. Veja os **logs** mais recentes
5. Procure por erros

---

## 🔧 Soluções

### Solução 1: Verificar se Backend está Online

No Railway:
- Serviço "Psipro" deve estar com status **"On-line"** (ponto verde)
- Se estiver **"Crashed"**, há um erro no backend

### Solução 2: Executar Seed no Backend

Se os usuários não existem, você pode:

1. **Criar usuário via /register** (mais fácil)
2. Ou executar seed no backend (se tiver acesso)

### Solução 3: Verificar Migrations

No Railway, no serviço Backend:
1. Vá em **Deployments**
2. Clique no deployment mais recente
3. Veja os logs de build
4. Verifique se migrations foram executadas

---

## 🎯 Próximo Passo

**Me diga:**
1. O que aparece no **Console** do navegador (F12)?
2. O que aparece na aba **Network** quando tenta fazer login?
3. O backend está **online** no Railway?

---

**Vamos debugar isso! 🔍**
