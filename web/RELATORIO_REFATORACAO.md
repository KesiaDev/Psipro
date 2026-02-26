# Relatório de Refatoração para Produção — PsiPro Web

**Data:** 12/02/2025  
**Escopo:** Projeto Next.js App Router

---

## 1. Arquivos .md removidos

Foram removidos **66 arquivos** .md de guias, troubleshooting e documentação interna de desenvolvimento, mantendo apenas:

- `README.md` — documentação principal do projeto
- `app/insights/README.md` — documentação do módulo de insights
- `public/brand/README.md` — instruções de assets de marca

**Arquivos removidos (amostra):**
ADICIONAR_JWT_SECRET, AGORA_EXECUTE, AUTENTICACAO_IMPLEMENTADA, BACKEND_DEPLOYADO_SUCESSO, COMO_TESTAR, COMECE_AQUI, CONFIGURAR_*, CORRIGIR_*, CREDENCIAIS_TESTE, DASHBOARD_IMPLEMENTADO, DEBUG_ERRO_LOGIN, DEPLOY_RAILWAY, ESTRUTURA_PROJETO, FLUXO_AUTORIZACAO_BETA, GUIA_*, INSTRUCOES_*, INSIGHTS_IMPLEMENTADO, INTEGRACAO_API, O_QUE_*, ONDE_ESTA_*, ONBOARDING_IMPLEMENTADO, PASSO_A_PASSO_*, PROBLEMA_RESOLVIDO, PROXIMO_*, RESOLVER_ERRO_CONEXAO, RESUMO_PROJETO_WEB, SALVAR_JWT_SECRET, SOLUCAO_*, TUDO_*, TROUBLESHOOTING_DEPLOY, VARIAVEIS_AMBIENTE_RAILWAY, testar-web.md, entre outros.

---

## 2. Estrutura organizada

### Novos arquivos

- `lib/env.ts` — Configuração centralizada de variáveis de ambiente
- `.env.example` — Template de variáveis de ambiente
- `app/global-error.tsx` — Tratamento global de erros (Next.js App Router)

### Estrutura atual

```
web/
├── app/
│   ├── global-error.tsx      # Tratamento global de erros
│   ├── layout.tsx
│   ├── components/
│   ├── contexts/
│   ├── handoff/
│   ├── insights/
│   ├── services/
│   └── ...
├── lib/
│   └── env.ts                # Config centralizada
├── public/
├── .env.example
├── package.json
└── README.md
```

---

## 3. NEXT_PUBLIC_API_URL e uso em fetch

### Implementação

- **`lib/env.ts`**  
  - Função `getApiUrl()` que retorna a URL base da API  
  - Lê `process.env.NEXT_PUBLIC_API_URL` com fallback para `http://localhost:3001/api`  
  - Remove trailing slash

- **`.env.example`**  
  - Variável `NEXT_PUBLIC_API_URL` documentada e configurável

### Pontos de uso

- `app/services/api.ts` — Cliente Axios usa `getApiUrl()`
- `app/handoff/HandoffClient.tsx` — `fetch` para `/auth/handoff` usa `getApiUrl()`
- `app/test/page.tsx` — `fetch` para login e `/auth/me` usa `getApiUrl()`

---

## 4. Tratamento global de erro

- **`app/global-error.tsx`**  
  - Error Boundary global do Next.js App Router  
  - Captura erros não tratados na árvore de componentes  
  - Renderiza fora do layout principal (substitui o body)  
  - Ações: “Tentar novamente”, “Ir para início”, “Recarregar página”  
  - Em desenvolvimento: exibe detalhes do erro (message, stack)  
  - Estilos neutros para funcionar em tema claro/escuro  

- **`app/components/ErrorBoundary.tsx`**  
  - Mantido para erros em partes da árvore (não substituído)  
  - Continua usado no layout principal

---

## 5. Axios e interceptor JWT

### Mudanças em `app/services/api.ts`

- Migração de `fetch` para **Axios**
- **Request interceptor:**  
  - Lê token em `localStorage` (chave `psipro_token`)  
  - Define `Authorization: Bearer <token>`  
  - Para requisições com `FormData`, remove `Content-Type` para o browser definir o boundary

- **Response interceptor:**  
  - Tratamento de erros de resposta  
  - Em respostas 401: remove token de `localStorage`  
  - Normaliza erro para `ApiError` com `message`, `status`, `errors`  
  - Re-throw para camadas superiores

### Dependência

- `axios` adicionada em `package.json`

### Compatibilidade com serviços

A interface pública do cliente foi preservada:

- `api.get<T>(endpoint, params?)`
- `api.post<T>(endpoint, data?)`
- `api.postFormData<T>(endpoint, formData)`
- `api.put<T>(endpoint, data?)`
- `api.patch<T>(endpoint, data?)`
- `api.delete<T>(endpoint)`

Nenhum serviço (`authService`, `clinicService`, `patientService`, `dashboardService`, `appointmentService`) precisou de alteração.

---

## 6. Resumo técnico

| Item                         | Status |
|-----------------------------|--------|
| Remoção de .md desnecessários | Concluído |
| Estrutura limpa             | Concluído |
| `NEXT_PUBLIC_API_URL` centralizado | Concluído |
| Uso de `getApiUrl()` em fetch | Concluído |
| Tratamento global de erro   | Concluído |
| Axios com interceptor JWT   | Concluído |
| `.env.example` criado       | Concluído |
| Build de produção           | OK |

---

## 7. Próximos passos sugeridos

1. **Produção:** definir `NEXT_PUBLIC_API_URL` no host (Vercel, Railway, etc.)
2. **Monitoramento:** integrar `global-error.tsx` com Sentry ou similar (opcional)
3. **Segurança:** revisar `npm audit` e atualizar dependências conforme necessário

---

**Build:** `npm run build` executado com sucesso.
