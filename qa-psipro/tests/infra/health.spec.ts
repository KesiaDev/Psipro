import { test, expect } from '@playwright/test';

const API_URL = process.env.API_URL || process.env.BASE_URL || 'https://psipro-backend-production.up.railway.app';
const DASHBOARD_URL = process.env.BASE_URL || 'https://psipro-dashboard-production.up.railway.app';

test.describe('Infraestrutura - Health Checks', () => {
  test('backend online', async ({ request }) => {
    const response = await request.get(`${API_URL}/health`);

    expect(response.status()).toBeLessThan(500);
  });

  test('dashboard respondendo', async ({ request }) => {
    const start = Date.now();
    const response = await request.get(DASHBOARD_URL);
    const elapsed = Date.now() - start;

    expect(response.status()).toBeLessThan(500);
    expect(elapsed).toBeLessThan(10000);
  });

  test('GET / - raiz responde', async ({ request }) => {
    const response = await request.get(DASHBOARD_URL);
    expect([200, 301, 302, 404]).toContain(response.status());
  });
});
