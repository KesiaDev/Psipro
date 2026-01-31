# 🚀 INICIAR BACKEND AGORA

## ⚠️ PROBLEMA: Backend Não Está Rodando!

O erro de conexão acontece porque o **backend não está iniciado**.

---

## ✅ SOLUÇÃO: Iniciar Backend

### Passo 1: Abrir NOVO Terminal

Abra um **NOVO terminal** (deixe o terminal do Next.js rodando).

### Passo 2: Ir para Pasta do Backend

```powershell
cd C:\Users\User\Desktop\psipro\backend
```

### Passo 3: Iniciar Backend

```powershell
npm run start:dev
```

**Aguarde** até ver:
```
[NestApplication] Nest application successfully started on port 3001
```

---

## 📋 Resumo

Você precisa ter **2 terminais rodando ao mesmo tempo**:

### Terminal 1: Backend
```powershell
cd C:\Users\User\Desktop\psipro\backend
npm run start:dev
```
**Deve mostrar**: "started on port 3001"

### Terminal 2: Frontend (Next.js)
```powershell
cd C:\Users\User\Desktop\psipro\web
npm run dev
```
**Deve mostrar**: "Ready on http://localhost:3000"

---

## ✅ Depois que Backend Iniciar

1. Aguarde ver: "successfully started on port 3001"
2. Volte para o navegador
3. Tente fazer login novamente
4. **Deve funcionar!** ✅

---

## 🎯 Verificar se Funcionou

Depois de iniciar o backend, teste no navegador:

1. Acesse: `http://localhost:3001`
2. Se mostrar algo (mesmo erro 404), backend está rodando ✅
3. Se der "Não é possível acessar", backend não iniciou ❌

---

**INICIE O BACKEND AGORA E TENTE NOVAMENTE! 🚀**
