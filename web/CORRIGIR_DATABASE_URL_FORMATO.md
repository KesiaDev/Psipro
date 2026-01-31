# ⚠️ ERRO: DATABASE_URL formato inválido

## 🔍 PROBLEMA IDENTIFICADO

O erro mostra:
```
Error validating datasource 'db': the URL must start with the protocol 'postgresql://' or 'postgres://'
```

Isso significa que:
- ❌ A `DATABASE_URL` não está no formato correto
- ❌ Ou está vazia
- ❌ Ou não está conectada ao PostgreSQL corretamente

---

## ✅ SOLUÇÃO: Conectar DATABASE_URL usando Variable Reference

### PASSO 1: Ir para Variables do Backend

1. No serviço **Backend (Psipro)**
2. Vá em **"Variables"** (aba no topo)

---

### PASSO 2: Verificar DATABASE_URL

Na lista, procure por `DATABASE_URL`:

**Se NÃO existir:**
- Continue para o Passo 3

**Se EXISTIR mas não está conectada:**
- Delete a variável atual
- Continue para o Passo 3

---

### PASSO 3: Adicionar DATABASE_URL usando Reference

1. Clique em **"+ New Variable"**
2. Em **Name**, digite: `DATABASE_URL`
3. Em **Value**, **NÃO digite nada diretamente**
4. Procure por um botão ou ícone de **cadeia/link** (🔗) ou **"Reference"**
5. Ou clique em **"Add Variable"** no prompt que aparece (se houver)
6. Selecione o serviço **"Postgres"**
7. Selecione a variável **"DATABASE_URL"** do Postgres
8. Clique em **Salvar** ou **Add**

---

### PASSO 4: Verificar se conectou

Após adicionar, você deve ver:
- ✅ `DATABASE_URL` na lista
- ✅ Mostrando que está conectada ao Postgres (pode ter ícone de link 🔗)
- ✅ Valor mascarado (****)

---

### PASSO 5: Redeploy

1. Vá em **"Deployments"**
2. Clique em **"Redeploy"**
3. Aguarde o deploy
4. Verifique os logs

---

## 🎯 ALTERNATIVA: Copiar DATABASE_URL Manualmente

Se não conseguir usar Reference:

1. **No serviço PostgreSQL:**
   - Vá em **"Variables"**
   - Clique no ícone de **olho** 👁️ ao lado de `DATABASE_URL`
   - **Copie** o valor completo (deve começar com `postgresql://`)

2. **No serviço Backend:**
   - Vá em **"Variables"**
   - Clique em **"+ New Variable"**
   - **Name**: `DATABASE_URL`
   - **Value**: Cole o valor que você copiou
   - Salve

---

## ✅ CHECKLIST

- [ ] DATABASE_URL existe no serviço Backend?
- [ ] DATABASE_URL está conectada ao Postgres (usando Reference)?
- [ ] Ou DATABASE_URL tem valor que começa com `postgresql://`?
- [ ] Redeploy feito?

---

## 💡 IMPORTANTE

**A forma mais confiável é usar Variable Reference!**

- Railway conecta automaticamente
- Se o PostgreSQL mudar, atualiza automaticamente
- Garante que o formato está correto

---

**Vá em Variables do Backend e conecte DATABASE_URL ao Postgres usando Reference! 🚀**

