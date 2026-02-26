import { Injectable, NotFoundException, ForbiddenException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { PatientAccessHelper } from '../common/helpers/patient-access.helper';
import { CreateSessionDto } from './dto/create-session.dto';

@Injectable()
export class SessionsService {
  constructor(
    private prisma: PrismaService,
    private patientAccess: PatientAccessHelper,
  ) {}

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
          where.patient = { clinicId };
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
    const hasAccess = await this.patientAccess.hasAccessToPatient(
      patientId,
      userId,
    );
    if (!hasAccess) {
      throw new NotFoundException('Paciente não encontrado');
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

  async create(
    userId: string,
    clinicId: string | undefined,
    createSessionDto: CreateSessionDto,
  ) {
    const hasAccess = await this.patientAccess.hasAccessToPatient(
      createSessionDto.patientId,
      userId,
    );
    if (!hasAccess) {
      throw new ForbiddenException('Paciente não encontrado ou acesso negado');
    }

    const patient = await this.prisma.patient.findUnique({
      where: { id: createSessionDto.patientId },
      select: { clinicId: true },
    });
    const effectiveClinicId = clinicId ?? patient?.clinicId ?? undefined;

    return this.prisma.session.create({
      data: {
        ...createSessionDto,
        userId,
        clinicId: effectiveClinicId,
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

