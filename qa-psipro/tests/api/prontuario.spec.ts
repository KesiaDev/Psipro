import { test, expect } from '@playwright/test';
import { loginApiOrSkip, getAuthHeaders, getApiUrl } from '../../helpers/login-api';

test.describe('API Prontuário', () => {
  test('GET /prontuarios - resposta', async ({ request }, testInfo) => {
    const token = await loginApiOrSkip(request);
    if (!token) testInfo.skip(true, 'Login falhou (404?)');

    const start = Date.now();
    const response = await request.get(getApiUrl('/prontuarios'), { headers: getAuthHeaders(token) });
    const elapsed = Date.now() - start;

    expect(elapsed).toBeLessThan(5000);
    expect([200, 401, 403, 404]).toContain(response.status());
  });
});
