import { chromium } from 'playwright';
import * as fs from 'fs';
import * as path from 'path';

const BASE_URL = (process.env.BASE_URL || 'https://psipro-dashboard-production.up.railway.app').replace(/\/$/, '');
const USER = process.env.AUTH_USER || 'terapeutaclaudiacruz@gmail.com';
const PASS = process.env.AUTH_PASS || 'senha123';

interface ExplorerResult {
  page: string;
  path: string;
  status?: number;
  statusText?: string;
  jsErrors: string[];
  explored: boolean;
  error?: string;
}

const results: ExplorerResult[] = [];
const visited = new Set<string>();
const maxDepth = 2;
const sameOrigin = (url: string) => url.startsWith(BASE_URL) || url.startsWith('/');

function normalizePath(url: string): string {
  try {
    const u = new URL(url, BASE_URL);
    if (u.origin !== new URL(BASE_URL).origin) return '';
    const p = u.pathname || '/';
    return p === '' ? '/' : p;
  } catch {
    return '';
  }
}

async function explore() {
  const reportDir = path.join(process.cwd(), 'reports');
  if (!fs.existsSync(reportDir)) fs.mkdirSync(reportDir, { recursive: true });

  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext();
  const page = await context.newPage();

  const jsErrors: string[] = [];
  page.on('pageerror', (err) => {
    jsErrors.push(err.message);
    console.log('⚠️ Erro JS:', err.message);
  });

  console.log('Explorer Agent V3 iniciando...');
  console.log('Base URL:', BASE_URL);

  const loginPaths = ['/login', '/auth/login', '/'];
  let loggedIn = false;

  for (const loginPath of loginPaths) {
    try {
      await page.goto(`${BASE_URL}${loginPath}`, { waitUntil: 'domcontentloaded', timeout: 15000 });

      const emailInput = page.locator('input[type=email], input[name=email]').first();
      const passInput = page.locator('input[type=password], input[name=password]').first();
      const submitBtn = page.locator('button[type=submit]').first();

      if ((await emailInput.count()) > 0 && (await passInput.count()) > 0) {
        await emailInput.fill(USER);
        await passInput.fill(PASS);
        await submitBtn.click();
        await page.waitForURL((url) => !url.pathname.match(/\/login\/?$/), { timeout: 15000 }).catch(() => {});
        loggedIn = !page.url().includes('/login');
        if (loggedIn) {
          console.log('Login OK');
          break;
        }
      }
    } catch {
      continue;
    }
  }

  if (!loggedIn) {
    console.warn('Login não efetuado - explorando páginas públicas');
  }

  const initialPaths = [
    '/',
    '/pacientes',
    '/patients',
    '/agenda',
    '/calendar',
    '/sessions',
    '/prontuarios',
    '/relatorios',
    '/reports',
    '/settings',
    '/configuracoes',
    '/clinics',
    '/financeiro',
  ];

  async function visitPage(pathStr: string, depth: number) {
    const fullUrl = pathStr.startsWith('http') ? pathStr : `${BASE_URL}${pathStr}`;
    const normalized = normalizePath(fullUrl);
    if (!normalized || visited.has(normalized) || depth > maxDepth) return;

    visited.add(normalized);
    const pathErrors: string[] = [];

    const errorHandler = (err: Error) => pathErrors.push(err.message);
    page.once('pageerror', errorHandler);

    try {
      console.log('Explorando:', normalized);

      const response = await page.goto(fullUrl, {
        waitUntil: 'domcontentloaded',
        timeout: 15000,
      });

      const status = response?.status();
      const statusText = response?.statusText();

      if (status && status >= 400) {
        console.log('⚠️ Página com problema:', normalized, status, statusText);
      }

      results.push({
        page: fullUrl,
        path: normalized,
        status,
        statusText,
        jsErrors: [...pathErrors],
        explored: true,
      });

      if (depth < maxDepth && status && status < 400) {
        const links = await page.$$eval('a[href]', (as) =>
          as.map((a) => (a as HTMLAnchorElement).href).filter((h) => h && !h.startsWith('javascript:'))
        );

        const internalLinks = [...new Set(links.filter(sameOrigin).map(normalizePath).filter(Boolean))];
        for (const link of internalLinks.slice(0, 15)) {
          if (!visited.has(link)) await visitPage(link, depth + 1);
        }
      }
    } catch (e) {
      console.log('Erro ao carregar:', normalized, (e as Error).message);
      results.push({
        page: fullUrl,
        path: normalized,
        status: 0,
        jsErrors: [...pathErrors],
        explored: false,
        error: (e as Error).message,
      });
    }

    page.off('pageerror', errorHandler);
  }

  for (const p of initialPaths) {
    if (!visited.has(p === '' ? '/' : p)) {
      await visitPage(p.startsWith('/') ? p : `/${p}`, 0);
    }
  }

  await browser.close();

  writeReport(reportDir);
  printSummary();
  console.log('Exploração finalizada');
}

function writeReport(reportDir: string) {
  const reportPath = path.join(reportDir, 'explorer-report.json');
  fs.writeFileSync(reportPath, JSON.stringify(results, null, 2), 'utf-8');
  console.log('Relatório salvo em', reportPath);
}

function printSummary() {
  const problematic = results.filter(
    (r) => (r.status && r.status >= 400) || r.jsErrors.length > 0 || r.error
  );
  if (problematic.length > 0) {
    console.log('\n--- Resumo de problemas ---');
    problematic.forEach((r) => {
      console.log(`  ${r.path}: status=${r.status}, erros=${r.jsErrors?.length || 0}${r.error ? `, ${r.error}` : ''}`);
    });
  }
}

explore();
