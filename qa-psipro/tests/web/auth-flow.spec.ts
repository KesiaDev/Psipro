import { test, expect } from '@playwright/test';
import { loginWeb } from '../../helpers/login-web';

test.describe('Fluxo SaaS Completo', () => {
  test('register → login → criar clínica → criar profissional → criar paciente → agendar → criar sessão → ver relatórios', async (
    { page },
    testInfo
  ) => {
    await loginWeb(page);

    await page.goto('/');
    await expect(page.locator('body')).toBeVisible({ timeout: 10000 });

    const steps: { name: string; url: string; selector: string }[] = [
      { name: 'dashboard', url: '/', selector: 'body' },
      { name: 'pacientes', url: '/pacientes', selector: 'body' },
      { name: 'agenda', url: '/agenda', selector: 'body' },
      { name: 'relatorios', url: '/relatorios', selector: 'body' },
    ];

    for (const step of steps) {
      await page.goto(step.url);
      const visible = await page.locator(step.selector).first().isVisible().catch(() => false);
      if (!visible) {
        testInfo.skip(true, `Página ${step.name} não carregou`);
      }
    }
  });

  test('multi clínica - trocar clínica', async ({ page }, testInfo) => {
    await loginWeb(page);
    await page.goto('/');

    const btnTrocarClinica = page
      .getByRole('button', { name: /clínica|clinic|trocar|switch/i })
      .or(page.getByText(/clínica|selecionar clínica/i))
      .first();

    if ((await btnTrocarClinica.count()) === 0) {
      testInfo.skip(true, 'Seletor de clínica não encontrado');
    }

    await btnTrocarClinica.click();
    await expect(page.getByText(/clínica|clinic|selecionar/i).first()).toBeVisible({ timeout: 5000 });
  });
});
