# ✅ SALVAR JWT_SECRET

## 🎯 O QUE FAZER AGORA

Vejo que você está adicionando `JWT_SECRET` com o valor:
```
minha_chave_super_secreta_123456789_abcdefghijklmnop
```

---

## 🚀 PASSO A PASSO

### 1️⃣ SALVAR JWT_SECRET

1. Vejo que há um botão **"Add"** (roxo) ao lado do campo
2. Clique em **"Add"** para salvar a variável
3. A variável deve aparecer na lista abaixo

---

### 2️⃣ VERIFICAR SE SALVOU

Após clicar em "Add", você deve ver:
- ✅ `JWT_SECRET` na lista de variáveis
- ✅ Valor mascarado (****) ao lado
- ✅ Ícone de três pontinhos (...) para editar

---

### 3️⃣ VERIFICAR DATABASE_URL

Na lista, verifique se também tem:
- ✅ `DATABASE_URL` (já está na lista, mascarado)

---

### 4️⃣ FAZER REDEPLOY

1. Após salvar `JWT_SECRET`, vá em **"Deployments"** (aba no topo)
2. Clique em **"Redeploy"** ou **"Deploy"**
3. Aguarde o deploy (2-5 minutos)
4. Verifique os logs

---

## ✅ CHECKLIST

- [ ] JWT_SECRET salvo (clicou em "Add")
- [ ] JWT_SECRET aparece na lista
- [ ] DATABASE_URL também está na lista
- [ ] Redeploy feito
- [ ] Logs verificados

---

## 🎯 RESULTADO ESPERADO

Após salvar e fazer redeploy, os logs devem mostrar:
- ✅ Módulos carregando
- ✅ Servidor iniciando
- ✅ "PsiPro API running on..."
- ❌ Sem erros de "JwtStrategy requires a secret"

---

**Clique em "Add" para salvar o JWT_SECRET e depois faça redeploy! 🚀**

