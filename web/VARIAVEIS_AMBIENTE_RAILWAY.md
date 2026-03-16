# 🔐 Variáveis de Ambiente: Local vs Railway

## ❌ NÃO Commitar `.env.local`

O arquivo `.env.local` **NÃO deve** ser commitado no Git porque:

1. ✅ Já está no `.gitignore` (linha 34: `.env*`)
2. ✅ É específico para **desenvolvimento local**
3. ✅ Pode conter informações sensíveis
4. ✅ Cada desenvolvedor tem suas próprias configurações locais

---

## 🎯 Como Funciona

### **Desenvolvimento Local** (Agora)
- Arquivo: `.env.local` (não vai pro Git)
- Uso: `npm run dev` localmente
- Configuração: Você já configurou ✅

### **Produção no Railway** (Quando fizer deploy do Web)
- **NÃO usa arquivo `.env`**
- Configuração: **Dashboard do Railway** → Variables
- O Railway injeta as variáveis diretamente no ambiente

---

## 📋 Quando Fizer Deploy do Web no Railway

Quando você for fazer deploy do **frontend Web** no Railway, você vai:

1. **Criar serviço Web** no Railway
2. **Configurar Root Directory**: `web`
3. **Adicionar Variáveis** no dashboard do Railway:

   ```
   NEXT_PUBLIC_API_URL=https://psipro-production.up.railway.app/api
   PORT=3000
   NODE_ENV=production
   ```

4. **NÃO precisa commitar `.env.local`!**

---

## ✅ Resumo

| Ambiente | Onde Configurar | Commit no Git? |
|----------|----------------|----------------|
| **Local** | `.env.local` | ❌ NÃO |
| **Railway** | Dashboard → Variables | N/A (não usa arquivo) |

---

## 🎯 Agora

- ✅ `.env.local` configurado para desenvolvimento local
- ✅ Não precisa commitar no Git
- ✅ Quando fazer deploy do Web no Railway, configure as variáveis no dashboard

---

**Resumo: NÃO commite `.env.local`. No Railway, configure no dashboard! 🚀**
