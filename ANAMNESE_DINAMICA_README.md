# Sistema de Anamnese Dinâmica - Psipro

## 📋 Visão Geral

O sistema de anamnese dinâmica permite criar, editar e usar modelos de formulário personalizados para coleta de dados dos pacientes. Cada modelo pode ter campos de diferentes tipos (texto, data, seleção, etc.) e ser reutilizado para diferentes pacientes.

## 🚀 Funcionalidades

### ✅ Implementadas
- ✅ Criação de modelos de anamnese personalizados
- ✅ Edição de modelos existentes
- ✅ Remoção de modelos
- ✅ Campos dinâmicos com diferentes tipos:
  - Texto curto
  - Texto longo
  - Data
  - Seleção única (radio button)
  - Múltipla escolha (checkbox)
  - Título (separador visual)
- ✅ Validação de campos obrigatórios
- ✅ Preenchimento de anamnese usando modelos
- ✅ Listagem de anamneses preenchidas
- ✅ Persistência no banco de dados (Room)
- ✅ Interface moderna com Material Design 3

### 🔄 Em Desenvolvimento
- 🔄 Edição de anamneses já preenchidas
- 🔄 Exportação de anamneses (PDF, Excel)
- 🔄 Duplicação de modelos
- 🔄 Reordenação de campos
- 🔄 Templates pré-definidos

## 📱 Como Usar

### 1. Acessar o Sistema
1. Abra a ficha de um paciente
2. Clique no botão "Anamnese Dinâmica"
3. Você verá a lista de anamneses já preenchidas

### 2. Criar Nova Anamnese
1. Clique no botão "+" (Nova Anamnese)
2. Selecione um modelo existente ou crie um novo
3. Preencha os campos do formulário
4. Clique em "Salvar"

### 3. Gerenciar Modelos
1. Na tela de seleção de modelos, clique no ícone de editar (✏️)
2. Modifique o nome do modelo
3. Adicione, edite ou remova campos
4. Salve as alterações

### 4. Criar Novo Modelo
1. Na tela de seleção de modelos, clique no ícone "+"
2. Digite o nome do modelo
3. Adicione campos conforme necessário
4. Salve o modelo

## 🏗️ Arquitetura

### Telas (Compose)
- `AnamneseCompleteFlow`: Fluxo principal que gerencia todas as telas
- `AnamneseListScreen`: Lista de anamneses preenchidas
- `AnamneseModelSelectionScreen`: Seleção de modelos
- `AnamneseModelEditScreen`: Criação/edição de modelos
- `AnamneseFormScreen`: Preenchimento de formulário

### ViewModel
- `AnamneseViewModel`: Gerencia dados e operações de anamnese

### Entidades (Room)
- `AnamneseModel`: Modelo de anamnese
- `AnamneseCampo`: Campo do modelo
- `AnamnesePreenchida`: Anamnese preenchida por um paciente

### DAOs
- `AnamneseModelDao`: Operações de modelo
- `AnamneseCampoDao`: Operações de campo
- `AnamnesePreenchidaDao`: Operações de anamnese preenchida

## 📊 Tipos de Campo

| Tipo | Descrição | Exemplo |
|------|-----------|---------|
| `TEXTO_CURTO` | Campo de texto de uma linha | Nome, telefone |
| `TEXTO_LONGO` | Campo de texto de múltiplas linhas | Queixa principal, histórico |
| `DATA` | Campo de data | Data de nascimento |
| `SELECAO_UNICA` | Seleção única (radio button) | Estado civil |
| `MULTIPLA_ESCOLHA` | Seleção múltipla (checkbox) | Sintomas, medicamentos |
| `TITULO` | Título/separador visual | Seções do formulário |

## 🔧 Configuração

### Modelos Padrão
O sistema vem com 3 modelos pré-configurados:
1. **Anamnese Adulto**: Para pacientes adultos
2. **Anamnese Infantil**: Para crianças
3. **Anamnese Casal**: Para terapia de casal

### Personalização
Você pode:
- Criar novos modelos
- Editar modelos existentes
- Remover modelos (exceto os padrão)
- Adicionar campos personalizados

## 🗄️ Banco de Dados

### Tabelas
- `AnamneseModel`: Armazena os modelos
- `AnamneseCampo`: Armazena os campos de cada modelo
- `AnamnesePreenchida`: Armazena as anamneses preenchidas

### Relacionamentos
- Um modelo tem muitos campos (1:N)
- Um modelo pode ter muitas anamneses preenchidas (1:N)
- Uma anamnese preenchida pertence a um modelo e um paciente

## 🎨 Interface

### Design System
- Material Design 3
- Tema claro/escuro
- Cores personalizadas (bronze/gold)
- Componentes reutilizáveis

### Navegação
- Fluxo linear e intuitivo
- Botões de voltar em todas as telas
- Feedback visual (loading, erros, sucesso)

## 🧪 Testes

### Utilitários de Teste
- `AnamneseTestUtils`: Cria dados de exemplo para testes
- Modelos e campos pré-configurados
- Dados realistas para demonstração

### Como Testar
1. Instale o app
2. Crie um paciente
3. Acesse "Anamnese Dinâmica"
4. Teste criar, editar e usar modelos
5. Verifique a persistência dos dados

## 🚨 Limitações Atuais

1. **Edição de anamnese preenchida**: Não implementada
2. **Exportação**: Não implementada
3. **Validação avançada**: Apenas campos obrigatórios
4. **Upload de arquivos**: Não implementado
5. **Sincronização**: Apenas local

## 🔮 Próximas Melhorias

1. **Edição de anamnese**: Permitir editar anamneses já salvas
2. **Exportação**: Gerar PDF/Excel das anamneses
3. **Templates**: Mais modelos pré-definidos
4. **Validação**: Regras de validação personalizadas
5. **Anexos**: Upload de documentos/imagens
6. **Sincronização**: Backup na nuvem

## 📞 Suporte

Para dúvidas ou problemas:
1. Verifique se o banco foi criado corretamente
2. Confirme se as permissões estão corretas
3. Teste com dados de exemplo
4. Consulte os logs para erros

---

**Versão**: 1.0.0  
**Data**: Dezembro 2024  
**Desenvolvedor**: Sistema Psipro 