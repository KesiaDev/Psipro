@echo off
echo Corrigindo TODOS os imports...
echo.

REM Substituir todos os imports com com.example.psipro
powershell -Command "Get-ChildItem -Path 'app/src/main/java' -Recurse -Filter '*.kt' | ForEach-Object { $content = Get-Content $_.FullName -Raw; $content = $content -replace 'import com\.example\.psipro', 'import com.psipro.app'; Set-Content $_.FullName $content }"

echo Todos os imports corrigidos!
echo.
pause 