import { ExecutionContext } from '@nestjs/common';
import { Request } from 'express';

/** IPs considerados internos — não aplicam rate limit */
const INTERNAL_IPS = new Set([
  '127.0.0.1',
  '::1',
  '::ffff:127.0.0.1',
  'localhost',
]);

/**
 * Verifica se o IP da requisição é interno.
 * Variável de ambiente THROTTLE_SKIP_IPS pode adicionar IPs (separados por vírgula).
 */
export function isInternalIp(req: Request): boolean {
  const rawIp = req.ip ?? req.socket?.remoteAddress;
  const forwarded = req.headers['x-forwarded-for'];
  const ip = forwarded
    ? (typeof forwarded === 'string' ? forwarded.split(',')[0] : forwarded[0])?.trim() ?? rawIp
    : rawIp;

  if (!ip) return false;
  if (INTERNAL_IPS.has(ip)) return true;

  const extra = process.env.THROTTLE_SKIP_IPS;
  if (extra) {
    const list = extra.split(',').map((s) => s.trim()).filter(Boolean);
    if (list.includes(ip)) return true;
  }

  return false;
}

export function getSkipIfInternalIp() {
  return (context: ExecutionContext): boolean => {
    const req = context.switchToHttp().getRequest<Request>();
    return isInternalIp(req);
  };
}
