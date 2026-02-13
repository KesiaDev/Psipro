# 🔧 Solução Definitiva - Problema de Lock File

## ⚠️ Se o servidor ainda não iniciar:

### Opção 1: Usar o Script Atualizado (Recomendado)

Execute no PowerShell:

```powershell
cd C:\Users\User\Desktop\psipro\web
.\iniciar-servidor.ps1
```

### Opção 2: Manual (Passo a Passo)

1. **Feche TODOS os terminais** (incluindo este)

2. **Abra o Gerenciador de Tarefas** (Ctrl+Shift+Esc)

3. **Finalize todos os processos Node.js:**
   - Vá na aba "Detalhes"
   - Procure por "node.exe"
   - Selecione todos e clique em "Finalizar tarefa"

4. **Aguarde 5 segundos**

5. **Abra um NOVO terminal PowerShell**

6. **Execute:**
```powershell
cd C:\Users\User\Desktop\psipro\web

# Finalizar processos
Get-Process | Where-Object {$_.ProcessName -eq "node"} | Stop-Process -Force

# Aguardar
Start-Sleep -Seconds 3

# Remover tudo
Remove-Item -Recurse -Force .next -ErrorAction SilentlyContinue
Remove-Item -Recurse -Force node_modules\.cache -ErrorAction SilentlyContinue

# Iniciar
npm run dev
```

### Opção 3: Reiniciar o Computador

Se nada funcionar, reinicie o computador e tente novamente.

---

## ✅ Verificar se Funcionou

Após executar, você deve ver:

```
✓ Ready in X seconds
Local: http://localhost:3001
```

**NÃO deve aparecer:**
- ❌ "Unable to acquire lock"
- ❌ "Port is in use"
- ❌ Erros vermelhos

---

## 🎯 Testar o Onboarding

1. Acesse: http://localhost:3001/dashboard
2. Limpe o localStorage (F12 → Console):
   ```javascript
   localStorage.removeItem('psipro_onboarding_completed')
   ```
3. Recarregue a página (F5)
4. O onboarding deve aparecer automaticamente!

---

## 📝 Se Ainda Não Funcionar

Me avise e tentaremos outra abordagem!



