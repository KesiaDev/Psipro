/**
 * Sanitização de dados sensíveis para logs.
 * Nunca logar: CPF, senha, token, dados pessoais sensíveis.
 * Formato compatível com Datadog/Sentry.
 */

const SENSITIVE_KEYS = [
  'password', 'senha', 'token', 'refreshToken', 'accessToken',
  'authorization', 'bearer', 'cpf', 'cnpj', 'rg', 'creditCard',
  'cvv', 'secret', 'apiKey', 'api_key', 'cookie',
];

const REDACT = '[REDACTED]';

function isObject(obj: unknown): obj is Record<string, unknown> {
  return obj !== null && typeof obj === 'object' && !Array.isArray(obj);
}

export function sanitizeForLog<T>(obj: T): T {
  if (obj === null || obj === undefined) return obj;
  if (Array.isArray(obj)) return obj.map((item) => sanitizeForLog(item)) as T;
  if (!isObject(obj)) return obj;

  const result: Record<string, unknown> = {};
  for (const [key, value] of Object.entries(obj)) {
    const keyLower = key.toLowerCase();
    if (SENSITIVE_KEYS.some((k) => keyLower.includes(k.toLowerCase()))) {
      result[key] = REDACT;
    } else {
      result[key] = sanitizeForLog(value);
    }
  }
  return result as T;
}
