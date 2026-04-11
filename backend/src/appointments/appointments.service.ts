import { ConflictException, Injectable, NotFoundException, Inject, forwardRef } from '@nestjs/common';
import { Prisma } from '@prisma/client';
import { AuditService } from '../audit/audit.service';
import { PrismaService } from '../prisma/prisma.service';
import { GoogleCalendarService } from '../integrations/google-calendar/google-calendar.service';
import { WhatsAppService } from '../integrations/whatsapp/whatsapp.service';
import { whereNotDeleted } from '../prisma/soft-delete.helper';

/** Código de erro Prisma para violação de unique constraint */
const PRISMA_UNIQUE_VIOLATION = 'P2002';
import { CreateAppointmentDto } from './dto/create-appointment.dto';
import { UpdateAppointmentDto } from './dto/update-appointment.dto';

/** Status considerados como "realizada" (aceita tanto backend quanto Android) */
const STATUS_REALIZADA = ['realizada', 'REALIZADO'];

/** Status considerados como cancelamento */
const STATUS_CANCELADO = ['cancelada', 'cancelado', 'CANCELADO', 'CANCELADA'];

@Injectable()
export class AppointmentsService {
  constructor(
    private prisma: PrismaService,
    private auditService: AuditService,
    private googleCalendar: GoogleCalendarService,
    @Inject(forwardRef(() => WhatsAppService)) private whatsApp: WhatsAppService,
  ) {}

  private async checkScheduleConflict(
    userId: string,
    clinicId: string,
    scheduledAt: Date,
    duration: number,
    excludeId?: string,
  ): Promise<void> {
    const appointments = await this.prisma.appointment.findMany({
      where: whereNotDeleted('appointment', {
        userId,
        clinicId,
        status: { notIn: ['cancelada', 'cancelado'] },
        ...(excludeId && { id: { not: excludeId } }),
      }),
      select: { id: true, scheduledAt: true, duration: true },
    });

    const newStart = scheduledAt.getTime();
    const newEnd = newStart + duration * 60 * 1000;

    const hasConflict = appointments.some((a) => {
      const aStart = a.scheduledAt.getTime();
      const aEnd = aStart + (a.duration ?? 60) * 60 * 1000;
      return aStart < newEnd && aEnd > newStart;
    });

    if (hasConflict) {
      throw new ConflictException(
        'Horário conflitante: já existe consulta agendada no mesmo período para este profissional.',
      );
    }
  }

  async create(userId: string, dto: CreateAppointmentDto, clinicId: string) {
    const effectiveUserId = dto.professionalId || userId;
    if (dto.professionalId) {
      const membership = await this.prisma.clinicUser.findUnique({
        where: { clinicId_userId: { clinicId, userId: dto.professionalId } },
      });
      if (!membership || membership.status !== 'active') {
        throw new NotFoundException('Profissional não encontrado na clínica');
      }
    }

    const patient = await this.prisma.patient.findFirst({
      where: whereNotDeleted('patient', { id: dto.patientId, clinicId }),
    });
    if (!patient) throw new NotFoundException('Paciente não encontrado ou não pertence à clínica');

    const scheduledAt = new Date(dto.scheduledAt);
    const duration = dto.duration ?? 60;

    await this.checkScheduleConflict(effectiveUserId, clinicId, scheduledAt, duration);

    const created = await this.prisma.appointment.create({
      data: {
        userId: effectiveUserId,
        patientId: dto.patientId,
        clinicId,
        scheduledAt,
        duration,
        type: dto.type,
        status: dto.status ?? 'agendada',
        notes: dto.notes,
      },
      include: {
        patient: { select: { id: true, name: true } },
        user: { select: { id: true, name: true } },
      },
    });

    this.auditService.log({
      userId: effectiveUserId,
      clinicId,
      action: 'appointment_creation',
      entity: 'Appointment',
      entityId: created.id,
      metadata: { patientId: dto.patientId, scheduledAt: dto.scheduledAt },
    }).catch(() => {});

    if (this.googleCalendar.isConfigured()) {
      this.googleCalendar
        .syncAppointmentToGoogle(
          effectiveUserId,
          clinicId,
          {
            id: created.id,
            patientId: created.patientId,
            scheduledAt: created.scheduledAt,
            duration: created.duration ?? 60,
            type: created.type,
            notes: created.notes,
            patientName: created.patient?.name,
          },
          null,
        )
        .catch(() => {});
    }

    // Enviar confirmação WhatsApp ao paciente (se integração estiver ativa e paciente tiver telefone)
    if (patient.phone) {
      const therapistName = (created.user as any)?.name ?? 'Terapeuta';
      this.whatsApp
        .sendConfirmationToPatient(
          effectiveUserId,
          clinicId,
          { name: patient.name, phone: patient.phone },
          { scheduledAt: created.scheduledAt, therapistName },
        )
        .catch(() => {});
    }

    return created;
  }

  async update(id: string, userId: string, dto: UpdateAppointmentDto, clinicId: string) {
    const existing = await this.prisma.appointment.findFirst({
      where: whereNotDeleted('appointment', { id, userId, clinicId }),
      include: { session: { include: { payment: true } } },
    });
    if (!existing) {
      throw new NotFoundException('Agendamento não encontrado');
    }

    const scheduledAt = dto.scheduledAt ? new Date(dto.scheduledAt) : existing.scheduledAt;
    const duration = dto.duration ?? existing.duration ?? 60;

    await this.checkScheduleConflict(userId, clinicId, scheduledAt, duration, id);

    const newStatus = dto.status;
    const wasRealizada = STATUS_REALIZADA.includes(existing.status);
    const becomesRealizada = newStatus && STATUS_REALIZADA.includes(newStatus);
    const becomesCancelado = newStatus && STATUS_CANCELADO.includes(newStatus);

    const updated = await this.prisma.$transaction(async (tx) => {
      const updatedApt = await tx.appointment.update({
        where: { id },
        data: {
          ...(dto.scheduledAt && { scheduledAt }),
          ...(dto.duration != null && { duration: dto.duration }),
          ...(dto.type !== undefined && { type: dto.type }),
          ...(dto.status !== undefined && { status: dto.status }),
          ...(dto.notes !== undefined && { notes: dto.notes }),
        },
        include: {
          patient: { select: { id: true, name: true } },
          user: { select: { id: true, name: true } },
        },
      });

      // B2: status → REALIZADO: criar Session + Payment (cobrança automática)
      // Idempotência: se já existir Session/Payment para este appointment → não criar nova cobrança
      let paymentCreated = false;
      let paymentCancelled = false;
      let cancelledPaymentId: string | null = null;

      if (newStatus && becomesRealizada && !wasRealizada) {
        const existingSessionForAppointment = await tx.session.findFirst({
          where: { appointmentId: updatedApt.id, deletedAt: null },
          include: { payment: true },
        });
        if (existingSessionForAppointment) {
          return { updated: updatedApt, paymentCreated: false, paymentCancelled: false, cancelledPaymentId: null };
        }

        const sessionDate = updatedApt.scheduledAt;
        const dueDate = new Date(sessionDate);
        dueDate.setDate(dueDate.getDate() + 7);

        const session = await tx.session.create({
          data: {
            userId: updatedApt.userId,
            patientId: updatedApt.patientId,
            appointmentId: updatedApt.id,
            date: sessionDate,
            duration: updatedApt.duration ?? 60,
            status: 'realizada',
            source: 'backend',
          },
        });

        const createPaymentWithRetry = async (retryCount = 0): Promise<void> => {
          const maxResult = await tx.payment.aggregate({
            where: { patientId: updatedApt.patientId, userId: updatedApt.userId, deletedAt: null },
            _max: { sessionNumber: true },
          });
          const sessionNumber = (maxResult._max.sessionNumber ?? 0) + 1;

          try {
            await tx.payment.create({
              data: {
                userId: updatedApt.userId,
                patientId: updatedApt.patientId,
                clinicId: updatedApt.clinicId,
                sessionId: session.id,
                sessionNumber,
                amount: new Prisma.Decimal(0),
                date: dueDate,
                status: 'pendente',
                source: 'backend',
              },
            });
          } catch (err: unknown) {
            const isUniqueViolation =
              err &&
              typeof err === 'object' &&
              'code' in err &&
              (err as { code?: string }).code === PRISMA_UNIQUE_VIOLATION;
            if (isUniqueViolation && retryCount < 1) {
              return createPaymentWithRetry(retryCount + 1);
            }
            throw err;
          }
        };
        await createPaymentWithRetry();
        paymentCreated = true;
      }

      // B2: status → CANCELADO: cancelar cobrança vinculada
      if (newStatus && becomesCancelado && existing.session?.payment && !existing.session.payment.deletedAt) {
        cancelledPaymentId = existing.session.payment.id;
        await tx.payment.update({
          where: { id: cancelledPaymentId },
          data: { status: 'cancelado' },
        });
        paymentCancelled = true;
      }

      return { updated: updatedApt, paymentCreated, paymentCancelled, cancelledPaymentId };
    });

    // Auditoria (fora da transação, nunca quebra fluxo)
    if (newStatus) {
      this.auditService.log({
        userId,
        clinicId,
        action: 'appointment_status_change',
        entity: 'Appointment',
        entityId: id,
        metadata: { from: existing.status, to: newStatus },
      }).catch(() => {});
    }
    if (updated.paymentCreated) {
      this.auditService.log({
        userId,
        clinicId,
        action: 'payment_creation',
        entity: 'Payment',
        entityId: undefined,
        metadata: { appointmentId: id, patientId: existing.patientId },
      }).catch(() => {});
    }
    if (updated.paymentCancelled && updated.cancelledPaymentId) {
      this.auditService.log({
        userId,
        clinicId,
        action: 'payment_cancellation',
        entity: 'Payment',
        entityId: updated.cancelledPaymentId,
        metadata: { appointmentId: id },
      }).catch(() => {});
    }

    return updated.updated;
  }

  async findAll(userId: string, clinicId: string) {
    const clinicUser = await this.prisma.clinicUser.findUnique({
      where: { clinicId_userId: { clinicId, userId } },
      select: { canViewAllPatients: true, role: true },
    });
    if (!clinicUser) throw new NotFoundException('Clínica não encontrada ou sem acesso');

    const where: any = { clinicId };
    if (!clinicUser.canViewAllPatients && !['owner', 'admin'].includes(clinicUser.role)) {
      where.userId = userId;
    }

    const appointments = await this.prisma.appointment.findMany({
      where: whereNotDeleted('appointment', where),
      include: {
        patient: {
          select: {
            id: true,
            name: true,
          },
        },
        user: {
          select: {
            id: true,
            name: true,
          },
        },
        session: {
          include: {
            payment: {
              select: {
                status: true,
                date: true,
                deletedAt: true,
              },
            },
          },
        },
      },
      orderBy: { scheduledAt: 'asc' },
    });

    /**
     * Contrato mínimo (compatível) para Android/Web:
     * - startsAt/endsAt derivados de scheduledAt/duration
     * - paymentStatus derivado do pagamento (quando existir)
     */
    return appointments.map((a) => {
      const startsAt = a.scheduledAt;
      const endsAt = new Date(a.scheduledAt.getTime() + (a.duration ?? 60) * 60 * 1000);
      const session = a.session?.deletedAt ? null : a.session;
      const payment = session?.payment?.deletedAt ? null : session?.payment;
      const paymentStatus = payment?.status ?? 'pendente';

      return {
        ...a,
        session,
        startsAt,
        endsAt,
        paymentStatus,
      };
    });
  }

  async getToday(userId: string, clinicId: string) {
    console.log('GET /appointments/today chamado');
    const clinicUser = await this.prisma.clinicUser.findUnique({
      where: { clinicId_userId: { clinicId, userId } },
      select: { canViewAllPatients: true, role: true },
    });
    if (!clinicUser) throw new NotFoundException('Clínica não encontrada ou sem acesso');

    const where: any = { clinicId };
    if (!clinicUser.canViewAllPatients && !['owner', 'admin'].includes(clinicUser.role)) {
      where.userId = userId;
    }

    const now = new Date();
    const startOfDay = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0, 0);
    const endOfDay = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59, 999);

    const appointments = await this.prisma.appointment.findMany({
      where: {
        ...whereNotDeleted('appointment', where),
        status: { notIn: ['cancelada', 'cancelado', 'CANCELADO', 'CANCELADA'] },
        scheduledAt: { gte: startOfDay, lte: endOfDay },
      },
      include: {
        patient: { select: { id: true, name: true } },
      },
      orderBy: { scheduledAt: 'asc' },
    });

    const appointmentsFormatted = appointments.map((a) => {
      const time = a.scheduledAt.toTimeString().slice(0, 5);
      const statusMap: Record<string, string> = {
        agendada: 'scheduled',
        confirmada: 'confirmed',
        realizada: 'completed',
        cancelada: 'cancelled',
      };
      return {
        id: a.id,
        patient: a.patient?.name ?? '—',
        patientName: a.patient?.name ?? '—',
        time,
        startTime: time,
        type: a.type ?? 'Consulta',
        sessionType: a.type ?? 'Consulta',
        status: statusMap[a.status] ?? 'pending',
      };
    });

    return {
      count: appointments.length,
      appointments: appointmentsFormatted,
      data: appointmentsFormatted,
      items: appointments,
    };
  }

  async getRecent(userId: string, clinicId: string) {
    const clinicUser = await this.prisma.clinicUser.findUnique({
      where: { clinicId_userId: { clinicId, userId } },
      select: { canViewAllPatients: true, role: true },
    });
    if (!clinicUser) throw new NotFoundException('Clínica não encontrada ou sem acesso');

    const where: any = { clinicId };
    if (!clinicUser.canViewAllPatients && !['owner', 'admin'].includes(clinicUser.role)) {
      where.userId = userId;
    }

    const appointments = await this.prisma.appointment.findMany({
      where: {
        ...whereNotDeleted('appointment', where),
        status: { notIn: ['cancelada', 'cancelado', 'CANCELADO', 'CANCELADA'] },
        scheduledAt: { gte: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000) },
      },
      include: { patient: { select: { id: true, name: true } } },
      orderBy: { scheduledAt: 'desc' },
      take: 10,
    });

    return appointments.map((a) => ({
      id: a.id,
      patient: a.patient?.name ?? '—',
      time: a.scheduledAt.toTimeString().slice(0, 5),
      type: a.type ?? 'Consulta',
      status: a.status,
      scheduledAt: a.scheduledAt.toISOString(),
    }));
  }

  async findOne(id: string, userId: string, clinicId: string) {
    const apt = await this.prisma.appointment.findFirst({
      where: whereNotDeleted('appointment', { id, userId, clinicId }),
      include: {
        patient: { select: { id: true, name: true } },
        user: { select: { id: true, name: true } },
        session: true,
      },
    });
    if (!apt) {
      throw new NotFoundException('Agendamento não encontrado');
    }
    return apt;
  }

  /**
   * Retorna horários disponíveis para um profissional em uma data.
   * Horário comercial: 08:00 às 20:00. Slots de `durationMinutes` minutos.
   */
  async getAvailableSlots(
    userId: string,
    clinicId: string,
    date: string, // YYYY-MM-DD
    durationMinutes = 60,
  ): Promise<Array<{ startTime: string; endTime: string; isoDateTime: string }>> {
    // Carrega consultas existentes do profissional no dia
    const dayStart = new Date(`${date}T00:00:00.000Z`);
    const dayEnd = new Date(`${date}T23:59:59.999Z`);

    const existing = await this.prisma.appointment.findMany({
      where: {
        userId,
        clinicId,
        scheduledAt: { gte: dayStart, lte: dayEnd },
        status: { notIn: ['cancelada', 'cancelado'] },
        deletedAt: null,
      },
      select: { scheduledAt: true, duration: true },
    });

    const slots: Array<{ startTime: string; endTime: string; isoDateTime: string }> = [];
    // Horário comercial 08:00-20:00 (Brasília = UTC-3)
    const START_HOUR = 8;
    const END_HOUR = 20;

    for (let hour = START_HOUR; hour + durationMinutes / 60 <= END_HOUR; hour += durationMinutes / 60) {
      const slotStart = new Date(`${date}T${String(Math.floor(hour)).padStart(2, '0')}:${String((hour % 1) * 60).padStart(2, '0')}:00.000Z`);
      // Ajusta para UTC-3 (Brasil)
      slotStart.setHours(slotStart.getHours() + 3);
      const slotEnd = new Date(slotStart.getTime() + durationMinutes * 60 * 1000);

      const conflict = existing.some((a) => {
        const aStart = a.scheduledAt.getTime();
        const aEnd = aStart + (a.duration ?? 60) * 60 * 1000;
        return aStart < slotEnd.getTime() && aEnd > slotStart.getTime();
      });

      if (!conflict && slotStart > new Date()) {
        const fmt = (d: Date) =>
          `${String(d.getUTCHours()).padStart(2, '0')}:${String(d.getUTCMinutes()).padStart(2, '0')}`;
        slots.push({
          startTime: fmt(slotStart),
          endTime: fmt(slotEnd),
          isoDateTime: slotStart.toISOString(),
        });
      }
    }

    return slots;
  }

  async delete(id: string, userId: string, clinicId: string) {
    const existing = await this.prisma.appointment.findFirst({
      where: whereNotDeleted('appointment', { id, userId, clinicId }),
      include: { patient: { select: { id: true, name: true } } },
    });
    if (!existing) {
      throw new NotFoundException('Agendamento não encontrado');
    }
    await this.prisma.appointment.update({
      where: { id },
      data: { deletedAt: new Date() },
    });

    this.auditService.log({
      userId,
      clinicId,
      action: 'appointment_deletion',
      entity: 'Appointment',
      entityId: id,
      metadata: { patientId: existing.patientId, patientName: existing.patient?.name },
    }).catch(() => {});
  }
}

