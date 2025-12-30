# 🏥 PsiPro para Clínicas com Múltiplos Psicólogos

## 📋 Análise da Situação Atual

### ✅ O que já funciona:
- Cada `User` (psicólogo) já tem isolamento de dados via `userId`
- Estrutura de autenticação JWT implementada
- Relacionamentos bem definidos (User → Patients → Sessions → Payments)

### ❌ O que falta para clínica:
- Modelo de **Clínica/Organização**
- **Compartilhamento de pacientes** entre psicólogos da mesma clínica
- **Roles e permissões** (Admin da clínica, Psicólogo, etc)
- **Visão consolidada** para gestão da clínica
- **Agenda compartilhada** (ver disponibilidade de todos)

---

## 🎯 Arquitetura Proposta: Multi-Tenant com Compartilhamento

### 1️⃣ Modelo de Dados (Prisma Schema)

```prisma
// Clínica / Organização
model Clinic {
  id          String   @id @default(uuid())
  name        String
  cnpj        String?  @unique
  address     String?
  phone       String?
  email       String?
  plan        String   @default("basic") // basic | professional | enterprise
  status      String   @default("active") // active | suspended | cancelled
  
  createdAt   DateTime @default(now())
  updatedAt   DateTime @updatedAt

  // Relações
  users       ClinicUser[]
  patients    Patient[]
  appointments Appointment[]
  
  @@map("clinics")
}

// Relação User ↔ Clinic (Many-to-Many com role)
model ClinicUser {
  id          String   @id @default(uuid())
  clinicId    String
  userId      String
  role        String   @default("psychologist") // owner | admin | psychologist | assistant
  status      String   @default("active") // active | inactive | invited
  
  // Permissões específicas
  canViewAllPatients Boolean @default(false)
  canEditAllPatients Boolean @default(false)
  canViewFinancial  Boolean @default(false)
  canManageUsers    Boolean @default(false)
  
  joinedAt    DateTime @default(now())
  createdAt   DateTime @default(now())
  updatedAt   DateTime @updatedAt

  // Relações
  clinic      Clinic   @relation(fields: [clinicId], references: [id], onDelete: Cascade)
  user        User     @relation(fields: [userId], references: [id], onDelete: Cascade)

  @@unique([clinicId, userId])
  @@index([clinicId])
  @@index([userId])
  @@map("clinic_users")
}

// Atualizar User para suportar clínicas múltiplas
model User {
  id        String   @id @default(uuid())
  email     String   @unique
  name      String
  password  String
  phone     String?
  cpf       String?  @unique
  license   String?  // CRP, etc
  
  // Se for psicólogo autônomo (sem clínica)
  isIndependent Boolean @default(true)
  
  createdAt DateTime @default(now())
  updatedAt DateTime @updatedAt

  // Relações
  clinics        ClinicUser[]  // Clínicas que o usuário pertence
  patients       Patient[]     // Pacientes próprios (se independente)
  appointments  Appointment[]
  sessions      Session[]
  payments      Payment[]
  financialRecords FinancialRecord[]
  documents     Document[]

  @@map("users")
}

// Atualizar Patient para suportar clínica
model Patient {
  id          String   @id @default(uuid())
  
  // Se paciente de clínica
  clinicId    String?  // Clínica dona do paciente
  clinicOwnerId String? // Psicólogo que cadastrou na clínica
  
  // Se paciente de psicólogo independente
  userId      String?  // Psicólogo dono (se independente)
  
  name        String
  cpf         String?
  phone       String?
  email       String?
  birthDate   DateTime?
  address     String?
  emergencyContact String?
  observations String?
  status      String   @default("Ativo")
  type        String?
  
  // Compartilhamento
  sharedWith  String[] // IDs dos psicólogos que podem ver/atender
  
  // Sincronização
  source      String   @default("app")
  syncHash    String?
  lastSyncedAt DateTime?
  createdAt   DateTime @default(now())
  updatedAt   DateTime @updatedAt

  // Relações
  clinic      Clinic?  @relation(fields: [clinicId], references: [id], onDelete: Cascade)
  user        User?    @relation(fields: [userId], references: [id], onDelete: Cascade)
  appointments Appointment[]
  sessions    Session[]
  payments    Payment[]
  documents   Document[]

  @@index([clinicId])
  @@index([userId])
  @@index([syncHash])
  @@map("patients")
}

// Atualizar Appointment para suportar clínica
model Appointment {
  id          String   @id @default(uuid())
  
  // Se de clínica
  clinicId    String?
  
  // Psicólogo que vai atender
  userId      String
  
  patientId   String
  scheduledAt DateTime
  duration    Int      @default(60)
  type        String?
  notes       String?
  status      String   @default("agendada")
  
  // Sincronização
  source      String   @default("app")
  syncHash    String?
  lastSyncedAt DateTime?
  createdAt   DateTime @default(now())
  updatedAt   DateTime @updatedAt

  // Relações
  clinic      Clinic?  @relation(fields: [clinicId], references: [id], onDelete: Cascade)
  user        User     @relation(fields: [userId], references: [id], onDelete: Cascade)
  patient     Patient  @relation(fields: [patientId], references: [id], onDelete: Cascade)
  session     Session?

  @@index([clinicId])
  @@index([userId])
  @@index([patientId])
  @@index([scheduledAt])
  @@map("appointments")
}

// Atualizar Clinic no schema
model Clinic {
  // ... campos acima ...
  appointments Appointment[]
}
```

---

## 🔐 Sistema de Permissões

### Roles na Clínica:

1. **Owner** (Dono)
   - Controle total da clínica
   - Pode gerenciar todos os usuários
   - Acesso a todos os dados
   - Pode deletar clínica

2. **Admin** (Administrador)
   - Pode gerenciar psicólogos e assistentes
   - Acesso a todos os pacientes da clínica
   - Acesso financeiro consolidado
   - Não pode deletar clínica

3. **Psychologist** (Psicólogo)
   - Acesso aos próprios pacientes
   - Acesso a pacientes compartilhados com ele
   - Pode criar/editar próprias sessões
   - Não vê dados de outros psicólogos

4. **Assistant** (Assistente/Secretária)
   - Pode agendar consultas
   - Pode ver agenda (sem dados clínicos)
   - Não vê anotações de sessões
   - Pode gerenciar documentos administrativos

---

## 📱 Adaptações no App Android

### 1. Tela de Seleção de Contexto
```
Ao abrir o app:
- Se usuário tem clínicas: "Trabalhar como [Clínica X]" ou "Modo Independente"
- Se só independente: direto para dashboard
```

### 2. Sincronização por Contexto
```kotlin
// SyncService.kt
class SyncService {
    suspend fun sync(context: WorkContext) {
        when (context) {
            is ClinicContext -> {
                // Sincronizar dados da clínica
                syncClinicPatients(clinicId = context.clinicId)
                syncClinicAppointments(clinicId = context.clinicId)
            }
            is IndependentContext -> {
                // Sincronizar dados próprios
                syncOwnPatients(userId = context.userId)
            }
        }
    }
}
```

### 3. Filtros na Agenda
- Ver apenas minhas consultas
- Ver consultas da clínica (se tiver permissão)
- Ver disponibilidade de outros psicólogos

---

## 💻 Adaptações na Web

### 1. Dashboard da Clínica (Admin/Owner)
```tsx
// app/clinica/dashboard/page.tsx
- Métricas consolidadas de todos os psicólogos
- Gráfico de receita total da clínica
- Lista de psicólogos ativos
- Pacientes da clínica (todos)
- Agenda consolidada
```

### 2. Gestão de Usuários
```tsx
// app/clinica/usuarios/page.tsx
- Lista de psicólogos da clínica
- Convidar novo psicólogo (por email)
- Editar permissões
- Remover usuário
```

### 3. Pacientes Compartilhados
```tsx
// app/pacientes/page.tsx
- Filtro: "Meus pacientes" | "Pacientes da clínica" | "Compartilhados comigo"
- Badge indicando se é paciente próprio ou compartilhado
- Ao criar paciente: opção "Compartilhar com clínica"
```

### 4. Agenda Compartilhada
```tsx
// app/agenda/page.tsx
- Visualização por psicólogo (filtro)
- Cores diferentes por psicólogo
- Ver disponibilidade de todos (se tiver permissão)
```

---

## 🔄 Fluxo de Trabalho

### Cenário 1: Psicólogo Autônomo
```
1. Cria conta → isIndependent = true
2. Cadastra pacientes próprios
3. Agenda consultas
4. Tudo funciona como hoje
```

### Cenário 2: Clínica Nova
```
1. Owner cria conta → cria clínica
2. Convida psicólogos por email
3. Psicólogos aceitam convite → entram na clínica
4. Owner/Admin cadastra pacientes da clínica
5. Psicólogos podem ser atribuídos a pacientes
6. Cada psicólogo vê seus pacientes + compartilhados
```

### Cenário 3: Psicólogo em Múltiplas Clínicas
```
1. Psicólogo pode pertencer a várias clínicas
2. Ao abrir app/web: escolhe contexto (Clínica A, Clínica B, ou Independente)
3. Dados isolados por contexto
4. Sincronização separada por clínica
```

---

## 🚀 Implementação por Fases

### Fase 1: Base Multi-Tenant (Sem Compartilhamento)
- ✅ Adicionar modelo `Clinic` e `ClinicUser`
- ✅ Atualizar schema do Prisma
- ✅ Migrar dados existentes (usuários → clínicas próprias)
- ✅ Isolamento de dados por clínica
- ✅ Autenticação com contexto de clínica

### Fase 2: Compartilhamento Básico
- ✅ Pacientes compartilhados na clínica
- ✅ Agenda compartilhada (visualização)
- ✅ Permissões básicas (role-based)

### Fase 3: Gestão Avançada
- ✅ Dashboard consolidado para admin
- ✅ Convites por email
- ✅ Relatórios consolidados
- ✅ Financeiro consolidado

### Fase 4: Recursos Avançados
- ✅ Múltiplas clínicas por usuário
- ✅ Transferência de pacientes
- ✅ Histórico de mudanças
- ✅ Auditoria

---

## 📊 Exemplo de Queries

### Listar pacientes da clínica (com permissão)
```typescript
// patients.service.ts
async findAll(userId: string, clinicId?: string) {
  const user = await this.getUserWithClinics(userId);
  
  if (clinicId && user.clinics.some(c => c.clinicId === clinicId)) {
    // Se tem acesso à clínica
    return this.prisma.patient.findMany({
      where: {
        OR: [
          { clinicId: clinicId },
          { userId: userId }, // Próprios pacientes
          { sharedWith: { has: userId } } // Compartilhados
        ]
      }
    });
  }
  
  // Senão, apenas próprios
  return this.prisma.patient.findMany({
    where: { userId: userId }
  });
}
```

### Criar paciente na clínica
```typescript
async create(createDto: CreatePatientDto, userId: string) {
  const user = await this.getUserWithClinics(userId);
  
  if (createDto.clinicId) {
    // Verificar se usuário pertence à clínica
    const hasAccess = user.clinics.some(
      c => c.clinicId === createDto.clinicId && c.status === 'active'
    );
    
    if (!hasAccess) {
      throw new ForbiddenException('Sem acesso a esta clínica');
    }
    
    return this.prisma.patient.create({
      data: {
        ...createDto,
        clinicId: createDto.clinicId,
        clinicOwnerId: userId
      }
    });
  }
  
  // Paciente independente
  return this.prisma.patient.create({
    data: {
      ...createDto,
      userId: userId
    }
  });
}
```

---

## ✅ Vantagens desta Arquitetura

1. **Escalável**: Suporta de 1 a 100+ psicólogos
2. **Flexível**: Psicólogo pode ser independente E de clínica
3. **Seguro**: Isolamento de dados por clínica + permissões
4. **Compatível**: Não quebra funcionalidades existentes
5. **Evolutivo**: Pode adicionar features gradualmente

---

## 🎯 Resumo

**SIM, o PsiPro pode suportar clínicas com múltiplos psicólogos!**

A arquitetura atual já tem base sólida (isolamento por userId). Com as adaptações propostas:

- ✅ Cada clínica tem seus próprios dados
- ✅ Psicólogos podem ver pacientes compartilhados
- ✅ Agenda pode ser compartilhada
- ✅ Admin vê visão consolidada
- ✅ Psicólogo independente continua funcionando
- ✅ Um psicólogo pode estar em múltiplas clínicas

**Próximo passo**: Implementar Fase 1 (base multi-tenant) quando você quiser!




