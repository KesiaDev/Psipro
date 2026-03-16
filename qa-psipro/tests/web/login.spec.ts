import { test, expect } from '@playwright/test';

const EMAIL = process.env.AUTH_USER || 'terapeutaclaudiacruz@gmail.com';
const PASSWORD = process.env.AUTH_PASS || 'senha123';

test.describe('Login Web', () => {
  test('login válido', async ({ page }) => {
    await page.goto('/login');

    await page.fill('input[name=email], input[type=email]', EMAIL);
    await page.fill('input[name=password], input[type=password]', PASSWORD);

    await page.click('button[type=submit]');

    await expect(page).toHaveURL(/dashboard/);
  });

  test('login inválido', async ({ page }) => {
    await page.goto('/login');

    await page.fill('input[name=email], input[type=email]', 'invalido@test.com');
    await page.fill('input[name=password], input[type=password]', 'senhaerrada');

    await page.click('button[type=submit]');

    const erroOuPermanece = page.getByText(/inválid|incorreto|erro|credencial/i);
    await expect(erroOuPermanece.or(page.locator('form'))).toBeVisible({ timeout: 5000 });
    expect(page.url()).toContain('/login');
  });
});
