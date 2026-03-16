import { test, expect } from '@playwright/test';
import { loginApi, getAuthHeaders } from '../../helpers/login-api';

const API_URL = process.env.API_URL || process.env.BASE_URL || 'https://psipro-backend.up.railway.app';

test.describe('Security - LGPD', () => {
  test('dados sensíveis não expostos em logs de erro', async ({ request }) => {
    const response = await request.post(`${API_URL}/auth/login`, {
      data: { email: 'teste@psipro.com.br', password: 'senha123' },
      headers: { 'Content-Type': 'application/json' },
    });

    const body = await response.text();
    expect(body).not.toContain('senha123');
    expect(body.toLowerCase()).not.toMatch(/password|senha/);
  });

  test('endpoint de exportação de dados requer autenticação', async ({ request }) => {
    const response = await request.get(`${API_URL}/lgpd/export`);

    expect([401, 404]).toContain(response.status());
  });

  test('dados protegidos - resposta JSON válida', async ({ request }) => {
    const token = await loginApi(request);
    const headers = getAuthHeaders(token);

    const response = await request.get(`${API_URL}/pacientes/1`, { headers });

    if (response.ok) {
      const contentType = response.headers()['content-type'] || '';
      expect(contentType).toMatch(/application\/json/);
    }
  });
});
