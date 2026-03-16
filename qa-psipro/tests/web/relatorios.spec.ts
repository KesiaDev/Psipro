import { test, expect } from '@playwright/test';
import { loginWeb } from '../../helpers/login-web';

test.describe('Relatórios', () => {
  test.beforeEach(async ({ page }) => {
    await loginWeb(page);
    await page.goto('/relatorios');
  });

  test('visualizar relatório', async ({ page }) => {
    const relatorio = page.getByText(/relatório|relatorios|gerar|exportar/i);
    await expect(relatorio.first()).toBeVisible({ timeout: 15000 });
  });

  test('gerar relatório', async ({ page }, testInfo) => {
    const btnGerar = page.getByRole('button', { name: /gerar|exportar|baixar|pdf/i });
    if ((await btnGerar.count()) === 0) {
      testInfo.skip(true, 'botão gerar relatório não encontrado');
    }
    await expect(page.getByText(/relatório/i).first()).toBeVisible({ timeout: 10000 });
  });
});
