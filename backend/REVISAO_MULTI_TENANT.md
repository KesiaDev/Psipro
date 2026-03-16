# Revisão Multi-Tenant — Relatório Final

## Objetivo
Garantir isolamento multi-tenant absoluto em todos os endpoints protegidos do NestJS.

## Endpoints Revisados

### Auth (`/auth`)
| Endpoint | Guard | Roles | clinicId | Observação |
|----------|-------|-------|---------|------------|
| POST /login | - | - | - | Público |
| POST /logout | - | - | - | Público (valida refresh) |
| POST /refresh | - | - | - | Público |
| POST /register | - | - | - | Público |
| POST /handoff | - | - | - | Público (valida token) |
| POST /switch-clinic | JwtAuthGuard | - | - | Valida pertencimento via ClinicUser |
| GET /me | JwtAuthGuard | - | - | Retorna user + clinicId do JWT |

### Clinics (`/clinics`)
| Endpoint | Guard | Roles | clinicId | Observação |
|----------|-------|-------|---------|------------|
| POST / | JwtAuthGuard, RolesGuard | admin | - | Cria clínica |
| GET / | JwtAuthGuard, RolesGuard | admin | - | Lista clínicas do user (ClinicUser) |
| GET /:id | JwtAuthGuard, RolesGuard | admin | params.id | Valida ClinicUser |
| PUT /:id | JwtAuthGuard, RolesGuard | admin | params.id | assertCanManageClinic |
| DELETE /:id | JwtAuthGuard, RolesGuard | admin | params.id | assertCanManageClinic |
| POST /:id/invite | JwtAuthGuard, RolesGuard | admin | params.id | canManageUsers |
| PUT /:id/users/:userId | JwtAuthGuard, RolesGuard | admin | params.id | canManageUsers |
| DELETE /:id/users/:userId | JwtAuthGuard, RolesGuard | admin | params.id | canManageUsers |
| GET /:id/stats | JwtAuthGuard, RolesGuard | admin | params.id | Filtra por clinicId em todas queries |

### Patients (`/patients`)
| Endpoint | Guard | Roles | clinicId | Observação |
|----------|-------|-------|---------|------------|
| GET / | JwtAuthGuard, ClinicGuard, RolesGuard | admin, psychologist | CurrentClinicId | where: { clinicId } |
| GET /:id | JwtAuthGuard, ClinicGuard, RolesGuard | admin, psychologist | CurrentClinicId | where: { id, clinicId } |
| POST / | JwtAuthGuard, ClinicGuard, RolesGuard | admin, psychologist | CurrentClinicId | clinicId obrigatório |
| POST /import | JwtAuthGuard, ClinicGuard, RolesGuard | admin, psychologist | CurrentClinicId | clinicId obrigatório |
| PATCH /:id | JwtAuthGuard, ClinicGuard, RolesGuard | admin, psychologist | CurrentClinicId | where: { id, clinicId } |
| DELETE /:id | JwtAuthGuard, ClinicGuard, RolesGuard | admin, psychologist | CurrentClinicId | deleteMany where: { id, clinicId } |

### Appointments (`/appointments`) — **CORRIGIDO**
| Endpoint | Guard | Roles | clinicId | Observação |
|----------|-------|-------|---------|------------|
| GET / | JwtAuthGuard, ClinicGuard, RolesGuard | admin, psychologist | CurrentClinicId | where: { clinicId } + canViewAllPatients |
| GET /:id | JwtAuthGuard, ClinicGuard, RolesGuard | admin, psychologist | CurrentClinicId | where: { id, userId, clinicId } |
| POST / | JwtAuthGuard, ClinicGuard, RolesGuard | admin, psychologist | CurrentClinicId | Valida patient.clinicId, checkScheduleConflict com clinicId |
| PUT /:id | JwtAuthGuard, ClinicGuard, RolesGuard | admin, psychologist | CurrentClinicId | where: { id, userId, clinicId } |
| DELETE /:id | JwtAuthGuard, ClinicGuard, RolesGuard | admin, psychologist | CurrentClinicId | where: { id, userId, clinicId } |

### Financial (`/financial`) — **CORRIGIDO**
| Endpoint | Guard | Roles | clinicId | Observação |
|----------|-------|-------|---------|------------|
| GET /summary | JwtAuthGuard, ClinicGuard, RolesGuard | admin | CurrentClinicId | Payment: userId + clinicId; Session: patient.clinicId |
| GET /patient/:patientId | JwtAuthGuard, ClinicGuard, RolesGuard | admin | CurrentClinicId | Patient: id + clinicId; Payment: clinicId |

### Payments (`/payments`) — **CORRIGIDO**
| Endpoint | Guard | Roles | clinicId | Observação |
|----------|-------|-------|---------|------------|
| POST / | JwtAuthGuard, ClinicGuard, RolesGuard | admin, psychologist | CurrentClinicId | Patient: id + clinicId; Payment: clinicId |
| GET /patient/:patientId | JwtAuthGuard, ClinicGuard, RolesGuard | admin, psychologist | CurrentClinicId | Patient: id + clinicId; Payment: clinicId |

### Sessions (`/sessions`) — **CORRIGIDO**
| Endpoint | Guard | Roles | clinicId | Observação |
|----------|-------|-------|---------|------------|
| GET / | JwtAuthGuard, ClinicGuard, RolesGuard | admin, psychologist | CurrentClinicId | where: { patient: { clinicId } } |
| GET /?patientId= | JwtAuthGuard, ClinicGuard, RolesGuard | admin, psychologist | CurrentClinicId | Patient: id + clinicId |
| POST / | JwtAuthGuard, ClinicGuard, RolesGuard | admin, psychologist | CurrentClinicId | Patient: id + clinicId |

### Documents (`/documents`) — **CORRIGIDO**
| Endpoint | Guard | Roles | clinicId | Observação |
|----------|-------|-------|---------|------------|
| GET / | JwtAuthGuard, ClinicGuard, RolesGuard | admin, psychologist, assistant | CurrentClinicId | where: { userId, patient: { clinicId } } |

### Dashboard (`/dashboard`)
| Endpoint | Guard | Roles | clinicId | Observação |
|----------|-------|-------|---------|------------|
| GET /metrics | JwtAuthGuard, ClinicGuard, RolesGuard | admin, psychologist | CurrentClinicId | Todas queries com clinicId |
| GET /agenda-summary | JwtAuthGuard, ClinicGuard, RolesGuard | admin, psychologist | CurrentClinicId | where: { clinicId } |
| GET /finance-summary | JwtAuthGuard, ClinicGuard, RolesGuard | admin, psychologist | CurrentClinicId | where: { clinicId } |

### Insights (`/insights`) — **CORRIGIDO**
| Endpoint | Guard | Roles | clinicId | Observação |
|----------|-------|-------|---------|------------|
| GET / | JwtAuthGuard, ClinicGuard, RolesGuard | admin, psychologist | CurrentClinicId | Insight: userId (modelo sem clinicId) |
| GET /summary | JwtAuthGuard, ClinicGuard, RolesGuard | admin, psychologist | CurrentClinicId | collectInsightData filtra Session, Appointment, Payment, Patient por clinicId |
| PATCH /:id/dismiss | JwtAuthGuard, ClinicGuard, RolesGuard | admin, psychologist | CurrentClinicId | Insight: userId |

### Sync (`/sync`)
| Endpoint | Guard | Roles | clinicId | Observação |
|----------|-------|-------|---------|------------|
| GET /patients | JwtAuthGuard, ClinicGuard, RolesGuard | admin, psychologist | CurrentClinicId | where: { clinicId } |
| POST /patients | JwtAuthGuard, ClinicGuard, RolesGuard | admin, psychologist | CurrentClinicId | findFirst { id, clinicId } — **CORRIGIDO** |

---

## Correções Aplicadas

### 1. Appointments
- **Antes:** Apenas JwtAuthGuard; clinicId opcional via query; queries por userId.
- **Depois:** ClinicGuard + RolesGuard (admin, psychologist); clinicId obrigatório via x-clinic-id; todas as queries com `clinicId`; validação de patient.clinicId no create; checkScheduleConflict filtra por clinicId.

### 2. Financial
- **Antes:** Apenas JwtAuthGuard + RolesGuard; queries apenas por userId.
- **Depois:** ClinicGuard adicionado; getSummary e getPatientFinancial recebem clinicId; todas as queries Payment/Patient com `clinicId` ou `patient.clinicId`.

### 3. Payments
- **Antes:** Apenas JwtAuthGuard; clinicId opcional; findByPatient sem filtro clinicId.
- **Depois:** ClinicGuard + RolesGuard (admin, psychologist); clinicId obrigatório; create valida patient.clinicId; findByPatient valida patient e filtra Payment por clinicId.

### 4. Sessions
- **Antes:** Apenas JwtAuthGuard; clinicId opcional via query; findAll/session sem filtro clinicId obrigatório.
- **Depois:** ClinicGuard + RolesGuard (admin, psychologist); clinicId obrigatório; findAll filtra por `patient.clinicId`; findByPatient e create validam patient.clinicId.

### 5. Documents
- **Antes:** Apenas JwtAuthGuard; queries por userId e patientId.
- **Depois:** ClinicGuard + RolesGuard (admin, psychologist, assistant); where: `patient: { clinicId }`.

### 6. Insights
- **Antes:** Apenas JwtAuthGuard; collectInsightData sem filtro clinicId.
- **Depois:** ClinicGuard + RolesGuard (admin, psychologist); collectInsightData filtra Session, Appointment, Payment, Patient por `clinicId` ou `patient.clinicId`.

### 7. Sync
- **Antes:** findUnique sem clinicId no where (risco de cross-tenant).
- **Depois:** findFirst com `where: { id: p.id, clinicId }` para garantir isolamento.
- **RolesGuard** adicionado (admin, psychologist).

### 8. Dashboard
- **RolesGuard** adicionado (admin, psychologist). Já utilizava ClinicGuard e clinicId em todas as queries.

---

## Requisito: Header x-clinic-id

Todos os endpoints que usam **ClinicGuard** exigem o header `x-clinic-id` em toda requisição. O cliente (Web/Android) deve enviar a clínica ativa do usuário.

Sem o header, o endpoint retorna **400 Bad Request**:
```
Header x-clinic-id é obrigatório para este endpoint
```

---

## Resumo de Roles por Recurso

| Recurso | Roles |
|---------|-------|
| /clinics | admin |
| /patients | admin, psychologist |
| /appointments | admin, psychologist |
| /financial | admin |
| /payments | admin, psychologist |
| /sessions | admin, psychologist |
| /documents | admin, psychologist, assistant |
| /dashboard | admin, psychologist |
| /insights | admin, psychologist |
| /sync | admin, psychologist |
