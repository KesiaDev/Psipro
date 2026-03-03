import { NestFactory } from '@nestjs/core';
import { ValidationPipe } from '@nestjs/common';
import helmet from 'helmet';
import { AppModule } from './app.module';
import { loggingMiddleware } from './logger/logging.middleware';

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

/** Origens CORS permitidas (CORS_ORIGINS env, ex: https://app.psipro.com,https://web.psipro.com) */
function getCorsOrigins(): string[] | boolean {
  const raw = process.env.CORS_ORIGINS?.trim();
  if (!raw) {
    return process.env.NODE_ENV === 'production' ? [] : ['http://localhost:3000', 'http://localhost:5173'];
  }
  return raw.split(',').map((o) => o.trim()).filter(Boolean);
}

async function bootstrap() {
  const app = await NestFactory.create(AppModule);

  app.use(helmet());
  app.use(loggingMiddleware);

  const origins = getCorsOrigins();
  app.enableCors({
    origin: Array.isArray(origins) && origins.length > 0 ? origins : false,
    credentials: true,
    methods: ['GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'OPTIONS'],
    allowedHeaders: ['Content-Type', 'Authorization', 'x-clinic-id'],
  });

  // Helmet já remove X-Powered-By por padrão (hidePoweredBy: true)

  app.useGlobalPipes(
    new ValidationPipe({
      whitelist: true,
      forbidNonWhitelisted: true,
      transform: true,
    }),
  );

  app.setGlobalPrefix('api');

  const port = process.env.PORT ? Number(process.env.PORT) : 8080;
  await app.listen(port, '0.0.0.0');

  const httpAdapter = app.getHttpAdapter();
  const expressApp = (httpAdapter as { getInstance?: () => any }).getInstance?.();
  const mappedRoutes = expressApp ? getMappedRoutes(expressApp) : [];

  console.log(`🚀 PsiPro API running on port ${port}/api`);
  console.log('Mapped routes:', mappedRoutes);

  const hasAppointmentsToday = mappedRoutes.some((r) => r.includes('/appointments/today'));
  const hasDashboardCount = mappedRoutes.some((r) => r.includes('/dashboard/count'));
  console.log('✓ /api/appointments/today:', hasAppointmentsToday ? 'SIM' : 'NÃO');
  console.log('✓ /api/dashboard/count:', hasDashboardCount ? 'SIM' : 'NÃO');
}

bootstrap();


