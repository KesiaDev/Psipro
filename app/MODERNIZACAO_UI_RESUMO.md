# Modernização da UI - PsiPro Android

## Resumo das alterações

### 1. Design System (`ui/theme/psipro/`)

| Arquivo | Descrição |
|---------|-----------|
| **PsiproColors.kt** | Paleta principal: Primary #C89B3C, Background Dark #0F0F10, Surface Dark #1A1A1D, Text Primary/Secondary |
| **PsiproSpacing.kt** | Sistema de espaçamento (xs, sm, md, lg, xl, xxl) alinhado ao Web |
| **PsiproShapes.kt** | Bordas arredondadas: Small (8dp), Medium (12dp), Large (16dp), Card/Button |
| **PsiproTypography.kt** | Hierarquia: HeadlineLarge, TitleMedium, BodyMedium, LabelSmall |
| **PsiproTheme.kt** | Tema Material 3 com Dark e Light Mode |

### 2. Componentes reutilizáveis (`ui/components/`)

| Componente | Características |
|------------|-----------------|
| **PsiproCard** | Bordas 16dp, sombra suave, animação scale ao clicar |
| **PsiproButton** | Primário dourado, animação scale |
| **PsiproInput** | OutlinedTextField moderno, cores do tema |
| **PsiproTopBar** | Minimalista, título, botão voltar |
| **PsiproFloatingButton** | FAB maior, sombra suave, animação scale |
| **PsiproListItem** | Avatar, título, subtítulo, trailing, animação |
| **PsiproEmptyState** | Ícone, título, subtítulo, botão de ação |
| **EmptyStates** | EmptyPatients, EmptyAgenda, EmptySessions |

### 3. Tela de Pacientes redesenhada

- **PatientsScreen** (Compose): cards modernos, avatar circular com inicial, nome em destaque, telefone/email, botão agendar
- **EmptyState** elegante quando não há pacientes
- **FAB** moderno para cadastrar
- **Campo de busca** estilizado
- **Animações**: AnimatedVisibility, fadeIn/fadeOut, slideInVertically

### 4. Integração

- **PatientsFragment** migrado para ComposeView com PsiproTheme
- Fluxo: ViewModel (StateFlow) → collectAsState → PatientsScreen

### 5. Cores adicionadas (`colors.xml`)

- `psipro_primary`: #C89B3C
- `psipro_background_dark`: #0F0F10
- `psipro_surface_dark`: #1A1A1D

## Próximos passos (opcional)

1. **Agenda**: Aplicar mesmo design system ao ScheduleFragment
2. **Dashboard**: Atualizar HomeFragment com cards modernos
3. **Drawer/BottomNav**: Atualizar com ícones maiores, cor ativa dourada
4. **Fontes**: Adicionar Inter ou Poppins em `res/font/` e atualizar PsiproTypography

## Preview das telas

A tela de **Pacientes** foi completamente redesenhada. Para visualizar:
1. Build e run do app
2. Navegar para a aba "Pacientes"
3. Nova interface com cards, empty state e FAB modernos
