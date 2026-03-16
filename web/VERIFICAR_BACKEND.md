# 🔍 Verificar se Backend está Rodando

## ⚠️ Erro de Conexão = Backend Não Está Rodando

O erro "Erro de conexão" significa que o **backend não está acessível**.

---

## ✅ Solução: Iniciar o Backend

### Passo 1: Abrir Terminal do Backend

Abra um **NOVO terminal** (deixe o terminal do Next.js rodando).

### Passo 2: Ir para Pasta do Backend

```powershell
cd C:\Users\User\Desktop\psipro\backend
```

### Passo 3: Iniciar Backend

```powershell
npm run start:dev
```

**Ou:**
```powershell
npm start
```

### Passo 4: Aguardar

Você deve ver algo como:
```
[NestApplication] Nest application successfully started on port 3001
```

**Anote a porta!** (geralmente 3001 ou 3000)

---

## 🔧 Se Backend Estiver em Porta Diferente

Se o backend iniciar em uma porta diferente de 3001:

1. Veja qual porta aparece no terminal
2. Edite `.env.local` na pasta `web/`
3. Altere para a porta correta:
   ```env
   NEXT_PUBLIC_API_URL=http://localhost:XXXX/api
   ```
   (Substitua XXXX pela porta do backend)
4. Reinicie o servidor Next.js

---

## ✅ Quando Funcionar

Você precisa ter **2 terminais rodando**:

1. **Terminal 1 - Backend:**
   ```
   cd backend
   npm run start:dev
   ```
   Deve mostrar: "started on port XXXX"

2. **Terminal 2 - Frontend (Next.js):**
   ```
   cd web
   npm run dev
   ```
   Deve mostrar: "Ready on http://localhost:3000"

---

## 🎯 Resumo

**Para o login funcionar:**
1. ✅ Backend deve estar rodando (terminal separado)
2. ✅ `.env.local` deve ter a URL correta com `/api`
3. ✅ Porta no `.env.local` deve corresponder à porta do backend
4. ✅ Next.js deve ter sido reiniciado após alterar `.env.local`
