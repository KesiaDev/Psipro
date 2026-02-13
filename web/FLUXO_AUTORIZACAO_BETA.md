# 🔐 Fluxo de Autorização Beta - PsiPro

## 📋 Estado Atual

**⚠️ ATENÇÃO**: Atualmente, o sistema está em **modo mock/desenvolvimento**.

### O que acontece agora:
1. ✅ Usuário preenche formulário de solicitação
2. ⚠️ Dados são apenas **logados no console** (não salvos)
3. ⚠️ **Ninguém recebe** a solicitação automaticamente
4. ⚠️ **Ninguém autoriza** automaticamente
5. ⚠️ Acesso é liberado apenas se houver token JWT (desenvolvimento)

---

## 🎯 Como DEVERIA Funcionar (Fluxo Ideal)

### Opção 1: Manual (Recomendado para Beta Fechado)

```
1. Usuário solicita acesso
   ↓
2. Dados salvos no banco (tabela: beta_requests)
   ↓
3. Admin recebe notificação (email/dashboard)
   ↓
4. Admin avalia solicitação
   ↓
5. Admin aprova/rejeita manualmente
   ↓
6. Sistema envia email ao usuário
   ↓
7. Se aprovado: usuário pode criar conta e acessar
```

**Quem recebe**: Administrador do PsiPro (você/equipe)
**Quem autoriza**: Administrador do PsiPro (aprovação manual)

---

### Opção 2: Semi-Automático

```
1. Usuário solicita acesso
   ↓
2. Sistema valida automaticamente (ex: CRP válido, email profissional)
   ↓
3. Se válido: aprova automaticamente
   ↓
4. Se inválido: envia para revisão manual
   ↓
5. Admin revisa casos duvidosos
```

**Quem recebe**: Sistema (validação) + Admin (casos especiais)
**Quem autoriza**: Sistema (automático) + Admin (manual para casos especiais)

---

### Opção 3: Totalmente Automático

```
1. Usuário solicita acesso
   ↓
2. Sistema aprova automaticamente
   ↓
3. Usuário recebe email com link de cadastro
   ↓
4. Usuário cria conta e acessa
```

**Quem recebe**: Ninguém (automático)
**Quem autoriza**: Sistema (automático)

⚠️ **Não recomendado para beta fechado** - perde controle de acesso

---

## 🛠️ Implementação Recomendada (Opção 1 - Manual)

### Backend: Criar Endpoints

#### 1. Endpoint para Salvar Solicitação
```typescript
POST /api/beta/request
Body: {
  fullName: string
  email: string
  city?: string
  state?: string
  practiceType?: string
  expectations?: string
}
Response: { id: string, status: 'pending' }
```

#### 2. Endpoint para Listar Solicitações (Admin)
```typescript
GET /api/beta/requests
Headers: Authorization: Bearer {admin_token}
Response: BetaRequest[]
```

#### 3. Endpoint para Aprovar/Rejeitar (Admin)
```typescript
PATCH /api/beta/requests/:id
Headers: Authorization: Bearer {admin_token}
Body: { status: 'approved' | 'rejected', notes?: string }
Response: BetaRequest
```

#### 4. Endpoint para Verificar Status do Usuário
```typescript
GET /api/auth/beta-status
Headers: Authorization: Bearer {token}
Response: { hasAccess: boolean, status?: 'pending' | 'approved' | 'rejected' }
```

---

### Banco de Dados: Tabela

```prisma
model BetaRequest {
  id          String   @id @default(uuid())
  fullName    String
  email       String   @unique
  city        String?
  state       String?
  practiceType String?
  expectations String?
  status      String   @default("pending") // pending | approved | rejected
  reviewedBy  String?  // ID do admin que revisou
  reviewedAt  DateTime?
  notes       String?  // Notas do admin
  createdAt   DateTime @default(now())
  updatedAt   DateTime @updatedAt
}
```

---

### Frontend: Atualizar Componentes

#### 1. BetaAccessForm - Enviar para API
```typescript
// Substituir console.log por:
const response = await api.post('/beta/request', formData);
```

#### 2. BetaAccessGate - Verificar Status
```typescript
// Substituir mock por:
const response = await api.get('/auth/beta-status');
setHasAccess(response.hasAccess === true);
```

---

## 👥 Quem Recebe e Autoriza (Recomendação)

### Para Beta Fechado (Controle Total):

**Quem recebe:**
- 📧 **Email** para: `admin@psipro.com.br` (ou email configurado)
- 📊 **Dashboard Admin** (página `/admin/beta-requests`)

**Quem autoriza:**
- 👤 **Administrador do PsiPro** (você/equipe)
- ✅ Aprovação **manual** via dashboard ou email
- 📝 Pode adicionar notas sobre a decisão

### Processo de Autorização:

1. **Admin recebe notificação** (email ou dashboard)
2. **Admin acessa dashboard** (`/admin/beta-requests`)
3. **Admin vê lista de solicitações pendentes**
4. **Admin avalia cada solicitação:**
   - Lê informações do profissional
   - Verifica CRP (se fornecido)
   - Analisa expectativas
5. **Admin aprova ou rejeita:**
   - ✅ Aprovar → Sistema envia email de boas-vindas
   - ❌ Rejeitar → Sistema envia email educado explicando
6. **Sistema atualiza status** no banco

---

## 📧 Emails Automáticos

### Email 1: Confirmação de Solicitação
```
Assunto: Recebemos sua solicitação de acesso ao PsiPro Beta

Olá {fullName},

Recebemos sua solicitação de acesso ao beta do PsiPro. 
Estamos analisando e entraremos em contato em breve.

Atenciosamente,
Equipe PsiPro
```

### Email 2: Aprovação
```
Assunto: Acesso ao PsiPro Beta aprovado! 🎉

Olá {fullName},

Ótimas notícias! Sua solicitação de acesso ao beta foi aprovada.

Clique no link abaixo para criar sua conta:
{link_cadastro}

Bem-vindo ao PsiPro!

Equipe PsiPro
```

### Email 3: Rejeição (Educado)
```
Assunto: Sobre sua solicitação de acesso ao PsiPro Beta

Olá {fullName},

Agradecemos seu interesse no PsiPro. No momento, estamos 
limitando o acesso ao beta para garantir a melhor experiência.

Manteremos seu contato para futuras oportunidades.

Atenciosamente,
Equipe PsiPro
```

---

## 🚀 Implementação Rápida (Solução Temporária)

### Opção A: Email Manual
1. Formulário envia dados para um **webhook** ou **email**
2. Você recebe email com os dados
3. Você aprova manualmente criando o usuário no banco
4. Usuário recebe email com instruções

### Opção B: Planilha Google Sheets
1. Formulário envia para **Google Sheets** (via API)
2. Você monitora a planilha
3. Você aprova manualmente
4. Sistema sincroniza com banco

### Opção C: Dashboard Admin Simples
1. Criar página `/admin/beta-requests` (protegida)
2. Listar solicitações do banco
3. Botões "Aprovar" / "Rejeitar"
4. Sistema atualiza status e envia emails

---

## ✅ Checklist de Implementação

### Backend
- [ ] Criar tabela `BetaRequest` no Prisma
- [ ] Criar endpoint `POST /api/beta/request`
- [ ] Criar endpoint `GET /api/beta/requests` (admin)
- [ ] Criar endpoint `PATCH /api/beta/requests/:id` (admin)
- [ ] Criar endpoint `GET /api/auth/beta-status`
- [ ] Implementar envio de emails (SendGrid, Resend, etc.)
- [ ] Criar middleware de autenticação admin

### Frontend
- [ ] Atualizar `BetaAccessForm` para enviar para API
- [ ] Atualizar `BetaAccessGate` para verificar status real
- [ ] Criar página `/admin/beta-requests` (opcional)
- [ ] Adicionar loading states
- [ ] Adicionar tratamento de erros

### Infraestrutura
- [ ] Configurar serviço de email (SendGrid, Resend, etc.)
- [ ] Configurar variáveis de ambiente
- [ ] Criar conta de admin
- [ ] Configurar notificações

---

## 🎯 Recomendação Final

**Para Beta Fechado (Controle Total):**

1. ✅ **Implementar Opção 1 (Manual)**
2. ✅ **Admin recebe email** com cada solicitação
3. ✅ **Dashboard admin** para gerenciar solicitações
4. ✅ **Aprovação manual** garante qualidade dos usuários beta
5. ✅ **Emails automáticos** para comunicação

**Quem recebe**: Você (admin@psipro.com.br)  
**Quem autoriza**: Você (via dashboard ou email de resposta)

---

## 📝 Próximos Passos

1. **Decidir**: Manual, Semi-automático ou Automático?
2. **Implementar backend**: Endpoints e tabela no banco
3. **Configurar emails**: Serviço de email
4. **Criar dashboard admin**: (opcional, mas recomendado)
5. **Testar fluxo completo**: Solicitação → Aprovação → Acesso

---

## 💡 Dica

Para começar rápido, você pode:
1. Implementar apenas o endpoint de salvar solicitação
2. Receber emails manualmente
3. Aprovar criando usuários diretamente no banco
4. Depois evoluir para dashboard admin

**O importante é ter controle sobre quem entra no beta!**


