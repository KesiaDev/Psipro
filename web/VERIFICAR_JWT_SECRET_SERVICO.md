# ⚠️ ERRO: JWT_SECRET não está sendo lido

## 🔍 PROBLEMA

O erro mostra que `JWT_SECRET` não está sendo encontrado pelo backend, mesmo que você tenha configurado.

**Possíveis causas:**
- A variável está no serviço errado
- A variável não está conectada ao serviço Backend
- Precisa de redeploy para pegar a variável

---

## ✅ SOLUÇÃO: Verificar JWT_SECRET no Serviço Correto

### PASSO 1: Ir para o Serviço Backend (Psipro)

1. No menu lateral, clique no serviço **"Psipro"** (Backend)
2. Vá em **"Variables"** (aba no topo)

---

### PASSO 2: Verificar se JWT_SECRET existe

Na lista de Variables, procure por:
- `JWT_SECRET`

**Se NÃO existir:**
- Clique em **"+ New Variable"**
- **Name**: `JWT_SECRET`
- **Value**: Digite uma chave aleatória longa (ex: `minha_chave_super_secreta_123456789_abcdefghijklmnop`)
- Clique em **Salvar**

**Se EXISTIR:**
- Verifique se o valor está correto
- Se estiver mascarado (****), está OK
- Se estiver vazio, adicione um valor

---

### PASSO 3: Verificar se DATABASE_URL também existe

Na mesma lista, verifique se tem:
- `DATABASE_URL`

**Se não existir:**
- Siga o guia anterior para conectar ao PostgreSQL

---

### PASSO 4: Redeploy Forçado

1. Vá em **"Deployments"**
2. Clique em **"Redeploy"** (ou **"Deploy"**)
3. Aguarde o deploy completar
4. Verifique os logs

**Importante:** Após adicionar/modificar variáveis, sempre faça redeploy!

---

## 🎯 VERIFICAÇÃO RÁPIDA

No serviço **Backend (Psipro)** → **Variables**, você deve ter:

- ✅ `JWT_SECRET` (com valor)
- ✅ `DATABASE_URL` (conectada ao Postgres)

**Se faltar alguma, adicione!**

---

## 💡 DICA

**As variáveis devem estar no MESMO serviço que está rodando o código!**

- Se o código está no serviço "Psipro" (Backend)
- As variáveis devem estar no serviço "Psipro" (Backend)
- Não adianta ter no PostgreSQL ou em outro lugar

---

## ✅ CHECKLIST

- [ ] JWT_SECRET existe no serviço Backend (Psipro)?
- [ ] JWT_SECRET tem um valor?
- [ ] DATABASE_URL existe no serviço Backend (Psipro)?
- [ ] Redeploy feito após adicionar variáveis?

---

**Vá no serviço Psipro (Backend) → Variables e verifique se JWT_SECRET está lá! 🚀**

