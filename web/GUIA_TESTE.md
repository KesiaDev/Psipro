# 🧪 Guia de Teste - PsiPro Web

## 📋 Pré-requisitos

1. **Backend rodando** na porta 3000 (ou configurar `NEXT_PUBLIC_API_URL`)
2. **Token JWT** válido no localStorage
3. **Navegador** atualizado (Chrome, Firefox, Edge)

---

## 🚀 Como Testar

### 1. Iniciar o Servidor de Desenvolvimento

```bash
cd web
npm run dev
```

O servidor iniciará em `http://localhost:3001` (ou próxima porta disponível).

### 2. Configurar Variável de Ambiente

Crie um arquivo `.env.local` na pasta `web/`:

```bash
NEXT_PUBLIC_API_URL=http://localhost:3000
```

**Importante**: Reinicie o servidor após criar/modificar o `.env.local`.

### 3. Configurar Token JWT (Temporário para Teste)

Abra o console do navegador (F12) e execute:

```javascript
// Substitua pelo token real do seu backend
localStorage.setItem('psipro_token', 'seu-token-jwt-aqui');
```

**Para obter um token válido:**
- Faça login no backend via API
- Ou use o endpoint de autenticação do backend

---

## ✅ Checklist de Testes

### Página Principal (/)
- [ ] Página carrega sem erros
- [ ] Tema claro/escuro funciona
- [ ] Header e Sidebar renderizam corretamente

### Página de Clínicas (/clinica)
- [ ] Lista de clínicas carrega (se houver)
- [ ] Empty state aparece se não houver clínicas
- [ ] Skeleton loader aparece durante carregamento
- [ ] Ao clicar em uma clínica, detalhes são carregados
- [ ] Lista de membros aparece corretamente
- [ ] Badges de role aparecem (Proprietário, Admin, etc)
- [ ] Botão "Convidar Membro" aparece apenas se `canManageUsers === true`
- [ ] Modal de convite abre e fecha corretamente
- [ ] Toast de sucesso/erro aparece ao convidar

### ClinicSelector (Header)
- [ ] Seletor aparece no Header
- [ ] Dropdown abre e fecha
- [ ] Lista de clínicas aparece no dropdown
- [ ] Ao selecionar clínica, ela é salva no localStorage
- [ ] Badge de role aparece no dropdown
- [ ] "Modo Independente" aparece se não houver clínica selecionada

### Toast Notifications
- [ ] Toasts aparecem no canto superior direito
- [ ] Toasts desaparecem automaticamente após 3 segundos
- [ ] Toasts podem ser fechados manualmente
- [ ] Cores corretas (verde=sucesso, vermelho=erro, etc)

### Tratamento de Erros
- [ ] Erro 401 limpa token e mostra mensagem
- [ ] Erro 403 mostra mensagem de permissão negada
- [ ] Erro de rede mostra mensagem apropriada
- [ ] Erros aparecem como toast

---

## 🐛 Problemas Comuns

### Erro: "Failed to fetch" ou "Network Error"
**Causa**: Backend não está rodando ou URL incorreta
**Solução**: 
1. Verifique se o backend está rodando na porta 3000
2. Verifique o arquivo `.env.local` com `NEXT_PUBLIC_API_URL`

### Erro: "401 Unauthorized"
**Causa**: Token JWT inválido ou ausente
**Solução**: 
1. Verifique se o token está no localStorage: `localStorage.getItem('psipro_token')`
2. Obtenha um token válido fazendo login no backend

### Erro: "Cannot read property 'map' of undefined"
**Causa**: Dados não foram carregados ainda
**Solução**: Verifique se há estados de loading adequados

### Página não carrega
**Causa**: Erro de compilação
**Solução**: 
1. Verifique o console do terminal onde o `npm run dev` está rodando
2. Verifique erros no console do navegador (F12)

---

## 🔍 Verificações no Console

Abra o console do navegador (F12) e verifique:

1. **Sem erros vermelhos** no console
2. **Requisições HTTP** aparecem na aba Network:
   - `GET /clinics` - deve retornar 200
   - `GET /clinics/:id` - deve retornar 200
   - `POST /clinics/:id/invite` - deve retornar 201 ou 200

3. **Token presente**:
   ```javascript
   localStorage.getItem('psipro_token') // não deve ser null
   ```

---

## 📝 Testes Manuais Recomendados

### Teste 1: Listar Clínicas
1. Acesse `/clinica`
2. Verifique se a lista de clínicas aparece
3. Se vazio, verifique se o empty state aparece

### Teste 2: Ver Detalhes da Clínica
1. Clique em uma clínica na lista
2. Verifique se os detalhes carregam
3. Verifique se a lista de membros aparece

### Teste 3: Convidar Usuário
1. Selecione uma clínica onde você tem `canManageUsers`
2. Clique em "Convidar Membro"
3. Digite um email válido
4. Clique em "Enviar Convite"
5. Verifique se o toast de sucesso aparece

### Teste 4: Selecionar Clínica
1. Use o ClinicSelector no Header
2. Selecione uma clínica
3. Recarregue a página
4. Verifique se a clínica ainda está selecionada (persistência)

---

## 🎯 Resultado Esperado

Após todos os testes:
- ✅ Página carrega sem erros
- ✅ Dados são buscados da API real
- ✅ Loading states funcionam
- ✅ Erros são tratados adequadamente
- ✅ Toasts aparecem para feedback
- ✅ Permissões são respeitadas na UI
- ✅ Nenhum mock ou dado hardcoded

---

## 📞 Suporte

Se encontrar problemas:
1. Verifique os logs do terminal (`npm run dev`)
2. Verifique o console do navegador (F12)
3. Verifique a aba Network para requisições HTTP
4. Verifique se o backend está respondendo corretamente



