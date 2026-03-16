import { BadRequestException, ForbiddenException, Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { whereNotDeleted } from '../prisma/soft-delete.helper';
import { SyncSessionDto } from './dto/sync-session.dto';

@Injectable()
export class SyncSessionsService {
  constructor(private prisma: PrismaService) {}

  async getSessions(clinicId: string, updatedAfter?: string) {
    const updatedAfterDate = updatedAfter ? new Date(updatedAfter) : undefined;
    if (updatedAfter && Number.isNaN(updatedAfterDate!.getTime())) {
      throw new BadRequestException('updatedAfter inválido');
    }

    const rows = await this.prisma.session.findMany({
      where: {
        ...whereNotDeleted('session', {
          patient: { clinicId, deletedAt: null },
          ...(updatedAfterDate ? { updatedAt: { gt: updatedAfterDate } } : {}),
        }),
      },
      orderBy: { date: 'asc' },
      select: {
        id: true,
        userId: true,
        patientId: true,
        date: true,
        duration: true,
        notes: true,
        status: true,
        createdAt: true,
        updatedAt: true,
      },
    });
    return rows.map(({ userId, ...r }) => ({ ...r, professionalId: userId }));
  }

  async syncSessions(userId: string, clinicId: string, incoming: SyncSessionDto[]) {
    await this.prisma.$transaction(async (tx) => {
      for (const s of incoming) {
        const incomingUpdatedAt = new Date(s.updatedAt);
        if (Number.isNaN(incomingUpdatedAt.getTime())) {
          throw new BadRequestException('updatedAt inválido em uma ou mais sessões');
        }

        const [patient, membership] = await Promise.all([
          tx.patient.findFirst({
            where: whereNotDeleted('patient', { id: s.patientId, clinicId }),
          }),
          tx.clinicUser.findUnique({
            where: { clinicId_userId: { clinicId, userId: s.professionalId } },
            select: { status: true },
          }),
        ]);

        if (!patient) {
          throw new ForbiddenException(`Paciente ${s.patientId} não encontrado ou não pertence à clínica`);
        }
        if (!membership || membership.status !== 'active') {
          throw new ForbiddenException(`Profissional ${s.professionalId} não encontrado na clínica`);
        }

        const existing = await tx.session.findFirst({
          where: { id: s.id, patient: { clinicId } },
        });

        const date = new Date(s.date);
        if (Number.isNaN(date.getTime())) {
          throw new BadRequestException('date inválido em uma ou mais sessões');
        }

        if (!existing) {
          await tx.session.create({
            data: {
              id: s.id,
              userId: s.professionalId,
              patientId: s.patientId,
              date,
              duration: s.duration,
              notes: s.notes ?? null,
              status: s.status ?? 'realizada',
              source: 'app',
              lastSyncedAt: new Date(),
            },
          });
          continue;
        }

        if (incomingUpdatedAt > existing.updatedAt) {
          await tx.session.update({
            where: { id: existing.id },
            data: {
              userId: s.professionalId,
              patientId: s.patientId,
              date,
              duration: s.duration,
              notes: s.notes ?? existing.notes,
              status: s.status ?? existing.status,
              lastSyncedAt: new Date(),
            },
          });
        }
      }
    });

    return this.getSessions(clinicId);
  }
}
