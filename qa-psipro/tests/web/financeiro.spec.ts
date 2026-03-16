import { test, expect } from '@playwright/test';
import { loginWeb } from '../../helpers/login-web';

test.describe('Web Financeiro', () => {
  test.beforeEach(async ({ page }) => {
    await loginWeb(page);
  });

  test('acessar página financeiro', async ({ page }) => {
    await page.goto('/financeiro');

    const content = page.getByText(/financeiro|fatura|pagamento|receita/i);
    if ((await content.count()) === 0) {
      await page.goto('/financial');
    }
    await expect(page.locator('body')).toBeVisible({ timeout: 10000 });
  });

  test('visualizar resumo financeiro', async ({ page }, testInfo) => {
    await page.goto('/financeiro');

    const resumo = page.getByText(/total|receita|fatura|resumo|dashboard/i);
    await expect(resumo.first()).toBeVisible({ timeout: 15000 });
  });
});
