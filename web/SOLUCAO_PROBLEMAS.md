# 🔧 Solução de Problemas - PsiPro Web

## ❌ Erro "Not Found"

### Causas Possíveis:

1. **Backend não está rodando**
2. **URL da API incorreta**
3. **Endpoint não existe ou está em rota diferente**

---

## ✅ Soluções Passo a Passo

### 1. Verificar se o Backend está rodando

Abra um novo terminal e execute:

```bash
cd backend
npm run start:dev
```

**Verifique:**
- O servidor deve iniciar na porta **3000**
- Deve aparecer: `Nest application successfully started`
- Não deve haver erros em vermelho

### 2. Testar se o Backend está acessível

Abra o navegador e acesse:
```
http://localhost:3000
```

Ou teste o endpoint de health (se existir):
```
http://localhost:3000/auth/me
```

**Se der erro 401**, o backend está funcionando! (401 é esperado sem token)

**Se der "Cannot GET /"**, o backend está funcionando mas não tem rota raiz (normal)

**Se der "ERR_CONNECTION_REFUSED"**, o backend NÃO está rodando

### 3. Verificar arquivo .env.local

Na pasta `web/`, crie ou verifique o arquivo `.env.local`:

```bash
NEXT_PUBLIC_API_URL=http://localhost:3000
```

**Importante:**
- O arquivo deve estar na pasta `web/` (mesmo nível do `package.json`)
- Reinicie o servidor Next.js após criar/modificar

### 4. Verificar Porta do Backend

Se o backend estiver em outra porta (ex: 3001), atualize o `.env.local`:

```bash
NEXT_PUBLIC_API_URL=http://localhost:3001
```

### 5. Verificar CORS (se necessário)

Se o backend estiver em outra origem, verifique se CORS está configurado no backend.

No arquivo `backend/src/main.ts`, deve ter algo como:

```typescript
app.enableCors({
  origin: 'http://localhost:3001',
  credentials: true,
});
```

---

## 🧪 Teste Rápido

### Teste 1: Backend está rodando?

```bash
# No terminal
curl http://localhost:3000/auth/me
```

**Resposta esperada:**
- `401 Unauthorized` = ✅ Backend funcionando
- `ERR_CONNECTION_REFUSED` = ❌ Backend não está rodando

### Teste 2: Login funciona?

```bash
curl -X POST http://localhost:3000/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"owner@psiclinic.com","password":"senha123"}'
```

**Resposta esperada:**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": { ... }
}
```

---

## 🔍 Debug no Navegador

1. Abra o console (F12)
2. Vá na aba **Network**
3. Tente fazer login
4. Veja a requisição:
   - **URL**: Deve ser `http://localhost:3000/auth/login`
   - **Status**: 
     - `404` = Endpoint não existe
     - `0` ou `Failed` = Backend não está acessível
     - `401` = Credenciais inválidas (backend funcionando!)
     - `200` = ✅ Sucesso!

---

## 📝 Checklist de Verificação

- [ ] Backend está rodando (`npm run start:dev` no backend)
- [ ] Backend responde em `http://localhost:3000`
- [ ] Arquivo `.env.local` existe na pasta `web/`
- [ ] `NEXT_PUBLIC_API_URL=http://localhost:3000` no `.env.local`
- [ ] Servidor Next.js foi reiniciado após criar `.env.local`
- [ ] Porta do backend corresponde à URL configurada
- [ ] CORS está configurado (se necessário)

---

## 🚨 Erros Comuns

### "Failed to fetch" ou "NetworkError"
**Causa**: Backend não está rodando ou URL incorreta
**Solução**: Verifique se o backend está rodando e a URL no `.env.local`

### "404 Not Found"
**Causa**: Endpoint não existe ou rota incorreta
**Solução**: Verifique se o endpoint `/auth/login` existe no backend

### "401 Unauthorized" (no login)
**Causa**: Credenciais incorretas
**Solução**: Use `owner@psiclinic.com` / `senha123` (do seed)

### "CORS error"
**Causa**: CORS não configurado no backend
**Solução**: Configure CORS no `backend/src/main.ts`

---

## 💡 Dica

Use a página `/test` para diagnosticar:
1. Ela mostra o status do token
2. Tem botão para testar backend
3. Mostra mensagens de erro mais claras

**Acesse**: http://localhost:3001/test


