@echo off
echo Corrigindo packages...
echo.

for /r "app\src\main\java" %%f in (*.kt) do (
    echo Processando: %%f
    powershell -Command "(Get-Content '%%f') -replace 'package com\.example\.psipro', 'package com.psipro.app' | Set-Content '%%f'"
)

echo.
echo Packages corrigidos!
pause 