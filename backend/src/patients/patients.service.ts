import { Injectable, NotFoundException, ForbiddenException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { CreatePatientDto } from './dto/create-patient.dto';
import { UpdatePatientDto } from './dto/update-patient.dto';

@Injectable()
export class PatientsService {
  constructor(private prisma: PrismaService) {}

  async findAll(userId: string) {
    return this.prisma.patient.findMany({
      where: { userId },
      orderBy: { updatedAt: 'desc' },
    });
  }

  async findOne(id: string, userId: string) {
    const patient = await this.prisma.patient.findUnique({
      where: { id },
      include: {
        sessions: {
          orderBy: { date: 'desc' },
          take: 10,
        },
        payments: {
          orderBy: { date: 'desc' },
        },
      },
    });

    if (!patient) {
      throw new NotFoundException('Paciente não encontrado');
    }

    if (patient.userId !== userId) {
      throw new ForbiddenException('Acesso negado');
    }

    return patient;
  }

  async create(userId: string, createPatientDto: CreatePatientDto) {
    return this.prisma.patient.create({
      data: {
        ...createPatientDto,
        userId,
        source: createPatientDto.source || 'web',
      },
    });
  }

  async update(id: string, userId: string, updatePatientDto: UpdatePatientDto) {
    const patient = await this.findOne(id, userId);

    return this.prisma.patient.update({
      where: { id },
      data: {
        ...updatePatientDto,
        lastSyncedAt: new Date(),
      },
    });
  }
}

