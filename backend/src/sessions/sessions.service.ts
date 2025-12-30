import { Injectable, NotFoundException, ForbiddenException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { CreateSessionDto } from './dto/create-session.dto';

@Injectable()
export class SessionsService {
  constructor(private prisma: PrismaService) {}

  async findAll(userId: string, clinicId?: string) {
    const where: any = { userId };

    if (clinicId) {
      const clinicUser = await this.prisma.clinicUser.findUnique({
        where: {
          clinicId_userId: {
            clinicId: clinicId,
            userId: userId,
          },
        },
      });

      if (clinicUser && clinicUser.status === 'active') {
        if (clinicUser.canViewAllPatients || ['owner', 'admin'].includes(clinicUser.role)) {
          where.patient = { clinicId: clinicId };
        }
      }
    }

    return this.prisma.session.findMany({
      where,
      include: {
        patient: {
          select: {
            id: true,
            name: true,
          },
        },
      },
      orderBy: { date: 'desc' },
    });
  }

  async findByPatient(patientId: string, userId: string) {
    // Verificar acesso ao paciente (próprio, clínica ou compartilhado)
    const patient = await this.prisma.patient.findUnique({
      where: { id: patientId },
    });

    if (!patient) {
      throw new NotFoundException('Paciente não encontrado');
    }

    // Verificar se tem acesso
    const hasAccess =
      patient.userId === userId ||
      (patient.clinicId &&
        (await this.hasClinicAccess(patient.clinicId, userId))) ||
      (patient.sharedWith && patient.sharedWith.includes(userId));

    if (!hasAccess) {
      throw new ForbiddenException('Acesso negado');
    }

    return this.prisma.session.findMany({
      where: {
        patientId,
      },
      orderBy: { date: 'desc' },
    });
  }

  private async hasClinicAccess(clinicId: string, userId: string): Promise<boolean> {
    const clinicUser = await this.prisma.clinicUser.findUnique({
      where: {
        clinicId_userId: {
          clinicId: clinicId,
          userId: userId,
        },
      },
    });
    return clinicUser?.status === 'active' && clinicUser.canViewAllPatients;
  }

  async create(userId: string, createSessionDto: CreateSessionDto) {
    // Verificar se o paciente pertence ao usuário
    const patient = await this.prisma.patient.findUnique({
      where: { id: createSessionDto.patientId },
    });

    if (!patient || patient.userId !== userId) {
      throw new ForbiddenException('Paciente não encontrado ou acesso negado');
    }

    return this.prisma.session.create({
      data: {
        ...createSessionDto,
        userId,
        date: new Date(createSessionDto.date),
        source: createSessionDto.source || 'app',
      },
      include: {
        patient: {
          select: {
            id: true,
            name: true,
          },
        },
      },
    });
  }
}

