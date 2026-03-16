import { test, expect } from '@playwright/test';

const DASHBOARD_URL = process.env.BASE_URL || 'https://psipro-dashboard-production.up.railway.app';
const API_URL = process.env.API_URL || 'https://psipro-backend-production.up.railway.app';

test.describe('Infraestrutura - Railway Deploy', () => {
  test('dashboard Railway está online', async ({ request }) => {
    const response = await request.get(DASHBOARD_URL);
    expect(response.status()).toBeLessThan(500);
  });

  test('backend Railway está online', async ({ request }) => {
    const response = await request.get(API_URL);
    expect(response.status()).toBeLessThan(500);
  });

  test('dashboard retorna HTML', async ({ request }) => {
    const response = await request.get(DASHBOARD_URL);
    const contentType = response.headers()['content-type'] || '';
    expect(contentType).toMatch(/text\/html|application\/json/);
  });
});
