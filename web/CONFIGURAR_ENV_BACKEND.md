# 🔧 Configurar .env do Backend

## ⚠️ Erro: "JwtStrategy requires a secret or key"

Este erro acontece porque o backend precisa de um arquivo `.env` com variáveis de ambiente.

---

## ✅ Solução: Criar Arquivo .env

Criei um arquivo `.env` básico no backend. Mas você precisa ajustar:

### 1. Arquivo `.env` Criado

Localização: `C:\Users\User\Desktop\psipro\backend\.env`

Conteúdo inicial:
```env
JWT_SECRET=psipro_jwt_secret_key_dev_123456789_abcdefghijklmnop
PORT=3001
DATABASE_URL=postgresql://user:password@localhost:5432/psipro?schema=public
```

---

## ⚠️ IMPORTANTE: Ajustar DATABASE_URL

O `DATABASE_URL` acima é um **exemplo**. Você precisa:

### Opção 1: Se tiver PostgreSQL local

1. Instale PostgreSQL (se não tiver)
2. Crie um banco `psipro`
3. Ajuste o `.env` com suas credenciais:
   ```env
   DATABASE_URL=postgresql://seu_usuario:sua_senha@localhost:5432/psipro?schema=public
   ```

### Opção 2: Usar SQLite (mais simples para desenvolvimento)

Se você não tem PostgreSQL, pode usar SQLite temporariamente:

```env
DATABASE_URL="file:./dev.db"
```

**Mas isso requer ajustar o schema do Prisma!**

---

## 📝 Arquivo .env Completo (Ajuste conforme necessário)

```env
# JWT Secret (para autenticação)
JWT_SECRET=psipro_jwt_secret_key_dev_123456789_abcdefghijklmnop

# Porta do servidor
PORT=3001

# Database URL
# Para PostgreSQL local:
DATABASE_URL=postgresql://usuario:senha@localhost:5432/psipro?schema=public

# Para desenvolvimento, você pode usar SQLite (mas precisa ajustar Prisma):
# DATABASE_URL="file:./dev.db"

# Ambiente
NODE_ENV=development
```

---

## ✅ Após Criar/Ajustar .env

1. **Salve o arquivo `.env`**
2. **Tente iniciar o backend novamente:**
   ```powershell
   npm run start:dev
   ```

---

## 🎯 Próximos Passos

1. ✅ Arquivo `.env` criado
2. ⚠️ Ajustar `DATABASE_URL` (se necessário)
3. ⏳ Tentar iniciar backend: `npm run start:dev`
4. ⏳ Se der erro de banco, configurar PostgreSQL ou ajustar Prisma

---

**O arquivo `.env` foi criado. Ajuste o DATABASE_URL se necessário e tente iniciar novamente! 🚀**
