import { Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { CreatePaymentDto } from './dto/create-payment.dto';

@Injectable()
export class PaymentsService {
  constructor(private prisma: PrismaService) {}

  async create(userId: string, createPaymentDto: CreatePaymentDto) {
    // Verificar se o paciente pertence ao usuário
    const patient = await this.prisma.patient.findUnique({
      where: { id: createPaymentDto.patientId },
    });

    if (!patient || patient.userId !== userId) {
      throw new Error('Paciente não encontrado ou acesso negado');
    }

    const payment = await this.prisma.payment.create({
      data: {
        ...createPaymentDto,
        userId,
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




