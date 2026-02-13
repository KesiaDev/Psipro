# 🧪 Como Testar a Aplicação Web Localmente

## 🚀 PASSO 1: Iniciar o Servidor

### Opção A: Script PowerShell (Recomendado)

Abra o PowerShell na pasta `web` e execute:

```powershell
.\iniciar-servidor.ps1
```

### Opção B: Comando Manual

```powershell
npm run dev
```

---

## ✅ PASSO 2: Verificar se Funcionou

Você deve ver no terminal:

```
✓ Ready in X seconds
- Local:        http://localhost:3000
```

**Porta padrão**: `http://localhost:3000`

Se a porta 3000 estiver ocupada, o Next.js usará a 3001 automaticamente.

---

## 🌐 PASSO 3: Acessar no Navegador

Abra seu navegador e acesse:

```
http://localhost:3000
```

---

## 📋 O QUE TESTAR

### 1. 🏠 **Landing Page** (Página Inicial)

**URL**: `http://localhost:3000/`

**O que verificar**:
- ✅ Página carrega sem erros
- ✅ Seções aparecem corretamente (Hero, Problema, Solução, etc.)
- ✅ Botão "Solicitar Acesso Beta" abre o modal
- ✅ Formulário de beta access funciona

---

### 2. 🔐 **Página de Login**

**URL**: `http://localhost:3000/login`

**O que verificar**:
- ✅ Formulário aparece
- ✅ Campos de email e senha funcionam
- ✅ Botão "Entrar" está visível
- ⚠️ **Login real**: Só funcionará se o backend estiver rodando

**Para testar login completo**:
1. Backend precisa estar rodando em `http://localhost:3001`
2. Usuário precisa existir no banco de dados
3. Ou criar um usuário via `/register`

---

### 3. 📝 **Página de Registro**

**URL**: `http://localhost:3000/register`

**O que verificar**:
- ✅ Formulário aparece
- ✅ Campos: Nome, Email, Senha, Confirmar Senha
- ✅ Validação básica funciona
- ⚠️ **Registro real**: Só funcionará com backend rodando

---

### 4. 🏠 **Dashboard** (Após Login)

**URL**: `http://localhost:3000/dashboard`

**O que verificar**:
- ✅ Se não estiver logado, redireciona para `/login`
- ✅ Se estiver logado, mostra o dashboard
- ✅ Métricas aparecem (mesmo que com zeros)
- ✅ Insights aparecem (pelo menos o insight padrão)
- ✅ Onboarding modal abre no primeiro acesso

**Para testar sem backend**:
- A página carrega, mas mostra dados vazios (mockados)
- Onboarding funciona normalmente

---

### 5. 👥 **Página de Pacientes**

**URL**: `http://localhost:3000/pacientes`

**O que verificar**:
- ✅ Página carrega
- ✅ Lista vazia aparece (sem pacientes ainda)
- ✅ Botão "Importar Pacientes" funciona
- ✅ Modal de importação abre

---

### 6. 📅 **Página de Agenda**

**URL**: `http://localhost:3000/agenda`

**O que verificar**:
- ✅ Página carrega
- ✅ Calendário aparece
- ✅ Visualização mensal/semanal/diária (se implementado)

---

### 7. 💰 **Página Financeiro**

**URL**: `http://localhost:3000/financeiro`

**O que verificar**:
- ✅ Página carrega
- ✅ Cards de métricas aparecem (dados mockados)
- ✅ Gráficos aparecem

---

### 8. 🏥 **Página de Clínicas**

**URL**: `http://localhost:3000/clinica`

**O que verificar**:
- ✅ Página carrega
- ✅ Seletor de clínica aparece
- ✅ Lista de clínicas (se houver)

---

## 🔧 TESTAR COM BACKEND REAL

### Pré-requisitos:

1. **Backend rodando** em `http://localhost:3001`
2. **Banco de dados configurado**
3. **Variável de ambiente** (opcional):

Crie um arquivo `.env.local` na pasta `web`:

```env
NEXT_PUBLIC_API_URL=http://localhost:3001/api
```

### Fluxo de Teste Completo:

1. **Registrar um usuário**:
   - Acesse `/register`
   - Preencha: Nome, Email, Senha
   - Clique em "Criar Conta"
   - Deve redirecionar para `/dashboard`

2. **Fazer login**:
   - Acesse `/login`
   - Use email e senha criados
   - Deve redirecionar para `/dashboard`

3. **Testar dashboard com dados reais**:
   - Dashboard deve carregar dados do backend
   - Métricas devem aparecer (se houver dados)

---

## ⚠️ PROBLEMAS COMUNS

### Erro: "Unable to acquire lock"

**Solução**: Execute o script `iniciar-servidor.ps1` que mata processos Node anteriores.

### Erro: "Port 3000 is already in use"

**Solução**: O Next.js automaticamente usa a porta 3001. Acesse `http://localhost:3001`

### Página não carrega / Erro 500

**Solução**: 
1. Pare o servidor (Ctrl+C)
2. Execute `.\iniciar-servidor.ps1` novamente
3. Verifique o console do terminal para erros

### Login não funciona

**Verifique**:
- Backend está rodando?
- URL da API está correta?
- Usuário existe no banco?
- Token está sendo salvo? (F12 → Application → Local Storage → `psipro_token`)

---

## 🎯 CHECKLIST RÁPIDO DE TESTE

- [ ] Landing page carrega
- [ ] Página de login carrega
- [ ] Página de registro carrega
- [ ] Dashboard carrega (após login)
- [ ] Onboarding aparece no primeiro acesso
- [ ] Navegação entre páginas funciona
- [ ] Header e Sidebar aparecem nas páginas autenticadas
- [ ] Logout funciona (botão no header)

---

## 📝 NOTAS IMPORTANTES

1. **Dados Mockados**: A maioria das páginas ainda usa dados mockados. Isso é normal! O backend real só é necessário para autenticação e dados reais.

2. **Autenticação**: Para testar login/registro real, o backend DEVE estar rodando. Sem backend, você verá erros de conexão.

3. **Onboarding**: Funciona completamente sem backend. É puramente frontend.

4. **Beta Access**: O formulário de beta access é apenas frontend. Não envia dados reais ainda (backend não implementado).

---

**Última atualização**: 30/12/2025
