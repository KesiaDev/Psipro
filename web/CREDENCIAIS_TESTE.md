# 🔐 Credenciais de Teste - PsiPro

## 📝 Usuários do Seed (Backend)

O backend tem um seed com usuários de teste pré-cadastrados. Use estas credenciais:

| Email | Senha | Role | Descrição |
|-------|-------|------|-----------|
| `owner@psiclinic.com` | `senha123` | Owner da Clínica | Proprietário da clínica |
| `psicologo2@psiclinic.com` | `senha123` | Psicólogo | Psicólogo membro da clínica |
| `psicologo@psipro.com` | `senha123` | Independente | Psicólogo independente (sem clínica) |

---

## 🚀 Como Usar

### Opção 1: Página de Login (`/login`)

1. Acesse `http://localhost:3000/login`
2. Use qualquer uma das credenciais acima
3. Clique em "Entrar"
4. Você será redirecionado para `/dashboard`

### Opção 2: Página de Teste (`/test`) - RECOMENDADO

1. Acesse `http://localhost:3000/test`
2. As credenciais já vêm pré-preenchidas:
   - Email: `owner@psiclinic.com`
   - Senha: `senha123`
3. Clique em "Fazer Login"
4. O token será salvo automaticamente
5. Você pode testar a conexão com a API

### Opção 3: Criar Novo Usuário (`/register`)

1. Acesse `http://localhost:3000/register`
2. Preencha:
   - Nome completo
   - Email (qualquer email válido)
   - Senha (mínimo 6 caracteres)
   - Confirmar senha
3. Clique em "Criar conta"
4. Você será redirecionado para `/dashboard`

---

## ⚠️ Pré-requisitos

Para que o login funcione, você precisa:

1. **Backend rodando**
   - Backend deve estar em `http://localhost:3001` (ou porta configurada)
   - Se o backend estiver em outra porta, ajuste `NEXT_PUBLIC_API_URL` no `.env.local`

2. **Banco de dados com seed executado**
   - O seed do backend cria os usuários de teste
   - Se os usuários não existirem, você pode criar um novo via `/register`

3. **Variável de ambiente configurada**
   - Crie `.env.local` na pasta `web/`:
   ```env
   NEXT_PUBLIC_API_URL=http://localhost:3001/api
   ```

---

## 🧪 Qual Usuário Usar?

- **`owner@psiclinic.com`**: Use se quiser testar funcionalidades de proprietário de clínica
- **`psicologo2@psiclinic.com`**: Use para testar como psicólogo membro
- **`psicologo@psipro.com`**: Use para testar modo independente (sem clínica)
- **Novo usuário via `/register`**: Use para testar desde o início (sem dados pré-carregados)

---

## 💡 Dica

A página `/test` é a melhor opção para começar, pois:
- ✅ Credenciais já pré-preenchidas
- ✅ Mostra status da conexão com API
- ✅ Permite testar token manualmente
- ✅ Mostra se as clínicas foram carregadas

---

**Nota**: Essas credenciais são apenas para desenvolvimento/teste. Em produção, os usuários precisarão se registrar normalmente.
