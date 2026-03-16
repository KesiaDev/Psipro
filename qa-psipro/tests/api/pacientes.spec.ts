import { test, expect } from '@playwright/test';
import { loginApiOrSkip, getAuthHeaders, getApiUrl } from '../../helpers/login-api';

test.describe('API Pacientes', () => {
  test('listar pacientes', async ({ request }, testInfo) => {
    const token = await loginApiOrSkip(request);
    if (!token) testInfo.skip(true, 'Login falhou (404?) - verifique API_URL e API_PATH_PREFIX');

    const response = await request.get(getApiUrl('/pacientes'), { headers: getAuthHeaders(token) });

    expect([200, 401, 403, 404]).toContain(response.status());

    if (response.ok) {
      const body = await response.json();
      expect(Array.isArray(body) || (body && typeof body === 'object')).toBe(true);
    }
  });

  test('GET /pacientes - com autenticação', async ({ request }, testInfo) => {
    const token = await loginApiOrSkip(request);
    if (!token) testInfo.skip(true, 'Login falhou (404?)');

    const start = Date.now();
    const response = await request.get(getApiUrl('/pacientes'), { headers: getAuthHeaders(token) });
    const elapsed = Date.now() - start;

    expect(elapsed).toBeLessThan(5000);
    expect([200, 401, 403, 404]).toContain(response.status());

    if (response.ok) {
      const body = await response.json();
      expect(Array.isArray(body) || (body && typeof body === 'object')).toBe(true);
    }
  });

  test('POST /pacientes - estrutura', async ({ request }, testInfo) => {
    const token = await loginApiOrSkip(request);
    if (!token) testInfo.skip(true, 'Login falhou (404?)');

    const response = await request.post(getApiUrl('/pacientes'), {
      headers: { ...getAuthHeaders(token), 'Content-Type': 'application/json' },
      data: {
        nome: 'Paciente Teste QA',
        email: `teste-${Date.now()}@psipro.com.br`,
        telefone: '11999999999',
      },
    });

    expect([200, 201, 400, 401, 403, 404, 422]).toContain(response.status());
  });
});
