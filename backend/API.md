# PsiPro API - Documentação

API única para sincronização entre App Android e Web.

## Base URL

```
http://localhost:3001/api
```

## Autenticação

Todos os endpoints (exceto `/auth/login`) requerem um token JWT no header:

```
Authorization: Bearer <token>
```

## Endpoints

### Autenticação

#### POST /auth/login
Login e obtenção de token.

**Request:**
```json
{
  "email": "psicologo@psipro.com",
  "password": "senha123"
}
```

**Response:**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "uuid",
    "email": "psicologo@psipro.com",
    "name": "Psicólogo Exemplo"
  }
}
```

#### GET /auth/me
Obter dados do usuário autenticado.

**Headers:**
```
Authorization: Bearer <token>
```

---

### Pacientes

#### GET /patients
Listar todos os pacientes do usuário.

**Response:**
```json
[
  {
    "id": "uuid",
    "name": "Maria Silva Santos",
    "cpf": "123.456.789-00",
    "phone": "(11) 98765-4321",
    "email": "maria@email.com",
    "status": "Ativo",
    "type": "Adulto",
    "createdAt": "2025-01-01T00:00:00.000Z",
    "updatedAt": "2025-01-01T00:00:00.000Z"
  }
]
```

#### POST /patients
Criar novo paciente.

**Request:**
```json
{
  "name": "João Silva",
  "phone": "(11) 98765-4321",
  "email": "joao@email.com",
  "source": "web"
}
```

#### GET /patients/:id
Obter detalhes de um paciente.

**Response:**
```json
{
  "id": "uuid",
  "name": "Maria Silva Santos",
  "sessions": [...],
  "payments": [...]
}
```

#### PATCH /patients/:id
Atualizar paciente.

---

### Sessões

#### POST /sessions
Registrar sessão realizada.

**Request:**
```json
{
  "patientId": "uuid",
  "date": "2025-03-15T14:00:00.000Z",
  "duration": 60,
  "status": "realizada",
  "notes": "Sessão focada em técnicas de relaxamento...",
  "source": "app"
}
```

#### GET /sessions
Listar todas as sessões.

#### GET /sessions?patientId=uuid
Listar sessões de um paciente específico.

---

### Pagamentos

#### POST /payments
Registrar pagamento.

**Request:**
```json
{
  "patientId": "uuid",
  "amount": 150.00,
  "date": "2025-03-15T14:00:00.000Z",
  "method": "PIX",
  "status": "pago",
  "sessionId": "uuid",
  "source": "app"
}
```

#### GET /payments/patient/:patientId
Listar pagamentos de um paciente.

---

### Financeiro

#### GET /financial/summary
Resumo financeiro geral.

**Response:**
```json
{
  "receitaHoje": 450.00,
  "receitaMes": 6300.00,
  "totalRecebido": 18900.00,
  "totalAReceber": 1200.00,
  "ticketMedio": 150.00
}
```

#### GET /financial/patient/:patientId
Resumo financeiro do paciente.

**Response:**
```json
{
  "totalFaturado": 1800.00,
  "totalRecebido": 1500.00,
  "totalAberto": 300.00
}
```

---

### Agendamentos

#### GET /appointments
Listar todos os agendamentos.

---

### Documentos

#### GET /documents
Listar documentos.

**Query params:**
- `patientId` (opcional): Filtrar por paciente

---

### Insights

#### GET /insights
Listar insights não descartados.

#### PATCH /insights/:id/dismiss
Descartar um insight.

---

## Sincronização

### Fluxo do App Android

1. App registra ação localmente (Room)
2. App envia para API em background
3. API persiste e retorna confirmação
4. App marca como sincronizado

### Resolução de Conflitos

- Conflitos resolvidos por `updatedAt` (última atualização vence)
- Campo `syncHash` para detectar mudanças
- Campo `source` indica origem (app | web)

---

## Códigos de Status

- `200` - Sucesso
- `201` - Criado
- `400` - Requisição inválida
- `401` - Não autenticado
- `403` - Acesso negado
- `404` - Não encontrado
- `500` - Erro interno

---

## Exemplo de Uso

```typescript
// Login
const response = await fetch('http://localhost:3001/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    email: 'psicologo@psipro.com',
    password: 'senha123'
  })
});

const { access_token } = await response.json();

// Listar pacientes
const patients = await fetch('http://localhost:3001/api/patients', {
  headers: {
    'Authorization': `Bearer ${access_token}`
  }
});
```




