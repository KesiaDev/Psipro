# Script PowerShell para iniciar o servidor Next.js limpo

Write-Host "🛑 Finalizando TODOS os processos Node.js..." -ForegroundColor Yellow
Get-Process | Where-Object {$_.ProcessName -eq "node"} | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 3

Write-Host "🔍 Verificando processos restantes..." -ForegroundColor Yellow
$remaining = Get-Process | Where-Object {$_.ProcessName -eq "node"} | Measure-Object
if ($remaining.Count -gt 0) {
    Write-Host "⚠️ Ainda há processos Node rodando. Tentando novamente..." -ForegroundColor Red
    Get-Process | Where-Object {$_.ProcessName -eq "node"} | Stop-Process -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 2
}

Write-Host "🧹 Removendo pasta .next..." -ForegroundColor Yellow
if (Test-Path .next) {
    Remove-Item -Recurse -Force .next -ErrorAction SilentlyContinue
    Write-Host "✅ Pasta .next removida" -ForegroundColor Green
}

Write-Host "🧹 Removendo cache do node_modules..." -ForegroundColor Yellow
if (Test-Path node_modules\.cache) {
    Remove-Item -Recurse -Force node_modules\.cache -ErrorAction SilentlyContinue
    Write-Host "✅ Cache removido" -ForegroundColor Green
}

Write-Host "🧹 Removendo arquivos lock..." -ForegroundColor Yellow
Get-ChildItem -Path . -Filter "*.lock" -Recurse -ErrorAction SilentlyContinue | Remove-Item -Force -ErrorAction SilentlyContinue
Write-Host "✅ Arquivos lock removidos" -ForegroundColor Green

Write-Host "🚀 Iniciando servidor Next.js..." -ForegroundColor Green
npm run dev

