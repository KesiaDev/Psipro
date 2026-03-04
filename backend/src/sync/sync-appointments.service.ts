import { BadRequestException, ForbiddenException, Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { whereNotDeleted } from '../prisma/soft-delete.helper';
import { SyncAppointmentDto } from './dto/sync-appointment.dto';

@Injectable()
export class SyncAppointmentsService {
  constructor(private prisma: PrismaService) {}

  /**
   * GET - Retorna agendamentos da clínica para sincronização.
   * Filtra por clinicId (header) e updatedAfter (query).
   */
  async getAppointments(clinicId: string, updatedAfter?: string) {
    const updatedAfterDate = updatedAfter ? new Date(updatedAfter) : undefined;
    if (updatedAfter && Number.isNaN(updatedAfterDate!.getTime())) {
      throw new BadRequestException('updatedAfter inválido');
    }

    const rows = await this.prisma.appointment.findMany({
      where: whereNotDeleted('appointment', {
        clinicId,
        ...(updatedAfterDate ? { updatedAt: { gt: updatedAfterDate } } : {}),
      }),
      orderBy: { scheduledAt: 'asc' },
      select: {
        id: true,
        clinicId: true,
        userId: true,
        patientId: true,
        scheduledAt: true,
        duration: true,
        type: true,
        notes: true,
        status: true,
        createdAt: true,
        updatedAt: true,
      },
    });
    return rows.map(({ userId, ...r }) => ({ ...r, professionalId: userId }));
  }

  /**
   * POST - Recebe lista do app e resolve conflitos no backend.
   * Backend é source of truth; registros com updatedAt mais recente vencem.
   * Retorna lista atualizada da clínica após sync.
   */
  async syncAppointments(userId: string, clinicId: string, incoming: SyncAppointmentDto[]) {
    const results = await this.prisma.$transaction(async (tx) => {
      const processed: Array<{
        id: string;
        clinicId: string | null;
        userId: string;
        patientId: string;
        scheduledAt: Date;
        duration: number;
        type: string | null;
        notes: string | null;
        status: string;
        createdAt: Date;
        updatedAt: Date;
      }> = [];

      for (const a of incoming) {
        const incomingUpdatedAt = new Date(a.updatedAt);
        if (Number.isNaN(incomingUpdatedAt.getTime())) {
          throw new BadRequestException('updatedAt inválido em um ou mais agendamentos');
        }

        // Validar que patient e professional pertencem à clínica
        const [patient, membership] = await Promise.all([
          tx.patient.findFirst({
            where: whereNotDeleted('patient', { id: a.patientId, clinicId }),
          }),
          tx.clinicUser.findUnique({
            where: { clinicId_userId: { clinicId, userId: a.professionalId } },
            select: { status: true },
          }),
        ]);

        if (!patient) {
          throw new ForbiddenException(`Paciente ${a.patientId} não encontrado ou não pertence à clínica`);
        }
        if (!membership || membership.status !== 'active') {
          throw new ForbiddenException(`Profissional ${a.professionalId} não encontrado na clínica`);
        }

        const existing = await tx.appointment.findFirst({
          where: { id: a.id, clinicId },
        });

        const scheduledAt = new Date(a.scheduledAt);
        if (Number.isNaN(scheduledAt.getTime())) {
          throw new BadRequestException('scheduledAt inválido em um ou mais agendamentos');
        }

        if (!existing) {
          const created = await tx.appointment.create({
            data: {
              id: a.id,
              clinicId,
              userId: a.professionalId,
              patientId: a.patientId,
              scheduledAt,
              duration: a.duration,
              type: a.type ?? null,
              notes: a.notes ?? null,
              status: a.status ?? 'agendada',
              source: 'app',
              lastSyncedAt: new Date(),
            },
          });
          processed.push({
            id: created.id,
            clinicId: created.clinicId,
            userId: created.userId,
            patientId: created.patientId,
            scheduledAt: created.scheduledAt,
            duration: created.duration,
            type: created.type,
            notes: created.notes,
            status: created.status,
            createdAt: created.createdAt,
            updatedAt: created.updatedAt,
          });
          continue;
        }

        // Conflito: backend decide; updatedAt mais recente vence
        if (incomingUpdatedAt > existing.updatedAt) {
          const updated = await tx.appointment.update({
            where: { id: existing.id },
            data: {
              userId: a.professionalId,
              patientId: a.patientId,
              scheduledAt,
              duration: a.duration,
              type: a.type ?? existing.type,
              notes: a.notes ?? existing.notes,
              status: a.status ?? existing.status,
              lastSyncedAt: new Date(),
            },
          });
          processed.push({
            id: updated.id,
            clinicId: updated.clinicId,
            userId: updated.userId,
            patientId: updated.patientId,
            scheduledAt: updated.scheduledAt,
            duration: updated.duration,
            type: updated.type,
            notes: updated.notes,
            status: updated.status,
            createdAt: updated.createdAt,
            updatedAt: updated.updatedAt,
          });
        } else {
          processed.push({
            id: existing.id,
            clinicId: existing.clinicId,
            userId: existing.userId,
            patientId: existing.patientId,
            scheduledAt: existing.scheduledAt,
            duration: existing.duration,
            type: existing.type,
            notes: existing.notes,
            status: existing.status,
            createdAt: existing.createdAt,
            updatedAt: existing.updatedAt,
          });
        }
      }

      return processed;
    });

    // Retornar lista atualizada da clínica após sync (incluindo os processados + demais do backend)
    return this.getAppointments(clinicId);
  }
}
