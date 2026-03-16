# Testes Mobile PsiPro - Android

Os testes Android estão em **estrutura de exemplo** e precisam ser integrados ao projeto `psipro-app` (módulo Android).

## Integração

1. Copie os arquivos `*Test.kt` para o módulo de testes do app:
   ```
   psipro-app/src/androidTest/java/com/psipro/app/
   ```

2. Instale dependências no `build.gradle.kts` do app:
   ```kotlin
   androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.0")
   androidTestImplementation("androidx.test.ext:junit:1.2.0")
   ```

3. Ajuste os imports e os nomes das composables (`LoginScreen`, `AgendaScreen`, etc.) para os do seu app.

## Execução

```bash
cd psipro-app
./gradlew connectedAndroidTest
```

Ou no Android Studio: Run > Run 'androidTest'.
