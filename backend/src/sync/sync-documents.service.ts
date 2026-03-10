import { BadRequestException, ForbiddenException, Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { whereNotDeleted } from '../prisma/soft-delete.helper';
import { SyncDocumentDto } from './dto/sync-document.dto';

@Injectable()
export class SyncDocumentsService {
  constructor(private prisma: PrismaService) {}

  /**
   * GET - Retorna documentos da clínica para sincronização.
   * Filtra por clinicId (header), patientId e updatedAfter (query).
   */
  async getDocuments(
    userId: string,
    clinicId: string,
    patientId?: string,
    updatedAfter?: string,
  ) {
    const updatedAfterDate = updatedAfter ? new Date(updatedAfter) : undefined;
    if (updatedAfter && updatedAfterDate && Number.isNaN(updatedAfterDate.getTime())) {
      throw new BadRequestException('updatedAfter inválido');
    }

    const patientWhere = whereNotDeleted('patient', { clinicId }) as Record<string, unknown>;
    const docs = await this.prisma.document.findMany({
      where: {
        userId,
        patient: { ...patientWhere, deletedAt: null },
        ...(patientId && { patientId }),
        ...(updatedAfterDate && { updatedAt: { gt: updatedAfterDate } }),
      },
      orderBy: { updatedAt: 'desc' },
      select: {
        id: true,
        userId: true,
        patientId: true,
        name: true,
        type: true,
        fileUrl: true,
        content: true,
        status: true,
        source: true,
        syncHash: true,
        lastSyncedAt: true,
        createdAt: true,
        updatedAt: true,
      },
    });
    return docs;
  }

  /**
   * POST - Recebe lista do app e resolve conflitos no backend.
   * Cria ou atualiza documentos. patientId deve ser UUID do backend.
   */
  async syncDocuments(
    userId: string,
    clinicId: string,
    incoming: SyncDocumentDto[],
  ) {
    return this.prisma.$transaction(async (tx) => {
      const results = [];

      for (const d of incoming) {
        const incomingUpdatedAt = new Date(d.updatedAt);
        if (Number.isNaN(incomingUpdatedAt.getTime())) {
          throw new BadRequestException('updatedAt inválido em um ou mais documentos');
        }

        if (!d.patientId) {
          throw new BadRequestException('patientId obrigatório em cada documento');
        }

        const patient = await tx.patient.findFirst({
          where: whereNotDeleted('patient', { id: d.patientId, clinicId }),
        });

        if (!patient) {
          throw new ForbiddenException(`Paciente ${d.patientId} não encontrado ou não pertence à clínica`);
        }

        const contentJson = d.content
          ? (typeof d.content === 'object' ? d.content : JSON.parse(String(d.content)))
          : null;

        if (!d.id) {
          const created = await tx.document.create({
            data: {
              userId,
              patientId: d.patientId,
              name: d.name,
              type: d.type,
              fileUrl: d.fileUrl ?? null,
              content: contentJson,
              status: d.status ?? 'Ativo',
              source: d.source ?? 'app',
              lastSyncedAt: new Date(),
            },
          });
          results.push(created);
          continue;
        }

        const existing = await tx.document.findFirst({
          where: { id: d.id, userId },
        });

        if (!existing) {
          const created = await tx.document.create({
            data: {
              id: d.id,
              userId,
              patientId: d.patientId,
              name: d.name,
              type: d.type,
              fileUrl: d.fileUrl ?? null,
              content: contentJson,
              status: d.status ?? 'Ativo',
              source: d.source ?? 'app',
              lastSyncedAt: new Date(),
            },
          });
          results.push(created);
          continue;
        }

        if (incomingUpdatedAt > existing.updatedAt) {
          const updated = await tx.document.update({
            where: { id: existing.id },
            data: {
              name: d.name ?? existing.name,
              type: d.type ?? existing.type,
              fileUrl: d.fileUrl ?? existing.fileUrl,
              content: contentJson ?? existing.content,
              status: d.status ?? existing.status,
              source: d.source ?? existing.source,
              lastSyncedAt: new Date(),
            },
          });
          results.push(updated);
        } else {
          results.push(existing);
        }
      }

      return results;
    });
  }
}
