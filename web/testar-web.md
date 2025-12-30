# 🧪 Checklist de Testes - PsiPro Web

## ✅ Pré-requisitos

1. **Servidor Next.js rodando**
   ```bash
   cd web
   npm run dev
   ```
   - Acesse: `http://localhost:3001` (ou porta mostrada no terminal)

2. **Arquivo `.env.local` criado** (opcional, mas recomendado)
   ```bash
   NEXT_PUBLIC_API_URL=http://localhost:3000
   ```

3. **Backend rodando** (opcional para testes completos)
   - Backend na porta 3000
   - Ou ajustar `NEXT_PUBLIC_API_URL` no `.env.local`

---

## 🧪 Testes por Página

### 1. Landing Page (`/`)
- [ ] Página carrega sem erros
- [ ] Hero section aparece
- [ ] Botões "Conhecer a plataforma" e "Solicitar acesso" funcionam
- [ ] Seções: Problema, Proposta, Como funciona, Diferenciais
- [ ] Footer aparece
- [ ] Modal de solicitação de acesso abre ao clicar em "Solicitar acesso"
- [ ] Tema claro/escuro funciona (se houver toggle)

### 2. Página Beta (`/beta`)
- [ ] Página carrega
- [ ] Título e subtítulo aparecem
- [ ] Seção "O que é o beta" está visível
- [ ] Botão "Solicitar acesso ao beta" abre formulário
- [ ] Formulário tem todos os campos:
  - Nome completo
  - E-mail profissional
  - Cidade/Estado
  - Tipo de prática
  - Campo opcional de expectativas
- [ ] Formulário envia (mostra mensagem de sucesso)
- [ ] Link "Voltar à página inicial" funciona

### 3. Dashboard (`/dashboard`)
- [ ] Página carrega
- [ ] Cabeçalho com título "Dashboard" aparece
- [ ] 5 cards de métricas aparecem (mesmo com valores zerados)
- [ ] Bloco de Agenda aparece (com empty state se não houver dados)
- [ ] Bloco Financeiro aparece (com empty state se não houver dados)
- [ ] Bloco de Insights aparece (pelo menos 1 insight padrão)
- [ ] Bloco de Ações Recomendadas aparece
- [ ] Onboarding abre automaticamente se for primeiro acesso
- [ ] Onboarding pode ser pulado
- [ ] Onboarding não aparece após completado

### 4. Onboarding (Modal)
- [ ] Abre automaticamente no primeiro acesso ao dashboard
- [ ] Etapa 1: Boas-vindas aparece
- [ ] Botão "Próximo" avança
- [ ] Botão "Pular" fecha o modal
- [ ] Indicador de progresso aparece
- [ ] Etapa 2: Como funciona (3 cards)
- [ ] Etapa 3: O que fazer primeiro (checklist)
- [ ] Etapa 4: Dashboard explicado
- [ ] Etapa 5: Finalização
- [ ] Botão "Ir para o Dashboard" finaliza
- [ ] Após finalizar, não aparece mais

### 5. Página de Clínicas (`/clinica`)
- [ ] Página carrega
- [ ] Se não houver token: mostra empty state ou erro apropriado
- [ ] Se houver token: lista clínicas ou mostra empty state
- [ ] Skeleton loader aparece durante carregamento
- [ ] Ao clicar em uma clínica: detalhes carregam
- [ ] Lista de membros aparece (se houver)
- [ ] Badges de role aparecem corretamente
- [ ] Botão "Convidar Membro" aparece (se tiver permissão)
- [ ] Modal de convite abre e fecha

### 6. Página de Pacientes (`/pacientes`)
- [ ] Página carrega
- [ ] Lista de pacientes aparece (ou empty state)
- [ ] Skeleton loader durante carregamento
- [ ] Ao clicar em um paciente: página de detalhes abre
- [ ] Tabs funcionam: Visão Geral, Dados, Anamnese, etc.

### 7. Controle de Acesso Beta
- [ ] Sem token: tela de bloqueio aparece em páginas autenticadas
- [ ] Tela mostra: "O PsiPro está em beta fechado"
- [ ] Botão "Solicitar acesso" abre formulário
- [ ] Link "Saiba mais sobre o beta" leva para `/beta`
- [ ] Com token válido: páginas autenticadas funcionam normalmente

### 8. Header e Sidebar
- [ ] Header aparece em páginas autenticadas
- [ ] Sidebar aparece em páginas autenticadas
- [ ] Seletor de clínica funciona (se houver clínicas)
- [ ] Links do sidebar funcionam
- [ ] Tema claro/escuro funciona (se houver toggle)

### 9. Sistema de Toast
- [ ] Toasts aparecem ao fazer ações
- [ ] Toasts desaparecem automaticamente
- [ ] Tipos: success, error, info, warning

### 10. Página de Teste (`/test`)
- [ ] Página carrega
- [ ] Formulário de login funciona
- [ ] Token é salvo no localStorage
- [ ] Status mostra: Token presente, API URL, Clínicas
- [ ] Botão "Testar Conexão com API" funciona (se backend estiver rodando)

---

## 🔍 Testes de Funcionalidades

### Sistema de Insights
- [ ] Insights aparecem no dashboard
- [ ] Insights têm tipos corretos (info, warning, success, tip)
- [ ] Priorização funciona (warnings primeiro)
- [ ] Limite de 3 insights respeitado
- [ ] Mensagem padrão quando não há dados

### Tema Claro/Escuro
- [ ] Toggle funciona (se houver)
- [ ] Preferência é salva no localStorage
- [ ] Tema persiste após recarregar página

### Responsividade
- [ ] Layout funciona em mobile
- [ ] Layout funciona em tablet
- [ ] Layout funciona em desktop
- [ ] Menu mobile funciona (se houver)

---

## 🐛 Verificações de Erros

### Console do Navegador (F12)
- [ ] Sem erros no console
- [ ] Sem warnings críticos
- [ ] Requisições HTTP funcionam (se backend estiver rodando)

### Network Tab
- [ ] Requisições para API usam URL correta
- [ ] Headers de autenticação são enviados (se houver token)
- [ ] Erros 401/403 são tratados corretamente

---

## 📝 Notas de Teste

### Se o Backend NÃO estiver rodando:
- ✅ Páginas estáticas devem funcionar normalmente
- ✅ Landing page, Beta page, Dashboard (com mocks) funcionam
- ⚠️ Páginas que dependem de API mostrarão empty states ou erros
- ⚠️ Login não funcionará

### Se o Backend ESTIVER rodando:
- ✅ Todas as funcionalidades devem funcionar
- ✅ Login na página `/test` deve funcionar
- ✅ Clínicas e pacientes devem carregar
- ✅ Dashboard deve mostrar dados reais

---

## ✅ Resultado Esperado

Após todos os testes:
- ✅ Nenhum erro crítico no console
- ✅ Todas as páginas carregam
- ✅ Navegação funciona
- ✅ Componentes renderizam corretamente
- ✅ Empty states aparecem quando apropriado
- ✅ Toasts funcionam
- ✅ Onboarding funciona
- ✅ Sistema de beta funciona

---

## 🚨 Problemas Comuns

### "Cannot find module"
- **Solução**: Execute `npm install` na pasta `web/`

### "Port already in use"
- **Solução**: Pare outros processos Node.js ou use outra porta

### "API connection failed"
- **Solução**: Verifique se backend está rodando e `NEXT_PUBLIC_API_URL` está correto

### "Token not found"
- **Solução**: Faça login na página `/test` ou adicione token manualmente

---

## 📊 Status do Teste

Data: _______________
Testador: _______________

- [ ] Todos os testes passaram
- [ ] Alguns testes falharam (anotar abaixo)
- [ ] Erros críticos encontrados (anotar abaixo)

**Observações:**
_________________________________________________
_________________________________________________
_________________________________________________

