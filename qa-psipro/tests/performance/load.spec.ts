import { test, expect } from '@playwright/test';
import { loginApi, getAuthHeaders } from '../../helpers/login-api';

const API_URL = process.env.API_URL || process.env.BASE_URL || 'https://psipro-backend.up.railway.app';

test.describe('Performance - Load', () => {
  test('10 logins simultâneos', async ({ request }) => {
    const logins = Array(10)
      .fill(null)
      .map(() =>
        request.post(`${API_URL}/auth/login`, {
          data: {
            email: process.env.AUTH_USER || 'terapeutaclaudiacruz@gmail.com',
            password: process.env.AUTH_PASS || 'senha123',
          },
          headers: { 'Content-Type': 'application/json' },
        })
      );

    const results = await Promise.all(logins);
    const success = results.filter((r) => r.status() === 200 || r.status() === 201).length;
    const failures = results.filter((r) => r.status() >= 400).length;

    expect(failures).toBeLessThan(5);
    expect(success).toBeGreaterThanOrEqual(5);
  });

  test('GET /pacientes - tempo de resposta aceitável', async ({ request }) => {
    const token = await loginApi(request);
    const headers = getAuthHeaders(token);

    const start = Date.now();
    const response = await request.get(`${API_URL}/pacientes`, { headers });
    const elapsed = Date.now() - start;

    expect(response.status()).toBeLessThan(500);
    expect(elapsed).toBeLessThan(2000);
  });
});
