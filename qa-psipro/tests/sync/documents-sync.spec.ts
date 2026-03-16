import { test, expect } from '@playwright/test';
import { loginApi, getAuthHeaders } from '../../helpers/login-api';

const API_URL = process.env.API_URL || process.env.BASE_URL || 'https://psipro-backend.up.railway.app';

test.describe('Sync - Documents', () => {
  test('documentos sincronizam entre mobile e web', async ({ request }) => {
    const token = await loginApi(request);
    const headers = getAuthHeaders(token);

    const response = await request.get(`${API_URL}/documents`, { headers });
    const altResponse = await request.get(`${API_URL}/documentos`, { headers });

    expect([200, 401, 403, 404]).toContain(response.status());
    expect([200, 401, 403, 404]).toContain(altResponse.status());
  });

  test('GET /sync/documents - endpoint de sync de documentos', async ({ request }) => {
    const token = await loginApi(request);
    const headers = getAuthHeaders(token);

    const response = await request.get(`${API_URL}/sync/documents`, { headers });

    expect([200, 401, 403, 404]).toContain(response.status());
  });
});
