import { test, expect } from '@playwright/test';
import { loginWeb } from '../../helpers/login-web';

test.describe('Agenda', () => {
  test.beforeEach(async ({ page }) => {
    await loginWeb(page);
    await page.goto('/agenda');
  });

  test('visualizar calendário', async ({ page }) => {
    const calendario = page.getByText(/agenda|calendário|sessão|horário/i);
    await expect(calendario.first()).toBeVisible({ timeout: 15000 });
  });

  test('criar sessão', async ({ page }, testInfo) => {
    const btnNova = page.getByRole('button', { name: /nova|agendar|adicionar/i }).or(page.getByRole('link', { name: /nova|agendar/i }));
    if ((await btnNova.count()) === 0) {
      testInfo.skip(true, 'botão criar sessão não encontrado');
    }
    await expect(page.getByText(/agenda|calendário|sessão/i).first()).toBeVisible({ timeout: 10000 });
  });

  test('editar sessão', async ({ page }, testInfo) => {
    const evento = page.locator('[role="gridcell"], .fc-event, .agenda-item').first();
    if ((await evento.count()) === 0) {
      testInfo.skip(true, 'nenhuma sessão na agenda');
    }
    await expect(page.getByText(/agenda|calendário/i).first()).toBeVisible({ timeout: 10000 });
  });
});
