# 💰 DOCUMENTAÇÃO COMPLETA - SISTEMA FINANCEIRO DO PSIPRO

**Data:** Dezembro 2024  
**Versão:** 1.0.0  
**Status:** ✅ Funcional e Completo

---

## 📋 ÍNDICE

1. [Visão Geral do Sistema Financeiro](#visão-geral)
2. [Entidades e Estrutura de Dados](#entidades)
3. [Sistema de Cobranças de Sessão](#cobrancas-sessao)
4. [Sistema de Cobranças de Agendamento](#cobrancas-agendamento)
5. [Registros Financeiros Gerais](#registros-financeiros)
6. [Dashboard Financeiro Geral](#dashboard-geral)
7. [Dashboard Financeiro por Paciente](#dashboard-paciente)
8. [Funcionalidades Implementadas](#funcionalidades)
9. [Fluxos de Trabalho](#fluxos)
10. [Integrações e Automações](#integracoes)

---

## 🎯 VISÃO GERAL DO SISTEMA FINANCEIRO {#visão-geral}

O Psipro possui **3 sistemas financeiros integrados** que trabalham em conjunto:

### 1. **CobrancaSessao** (Cobranças de Sessões)
- ✅ Criado **AUTOMATICAMENTE** ao registrar uma sessão para um paciente
- ✅ Vinculado à anotação de sessão (`AnotacaoSessao`)
- ✅ Gerencia pagamentos de sessões realizadas
- ✅ Status: A_RECEBER, PAGO, VENCIDO, CANCELADO

### 2. **CobrancaAgendamento** (Cobranças de Agendamentos)
- ✅ Criado **AUTOMATICAMENTE** ao criar um agendamento
- ✅ Vinculado ao agendamento (`Appointment`)
- ✅ Gerencia pagamentos de agendamentos futuros
- ✅ Motivos: AGENDAMENTO, CANCELAMENTO, FALTA

### 3. **FinancialRecord** (Registros Financeiros Gerais)
- ✅ Receitas e despesas gerais do consultório
- ✅ Não vinculado necessariamente a pacientes
- ✅ Tipos: RECEITA, DESPESA
- ✅ Usado para controle financeiro geral

---

## 📊 ENTIDADES E ESTRUTURA DE DADOS {#entidades}

### 1. CobrancaSessao

**Localização:** `app/src/main/java/com/psipro/app/data/entities/CobrancaSessao.kt`

```kotlin
@Entity(tableName = "cobrancas_sessao")
data class CobrancaSessao(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: Long,                    // ID do paciente
    val anotacaoSessaoId: Long,             // ID da anotação de sessão
    val numeroSessao: Int,                  // Número sequencial da sessão
    val valor: Double,                      // Valor da sessão
    val dataSessao: Date,                   // Data em que a sessão foi realizada
    val dataVencimento: Date,               // Data de vencimento (7 dias após sessão)
    val dataPagamento: Date? = null,       // Data do pagamento (se pago)
    val status: StatusPagamento,            // Status do pagamento
    val observacoes: String = "",           // Observações adicionais
    val pixCopiaCola: String = "",          // Chave PIX para pagamento
    val tipoSessaoId: Long? = null,         // Tipo de sessão (Individual, Casal, etc.)
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)
```

**Status de Pagamento:**
```kotlin
enum class StatusPagamento {
    PAGO,           // Pagamento recebido
    A_RECEBER,      // Aguardando pagamento
    VENCIDO,        // Pagamento vencido
    CANCELADO       // Cobrança cancelada
}
```

**Relacionamentos:**
- `patientId` → `Patient` (ForeignKey CASCADE)
- `anotacaoSessaoId` → `AnotacaoSessao` (ForeignKey CASCADE)

---

### 2. CobrancaAgendamento

**Localização:** `app/src/main/java/com/psipro/app/data/entities/CobrancaAgendamento.kt`

```kotlin
@Entity(tableName = "cobrancas_agendamento")
data class CobrancaAgendamento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: Long,                    // ID do paciente
    val appointmentId: Long,                // ID do agendamento
    val valor: Double,                      // Valor do agendamento
    val dataAgendamento: Date,              // Data do agendamento
    val dataVencimento: Date,               // Data de vencimento
    val dataPagamento: Date? = null,       // Data do pagamento (se pago)
    val status: StatusPagamento,            // Status do pagamento
    val motivo: String,                     // "AGENDAMENTO", "CANCELAMENTO", "FALTA"
    val observacoes: String = "",           // Observações adicionais
    val pixCopiaCola: String = "",          // Chave PIX para pagamento
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)
```

**Relacionamentos:**
- `patientId` → `Patient` (ForeignKey CASCADE)
- `appointmentId` → `Appointment` (ForeignKey CASCADE)

---

### 3. FinancialRecord

**Localização:** `app/src/main/java/com/psipro/app/data/entities/FinancialRecord.kt`

```kotlin
@Entity(tableName = "financial_records")
data class FinancialRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: Long? = null,           // Opcional: pode estar vinculado a um paciente
    val description: String,                // Descrição da receita/despesa
    val value: Double,                      // Valor
    val type: String,                       // "RECEITA" ou "DESPESA"
    val date: Date = Date()                 // Data do registro
)
```

---

## 💳 SISTEMA DE COBRANÇAS DE SESSÃO {#cobrancas-sessao}

### Criação Automática

**Quando:** Ao registrar uma anotação de sessão para um paciente

**Localização do Código:**
- `app/src/main/java/com/psipro/app/ui/viewmodels/AnotacaoSessaoViewModel.kt`

**Fluxo:**
1. Usuário preenche os dados da sessão (tipo, valor, assuntos, etc.)
2. Ao salvar a anotação de sessão, o sistema:
   - Cria a `AnotacaoSessao`
   - **Automaticamente cria** a `CobrancaSessao` vinculada
   - Define o valor da sessão (do paciente ou informado manualmente)
   - Define data de vencimento (7 dias após a sessão)
   - Status inicial: `A_RECEBER`

### Campos Importantes

- **Valor da Sessão:** Obtido do campo `sessionValue` do paciente ou informado manualmente
- **Data de Vencimento:** Calculada automaticamente (7 dias após a data da sessão)
- **Número da Sessão:** Sequencial automático baseado nas sessões anteriores do paciente
- **Status Inicial:** Sempre `A_RECEBER` ao criar

### DAO - CobrancaSessaoDao

**Localização:** `app/src/main/java/com/psipro/app/data/dao/CobrancaSessaoDao.kt`

**Queries Principais:**
```kotlin
// Buscar por paciente
fun getByPatientId(patientId: Long): Flow<List<CobrancaSessao>>

// Buscar por status
fun getByStatus(status: StatusPagamento): Flow<List<CobrancaSessao>>

// Buscar por período
fun getByPeriodo(dataInicio: Date, dataFim: Date): Flow<List<CobrancaSessao>>

// Buscar vencidas
fun getVencidas(dataAtual: Date): Flow<List<CobrancaSessao>>

// Estatísticas
suspend fun getTotalRecebidoGeral(): Double?
suspend fun getTotalAReceber(): Double?
suspend fun getCountPendentes(): Int
suspend fun getCountByStatus(status: StatusPagamento): Int

// Operações
suspend fun marcarComoPago(cobrancaId: Long, status: StatusPagamento, dataPagamento: Date?)
suspend fun desmarcarComoPago(cobrancaId: Long, status: StatusPagamento)
suspend fun editarCobranca(cobrancaId: Long, valor: Double, observacoes: String)
```

### Repository - CobrancaSessaoRepository

**Localização:** `app/src/main/java/com/psipro/app/data/repository/CobrancaSessaoRepository.kt`

**Funcionalidades:**
- Encapsula acesso ao DAO
- Lógica de negócio para cobranças
- Validações e transformações de dados

### ViewModel - CobrancaSessaoViewModel

**Localização:** `app/src/main/java/com/psipro/app/ui/viewmodels/CobrancaSessaoViewModel.kt`

**Funcionalidades:**
- Gerenciamento de estado das cobranças
- Cálculo de resumos financeiros por paciente
- Operações de marcar/desmarcar como pago
- Edição de cobranças

---

## 📅 SISTEMA DE COBRANÇAS DE AGENDAMENTO {#cobrancas-agendamento}

### Criação Automática

**Quando:** Ao criar um agendamento para um paciente

**Localização do Código:**
- `app/src/main/java/com/psipro/app/ui/AppointmentScheduleActivity.kt`

**Fluxo:**
1. Usuário cria um agendamento
2. Sistema verifica se o paciente tem valor de sessão configurado
3. **Automaticamente cria** a `CobrancaAgendamento` vinculada
4. Define valor baseado no `sessionValue` do paciente
5. Define data de vencimento
6. Status inicial: `A_RECEBER`
7. Motivo: "AGENDAMENTO"

### Motivos de Cobrança

- **AGENDAMENTO:** Cobrança gerada ao criar agendamento
- **CANCELAMENTO:** Cobrança gerada ao cancelar agendamento (se aplicável)
- **FALTA:** Cobrança gerada quando paciente falta ao agendamento

### DAO - CobrancaAgendamentoDao

**Localização:** `app/src/main/java/com/psipro/app/data/dao/CobrancaAgendamentoDao.kt`

**Queries Principais:**
```kotlin
// Buscar por paciente
fun getByPatientId(patientId: Long): Flow<List<CobrancaAgendamento>>

// Buscar por agendamento
fun getByAppointmentId(appointmentId: Long): Flow<CobrancaAgendamento?>

// Buscar por status
fun getByStatus(status: StatusPagamento): Flow<List<CobrancaAgendamento>>

// Operações
suspend fun marcarComoPago(cobrancaId: Long, status: StatusPagamento, dataPagamento: Date?)
```

### Repository - CobrancaAgendamentoRepository

**Localização:** `app/src/main/java/com/psipro/app/data/repository/CobrancaAgendamentoRepository.kt`

### ViewModel - CobrancaAgendamentoViewModel

**Localização:** `app/src/main/java/com/psipro/app/ui/viewmodels/CobrancaAgendamentoViewModel.kt`

---

## 📝 REGISTROS FINANCEIROS GERAIS {#registros-financeiros}

### Propósito

Registros financeiros gerais para controle de receitas e despesas do consultório que não estão necessariamente vinculados a pacientes ou sessões específicas.

**Exemplos:**
- Receitas: Vendas de materiais, cursos, etc.
- Despesas: Aluguel, materiais, equipamentos, etc.

### DAO - FinancialRecordDao

**Localização:** `app/src/main/java/com/psipro/app/data/dao/FinancialRecordDao.kt`

**Queries:**
```kotlin
// Buscar todos
fun getAllRecords(): Flow<List<FinancialRecord>>

// Buscar por paciente (opcional)
fun getByPatient(patientId: Long): Flow<List<FinancialRecord>>

// Operações CRUD
suspend fun insertRecord(record: FinancialRecord): Long
suspend fun updateRecord(record: FinancialRecord)
suspend fun deleteRecord(record: FinancialRecord)
```

### Repository - FinancialRecordRepository

**Localização:** `app/src/main/java/com/psipro/app/data/repository/FinancialRecordRepository.kt`

---

## 📊 DASHBOARD FINANCEIRO GERAL {#dashboard-geral}

### Tela Principal

**Localização:** `app/src/main/java/com/psipro/app/ui/screens/FinanceiroDashboardScreen.kt`

**Activity:** `app/src/main/java/com/psipro/app/ui/screens/FinanceiroDashboardActivity.kt`

### ViewModel - FinanceiroUnificadoViewModel

**Localização:** `app/src/main/java/com/psipro/app/ui/viewmodels/FinanceiroUnificadoViewModel.kt`

**Funcionalidades:**

#### 1. Carregamento de Dados
```kotlin
fun carregarDadosFinanceiros()
// Carrega todas as cobranças e registros financeiros
// Calcula resumo unificado
```

#### 2. Filtros e Consultas
```kotlin
fun carregarDadosPorPeriodo(dataInicio: Date, dataFim: Date)
fun carregarDadosPorStatus(status: StatusPagamento)
fun carregarCobrancasVencidas()
fun carregarDadosPorPaciente(patientId: Long)
```

#### 3. Operações de Cobrança
```kotlin
fun marcarCobrancaComoPago(cobrancaId: Long)
fun desmarcarCobrancaComoPago(cobrancaId: Long)
fun editarCobranca(cobrancaId: Long, valor: Double, observacoes: String)
```

#### 4. Cálculo de Resumo
```kotlin
data class ResumoFinanceiroUnificado(
    val totalRecebido: Double,      // Soma de todas as cobranças pagas
    val totalAReceber: Double,      // Soma de todas as cobranças pendentes
    val totalDespesas: Double,      // Soma de todas as despesas
    val resultadoPrevisto: Double,  // A receber - despesas
    val countPendentes: Int,        // Quantidade de cobranças pendentes
    val countVencidas: Int,         // Quantidade de cobranças vencidas
    val countCobrancas: Int,        // Total de cobranças
    val countRecords: Int           // Total de registros financeiros
)
```

### Interface do Dashboard

**Componentes Exibidos:**
1. **Header:** Título "Financeiro" com botão de atualizar
2. **Botão de Carregamento:** Carrega dados manualmente
3. **Resumo Financeiro:**
   - Total Recebido (verde)
   - Total a Receber (amarelo)
   - Quantidade de Pendentes
   - Quantidade de Vencidas
   - Total Geral

**Cores e Status:**
- ✅ **Verde (Success):** Valores recebidos/pagos
- ⚠️ **Amarelo (Warning):** Valores a receber/pendentes
- ❌ **Vermelho (Error):** Valores vencidos

---

## 👤 DASHBOARD FINANCEIRO POR PACIENTE {#dashboard-paciente}

### Tela Principal

**Localização:** `app/src/main/java/com/psipro/app/ui/screens/FinanceiroPacienteScreen.kt`

**Activity:** `app/src/main/java/com/psipro/app/FinancialPacienteActivity.kt`

### Funcionalidades

#### 1. Configuração Financeira do Paciente

**Campos Editáveis:**
- **Valor da Sessão:** Valor padrão para cobranças deste paciente
- **Dia da Cobrança:** Dia do mês para gerar lembretes
- **Lembrete de Cobrança:** Ativar/desativar lembretes automáticos

**Localização no Código:**
- Entity: `Patient.sessionValue`, `Patient.diaCobranca`, `Patient.lembreteCobranca`

#### 2. Resumo do Paciente

**Cards Exibidos:**
- **Total Pago:** Soma de todas as cobranças pagas do paciente
- **A Receber:** Soma de todas as cobranças pendentes do paciente

#### 3. Lista de Cobranças de Sessão

**Informações Exibidas:**
- Número da sessão
- Data da sessão
- Data de vencimento
- Valor
- Status (com chip colorido)
- Botões de ação:
  - **Marcar como Pago** (se A_RECEBER)
  - **Desmarcar** (se PAGO)
  - **Editar** (se PAGO)

**Cores por Status:**
- 🟢 **Verde:** PAGO
- 🟡 **Amarelo:** A_RECEBER
- 🔴 **Vermelho:** VENCIDO
- ⚪ **Cinza:** CANCELADO

#### 4. Lista de Cobranças de Agendamento

**Informações Exibidas:**
- Data do agendamento
- Data de vencimento
- Motivo (AGENDAMENTO, CANCELAMENTO, FALTA)
- Valor
- Status
- Botão **Marcar como Pago** (se A_RECEBER)

#### 5. Estado Vazio

Quando não há cobranças, exibe mensagem:
> "Nenhuma cobrança encontrada para este paciente. Cadastre uma anotação de sessão ou marque um agendamento como realizado para gerar cobranças automaticamente."

---

## ⚙️ FUNCIONALIDADES IMPLEMENTADAS {#funcionalidades}

### ✅ Funcionalidades Completas

#### 1. Criação Automática de Cobranças
- ✅ Cobrança de sessão criada ao registrar anotação de sessão
- ✅ Cobrança de agendamento criada ao criar agendamento
- ✅ Valor obtido automaticamente do paciente
- ✅ Data de vencimento calculada automaticamente (7 dias)

#### 2. Gerenciamento de Status
- ✅ Marcar como pago
- ✅ Desmarcar como pago
- ✅ Atualização automática de status para VENCIDO
- ✅ Cancelamento de cobranças

#### 3. Dashboard Geral
- ✅ Visualização de resumo financeiro completo
- ✅ Total recebido (cobranças pagas)
- ✅ Total a receber (cobranças pendentes)
- ✅ Total de despesas
- ✅ Resultado previsto (a receber - despesas)
- ✅ Contadores de pendentes e vencidas
- ✅ Filtros por período
- ✅ Filtros por status

#### 4. Dashboard por Paciente
- ✅ Visualização de resumo do paciente
- ✅ Lista de todas as cobranças do paciente
- ✅ Edição de dados financeiros do paciente
- ✅ Marcar/desmarcar pagamentos
- ✅ Edição de cobranças

#### 5. Integrações
- ✅ Vinculação automática com anotações de sessão
- ✅ Vinculação automática com agendamentos
- ✅ Integração com dados do paciente
- ✅ Suporte a PIX (campo `pixCopiaCola`)

#### 6. Notificações e Lembretes
- ✅ Sistema de lembretes de cobrança (WorkManager)
- ✅ Notificações de cobranças vencidas
- ✅ Configuração por paciente (dia da cobrança, ativar/desativar)

#### 7. Relatórios e Exportação
- ✅ Cálculo de totais e estatísticas
- ✅ Filtros por período
- ✅ Visualização de histórico completo

---

## 🔄 FLUXOS DE TRABALHO {#fluxos}

### Fluxo 1: Registrar Sessão e Gerar Cobrança

```
1. Usuário acessa Detalhes do Paciente
2. Clica em "Nova Sessão"
3. Preenche dados da sessão:
   - Tipo de sessão
   - Valor (preenchido automaticamente do paciente)
   - Assuntos abordados
   - Estado emocional
   - Intervenções
   - Tarefas
   - Evolução
4. Clica em "Salvar"
5. Sistema cria AnotacaoSessao
6. Sistema AUTOMATICAMENTE cria CobrancaSessao:
   - Valor: sessionValue do paciente
   - Data de vencimento: 7 dias após sessão
   - Status: A_RECEBER
   - Número da sessão: sequencial
7. Cobrança aparece no Dashboard Financeiro
```

### Fluxo 2: Criar Agendamento e Gerar Cobrança

```
1. Usuário acessa Agenda
2. Clica em "Novo Agendamento"
3. Seleciona paciente
4. Define data e horário
5. Preenche outros dados
6. Clica em "Salvar"
7. Sistema cria Appointment
8. Sistema AUTOMATICAMENTE cria CobrancaAgendamento:
   - Valor: sessionValue do paciente
   - Data de vencimento: calculada
   - Status: A_RECEBER
   - Motivo: "AGENDAMENTO"
9. Cobrança aparece no Dashboard Financeiro
```

### Fluxo 3: Marcar Cobrança como Paga

```
1. Usuário acessa Dashboard Financeiro ou Financeiro do Paciente
2. Visualiza lista de cobranças
3. Identifica cobrança com status A_RECEBER
4. Clica em "Marcar como Pago"
5. Sistema atualiza:
   - Status: PAGO
   - dataPagamento: Data atual
6. Dashboard atualiza automaticamente:
   - Total Recebido aumenta
   - Total a Receber diminui
   - Contadores atualizados
```

### Fluxo 4: Visualizar Resumo Financeiro

```
1. Usuário acessa Dashboard Financeiro
2. Clica em "Carregar Dados Financeiros"
3. Sistema carrega:
   - Todas as cobranças de sessão
   - Todas as cobranças de agendamento
   - Todos os registros financeiros gerais
4. Sistema calcula resumo:
   - Total recebido (cobranças PAGO)
   - Total a receber (cobranças A_RECEBER + VENCIDO)
   - Total despesas (FinancialRecord tipo DESPESA)
   - Resultado previsto
5. Exibe cards com valores formatados
```

### Fluxo 5: Configurar Dados Financeiros do Paciente

```
1. Usuário acessa Detalhes do Paciente
2. Navega para "Financeiro" do paciente
3. Visualiza formulário de configuração:
   - Valor da sessão
   - Dia da cobrança
   - Lembrete de cobrança (checkbox)
4. Edita valores
5. Clica em "Salvar"
6. Sistema atualiza Patient:
   - sessionValue
   - diaCobranca
   - lembreteCobranca
7. Novas cobranças usarão esses valores
```

---

## 🔗 INTEGRAÇÕES E AUTOMAÇÕES {#integracoes}

### 1. Integração com Anotações de Sessão

**Arquivo:** `app/src/main/java/com/psipro/app/ui/viewmodels/AnotacaoSessaoViewModel.kt`

**Quando:** Ao salvar `AnotacaoSessao`

**O que faz:**
- Cria `CobrancaSessao` automaticamente
- Vincula `anotacaoSessaoId`
- Usa valor do paciente ou informado
- Calcula vencimento (7 dias)

### 2. Integração com Agendamentos

**Arquivo:** `app/src/main/java/com/psipro/app/ui/AppointmentScheduleActivity.kt`

**Quando:** Ao criar `Appointment`

**O que faz:**
- Cria `CobrancaAgendamento` automaticamente
- Vincula `appointmentId`
- Usa valor do paciente
- Define motivo "AGENDAMENTO"

### 3. Integração com Pacientes

**Campos do Patient:**
```kotlin
val sessionValue: Double        // Valor padrão da sessão
val diaCobranca: Int            // Dia do mês para lembretes
val lembreteCobranca: Boolean   // Ativar lembretes
```

**Uso:**
- Valor da sessão usado automaticamente nas cobranças
- Dia da cobrança usado para lembretes
- Lembrete ativado/desativado por paciente

### 4. Sistema de Notificações

**Arquivos:**
- `app/src/main/java/com/psipro/app/notification/LembreteCobrancaWorker.kt`
- `app/src/main/java/com/psipro/app/notification/CobrancaNotificationService.kt`
- `app/src/main/java/com/psipro/app/notification/FinanceiroNotificationService.kt`

**Funcionalidades:**
- Lembretes de cobrança (WorkManager)
- Notificações de cobranças vencidas
- Configurável por paciente

### 5. Integração com WhatsApp

**Funcionalidade:** Geração de mensagem para envio via WhatsApp

**Código:**
```kotlin
fun gerarMensagemWhatsApp(cobranca: CobrancaSessao): String
```

**Conteúdo da mensagem:**
- Número da sessão
- Data da sessão
- Valor
- Data de vencimento
- Chave PIX (se configurada)

---

## 📱 TELAS E COMPONENTES UI

### 1. FinanceiroDashboardActivity

**Localização:** `app/src/main/java/com/psipro/app/ui/screens/FinanceiroDashboardActivity.kt`

**Tipo:** Activity (host para Compose)

**Função:** Container principal do dashboard financeiro geral

### 2. FinanceiroDashboardScreen

**Localização:** `app/src/main/java/com/psipro/app/ui/screens/FinanceiroDashboardScreen.kt`

**Tipo:** Composable (Jetpack Compose)

**Componentes:**
- Header com título e botão de atualizar
- Botão de carregamento manual
- Cards de resumo financeiro
- Indicadores de loading e erro

**Cores Simplificadas:**
```kotlin
object SimpleColors {
    val Primary = Color(0xFF667eea)
    val Success = Color(0xFF10b981)
    val Warning = Color(0xFFf59e0b)
    val Error = Color(0xFFef4444)
    val Info = Color(0xFF3b82f6)
}
```

### 3. FinanceiroPacienteScreen

**Localização:** `app/src/main/java/com/psipro/app/ui/screens/FinanceiroPacienteScreen.kt`

**Tipo:** Composable (Jetpack Compose)

**Componentes:**
- TopAppBar com título e botão voltar
- Formulário de configuração financeira do paciente
- Cards de resumo (Total Pago, A Receber)
- Lista de cobranças de sessão
- Lista de cobranças de agendamento
- Estado vazio quando não há cobranças

### 4. FinanceiroComponents

**Localização:** `app/src/main/java/com/psipro/app/ui/screens/FinanceiroComponents.kt`

**Componentes Reutilizáveis:**
- StatusChip (chip colorido para status)
- Cards de resumo
- Componentes de lista

### 5. FinanceiroFragment

**Localização:** `app/src/main/java/com/psipro/app/ui/fragments/FinanceiroFragment.kt`

**Tipo:** Fragment (XML-based)

**Função:** Fragment alternativo para dashboard financeiro

---

## 🗄️ BANCO DE DADOS

### Tabelas

#### 1. cobrancas_sessao
```sql
CREATE TABLE cobrancas_sessao (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    patientId INTEGER NOT NULL,
    anotacaoSessaoId INTEGER NOT NULL,
    numeroSessao INTEGER NOT NULL,
    valor REAL NOT NULL,
    dataSessao INTEGER NOT NULL,
    dataVencimento INTEGER NOT NULL,
    dataPagamento INTEGER,
    status TEXT NOT NULL,
    observacoes TEXT,
    pixCopiaCola TEXT,
    tipoSessaoId INTEGER,
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL,
    FOREIGN KEY(patientId) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY(anotacaoSessaoId) REFERENCES anotacoes_sessao(id) ON DELETE CASCADE
);

CREATE INDEX index_cobrancas_sessao_patientId ON cobrancas_sessao(patientId);
CREATE INDEX index_cobrancas_sessao_anotacaoSessaoId ON cobrancas_sessao(anotacaoSessaoId);
```

#### 2. cobrancas_agendamento
```sql
CREATE TABLE cobrancas_agendamento (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    patientId INTEGER NOT NULL,
    appointmentId INTEGER NOT NULL,
    valor REAL NOT NULL,
    dataAgendamento INTEGER NOT NULL,
    dataVencimento INTEGER NOT NULL,
    dataPagamento INTEGER,
    status TEXT NOT NULL,
    motivo TEXT NOT NULL,
    observacoes TEXT,
    pixCopiaCola TEXT,
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL,
    FOREIGN KEY(patientId) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY(appointmentId) REFERENCES appointments(id) ON DELETE CASCADE
);

CREATE INDEX index_cobrancas_agendamento_patientId ON cobrancas_agendamento(patientId);
CREATE INDEX index_cobrancas_agendamento_appointmentId ON cobrancas_agendamento(appointmentId);
```

#### 3. financial_records
```sql
CREATE TABLE financial_records (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    patientId INTEGER,
    description TEXT NOT NULL,
    value REAL NOT NULL,
    type TEXT NOT NULL,
    date INTEGER NOT NULL
);
```

---

## 📈 CÁLCULOS E ESTATÍSTICAS

### Resumo Financeiro Unificado

**Fórmulas:**

```kotlin
// Total Recebido
totalRecebido = 
    SUM(cobrancas_sessao.valor WHERE status = 'PAGO') +
    SUM(cobrancas_agendamento.valor WHERE status = 'PAGO') +
    SUM(financial_records.value WHERE type = 'RECEITA')

// Total a Receber
totalAReceber = 
    SUM(cobrancas_sessao.valor WHERE status IN ('A_RECEBER', 'VENCIDO')) +
    SUM(cobrancas_agendamento.valor WHERE status IN ('A_RECEBER', 'VENCIDO'))

// Total Despesas
totalDespesas = 
    SUM(financial_records.value WHERE type = 'DESPESA')

// Resultado Previsto
resultadoPrevisto = totalAReceber - totalDespesas

// Contadores
countPendentes = COUNT(cobrancas WHERE status = 'A_RECEBER')
countVencidas = COUNT(cobrancas WHERE status = 'VENCIDO')
```

### Resumo por Paciente

```kotlin
// Total Pago do Paciente
totalRecebido = 
    SUM(cobrancas_sessao.valor WHERE patientId = X AND status = 'PAGO') +
    SUM(cobrancas_agendamento.valor WHERE patientId = X AND status = 'PAGO')

// Total a Receber do Paciente
totalAReceber = 
    SUM(cobrancas_sessao.valor WHERE patientId = X AND status IN ('A_RECEBER', 'VENCIDO')) +
    SUM(cobrancas_agendamento.valor WHERE patientId = X AND status IN ('A_RECEBER', 'VENCIDO'))
```

---

## 🔔 NOTIFICAÇÕES E LEMBRETES

### LembreteCobrancaWorker

**Localização:** `app/src/main/java/com/psipro/app/notification/LembreteCobrancaWorker.kt`

**Funcionalidade:**
- WorkManager para lembretes periódicos
- Verifica cobranças próximas do vencimento
- Envia notificações

### CobrancaNotificationService

**Localização:** `app/src/main/java/com/psipro/app/notification/CobrancaNotificationService.kt`

**Funcionalidade:**
- Notificações de cobranças vencidas
- Lembretes de pagamento

### FinanceiroNotificationService

**Localização:** `app/src/main/java/com/psipro/app/notification/FinanceiroNotificationService.kt`

**Funcionalidade:**
- Notificações gerais do módulo financeiro

---

## 🎨 FORMATAÇÃO E APRESENTAÇÃO

### Formatação de Valores

**Código:**
```kotlin
val formatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
formatter.format(valor) // Ex: R$ 150,00
```

### Formatação de Datas

**Código:**
```kotlin
val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
dateFormatter.format(data) // Ex: 26/12/2024
```

### Cores por Status

**StatusPagamento.PAGO:**
- Cor: Verde (Success)
- Background: Verde claro
- Ícone: CheckCircle

**StatusPagamento.A_RECEBER:**
- Cor: Amarelo (Warning)
- Background: Amarelo claro
- Ícone: Schedule

**StatusPagamento.VENCIDO:**
- Cor: Vermelho (Error)
- Background: Vermelho claro
- Ícone: Warning

**StatusPagamento.CANCELADO:**
- Cor: Cinza (Neutral)
- Background: Cinza claro
- Ícone: Cancel

---

## 🚀 MELHORIAS FUTURAS SUGERIDAS

### 1. Funcionalidades Adicionais
- [ ] Exportação de relatórios em PDF
- [ ] Gráficos de evolução mensal/anual
- [ ] Integração com PIX para pagamento direto
- [ ] Geração automática de boletos
- [ ] Histórico de alterações de status
- [ ] Relatórios personalizados

### 2. Otimizações
- [ ] Cache de cálculos de resumo
- [ ] Paginação de listas grandes
- [ ] Busca e filtros avançados
- [ ] Sincronização em nuvem

### 3. Integrações
- [ ] API de pagamento (Stripe, PagSeguro, etc.)
- [ ] Integração com contabilidade
- [ ] Exportação para Excel/CSV
- [ ] Backup automático

---

## 📝 RESUMO EXECUTIVO

### O que foi implementado:

✅ **3 sistemas financeiros integrados:**
1. Cobranças de Sessão (automático)
2. Cobranças de Agendamento (automático)
3. Registros Financeiros Gerais (manual)

✅ **2 dashboards completos:**
1. Dashboard Financeiro Geral
2. Dashboard Financeiro por Paciente

✅ **Funcionalidades principais:**
- Criação automática de cobranças
- Gerenciamento de status de pagamento
- Cálculo de resumos e estatísticas
- Filtros por período e status
- Edição de cobranças
- Configuração financeira por paciente
- Notificações e lembretes
- Integração com WhatsApp

✅ **Arquitetura:**
- MVVM com ViewModels
- Repository Pattern
- Room Database com Flow
- Jetpack Compose para UI
- Hilt para Dependency Injection

### Status: ✅ **COMPLETO E FUNCIONAL**

O sistema financeiro está totalmente implementado e funcional, com todas as funcionalidades principais operando corretamente.

---

**Documentação criada em:** 26/12/2024  
**Última atualização:** 26/12/2024

