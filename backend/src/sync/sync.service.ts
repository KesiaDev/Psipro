import { BadRequestException, ForbiddenException, Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { SyncPatientsQueryDto } from './dto/sync-patients-query.dto';
import { SyncPatientDto } from './dto/sync-patient.dto';

type PatientOrigin = 'ANDROID' | 'WEB';

@Injectable()
export class SyncService {
  constructor(private prisma: PrismaService) {}

  private async resolveClinicId(userId: string, requestedClinicId?: string) {
    if (requestedClinicId) {
      const membership = await this.prisma.clinicUser.findUnique({
        where: {
          clinicId_userId: {
            clinicId: requestedClinicId,
            userId,
          },
        },
        select: { clinicId: true, status: true },
      });

      if (!membership || membership.status !== 'active') {
        throw new ForbiddenException('Sem acesso a esta clínica');
      }

      return requestedClinicId;
    }

    const first = await this.prisma.clinicUser.findFirst({
      where: { userId, status: 'active' },
      select: { clinicId: true, joinedAt: true },
      orderBy: { joinedAt: 'asc' },
    });

    if (!first?.clinicId) {
      throw new ForbiddenException('Usuário não possui clínica ativa');
    }

    return first.clinicId;
  }

  async getPatients(
    userId: string,
    clinicIdFromHeader: string,
    query: SyncPatientsQueryDto,
  ) {
    const clinicId = clinicIdFromHeader;
    const updatedAfter = query.updatedAfter ? new Date(query.updatedAfter) : undefined;

    return this.prisma.patient.findMany({
      where: {
        clinicId,
        ...(updatedAfter ? { updatedAt: { gt: updatedAfter } } : {}),
      },
      orderBy: { createdAt: 'desc' },
    });
  }

  async syncPatients(userId: string, clinicId: string, incoming: SyncPatientDto[]) {

    // Processamento transacional: backend decide conflitos e retorna estado persistido.
    return this.prisma.$transaction(async (tx) => {
      const results = [];

      for (const p of incoming) {
        // Regra do endpoint: sync de pacientes da clínica.
        if (!p.clinicId || p.clinicId !== clinicId) {
          throw new ForbiddenException('clinicId inválido para este usuário');
        }

        const incomingUpdatedAt = new Date(p.updatedAt);
        if (Number.isNaN(incomingUpdatedAt.getTime())) {
          throw new BadRequestException('updatedAt inválido');
        }

        const origin: PatientOrigin =
          p.origin ?? (p.source === 'app' ? 'ANDROID' : p.source === 'web' ? 'WEB' : 'ANDROID');
        const source = p.source ?? (origin === 'ANDROID' ? 'app' : 'web');

        if (!p.id) {
          const created = await tx.patient.create({
            data: {
              clinicId,
              clinicOwnerId: userId,
              name: p.name,
              birthDate: p.birthDate ? new Date(p.birthDate) : undefined,
              cpf: p.cpf,
              phone: p.phone,
              email: p.email,
              address: p.address,
              emergencyContact: p.emergencyContact,
              observations: p.observations,
              status: p.status ?? 'Ativo',
              type: p.type,
              sharedWith: p.sharedWith ?? [],
              source,
              origin,
              lastSyncedAt: new Date(),
            },
          });
          results.push(created);
          continue;
        }

        const existing = await tx.patient.findUnique({
          where: { id: p.id },
        });

        if (!existing) {
          // `id` já deve ser global (gerado pelo backend em um sync anterior).
          const created = await tx.patient.create({
            data: {
              id: p.id,
              clinicId,
              clinicOwnerId: userId,
              name: p.name,
              birthDate: p.birthDate ? new Date(p.birthDate) : undefined,
              cpf: p.cpf,
              phone: p.phone,
              email: p.email,
              address: p.address,
              emergencyContact: p.emergencyContact,
              observations: p.observations,
              status: p.status ?? 'Ativo',
              type: p.type,
              sharedWith: p.sharedWith ?? [],
              source,
              origin,
              lastSyncedAt: new Date(),
            },
          });
          results.push(created);
          continue;
        }

        // Conflito: backend decide (compara `updatedAt` do cliente x persistido).
        // Se o cliente estiver mais recente, aplicamos update (updatedAt vira "agora" no servidor).
        if (incomingUpdatedAt > existing.updatedAt) {
          const updated = await tx.patient.update({
            where: { id: existing.id },
            data: {
              // Não mudamos clinicId automaticamente para evitar “movimentação” silenciosa.
              name: p.name ?? existing.name,
              birthDate: p.birthDate ? new Date(p.birthDate) : undefined,
              cpf: p.cpf,
              phone: p.phone,
              email: p.email,
              address: p.address,
              emergencyContact: p.emergencyContact,
              observations: p.observations,
              status: p.status,
              type: p.type,
              sharedWith: p.sharedWith,
              source,
              origin,
              lastSyncedAt: new Date(),
            },
          });
          results.push(updated);
        } else {
          // Mantém registro do backend (estado final persistido).
          results.push(existing);
        }
      }

      return results;
    });
  }
}

