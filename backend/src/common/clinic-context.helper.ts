import { ForbiddenException, Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';

/**
 * Helper para validar acesso do usuário à clínica via ClinicUser.
 * Arquitetura multi-clinic: 1 usuário → várias clínicas (N:N).
 */
@Injectable()
export class ClinicContextHelper {
  constructor(private prisma: PrismaService) {}

  async getCurrentClinic(userId: string, clinicId: string) {
    const relation = await this.prisma.clinicUser.findUnique({
      where: {
        clinicId_userId: {
          userId,
          clinicId,
        },
      },
    });

    if (!relation || relation.status !== 'active') {
      throw new ForbiddenException('Usuário não pertence a esta clínica');
    }

    return relation;
  }
}
