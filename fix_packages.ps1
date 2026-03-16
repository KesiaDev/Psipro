Get-ChildItem -Path "app/src/main/java" -Recurse -Filter "*.kt" | ForEach-Object {
    $content = Get-Content $_.FullName -Raw
    $content = $content -replace "package com\.example\.psipro", "package com.psipro.app"
    Set-Content $_.FullName $content
    Write-Host "Processado: $($_.Name)"
}
Write-Host "Concluído!" 