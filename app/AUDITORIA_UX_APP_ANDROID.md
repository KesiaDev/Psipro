# Auditoria UX PsiPro Android — Minimalista, Rápido e Elegante

**Objetivo:** Deixar o app minimalista, rápido e elegante mediante simplificação visual, padronização e consistência do Design System, sem alterar regras de negócio, arquitetura ou criar novas telas.

---

## 1. Avaliação por Tela

### 1.1 Tela de Login (`MainActivity` + `activity_main.xml`)

| Aspecto | Situação Atual | Problema |
|---------|---------------|----------|
| **Layout** | Logo grande (280x180dp), múltiplos botões (Entrar, Google, Criar conta, Esqueci senha), marca no rodapé | Excesso visual; muitos CTAs competindo por atenção |
| **Hierarquia** | Logo e marca ocupam muito espaço | Pouco espaço para o formulário em telas menores |
| **Consistência de estilos** | Usa `LoginInputLayout`, `LoginButton`, `GoogleSignInButton` | Estilos bem definidos; porém `activity_backend_login.xml` usa estilos inline diferentes (boxCornerRadius 24dp direto, sem style) |
| **Cores** | `@color/bronze_gold`, `?attr/colorPrimary` | Maioria usa recursos; GoogleSignInButton tem `text_black` e `white` hardcoded no style |
| **Feedback** | Sem indicador de loading no botão; ProgressBar separado | Loading pouco visível; sem desabilitar botão durante requisição |

**Sugestões:**
- Reduzir logo para proporção mais compacta (ex.: max 200dp de largura)
- Agrupar "Criar conta" e "Esqueci senha" como links secundários, menos proeminentes
- Unificar estilos com `activity_backend_login` ou deprecar uma das telas
- Adicionar ProgressBar/loading no próprio botão durante login

---

### 1.2 Dashboard (`activity_dashboard.xml` + fragments)

| Aspecto | Situação Atual | Problema |
|---------|---------------|----------|
| **welcomeText** | TextView no root do layout, constraint top | Possível sobreposição com toolbar ou conteúdo; parece legado |
| **Bottom nav** | `itemIconTint` e `itemTextColor` = `@color/bronze_gold` | Cores hardcoded; deveria usar `?attr/colorPrimary` |
| **Navegação** | Drawer + BottomNav | Dupla navegação pode confundir; bottom nav só 3 itens (Home, Agenda, Pacientes) |
| **Home** | HomeInteligenteFragment (Compose) com HomeScreen | Bem estruturado; usa MaterialTheme |
| **Fragment antigo** | fragment_home.xml (HomeFragment?) com cards, TabLayout | Pode estar obsoleto; verificar qual é exibido |

**Sugestões:**
- Remover ou reposicionar `welcomeText` se não for usado; o cumprimento já existe no HomeScreen (Compose)
- Substituir `@color/bronze_gold` por `?attr/colorPrimary` no BottomNav
- Simplificar: manter apenas BottomNav para itens principais; Drawer para configurações e demais

---

### 1.3 Cadastro de Paciente (`activity_cadastro_paciente.xml`)

| Aspecto | Situação Atual | Problema |
|---------|---------------|----------|
| **Comprimento** | Formulário muito longo (15+ campos em um único ScrollView) | Sobrecarga cognitiva; sensação de burocracia |
| **Hierarquia** | Títulos de seção em `@color/bronze_gold` 18sp; espaçamento mt-16, mt-12 misturados | Consistência de espaçamento irregular |
| **Texto auxiliar** | "Utilizado para importar o formulário de anamnese" com `@android:color/darker_gray` | Cor hardcoded; não segue tema escuro |
| **Campos** | Nenhuma máscara (CPF, telefone, data) | Usabilidade prejudicada |
| **Botões** | Um só botão Salvar; ProgressBar separado | Loading não integrado ao botão |
| **Agrupamento** | Seções: Dados, Anamnese, Clínico, Financeiro, Contato emergência | Bom agrupamento; poderia usar expansão ou steps |

**Sugestões:**
- Considerar wizard em etapas (Dados básicos → Financeiro → Contato) para reduzir percepção de formulário longo
- Trocar `@android:color/darker_gray` por `?attr/textColorSecondary` ou equivalente do tema
- Padronizar margens (ex.: 12dp entre campos, 24dp entre seções)
- Adicionar máscaras para CPF, telefone e data (ou indicar formato no hint)
- Usar ProgressBar no botão durante salvamento

---

### 1.4 Agenda (`ScheduleFragment` → `WeeklyAgendaScreen` Compose)

| Aspecto | Situação Atual | Problema |
|---------|---------------|----------|
| **Cores** | `agendaColor = colorResource(R.color.bronze_gold)` | Uso correto de recurso |
| **Layout** | Semana, 4 dias, Dia, Mês | Múltiplas visualizações; "4 dias" é pouco usual |
| **Complexidade** | +800 linhas em um único arquivo | Difícil manutenção; muitos componentes inline |
| **Cores de status** | AppointmentForm usa `Color(0xFF4CAF50)`, `Color(0xFF2196F3)`, etc. | Cores hardcoded; deveria usar StatusColors ou MaterialTheme |
| **Paleta de cores** | Lista fixa de hex para cor do agendamento | Hardcoded: "#2196F3", "#43A047", etc. |

**Sugestões:**
- Extrair componentes (ex.: AgendaHeader, DayColumn, AppointmentCard) para arquivos separados
- Substituir cores hardcoded em AppointmentForm e WeeklyAgendaScreen por `MaterialTheme.colorScheme` ou `StatusColors`
- Avaliar manter apenas "Semana" e "Dia" para simplificar

---

### 1.5 Financeiro

#### FinanceiroFragment
| Aspecto | Situação Atual | Problema |
|---------|---------------|----------|
| **Comportamento** | Exibe `fragment_financeiro_loading` e redireciona em 100ms para FinanceiroDashboardActivity | Flash de tela desnecessário; má UX |
| **Navegação** | Fragment no BottomNav abre Activity | Inconsistência: outros itens mostram fragments; aqui abre outra tela |

#### fragment_financeiro.xml (legado?)
| Aspecto | Situação Atual | Problema |
|---------|---------------|----------|
| **Botões** | Três botões iguais (Exportar, Relatório IR, Relatório Sessões) com `backgroundTint="@color/bronze_gold"` | Repetição de estilo; cor correta mas poderia usar style |
| **Títulos** | "Financeiro", "Resumo", "Registros financeiros" centralizados, `@color/bronze_gold` | Muitos títulos de mesmo nível |
| **RecyclerView** | Sem espaçamento/padding definido | Itens podem colar nas bordas |

#### FinanceiroDashboardScreen (Compose)
| Aspecto | Situação Atual | Problema |
|---------|---------------|----------|
| **Design** | Usa MaterialTheme, Material 3 | Bom |
| **Botão "Carregar Dados"** | Proeminente no topo | Estranho exigir ação manual para ver dados; deveria carregar automaticamente |
| **Feedback** | Card de erro, CircularProgressIndicator | Ok |

**Sugestões:**
- Remover redirecionamento do FinanceiroFragment: exibir conteúdo diretamente no fragment (ou migrar FinanceiroDashboard para fragment)
- Evitar flash de loading; carregar dados ao montar a tela
- Padronizar botões em fragment_financeiro com `@style/LoginButton` ou novo `ButtonPrimary`

---

## 2. Simplificação de Layout e Redução de Excesso Visual

| Local | excesso | Ação sugerida |
|-------|--------|---------------|
| Login | Logo + marca + 4 CTAs | Logo menor; links secundários menores |
| Cadastro paciente | 15+ campos em tela única | Wizard ou seções colapsáveis |
| Agenda | 4 modos de visualização | Reduzir para 2 (Semana, Dia) |
| fragment_financeiro | 3 títulos + 3 botões repetidos | Agrupar ações em menu ou FAB |
| Dashboard | welcomeText duplicado | Remover do activity_dashboard |
| Drawer header | 3 textos em bronze_gold | Diferenciar hierarquia (nome em destaque, CRP secundário) |

---

## 3. Hierarquia e Uso de Espaço

| Problema | Recomendação |
|----------|--------------|
| Títulos todos com mesmo peso visual | H1: 24sp bold, H2: 18sp semibold, H3: 16sp medium; usar `?attr/textColorPrimary` e `textColorSecondary` |
| Margens inconsistentes (12dp, 16dp, 24dp, 32dp) | Padronizar: 8dp (compacto), 16dp (padrão), 24dp (seções) |
| Formulários com pouco respiro | Mínimo 16dp entre campos; 24dp entre blocos |
| Cards sem elevation consistente | 2dp para cards padrão; 4dp para cards em destaque |

---

## 4. Padronização de Componentes

### 4.1 Situação Atual

| Componente | Onde existe | Uso |
|------------|-------------|-----|
| `LoginInputLayout` | styles.xml | Login, Cadastro paciente |
| `LoginButton` | styles.xml | Login, Cadastro |
| `GoogleSignInButton` | styles.xml | Login principal |
| `PsiProCard`, `PsiProPrimaryButton` | PsiProComponents.kt | Compose; poucos usos |
| `PsiProColors`, `PsiProDimens` | Theme.kt | Compose |
| `StatusColors` | Theme.kt | Sucesso, erro, aviso — subutilizado |
| Cores de agenda | colors.xml (agenda_*) | Múltiplas variantes; duplicação com bronze_gold |

### 4.2 Inconsistências

- **activity_backend_login** não usa `LoginInputLayout`; usa `Widget.MaterialComponents.TextInputLayout.OutlinedBox` com atributos inline
- **activity_create_account** idem: estilos inline em vez de `LoginInputLayout`
- **fragment_financeiro** usa `Widget.MaterialComponents.Button` direto, sem style
- Compose: AutoavaliacaoScreen, NotificationsScreen, DocumentosScreen, AppointmentForm, FinanceiroComponents usam `Color(0x...)` em vez de tema

---

## 5. Feedback de Erro e Sucesso

| Tela | Situação | Problema |
|------|----------|----------|
| Login | Toast para erro | Ok; poderia ter erro inline no formulário |
| Cadastro paciente | Toast provável | Verificar se há validação e mensagens claras |
| Cadastro paciente | ProgressBar no fim do form | Pouco visível; botão deveria mostrar loading |
| Agenda | Conflito 409 → mensagem amigável | Ok (AppointmentViewModel) |
| Financeiro | Card de erro quando falha | Bom |
| Geral | Sem Snackbar para sucesso em ações críticas | Toast usado; Snackbar seria mais moderno |

---

## 6. Cores Hardcoded e Mistura de Estilos

### 6.1 Cores Hardcoded em Kotlin

| Arquivo | Quantidade | Exemplo |
|---------|------------|---------|
| AutoavaliacaoScreen.kt | ~40 | `Color(0xFFCD7F32)`, `Color(0xFFD2691E)`, `Color(0xFF4CAF50)` |
| AppointmentForm.kt | ~15 | StatusOption com Color hex; paleta de cores |
| FinanceiroComponents.kt | 1 | `Color(0xFF2196F3)` |
| NotificationsScreen.kt | 5 | `Color(0xFFD4AF37)`, `Color.White`, `Color(0xFFF8F9FA)` |
| RelatorioDiarioScreen.kt | 1 | `Color(0xFFFF9800)` |
| DocumentosScreen.kt | 1 | `Color(0xFF1E1E1E)` para detecção de tema |
| PsiproTheme.kt | 1 | `surfaceVariant = Color(0xFF2D2D2D)` |
| Theme.kt (PsiProColors, StatusColors) | 15+ | OK — é o Design System |

### 6.2 Cores em XML

| Uso | Situação |
|-----|----------|
| `@color/bronze_gold` | Consistente em layouts |
| `@android:color/darker_gray` | activity_cadastro_paciente — não respeita tema |
| `@color/surface_black` | item_financeiro — correto |
| Estilos com cores fixas | LoginInputLayout usa `@color/surface_white`, `@color/text_black` — em tema escuro pode conflitar |

### 6.3 Recomendações

- Substituir todas as ocorrências de `Color(0x...)` em Compose por `colorResource(R.color.xxx)` ou `MaterialTheme.colorScheme`
- Criar `colorResource` para status (success, warning, error, info) e usar em AutoavaliacaoScreen, AppointmentForm, FinanceiroComponents
- Trocar `@android:color/darker_gray` por `?attr/textColorSecondary` ou `?android:attr/textColorSecondary`

---

## 7. Design System — Estado e Gaps

### 7.1 O que existe

- **colors.xml** / **colors-night**: Paleta bronze/gold, textos, superfícies, funcionais, agenda (legadas)
- **themes.xml**: Theme.Psipro com colorPrimary, surfaces, font Nunito
- **styles.xml**: LoginButton, LoginInputLayout, GoogleSignInButton, AgendaEventCard, MaterialAlertDialog
- **Theme.kt / PsiProColors / StatusColors / PsiProDimens**: Objeto central para Compose
- **PsiProComponents.kt**: PsiProCard, PsiProPrimaryButton, PsiProTopBar

### 7.2 Gaps

| Gap | Impacto |
|-----|---------|
| Theme.kt e colors.xml não estão alinhados | Valores como `0xFFB8860B` em Theme.kt vs `#B8860B` em colors — podem divergir |
| StatusColors com valores fixos | Não refletem colors.xml (error_red, success_green) |
| Falta de estilo para Botão Secundário (outline) | CreateAccount usa inline; backend_login também |
| Falta de estilo para Botão Danger | Exclusões usam estilos genéricos |
| Agenda usa 9 cores dedicadas (agenda_*) | Duplicam bronze_gold; consolidar em 2–3 |

---

## 8. Plano de Melhoria Priorizado

### Matriz: Impacto x Esforço

| P | Item | Impacto | Esforço |
|---|------|---------|---------|
| P1 | Eliminar cores hardcoded em Compose (Theme/colorResource) | Alto | Médio |
| P2 | Unificar estilos de login/cadastro (LoginInputLayout em todas as telas) | Alto | Baixo |
| P3 | Corrigir FinanceiroFragment (remover redirecionamento/loading desnecessário) | Alto | Baixo |
| P4 | Substituir @color/ @android:color por ?attr no XML onde aplicável | Alto | Baixo |
| P5 | Remover welcomeText duplicado do activity_dashboard | Médio | Baixo |
| P6 | Padronizar BottomNav e Drawer com ?attr/colorPrimary | Médio | Baixo |
| P7 | Simplificar tela de login (logo menor, hierarquia de CTAs) | Médio | Médio |
| P8 | Cadastro paciente: trocar @android:color/darker_gray + padronizar margens | Médio | Baixo |
| P9 | Consolidar cores da agenda (remover legadas, usar colorPrimary) | Médio | Médio |
| P10 | Agenda: extrair componentes e remover cores hardcoded | Médio | Médio |
| P11 | Adicionar loading no botão durante submit (Login, Cadastro) | Médio | Baixo |
| P12 | Alinhar Theme.kt/PsiProColors com colors.xml | Médio | Baixo |
| P13 | Cadastro paciente: avaliar wizard em etapas (opcional) | Baixo | Alto |

---

### Fase 1 — Quick Wins (1 sprint)

| # | Item | Descrição resumida |
|---|------|---------------------|
| P2 | Unificar estilos login/cadastro | Aplicar `LoginInputLayout` e `LoginButton` em `activity_backend_login` e `activity_create_account` |
| P3 | Corrigir FinanceiroFragment | Carregar dados ao abrir; evitar redirecionamento com delay |
| P4 | Atributos de tema em XML | Trocar `@android:color/darker_gray` e semelhantes por `?attr/textColorSecondary` |
| P5 | Remover welcomeText duplicado | Remover TextView do `activity_dashboard` |
| P6 | BottomNav/Drawer com tema | Usar `?attr/colorPrimary` em vez de `@color/bronze_gold` |
| P8 | Cadastro paciente | Corrigir cor do hint + padronizar margens (12dp/24dp) |
| P11 | Loading no botão | Mostrar ProgressBar no botão durante login/cadastro |

---

### Fase 2 — Consistência do Design System (1–2 sprints)

| # | Item | Descrição resumida |
|---|------|---------------------|
| P1 | Cores em Compose | Substituir `Color(0x...)` por `colorResource` ou `MaterialTheme` em AutoavaliacaoScreen, AppointmentForm, NotificationsScreen, FinanceiroComponents, RelatorioDiarioScreen, DocumentosScreen |
| P9 | Consolidar agenda | Reduzir/remover `agenda_*`; usar `colorPrimary` e variantes |
| P10 | Agenda refactor | Extrair composables; usar MaterialTheme/StatusColors |
| P12 | Alinhar Theme.kt | Garantir que PsiProColors/StatusColors espelhem colors.xml |

---

### Fase 3 — Refinamentos (1 sprint)

| # | Item | Descrição resumida |
|---|------|---------------------|
| P7 | Simplificar login | Reduzir logo; reorganizar CTAs (primário vs secundários) |
| P13 | Cadastro em etapas | Avaliar wizard (Dados → Financeiro → Contato) — opcional |

---

## 9. Checklist de Consistência

- [ ] Nenhuma cor hex em código Kotlin exceto em Theme.kt/PsiProColors
- [ ] Nenhum `@android:color/` em layouts — usar `?attr` ou `@color/`
- [ ] Todos os botões primários usam `@style/LoginButton` ou equivalente
- [ ] Todos os TextInputLayout usam `LoginInputLayout` ou variante
- [ ] Margens padronizadas: 8dp, 16dp, 24dp
- [ ] Compose sempre usa `MaterialTheme.colorScheme` ou `colorResource`

---

## 10. Resumo Executivo

| Categoria | Principais achados |
|-----------|--------------------|
| **Cores hardcoded** | ~65 ocorrências em 8 arquivos Kotlin; Theme.kt centralizado mas subutilizado |
| **Estilos misturados** | backend_login e create_account não usam LoginInputLayout; financeiro usa estilos inline |
| **Excesso visual** | Login com muitos elementos; cadastro paciente muito longo |
| **Layout** | welcomeText possivelmente duplicado; FinanceiroFragment com redirecionamento estranho |
| **Feedback** | Loading fora do botão; Toast OK; Snackbar poderia ser padrão para sucesso |

**Prioridade máxima:** Fase 1 (P2–P6, P8, P11) — alto impacto, baixo esforço, base para as demais melhorias.
