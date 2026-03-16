import { test, expect } from '@playwright/test';
import { loginWeb } from '../../helpers/login-web';

test.describe('Dashboard', () => {
  test.beforeEach(async ({ page }) => {
    await loginWeb(page);
    await page.goto('/');
  });

  test('dashboard carrega corretamente', async ({ page }) => {
    await expect(page).not.toHaveURL(/\/login$/);
    await expect(page.locator('body')).toBeVisible({ timeout: 10000 });
  });

  test('widgets aparecem', async ({ page }) => {
    const widgets = page.getByText(/paciente|sessão|agenda|total|resumo|dashboard/i);
    await expect(widgets.first()).toBeVisible({ timeout: 15000 });
  });
});
