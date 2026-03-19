import { NestFactory } from '@nestjs/core';
import { ValidationPipe } from '@nestjs/common';
import helmet from 'helmet';
import { AppModule } from './app.module';
import { loggingMiddleware } from './logger/logging.middleware';
import { SystemHealthService } from './system-health/system-health.service';

/** Extrai rotas mapeadas do Express (para log de deploy) */
function getMappedRoutes(expressApp: any): string[] {
  const routes: string[] = [];
  if (!expressApp?._router?.stack) return routes;

  function walk(stack: any[], prefix = '') {
    for (const layer of stack) {
      if (layer.route) {
        const path = (prefix + layer.route.path).replace(/\/+/g, '/') || '/';
        const methods = Object.keys(layer.route.methods).filter((m) => layer.route.methods[m]);
        methods.forEach((m) => routes.push(`${m.toUpperCase()} ${path}`));
      } else if (layer.name === 'router' && layer.handle?.stack) {
        const match = layer.regexp?.toString().match(/^\\\^?(.+)\\\$?\//);
        const parentPath = match ? match[1].replace(/\\\//g, '/') : '';
        walk(layer.handle.stack, prefix + (parentPath ? '/' + parentPath : ''));
      }
    }
  }
  walk(expressApp._router.stack);
  return [...new Set(routes)].sort();
}

async function bootstrap() {
  const app = await NestFactory.create(AppModule);

  app.use(
    helmet({
      crossOriginResourcePolicy: { policy: 'cross-origin' },
      crossOriginOpenerPolicy: { policy: 'same-origin-allow-popups' },
    }),
  );
  app.use(loggingMiddleware);

  const corsOrigins = [
    'https://psipro-dashboard-production.up.railway.app',
    'http://psipro-dashboard-production.up.railway.app',
    /^https:\/\/.*\.railway\.app$/,
    /^https:\/\/.*\.up\.railway\.app$/,
    'http://localhost:5173',
    'http://localhost:3000',
  ];
  app.enableCors({
    origin: (origin, callback) => {
      if (!origin) return callback(null, true);
      const allowed = corsOrigins.some((o) =>
        typeof o === 'string' ? o === origin : (o as RegExp).test(origin),
      );
      return callback(null, allowed);
    },
    methods: 'GET,HEAD,PUT,PATCH,POST,DELETE,OPTIONS',
    allowedHeaders: 'Content-Type, Authorization, X-Clinic-Id, x-clinic-id',
    credentials: true,
  });

  // Helmet já remove X-Powered-By por padrão (hidePoweredBy: true)

  app.useGlobalPipes(
    new ValidationPipe({
      whitelist: true,
      forbidNonWhitelisted: true,
      transform: true,
    }),
  );

  app.setGlobalPrefix('api', {
      exclude: ['system-health', 'system-health/full', 'system-health/metrics'],
    });

  // Rota /api/system-health para o dashboard (raiz /system-health para load balancers)
  const httpAdapter = app.getHttpAdapter();
  const expressApp = (httpAdapter as { getInstance?: () => any }).getInstance?.();
  const systemHealthService = app.get(SystemHealthService);
  if (expressApp && systemHealthService) {
    expressApp.get('/api/system-health', async (_req: any, res: any) => {
      try {
        const data = await systemHealthService.check();
        res.json(data);
      } catch (e) {
        res.status(500).json({ status: 'down', error: (e as Error).message });
      }
    });
    expressApp.get('/api/system-health/full', async (_req: any, res: any) => {
      try {
        const data = await systemHealthService.checkFull();
        res.json(data);
      } catch (e) {
        res.status(500).json({ status: 'down', error: (e as Error).message });
      }
    });
  }

  const port = process.env.PORT ? Number(process.env.PORT) : 8080;
  await app.listen(port, '0.0.0.0');

  const mappedRoutes = expressApp ? getMappedRoutes(expressApp) : [];

  console.log(`🚀 PsiPro API running on port ${port}/api`);
  console.log('Mapped routes:', mappedRoutes);

  const hasAppointmentsToday = mappedRoutes.some((r) => r.includes('/appointments/today'));
  const hasDashboardCount = mappedRoutes.some((r) => r.includes('/dashboard/count'));
  const hasReports = mappedRoutes.some((r) => r.includes('/reports') && r.startsWith('GET'));
  console.log('Reports module loaded');
  console.log('✓ /api/appointments/today:', hasAppointmentsToday ? 'SIM' : 'NÃO');
  console.log('✓ /api/dashboard/count:', hasDashboardCount ? 'SIM' : 'NÃO');
  console.log('✓ /api/reports (GET):', hasReports ? 'SIM' : 'NÃO');
}

bootstrap();


