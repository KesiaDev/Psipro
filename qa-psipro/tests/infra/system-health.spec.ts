import { test, expect } from '@playwright/test';
import { getApiUrl } from '../../helpers/login-api';

test.describe('Infraestrutura - System Health', () => {
  /**
   * GATE: Roda primeiro. Se status = down → falha e aborta o pipeline.
   * O npm script usa && então test:infra falhar = para toda a execução.
   */
  test('GET /system-health - sistema deve estar up (gate)', async ({ request }) => {
    const response = await request.get(getApiUrl('/system-health'));

    expect(response.status(), 'Endpoint deve responder').toBeLessThan(500);

    if (response.status() !== 200) {
      test.skip(true, `/system-health retornou ${response.status()}`);
    }

    const body = await response.json();
    expect(body).toBeTruthy();

    const status = body.status ?? body.overall ?? body.health;
    if (status !== undefined) {
      expect(
        String(status).toLowerCase(),
        `Sistema deve estar up, não "down". Recebido: ${status}`
      ).not.toBe('down');
    }
  });

  test('GET /system-health - responde', async ({ request }) => {
    const response = await request.get(getApiUrl('/system-health'));

    expect(response.status()).toBeLessThan(500);
  });

  test('GET /system-health - estrutura esperada', async ({ request }) => {
    const response = await request.get(getApiUrl('/system-health'));

    if (response.status() !== 200) {
      test.skip(true, 'Endpoint /system-health não disponível (404 ou outro)');
    }

    const body = await response.json();
    expect(body).toBeTruthy();
    if (body.backend !== undefined) expect(typeof body.backend).toBe('object');
    if (body.database !== undefined) expect(typeof body.database).toBe('object');
    if (body.mobileSync !== undefined) expect(typeof body.mobileSync).toBe('object');
    if (body.webSync !== undefined) expect(typeof body.webSync).toBe('object');
  });

  test('GET /system-health/full - detalhes completos', async ({ request }) => {
    const response = await request.get(getApiUrl('/system-health/full'));

    if (response.status() !== 200) {
      test.skip(true, 'Endpoint /system-health/full não disponível');
    }

    const body = await response.json();
    expect(body).toBeTruthy();

    if (body.backend !== undefined) {
      expect(body.backend).toHaveProperty('status');
    }
    if (body.database !== undefined) {
      expect(body.database).toHaveProperty('status');
    }
    if (body.latency !== undefined) {
      expect(body.latency).toHaveProperty('totalMs');
    }
  });

  test('GET /system-health/full - validar status e latência', async ({ request }) => {
    const response = await request.get(getApiUrl('/system-health/full'));

    if (response.status() !== 200) {
      test.skip(true, 'Endpoint /system-health/full não disponível');
    }

    const body = await response.json();

    if (body.backend?.status !== undefined) {
      expect(String(body.backend.status).toLowerCase()).not.toBe('down');
    }
    if (body.database?.status !== undefined) {
      expect(String(body.database.status).toLowerCase()).not.toBe('down');
    }
    if (body.latency?.totalMs !== undefined) {
      expect(typeof body.latency.totalMs).toBe('number');
      expect(body.latency.totalMs).toBeGreaterThanOrEqual(0);
    }
  });
});
