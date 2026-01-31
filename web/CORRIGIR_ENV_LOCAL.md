# 🔧 Correção Rápida: Erro de Conexão

## ⚠️ Problema Identificado

Seu `.env.local` tem:
```
NEXT_PUBLIC_API_URL=http://localhost:3000
```

**Falta o `/api` no final!** Deve ser:
```
NEXT_PUBLIC_API_URL=http://localhost:3000/api
```

---

## ✅ Solução Rápida

**Opção 1: Se backend está na porta 3000**
```env
NEXT_PUBLIC_API_URL=http://localhost:3000/api
```

**Opção 2: Se backend está na porta 3001 (mais comum)**
```env
NEXT_PUBLIC_API_URL=http://localhost:3001/api
```

---

## 🔍 Como Saber Qual Porta Usar?

1. **Veja no terminal do backend** onde você rodou `npm run start:dev`
2. Procure por: `Nest application successfully started on port XXXX`
3. Use essa porta no `.env.local`

---

## 📝 Passos para Corrigir

1. **Abra o arquivo `.env.local`** na pasta `web/`
2. **Altere para:**
   ```env
   NEXT_PUBLIC_API_URL=http://localhost:3001/api
   ```
   (Use a porta que seu backend está usando)
3. **Salve o arquivo**
4. **REINICIE o servidor Next.js**:
   - Pare (Ctrl+C)
   - Rode `npm run dev` novamente
5. **Tente fazer login novamente**

---

## ⚠️ IMPORTANTE

- URL deve terminar com `/api`
- Reinicie o servidor Next.js após alterar `.env.local`
- Backend deve estar rodando na porta especificada
