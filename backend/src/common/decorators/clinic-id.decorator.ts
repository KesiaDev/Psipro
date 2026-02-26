import { createParamDecorator, ExecutionContext } from '@nestjs/common';

/**
 * Decorator para obter clinicId do request (injetado pelo ClinicContextGuard).
 * Retorna string | null.
 */
export const ClinicId = createParamDecorator(
  (data: unknown, ctx: ExecutionContext): string | null => {
    const request = ctx.switchToHttp().getRequest();
    return request.clinicId ?? null;
  },
);
