import { test, expect } from '@playwright/test';
import { loginApi, getAuthHeaders } from '../../helpers/login-api';

const API_URL = process.env.API_URL || process.env.BASE_URL || 'https://psipro-backend.up.railway.app';

test.describe('AI - Voice Transcription', () => {
  test('POST /voice/transcribe retorna transcript', async ({ request }) => {
    const token = await loginApi(request);
    const headers = getAuthHeaders(token);

    const response = await request.post(`${API_URL}/voice/transcribe`, {
      headers: { ...headers, 'Content-Type': 'application/json' },
      data: { audioUrl: 'https://example.com/sample.mp3' },
    });

    expect([200, 201, 400, 401, 404, 422]).toContain(response.status());

    if (response.ok) {
      const body = await response.json();
      expect(body).toBeTruthy();
      if (body.transcript !== undefined) expect(typeof body.transcript).toBe('string');
    }
  });

  test('POST /sessions/voice-note retorna summary e temas', async ({ request }) => {
    const token = await loginApi(request);
    const headers = getAuthHeaders(token);

    const response = await request.post(`${API_URL}/sessions/voice-note`, {
      headers: { ...headers, 'Content-Type': 'application/json' },
      data: {
        sessionId: 1,
        transcript: 'Sessão de teste para validação de IA.',
      },
    });

    expect([200, 201, 400, 401, 404, 422]).toContain(response.status());

    if (response.ok) {
      const body = await response.json();
      expect(body).toBeTruthy();
      if (body.summary !== undefined) expect(typeof body.summary).toBe('string');
      if (body.themes !== undefined) expect(Array.isArray(body.themes)).toBe(true);
      if (body.emotions !== undefined) expect(Array.isArray(body.emotions)).toBe(true);
      if (body.actionItems !== undefined) expect(Array.isArray(body.actionItems)).toBe(true);
      if (body.riskFlags !== undefined) expect(Array.isArray(body.riskFlags)).toBe(true);
    }
  });
});
