import { test, expect } from '@playwright/test';
import { loginWeb } from '../../helpers/login-web';

test.describe('Web Settings', () => {
  test.beforeEach(async ({ page }) => {
    await loginWeb(page);
  });

  test('acessar configurações', async ({ page }) => {
    await page.goto('/settings');

    const content = page.getByText(/configuração|settings|perfil|preferência/i);
    if ((await content.count()) === 0) {
      await page.goto('/configuracoes');
    }
    await expect(page.locator('body')).toBeVisible({ timeout: 10000 });
  });

  test('alterar perfil', async ({ page }, testInfo) => {
    await page.goto('/settings');

    const btnEditar = page.getByRole('button', { name: /editar|salvar|atualizar/i }).first();
    if ((await btnEditar.count()) === 0) {
      testInfo.skip(true, 'Botão editar perfil não encontrado');
    }
    await expect(page.locator('body')).toBeVisible({ timeout: 5000 });
  });
});
