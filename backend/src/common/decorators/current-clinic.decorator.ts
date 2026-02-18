/**
 * ETAPA 4 — Decorator para injetar clinicId do contexto
 *
 * Uso em controller:
 *   @Get()
 *   findAll(@CurrentUser() user, @CurrentClinicId() clinicId: string | undefined) { ... }
 */
import { createParamDecorator, ExecutionContext } from '@nestjs/common';

export const CurrentClinicId = createParamDecorator(
  (data: unknown, ctx: ExecutionContext) => {
    const request = ctx.switchToHttp().getRequest();
    return request?.user?.clinicId;
  },
);
