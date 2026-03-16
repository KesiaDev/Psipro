import { BadRequestException, ForbiddenException, Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { whereNotDeleted } from '../prisma/soft-delete.helper';
import { SyncPaymentDto } from './dto/sync-payment.dto';
import { Decimal } from '@prisma/client/runtime/library';

@Injectable()
export class SyncPaymentsService {
  constructor(private prisma: PrismaService) {}

  async getPayments(clinicId: string, updatedAfter?: string) {
    const updatedAfterDate = updatedAfter ? new Date(updatedAfter) : undefined;
    if (updatedAfter && Number.isNaN(updatedAfterDate!.getTime())) {
      throw new BadRequestException('updatedAfter inválido');
    }

    const rows = await this.prisma.payment.findMany({
      where: whereNotDeleted('payment', {
        clinicId,
        ...(updatedAfterDate ? { updatedAt: { gt: updatedAfterDate } } : {}),
      }),
      orderBy: { date: 'desc' },
      select: {
        id: true,
        sessionId: true,
        amount: true,
        status: true,
        date: true,
        createdAt: true,
        updatedAt: true,
      },
    });
    return rows.map(({ date, ...r }) => ({ ...r, paidAt: date }));
  }

  async syncPayments(userId: string, clinicId: string, incoming: SyncPaymentDto[]) {
    await this.prisma.$transaction(async (tx) => {
      for (const p of incoming) {
        const incomingUpdatedAt = new Date(p.updatedAt);
        if (Number.isNaN(incomingUpdatedAt.getTime())) {
          throw new BadRequestException('updatedAt inválido em um ou mais pagamentos');
        }

        const session = await tx.session.findFirst({
          where: {
            id: p.sessionId,
            patient: { clinicId, deletedAt: null },
          },
          select: { id: true, userId: true, patientId: true },
        });

        if (!session) {
          throw new ForbiddenException(`Sessão ${p.sessionId} não encontrada ou não pertence à clínica`);
        }

        const existing = await tx.payment.findFirst({
          where: { id: p.id, clinicId },
        });

        const date = p.paidAt ? new Date(p.paidAt) : new Date();
        if (Number.isNaN(date.getTime())) {
          throw new BadRequestException('paidAt inválido em um ou mais pagamentos');
        }

        if (!existing) {
          await tx.payment.create({
            data: {
              id: p.id,
              userId: session.userId,
              patientId: session.patientId,
              clinicId,
              sessionId: p.sessionId,
              amount: new Decimal(p.amount),
              date,
              status: p.status ?? 'pendente',
              source: 'app',
              lastSyncedAt: new Date(),
            },
          });
          continue;
        }

        if (incomingUpdatedAt > existing.updatedAt) {
          await tx.payment.update({
            where: { id: existing.id },
            data: {
              amount: new Decimal(p.amount),
              status: p.status ?? existing.status,
              date: p.paidAt ? new Date(p.paidAt) : existing.date,
              lastSyncedAt: new Date(),
            },
          });
        }
      }
    });

    return this.getPayments(clinicId);
  }
}
