import { Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class AppointmentsService {
  constructor(private prisma: PrismaService) {}

  async findAll(userId: string, clinicId?: string) {
    const where: any = { userId };

    // Se especificou clínica, incluir consultas da clínica
    if (clinicId) {
      // Verificar se tem acesso à clínica
      const clinicUser = await this.prisma.clinicUser.findUnique({
        where: {
          clinicId_userId: {
            clinicId: clinicId,
            userId: userId,
          },
        },
      });

      if (clinicUser && clinicUser.status === 'active') {
        // Se pode ver todos, mostrar todas as consultas da clínica
        if (clinicUser.canViewAllPatients || ['owner', 'admin'].includes(clinicUser.role)) {
          where.OR = [{ userId }, { clinicId: clinicId }];
        } else {
          // Senão, apenas próprias
          where.userId = userId;
        }
      }
    }

    return this.prisma.appointment.findMany({
      where,
      include: {
        patient: {
          select: {
            id: true,
            name: true,
          },
        },
        user: {
          select: {
            id: true,
            name: true,
          },
        },
      },
      orderBy: { scheduledAt: 'asc' },
    });
  }
}

