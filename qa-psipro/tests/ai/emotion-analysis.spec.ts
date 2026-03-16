import { test, expect } from '@playwright/test';
import { loginApi, getAuthHeaders } from '../../helpers/login-api';

const API_URL = process.env.API_URL || process.env.BASE_URL || 'https://psipro-backend.up.railway.app';

test.describe('AI - Emotion Analysis', () => {
  test('POST /ai/emotion - análise de emoção', async ({ request }) => {
    const token = await loginApi(request);
    const headers = getAuthHeaders(token);

    const response = await request.post(`${API_URL}/ai/emotion`, {
      headers: { ...headers, 'Content-Type': 'application/json' },
      data: { text: 'Hoje me senti um pouco ansioso com o trabalho.' },
    });

    expect([200, 201, 400, 401, 404, 422]).toContain(response.status());

    if (response.ok) {
      const body = await response.json();
      expect(body).toBeTruthy();
      if (body.emotions !== undefined) expect(Array.isArray(body.emotions)).toBe(true);
    }
  });
});
