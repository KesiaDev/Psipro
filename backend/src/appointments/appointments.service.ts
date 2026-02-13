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

    const appointments = await this.prisma.appointment.findMany({
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
        session: {
          include: {
            payment: {
              select: {
                status: true,
                date: true,
              },
            },
          },
        },
      },
      orderBy: { scheduledAt: 'asc' },
    });

    /**
     * Contrato mínimo (compatível) para Android/Web:
     * - startsAt/endsAt derivados de scheduledAt/duration
     * - paymentStatus derivado do pagamento (quando existir)
     *
     * Importante: não removemos campos existentes para não quebrar clientes antigos.
     */
    return appointments.map((a) => {
      const startsAt = a.scheduledAt;
      const endsAt = new Date(a.scheduledAt.getTime() + (a.duration ?? 60) * 60 * 1000);
      const paymentStatus = a.session?.payment?.status ?? 'pendente';

      return {
        ...a,
        startsAt,
        endsAt,
        paymentStatus,
      };
    });
  }
}

