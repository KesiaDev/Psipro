# 🔧 Corrigir Erro "Cannot find module lockfile"

## ❌ Problema

Erro ao executar `npm run dev`:
```
Error: Cannot find module '../../../build/lockfile'
code: 'MODULE_NOT_FOUND'
```

## 🔍 Causa

O `node_modules` está **corrompido** ou **incompleto**. Isso pode acontecer quando:
- Arquivos foram removidos acidentalmente de `node_modules`
- Instalação de dependências foi interrompida
- Cache corrompido

## ✅ Solução: Reinstalar Dependências

### Passo 1: Remover node_modules e package-lock.json

```powershell
# Remover node_modules
Remove-Item -Recurse -Force node_modules -ErrorAction SilentlyContinue

# Remover package-lock.json (opcional, mas recomendado)
Remove-Item -Force package-lock.json -ErrorAction SilentlyContinue
```

### Passo 2: Limpar cache do npm

```powershell
npm cache clean --force
```

### Passo 3: Reinstalar dependências

```powershell
npm install
```

### Passo 4: Iniciar servidor

```powershell
npm run dev
```

---

## 🚀 Script Rápido (Tudo de uma vez)

Execute no PowerShell:

```powershell
Remove-Item -Recurse -Force node_modules -ErrorAction SilentlyContinue
Remove-Item -Force package-lock.json -ErrorAction SilentlyContinue
npm cache clean --force
npm install
npm run dev
```

---

## ⏱️ Tempo Estimado

- Remover arquivos: ~30 segundos
- Limpar cache: ~10 segundos
- Reinstalar: ~2-5 minutos (depende da internet)

**Total: ~3-6 minutos**

---

## ✅ Depois de Reinstalar

O servidor deve iniciar normalmente:
```
✓ Ready in X seconds
- Local:        http://localhost:3000
```

---

**Vou executar isso para você agora! 🚀**
