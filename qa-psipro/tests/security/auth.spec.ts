import { test, expect } from '@playwright/test';

const API_URL = process.env.API_URL || process.env.BASE_URL || 'https://psipro-backend.up.railway.app';

test.describe('Security - Auth', () => {
  test('login com credenciais inválidas retorna 401', async ({ request }) => {
    const response = await request.post(`${API_URL}/auth/login`, {
      data: { email: 'invalido@test.com', password: 'wrong' },
      headers: { 'Content-Type': 'application/json' },
    });

    expect([401, 403, 400, 422]).toContain(response.status());
  });

  test('acesso sem token retorna 401', async ({ request }) => {
    const response = await request.get(`${API_URL}/pacientes`);

    expect(response.status()).toBe(401);
  });

  test('token inválido retorna 401', async ({ request }) => {
    const response = await request.get(`${API_URL}/pacientes`, {
      headers: { Authorization: 'Bearer token-invalido-xyz' },
    });

    expect(response.status()).toBe(401);
  });
});
