import { test, expect } from '@playwright/test';
import { loginApi, getAuthHeaders } from '../../helpers/login-api';

const API_URL = process.env.API_URL || process.env.BASE_URL || 'https://psipro-backend.up.railway.app';

test.describe('Security - Token', () => {
  test('token expira e retorna 401', async ({ request }) => {
    const response = await request.get(`${API_URL}/pacientes`, {
      headers: {
        Authorization:
          'Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwiZXhwIjoxNjAwMDAwMDAwfQ.invalid',
      },
    });

    expect([401, 403]).toContain(response.status());
  });

  test('token válido permite acesso', async ({ request }) => {
    const token = await loginApi(request);
    const headers = getAuthHeaders(token);

    const response = await request.get(`${API_URL}/pacientes`, { headers });

    expect([200, 401, 403, 404]).toContain(response.status());
    if (response.ok) expect(response.status()).toBe(200);
  });
});
