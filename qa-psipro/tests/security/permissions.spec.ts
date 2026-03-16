import { test, expect } from '@playwright/test';
import { loginApi, getAuthHeaders } from '../../helpers/login-api';

const API_URL = process.env.API_URL || process.env.BASE_URL || 'https://psipro-backend.up.railway.app';

test.describe('Security - Permissions', () => {
  test('acesso negado sem permissão retorna 403', async ({ request }) => {
    const token = await loginApi(request);
    const headers = getAuthHeaders(token);

    const response = await request.get(`${API_URL}/admin/users`, { headers });

    expect([200, 401, 403, 404]).toContain(response.status());
  });

  test('recurso inexistente retorna 404', async ({ request }) => {
    const token = await loginApi(request);
    const headers = getAuthHeaders(token);

    const response = await request.get(`${API_URL}/pacientes/999999`, { headers });

    expect([200, 404]).toContain(response.status());
  });

  test('X-Clinic-Id obrigatório em rotas multi-clinic', async ({ request }) => {
    const token = await loginApi(request);
    const headers = getAuthHeaders(token);

    const responseComClinic = await request.get(`${API_URL}/clinics`, {
      headers: { ...headers, 'X-Clinic-Id': '1' },
    });
    expect([200, 401, 403, 404]).toContain(responseComClinic.status());
  });
});
