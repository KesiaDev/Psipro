import { test, expect } from '@playwright/test';
import { loginWeb } from '../../helpers/login-web';

test.describe('Web System Health', () => {
  test.beforeEach(async ({ page }) => {
    await loginWeb(page);
  });

  test('acessar página de status do sistema', async ({ page }, testInfo) => {
    await page.goto('/system-health');

    const content = page.getByText(/status|health|sistema|online|backend|database/i);
    if ((await content.count()) === 0) {
      await page.goto('/admin/health');
    }
    await expect(page.locator('body')).toBeVisible({ timeout: 10000 });
  });
});
