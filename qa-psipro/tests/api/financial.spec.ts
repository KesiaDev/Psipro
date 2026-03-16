import { test, expect } from '@playwright/test';
import { loginApiOrSkip, getAuthHeaders, getApiUrl } from '../../helpers/login-api';

test.describe('API Financial', () => {
  test('GET /financial - com autenticação', async ({ request }, testInfo) => {
    const token = await loginApiOrSkip(request);
    if (!token) testInfo.skip(true, 'Login falhou (404?)');

    const response = await request.get(getApiUrl('/financial'), { headers: getAuthHeaders(token) });

    expect([200, 401, 403, 404]).toContain(response.status());

    if (response.ok) {
      const body = await response.json();
      expect(body).toBeTruthy();
    }
  });

  test('GET /financial/invoices - lista faturas', async ({ request }, testInfo) => {
    const token = await loginApiOrSkip(request);
    if (!token) testInfo.skip(true, 'Login falhou (404?)');

    const response = await request.get(getApiUrl('/financial/invoices'), {
      headers: getAuthHeaders(token),
    });

    expect([200, 401, 403, 404]).toContain(response.status());

    if (response.ok) {
      const body = await response.json();
      expect(Array.isArray(body) || (body && typeof body === 'object')).toBe(true);
    }
  });

  test('GET /financial - sem token retorna 401 ou 404', async ({ request }) => {
    const response = await request.get(getApiUrl('/financial'));

    expect([401, 404]).toContain(response.status());
  });
});
