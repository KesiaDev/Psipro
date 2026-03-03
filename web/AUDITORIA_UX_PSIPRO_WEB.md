# Auditoria UX PsiPro Web — Experiência Premium

**Objetivo:** Transformar o produto em experiência premium mediante refinamentos de UX/UI, sem alterar arquitetura, backend ou criar novas funcionalidades.

---

## 1. Fricções nos Fluxos Principais

### 1.1 Login (`/login`)

| Fricção | Severidade | Descrição |
|---------|------------|-----------|
| Loading genérico | Média | "Carregando..." sem indicação visual de progresso nem branding |
| Falta de link "Esqueci minha senha" | Alta | Fluxo incompleto; usuário pode ficar bloqueado |
| Erro duplicado | Média | Erro geral aparece no card E como toast; pode confundir |
| Botão sem min-height | Baixa | Botão pode ter altura inconsistente em diferentes estados |
| Sem focus visible acessível | Baixa | Alguns elementos podem não ter outline claro para navegação por teclado |

**Pontos positivos:** Validação em tempo real, limpeza de erros ao digitar, suporte a `returnUrl`.

---

### 1.2 Cadastro (`/register`)

| Fricção | Severidade | Descrição |
|---------|------------|-----------|
| Formulário longo sem progresso | Média | 4 campos sem indicação de progresso ou etapas |
| Mensagem de senha genérica | Baixa | "Senha deve ter pelo menos 6 caracteres" — não indica requisitos (maiúscula, número, etc.) |
| Sem link "Esqueci minha senha" no fluxo de login vinculado | Baixa | Consistência com página de login |
| Mesmas inconsistências de feedback do login | Média | Loading, erros, tipografia |

---

### 1.3 Criar Paciente (`CreatePatientModal`)

| Fricção | Severidade | Descrição |
|---------|------------|-----------|
| Subtítulo técnico | Alta | "Cadastre manualmente e salve no backend" — linguagem não adequada ao usuário final |
| CPF sem máscara | Média | Campo aceita qualquer input; difícil de preencher corretamente |
| Telefone sem máscara | Média | Mesmo problema; formato (11) 99999-9999 não é aplicado |
| Validação apenas em nome | Média | Email/CPF sem validação básica |
| Sem feedback de sucesso no modal | Média | Modal fecha; sucesso só via toast (pode passar despercebido) |
| Botões com hierarquia invertida em alguns contextos | Baixa | "Cancelar" e "Salvar" — secondary poderia ter menor ênfase |

---

### 1.4 Criar Agendamento

**Status:** O fluxo de criar/gerenciar agendamentos não existe na Web. O CTA orienta: *"Para criar ou gerenciar consultas, utilize o app PsiPro no celular."*

**Recomendação:** Manter como está (escopo atual). Se no futuro for adicionada criação de agendamento na Web, seguir os mesmos padrões definidos neste plano.

---

### 1.5 Confirmar Sessão

**Status:** Também realizado no app. A Web exibe visão consolidada (agenda, financeiro) e não oferece confirmação de sessão.

**Recomendação:** Sem alterações. Qualquer futura integração deve reutilizar padrões de feedback e hierarquia deste documento.

---

### 1.6 Visualizar Financeiro (`/financeiro`)

| Fricção | Severidade | Descrição |
|---------|------------|-----------|
| Dados mockados vs. API | Crítica | Cards mostram valores fixos (R$ 450, R$ 6.300...) enquanto `loadFinancialData()` carrega dados reais que não são usados nos cards principais |
| Loading só para API | Média | Skeleton/loading não reflete o carregamento real dos cards; usuário vê dados falsos |
| Gráfico de barras sem tooltip rico | Baixa | Hover com `title` nativo; poderia ter tooltip customizado mais informativo |
| Botões de período sem indicação de loading | Média | Ao mudar Mês/Trimestre/Ano, não há feedback visual |
| Estado vazio | Ausente | Não há estado vazio quando não há dados financeiros |
| Alertas com ações não funcionais | Média | "Ver detalhes", "Analisar" não navegam ou executam ação |

---

## 2. Hierarquia Visual, Espaçamento e Consistência

### 2.1 Hierarquia Visual

| Elemento | Situação Atual | Problema |
|----------|---------------|----------|
| Títulos de página | `text-2xl` a `text-3xl`, `font-bold` | Consistência entre páginas varia (Dashboard vs. Agenda vs. Financeiro) |
| Subtítulos | `text-sm` a `text-lg` | Escala não padronizada |
| Cards de métrica | Estilos inline diferentes em agenda, financeiro e dashboard | `MetricCard` existe mas não é usado em agenda/financeiro |
| Ícones em cards | Emojis (📅, 💰) | Dimensões e opacidade variam |

### 2.2 Espaçamento

| Área | Situação | Recomendação |
|------|----------|--------------|
| Entre seções | `mb-6` a `mb-8` | Padronizar escala: 4, 6, 8, 12 |
| Padding de cards | `p-4` a `p-8` | Definir: card compacto `p-4`, padrão `p-6`, expanso `p-8` |
| Formulários | `space-y-4` a `space-y-6` | Padronizar `space-y-5` para formulários |
| Grid de métricas | `gap-5` | Ok; manter |

### 2.3 Feedback de Ações

| Ação | Situação | Recomendação |
|------|----------|--------------|
| Submit de formulários | Botão desabilitado + texto "Salvando..." | Adicionar spinner no botão |
| Navegação | Sem feedback | Manter; não adicionar loading em navegação interna |
| Importar pacientes | `importing ? "Importando..." : "Confirmar"` | Adicionar spinner |
| Criar paciente | `isSubmitting` no botão | Adicionar spinner |
| Mudança de período (financeiro) | Sem loading | Indicar recarregamento ao trocar período |

### 2.4 Estados Vazios

| Tela | Situação | Recomendação |
|------|----------|--------------|
| Pacientes | Boa estrutura: ícone, título, descrição, CTAs | Refinar copy para tom mais acolhedor |
| Dashboard Agenda | SectionCard com emptyState | Ok |
| Dashboard Financeiro | SectionCard com emptyState | Ok |
| Financeiro (página) | Sem estado vazio | Criar empty state quando não houver dados |
| Lista filtrada (pacientes) | "Nenhum paciente encontrado" | Melhorar mensagem e sugerir limpar filtros |
| Import Patients Modal | Steps com validação | Já tem fluxo; revisar mensagens de erro |

### 2.5 Mensagens de Erro

| Contexto | Situação | Recomendação |
|----------|----------|--------------|
| Login/Register | Mensagens genéricas do backend | Mapear códigos comuns (401, 429, etc.) para mensagens amigáveis |
| Criação de paciente | "Erro ao cadastrar paciente" | Incluir sugestão de ação (ex.: "Verifique se o email já está em uso") |
| Importação | `alert()` nativo | Substituir por Toast ou mensagens inline no modal |
| API genérica | "Erro ao carregar..." | Padronizar formato: "Não foi possível [ação]. [Sugestão]." |

### 2.6 Microinterações

| Elemento | Situação | Recomendação |
|----------|----------|--------------|
| Botões | `transition-colors` | Adicionar `transition-transform` e leve scale no hover |
| Cards | `hover:shadow-md` em alguns | Padronizar hover em todos os cards clicáveis |
| Inputs | `focus:ring-2` | Manter; verificar contraste no dark mode |
| Toast | `animate-in slide-in-from-top-5` | Ok; considerar animação de saída |
| Sidebar | `transition-transform` | Ok |
| Modal | Sem animação de entrada | Adicionar `scale-in` ou `fade-in` suave |

---

## 3. Padrões a Estabelecer

### 3.1 Botões

| Variante | Uso | Classes Tailwind |
|----------|-----|------------------|
| **Primary** | Ação principal (Salvar, Entrar, Criar) | `bg-psipro-primary text-white hover:bg-psipro-primary-dark rounded-lg px-4 py-2.5 font-medium transition-all duration-200 active:scale-[0.98]` |
| **Secondary** | Ação secundária (Cancelar, Voltar) | `bg-psipro-surface border border-psipro-border text-psipro-text hover:bg-psipro-surface-elevated rounded-lg px-4 py-2.5 font-medium transition-all duration-200` |
| **Danger** | Ações destrutivas (Excluir) | `bg-psipro-error text-white hover:bg-psipro-error/90 rounded-lg px-4 py-2.5 font-medium transition-all duration-200` |
| **Ghost** | Ações terciárias (Links, Fechar) | `text-psipro-text-secondary hover:text-psipro-text hover:bg-psipro-surface rounded-lg px-3 py-2 transition-colors` |

**Regras:**
- Altura mínima 40px
- `disabled:opacity-50 disabled:cursor-not-allowed`
- Loading: spinner à esquerda do texto + texto alterado (ex.: "Salvando...")

---

### 3.2 Cards

| Tipo | Uso | Estrutura |
|------|-----|-----------|
| **Card padrão** | Conteúdo genérico | `bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm overflow-hidden` |
| **Card de métrica** | KPIs, números | Usar `MetricCard`; padding `p-6`; ícone opaco 60% |
| **Card clicável** | Linhas de tabela, itens de lista | Adicionar `hover:bg-psipro-surface cursor-pointer transition-colors` |
| **Card de seção** | Título + conteúdo | Usar `SectionCard`; header `border-b border-psipro-divider p-6` |

---

### 3.3 Títulos

| Nível | Uso | Classes |
|-------|-----|---------|
| **H1 (Página)** | Título principal da página | `text-2xl sm:text-3xl font-bold text-psipro-text tracking-tight mb-2` |
| **H2 (Seção)** | Título de bloco/card | `text-lg font-semibold text-psipro-text` |
| **H3 (Subseção)** | Título menor | `text-base font-semibold text-psipro-text` |
| **Subtítulo** | Descrição sob o título | `text-base sm:text-lg text-psipro-text-secondary` |

---

### 3.4 Métricas

| Elemento | Padrão |
|----------|--------|
| Valor | `text-2xl` ou `text-3xl font-bold` + cor semântica (primary, success, warning, error) |
| Label | `text-sm text-psipro-text-secondary font-medium` |
| Subtitle | `text-xs text-psipro-text-muted` |
| Ícone | Emoji ou ícone SVG, `opacity-60`, alinhado à direita |

---

### 3.5 Alerts

| Tipo | Background | Border | Texto |
|------|------------|--------|-------|
| **Error** | `bg-psipro-error/10` | `border-psipro-error/20` | `text-psipro-error` |
| **Warning** | `bg-psipro-warning/10` | `border-psipro-warning/30` | `text-psipro-warning` |
| **Success** | `bg-psipro-success/10` | `border-psipro-success/30` | `text-psipro-success` |
| **Info** | `bg-psipro-primary/10` | `border-psipro-primary/30` | `text-psipro-primary` |

**Estrutura:** Ícone + mensagem + (opcional) ação. Padding `p-4 rounded-lg`.

---

## 4. Cores e Tipografia

### 4.1 Cores

- **Paleta:** Já definida em `globals.css` (primary, success, error, warning, surfaces, text).
- **Problema:** Uso inconsistente de `text-psipro-text` vs `text-psipro-text-primary` (variável existe mas alguns lugares usam `text-psipro-text`).
- **Recomendação:** Padronizar nomenclatura no CSS/Tailwind e documentar uso de cada variável.

### 4.2 Tipografia

- **Font:** Geist Sans (via `--font-geist-sans`).
- **Situação:** Consistente no layout; boa legibilidade.
- **Recomendação:** Garantir `leading-relaxed` em parágrafos longos; `tracking-tight` em títulos grandes.

---

## 5. Plano de Melhoria Priorizado

### Matriz de Priorização (Impacto x Esforço)

| Impacto | Esforço Baixo | Esforço Médio | Esforço Alto |
|---------|---------------|---------------|--------------|
| **Alto** | P1, P2, P4 | P3, P6 | P10 |
| **Médio** | P5, P7, P8 | P9, P11 | — |
| **Baixo** | P12 | P13 | — |

---

### Fase 1 — Quick Wins (1–2 sprints)

| # | Item | Descrição | Impacto | Esforço |
|---|------|-----------|---------|---------|
| **P1** | Corrigir dados do Financeiro | Usar dados da API nos cards em vez de mocks; exibir skeleton durante loading | Crítico | Médio |
| **P2** | Padronizar loading (Auth, BetaGate) | Substituir "Carregando..." por spinner + branding | Alto | Baixo |
| **P3** | Criar componente `Button` reutilizável | Primary, Secondary, Danger, Ghost com loading state | Alto | Médio |
| **P4** | Remover subtítulo técnico do CreatePatientModal | Trocar "Cadastre manualmente e salve no backend" por "Preencha os dados para cadastrar o paciente" | Alto | Baixo |
| **P5** | Padronizar mensagens de erro | Criar helper para mapear erros de API em mensagens amigáveis | Médio | Baixo |

---

### Fase 2 — Consistência Visual (2–3 sprints)

| # | Item | Descrição | Impacto | Esforço |
|---|------|-----------|---------|---------|
| **P6** | Substituir `alert()` no ImportPatientsModal | Usar Toast ou alerta inline no modal | Alto | Médio |
| **P7** | Adicionar máscara CPF e telefone no CreatePatientModal | Melhorar usabilidade sem lib externa (regex + formatação) | Médio | Baixo |
| **P8** | Padronizar empty states | Revisar copy e layout em Pacientes, Financeiro, listas filtradas | Médio | Baixo |
| **P9** | Criar componente `Alert` reutilizável | Error, Warning, Success, Info seguindo padrão da seção 3.5 | Médio | Médio |
| **P10** | Unificar cards de métrica (Dashboard, Agenda, Financeiro) | Usar `MetricCard` em todas as páginas com variantes de cor | Alto | Médio |

---

### Fase 3 — Refinamentos Premium (2 sprints)

| # | Item | Descrição | Impacto | Esforço |
|---|------|-----------|---------|---------|
| **P11** | Microinterações em botões e cards | Hover scale, transições suaves, feedback tátil | Médio | Médio |
| **P12** | Animação de entrada em modais | `scale-in` ou `fade-in` ao abrir | Baixo | Baixo |
| **P13** | Documentar Design System | Criar `DESIGN_SYSTEM.md` com componentes, tokens e exemplos | Médio | Médio |

---

### Itens Fora do Escopo Atual (não implementar agora)

- **Esqueci minha senha:** Nova funcionalidade — requer backend.
- **Criar/confirmar agendamento na Web:** Nova funcionalidade.
- **Alteração de arquitetura ou backend:** Excluído.

---

## 6. Resumo Executivo

| Categoria | Principais Achados |
|-----------|--------------------|
| **Fricções críticas** | Financeiro exibe dados mockados em vez dos dados da API |
| **Fricções altas** | CreatePatientModal com copy técnico; falta de "Esqueci senha"; `alert()` em importação |
| **Oportunidades** | Padronizar botões, cards, alerts e empty states |
| **Forças** | Boa estrutura de cores, tipografia e temas; fluxos de auth e dashboard bem organizados |

**Próximos passos recomendados:** Iniciar pela Fase 1, priorizando P1 (dados reais no Financeiro) e P3 (componente Button), que trazem maior impacto na percepção premium com esforço controlado.
