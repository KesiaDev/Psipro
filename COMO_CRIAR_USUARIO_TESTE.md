# 🔐 Como Criar Usuário de Teste no Firebase

## Problema
O erro `FirebaseAuthInvalidCredentialsException` geralmente significa que:
- O email/senha não existe no Firebase
- As credenciais estão incorretas
- A conta foi desativada

## Solução: Criar Usuário de Teste

### Opção 1: Criar pelo Firebase Console (Recomendado)

1. **Acesse o Firebase Console:**
   - https://console.firebase.google.com/project/psipro-6237d/authentication/users

2. **Clique em "Adicionar usuário"** (botão azul no topo)

3. **Preencha os dados:**
   - **E-mail:** Digite um email válido (ex: `teste@psipro.com`)
   - **Senha:** Digite uma senha (mínimo 6 caracteres)
   - **Desabilitar e-mail:** Deixe desmarcado

4. **Clique em "Adicionar usuário"**

5. **Teste no app:**
   - Use o email e senha que você criou
   - O login deve funcionar

### Opção 2: Criar pelo App (Criar Conta)

1. **No app, clique em "CRIAR CONTA"**

2. **Preencha:**
   - Nome completo
   - E-mail válido
   - Senha (mínimo 6 caracteres)
   - Confirmar senha

3. **Clique em "CRIAR CONTA"**

4. **Se der erro, verifique:**
   - E-mail já existe? → Tente outro email
   - Senha muito fraca? → Use pelo menos 6 caracteres
   - E-mail inválido? → Use formato correto (ex: teste@email.com)

### Opção 3: Verificar Usuário Existente

1. **Firebase Console → Authentication → Users**
2. **Procure pelo email que você está tentando usar**
3. **Se não existir, crie um novo**
4. **Se existir, verifique:**
   - Status: Deve estar "Ativado"
   - Se estiver desativado, clique nos três pontos → "Reativar usuário"

## Verificar Métodos de Login Habilitados

1. **Firebase Console → Authentication → Método de login**
2. **Verifique se estão ativados:**
   - ✅ **E-mail/senha** → Deve estar "ativado"
   - ✅ **Google** → Deve estar "ativado"

## Testar Login

### Teste 1: Login com Email/Senha
```
Email: teste@psipro.com
Senha: senha123
```

### Teste 2: Criar Nova Conta
- Use um email que você tenha acesso
- Use uma senha forte (mínimo 6 caracteres)
- Verifique se recebeu confirmação

### Teste 3: Recuperar Senha
- Se esqueceu a senha, use "Esqueci minha senha"
- Verifique o email de recuperação

## Erros Comuns

### "The supplied auth credential is incorrect"
- **Causa:** Email ou senha incorretos
- **Solução:** Verifique as credenciais ou crie uma nova conta

### "ERROR_USER_NOT_FOUND"
- **Causa:** Conta não existe
- **Solução:** Crie a conta primeiro (pelo app ou Firebase Console)

### "ERROR_WRONG_PASSWORD"
- **Causa:** Senha incorreta
- **Solução:** Use "Esqueci minha senha" ou verifique a senha

### "ERROR_EMAIL_ALREADY_IN_USE"
- **Causa:** Email já cadastrado
- **Solução:** Use outro email ou faça login com o existente

## Dica Importante

**Sempre crie o primeiro usuário pelo Firebase Console** para garantir que está funcionando. Depois você pode criar outros pelo app.

