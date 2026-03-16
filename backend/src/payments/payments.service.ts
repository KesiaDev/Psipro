import { Injectable } from '@nestjs/common';
import { AuditService } from '../audit/audit.service';
import { PrismaService } from '../prisma/prisma.service';
import { whereNotDeleted } from '../prisma/soft-delete.helper';
import { CreatePaymentDto } from './dto/create-payment.dto';

@Injectable()
export class PaymentsService {
  constructor(
    private prisma: PrismaService,
    private auditService: AuditService,
  ) {}

  async create(userId: string, createPaymentDto: CreatePaymentDto, clinicId: string) {
    const patient = await this.prisma.patient.findFirst({
      where: { id: createPaymentDto.patientId, clinicId },
    });

    if (!patient) {
      throw new Error('Paciente não encontrado ou não pertence à clínica');
    }

    const payment = await this.prisma.payment.create({
      data: {
        ...createPaymentDto,
        userId,
        clinicId,
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

    this.auditService.log({
      userId,
      clinicId,
      action: 'payment_creation',
      entity: 'Payment',
      entityId: payment.id,
      metadata: { patientId: createPaymentDto.patientId, amount: Number(payment.amount) },
    }).catch(() => {});

    // Contrato mínimo (compatível) para consumo por Android/Web.
    // Não removemos campos existentes; apenas acrescentamos aliases estáveis.
    return {
      ...payment,
      appointmentId: payment.session?.appointmentId ?? null,
      paidAt: payment.status === 'pago' ? payment.date : null,
    };
  }

  async findByPatient(patientId: string, userId: string, clinicId: string) {
    const patient = await this.prisma.patient.findFirst({
      where: whereNotDeleted('patient', { id: patientId, clinicId }),
    });
    if (!patient) throw new Error('Paciente não encontrado ou não pertence à clínica');

    const payments = await this.prisma.payment.findMany({
      where: whereNotDeleted('payment', {
        patientId,
        userId,
        clinicId,
      }),
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




