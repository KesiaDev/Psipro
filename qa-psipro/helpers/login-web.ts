import { Page } from '@playwright/test';

const EMAIL = process.env.AUTH_USER || 'terapeutaclaudiacruz@gmail.com';
const PASSWORD = process.env.AUTH_PASS || 'senha123';

/**
 * Login reutilizável no app web.
 * Evita repetir fluxo de login em todos os testes.
 */
export async function loginWeb(page: Page): Promise<void> {
  await page.goto('/login');

  await page.fill('input[name=email], input[type=email]', EMAIL);
  await page.fill('input[name=password], input[type=password]', PASSWORD);

  await page.click('button[type=submit]');

  await page.waitForURL((url) => !url.pathname.match(/\/login\/?$/), { timeout: 15000 });
}
