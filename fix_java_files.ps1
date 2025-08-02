Get-ChildItem -Path "app/src/main/java" -Recurse -Filter "*.java" | ForEach-Object {
    $content = Get-Content $_.FullName -Raw
    $content = $content -replace "com\.example\.psipro", "com.psipro.app"
    Set-Content $_.FullName $content
    Write-Host "Processado: $($_.Name)"
}
Write-Host "Concluído!" 