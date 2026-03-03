import {
  Injectable,
  NestInterceptor,
  ExecutionContext,
  CallHandler,
} from '@nestjs/common';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Request } from 'express';

/**
 * Interceptor global para medir duração da requisição.
 * Atribui req._endTime e req._durationMs após conclusão.
 * O ExceptionFilter usa esses valores ao logar erros.
 */
@Injectable()
export class LoggingInterceptor implements NestInterceptor {
  intercept(context: ExecutionContext, next: CallHandler): Observable<unknown> {
    const req = context.switchToHttp().getRequest<Request & { _startTime?: number; id?: string }>();
    const start = req._startTime ?? Date.now();
    req._startTime = start;

    return next.handle().pipe(
      tap({
        next: () => {
          const duration = Date.now() - start;
          (req as Request & { _durationMs?: number })._durationMs = duration;
        },
        error: () => {
          const duration = Date.now() - start;
          (req as Request & { _durationMs?: number })._durationMs = duration;
        },
      }),
    );
  }
}
