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

    return this.prisma.payment.create({
      data: {
        ...createPaymentDto,
        userId,
        date: new Date(createPaymentDto.date),
        amount: createPaymentDto.amount,
        source: createPaymentDto.source || 'app',
      },
    });
  }

  async findByPatient(patientId: string, userId: string) {
    return this.prisma.payment.findMany({
      where: {
        patientId,
        userId,
      },
      orderBy: { date: 'desc' },
    });
  }
}




