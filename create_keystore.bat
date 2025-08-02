@echo off
echo Criando keystore para PsiPro...
echo.

REM Tentar encontrar o Java
set JAVA_HOME=C:\Program Files\Java\jdk-17
if not exist "%JAVA_HOME%\bin\keytool.exe" (
    set JAVA_HOME=C:\Program Files\Java\jdk-11
)
if not exist "%JAVA_HOME%\bin\keytool.exe" (
    set JAVA_HOME=C:\Program Files\Java\jdk-8
)

if exist "%JAVA_HOME%\bin\keytool.exe" (
    echo Java encontrado em: %JAVA_HOME%
    "%JAVA_HOME%\bin\keytool.exe" -genkey -v -keystore app/keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias psipro_key -storepass psipro2024 -keypass psipro2024 -dname "CN=PsiPro, OU=Development, O=PsiPro, L=City, S=State, C=BR"
    echo.
    echo Keystore criado com sucesso!
    echo Localização: app/keystore.jks
) else (
    echo ERRO: Java não encontrado!
    echo Por favor, instale o Java ou use o Android Studio para criar o keystore.
)

pause 