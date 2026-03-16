# 🔧 Como Configurar SHA-1 no Firebase para Login Google

## Problema
Se o login com Google não funciona após escolher o e-mail (não dá nenhuma reação), geralmente é porque o **SHA-1** do seu certificado de debug não está configurado no Firebase Console.

## Solução

### 1. Obter o SHA-1 do seu certificado

#### No Windows (PowerShell):
```powershell
cd android
./gradlew signingReport
```

Ou se estiver na raiz do projeto:
```powershell
cd app
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

#### No Android Studio:
1. Abra o terminal no Android Studio (aba inferior)
2. Execute:
   ```
   ./gradlew signingReport
   ```
3. Procure por "SHA1:" na saída

### 2. Adicionar SHA-1 no Firebase Console

1. Acesse: https://console.firebase.google.com/
2. Selecione seu projeto: **psipro-6237d**
3. Vá em **Configurações do Projeto** (ícone de engrenagem)
4. Role até **Seus apps** → Selecione o app Android
5. Clique em **Adicionar impressão digital**
6. Cole o SHA-1 que você obteve
7. Clique em **Salvar**

### 3. Baixar o novo google-services.json

1. No Firebase Console, na mesma página do app
2. Clique em **Baixar google-services.json**
3. Substitua o arquivo `app/google-services.json` pelo novo
4. Sincronize o projeto: **File → Sync Project with Gradle Files**

### 4. Limpar e Reconstruir

1. **Build → Clean Project**
2. **Build → Rebuild Project**
3. Execute o app novamente

## Verificação

Após configurar, você deve ver nos logs:
```
GoogleSignIn: Callback recebido - ResultCode: -1, Data: true
GoogleSignIn: Processando resultado do login...
GoogleSignIn: Task obtida, isComplete: true, isSuccessful: true
GoogleSignIn: Conta obtida: seuemail@gmail.com
GoogleSignIn: Credential criada, autenticando com Firebase...
GoogleSignIn: Firebase auth completado - Sucesso: true
GoogleSignIn: Login Firebase bem-sucedido - User: seuemail@gmail.com
```

## Erros Comuns

### Erro 10 (DEVELOPER_ERROR)
- **Causa**: SHA-1 não configurado ou google-services.json desatualizado
- **Solução**: Siga os passos acima

### "Credenciais inválidas"
- **Causa**: Client ID incorreto ou SHA-1 não corresponde
- **Solução**: Verifique o Client ID no código e no Firebase Console

### "Login com Google não está habilitado"
- **Causa**: Método de autenticação não habilitado no Firebase
- **Solução**: Firebase Console → Authentication → Sign-in method → Habilitar Google

## SHA-1 para Release

Quando for publicar na Play Store, você também precisará adicionar o SHA-1 do certificado de release:

```powershell
keytool -list -v -keystore app\keystore.jks -alias psipro_key
```

Use a senha: `psipro2024`

Adicione esse SHA-1 também no Firebase Console.

