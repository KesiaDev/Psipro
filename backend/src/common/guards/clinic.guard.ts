import {
  CanActivate,
  ExecutionContext,
  Injectable,
  BadRequestException,
} from '@nestjs/common';
import { ClinicContextHelper } from '../clinic-context.helper';

/**
 * Valida x-clinic-id no header e garante que o usuário pertence à clínica.
 * Anexa clinicId validado em request.clinicId.
 */
@Injectable()
export class ClinicGuard implements CanActivate {
  constructor(private clinicContext: ClinicContextHelper) {}

  async canActivate(context: ExecutionContext): Promise<boolean> {
    const request = context.switchToHttp().getRequest();
    const userId = request?.user?.id ?? request?.user?.sub;
    const clinicId = request.headers['x-clinic-id'];

    if (!clinicId || typeof clinicId !== 'string' || !clinicId.trim()) {
      throw new BadRequestException(
        'Header x-clinic-id é obrigatório para este endpoint',
      );
    }

    await this.clinicContext.getCurrentClinic(userId, clinicId.trim());
    request.clinicId = clinicId.trim();
    return true;
  }
}
