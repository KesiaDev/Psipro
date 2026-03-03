import {
  ExceptionFilter,
  Catch,
  ArgumentsHost,
  HttpException,
  HttpStatus,
} from '@nestjs/common';
import { Request, Response } from 'express';
import { LoggerService } from './logger.service';

/** 4xx considerados críticos para logging */
const CRITICAL_4XX = [401, 403, 404, 409, 429];

@Catch()
export class LoggingExceptionFilter implements ExceptionFilter {
  constructor(private readonly loggerService: LoggerService) {}

  catch(exception: unknown, host: ArgumentsHost): void {
    const ctx = host.switchToHttp();
    const res = ctx.getResponse<Response>();
    const req = ctx.getRequest<Request & { id?: string; _startTime?: number; _durationMs?: number; user?: unknown; clinicId?: string }>();

    const status =
      exception instanceof HttpException
        ? exception.getStatus()
        : HttpStatus.INTERNAL_SERVER_ERROR;

    const durationMs = req._durationMs ?? (req._startTime ? Date.now() - req._startTime : undefined);
    const ctxBase = this.loggerService.buildContext({
      ...req,
      _startTime: req._startTime,
    });

    const context = {
      ...ctxBase,
      durationMs,
      statusCode: status,
      errorMessage: exception instanceof Error ? exception.message : String(exception),
    };

    if (status >= 500) {
      this.loggerService.error5xx(
        'server_error',
        exception,
        context,
      );
    } else if (status === 429) {
      this.loggerService.rateLimitBlocked('rate_limit_blocked', {
        ...context,
        statusCode: 429,
      });
    } else if (CRITICAL_4XX.includes(status)) {
      this.loggerService.error4xxCritical('client_error_critical', {
        ...context,
        statusCode: status,
      });
    }

    const body =
      exception instanceof HttpException
        ? exception.getResponse()
        : { statusCode: 500, message: 'Internal server error' };

    const responseBody = typeof body === 'object' ? body : { message: body };
    if (typeof (responseBody as Record<string, unknown>).statusCode !== 'number') {
      (responseBody as Record<string, unknown>).statusCode = status;
    }

    res.status(status).json(responseBody);
  }
}
