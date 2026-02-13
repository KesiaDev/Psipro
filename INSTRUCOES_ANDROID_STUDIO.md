# 🚀 INSTRUÇÕES PARA RESOLVER O PROBLEMA NO ANDROID STUDIO

## ✅ **CORREÇÕES JÁ APLICADAS:**

1. ✅ Removido módulo `:compose` inexistente do `settings.gradle.kts`
2. ✅ Cache do Gradle corrompido removido
3. ✅ Gradle wrapper 8.11.1 baixado novamente
4. ✅ Configuração do `settings.gradle.kts` ajustada

## 📋 **PASSOS NO ANDROID STUDIO:**

### **PASSO 1: Invalidar Caches**
1. No Android Studio, vá em: **File → Invalidate Caches / Restart...**
2. Marque todas as opções:
   - ✅ Clear file system cache and Local History
   - ✅ Clear downloaded shared indexes
3. Clique em **"Invalidate and Restart"**
4. Aguarde o Android Studio reiniciar completamente

### **PASSO 2: Sincronizar Projeto**
1. Após reiniciar, vá em: **File → Sync Project with Gradle Files**
2. Aguarde a sincronização completar (pode demorar alguns minutos)
3. Verifique se aparecem erros na aba "Build"

### **PASSO 3: Verificar Run Configuration**
1. Clique no dropdown ao lado do botão de Run (onde está escrito "app")
2. Se não aparecer "app", clique em **"Edit Configurations..."**
3. Clique no **"+"** e selecione **"Android App"**
4. Configure:
   - **Name:** app
   - **Module:** app
   - **Launch:** Default Activity
5. Clique em **OK**

### **PASSO 4: Se Ainda Não Funcionar**
1. **File → Close Project**
2. **File → Open** → Navegue até a pasta `C:\Users\User\AndroidStudioProjects\Psipro`
3. Selecione a pasta e clique em **OK**
4. Aguarde o Android Studio indexar o projeto novamente

## 🔍 **VERIFICAÇÕES IMPORTANTES:**

### Verificar JDK:
1. **File → Project Structure → SDK Location**
2. Certifique-se que o **JDK location** está apontando para: `C:\Program Files\Java\jdk-17`

### Verificar Gradle:
1. **File → Settings → Build, Execution, Deployment → Build Tools → Gradle**
2. Certifique-se que está usando: **"Gradle wrapper (recommended)"**
3. A versão deve ser: **8.11.1**

## ⚠️ **SE O PROBLEMA PERSISTIR:**

Execute no terminal (na pasta do projeto):
```bash
./gradlew clean
./gradlew build
```

Depois tente sincronizar novamente no Android Studio.

## 📝 **NOTA:**

O erro "Unable to find modules to build for 'app' Run Configuration" geralmente é resolvido após invalidar os caches e sincronizar o projeto. O Android Studio precisa reindexar o projeto após a atualização.

---

**Status:** ✅ Gradle wrapper corrigido e baixado  
**Próximo passo:** Invalidar caches no Android Studio


