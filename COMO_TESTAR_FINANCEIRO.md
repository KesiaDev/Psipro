# 💰 COMO TESTAR O SISTEMA FINANCEIRO DO PSIPRO

## 📋 COMO O SISTEMA FINANCEIRO FUNCIONA

O Psipro possui **2 sistemas financeiros integrados**:

### 1. **CobrancaSessao** (Cobranças de Sessões)
- ✅ **Criado AUTOMATICAMENTE** quando você registra uma sessão para um paciente
- ✅ Localização: `Detalhes do Paciente > Nova Sessão`
- ✅ Preencha: Tipo de sessão, valor, assuntos, etc.
- ✅ Ao salvar, uma cobrança é criada com:
  - Valor da sessão
  - Data da sessão
  - Vencimento (7 dias após)
  - Status: A RECEBER

### 2. **FinancialRecord** (Registros Financeiros Gerais)
- ✅ **Criado AUTOMATICAMENTE** quando você agenda uma consulta
- ✅ Localização: `Agenda > Novo Agendamento`
- ✅ Ao criar agendamento, um registro financeiro é criado com o valor da sessão do paciente

## 🎯 PASSOS PARA TESTAR O FINANCEIRO

### **PASSO 1: Cadastrar um Paciente**
1. Abra o app
2. Vá em "Pacientes"
3. Clique no botão "+" para adicionar
4. Preencha:
   - Nome: João Silva
   - CPF: 123.456.789-00
   - Data de Nascimento: 01/01/1990
   - Telefone: (11) 98765-4321
   - **IMPORTANTE**: Valor da Sessão: R$ 150,00
5. Salvar

### **PASSO 2: Registrar uma Sessão (GERA COBRANÇA)**
1. Clique no paciente cadastrado
2. Na tela de detalhes, procure por "Anotações da Sessão" ou "Nova Sessão"
3. Clique em "Nova Sessão"
4. Preencha:
   - Tipo de sessão: Individual (ou outro)
   - Valor: R$ 150,00 (deve preencher automaticamente)
   - Assuntos abordados: "Ansiedade e estresse"
   - Estado emocional: "Ansioso"
   - Intervenções: "Técnicas de respiração"
5. Clique em "Salvar"
6. ✅ **Uma cobrança foi criada automaticamente!**

### **PASSO 3: Criar Agendamento (GERA FINANCIAL RECORD)**
1. Vá para "Agenda"
2. Clique no botão "+" ou "Novo Agendamento"
3. Selecione o paciente
4. Escolha data e horário
5. Preencha os dados
6. Salvar
7. ✅ **Um registro financeiro foi criado automaticamente!**

### **PASSO 4: Acessar o Dashboard Financeiro**
1. No menu principal, clique em "Financeiro" ou "Dashboard Financeiro"
2. Você verá:
   - **Total Recebido**: Soma de todas as cobranças pagas
   - **A Receber**: Soma de todas as cobranças pendentes
   - **Total Despesas**: Soma dos registros financeiros do tipo DESPESA
   - **Resultado Previsto**: A receber - despesas

## 🔍 VERIFICAR SE ESTÁ FUNCIONANDO

### Logs para Debug:
Após registrar uma sessão, verifique o Logcat:
```
AnotacaoSessaoVM: Cobrança criada com sucesso: ID=X, Valor=150.0, Tipo=1
```

### Verificar Dashboard:
1. Acesse o Dashboard Financeiro
2. Verifique se os cartões mostram valores
3. Na aba "RECEITAS", veja a lista de cobranças

## 🐛 SE AINDA NÃO APARECER NADA

### Problema 1: Nenhum Dado no Dashboard
**Causa**: Nenhuma sessão ou agendamento foi criado ainda
**Solução**: Siga os PASSOS 1-3 acima

### Problema 2: Dashboard Carrega mas Mostra R$ 0,00
**Causa**: O valor da sessão não foi preenchido
**Solução**: 
1. Edite o paciente
2. Preencha o "Valor da Sessão"
3. Registre uma nova sessão

### Problema 3: Dados Não Atualizam
**Causa**: Cache do ViewModel
**Solução**: 
1. Feche o app completamente
2. Abra novamente
3. Acesse o Dashboard Financeiro

## 📊 ESTRUTURA DOS DADOS

### CobrancaSessao
```kotlin
- id: Long (auto)
- patientId: Long
- anotacaoSessaoId: Long
- numeroSessao: Int
- valor: Double ⬅️ IMPORTANTE!
- dataSessao: Date
- dataVencimento: Date (7 dias após sessão)
- status: StatusPagamento (A_RECEBER, PAGO, VENCIDO, CANCELADO)
- tipoSessaoId: Long? (tipo de sessão)
```

### FinancialRecord
```kotlin
- id: Long (auto)
- patientId: Long?
- description: String
- value: Double ⬅️ IMPORTANTE!
- type: String (RECEITA ou DESPESA)
- date: Date
```

## ✅ TESTE COMPLETO

Crie este cenário para testar tudo:

1. **Paciente 1 - Maria**
   - Valor sessão: R$ 150,00
   - Registre 2 sessões
   - Marque 1 como paga

2. **Paciente 2 - João**
   - Valor sessão: R$ 200,00
   - Registre 1 sessão
   - Deixe como A RECEBER

3. **Agendamento**
   - Crie 1 agendamento futuro

**Resultado Esperado no Dashboard:**
- Total Recebido: R$ 150,00
- A Receber: R$ 350,00 (150 + 200)
- Total Despesas: R$ 0,00
- Resultado Previsto: R$ 350,00

## 🚀 MELHORIAS FUTURAS

Para melhorar ainda mais o financeiro:

1. **Adicionar botão "Adicionar Despesa Manual"**
2. **Permitir editar valores de cobranças**
3. **Exportar relatório financeiro em PDF**
4. **Gráficos de evolução mensal**
5. **Integração com PIX**

---

**O sistema financeiro ESTÁ FUNCIONANDO! Você só precisa:**
1. ✅ Cadastrar pacientes com valor de sessão
2. ✅ Registrar sessões
3. ✅ Acessar o Dashboard Financeiro



