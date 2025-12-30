# 🚨 INSTRUÇÕES URGENTES - Resolver Lock File

## ⚠️ O Problema

O Next.js não consegue iniciar porque há um arquivo de lock que indica que outra instância está rodando.

## ✅ SOLUÇÃO DEFINITIVA

### Método 1: Usar o Script (Mais Fácil)

1. **Feche TODOS os terminais** (Ctrl+C em todos)

2. **Abra um NOVO PowerShell** (não use o terminal atual)

3. **Execute:**
```powershell
cd C:\Users\User\Desktop\psipro\web
.\limpar-e-iniciar.ps1
```

### Método 2: Manual (Se o script não funcionar)

1. **Abra o Gerenciador de Tarefas**
   - Pressione: `Ctrl + Shift + Esc`
   - Ou: `Ctrl + Alt + Del` → Gerenciador de Tarefas

2. **Vá na aba "Detalhes"**

3. **Procure por "node.exe"**

4. **Selecione TODOS os processos "node.exe"**
   - Clique no primeiro
   - Segure Ctrl e clique nos outros
   - Ou: Selecione todos com Ctrl+A

5. **Clique em "Finalizar tarefa"** (canto inferior direito)

6. **Aguarde 5 segundos**

7. **Abra um NOVO terminal PowerShell**

8. **Execute:**
```powershell
cd C:\Users\User\Desktop\psipro\web

# Remover pasta .next
Remove-Item -Recurse -Force .next -ErrorAction SilentlyContinue

# Iniciar servidor
npm run dev
```

### Método 3: Reiniciar o Computador

Se NADA funcionar:

1. Salve seu trabalho
2. Reinicie o computador
3. Após reiniciar, execute:
```powershell
cd C:\Users\User\Desktop\psipro\web
npm run dev
```

---

## ✅ Como Saber se Funcionou

Você deve ver no terminal:

```
✓ Ready in X seconds
Local: http://localhost:3001
```

**NÃO deve aparecer:**
- ❌ "Unable to acquire lock"
- ❌ Erros vermelhos

---

## 🎯 Depois que Funcionar

1. Acesse: http://localhost:3001/dashboard
2. Para testar o onboarding:
   - Abra o console (F12)
   - Execute: `localStorage.removeItem('psipro_onboarding_completed')`
   - Recarregue a página (F5)
   - O onboarding deve aparecer!

---

## 📞 Se Ainda Não Funcionar

Me avise e vamos tentar outra abordagem!


