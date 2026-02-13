# Assets de Marca PsiPro

Esta pasta contém os arquivos de identidade visual do PsiPro.

## Arquivos necessários

### 1. Logo Principal
- **Arquivo:** `logo-psipro.svg`
- **Formato:** SVG
- **Recomendações:**
  - Altura: 32px (máximo 36px)
  - Largura: proporcional
  - Usar `fill="currentColor"` para suporte a temas claro/escuro
  - Ou criar versões separadas: `logo-psipro-light.svg` e `logo-psipro-dark.svg`

### 2. Ícone do App
- **Arquivo:** `icon-psipro.svg` ou `icon-psipro.png`
- **Formato:** SVG (preferencial) ou PNG
- **Tamanho:** 512x512px
- **Uso:** Favicon e ícone da aplicação

### 3. Favicon
- **Arquivo:** `favicon.ico`
- **Formato:** ICO
- **Tamanhos:** 16x16, 32x32, 48x48 (múltiplos tamanhos em um arquivo)
- **Localização:** `/app/favicon.ico` (raiz do app)

## Como substituir os arquivos placeholder

1. **Logo:**
   - Substitua `/public/brand/logo-psipro.svg` pelo arquivo oficial
   - Se usar versões separadas para claro/escuro, atualize `Header.tsx` para alternar entre elas

2. **Ícone:**
   - Substitua `/public/brand/icon-psipro.svg` pelo arquivo oficial
   - Ou use PNG: `/public/brand/icon-psipro.png` (atualize `layout.tsx` se necessário)

3. **Favicon:**
   - Substitua `/app/favicon.ico` pelo arquivo oficial
   - Ou crie um arquivo `icon.png` em `/app/` (Next.js detecta automaticamente)

## Notas técnicas

- Os SVGs devem usar `fill="currentColor"` para funcionar com o sistema de temas
- A logo no Header tem altura fixa de 32px (`h-8`)
- O Next.js otimiza automaticamente imagens via componente `Image`
- O favicon é configurado via metadata em `app/layout.tsx`

## Estrutura atual

```
/public/brand/
  ├── logo-psipro.svg (placeholder - substituir)
  ├── icon-psipro.svg (placeholder - substituir)
  └── README.md (este arquivo)

/app/
  └── favicon.ico (substituir se necessário)
```





