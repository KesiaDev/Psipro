# 🧪 Guia: Testar Todos os Usuários de Teste

Este guia mostra como testar cada usuário do seed e o que esperar de cada um.

---

## 📋 Checklist Rápido

- [ ] Testar `owner@psiclinic.com` (Owner da Clínica)
- [ ] Testar `psicologo2@psiclinic.com` (Psicólogo)
- [ ] Testar `psicologo@psipro.com` (Independente)
- [ ] Testar criação de novo usuário via `/register`

---

## 1️⃣ Owner da Clínica (`owner@psiclinic.com`)

### Credenciais
- **Email**: `owner@psiclinic.com`
- **Senha**: `senha123`
- **Role**: Owner/Proprietário da Clínica

### O que esperar:

✅ **Dashboard**:
- Deve mostrar dados da clínica
- Métricas de pacientes, sessões, receita
- Insights gerados

✅ **Página de Clínicas** (`/clinica`):
- Deve listar clínicas (ou mostrar a clínica do usuário)
- Pode gerenciar membros da clínica
- Badge "Owner" ou "Proprietário" visível

✅ **Pacientes** (`/pacientes`):
- Lista de pacientes da clínica
- Pode importar/criar pacientes

✅ **Agenda** (`/agenda`):
- Consultas agendadas
- Visualização de calendário

✅ **Financeiro** (`/financeiro`):
- Dados financeiros da clínica

### Como testar:

1. Acesse `http://localhost:3000/login`
2. Use: `owner@psiclinic.com` / `senha123`
3. Clique em "Entrar"
4. Navegue pelas páginas acima
5. Verifique se o badge de role aparece no Header
6. Teste funcionalidades de gerenciamento (se disponíveis)

---

## 2️⃣ Psicólogo Membró (`psicologo2@psiclinic.com`)

### Credenciais
- **Email**: `psicologo2@psiclinic.com`
- **Senha**: `senha123`
- **Role**: Psicólogo (membro da clínica)

### O que esperar:

✅ **Dashboard**:
- Dados da clínica (mas com permissões limitadas)
- Métricas relacionadas ao usuário

✅ **Página de Clínicas** (`/clinica`):
- Pode ver a clínica
- **NÃO pode** gerenciar membros (sem permissão de admin)
- Badge "Psicólogo" ou similar

✅ **Pacientes** (`/pacientes`):
- Lista de pacientes da clínica
- Pode visualizar pacientes compartilhados

✅ **Agenda** (`/agenda`):
- Suas próprias consultas
- Consultas da clínica (se tiver permissão)

✅ **Financeiro** (`/financeiro`):
- Dados financeiros (limitados às suas sessões, se aplicável)

### Como testar:

1. **Primeiro, faça logout** do usuário anterior (se estiver logado)
2. Acesse `http://localhost:3000/login`
3. Use: `psicologo2@psiclinic.com` / `senha123`
4. Clique em "Entrar"
5. Compare as diferenças com o Owner:
   - Menos permissões
   - Badge diferente
   - Funcionalidades de gerenciamento podem estar desabilitadas

---

## 3️⃣ Psicólogo Independente (`psicologo@psipro.com`)

### Credenciais
- **Email**: `psicologo@psipro.com`
- **Senha**: `senha123`
- **Role**: Independente (sem clínica)

### O que esperar:

✅ **Dashboard**:
- Modo independente ativo
- Métricas pessoais (sem clínica)
- Pode mostrar "Modo Independente" no header

✅ **Página de Clínicas** (`/clinica`):
- Pode não ter clínicas associadas
- Pode criar/gerenciar sua própria clínica (se permitido)

✅ **Pacientes** (`/pacientes`):
- Seus próprios pacientes (não compartilhados)

✅ **Agenda** (`/agenda`):
- Suas próprias consultas

✅ **Financeiro** (`/financeiro`):
- Dados financeiros pessoais

### Como testar:

1. **Faça logout** do usuário anterior
2. Acesse `http://localhost:3000/login`
3. Use: `psicologo@psipro.com` / `senha123`
4. Clique em "Entrar"
5. Verifique:
   - "Modo Independente" no header
   - Dados não compartilhados com clínicas
   - Experiência mais simplificada

---

## 4️⃣ Novo Usuário (Registro)

### Criar novo usuário

### O que esperar:

✅ **Primeiro acesso**:
- Onboarding modal deve aparecer
- Dashboard vazio (sem dados)

✅ **Dashboard**:
- Empty states (dados vazios)
- Insights padrão ("Começando a usar o PsiPro")
- Mensagens orientativas

✅ **Pacientes** (`/pacientes`):
- Lista vazia
- Botão para importar/criar primeiro paciente

✅ **Agenda** (`/agenda`):
- Sem consultas agendadas
- Empty states

✅ **Financeiro** (`/financeiro`):
- Dados zerados
- Empty states

### Como testar:

1. **Faça logout** do usuário anterior
2. Acesse `http://localhost:3000/register`
3. Preencha:
   - Nome completo: `Teste Usuario`
   - Email: `teste@example.com` (ou qualquer email único)
   - Senha: `senha123`
   - Confirmar senha: `senha123`
4. Clique em "Criar conta"
5. Deve redirecionar para `/dashboard`
6. **Onboarding modal deve aparecer automaticamente**
7. Navegue pelas páginas e veja os empty states

---

## 🔄 Fluxo Completo de Teste (Recomendado)

### Ordem sugerida:

1. **Comece com Owner** (`owner@psiclinic.com`)
   - Teste todas as funcionalidades
   - Veja dados completos
   - Entenda a experiência completa

2. **Teste Psicólogo Membró** (`psicologo2@psiclinic.com`)
   - Compare com Owner
   - Veja diferenças de permissões
   - Teste limitações de acesso

3. **Teste Independente** (`psicologo@psipro.com`)
   - Veja experiência simplificada
   - Teste modo independente
   - Compare com membros de clínica

4. **Crie novo usuário** (`/register`)
   - Teste fluxo de registro
   - Veja onboarding
   - Teste empty states
   - Veja experiência de primeiro acesso

---

## ✅ Checklist de Testes por Usuário

### Para cada usuário, verifique:

- [ ] Login funciona
- [ ] Redireciona para `/dashboard` após login
- [ ] Header mostra nome/avatar corretos
- [ ] Badge de role aparece (se aplicável)
- [ ] Dashboard carrega (com ou sem dados)
- [ ] Navegação funciona (Sidebar)
- [ ] Logout funciona
- [ ] Após logout, rotas protegidas redirecionam para `/login`

### Páginas a testar (para cada usuário):

- [ ] `/dashboard` - Dashboard principal
- [ ] `/pacientes` - Lista de pacientes
- [ ] `/agenda` - Agenda
- [ ] `/financeiro` - Financeiro
- [ ] `/clinica` - Gestão de clínica
- [ ] Header - Avatar, logout, seletor de clínica
- [ ] Sidebar - Navegação

---

## 🎯 Dicas de Teste

### Use a página `/test`

A página `/test` é útil para:
- Fazer login rapidamente (credenciais pré-preenchidas)
- Ver status do token
- Testar conexão com API
- Ver clínicas carregadas

**Acesse**: `http://localhost:3000/test`

### Use DevTools (F12)

- **Console**: Veja erros e logs
- **Network**: Veja requisições à API
- **Application > Local Storage**: Veja token salvo (`psipro_token`)

### Limpar dados entre testes

Para testar clean state:

1. Abra DevTools (F12)
2. Vá em **Application > Local Storage**
3. Delete `psipro_token` e `psipro_user`
4. Recarregue a página
5. Faça login com próximo usuário

---

## 🐛 Problemas Comuns

### "Credenciais inválidas"
- Verifique se o backend está rodando
- Verifique se o seed foi executado
- Verifique se as credenciais estão corretas

### "Token não encontrado"
- Verifique se o backend está acessível
- Verifique `NEXT_PUBLIC_API_URL` no `.env.local`
- Veja erros no console (F12)

### Dados não aparecem
- Normal para usuário novo (empty states)
- Para seed users, verifique se o seed criou dados
- Veja erros no console/Network tab

---

## 📝 Notas

- **Seed do backend**: Os usuários de teste são criados pelo seed do backend
- **Dados compartilhados**: Psicólogos da mesma clínica veem dados compartilhados
- **Modo independente**: Psicólogo independente não vê dados de clínicas
- **Onboarding**: Só aparece no primeiro acesso (localStorage)

---

**Boa sorte com os testes! 🚀**
