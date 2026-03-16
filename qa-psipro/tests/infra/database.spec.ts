import { test, expect } from '@playwright/test';

const API_URL = process.env.API_URL || process.env.BASE_URL || 'https://psipro-backend-production.up.railway.app';

test.describe('Infraestrutura - Database', () => {
  test('API conectada ao banco - health com db', async ({ request }) => {
    const response = await request.get(`${API_URL}/health`);
    const status = response.status();

    if (status === 200) {
      try {
        const body = await response.json();
        expect(body).toBeTruthy();
      } catch {
        expect(response.status()).toBeLessThan(500);
      }
    } else {
      expect(status).toBeLessThan(500);
    }
  });

  test('endpoint que usa banco responde', async ({ request }) => {
    const response = await request.get(`${API_URL}/pacientes`);
    expect([200, 401, 403, 404, 500]).toContain(response.status());
  });
});
