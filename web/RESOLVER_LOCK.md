# 🔧 Resolver Problema de Lock File - Passo a Passo

## ⚠️ O Problema

O Next.js não consegue iniciar porque há um arquivo de lock indicando que outra instância está rodando.

## ✅ SOLUÇÃO DEFINITIVA (FAÇA ISSO AGORA)

### Passo 1: Abrir Gerenciador de Tarefas
- Pressione: `Ctrl + Shift + Esc`
- Ou: `Ctrl + Alt + Del` → Gerenciador de Tarefas

### Passo 2: Finalizar TODOS os processos Node.js
1. Vá na aba **"Detalhes"**
2. Procure por **"node.exe"**
3. **Selecione TODOS** (Ctrl+A ou clique em cada um segurando Ctrl)
4. Clique em **"Finalizar tarefa"** (canto inferior direito)
5. Confirme se necessário

### Passo 3: Aguardar 5 segundos

### Passo 4: Abrir NOVO terminal PowerShell
- **IMPORTANTE**: Feche todos os terminais antigos
- Abra um terminal completamente novo

### Passo 5: Executar comandos
```powershell
cd C:\Users\User\Desktop\psipro\web

# Remover pasta .next
Remove-Item -Recurse -Force .next -ErrorAction SilentlyContinue

# Iniciar servidor
npm run dev
```

## ✅ Verificar se Funcionou

Você deve ver:
```
✓ Ready in X seconds
Local: http://localhost:3001
```

**NÃO deve aparecer:**
- ❌ "Unable to acquire lock"
- ❌ Erros vermelhos

## 🎯 Depois que Funcionar

1. Acesse: http://localhost:3001
2. Você verá a **landing page** que acabamos de criar!
3. Clique em "Conhecer a plataforma" para ir ao dashboard
4. O onboarding aparecerá automaticamente (se for primeiro acesso)

---

## 💡 Por que isso acontece?

O Next.js cria um arquivo de lock quando inicia para evitar múltiplas instâncias. Se o processo anterior não foi finalizado corretamente, o lock fica "preso" e impede novos inícios.

**Solução**: Sempre finalizar processos pelo Gerenciador de Tarefas quando houver problemas.


