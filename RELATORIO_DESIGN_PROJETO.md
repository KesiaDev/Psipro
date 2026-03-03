# Relatório de Design — Projeto PsiPro

**Data:** 2025  
**Objetivo:** Análise do sistema de design e sugestões para padrão premium moderno (estilo SaaS/Web)

---

## 1. Arquivo Theme.kt Completo

### 1.1 `Theme.kt` (Cores e Dimensões)

```kotlin
package com.psipro.app.ui.compose

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object PsiProColors {
    val GoldPrimary = Color(0xFFB8860B)
    val GoldPrimaryDark = Color(0xFF8B6914)
    val BackgroundDark = Color(0xFF121212)
    val CardDark = Color(0xFF1E1E1E)
    val BorderGoldSoft = Color(0x4DB8860B) // Gold 30% opacidade

    val OnPrimary = Color.Black
    val OnBackground = Color.White
    val OnSurface = Color.White
    val OnSurfaceVariant = Color(0xFFAAAAAA)
}

object StatusColors {
    val Success = Color(0xFF4CAF50)
    val Warning = Color(0xFFFF9800)
    val Error = Color(0xFFF44336)
    val Info = Color(0xFF2196F3)
    val Neutral = Color(0xFF9E9E9E)
    val SuccessBackground = Color(0xFFE8F5E8)
    val WarningBackground = Color(0xFFFFF3E0)
    val ErrorBackground = Color(0xFFFFEBEE)
    val NeutralBackground = Color(0xFFF5F5F5)
}

object PsiProDimens {
    val RadiusDefault: Dp = 20.dp
    val RadiusSmall: Dp = 12.dp
    val RadiusLarge: Dp = 24.dp
}
```

### 1.2 `PsiproTheme.kt` (Tema Material 3)

```kotlin
package com.psipro.app.ui.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PsiProDarkColorScheme = darkColorScheme(
    primary = PsiProColors.GoldPrimary,
    onPrimary = PsiProColors.OnPrimary,
    secondary = PsiProColors.GoldPrimaryDark,
    onSecondary = PsiProColors.OnPrimary,
    background = PsiProColors.BackgroundDark,
    onBackground = PsiProColors.OnBackground,
    surface = PsiProColors.CardDark,
    onSurface = PsiProColors.OnSurface,
    outline = PsiProColors.GoldPrimary,
    onSurfaceVariant = PsiProColors.OnSurfaceVariant,
    surfaceVariant = Color(0xFF2D2D2D)
)

@Composable
fun PsiproTheme(
    useDarkTheme: Boolean = true, // Parâmetro não utilizado
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PsiProDarkColorScheme,
        typography = androidx.compose.material3.Typography(), // Default
        content = content
    )
}
```

**Observações:**
- Não há `shape` customizado no MaterialTheme
- `Typography` usa o padrão do Material 3 (Roboto)
- Parâmetro `useDarkTheme` existe mas é ignorado (app sempre escuro)

---

## 2. Tipografia Utilizada

### 2.1 XML / View System

| Atributo | Valor |
|----------|-------|
| **Fonte principal** | Nunito |
| **Definição** | `@font/nunito` (font-family com regular, italic, bold, bold-italic) |
| **Arquivos** | `nunito.xml`, `nunito_regular`, `nunito_italic`, `nunito_bold`, `nunito_bold_italic` |
| **Aplicação** | `themes.xml` → `fontFamily="@font/nunito"` |

### 2.2 Jetpack Compose

| Uso | Estilo |
|-----|--------|
| **MaterialTheme.typography** | Padrão do Material 3 (Roboto) |
| **Estilos mais usados** | `bodyMedium`, `titleMedium`, `titleLarge`, `bodySmall`, `headlineMedium` |
| **Customização** | Nenhuma – Typography padrão |

### Inconsistência

- XML usa **Nunito**
- Compose usa **Roboto** (padrão)
- Resultado: telas XML e Compose com fontes diferentes.

---

## 3. Sistema de Cores

### 3.1 Compose (`PsiProColors`)

| Cor | Hex | Uso |
|-----|-----|-----|
| GoldPrimary | `#B8860B` | Primária (botões, acentos) |
| GoldPrimaryDark | `#8B6914` | Secundária |
| BackgroundDark | `#121212` | Fundo geral |
| CardDark | `#1E1E1E` | Cards, superfícies |
| BorderGoldSoft | `#4DB8860B` (30% alpha) | Bordas |
| OnSurfaceVariant | `#AAAAAA` | Texto secundário |

### 3.2 XML (`colors.xml`)

| Cor | Hex | Observação |
|-----|-----|------------|
| bronze_gold | `#B8860B` | Alinhado ao Compose |
| bronze_gold_dark | `#8B6914` | Alinhado |
| primary_bronze | `#CD7F32` | Bronze diferente |
| secondary_bronze | `#B87333` | Outro tom |
| accent_bronze | `#D2691E` | Outro tom |
| colorPrimary (Material) | `#D1A054` | Valor diferente |

### 3.3 values-night (`colors.xml`)

| Cor | Hex | Observação |
|-----|-----|------------|
| bronze_gold | `#9e742c` | Tom diferente do Compose |
| bronze_gold_dark | `#9e742c` | Igual ao gold (sem variação) |
| background_black | `#121212` | OK |
| surface_black | `#1E1E1E` | OK |

### 3.4 Problemas

1. Três paletas diferentes: Compose, XML light, XML dark
2. Bronze (#CD7F32) vs Gold (#B8860B) em usos paralelos
3. `colorPrimary` XML (`#D1A054`) ≠ `PsiProColors.GoldPrimary` (`#B8860B`)

---

## 4. Material 3 Configurado Corretamente?

### O que está correto

| Item | Status |
|------|--------|
| `darkColorScheme` | OK |
| Tokens principais (primary, surface, etc.) | OK |
| Uso de `MaterialTheme.colorScheme` | Parcial |
| Componentes Material 3 | OK (Button, Card, TextField, etc.) |

### O que está incorreto ou incompleto

| Item | Problema |
|------|----------|
| **Typography** | Usa default; sem fonte Nunito no Compose |
| **Shapes** | Não definidos no tema; `PsiProDimens` usado manualmente |
| **`outlinedTextFieldColors`** | API deprecada (`TextFieldDefaults.colors` é a nova) |
| **`tertiary`** | Não definido no `ColorScheme` |
| **`inverseSurface`, `inverseOnSurface`** | Não definidos |

### Conclusão

Material 3 está parcialmente configurado: esquema de cores funciona, mas falta tipografia, shapes e ajuste de APIs deprecadas.

---

## 5. Componentes Reutilizáveis Existentes

### 5.1 `PsiProComponents.kt`

| Componente | Descrição | Adoção |
|------------|-----------|--------|
| **PsiProCard** | Card com borda opcional dourada, raio 20dp | Baixa – definido mas pouco usado |
| **PsiProPrimaryButton** | Botão dourado full-width | Baixa |
| **PsiProTopBar** | TopAppBar com back e ações | Baixa |

### 5.2 Componentes Locais (não compartilhados)

| Componente | Localização | Descrição |
|------------|-------------|-----------|
| **themedOutlinedTextField** | `SimplifiedAnamneseScreen.kt` | OutlinedTextField com estilos do tema – deveria estar em componente global |
| **customTextFieldColors** | `AppointmentForm.kt` | Cores de TextField locais |
| **StatusOption/Chip colors** | `FinanceiroComponents.kt` | Cores de status hardcoded |

### 5.3 Uso de Componentes

- **PsiProCard**: usado principalmente em `SimplifiedAnamneseScreen`
- **PsiProPrimaryButton**: uso restrito
- **PsiProTopBar**: uso restrito
- Maioria das telas usa `Card`, `Button`, `OutlinedTextField` diretos, sem wrapper

### Componentes faltando

- `PsiProOutlinedButton`
- `PsiProTextField` (com `OutlinedTextFieldDefaults.colors`)
- `PsiProFilledCard` / `PsiProElevatedCard`
- `PsiProChip` / `PsiProFilterChip`
- `PsiProDialog`
- `PsiProSnackbar`

---

## 6. Inconsistências Visuais

### 6.1 Cores hardcoded

| Arquivo | Exemplo | Problema |
|---------|---------|----------|
| `AutoavaliacaoScreen.kt` | `Color(0xFFCD7F32)`, `Color(0xFF1A1A1A)` | ~40 ocorrências; ignora MaterialTheme |
| `FinanceiroComponents.kt` | `Color(0xFF2196F3)` | Status sem tokens semânticos |
| `AppointmentAdapter.kt` | `#4CAF50`, `#2196F3` | Cores em XML do adapter |
| `NotificationsScreen.kt` | `Color(0xFFD4AF37)`, `Color.White` | Branco em tema escuro |
| `RelatorioDiarioScreen.kt` | `Color(0xFFFF9800)` | Laranja hardcoded |
| `AppointmentForm.kt` | `Color(0xFF4CAF50)` etc. | `colorResource` misturado com hardcode |

### 6.2 Modo claro em app escuro

- `AutoavaliacaoScreen.kt` usa `isSystemInDarkTheme()` e fallbacks para light
- App declara tema escuro único
- Resultado: lógica de tema redundante e possível inconsistência

### 6.3 Paletas distintas

- Compose: dourado (#B8860B)
- XML: bronze (#CD7F32) e gold
- values-night: #9e742c
- Falta uma única fonte de verdade

### 6.4 Tipografia

- Nunito no XML
- Roboto no Compose
- Falta tipografia unificada

### 6.5 Bordas e raios

- `PsiProDimens.RadiusDefault = 20.dp`
- `RoundedCornerShape(16.dp)` em vários lugares (ex.: `BillingDialog`, `AppointmentForm`)
- Inconsistência entre 12, 16, 20 e 24 dp

---

## 7. Sugestões Técnicas para Padrão Premium Moderno (SaaS/Web)

### 7.1 Sistema de Design Unificado

```
design/
├── theme/
│   ├── PsiProTheme.kt      # Theme completo
│   ├── PsiProColor.kt      # Única fonte de cores
│   ├── PsiProTypography.kt # Tipografia Nunito no Compose
│   └── PsiProShape.kt      # Shapes padronizados
└── components/
    ├── PsiProButton.kt     # Primário, Secundário, Text
    ├── PsiProCard.kt       # Default, Outlined, Filled
    ├── PsiProTextField.kt  # TextField padronizado
    ├── PsiProTopBar.kt
    ├── PsiProDialog.kt
    └── PsiProChip.kt       # Status chips
```

### 7.2 Tipografia Compose (Nunito)

```kotlin
val PsiProTypography = Typography(
    displayLarge = TextStyle(fontFamily = Nunito, fontSize = 57.sp, ...),
    headlineLarge = TextStyle(fontFamily = Nunito, fontSize = 32.sp, ...),
    titleLarge = TextStyle(fontFamily = Nunito, fontSize = 22.sp, ...),
    bodyLarge = TextStyle(fontFamily = Nunito, fontSize = 16.sp, ...),
    labelLarge = TextStyle(fontFamily = Nunito, fontSize = 14.sp, ...)
)
```

- Adicionar `FontFamily` com `Font(R.font.nunito_regular)` etc.
- Usar em `MaterialTheme(typography = PsiProTypography)`

### 7.3 Paleta de Cores Única

- Remover duplicação entre XML e Compose
- Definir tokens em um único lugar e mapear para Compose e XML
- Manter apenas tema escuro, removendo lógica de light no código
- Incluir `tertiary` e cores semânticas (ex.: `success`, `warning`) no ColorScheme

### 7.4 Shapes no MaterialTheme

```kotlin
val PsiProShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp)
)
```

### 7.5 Componentes Estilo SaaS/Web

| Componente | Características |
|------------|-----------------|
| **Botões** | Estados hover/pressed, loading, ícones alinhados |
| **Cards** | Elevação sutil, bordas discretas, padding consistente |
| **TextFields** | Floating label, helper/erro, ícones opcionais |
| **Dialogs** | Bordas arredondadas, animações, responsivos |
| **Listas** | Dividers discretos, densidade uniforme |
| **Chips** | Status com cor semântica e contraste adequado |

### 7.6 Migrar APIs Deprecadas

- Substituir `outlinedTextFieldColors(...)` por `OutlinedTextFieldDefaults.colors(...)`
- Usar `OutlinedTextFieldDefaults.colors` com parâmetros por estado quando disponível
- Revisar uso de `Icons.Default.ArrowBack` e migrar para `Icons.AutoMirrored.Filled.ArrowBack` onde aplicável

### 7.7 Ajustes de UX Premium

1. **Microinterações**: ripple, transições suaves
2. **Espaçamento**: grid 4/8 dp
3. **Hierarquia**: `displayLarge` para títulos, `bodyMedium` para corpo
4. **Contraste**: garantir WCAG AA em textos
5. **Consistência**: só componentes do design system em telas novas

### 7.8 Checklist de Implementação

- [ ] Criar `PsiProTypography` com Nunito
- [ ] Definir `PsiProShapes` no Theme
- [ ] Unificar cores em um único arquivo
- [ ] Mover `themedOutlinedTextField` para `PsiProTextField`
- [ ] Criar `PsiProSecondaryButton`, `PsiProOutlinedButton`
- [ ] Padronizar raios (ex.: 12, 16, 20 dp)
- [ ] Remover cores hardcoded e usar `MaterialTheme.colorScheme`
- [ ] Remover fallbacks de light theme
- [ ] Documentar design system em Markdown
- [ ] Adotar componentes em pelo menos 80% das telas Compose

---

## Resumo Executivo

| Aspecto | Estado Atual | Recomendação |
|---------|--------------|--------------|
| Cores | Três paletas conflitantes | Única fonte de verdade |
| Tipografia | XML Nunito / Compose Roboto | Nunito em todo o app |
| Material 3 | Parcial | Completar (Typography, Shapes) |
| Componentes | 3 componentes, baixa adoção | Ampliar e padronizar uso |
| Consistência | Muitas inconsistências | Design system formal |
| APIs | Algumas deprecadas | Migrar para APIs atuais |

---

*Relatório gerado com base em análise do código-fonte do projeto PsiPro.*
