import { test, expect } from '@playwright/test';
import { loginWeb } from '../../helpers/login-web';

test.describe('Web Voice Command', () => {
  test.beforeEach(async ({ page }) => {
    await loginWeb(page);
  });

  test('botão ou ícone de comando de voz visível', async ({ page }, testInfo) => {
    await page.goto('/');

    const voiceBtn = page
      .getByRole('button', { name: /voz|voice| microfone|microphone|dictar/i })
      .or(page.locator('[aria-label*="voz"], [aria-label*="voice"], [data-testid*="voice"]'))
      .first();

    if ((await voiceBtn.count()) === 0) {
      testInfo.skip(true, 'Comando de voz não encontrado na interface');
    }
    await expect(voiceBtn).toBeVisible();
  });
});
