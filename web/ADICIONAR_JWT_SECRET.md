# ⚠️ ERRO: JwtStrategy requires a secret or key

## 🔍 PROBLEMA IDENTIFICADO

O erro mostra:
```
ERROR [ExceptionHandler] JwtStrategy requires a secret or key
```

Isso significa que:
- ✅ O backend está iniciando corretamente
- ❌ Falta a variável de ambiente `JWT_SECRET`

---

## ✅ SOLUÇÃO: Adicionar JWT_SECRET

### PASSO 1: Ir para Variables

1. No Railway, no serviço do Backend
2. No topo, clique na aba **"Variables"**

---

### PASSO 2: Adicionar JWT_SECRET

1. Clique em **"+ New Variable"** ou **"+ Add Variable"**
2. Em **Name**, digite: `JWT_SECRET`
3. Em **Value**, digite uma chave aleatória longa, por exemplo:
   ```
   sua_chave_super_secreta_aleatoria_muito_longa_aqui_123456789
   ```
   
   **Ou gere uma chave segura:**
   - Use um gerador online de chaves aleatórias
   - Ou use: `openssl rand -base64 32` (se tiver openssl)
   - Ou qualquer string longa e aleatória

4. Clique em **Salvar** ou **Add**

---

### PASSO 3: Verificar Outras Variáveis

Certifique-se de que também tem:

- `DATABASE_URL` (Railway cria automaticamente quando você adiciona PostgreSQL)
- `PORT=3001` (opcional, Railway define automaticamente)
- `NODE_ENV=production` (opcional, mas recomendado)

---

### PASSO 4: Redeploy

1. Após adicionar `JWT_SECRET`, o Railway vai redeployar automaticamente
2. Ou vá em **Deployments** → **Redeploy**
3. Aguarde o deploy
4. Verifique os logs

---

## ✅ CHECKLIST

- [ ] Variável `JWT_SECRET` adicionada
- [ ] Valor é uma string longa e aleatória
- [ ] Variável salva
- [ ] Redeploy feito (automático ou manual)
- [ ] Logs verificados

---

## 💡 DICA

**A chave JWT_SECRET deve ser:**
- Longa (pelo menos 32 caracteres)
- Aleatória (não use palavras comuns)
- Secreta (não compartilhe publicamente)

**Exemplos de chaves válidas:**
```
a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0
super_secret_key_123456789_abcdefghijklmnop
```

---

## 🎯 RESULTADO ESPERADO

Após adicionar `JWT_SECRET`, os logs devem mostrar:
- ✅ Módulos carregando
- ✅ Servidor iniciando
- ✅ "PsiPro API running on..."
- ✅ Sem erros de JWT

---

**Vá em Variables e adicione `JWT_SECRET`! 🚀**

