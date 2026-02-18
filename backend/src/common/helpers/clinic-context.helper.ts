/**
 * ETAPA 4 — Helper para isolamento multi-tenant
 *
 * getCurrentClinicId(req) extrai o clinicId do usuário autenticado.
 * Toda query deve filtrar por clinicId quando disponível.
 */
import { ExecutionContext } from '@nestjs/common';

export function getCurrentClinicId(ctx: ExecutionContext): string | undefined {
  const request = ctx.switchToHttp().getRequest();
  return request?.user?.clinicId;
}
