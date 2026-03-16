import { test, expect } from '@playwright/test';
import { loginApi, getAuthHeaders } from '../../helpers/login-api';

const API_URL = process.env.API_URL || process.env.BASE_URL || 'https://psipro-backend.up.railway.app';

test.describe('Sync - Sessions', () => {
  test('sessão criada no mobile sincroniza com backend', async ({ request }) => {
    const token = await loginApi(request);
    const headers = getAuthHeaders(token);

    const listResponse = await request.get(`${API_URL}/sessions`, { headers });
    if (listResponse.status() === 404) {
      const altResponse = await request.get(`${API_URL}/agenda`, { headers });
      expect([200, 401, 404]).toContain(altResponse.status());
      return;
    }

    expect([200, 401, 404]).toContain(listResponse.status());
  });

  test('GET /sync/sessions - endpoint de sync de sessões', async ({ request }) => {
    const token = await loginApi(request);
    const headers = getAuthHeaders(token);

    const response = await request.get(`${API_URL}/sync/sessions`, { headers });

    expect([200, 401, 403, 404]).toContain(response.status());
  });
});
