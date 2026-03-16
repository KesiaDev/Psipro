import { test, expect } from '@playwright/test';
import { loginApi, getAuthHeaders } from '../../helpers/login-api';

const API_URL = process.env.API_URL || process.env.BASE_URL || 'https://psipro-backend.up.railway.app';

test.describe('AI - Patterns', () => {
  test('GET /ai/patterns - padrões clínicos', async ({ request }) => {
    const token = await loginApi(request);
    const headers = getAuthHeaders(token);

    const response = await request.get(`${API_URL}/ai/patterns`, { headers });

    expect([200, 401, 403, 404]).toContain(response.status());

    if (response.ok) {
      const body = await response.json();
      expect(body).toBeTruthy();
    }
  });

  test('GET /ai/patterns/:pacienteId - padrões por paciente', async ({ request }) => {
    const token = await loginApi(request);
    const headers = getAuthHeaders(token);

    const response = await request.get(`${API_URL}/ai/patterns/1`, { headers });

    expect([200, 401, 403, 404]).toContain(response.status());
  });
});
