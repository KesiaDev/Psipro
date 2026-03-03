# Análise Completa para Refatoração de Design — App Android PsiPro

**Objetivo:** Transformar o app visualmente equivalente ao Web (psipro-dashboard Lovable), com design premium e estética SaaS.

---

## 1) ONDE EXISTEM CORES HARDCODED

### Compose (Kotlin)

| Arquivo | Linha / Local | Problema |
|---------|---------------|----------|
| **EditorModeloAnamneseScreen.kt** | 91 | `Color.Gray` hardcoded |
| **ResumoFinanceiroScreen.kt** | 97, 103, 109, 125, 131, 137, etc. | `Color.White` múltiplos |
| **WeeklyAgendaScreen.kt** | 253, 480, 375 | `Color.Black.copy(alpha=0.3)`, `Color(parseColor)`, `Color.Transparent` |
| **FinanceiroComponents.kt** | 20-23, 43-46 | `Color.White` em status badges; `Color(0xFF2196F3)` para REALIZADO |
| **DocumentosScreen.kt** | 60 | `Color(0xFF1E1E1E)` para detecção dark |
| **MenuAnamneseScreen.kt** | 90 | `onSurface.copy(alpha = 0.7f)` |
| **HomeScreen.kt** | 218 | `Color.Red.copy(alpha = 0.1f)` em ErrorCard |
| **AnamneseListScreen.kt** | 62-63, 80 | `bronze` variável local, não design system |
| **FinanceiroPacienteScreen.kt** | 176, 182, 188, 283, 294, 301 | `Color.White` em cards de status |
| **BillingDialog.kt** | Múltiplos | `MaterialTheme.colorScheme.*` — OK; mas usa `Card()` direto |
| **VidaEmocionalScreen.kt** | 41 | `MaterialTheme.colorScheme.error` — OK |

### XML (layout/drawable)

| Arquivo | Problema |
|---------|----------|
| **drawable/rounded_background.xml** | `#1A1A1A` hardcoded |
| **dialog_appointment.xml** | `#1A1A1A` |
| **rounded_edittext_bg.xml** | `#FFD7B98A` |
| **circle_button_background.xml** | `#99000000` |
| **modern_gradient_background.xml** | `#F8F9FA`, `#E9ECEF`, `#DEE2E6` |
| **values/colors.xml** | ~40 cores — OK se centralizadas; mas duplicadas em values-night |
| **item_appointment.xml** | `tools:chipBackgroundColor="#2196F3"` |

### Resumo de cores hardcoded

- **~15 arquivos** com `Color.*` ou hex em Compose
- **~8 drawables/layouts** com hex em XML
- **StatusColors** e **PsiProColors** existem, mas não são usados consistentemente

---

## 2) ONDE HÁ COMPONENTES NÃO PADRONIZADOS

### Uso direto de `Card()` (sem PsiProCard)

| Arquivo | Quantidade | Contexto |
|---------|------------|----------|
| HomeScreen.kt | 5 | NextAppointmentCard, SummaryCard, ErrorCard, MainStateCard |
| FinanceiroDashboardScreen.kt | 5+ | Cards de métricas, lista |
| FinanceiroPacienteScreen.kt | 6+ | Cards de status, cobranças |
| ResumoFinanceiroScreen.kt | 6 | Cards de métricas Success/Warning/Error/Info |
| NovaSessaoScreen.kt | 2+ | Cards de seleção |
| WeeklyAgendaScreen.kt | 2+ | AppointmentCard, overlay |
| BillingDialog.kt | 3 | Cards de diálogo |
| DocumentosScreen.kt | Múltiplos | Lista, diálogos |
| DetalheCobrancaScreen.kt | 3 | Cards de cobrança |
| AutoavaliacaoScreen.kt | 5+ | StatCard, Cards de resposta |
| MenuPrincipalScreen.kt | 2 | MenuCard |
| MenuAnamneseScreen.kt | 2 | Cards de seção |
| AnamneseListScreen.kt | 3 | Cards de modelo |
| ArquivosScreen.kt | 3+ | ArquivoCard, diálogos |
| HistoricoMedicoScreen.kt | 1 | Card de histórico |
| ObservacoesClinicasScreen.kt | 1 | Card |
| RelatorioDiarioScreen.kt | 4 | Cards |
| EditorModeloAnamneseScreen.kt | 1 | Card de campo |
| AnamneseFormScreen.kt | 2 | Cards |
| AnamneseSection.kt | 1 | Card |
| AppointmentForm.kt | 3+ | SegmentedButton, Card |
| SimplifiedAnamneseScreen.kt | 1 | Card |
| AnotacoesSessaoScreen.kt | 2 | Cards |
| NotificationsScreen.kt | 1 | Card |
| ModelosAnamneseScreen.kt | 1 | Card |
| AnamneseModelEditScreen.kt | 2 | Cards |
| HistoricoFamiliarScreen.kt | 1 | Card |

**Total estimado: ~70+ usos de `Card()` direto.**

### Uso direto de `Button()` (sem PsiProPrimaryButton)

| Arquivo | Quantidade |
|---------|------------|
| HomeScreen.kt | 3 |
| NovaSessaoScreen.kt | 4+ |
| FinanceiroDashboardScreen.kt | 1 |
| FinanceiroPacienteScreen.kt | 6+ |
| BillingDialog.kt | 4 |
| AutoavaliacaoScreen.kt | 2+ |
| AnamneseListScreen.kt | 2 |
| AnamneseSection.kt | 4 |
| AnamneseFormScreen.kt | 1 |
| FormularioAnamneseScreen.kt | 4 |
| EditorModeloAnamneseScreen.kt | 2 |
| AnamneseCompleteFlow.kt | 1 |
| AnamneseModelEditScreen.kt | 2 |
| AgendamentoTabsScreen.kt | 1 |
| HistoricoMedicoScreen.kt | 1 |
| HistoricoFamiliarScreen.kt | 1 |
| ObservacoesClinicasScreen.kt | 2 |
| AudioTranscriptionScreen.kt | 4 |
| AppointmentForm.kt | 1 |
| DetalheCobrancaScreen.kt | 2 |

**Total estimado: ~50+ usos de `Button()` direto.**

### Componentes não existentes no design system

- **PsiProStatCard** — não existe; métricas usam `Card()` genérico
- **PsiProSectionTitle** — não existe; títulos usam `Text()` direto
- **PsiProSurfaceCard** — não existe; equivalente parcial em PsiProCard
- **PsiProMetricValue** — não existe; números usam `Text()` com `fontSize` variado
- **PsiProInput** — não existe; inputs são Material padrão
- **PsiProStatusBadge** — não existe; FinanceiroComponents tem funções próprias

---

## 3) ONDE O LAYOUT ESTÁ SIMPLES DEMAIS

### Dashboard (HomeScreen.kt)

- **GreetingHeader:** Existe `HomeHeader` mas não segue padrão Web (greeting + data)
- **Métricas:** `SummaryCardsRow` usa cards pequenos (120.dp) em LazyRow; Web usa grid 2 colunas com métricas grandes
- **Consultas de Hoje:** Lista inline; Web usa SectionCard com lista diferenciada
- **Espaçamento:** 20.dp — Web usa 24–32dp entre blocos
- **Tipografia:** Títulos sem hierarquia forte; números não em destaque grande

### Login (MainActivity.kt / activity_main.xml)

- Layout XML tradicional (não Compose)
- Inputs Material padrão (TextInputLayout)
- Fundo: NestedScrollView simples, sem gradiente ou elevação
- Sem card central elevado
- Botão Google com estilo básico

### Drawer (activity_dashboard.xml / navigation_view)

- NavigationView Material padrão
- Sem item ativo destacado visualmente
- Avatar simples
- Sem separação clara entre seções (Home/Agenda vs Config/Suporte)
- Sem transição customizada

### Agenda (WeeklyAgendaScreen.kt)

- Layout em colunas que lembra tabela
- Horários com tipografia básica
- `AppointmentCard` com `Card()` direto, sem borda dourada
- Horários vazios vs ocupados pouco diferenciados
- Status com chip mas sem badge padronizado

### Outras telas

- **FinanceiroDashboardScreen:** Grid de cards; falta hierarquia e espaçamento maior
- **FinanceiroPacienteScreen:** Lista densa; cards de status repetitivos
- **ArquivosScreen / DocumentosScreen:** Listas básicas
- **Anamnese:** Múltiplos fluxos; cards e botões sem padrão único

---

## 4) PLANO DE REFATORAÇÃO EM ETAPAS

### FASE 0 — Preparação (pré-requisitos)

1. **Restaurar suporte a modo claro e escuro**  
   - Reverter `App.kt`/`MainActivity` que forçam `MODE_NIGHT_YES`  
   - Garantir `PsiproTheme` com `LightColors` e `DarkColors` (já parcialmente existe)

2. **Expandir Theme.kt**  
   - Adicionar variantes para light: `BackgroundLight`, `CardLight`, `BorderLight`  
   - Manter `BorderGoldSoft` para dark; criar `BorderLight` para light  
   - Garantir `PsiProDimens.RadiusDefault = 20.dp` em todo o sistema

3. **Criar componentes do design system**  
   - `PsiProStatCard(title, value, icon, modifier)`  
   - `PsiProSectionTitle(text)`  
   - `PsiProSurfaceCard(borderGold, content)` — wrapper de PsiProCard  
   - `PsiProMetricValue(value)` — Text com typography display/large  
   - `PsiProInput` — opcional; ou padronizar OutlinedTextField com estilo único  
   - `PsiProStatusBadge(status)` — unificar FinanceiroComponents

4. **Eliminar cores hardcoded**  
   - Substituir `Color.White`, `Color.Gray`, `Color.Red`, etc. por `PsiProColors.*` ou `MaterialTheme.colorScheme.*`  
   - Atualizar drawables para usar `?attr/color*` onde possível

---

### FASE 1 — Design system completo

| Tarefa | Arquivos | Prioridade |
|--------|----------|------------|
| Expandir PsiProColors (light + dark) | Theme.kt | Alta |
| Criar PsiProStatCard | PsiProComponents.kt | Alta |
| Criar PsiProSectionTitle | PsiProComponents.kt | Alta |
| Criar PsiProSurfaceCard | PsiProComponents.kt | Alta |
| Criar PsiProMetricValue | PsiProComponents.kt | Alta |
| Criar PsiProStatusBadge | PsiProComponents.kt ou FinanceiroComponents.kt | Média |
| Auditar e remover Color() hardcoded | ~15 arquivos | Alta |
| Substituir Card() por PsiProCard/PsiProSurfaceCard | ~25 arquivos | Alta |
| Substituir Button() por PsiProPrimaryButton onde primário | ~20 arquivos | Média |

---

### FASE 2 — Dashboard completo

| Tarefa | Descrição |
|--------|-----------|
| GreetingHeader | "Bom dia, Dra Maria 👋" + subtítulo com data |
| Grid de métricas | 2 colunas adaptável; PsiProStatCard; Pacientes, Sessões hoje, Pendências, Receita |
| Card "Consultas de Hoje" | PsiProSurfaceCard grande; lista com fundo diferenciado |
| Status | Badge Confirmado/Pendente com PsiProStatusBadge |
| Espaçamento | 16–24dp entre blocos |
| Arquivos | HomeScreen.kt, HomeViewModel.kt (ajustar uiState) |

---

### FASE 3 — Login premium

| Tarefa | Descrição |
|--------|-----------|
| Migrar para Compose | Ou manter XML com estilos refinados |
| Card central elevado | Fundo elegante (gradiente sutil); card com elevação |
| Inputs customizados | Bordas 20.dp; estilo alinhado ao Web |
| Botão primário | PsiProPrimaryButton |
| Google button | Estilo secundário refinado |
| Arquivos | MainActivity.kt (Compose) ou activity_main.xml + styles |

---

### FASE 4 — Drawer refinado

| Tarefa | Descrição |
|--------|-----------|
| Item ativo destacado | Background + ícone cor primária |
| Avatar | CircleImage com borda dourada; nome/email |
| Separação | Divisor entre seções principais e secundárias |
| Transição | Animação suave ao abrir/fechar |
| Arquivos | activity_dashboard.xml, menu/drawer_menu.xml, DashboardActivity.kt |

---

### FASE 5 — Agenda refinada

| Tarefa | Descrição |
|--------|-----------|
| Horários | Tipografia melhor; PsiProSectionTitle para dias |
| Sessões | PsiProSurfaceCard arredondada |
| Vazio vs ocupado | Cor/fundo diferenciado |
| Status | PsiProStatusBadge em cada item |
| Arquivos | WeeklyAgendaScreen.kt, AppointmentForm.kt |

---

### FASE 6 — Telas secundárias (ondemand)

- FinanceiroDashboardScreen  
- FinanceiroPacienteScreen  
- DocumentosScreen, ArquivosScreen  
- Anamnese (fluxos)  
- NotificationsScreen  

---

## 5) REFERÊNCIA VISUAL DO WEB (Lovable)

### Cores (globals.css)

- **Dark:** background `#121212`, surface `#1E1E1E`, elevated `#232323`
- **Border:** `#2E2E2E` (dark), `#E0E0E0` (light)
- **Primary/Gold:** `#B08D57`, `#B8860B` (gold-dark)

### Componentes Web

- **MetricCard:** `rounded-lg border border-psipro-border p-6`; valor `text-3xl font-bold`
- **SectionCard:** `rounded-lg border`; título `text-lg font-semibold`; `p-6`
- **Espaçamento:** `gap-5`, `gap-6`, `mb-6`, `mb-8`
- **Dashboard layout:** Grid 5 colunas (desktop), 2 colunas (tablet), 1 (mobile)

---

## 6) ORDEM DE EXECUÇÃO RECOMENDADA

1. **Fase 0 + Fase 1** — Design system e remoção de hardcodes (base para tudo)
2. **Fase 2** — Dashboard (maior impacto visual)
3. **Fase 3** — Login (primeira impressão)
4. **Fase 4** — Drawer (navegação diária)
5. **Fase 5** — Agenda
6. **Fase 6** — Demais telas conforme prioridade

---

## 7) RISCOS E CUIDADOS

- **Não misturar XML e Compose** — Login e Drawer estão em XML; migrar com cuidado ou refinar XML
- **Performance** — LazyColumn/LazyVerticalGrid já usados; manter
- **Arquitetura MVVM** — Não alterar ViewModels; apenas UI
- **Sem dependências pesadas** — Usar Material 3 + Compose nativo
- **Testes** — Validar em modo claro e escuro após cada fase
