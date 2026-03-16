import { defineConfig } from '@playwright/test';

const baseURL = process.env.BASE_URL || 'https://psipro-dashboard-production.up.railway.app';
const apiURL = process.env.API_URL || process.env.BASE_URL || 'https://psipro-backend-production.up.railway.app';

export default defineConfig({
  testDir: './tests',
  timeout: 30000,
  globalSetup: './global-setup.ts',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 2 : 4,
  use: {
    baseURL,
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
    trace: 'retain-on-failure',
  },
  reporter: [
    ['html', { outputFolder: 'playwright-report' }],
    ['junit', { outputFile: 'reports/junit.xml' }],
  ],
  projects: [
    { name: 'infra', testDir: './tests/infra', use: { baseURL: apiURL } },
    { name: 'api', testDir: './tests/api', use: { baseURL: apiURL }, workers: 1 },
    { name: 'web', testDir: './tests/web' },
    { name: 'ai', testDir: './tests/ai', use: { baseURL: apiURL } },
    { name: 'sync', testDir: './tests/sync', use: { baseURL: apiURL } },
    { name: 'security', testDir: './tests/security', use: { baseURL: apiURL } },
    { name: 'performance', testDir: './tests/performance', use: { baseURL: apiURL } },
    { name: 'explorer', testDir: './tests/explorer' },
  ],
});
