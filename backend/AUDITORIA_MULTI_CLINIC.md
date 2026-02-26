# Auditoria Completa — Arquitetura Multi-Clinic do Backend PsiPro

**Data:** 12/02/2025  
**Objetivo:** Validar configurações de segurança multi-tenant e isolamento entre clínicas.

---

## 1. Lista de Models Atuais do Prisma

| Model | Tabela | Descrição |
|-------|--------|-----------|
| **Clinic** | clinics | Clínica / Organização |
| **ClinicUser** | clinic_users | Pivot User ↔ Clinic (many-to-many com role) |
| **User** | users | Usuário profissional (psicólogo) |
| **Patient** | patients | Paciente |
| **Appointment** | appointments | Agendamento / Consulta |
| **Session** | sessions | Sessão realizada |
| **Payment** | payments | Pagamento |
| **FinancialRecord** | financial_records | Registro financeiro consolidado |
| **Document** | documents | Documento / Arquivo |
| **Insight** | insights | Insight gerado |
| **PatientOrigin** | (enum) | ANDROID \| WEB |

---

## 2. Verificação 1 — Model Clinic

**Status:** ✅ Existe

- `Clinic` com campos: id, name, cnpj, address, phone, email, plan, status
- Relações: `users` (ClinicUser[]), `patients`, `appointments`

---

## 3. Verificação 2 — Model ClinicUser (Pivot)

**Status:** ✅ Existe

- `ClinicUser` com: clinicId, userId, role, status
- Permissões: `canViewAllPatients`, `canEditAllPatients`, `canViewFinancial`, `canManageUsers`
- `@@unique([clinicId, userId])`
- Relações: `clinic`, `user` com `@relation` e `onDelete: Cascade`

---

## 4. Verificação 3 — Campo clinicId nas Entidades

| Entidade | clinicId | Relação @relation |
|----------|----------|-------------------|
| **Patient** | ✅ `clinicId String?` | ✅ `clinic Clinic? @relation(...)` |
| **Appointment** | ✅ `clinicId String?` | ✅ `clinic Clinic? @relation(...)` |
| **Session** | ❌ **NÃO possui** | — |
| **Payment** | ❌ **NÃO possui** | — |
| **FinancialRecord** | ❌ **NÃO possui** | — |
| **Document** | ❌ **NÃO possui** | — |
| **Insight** | ❌ **NÃO possui** | — |

---

## 5. Verificação 4 — clinicId com @relation

**Status:** ✅ Onde existe clinicId, a relação está correta

- `Patient.clinic` → `@relation(fields: [clinicId], references: [id], onDelete: Cascade)`
- `Appointment.clinic` → idem

---

## 6. Verificação 5 — Middleware/Guard/Interceptor X-Clinic-Id

**Status:** ❌ **NÃO EXISTE**

- Nenhum middleware, guard ou interceptor lê o header `X-Clinic-Id`
- Nenhum componente injeta `clinicId` automaticamente no request
- `clinicId` é passado **apenas via query string** (`?clinicId=xxx`) em alguns endpoints:
  - `GET /patients?clinicId=`
  - `GET /appointments?clinicId=`
  - `GET /sessions?clinicId=`
  - Sync: `clinicId` na query
- **Não há validação centralizada** de pertencimento do usuário à clínica antes do processamento

---

## 7. Verificação 6 — Filtro por clinicId nos Endpoints

| Módulo | Endpoint(s) | Filtra por clinicId? | Observação |
|--------|-------------|----------------------|------------|
| **Patients** | GET /, GET /:id, POST, PATCH, import | ✅ Sim | `findAll`, `findOne`, `create`, `update` usam `hasAccessToPatient` ou `getUserClinics` |
| **Appointments** | GET / | ✅ Sim (opcional) | `clinicId` via query; valida `ClinicUser` |
| **Sessions** | GET /, GET (byPatient), POST | ⚠️ Parcial | `findAll` e `findByPatient` consideram clínica; `create` **não** |
| **Payments** | POST, GET patient/:id | ❌ **Não** | Usa apenas `userId` e `patient.userId` |
| **Financial** | GET summary, GET patient/:id | ❌ **Não** | Usa apenas `userId`; paciente valida `patient.userId` |
| **Documents** | GET / | ❌ **Não** | Filtra só por `userId` |
| **Insights** | GET /, PATCH/:id | ❌ **Não** | Filtra só por `userId` |
| **Clinics** | CRUD, invite, etc. | ✅ Sim | Valida `ClinicUser` |
| **Sync** | POST/GET patients | ✅ Sim | `resolveClinicId` e validação de acesso |

---

## 8. Verificação 7 — Proteção Contra Acesso Cruzado

**Análise por módulo:**

### PatientsService
- ✅ `hasAccessToPatient`: considera `userId`, `clinicId` (via ClinicUser), `sharedWith`
- ✅ `findAll`: combina pacientes próprios, da clínica e compartilhados
- ✅ `create`/`update`: valida acesso à clínica quando `clinicId` é informado

### AppointmentsService
- ✅ `findAll`: valida `ClinicUser` quando `clinicId` é passado
- ⚠️ Sem `findOne`/create/update no controller analisado

### SessionsService
- ✅ `findByPatient`: usa `hasClinicAccess` e `sharedWith`
- ⚠️ `hasClinicAccess` exige `canViewAllPatients`; não considera `sharedWith` para clínica
- ❌ **Vulnerável** `create`: valida apenas `patient.userId === userId`, **bloqueando** psicólogos da clínica

### PaymentsService
- ❌ **Vulnerável**: `create` e `findByPatient` checam só `patient.userId === userId`
- ❌ Psicólogos da clínica **não** podem criar/listar pagamentos de pacientes da clínica

### FinancialService
- ❌ **Vulnerável**: `getPatientFinancial` checa `patient.userId !== userId`
- ❌ `getSummary`: agregado apenas por `userId`, sem contexto de clínica

### DocumentsService
- ❌ Filtra só por `userId`; sem `clinicId`
- ❌ Documentos de pacientes de clínica não ficam acessíveis a outros membros da clínica

### InsightsService
- ❌ Filtra só por `userId`; modelo sem `clinicId`
- ❌ Sem suporte multi-clínica

---

## 9. Pontos Vulneráveis Encontrados

### Críticos

1. **Payments**
   - `create` e `findByPatient` validam apenas `patient.userId === userId`
   - Membros da clínica não conseguem criar ou listar pagamentos de pacientes da clínica
   - Modelo `Payment` sem `clinicId` impede filtragem por clínica

2. **Sessions.create**
   - Mesma lógica: só permite quando `patient.userId === userId`
   - Impede criação de sessões por outros membros da clínica para pacientes compartilhados

3. **Financial**
   - `getPatientFinancial` bloqueia acesso de membros da clínica
   - `getSummary` não considera `clinicId`; visão apenas individual

4. **Ausência de X-Clinic-Id**
   - Nenhum mecanismo centralizado de contexto de clínica
   - Cliente pode omitir `clinicId` e obter dados misturados de várias clínicas

### Moderados

5. **Session**
   - Modelo sem `clinicId`; isolamento via `patient.clinicId` indiretamente
   - `hasClinicAccess` exige `canViewAllPatients`; não cobre `sharedWith` no fluxo de clínica

6. **Documents**
   - Sem `clinicId`; filtro só por `userId`
   - Documentos de clínica não visíveis para outros membros autorizados

7. **FinancialRecord**
   - Sem `clinicId`; agregados apenas por `userId`
   - Sem visão financeira por clínica

8. **Insight**
   - Sem `clinicId`; insights só por usuário
   - Sem visão por clínica

### Menores

9. **clinicId opcional**
   - Quando omitido, retorna dados de **todas** as clínicas do usuário
   - Não há exigência de contexto de clínica

---

## 10. O Que Está Correto

1. **Prisma**: modelos `Clinic` e `ClinicUser` bem definidos
2. **Patient**: `clinicId` com relação; `PatientsService` valida acesso (próprio, clínica, sharedWith)
3. **Appointment**: `clinicId` com relação; `findAll` valida `ClinicUser`
4. **ClinicPermissionsHelper**: helpers para permissões por clínica
5. **Sync**: `resolveClinicId` e validação de acesso; proteção contra `clinicId` inválido
6. **Clinics**: CRUD e gestão de membros validam `ClinicUser`
7. **PatientsService**: `hasAccessToPatient` cobre cenários principal/clínica/compartilhado
8. **SessionsService**: `findByPatient` e `findAll` consideram clínica em leitura (com ressalvas em `create`)

---

## 11. O Que Precisa Ser Implementado

### Prioridade Alta

1. **Header X-Clinic-Id (ou equivalente)**
   - Guard ou interceptor que leia o header
   - Injeção de `clinicId` no request (ex.: decorator)
   - Validação de pertencimento via `ClinicUser`

2. **Payments**
   - Incluir `clinicId` no model `Payment` (ou derivação via session → appointment → clinic)
   - Validar acesso como em `PatientsService`: próprio, clínica (ClinicUser) ou `sharedWith`
   - Atualizar `create` e `findByPatient` para usar essa lógica

3. **Sessions.create**
   - Trocar `patient.userId !== userId` por `hasAccessToPatient` ou equivalente
   - Permitir criação quando o usuário tem acesso ao paciente (clínica ou sharedWith)

4. **Financial**
   - `getPatientFinancial`: usar lógica de acesso ao paciente (clínica + sharedWith)
   - `getSummary`: suportar filtro por `clinicId` e agregar por clínica

### Prioridade Média

5. **Adicionar clinicId onde faz sentido**
   - `Session` (opcional, derivado de appointment/patient)
   - `Payment` (derivado de session/patient)
   - `FinancialRecord` (para visão por clínica)
   - `Document` (para isolamento por clínica)
   - `Insight` (para contexto de clínica)

6. **Documents**
   - Ajustar acesso para considerar `patient.clinicId` e `ClinicUser`
   - Permitir acesso a documentos de pacientes da clínica conforme permissões

7. **Guard de contexto de clínica**
   - Garantir `clinicId` quando o recurso for de clínica
   - Bloquear acesso cruzado quando `clinicId` conflitar com o recurso

### Prioridade Baixa

8. **Política de clinicId obrigatório**
   - Para recursos de clínica, exigir `clinicId` (header ou query)
   - Retornar 400 quando omitido em contextos que exigem clínica

---

## 12. Nível de Segurança Multi-Tenant

**Nota: 5/10**

### Justificativa

- **Modelagem (2/2):** Clinic, ClinicUser e relações principais corretas
- **Patient (2/2):** Isolamento bem implementado
- **Appointment (1.5/2):** Leitura por clínica ok; faltam outros métodos com mesma proteção
- **Session (1/2):** Leitura ok; `create` vulnerável
- **Payment (0/2):** Sem clinicId; validação inadequada
- **Financial (0/2):** Sem suporte multi-clínica
- **Documents (0/2):** Sem suporte
- **Insights (0/2):** Sem suporte
- **Contexto centralizado (0/2):** Sem header X-Clinic-Id, sem guard/interceptor
- **Proteção cruzada (1/2):** Parcial em Patient/Appointment; ausente em Payment/Financial/Documents

### Resumo

A base multi-clínica em Patient e Appointment está sólida, mas Payment, Session (create), Financial, Documents e Insights carecem de suporte e validação adequados. A ausência de um contexto de clínica centralizado (ex.: X-Clinic-Id) e de validações consistentes em todos os módulos aumenta o risco de acesso indevido entre clínicas.

---

*Auditoria realizada sem alteração de código. Recomenda-se implementar os itens da seção "O que precisa ser implementado" em ordem de prioridade.*
