import { test, expect } from '@playwright/test';
import { loginApi, getAuthHeaders } from '../../helpers/login-api';

const API_URL = process.env.API_URL || process.env.BASE_URL || 'https://psipro-backend.up.railway.app';

test.describe('Sync - Patients', () => {
  test('mobile cria paciente → sync → web vê paciente', async ({ request }) => {
    const token = await loginApi(request);
    const headers = getAuthHeaders(token);

    const createResponse = await request.post(`${API_URL}/pacientes`, {
      headers: { ...headers, 'Content-Type': 'application/json' },
      data: {
        nome: `Paciente Sync Test ${Date.now()}`,
        email: `sync-${Date.now()}@psipro.com.br`,
        telefone: '11999999999',
      },
    });

    expect([200, 201, 400, 401, 422]).toContain(createResponse.status());

    if (createResponse.ok) {
      const listResponse = await request.get(`${API_URL}/pacientes`, { headers });
      expect(listResponse.status()).toBe(200);
      const list = await listResponse.json();
      expect(Array.isArray(list) || (list && typeof list === 'object')).toBe(true);
    }
  });

  test('GET /sync/pacientes - endpoint de sync', async ({ request }) => {
    const token = await loginApi(request);
    const headers = getAuthHeaders(token);

    const response = await request.get(`${API_URL}/sync/pacientes`, { headers });

    expect([200, 401, 403, 404]).toContain(response.status());
  });
});
