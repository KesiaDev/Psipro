# 🚀 Como Testar a Página Web - PsiPro

## ⚡ Início Rápido

### 1. Verificar se o servidor está rodando

O servidor Next.js deve estar rodando. Se não estiver, execute:

```bash
cd web
npm run dev
```

Acesse: **http://localhost:3001** (ou a porta que aparecer no terminal)

---

## 🔧 Configuração Inicial

### 1. Criar arquivo `.env.local`

Na pasta `web/`, crie o arquivo `.env.local`:

```bash
NEXT_PUBLIC_API_URL=http://localhost:3000
```

**Importante**: Reinicie o servidor após criar este arquivo.

### 2. Obter Token JWT

Você tem **3 opções**:

#### Opção A: Usar a Página de Teste (Recomendado)
1. Acesse: **http://localhost:3001/test**
2. Use as credenciais do seed:
   - Email: `owner@psiclinic.com`
   - Senha: `senha123`
3. Clique em "Fazer Login"
4. O token será salvo automaticamente

#### Opção B: Via Console do Navegador
1. Abra o console (F12)
2. Execute:
```javascript
// Primeiro, faça login via API
fetch('http://localhost:3000/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    email: 'owner@psiclinic.com',
    password: 'senha123'
  })
})
.then(r => r.json())
.then(data => {
  localStorage.setItem('psipro_token', data.access_token);
  console.log('Token salvo!');
});
```

#### Opção C: Token Manual
1. Obtenha um token JWT válido do backend
2. Cole na página `/test` na seção "Token Manual"
3. Clique em "Salvar Token"

---

## ✅ Testes Recomendados

### Teste 1: Página de Teste (`/test`)
1. Acesse **http://localhost:3001/test**
2. Verifique o status:
   - ✅ Token JWT: Presente
   - ✅ API URL: Configurada
   - ✅ Clínicas: Carregadas
3. Clique em "Testar Conexão com API"
4. Deve aparecer: "Conexão OK! X clínica(s) encontrada(s)"

### Teste 2: Página de Clínicas (`/clinica`)
1. Acesse **http://localhost:3001/clinica**
2. Verifique:
   - Lista de clínicas aparece (ou empty state se não houver)
   - Skeleton loader durante carregamento
   - Ao clicar em uma clínica, detalhes carregam
   - Lista de membros aparece
   - Badges de role aparecem

### Teste 3: ClinicSelector (Header)
1. Olhe no Header (canto superior direito)
2. Deve aparecer um seletor de clínicas
3. Clique para abrir dropdown
4. Selecione uma clínica
5. Recarregue a página
6. A clínica deve continuar selecionada (persistência)

### Teste 4: Convidar Usuário
1. Acesse `/clinica`
2. Clique em uma clínica onde você tem permissão `canManageUsers`
3. Clique em "Convidar Membro"
4. Digite um email válido
5. Clique em "Enviar Convite"
6. Deve aparecer toast de sucesso

---

## 🐛 Solução de Problemas

### Erro: "Failed to fetch"
**Causa**: Backend não está rodando
**Solução**: 
```bash
cd backend
npm run start:dev
```

### Erro: "401 Unauthorized"
**Causa**: Token inválido ou ausente
**Solução**: 
1. Acesse `/test`
2. Faça login novamente
3. Ou verifique: `localStorage.getItem('psipro_token')`

### Página não carrega
**Causa**: Erro de compilação
**Solução**: 
1. Verifique o terminal onde `npm run dev` está rodando
2. Procure por erros em vermelho
3. Verifique o console do navegador (F12)

### Token não persiste
**Causa**: localStorage bloqueado
**Solução**: 
1. Verifique se não está em modo privado/incógnito
2. Verifique permissões do navegador

---

## 📊 Checklist de Testes

- [ ] Servidor Next.js rodando
- [ ] Backend rodando na porta 3000
- [ ] Arquivo `.env.local` criado
- [ ] Token JWT salvo no localStorage
- [ ] Página `/test` carrega sem erros
- [ ] Teste de conexão retorna sucesso
- [ ] Página `/clinica` carrega clínicas
- [ ] ClinicSelector aparece no Header
- [ ] Toast notifications funcionam
- [ ] Loading states aparecem
- [ ] Erros são tratados adequadamente

---

## 🎯 Resultado Esperado

Após todos os testes:
- ✅ Todas as páginas carregam sem erros
- ✅ Dados são buscados da API real (não mocks)
- ✅ Token JWT é usado automaticamente
- ✅ Loading states funcionam
- ✅ Erros são tratados e mostrados
- ✅ Toasts aparecem para feedback
- ✅ Permissões são respeitadas na UI

---

## 📝 Credenciais do Seed

Use estas credenciais para testar:

| Email | Senha | Role |
|-------|-------|------|
| `owner@psiclinic.com` | `senha123` | Owner da Clínica |
| `psicologo2@psiclinic.com` | `senha123` | Psicólogo |
| `psicologo@psipro.com` | `senha123` | Independente |

---

## 🔗 Links Úteis

- **Página de Teste**: http://localhost:3001/test
- **Página de Clínicas**: http://localhost:3001/clinica
- **Dashboard**: http://localhost:3001/dashboard
- **Backend API**: http://localhost:3000

---

## 💡 Dica

Use a página `/test` como ponto de partida. Ela permite:
- Fazer login facilmente
- Verificar status da conexão
- Testar token manualmente
- Ver clínicas carregadas

**Boa sorte com os testes! 🚀**



