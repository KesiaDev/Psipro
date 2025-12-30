# Script definitivo para limpar e iniciar o servidor Next.js

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  LIMPEZA COMPLETA E INICIO DO SERVIDOR" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Passo 1: Finalizar TODOS os processos Node
Write-Host "[1/5] Finalizando processos Node.js..." -ForegroundColor Yellow
$processes = Get-Process | Where-Object {$_.ProcessName -eq "node"}
if ($processes) {
    $processes | Stop-Process -Force -ErrorAction SilentlyContinue
    Write-Host "  Processos finalizados: $($processes.Count)" -ForegroundColor Green
} else {
    Write-Host "  Nenhum processo Node encontrado" -ForegroundColor Green
}

# Aguardar
Start-Sleep -Seconds 3

# Passo 2: Verificar se ainda ha processos
Write-Host "[2/5] Verificando processos restantes..." -ForegroundColor Yellow
$remaining = Get-Process | Where-Object {$_.ProcessName -eq "node"}
if ($remaining) {
    Write-Host "  Ainda ha processos. Finalizando novamente..." -ForegroundColor Red
    $remaining | Stop-Process -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 2
}
Write-Host "  Nenhum processo Node rodando" -ForegroundColor Green

# Passo 3: Remover pasta .next
Write-Host "[3/5] Removendo pasta .next..." -ForegroundColor Yellow
if (Test-Path ".next") {
    Remove-Item -Recurse -Force ".next" -ErrorAction SilentlyContinue
    Write-Host "  Pasta .next removida" -ForegroundColor Green
} else {
    Write-Host "  Pasta .next nao existe" -ForegroundColor Green
}

# Passo 4: Remover cache
Write-Host "[4/5] Removendo cache..." -ForegroundColor Yellow
if (Test-Path "node_modules\.cache") {
    Remove-Item -Recurse -Force "node_modules\.cache" -ErrorAction SilentlyContinue
    Write-Host "  Cache removido" -ForegroundColor Green
}

# Passo 5: Remover lock files
Write-Host "[5/5] Removendo arquivos lock..." -ForegroundColor Yellow
Get-ChildItem -Path . -Filter "*lock*" -Recurse -ErrorAction SilentlyContinue | Remove-Item -Force -ErrorAction SilentlyContinue
Write-Host "  Arquivos lock removidos" -ForegroundColor Green

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  INICIANDO SERVIDOR..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Iniciar servidor
npm run dev
