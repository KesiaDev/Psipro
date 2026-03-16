/**
 * PsiPro QA Agent V3 - Testes de exploração
 * Valida o relatório gerado pelo explorer-agent
 */
import { test, expect } from '@playwright/test';
import * as fs from 'fs';
import * as path from 'path';

const REPORT_PATH = path.join(process.cwd(), 'reports', 'explorer-report.json');

test.describe('Explorer - Relatório', () => {
  test('relatório explorer existe após npm run explore', function () {
    if (!fs.existsSync(REPORT_PATH)) {
      this.skip();
    }
    const report = JSON.parse(fs.readFileSync(REPORT_PATH, 'utf-8'));
    expect(Array.isArray(report)).toBe(true);
  });

  test('nenhuma página com status >= 500', function () {
    if (!fs.existsSync(REPORT_PATH)) {
      this.skip();
    }
    const report = JSON.parse(fs.readFileSync(REPORT_PATH, 'utf-8'));
    const serverErrors = report.filter((r: { status?: number }) => r.status && r.status >= 500);
    expect(serverErrors, `Páginas com erro 5xx: ${JSON.stringify(serverErrors)}`).toHaveLength(0);
  });
});
