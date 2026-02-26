import { Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class DocumentsService {
  constructor(private prisma: PrismaService) {}

  async findAll(
    userId: string,
    patientId?: string,
    clinicId?: string,
  ) {
    const where: { userId: string; patientId?: string; clinicId?: string } = {
      userId,
      ...(patientId && { patientId }),
    };
    if (clinicId) where.clinicId = clinicId;

    return this.prisma.document.findMany({
      where,
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




