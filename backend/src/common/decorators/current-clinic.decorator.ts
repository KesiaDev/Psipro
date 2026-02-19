/**
 * Decorator para injetar clinicId validado pelo ClinicGuard.
 * Lê request.clinicId (definido pelo ClinicGuard após validar x-clinic-id).
 * Requer @UseGuards(JwtAuthGuard, ClinicGuard).
 */
import { createParamDecorator, ExecutionContext } from '@nestjs/common';

export const CurrentClinicId = createParamDecorator(
  (data: unknown, ctx: ExecutionContext) => {
    const request = ctx.switchToHttp().getRequest();
    return request?.clinicId ?? request?.headers?.['x-clinic-id'];
  },
);
