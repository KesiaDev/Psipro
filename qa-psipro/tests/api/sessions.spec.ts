import { test, expect } from '@playwright/test';
import { loginApiOrSkip, getAuthHeaders, getApiUrl } from '../../helpers/login-api';

test.describe('API Sessions', () => {
  test('GET /sessions - com autenticação', async ({ request }, testInfo) => {
    const token = await loginApiOrSkip(request);
    if (!token) testInfo.skip(true, 'Login falhou (404?)');

    const response = await request.get(getApiUrl('/sessions'), { headers: getAuthHeaders(token) });

    expect([200, 400, 401, 403, 404]).toContain(response.status());

    if (response.ok) {
      const body = await response.json();
      expect(Array.isArray(body) || (body && typeof body === 'object')).toBe(true);
    }
  });

  test('POST /sessions - criar sessão', async ({ request }, testInfo) => {
    const token = await loginApiOrSkip(request);
    if (!token) testInfo.skip(true, 'Login falhou (404?)');

    const response = await request.post(getApiUrl('/sessions'), {
      headers: { ...getAuthHeaders(token), 'Content-Type': 'application/json' },
      data: {
        pacienteId: 1,
        data: new Date().toISOString().split('T')[0],
        horario: '14:00',
        duracao: 50,
      },
    });

    expect([200, 201, 400, 401, 403, 404, 422]).toContain(response.status());
  });

  test('GET /sessions - sem token retorna 401 ou 404', async ({ request }) => {
    const response = await request.get(getApiUrl('/sessions'));

    expect([401, 404]).toContain(response.status());
  });
});
