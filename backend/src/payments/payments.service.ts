import { ForbiddenException, Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { PatientAccessHelper } from '../common/helpers/patient-access.helper';
import { CreatePaymentDto } from './dto/create-payment.dto';

@Injectable()
export class PaymentsService {
  constructor(
    private prisma: PrismaService,
    private patientAccess: PatientAccessHelper,
  ) {}

  async create(
    userId: string,
    clinicId: string | undefined,
    createPaymentDto: CreatePaymentDto,
  ) {
    const hasAccess = await this.patientAccess.hasAccessToPatient(
      createPaymentDto.patientId,
      userId,
    );
    if (!hasAccess) {
      throw new ForbiddenException('Paciente não encontrado ou acesso negado');
    }

    const patient = await this.prisma.patient.findUnique({
      where: { id: createPaymentDto.patientId },
      select: { clinicId: true },
    });

    const effectiveClinicId = clinicId ?? patient?.clinicId ?? undefined;

    const payment = await this.prisma.payment.create({
      data: {
        ...createPaymentDto,
        userId,
        clinicId: effectiveClinicId,
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

  async findByPatient(
    patientId: string,
    userId: string,
    clinicId?: string,
  ) {
    const hasAccess = await this.patientAccess.hasAccessToPatient(
      patientId,
      userId,
    );
    if (!hasAccess) {
      throw new ForbiddenException('Paciente não encontrado ou acesso negado');
    }

    const where: { patientId: string; userId: string; clinicId?: string } = {
      patientId,
      userId,
    };
    if (clinicId) {
      where.clinicId = clinicId;
    }

    const payments = await this.prisma.payment.findMany({
      where,
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




