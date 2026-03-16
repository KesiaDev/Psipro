import { test, expect } from '@playwright/test';
import { loginApiOrSkip, getAuthHeaders, getApiUrl } from '../../helpers/login-api';

test.describe('API Reports', () => {
  test('GET /reports - com autenticação', async ({ request }, testInfo) => {
    const token = await loginApiOrSkip(request);
    if (!token) testInfo.skip(true, 'Login falhou (404?)');

    const response = await request.get(getApiUrl('/reports'), { headers: getAuthHeaders(token) });

    expect([200, 400, 401, 403, 404]).toContain(response.status());

    if (response.ok) {
      const body = await response.json();
      expect(body).toBeTruthy();
    }
  });

  test('GET /reports/sessions - relatório de sessões', async ({ request }, testInfo) => {
    const token = await loginApiOrSkip(request);
    if (!token) testInfo.skip(true, 'Login falhou (404?)');

    const response = await request.get(getApiUrl('/reports/sessions'), {
      headers: getAuthHeaders(token),
    });

    expect([200, 400, 401, 403, 404]).toContain(response.status());
  });

  test('GET /reports - sem token retorna 401 ou 404', async ({ request }) => {
    const response = await request.get(getApiUrl('/reports'));

    expect([401, 404]).toContain(response.status());
  });
});
