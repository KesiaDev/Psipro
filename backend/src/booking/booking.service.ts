import { Injectable, BadRequestException, Logger } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { randomBytes } from 'crypto';

const INTAKE_TTL_DAYS = 7;

@Injectable()
export class BookingService {
  private readonly logger = new Logger(BookingService.name);

  constructor(private readonly prisma: PrismaService) {}

  /**
   * Resolve userId e clinicId do profissional público configurado.
   * Aceita PUBLIC_BOOKING_USER_ID direto OU busca por PUBLIC_BOOKING_USER_EMAIL.
   */
  private async resolveProfessional(): Promise<{ userId: string; clinicId: string | null }> {
    let resolvedUserId: string | null = process.env.PUBLIC_BOOKING_USER_ID || null;

    if (!resolvedUserId) {
      const email = process.env.PUBLIC_BOOKING_USER_EMAIL;
      if (email) {
        const user = await this.prisma.user.findFirst({
          where: { email },
          select: { id: true },
        });
        if (user) resolvedUserId = user.id;
      }
    }

    if (!resolvedUserId) throw new BadRequestException('Agendamento público não configurado.');

    // Se PUBLIC_BOOKING_CLINIC_ID está configurado, usa direto
    const envClinicId = process.env.PUBLIC_BOOKING_CLINIC_ID || null;
    if (envClinicId) return { userId: resolvedUserId, clinicId: envClinicId };

    // Auto-descoberta 1: clínica principal do usuário (User.clinicId)
    const user = await this.prisma.user.findUnique({
      where: { id: resolvedUserId },
      select: { clinicId: true },
    });
    if (user?.clinicId) return { userId: resolvedUserId, clinicId: user.clinicId };

    // Auto-descoberta 2: primeiro vínculo em ClinicUser
    const membership = await this.prisma.clinicUser.findFirst({
      where: { userId: resolvedUserId },
      select: { clinicId: true },
      orderBy: { createdAt: 'asc' },
    });

    return { userId: resolvedUserId, clinicId: membership?.clinicId ?? null };
  }

  /**
   * Retorna os slots disponíveis para o profissional configurado via env.
   * Não requer autenticação — endpoint público.
   */
  async getPublicSlots(date: string, durationMinutes = 60) {
    const { userId, clinicId } = await this.resolveProfessional();

    const dayStart = new Date(`${date}T00:00:00.000Z`);
    const dayEnd = new Date(`${date}T23:59:59.999Z`);

    const existing = await this.prisma.appointment.findMany({
      where: {
        userId,
        ...(clinicId ? { clinicId } : {}),
        scheduledAt: { gte: dayStart, lte: dayEnd },
        status: { notIn: ['cancelada', 'cancelado'] },
        deletedAt: null,
      },
      select: { scheduledAt: true, duration: true },
    });

    const slots: Array<{ startTime: string; endTime: string; isoDateTime: string }> = [];
    const START_HOUR = 8;
    const END_HOUR = 20;

    for (
      let hour = START_HOUR;
      hour + durationMinutes / 60 <= END_HOUR;
      hour += durationMinutes / 60
    ) {
      const h = Math.floor(hour);
      const m = Math.round((hour - h) * 60);
      const slotStart = new Date(
        `${date}T${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:00.000-03:00`,
      );
      const slotEnd = new Date(slotStart.getTime() + durationMinutes * 60 * 1000);

      const conflict = existing.some((a) => {
        const aStart = a.scheduledAt.getTime();
        const aEnd = aStart + (a.duration ?? 60) * 60 * 1000;
        return aStart < slotEnd.getTime() && aEnd > slotStart.getTime();
      });

      if (!conflict && slotStart > new Date()) {
        const fmt = (d: Date) =>
          d.toLocaleTimeString('pt-BR', {
            timeZone: 'America/Sao_Paulo',
            hour: '2-digit',
            minute: '2-digit',
            hour12: false,
          });
        slots.push({
          startTime: fmt(slotStart),
          endTime: fmt(slotEnd),
          isoDateTime: slotStart.toISOString(),
        });
      }
    }

    return slots;
  }

  /**
   * Retorna dias do mês que têm pelo menos 1 slot disponível.
   */
  async getAvailableDays(year: number, month: number) {
    const daysInMonth = new Date(year, month, 0).getDate();
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const availableDays: number[] = [];

    for (let day = 1; day <= daysInMonth; day++) {
      const date = new Date(year, month - 1, day);
      if (date < today) continue;
      if (date.getDay() === 0) continue; // pula domingos

      const dateStr = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
      const slots = await this.getPublicSlots(dateStr);
      if (slots.length > 0) availableDays.push(day);
    }

    return availableDays;
  }

  /** Gera token de anamnese vinculado ao profissional configurado. */
  private async generateIntakeToken(userId: string, clinicId: string | null): Promise<string | null> {
    if (!clinicId) return null;
    try {
      const expiresAt = new Date();
      expiresAt.setDate(expiresAt.getDate() + INTAKE_TTL_DAYS);
      const token = randomBytes(6).toString('hex');
      await this.prisma.intakeToken.create({
        data: { token, clinicId, userId, expiresAt },
      });
      const base =
        process.env.WEB_APP_URL ||
        (process.env.RAILWAY_PUBLIC_DOMAIN
          ? `https://${process.env.RAILWAY_PUBLIC_DOMAIN}`
          : 'https://psipro-backend-production.up.railway.app');
      return `${base}/i/${token}`;
    } catch (err: any) {
      this.logger.warn(`[booking] Falha ao gerar token de anamnese: ${err.message}`);
      return null;
    }
  }

  /** Dispara webhook N8N (fire-and-forget) com os dados do agendamento. */
  private fireN8NWebhook(payload: Record<string, unknown>): void {
    const url = process.env.N8N_BOOKING_WEBHOOK_URL;
    if (!url) return;
    fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    }).catch((err) => this.logger.warn(`[booking] N8N webhook falhou: ${err.message}`));
  }

  /**
   * Cria um agendamento público (paciente não precisa ter conta no psipro).
   */
  async createPublicBooking(dto: {
    patientName: string;
    patientEmail?: string;
    patientPhone?: string;
    isoDateTime: string;
    notes?: string;
    durationMinutes?: number;
    isNewPatient?: boolean;
  }) {
    const { userId, clinicId } = await this.resolveProfessional();

    const scheduledAt = new Date(dto.isoDateTime);
    if (isNaN(scheduledAt.getTime())) throw new BadRequestException('Data/hora inválida.');
    if (scheduledAt <= new Date()) throw new BadRequestException('Horário já passou.');

    const duration = dto.durationMinutes ?? 60;
    const slotEnd = new Date(scheduledAt.getTime() + duration * 60 * 1000);

    // Verifica conflito
    const conflict = await this.prisma.appointment.findFirst({
      where: {
        userId,
        ...(clinicId ? { clinicId } : {}),
        status: { notIn: ['cancelada', 'cancelado'] },
        deletedAt: null,
        scheduledAt: { gte: scheduledAt, lt: slotEnd },
      },
    });
    if (conflict) throw new BadRequestException('Horário não disponível. Escolha outro horário.');

    // Busca ou cria o paciente pelo telefone ou email
    const orConditions = [
      dto.patientPhone ? { phone: dto.patientPhone } : null,
      dto.patientEmail ? { email: dto.patientEmail } : null,
    ].filter(Boolean) as any[];

    let patient = orConditions.length
      ? await this.prisma.patient.findFirst({
          where: { userId, OR: orConditions, deletedAt: null },
        })
      : null;

    if (!patient) {
      patient = await this.prisma.patient.create({
        data: {
          userId,
          ...(clinicId ? { clinicId, clinicOwnerId: userId } : {}),
          name: dto.patientName,
          email: dto.patientEmail || null,
          phone: dto.patientPhone || null,
        },
      });
    }

    const appointment = await this.prisma.appointment.create({
      data: {
        userId,
        ...(clinicId ? { clinicId } : {}),
        patientId: patient.id,
        scheduledAt,
        duration,
        type: 'Consulta',
        status: 'agendada',
        source: 'site_publico',
        notes: dto.notes || `Agendado pelo site por ${dto.patientName}`,
      },
    });

    // Gera link de anamnese apenas para pacientes novos
    const intakeLink = dto.isNewPatient
      ? await this.generateIntakeToken(userId, clinicId)
      : null;

    // Dispara N8N para enviar WhatsApp + e-mail ao paciente e notificar terapeuta
    this.fireN8NWebhook({
      appointmentId: appointment.id,
      patientName: patient.name,
      patientEmail: patient.email ?? null,
      patientPhone: patient.phone ?? null,
      scheduledAt: appointment.scheduledAt.toISOString(),
      intakeLink,
    });

    return {
      success: true,
      appointmentId: appointment.id,
      scheduledAt: appointment.scheduledAt.toISOString(),
      patientName: patient.name,
      intakeLink,
    };
  }
}
