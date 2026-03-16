import { test, expect } from '@playwright/test';
import { loginApi, getAuthHeaders } from '../../helpers/login-api';

const API_URL = process.env.API_URL || process.env.BASE_URL || 'https://psipro-backend.up.railway.app';

test.describe('Performance - Stress', () => {
  test('100 logins simultâneos (stress)', async ({ request }) => {
    const concurrency = 20;
    const total = 100;
    const batches: Promise<Awaited<ReturnType<typeof request.post>>[]>[] = [];

    for (let i = 0; i < total; i += concurrency) {
      const batch = Array(Math.min(concurrency, total - i))
        .fill(null)
        .map(() =>
          request.post(`${API_URL}/auth/login`, {
            data: {
              email: process.env.AUTH_USER || 'terapeutaclaudiacruz@gmail.com',
              password: process.env.AUTH_PASS || 'senha123',
            },
            headers: { 'Content-Type': 'application/json' },
          })
        );
      batches.push(Promise.all(batch));
    }

    const allBatches = await Promise.all(batches);
    const allResults = allBatches.flat();
    const success = allResults.filter((r) => r.status() === 200 || r.status() === 201).length;
    const failures = allResults.filter((r) => r.status() >= 500).length;

    expect(failures).toBe(0);
    expect(success).toBeGreaterThan(50);
  });
});
