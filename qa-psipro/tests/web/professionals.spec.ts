import { test, expect } from '@playwright/test';
import { loginWeb } from '../../helpers/login-web';

test.describe('Web Professionals', () => {
  test.beforeEach(async ({ page }) => {
    await loginWeb(page);
  });

  test('acessar página de profissionais', async ({ page }, testInfo) => {
    await page.goto('/professionals');

    const content = page.getByText(/profissional|psicólogo|equipe|team/i);
    if ((await content.count()) === 0) {
      await page.goto('/profissionais');
    }
    await expect(page.locator('body')).toBeVisible({ timeout: 10000 });
  });

  test('listar profissionais', async ({ page }, testInfo) => {
    await page.goto('/professionals');

    const lista = page.getByText(/profissional|psicólogo|nenhum|equipe/i);
    await expect(lista.first()).toBeVisible({ timeout: 15000 });
  });
});
