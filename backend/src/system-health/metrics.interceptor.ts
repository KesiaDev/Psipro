import {
  Injectable,
  NestInterceptor,
  ExecutionContext,
  CallHandler,
} from '@nestjs/common';
import { Observable } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { Request } from 'express';
import { MetricsService } from './metrics.service';

const SKIP_PATHS = ['/system-health', '/api/system-health'];

@Injectable()
export class MetricsInterceptor implements NestInterceptor {
  constructor(private readonly metricsService: MetricsService) {}

  intercept(context: ExecutionContext, next: CallHandler): Observable<unknown> {
    const req = context.switchToHttp().getRequest<Request>();
    const path = req.path ?? req.url?.split('?')[0] ?? '';

    if (SKIP_PATHS.some((p) => path === p || path.startsWith(p + '/'))) {
      return next.handle();
    }

    const start = Date.now();
    const clientId = this.getClientId(req);

    return next.handle().pipe(
      tap(() => {
        const latencyMs = Date.now() - start;
        this.metricsService.recordRequest(latencyMs, false, clientId);
      }),
      catchError((err) => {
        const latencyMs = Date.now() - start;
        this.metricsService.recordRequest(latencyMs, true, clientId);
        throw err;
      }),
    );
  }

  private getClientId(req: Request): string {
    const forwarded = req.headers['x-forwarded-for'];
    if (typeof forwarded === 'string') {
      return forwarded.split(',')[0].trim();
    }
    return req.ip ?? req.socket?.remoteAddress ?? 'unknown';
  }
}
