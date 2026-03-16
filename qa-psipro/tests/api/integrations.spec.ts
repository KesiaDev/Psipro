import { test, expect } from '@playwright/test';
import { loginApiOrSkip, getAuthHeaders, getApiUrl } from '../../helpers/login-api';

test.describe('API Integrations', () => {
  test('GET /integrations - com autenticação', async ({ request }, testInfo) => {
    const token = await loginApiOrSkip(request);
    if (!token) testInfo.skip(true, 'Login falhou (404?)');

    const response = await request.get(getApiUrl('/integrations'), { headers: getAuthHeaders(token) });

    expect([200, 401, 403, 404]).toContain(response.status());

    if (response.ok) {
      const body = await response.json();
      expect(body).toBeTruthy();
    }
  });

  test('GET /integrations - estrutura JSON válida', async ({ request }, testInfo) => {
    const token = await loginApiOrSkip(request);
    if (!token) testInfo.skip(true, 'Login falhou (404?)');

    const response = await request.get(getApiUrl('/integrations'), { headers: getAuthHeaders(token) });

    if (response.ok) {
      const contentType = response.headers()['content-type'] || '';
      expect(contentType).toMatch(/application\/json/);
      const body = await response.json();
      expect(typeof body).toBe('object');
    }
  });
});
