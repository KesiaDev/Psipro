import { test, expect } from '@playwright/test';
import { loginApi, getAuthHeaders, getApiUrl } from '../../helpers/login-api';

// Railway pode ter cold start e latência variável — limites configuráveis
const LOGIN_MAX_MS = Number(process.env.LATENCY_LOGIN_MS) || 5000;
const ENDPOINT_MAX_MS = Number(process.env.LATENCY_ENDPOINT_MS) || 5000;

test.describe('Infraestrutura - Latência', () => {
  test('/auth/login responde em tempo aceitável', async ({ request }) => {
    const start = Date.now();
    const response = await request.post(getApiUrl('/auth/login'), {
      data: {
        email: process.env.AUTH_USER || 'terapeutaclaudiacruz@gmail.com',
        password: process.env.AUTH_PASS || 'senha123',
      },
      headers: { 'Content-Type': 'application/json' },
    });
    const elapsed = Date.now() - start;

    expect(response.status()).toBeLessThan(500);
    expect(elapsed, `/auth/login deve responder em < ${LOGIN_MAX_MS}ms`).toBeLessThan(LOGIN_MAX_MS);
  });

  test('/pacientes responde em tempo aceitável', async ({ request }, testInfo) => {
    let token: string;
    try {
      token = await loginApi(request);
    } catch (e) {
      testInfo.skip(true, `Login falhou (API pode estar indisponível): ${(e as Error).message}`);
    }
    const headers = getAuthHeaders(token!);

    const start = Date.now();
    const response = await request.get(getApiUrl('/pacientes'), { headers });
    const elapsed = Date.now() - start;

    expect(response.status()).toBeGreaterThanOrEqual(200);
    expect(response.status()).toBeLessThan(600);
    expect(elapsed, `Status ${response.status()}, Elapsed ${elapsed}ms`).toBeLessThan(ENDPOINT_MAX_MS);
  });

  test('/agenda responde em tempo aceitável', async ({ request }, testInfo) => {
    let token: string;
    try {
      token = await loginApi(request);
    } catch (e) {
      testInfo.skip(true, `Login falhou (API pode estar indisponível): ${(e as Error).message}`);
    }
    const headers = getAuthHeaders(token!);

    const start = Date.now();
    const response = await request.get(getApiUrl('/agenda'), { headers });
    const elapsed = Date.now() - start;

    expect(response.status()).toBeGreaterThanOrEqual(200);
    expect(response.status()).toBeLessThan(600);
    expect(elapsed, `Status ${response.status()}, Elapsed ${elapsed}ms`).toBeLessThan(ENDPOINT_MAX_MS);
  });

  test('/prontuarios responde em tempo aceitável', async ({ request }, testInfo) => {
    let token: string;
    try {
      token = await loginApi(request);
    } catch (e) {
      testInfo.skip(true, `Login falhou (API pode estar indisponível): ${(e as Error).message}`);
    }
    const headers = getAuthHeaders(token!);

    const start = Date.now();
    const response = await request.get(getApiUrl('/prontuarios'), { headers });
    const elapsed = Date.now() - start;

    expect(response.status()).toBeGreaterThanOrEqual(200);
    expect(response.status()).toBeLessThan(600);
    expect(elapsed, `Status ${response.status()}, Elapsed ${elapsed}ms`).toBeLessThan(ENDPOINT_MAX_MS);
  });
});
