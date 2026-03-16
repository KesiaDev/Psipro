# 🚀 Próximo Passo: Testar o Sistema

## ✅ Status Atual

- ✅ Backend online no Railway (`psipro-production.up.railway.app`)
- ✅ Frontend configurado (`.env.local` com URL do Railway)
- ✅ Próximo passo: **TESTAR!**

---

## 📋 PASSO A PASSO PARA TESTAR

### 1️⃣ Iniciar Servidor Frontend

No terminal, na pasta `web/`:

```powershell
npm run dev
```

Aguarde aparecer:
```
✓ Ready in X seconds
- Local:        http://localhost:3000
```

---

### 2️⃣ Acessar no Navegador

Abra seu navegador e acesse:
```
http://localhost:3000
```

---

### 3️⃣ Testar Login

#### Opção A: Página de Login (Recomendado)

1. Acesse: `http://localhost:3000/login`
2. Use as credenciais de teste:
   - **Email**: `owner@psiclinic.com`
   - **Senha**: `senha123`
3. Clique em **"Entrar"**
4. Você será redirecionado para `/dashboard`

#### Opção B: Página de Teste (Mais Completo)

1. Acesse: `http://localhost:3000/test`
2. As credenciais já vêm pré-preenchidas
3. Clique em **"Fazer Login"**
4. Veja status da conexão com API

---

## 🔐 Credenciais de Teste Disponíveis

| Email | Senha | Tipo | Descrição |
|-------|-------|------|-----------|
| `owner@psiclinic.com` | `senha123` | Owner | Proprietário da clínica |
| `psicologo2@psiclinic.com` | `senha123` | Psicólogo | Psicólogo membro |
| `psicologo@psipro.com` | `senha123` | Independente | Psicólogo independente |

---

## 🧪 O QUE TESTAR

### ✅ Funcionalidades Básicas

1. **Landing Page** (`/`)
   - Página inicial carrega?
   - Botão "Solicitar Acesso Beta" funciona?

2. **Login** (`/login`)
   - Formulário aparece?
   - Login funciona com credenciais de teste?
   - Redireciona para `/dashboard` após login?

3. **Dashboard** (`/dashboard`)
   - Carrega após login?
   - Mostra métricas (mesmo que zeros)?
   - Insights aparecem?
   - Onboarding modal abre no primeiro acesso?

4. **Pacientes** (`/pacientes`)
   - Lista de pacientes carrega?
   - Pode criar/visualizar pacientes?

5. **Agenda** (`/agenda`)
   - Calendário aparece?
   - Consultas são exibidas?

6. **Financeiro** (`/financeiro`)
   - Dados financeiros carregam?
   - Gráficos aparecem?

7. **Clínicas** (`/clinica`)
   - Lista de clínicas carrega?
   - Pode gerenciar clínicas?

---

## 🎯 Checklist Rápido

- [ ] Servidor frontend iniciado (`npm run dev`)
- [ ] Acessar `http://localhost:3000`
- [ ] Fazer login com `owner@psiclinic.com` / `senha123`
- [ ] Dashboard carrega
- [ ] Navegar pelas páginas (Pacientes, Agenda, Financeiro, Clínicas)
- [ ] Logout funciona (botão no header)

---

## ⚠️ Problemas Comuns

### Erro: "Erro de conexão"

**Causa**: Backend não está acessível

**Solução**: 
- Verifique se o backend está online no Railway
- Verifique se a URL no `.env.local` está correta
- Veja o console do navegador (F12) para mais detalhes

### Login não funciona

**Verifique**:
- Backend está online no Railway?
- Usuário existe no banco? (usuários do seed)
- Token está sendo salvo? (F12 → Application → Local Storage → `psipro_token`)

### Porta 3000 ocupada

**Solução**: 
- O Next.js automaticamente usa a porta 3001
- Acesse `http://localhost:3001`

---

## 🎉 Pronto!

Depois de testar, você terá uma ideia completa de como o sistema funciona!

**Boa sorte com os testes! 🚀**
