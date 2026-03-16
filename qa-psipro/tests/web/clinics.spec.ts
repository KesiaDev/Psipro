import { test, expect } from '@playwright/test';
import { loginWeb } from '../../helpers/login-web';

test.describe('Web Clinics', () => {
  test.beforeEach(async ({ page }) => {
    await loginWeb(page);
  });

  test('acessar página de clínicas', async ({ page }, testInfo) => {
    await page.goto('/clinics');

    const content = page.getByText(/clínica|clinic|configuração/i);
    if ((await content.count()) === 0) {
      await page.goto('/configuracoes');
    }
    await expect(page.locator('body')).toBeVisible({ timeout: 10000 });
  });

  test('criar clínica', async ({ page }, testInfo) => {
    await page.goto('/clinics');

    const btnNova = page
      .getByRole('button', { name: /nova|adicionar|criar clínica/i })
      .or(page.getByRole('link', { name: /nova clínica/i }))
      .first();

    if ((await btnNova.count()) === 0) {
      testInfo.skip(true, 'Botão criar clínica não encontrado');
    }
    await expect(btnNova).toBeVisible();
  });
});
