# 🔧 Como Resolver Erro de Conexão no Login

## ⚠️ Erro: "Erro de conexão. Verifique sua internet."

Este erro aparece quando o frontend **não consegue se conectar ao backend**.

---

## 🔍 Causas Possíveis

### 1. Backend não está rodando ❌

**Mais comum!** O backend precisa estar rodando para o login funcionar.

### 2. URL da API incorreta

O arquivo `.env.local` pode estar com URL errada ou não existe.

### 3. Backend em porta diferente

Backend pode estar em outra porta que não a configurada.

---

## ✅ Soluções (Siga nesta ordem)

### Solução 1: Verificar se o Backend está rodando

**No backend**, você precisa ter um terminal com:

```bash
cd backend
npm run start:dev
# ou
npm run start
```

**Você deve ver algo como:**
```
[NestApplication] Nest application successfully started on port 3001
```

**Se o backend NÃO estiver rodando:**
1. Abra um novo terminal
2. Vá para a pasta `backend`
3. Execute `npm run start:dev`
4. Aguarde até ver "successfully started"

---

### Solução 2: Verificar/Criar `.env.local`

O arquivo `.env.local` deve estar na pasta `web/` com:

```env
NEXT_PUBLIC_API_URL=http://localhost:3001/api
```

**⚠️ IMPORTANTE:**
- Use a porta que o backend está usando (3001 ou 3000)
- Deve terminar com `/api`
- Reinicie o servidor Next.js após criar/modificar

**Como criar:**
1. Na pasta `web/`, crie arquivo `.env.local`
2. Cole: `NEXT_PUBLIC_API_URL=http://localhost:3001/api`
3. Salve o arquivo
4. **Reinicie o servidor Next.js** (Ctrl+C e `npm run dev` novamente)

---

### Solução 3: Verificar porta do Backend

O backend pode estar em portas diferentes:

- **Porta 3000**: `http://localhost:3000/api`
- **Porta 3001**: `http://localhost:3001/api`

**Como descobrir:**
1. Veja no terminal do backend em qual porta iniciou
2. Procure por: "started on port XXXX"
3. Use essa porta no `.env.local`

---

### Solução 4: Testar conexão com Backend

**Via navegador:**
1. Abra: `http://localhost:3001` (ou porta do backend)
2. Se mostrar algo (mesmo erro 404), backend está rodando ✅
3. Se der "Não é possível acessar este site", backend NÃO está rodando ❌

**Via terminal:**
```powershell
# Testar se backend responde
curl http://localhost:3001
# ou
Invoke-WebRequest http://localhost:3001
```

---

## 🎯 Checklist Rápido

- [ ] Backend está rodando? (veja terminal do backend)
- [ ] Arquivo `.env.local` existe na pasta `web/`?
- [ ] `.env.local` tem `NEXT_PUBLIC_API_URL` configurado?
- [ ] Porta no `.env.local` corresponde à porta do backend?
- [ ] Servidor Next.js foi reiniciado após criar `.env.local`?

---

## 🔧 Passo a Passo Completo

### Passo 1: Iniciar Backend

```bash
# Terminal 1 - Backend
cd C:\Users\User\Desktop\psipro\backend
npm run start:dev
```

Aguarde até ver: `Nest application successfully started on port XXXX`

**Anote a porta!** (geralmente 3001)

---

### Passo 2: Criar/Verificar `.env.local`

```bash
# Terminal 2 - Web
cd C:\Users\User\Desktop\psipro\web
```

Crie o arquivo `.env.local` com:
```env
NEXT_PUBLIC_API_URL=http://localhost:3001/api
```

**⚠️ Use a porta que o backend está usando!**

---

### Passo 3: Reiniciar Servidor Next.js

```bash
# Se o servidor estiver rodando, pare (Ctrl+C)
# Depois inicie novamente:
npm run dev
```

---

### Passo 4: Testar Login

1. Acesse `http://localhost:3000/login`
2. Use: `psicologo2@psiclinic.com` / `senha123`
3. Clique em "Entrar"

**Deve funcionar! ✅**

---

## 🐛 Problemas Comuns

### Erro persiste mesmo com backend rodando

**Solução:**
1. Verifique a porta no terminal do backend
2. Confirme que `.env.local` usa a mesma porta
3. Reinicie o servidor Next.js
4. Limpe cache do navegador (Ctrl+Shift+Del)

### Backend não inicia

**Possíveis causas:**
- Porta já em uso
- Erro no código do backend
- Banco de dados não conectado
- Variáveis de ambiente faltando

**Solução:**
- Veja erros no terminal do backend
- Verifique se banco de dados está rodando
- Verifique variáveis de ambiente do backend

### Erro CORS

Se aparecer erro de CORS no console:
- Backend precisa permitir requisições do frontend
- Verifique `CORS_ORIGIN` no backend

---

## 💡 Dica

**Use a página `/test` para diagnosticar:**

1. Acesse `http://localhost:3000/test`
2. Clique em "Testar Conexão com API"
3. Veja a mensagem de erro (mais detalhada)

---

## ✅ Quando Funciona

Quando tudo estiver certo, você verá:
- ✅ Login funciona
- ✅ Redireciona para `/dashboard`
- ✅ Dados carregam
- ✅ Sem erros no console

---

**Se ainda não funcionar, me mostre:**
1. O que aparece no terminal do backend
2. O conteúdo do `.env.local`
3. Erros no console do navegador (F12)
