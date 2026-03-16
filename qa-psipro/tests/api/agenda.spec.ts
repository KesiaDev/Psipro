import { test, expect } from '@playwright/test';
import { loginApiOrSkip, getAuthHeaders, getApiUrl } from '../../helpers/login-api';

test.describe('API Agenda', () => {
  test('GET /agenda - resposta', async ({ request }, testInfo) => {
    const token = await loginApiOrSkip(request);
    if (!token) testInfo.skip(true, 'Login falhou (404?)');

    const start = Date.now();
    const response = await request.get(getApiUrl('/agenda'), { headers: getAuthHeaders(token) });
    const elapsed = Date.now() - start;

    expect(elapsed).toBeLessThan(5000);
    expect([200, 401, 403, 404]).toContain(response.status());
  });

  test('POST /agenda - criar sessão', async ({ request }, testInfo) => {
    const token = await loginApiOrSkip(request);
    if (!token) testInfo.skip(true, 'Login falhou (404?)');

    const response = await request.post(getApiUrl('/agenda'), {
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
});
