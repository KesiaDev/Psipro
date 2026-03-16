@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"

echo ============================================
echo   Voice AI — Build Automatico
echo ============================================
echo.

REM ── Le versao do whisper_transcriber.py
for /f "tokens=2 delims='= " %%v in ('findstr /R "^VERSION" whisper_transcriber.py') do set "VER=%%~v"
set "VER=%VER:"=%"
echo Versao detectada: v%VER%
echo.

REM ── Verifica PyInstaller (tenta direto e via python -m)
set "PYI=pyinstaller"
%PYI% --version >nul 2>&1
if errorlevel 1 (
    set "PYI=python -m PyInstaller"
    !PYI! --version >nul 2>&1
    if errorlevel 1 (
        echo ERRO: PyInstaller nao encontrado.
        echo Instale com: python -m pip install pyinstaller
        pause
        exit /b 1
    )
)
echo PyInstaller encontrado.

REM ── Verifica Inno Setup
set "ISCC="
where iscc >nul 2>&1
if not errorlevel 1 (
    set "ISCC=iscc"
) else if exist "C:\Program Files (x86)\Inno Setup 6\ISCC.exe" (
    set "ISCC=C:\Program Files (x86)\Inno Setup 6\ISCC.exe"
) else if exist "C:\Program Files\Inno Setup 6\ISCC.exe" (
    set "ISCC=C:\Program Files\Inno Setup 6\ISCC.exe"
)
if "%ISCC%"=="" (
    echo ERRO: Inno Setup 6 nao encontrado.
    echo Instale de: https://jrsoftware.org/isdl.php
    pause
    exit /b 1
)
echo Inno Setup encontrado.
echo.

REM ── [1/7] Limpa builds anteriores
echo [1/7] Limpando builds anteriores...
if exist dist rmdir /s /q dist
if exist build rmdir /s /q build
echo OK
echo.

REM ── [2/7] Gera version info
echo [2/7] Gerando version info (v%VER%)...
python version_info.py
if errorlevel 1 (
    echo ERRO ao gerar version info
    pause
    exit /b 1
)
echo OK
echo.

REM ── [3/7] Build do app principal (VoiceAI.exe)
echo [3/7] Compilando VoiceAI.exe (transcritor)...
%PYI% VoiceAI.spec --noconfirm --clean
if errorlevel 1 (
    echo.
    echo ERRO ao compilar VoiceAI.exe
    pause
    exit /b 1
)
echo OK — dist\VoiceAI.exe
echo.

REM ── [4/7] Build do Voice Agent (VoiceAgent.exe)
echo [4/7] Compilando VoiceAgent.exe (agente de voz)...
%PYI% VoiceAgent.spec --noconfirm --clean
if errorlevel 1 (
    echo.
    echo ERRO ao compilar VoiceAgent.exe
    pause
    exit /b 1
)
echo OK — dist\VoiceAgent.exe
echo.

REM ── [5/7] Build do instalador via Inno Setup
echo [5/7] Compilando Voice AI Setup.exe (Inno Setup)...
"%ISCC%" setup.iss
if errorlevel 1 (
    echo.
    echo ERRO ao compilar instalador Inno Setup
    pause
    exit /b 1
)
echo OK — dist\Voice AI Setup.exe
echo.

REM ── [6/7] Limpa exes avulsos (ja embutidos no setup)
echo [6/7] Removendo exes avulsos...
if exist "dist\VoiceAI.exe" del /q "dist\VoiceAI.exe"
if exist "dist\VoiceAgent.exe" del /q "dist\VoiceAgent.exe"
echo OK
echo.

REM ── [7/7] Limpa temporarios
echo [7/7] Limpando arquivos temporarios...
if exist version_voiceai.txt del /q version_voiceai.txt
if exist version_voiceagent.txt del /q version_voiceagent.txt
if exist version_cache.ini del /q version_cache.ini
echo OK
echo.

REM ── Resumo
echo ============================================
echo   Build completo!
echo.
echo   Arquivo pronto:
echo   - dist\Voice AI Setup.exe   (instalador Inno Setup)
echo     (inclui VoiceAI + VoiceAgent embutidos)
echo   Versao: v%VER%
echo ============================================
echo.
echo Dois cliques em "dist\Voice AI Setup.exe" para instalar.
echo.
pause
