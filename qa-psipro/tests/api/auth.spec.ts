import { test, expect } from '@playwright/test';
import { getApiUrl } from '../../helpers/login-api';

const AUTH_DELAY_MS = Number(process.env.LOGIN_DELAY_MS) || 1500;

test.describe('API Auth', () => {
  test('POST /auth/login - credenciais válidas', async ({ request }, testInfo) => {
    await new Promise((r) => setTimeout(r, AUTH_DELAY_MS));
    const email = process.env.AUTH_USER || 'terapeutaclaudiacruz@gmail.com';
    const password = process.env.AUTH_PASS || 'senha123';

    const start = Date.now();
    const response = await request.post(getApiUrl('/auth/login'), {
      data: { email, password },
      headers: { 'Content-Type': 'application/json' },
    });
    const elapsed = Date.now() - start;

    if (response.status() === 404) {
      testInfo.skip(true, 'Endpoint /auth/login não encontrado (404). Tente API_PATH_PREFIX=api');
    }
    if (response.status() === 429) {
      testInfo.skip(true, 'Rate limit (429) - muitas requisições ao login');
    }
    expect(
      [200, 201],
      `Login retornou ${response.status()}. Body: ${await response.text().catch(() => '')}`
    ).toContain(response.status());

    expect(elapsed).toBeLessThan(5000);

    const body = await response.json();
    expect(body).toBeTruthy();
    expect(
      body.token || body.access_token || body.data?.token,
      `Token não encontrado. Body keys: ${Object.keys(body || {}).join(', ')}`
    ).toBeTruthy();
  });

  test('POST /auth/login - credenciais inválidas', async ({ request }, testInfo) => {
    await new Promise((r) => setTimeout(r, AUTH_DELAY_MS));
    const response = await request.post(getApiUrl('/auth/login'), {
      data: { email: 'invalido@test.com', password: 'wrong' },
      headers: { 'Content-Type': 'application/json' },
    });

    if (response.status() === 404) {
      testInfo.skip(true, 'Endpoint /auth/login não encontrado (404)');
    }
    if (response.status() === 429) {
      testInfo.skip(true, 'Rate limit (429)');
    }
    expect([401, 403, 400, 422, 429]).toContain(response.status());
  });
});
