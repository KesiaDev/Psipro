# 📍 Onde Está o Backend

## 🗂️ Localização do Backend

O backend está na pasta:
```
C:\Users\User\Desktop\psipro\backend
```

---

## 🔍 Como Acessar

### Via Explorador de Arquivos (Windows)

1. Abra o **Explorador de Arquivos**
2. Vá para: `C:\Users\User\Desktop\psipro\backend`
3. Você verá os arquivos do backend

### Via Terminal

```powershell
cd C:\Users\User\Desktop\psipro\backend
```

---

## 📁 Estrutura do Backend

Dentro da pasta `backend/`, você encontrará:

```
backend/
├── src/              # Código fonte (NestJS)
├── prisma/           # Schema do banco de dados
├── package.json      # Dependências
├── tsconfig.json     # Config TypeScript
└── railway.json      # Config Railway
```

---

## 🚀 Como Iniciar o Backend

### No Terminal:

1. **Abra um novo terminal**
2. **Entre na pasta do backend:**
   ```powershell
   cd C:\Users\User\Desktop\psipro\backend
   ```
3. **Inicie o backend:**
   ```powershell
   npm run start:dev
   ```

### O que você deve ver:

```
[NestApplication] Nest application successfully started on port 3001
```

---

## 🌐 Como Acessar o Backend (API)

Depois que o backend estiver rodando:

### No Navegador:

1. Acesse: `http://localhost:3001`
2. Pode mostrar erro 404 (normal, não tem rota raiz)
3. Ou teste: `http://localhost:3001/api`

### Endpoints Disponíveis:

- `POST /api/auth/login` - Login
- `POST /api/auth/register` - Registro
- `GET /api/clinics` - Listar clínicas
- `GET /api/patients` - Listar pacientes
- etc.

---

## ✅ Verificar se Backend Está Rodando

### Opção 1: Via Terminal

Veja se há um processo rodando na porta 3001:
```powershell
Get-NetTCPConnection -LocalPort 3001 -ErrorAction SilentlyContinue
```

### Opção 2: Via Navegador

Acesse: `http://localhost:3001`

- Se mostrar algo (mesmo erro): ✅ Backend rodando
- Se der "Não é possível acessar": ❌ Backend não está rodando

---

## 📝 Resumo

- **Localização**: `C:\Users\User\Desktop\psipro\backend`
- **Para iniciar**: `cd backend` → `npm run start:dev`
- **URL**: `http://localhost:3001`
- **API Base**: `http://localhost:3001/api`

---

**Precisa de ajuda para iniciar? Me diga! 🚀**
