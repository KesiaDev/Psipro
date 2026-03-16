import { Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class DocumentsService {
  constructor(private prisma: PrismaService) {}

  async findAll(userId: string, clinicId: string, patientId?: string) {
    return this.prisma.document.findMany({
      where: {
        userId,
        patient: { clinicId },
        ...(patientId && { patientId }),
      },
      include: {
        patient: {
          select: {
            id: true,
            name: true,
          },
        },
      },
      orderBy: { createdAt: 'desc' },
    });
  }
}




