/**
 * Global setup - valida /system-health antes de rodar qualquer teste.
 * Se status = "down" → aborta o pipeline (exit 1).
 */
import { getApiUrl } from './helpers/login-api';

async function globalSetup() {
  const url = getApiUrl('/system-health');
  console.log('[globalSetup] Verificando system-health...', url);

  try {
    const response = await fetch(url);
    const statusCode = response.status;

    if (statusCode >= 500) {
      console.error('[globalSetup] ABORT: Sistema retornou', statusCode);
      process.exit(1);
    }

    if (statusCode !== 200) {
      console.warn('[globalSetup] system-health retornou', statusCode, '- continuando (endpoint pode não existir)');
      return;
    }

    const body = await response.json();
    const status = body?.status ?? body?.overall ?? body?.health;

    if (status !== undefined && String(status).toLowerCase() === 'down') {
      console.error('[globalSetup] ABORT: Sistema está DOWN');
      process.exit(1);
    }

    console.log('[globalSetup] Sistema OK');
  } catch (e) {
    console.warn('[globalSetup] Erro ao checar system-health:', (e as Error).message);
    console.warn('[globalSetup] Continuando (endpoint pode não existir)...');
  }
}

export default globalSetup;
