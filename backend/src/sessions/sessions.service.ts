import { Injectable, NotFoundException, ForbiddenException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { VoiceService, extractAnamnesisText } from '../voice/voice.service';
import { whereNotDeleted } from '../prisma/soft-delete.helper';
import { CreateSessionDto } from './dto/create-session.dto';
import { UpdateSessionDto } from './dto/update-session.dto';

@Injectable()
export class SessionsService {
  constructor(
    private prisma: PrismaService,
    private voiceService: VoiceService,
  ) {}

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

  async findOne(id: string, userId: string, clinicId: string) {
    const clinicUser = await this.prisma.clinicUser.findUnique({
      where: { clinicId_userId: { clinicId, userId } },
      select: { canViewAllPatients: true, role: true },
    });
    if (!clinicUser) throw new NotFoundException('Clínica não encontrada ou sem acesso');

    const where: any = { id, patient: { clinicId } };
    if (!clinicUser.canViewAllPatients && !['owner', 'admin'].includes(clinicUser.role)) {
      where.userId = userId;
    }

    const session = await this.prisma.session.findFirst({
      where: whereNotDeleted('session', where),
      include: {
        patient: {
          select: {
            id: true,
            name: true,
            clinicId: true,
          },
        },
      },
    });
    if (!session) {
      throw new NotFoundException('Sessão não encontrada');
    }
    return session;
  }

  async update(
    id: string,
    userId: string,
    clinicId: string,
    updateSessionDto: UpdateSessionDto,
  ) {
    const session = await this.findOne(id, userId, clinicId);
    const effectiveUserId = updateSessionDto.professionalId || session.userId;
    if (updateSessionDto.professionalId) {
      const membership = await this.prisma.clinicUser.findUnique({
        where: { clinicId_userId: { clinicId, userId: updateSessionDto.professionalId } },
      });
      if (!membership || membership.status !== 'active') {
        throw new ForbiddenException('Profissional não encontrado na clínica');
      }
    }

    const data: any = {};
    if (updateSessionDto.patientId !== undefined) {
      const patient = await this.prisma.patient.findFirst({
        where: whereNotDeleted('patient', { id: updateSessionDto.patientId, clinicId }),
      });
      if (!patient) {
        throw new ForbiddenException('Paciente não encontrado ou não pertence à clínica');
      }
      data.patientId = updateSessionDto.patientId;
    }
    if (updateSessionDto.date !== undefined) data.date = new Date(updateSessionDto.date);
    if (updateSessionDto.duration !== undefined) data.duration = updateSessionDto.duration;
    if (updateSessionDto.notes !== undefined) data.notes = updateSessionDto.notes;
    if (updateSessionDto.professionalId !== undefined) data.userId = effectiveUserId;

    return this.prisma.session.update({
      where: { id },
      data,
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

  async updateVoiceNote(
    sessionId: string,
    transcript: string,
    userId: string,
    clinicId: string,
  ) {
    const session = await this.prisma.session.findFirst({
      where: whereNotDeleted('session', { id: sessionId }),
      include: {
        patient: {
          select: {
            clinicId: true,
            anamnesis: true,
            observations: true,
          },
        },
      },
    });
    if (!session) {
      throw new NotFoundException('Sessão não encontrada');
    }
    const patientClinicId = session.patient?.clinicId;
    if (patientClinicId && patientClinicId !== clinicId) {
      throw new ForbiddenException('Sessão não pertence à clínica');
    }

    const transcriptText = transcript.trim();
    const anamnesisText = extractAnamnesisText(session.patient?.anamnesis);
    const observations = session.patient?.observations?.trim() || '';

    try {
      const insights = await this.voiceService.generateInsights(transcriptText, {
        sessionNotes: session.notes || undefined,
        patientContext:
          anamnesisText || observations
            ? { anamnesis: anamnesisText, observations }
            : undefined,
      });
      return this.prisma.session.update({
        where: { id: sessionId },
        data: {
          transcript: transcriptText,
          summary: insights.summary || null,
          themes: insights.themes.length ? insights.themes : null,
          emotions: insights.emotions.length ? insights.emotions : null,
          actionItems: insights.actionItems.length ? insights.actionItems : null,
          riskFlags: insights.riskFlags.length ? insights.riskFlags : null,
        },
      });
    } catch (err) {
      // Salva transcript mesmo se insights falharem (ex: sem OPENAI_API_KEY)
      console.warn('[Sessions] Insights failed for session', sessionId, err);
      return this.prisma.session.update({
        where: { id: sessionId },
        data: { transcript: transcriptText },
      });
    }
  }

  async delete(id: string, clinicId: string) {
    const session = await this.prisma.session.findFirst({
      where: whereNotDeleted('session', { id }),
      include: { patient: { select: { clinicId: true } } },
    });
    if (!session) {
      throw new NotFoundException('Sessão não encontrada');
    }
    const patientClinicId = session.patient?.clinicId;
    if (patientClinicId && patientClinicId !== clinicId) {
      throw new NotFoundException('Sessão não encontrada');
    }

    await this.prisma.session.update({
      where: { id },
      data: { deletedAt: new Date() },
    });
    return { success: true };
  }
}

