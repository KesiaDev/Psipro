/**
 * ⚠️ ARQUIVO CRÍTICO - INTEGRAÇÃO BACKEND
 *
 * Este arquivo contém lógica essencial de integração com API,
 * autenticação ou variáveis de ambiente.
 *
 * NÃO alterar estrutura, headers, interceptors ou contratos de API
 * durante modernização visual.
 *
 * Qualquer alteração pode quebrar produção.
 */

/**
 * Configuração centralizada de variáveis de ambiente (client-side).
 * Use NEXT_PUBLIC_* para expor ao cliente.
 */

const API_URL =
  process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:3001/api';

/**
 * URL base da API backend (sem trailing slash)
 */
export function getApiUrl(): string {
  return API_URL.replace(/\/+$/, '');
}
