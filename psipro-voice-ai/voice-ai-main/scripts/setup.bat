@echo off
echo ==========================================
echo   WHISPER TRANSCRIBER - Setup
echo ==========================================
echo.

REM Verificar Python
python --version >nul 2>&1
if errorlevel 1 (
    echo ERRO: Python nao encontrado! Instale em python.org
    pause
    exit /b 1
)

echo [1/3] Instalando dependencias...
pip install -r requirements.txt
if errorlevel 1 (
    echo ERRO ao instalar dependencias.
    echo Se PyAudio falhar, tente: pip install pipwin && pipwin install pyaudio
    pause
    exit /b 1
)

echo.
echo [2/3] Verificando arquivo .env...
if not exist .env (
    copy .env.example .env
    echo.
    echo IMPORTANTE: Abra o arquivo .env e coloque sua OPENAI_API_KEY!
    echo Pegue sua key em: https://platform.openai.com/api-keys
    echo.
) else (
    echo .env ja existe, pulando.
)

echo.
echo [3/3] Setup completo!
echo.
echo Para iniciar, rode: python whisper_transcriber.py
echo Ou clique duas vezes em iniciar.bat
echo.
pause
