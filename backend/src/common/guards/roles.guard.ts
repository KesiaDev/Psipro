import {
  CanActivate,
  ExecutionContext,
  Injectable,
  ForbiddenException,
} from '@nestjs/common';
import { Reflector } from '@nestjs/core';
import { PrismaService } from '../../prisma/prisma.service';
import { ROLES_KEY } from '../decorators/roles.decorator';

/**
 * Guard de RBAC. Valida role via ClinicUser.role.
 * Deve ser usado após JwtAuthGuard. Para endpoints com clinicId, ClinicGuard deve rodar antes.
 */
@Injectable()
export class RolesGuard implements CanActivate {
  constructor(
    private reflector: Reflector,
    private prisma: PrismaService,
  ) {}

  async canActivate(context: ExecutionContext): Promise<boolean> {
    const requiredRoles = this.reflector.getAllAndOverride<string[]>(ROLES_KEY, [
      context.getHandler(),
      context.getClass(),
    ]);

    if (!requiredRoles?.length) {
      return true;
    }

    const request = context.switchToHttp().getRequest();
    const userId = request?.user?.id ?? request?.user?.sub;
    let clinicId = request?.clinicId ?? request?.user?.clinicId;

    if (!clinicId && request?.params?.id) {
      clinicId = request.params.id;
    }

    const effectiveRole = await this.getEffectiveRole(userId, clinicId);

    const hasRole = requiredRoles.some((r) => this.roleIncludes(effectiveRole, r));
    if (!hasRole) {
      throw new ForbiddenException('Sem permissão para este recurso');
    }

    return true;
  }

  private async getEffectiveRole(userId: string, clinicId: string | null): Promise<string> {
    if (clinicId) {
      const clinicUser = await this.prisma.clinicUser.findUnique({
        where: {
          clinicId_userId: { clinicId, userId },
        },
        select: { role: true, status: true },
      });
      if (clinicUser?.status === 'active') {
        return clinicUser.role;
      }
    }

    const user = await this.prisma.user.findUnique({
      where: { id: userId },
      select: { clinicId: true, role: true },
    });

    if (!user) return 'assistant';
    if (user.clinicId && user.role === 'OWNER') return 'owner';
    if (user.clinicId && user.role === 'PSYCHOLOGIST') return 'psychologist';
    if (user.role === 'ASSISTANT') return 'assistant';

    return 'assistant';
  }

  private roleIncludes(userRole: string, requiredRole: string): boolean {
    const hierarchy = ['assistant', 'psychologist', 'admin', 'owner'];
    const roleNorm = userRole.toLowerCase();
    const reqNorm = requiredRole.toLowerCase();

    if (reqNorm === 'admin') {
      return ['owner', 'admin'].includes(roleNorm);
    }
    if (reqNorm === 'psychologist') {
      return ['owner', 'admin', 'psychologist'].includes(roleNorm);
    }
    if (reqNorm === 'assistant') {
      return true;
    }
    return roleNorm === reqNorm;
  }
}
