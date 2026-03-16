# 🔍 Diagnóstico de Problemas de Login

## Checklist de Verificação

### 1. Verificar Logcat
Abra o Logcat no Android Studio e filtre por:
- `MainActivity`
- `FirebaseAuth`
- `GoogleSignIn`
- `FirebaseAuthErrorHelper`

### 2. Verificar Firebase Console
1. Acesse: https://console.firebase.google.com/project/psipro-6237d/authentication/users
2. Verifique se há usuários cadastrados
3. Verifique se os métodos de login estão habilitados:
   - Email/Senha: ✅ Ativado
   - Google: ✅ Ativado

### 3. Verificar google-services.json
- Arquivo deve estar em: `app/google-services.json`
- Package name deve ser: `com.psipro.app`
- SHA-1 deve estar configurado no Firebase Console

### 4. Verificar SHA-1
Execute no terminal:
```powershell
./gradlew signingReport
```

Procure por:
```
SHA1: 8A:21:33:E7:8F:F1:55:03:3B:19:FD:CC:B3:13:6D:6A:0B:E9:97:3B
```

Este SHA-1 deve estar no Firebase Console.

### 5. Testar Conexão
- Verifique se o dispositivo/emulador tem internet
- Teste abrir o Firebase Console no navegador

### 6. Limpar Cache do App
1. Configurações → Apps → Psipro
2. Armazenamento → Limpar cache
3. Desinstalar e reinstalar o app

### 7. Verificar Erros Comuns

#### Erro: "The supplied auth credential is incorrect"
- **Causa**: Email ou senha incorretos
- **Solução**: Verifique as credenciais no Firebase Console

#### Erro: "ERROR_INVALID_CREDENTIAL"
- **Causa**: Credenciais inválidas ou conta não existe
- **Solução**: Crie uma nova conta ou recupere a senha

#### Erro: "ERROR_NETWORK_REQUEST_FAILED"
- **Causa**: Problema de conexão
- **Solução**: Verifique a internet

#### Erro: "ERROR_INTERNAL_ERROR"
- **Causa**: Erro interno do Firebase
- **Solução**: Aguarde alguns minutos e tente novamente

### 8. Criar Usuário de Teste no Firebase Console
1. Firebase Console → Authentication → Users
2. Clique em "Adicionar usuário"
3. Digite email e senha
4. Tente fazer login no app com essas credenciais

### 9. Verificar Logs Específicos
Procure no Logcat por:
- `Firebase Auth instance:` - Deve mostrar uma instância válida
- `Current user:` - Deve ser null antes do login
- `Tentando login com:` - Deve mostrar o email
- `Login bem-sucedido:` - Deve aparecer após login bem-sucedido

### 10. Testar em Dispositivo Físico
Emuladores às vezes têm problemas com Firebase. Teste em um dispositivo físico.

## Comandos Úteis

### Limpar e Reconstruir
```powershell
./gradlew clean
./gradlew build
```

### Verificar SHA-1
```powershell
./gradlew signingReport
```

### Verificar se google-services.json está correto
```powershell
Get-Content app\google-services.json | Select-String "com.psipro.app"
```

