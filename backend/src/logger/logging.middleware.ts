import { Request, Response, NextFunction } from 'express';
import { randomUUID } from 'crypto';

declare global {
  namespace Express {
    interface Request {
      id?: string;
      _startTime?: number;
    }
  }
}

const ROUTES_TO_LOG = ['/api/appointments/today', '/api/dashboard/count'];

/**
 * Middleware que atribui requestId e _startTime a cada requisição.
 * Essenciais para correlação de logs e medição de duração.
 * Para rotas de validação, loga URL, headers e status.
 */
export function loggingMiddleware(req: Request, res: Response, next: NextFunction): void {
  req.id = (req.headers['x-request-id'] as string) ?? randomUUID();
  req._startTime = Date.now();

  const path = (req.path || req.url?.split('?')[0] || '').replace(/\/$/, '') || '/';
  const shouldLog = ROUTES_TO_LOG.some((r) => path === r || path.endsWith(r));

  if (shouldLog) {
    const onFinish = () => {
      const headers = {
        authorization: req.headers.authorization ? 'Bearer ***' : '(ausente)',
        'x-clinic-id': req.headers['x-clinic-id'] ?? '(ausente)',
      };
      console.log('[RouteValidation]', {
        url: `${req.method} ${req.originalUrl || req.url}`,
        path,
        headers,
        status: res.statusCode,
      });
    };
    res.on('finish', onFinish);
  }

  next();
}
