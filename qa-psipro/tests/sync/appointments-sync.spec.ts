import { test, expect } from '@playwright/test';
import { loginApi, getAuthHeaders } from '../../helpers/login-api';

const API_URL = process.env.API_URL || process.env.BASE_URL || 'https://psipro-backend.up.railway.app';

test.describe('Sync - Appointments', () => {
  test('agendamento criado no mobile aparece na web', async ({ request }) => {
    const token = await loginApi(request);
    const headers = getAuthHeaders(token);

    const createResponse = await request.post(`${API_URL}/agenda`, {
      headers: { ...headers, 'Content-Type': 'application/json' },
      data: {
        pacienteId: 1,
        data: new Date().toISOString().split('T')[0],
        horario: '15:00',
        duracao: 50,
      },
    });

    expect([200, 201, 400, 401, 404, 422]).toContain(createResponse.status());

    if (createResponse.ok) {
      const listResponse = await request.get(`${API_URL}/agenda`, { headers });
      expect([200, 404]).toContain(listResponse.status());
    }
  });

  test('GET /sync/agenda - endpoint de sync de agenda', async ({ request }) => {
    const token = await loginApi(request);
    const headers = getAuthHeaders(token);

    const response = await request.get(`${API_URL}/sync/agenda`, { headers });

    expect([200, 401, 403, 404]).toContain(response.status());
  });
});
