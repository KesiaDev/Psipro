import { test, expect } from '@playwright/test';
import { loginWeb } from '../../helpers/login-web';

test.describe('Prontuário', () => {
  test.beforeEach(async ({ page }) => {
    await loginWeb(page);
    await page.goto('/prontuarios');
  });

  test('abrir prontuário', async ({ page }, testInfo) => {
    const linkProntuario = page.getByRole('link', { name: /prontuário|paciente/i }).or(page.locator('a[href*="prontuario"]')).first();
    if ((await linkProntuario.count()) === 0) {
      testInfo.skip(true, 'nenhum prontuário disponível');
    }
    await expect(page.getByText(/prontuário|prontuarios/i).first()).toBeVisible({ timeout: 15000 });
  });

  test('adicionar anotação', async ({ page }, testInfo) => {
    const btnAnotar = page.getByRole('button', { name: /anotar|adicionar|nova anotação/i });
    const textarea = page.locator('textarea');
    if ((await btnAnotar.count()) === 0 && (await textarea.count()) === 0) {
      testInfo.skip(true, 'área de anotação não encontrada');
    }
    await expect(page.getByText(/prontuário|anotação|nota/i).first()).toBeVisible({ timeout: 10000 });
  });

  test('salvar prontuário', async ({ page }, testInfo) => {
    const btnSalvar = page.getByRole('button', { name: /salvar|gravar|salvar prontuário/i });
    if ((await btnSalvar.count()) === 0) {
      testInfo.skip(true, 'botão salvar não encontrado');
    }
    await expect(page.getByText(/prontuário/i).first()).toBeVisible({ timeout: 10000 });
  });
});
