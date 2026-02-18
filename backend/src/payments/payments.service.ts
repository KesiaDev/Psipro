import { Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { CreatePaymentDto } from './dto/create-payment.dto';

@Injectable()
export class PaymentsService {
  constructor(private prisma: PrismaService) {}

  async create(userId: string, createPaymentDto: CreatePaymentDto, clinicId?: string) {
    const patient = await this.prisma.patient.findUnique({
      where: { id: createPaymentDto.patientId },
    });

    if (!patient) {
      throw new Error('Paciente não encontrado');
    }

    const isOwner = patient.userId === userId;
    const isShared = patient.sharedWith?.includes(userId);
    const isSameClinic = clinicId && patient.clinicId === clinicId;
    if (!isOwner && !isShared && !isSameClinic) {
      throw new Error('Acesso negado ao paciente');
    }

    const resolvedClinicId = clinicId ?? patient.clinicId ?? undefined;

    const payment = await this.prisma.payment.create({
      data: {
        ...createPaymentDto,
        userId,
        clinicId: resolvedClinicId,
        date: new Date(createPaymentDto.date),
        amount: createPaymentDto.amount,
        source: createPaymentDto.source || 'app',
      },
      include: {
        session: {
          select: {
            appointmentId: true,
          },
        },
      },
    });

    // Contrato mínimo (compatível) para consumo por Android/Web.
    // Não removemos campos existentes; apenas acrescentamos aliases estáveis.
    return {
      ...payment,
      appointmentId: payment.session?.appointmentId ?? null,
      paidAt: payment.status === 'pago' ? payment.date : null,
    };
  }

  async findByPatient(patientId: string, userId: string) {
    const payments = await this.prisma.payment.findMany({
      where: {
        patientId,
        userId,
      },
      include: {
        session: {
          select: {
            appointmentId: true,
          },
        },
      },
      orderBy: { date: 'desc' },
    });

    return payments.map((p) => ({
      ...p,
      appointmentId: p.session?.appointmentId ?? null,
      paidAt: p.status === 'pago' ? p.date : null,
    }));
  }
}




