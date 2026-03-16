import { Injectable } from '@nestjs/common';
import pino from 'pino';
import { sanitizeForLog } from './sanitize.helper';

/** Contexto padrão de request para enriquecer logs (Datadog/Sentry-ready) */
export interface LogContext {
  requestId?: string;
  userId?: string;
  clinicId?: string;
  endpoint?: string;
  method?: string;
  statusCode?: number;
  durationMs?: number;
  ip?: string;
  userAgent?: string;
}

/** Categorias para envio futuro a Datadog/Sentry */
export type LogCategory =
  | 'error_5xx'
  | 'error_4xx_critical'
  | 'financial_conflict'
  | 'invalid_access'
  | 'refresh_token_invalid'
  | 'role_violation'
  | 'auth_failure'
  | 'request'
  | 'rate_limit_blocked';

@Injectable()
export class LoggerService {
  private logger: pino.Logger;

  constructor() {
    this.logger = pino({
      level: process.env.LOG_LEVEL ?? (process.env.NODE_ENV === 'production' ? 'info' : 'debug'),
      formatters: {
        level: (label) => ({ level: label }),
        bindings: () => ({}),
      },
      timestamp: pino.stdTimeFunctions.isoTime,
      // JSON puro para Datadog/Sentry
      base: {
        service: 'psipro-backend',
        env: process.env.NODE_ENV ?? 'development',
      },
      // JSON para stdout — Datadog/Sentry ingestam via collectores
    });
  }

  private emit(
    level: 'info' | 'warn' | 'error' | 'debug',
    message: string,
    context?: Record<string, unknown>,
    category?: LogCategory,
  ) {
    const base: Record<string, unknown> = {
      message,
      ...(context ?? {}),
      ...(category && { category, dd: { tags: [`category:${category}`] } }),
    };
    const sanitized = sanitizeForLog(base) as Record<string, unknown>;
    this.logger[level](sanitized as object);
  }

  /** Erro 5xx (server error) */
  error5xx(
    message: string,
    error: unknown,
    context: LogContext & { stack?: string },
  ) {
    const err = error instanceof Error ? error : new Error(String(error));
    this.emit('error', message, {
      ...context,
      error: err.message,
      stack: err.stack,
    }, 'error_5xx');
  }

  /** Erro 4xx crítico (401, 403, 404, 409) */
  error4xxCritical(
    message: string,
    context: LogContext & { statusCode: number; errorMessage?: string },
  ) {
    const category = this.infer4xxCategory(context.statusCode, context.endpoint);
    this.emit('warn', message, { ...context } as Record<string, unknown>, category);
  }

  /** Conflito financeiro ou de agenda */
  financialConflict(message: string, context: LogContext & Record<string, unknown>) {
    this.emit('warn', message, { ...context } as Record<string, unknown>, 'financial_conflict');
  }

  /** Tentativa de acesso inválida */
  invalidAccess(message: string, context: LogContext) {
    this.emit('warn', message, { ...context } as Record<string, unknown>, 'invalid_access');
  }

  /** Refresh token inválido/expirado/revogado */
  refreshTokenInvalid(message: string, context: LogContext) {
    this.emit('warn', message, { ...context } as Record<string, unknown>, 'refresh_token_invalid');
  }

  /** Violação de role/permissão */
  roleViolation(message: string, context: LogContext) {
    this.emit('warn', message, { ...context } as Record<string, unknown>, 'role_violation');
  }

  /** Tentativa bloqueada por rate limit */
  rateLimitBlocked(message: string, context: LogContext & Record<string, unknown>) {
    this.emit('warn', message, { ...context } as Record<string, unknown>, 'rate_limit_blocked');
  }

  /** Requisição com duração (para auditoria ou slow requests) */
  request(context: LogContext & { statusCode: number }) {
    this.emit('info', 'request_completed', { ...context } as Record<string, unknown>, 'request');
  }

  private infer4xxCategory(statusCode: number, endpoint?: string): LogCategory {
    if (statusCode === 401) {
      if (endpoint?.includes('/auth/refresh')) return 'refresh_token_invalid';
      return 'invalid_access';
    }
    if (statusCode === 403) return 'role_violation';
    if (statusCode === 409) return 'financial_conflict';
    return 'error_4xx_critical';
  }

  /** Log genérico com contexto (para debug/info) */
  logMessage(level: 'info' | 'warn' | 'error' | 'debug', message: string, meta?: Record<string, unknown>) {
    const sanitized = meta ? sanitizeForLog(meta) as Record<string, unknown> : {};
    this.logger[level]({ message, ...sanitized });
  }

  /** Build context a partir do request Express */
  buildContext(req: {
    id?: string;
    _startTime?: number;
    method?: string;
    url?: string;
    path?: string;
    ip?: string;
    headers?: Record<string, unknown>;
    user?: { id?: string; sub?: string; clinicId?: string };
    clinicId?: string;
  }): LogContext {
    const now = Date.now();
    const start = req._startTime ?? now;
    return {
      requestId: req.id,
      userId: req?.user?.id ?? req?.user?.sub,
      clinicId: req?.clinicId ?? req?.user?.clinicId,
      endpoint: req.url ?? req.path ?? req.url,
      method: req.method,
      durationMs: now - start,
      ip: req.ip,
      userAgent: typeof req.headers?.['user-agent'] === 'string' ? req.headers['user-agent'] : undefined,
    };
  }
}
