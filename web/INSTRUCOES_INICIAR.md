# 🚀 Como Iniciar o Servidor Corretamente

## ⚠️ Se estiver dando erro de lock file:

### Opção 1: Usar o Script PowerShell (Recomendado)

1. Abra o PowerShell na pasta `web`
2. Execute:
```powershell
.\iniciar-servidor.ps1
```

### Opção 2: Manual

1. **Finalize todos os processos Node.js:**
```powershell
taskkill /F /IM node.exe
```

2. **Remova a pasta .next:**
```powershell
Remove-Item -Recurse -Force .next
```

3. **Aguarde 2 segundos e inicie:**
```powershell
npm run dev
```

---

## ✅ Verificar se está funcionando

Após iniciar, você deve ver:
```
✓ Ready in X seconds
Local: http://localhost:3001
```

Se aparecer erro de lock novamente:
1. Feche TODOS os terminais
2. Abra um NOVO terminal
3. Execute o script ou os comandos acima

---

## 📝 Importante

- O servidor Next.js roda na porta **3001** (3000 está em uso)
- O backend deve estar na porta **3001** também
- URL da API: `http://localhost:3001/api`

---

## 🔧 Se ainda não funcionar

1. Reinicie o computador (última opção)
2. Ou use uma porta diferente editando `package.json`


