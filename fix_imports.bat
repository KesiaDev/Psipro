@echo off
echo Corrigindo imports R...
echo.

REM Substituir imports R em todos os arquivos .kt
powershell -Command "Get-ChildItem -Path 'app/src/main/java' -Recurse -Filter '*.kt' | ForEach-Object { $content = Get-Content $_.FullName -Raw; $content = $content -replace 'import com\.example\.psipro\.R', 'import com.psipro.app.R'; Set-Content $_.FullName $content }"

echo Imports R corrigidos!
echo.
pause 