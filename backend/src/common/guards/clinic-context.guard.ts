import {
  CanActivate,
  ExecutionContext,
  Injectable,
  BadRequestException,
  ForbiddenException,
} from '@nestjs/common';
import { Reflector } from '@nestjs/core';
import { PrismaService } from '../../prisma/prisma.service';
import { SKIP_CLINIC_CONTEXT } from '../decorators/skip-clinic-context.decorator';

@Injectable()
export class ClinicContextGuard implements CanActivate {
  constructor(
    private prisma: PrismaService,
    private reflector: Reflector,
  ) {}

  async canActivate(context: ExecutionContext): Promise<boolean> {
    const skip = this.reflector.getAllAndOverride<boolean>(SKIP_CLINIC_CONTEXT, [
      context.getHandler(),
      context.getClass(),
    ]);

    if (skip) {
      return true;
    }

    const request = context.switchToHttp().getRequest();
    const user = request.user;

    if (!user?.sub) {
      return true;
    }

    const headerClinicId = request.headers['x-clinic-id'] as string | undefined;
    const clinicIdFromHeader = headerClinicId?.trim() || null;

    const userClinics = await this.prisma.clinicUser.findMany({
      where: {
        userId: user.sub,
        status: 'active',
      },
      select: { clinicId: true },
    });

    const clinicIds = userClinics.map((uc) => uc.clinicId);

    if (clinicIds.length === 0) {
      request.clinicId = null;
      return true;
    }

    if (clinicIds.length === 1 && !clinicIdFromHeader) {
      request.clinicId = clinicIds[0];
      return true;
    }

    if (clinicIds.length >= 2 && !clinicIdFromHeader) {
      throw new BadRequestException(
        'Header X-Clinic-Id é obrigatório quando o usuário pertence a múltiplas clínicas',
      );
    }

    if (clinicIdFromHeader) {
      const hasAccess = clinicIds.includes(clinicIdFromHeader);
      if (!hasAccess) {
        throw new ForbiddenException('Usuário não pertence a esta clínica');
      }
      request.clinicId = clinicIdFromHeader;
    }

    return true;
  }
}
