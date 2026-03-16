import { APIRequestContext } from '@playwright/test';

const API_URL = (process.env.API_URL || process.env.BASE_URL || 'https://psipro-backend-production.up.railway.app').replace(
  /\/$/,
  ''
);
const API_PREFIX = (process.env.API_PATH_PREFIX ?? 'api').replace(/^\//, '').replace(/\/$/, '');
const EMAIL = process.env.AUTH_USER || 'terapeutaclaudiacruz@gmail.com';
const PASSWORD = process.env.AUTH_PASS || 'senha123';

export function getApiUrl(path: string): string {
  const p = path.startsWith('/') ? path : `/${path}`;
  return API_PREFIX ? `${API_URL}/${API_PREFIX}${p}` : `${API_URL}${p}`;
}

export interface AuthTokens {
  token?: string;
  access_token?: string;
  refresh_token?: string;
}

/**
 * Tenta login — se 404, retorna null (para testes que podem skip).
 */
export async function loginApiOrSkip(request: APIRequestContext): Promise<string | null> {
  try {
    return await loginApi(request);
  } catch {
    return null;
  }
}

const LOGIN_DELAY_MS = Number(process.env.LOGIN_DELAY_MS) || 1500;

/**
 * Login na API e retorna o token JWT.
 * Usa delay antes da requisição para evitar rate limit (429).
 */
export async function loginApi(request: APIRequestContext): Promise<string> {
  await new Promise((r) => setTimeout(r, LOGIN_DELAY_MS));

  const response = await request.post(getApiUrl('/auth/login'), {
    data: { email: EMAIL, password: PASSWORD },
    headers: { 'Content-Type': 'application/json' },
  });

  if (response.status() !== 200 && response.status() !== 201) {
    const body = await response.text();
    throw new Error(`Login API falhou: ${response.status()} - ${body}`);
  }

  const body = await response.json();
  const token =
    body.token ??
    body.access_token ??
    body.accessToken ??
    body.data?.token ??
    body.data?.accessToken;
  if (!token) throw new Error('Token não encontrado na resposta de login');
  return token;
}

/**
 * Login completo (retorna objeto com todos os tokens).
 * Mantido para compatibilidade com testes existentes.
 */
export async function loginApiFull(
  request: APIRequestContext,
  baseUrl = API_URL,
  email = EMAIL,
  password = PASSWORD
): Promise<AuthTokens> {
  const response = await request.post(`${baseUrl}/auth/login`, {
    data: { email, password },
    headers: { 'Content-Type': 'application/json' },
  });

  if (response.status() !== 200 && response.status() !== 201) {
    const body = await response.text();
    throw new Error(`Login API falhou: ${response.status()} - ${body}`);
  }

  const body = await response.json();
  return {
    token: body.token ?? body.access_token ?? body.data?.token,
    access_token: body.access_token ?? body.token,
    refresh_token: body.refresh_token,
  };
}

export function getAuthHeaders(tokenOrTokens: string | AuthTokens, clinicId?: string): Record<string, string> {
  const token = typeof tokenOrTokens === 'string'
    ? tokenOrTokens
    : (tokenOrTokens.token ?? tokenOrTokens.access_token);
  if (!token) throw new Error('Token não fornecido');
  const headers: Record<string, string> = { Authorization: `Bearer ${token}` };
  if (clinicId) headers['X-Clinic-Id'] = clinicId;
  return headers;
}
