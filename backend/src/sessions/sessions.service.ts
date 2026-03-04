import { Injectable, NotFoundException, ForbiddenException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { whereNotDeleted } from '../prisma/soft-delete.helper';
import { CreateSessionDto } from './dto/create-session.dto';

@Injectable()
export class SessionsService {
  constructor(private prisma: PrismaService) {}

  private startOfMonth(): Date {
    const now = new Date();
    return new Date(now.getFullYear(), now.getMonth(), 1, 0, 0, 0, 0);
  }

  private startOfWeek(): Date {
    const d = new Date();
    const day = d.getDay();
    const diff = d.getDate() - day + (day === 0 ? -6 : 1);
    return new Date(d.getFullYear(), d.getMonth(), diff, 0, 0, 0, 0);
  }

  async getStats(userId: string, clinicId: string) {
    const clinicUser = await this.prisma.clinicUser.findUnique({
      where: { clinicId_userId: { clinicId, userId } },
      select: { canViewAllPatients: true, role: true },
    });
    if (!clinicUser) throw new NotFoundException('Clínica não encontrada ou sem acesso');

    const where: any = { patient: { clinicId, deletedAt: null } };
    if (!clinicUser.canViewAllPatients && !['owner', 'admin'].includes(clinicUser.role)) {
      where.userId = userId;
    }

    const monthStart = this.startOfMonth();
    const weekStart = this.startOfWeek();

    const [sessionsThisMonth, sessionsThisWeek] = await Promise.all([
      this.prisma.session.count({
        where: {
          ...whereNotDeleted('session', where),
          status: 'realizada',
          date: { gte: monthStart },
        },
      }),
      this.prisma.session.count({
        where: {
          ...whereNotDeleted('session', where),
          status: 'realizada',
          date: { gte: weekStart },
        },
      }),
    ]);

    return {
      sessionsThisMonth,
      sessionsThisWeek,
    };
  }

  async findAll(userId: string, clinicId: string) {
    const clinicUser = await this.prisma.clinicUser.findUnique({
      where: { clinicId_userId: { clinicId, userId } },
      select: { canViewAllPatients: true, role: true },
    });
    if (!clinicUser) throw new NotFoundException('Clínica não encontrada ou sem acesso');

    const where: any = { patient: { clinicId } };
    if (!clinicUser.canViewAllPatients && !['owner', 'admin'].includes(clinicUser.role)) {
      where.userId = userId;
    }

    return this.prisma.session.findMany({
      where: {
        ...whereNotDeleted('session', where),
        patient: where.patient ? { ...where.patient, deletedAt: null } : { deletedAt: null },
      },
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

  async findByPatient(patientId: string, userId: string, clinicId: string) {
    const patient = await this.prisma.patient.findFirst({
      where: whereNotDeleted('patient', { id: patientId, clinicId }),
    });

    if (!patient) {
      throw new NotFoundException('Paciente não encontrado ou não pertence à clínica');
    }

    return this.prisma.session.findMany({
      where: whereNotDeleted('session', { patientId }),
      orderBy: { date: 'desc' },
    });
  }

  async create(userId: string, createSessionDto: CreateSessionDto, clinicId: string) {
    const effectiveUserId = createSessionDto.professionalId || userId;
    if (createSessionDto.professionalId) {
      const membership = await this.prisma.clinicUser.findUnique({
        where: { clinicId_userId: { clinicId, userId: createSessionDto.professionalId } },
      });
      if (!membership || membership.status !== 'active') {
        throw new ForbiddenException('Profissional não encontrado na clínica');
      }
    }

    const patient = await this.prisma.patient.findFirst({
      where: whereNotDeleted('patient', { id: createSessionDto.patientId, clinicId }),
    });

    if (!patient) {
      throw new ForbiddenException('Paciente não encontrado ou não pertence à clínica');
    }

    const { professionalId: _, ...sessionData } = createSessionDto;
    return this.prisma.session.create({
      data: {
        ...sessionData,
        userId: effectiveUserId,
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

