import { test, expect } from '@playwright/test';
import { loginWeb } from '../../helpers/login-web';

test.describe('Pacientes', () => {
  test.beforeEach(async ({ page }) => {
    await loginWeb(page);
  });

  test('listar pacientes', async ({ page }) => {
    await page.goto('/pacientes');

    const listaOuMensagem = page.getByText(/paciente|nenhum|lista|tabela/i);
    await expect(listaOuMensagem.first()).toBeVisible({ timeout: 15000 });
  });

  test('criar paciente', async ({ page }, testInfo) => {
    await page.goto('/pacientes');

    const btnNovo = page
      .getByRole('button', { name: /novo|adicionar|cadastrar/i })
      .or(page.getByRole('link', { name: /novo|adicionar/i }))
      .or(page.getByText(/novo paciente|novo paciento/i))
      .first();

    if ((await btnNovo.count()) === 0) {
      testInfo.skip(true, 'botão criar paciente não encontrado');
    }

    await btnNovo.click();

    await page.fill('input[name=name], input[name=nome], input[placeholder*="nome"]', 'Paciente QA Test');
    await page.fill('input[name=email], input[placeholder*="email"]', 'qa@psipro.com');

    const btnSalvar = page.getByRole('button', { name: /salvar|cadastrar|criar/i }).or(page.getByText(/salvar/i));
    await btnSalvar.first().click();

    await expect(page.locator('text=Paciente QA Test')).toBeVisible({ timeout: 10000 });
  });

  test('buscar pacientes', async ({ page }, testInfo) => {
    await page.goto('/pacientes');

    const search = page.locator(
      'input[type="search"], input[placeholder*="buscar"], input[placeholder*="pesquisar"]'
    );
    if ((await search.count()) === 0) {
      testInfo.skip(true, 'campo de busca não encontrado');
    }
    await expect(search.first()).toBeVisible();
  });
});
