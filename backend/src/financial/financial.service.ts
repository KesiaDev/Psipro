import { ForbiddenException, Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { PatientAccessHelper } from '../common/helpers/patient-access.helper';

@Injectable()
export class FinancialService {
  constructor(
    private prisma: PrismaService,
    private patientAccess: PatientAccessHelper,
  ) {}

  async getSummary(userId: string, clinicId?: string) {
    const now = new Date();
    const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);
    const startOfWeek = new Date(now);
    startOfWeek.setDate(now.getDate() - now.getDay());

    const paymentWhere: { userId: string; date?: object; status?: string; clinicId?: string } = { userId };
    if (clinicId) paymentWhere.clinicId = clinicId;

    // Receita do mês
    const monthlyRevenue = await this.prisma.payment.aggregate({
      where: {
        ...paymentWhere,
        date: { gte: startOfMonth },
        status: 'pago',
      },
      _sum: {
        amount: true,
      },
    });

    // Receita hoje
    const todayRevenue = await this.prisma.payment.aggregate({
      where: {
        ...paymentWhere,
        date: {
          gte: new Date(now.setHours(0, 0, 0, 0)),
          lt: new Date(now.setHours(23, 59, 59, 999)),
        },
        status: 'pago',
      },
      _sum: {
        amount: true,
      },
    });

    // Total a receber
    const pendingAmount = await this.prisma.payment.aggregate({
      where: {
        ...paymentWhere,
        status: 'pendente',
      },
      _sum: {
        amount: true,
      },
    });

    // Total recebido (últimos 3 meses)
    const threeMonthsAgo = new Date(now);
    threeMonthsAgo.setMonth(now.getMonth() - 3);
    const totalReceived = await this.prisma.payment.aggregate({
      where: {
        ...paymentWhere,
        date: { gte: threeMonthsAgo },
        status: 'pago',
      },
      _sum: {
        amount: true,
      },
    });

    // Ticket médio
    const sessionWhere: { userId: string; date?: object; status?: string; clinicId?: string } = { userId };
    if (clinicId) sessionWhere.clinicId = clinicId;
    const sessionsCount = await this.prisma.session.count({
      where: {
        ...sessionWhere,
        date: { gte: threeMonthsAgo },
        status: 'realizada',
      },
    });

    const ticketMedio = sessionsCount > 0 && totalReceived._sum.amount
      ? Number(totalReceived._sum.amount) / sessionsCount
      : 0;

    return {
      receitaHoje: Number(todayRevenue._sum.amount || 0),
      receitaMes: Number(monthlyRevenue._sum.amount || 0),
      totalRecebido: Number(totalReceived._sum.amount || 0),
      totalAReceber: Number(pendingAmount._sum.amount || 0),
      ticketMedio: ticketMedio,
    };
  }

  async getPatientFinancial(
    patientId: string,
    userId: string,
    clinicId?: string,
  ) {
    const hasAccess = await this.patientAccess.hasAccessToPatient(
      patientId,
      userId,
    );
    if (!hasAccess) {
      throw new ForbiddenException('Acesso negado');
    }

    const aggWhere: { patientId: string; userId: string; clinicId?: string } = {
      patientId,
      userId,
    };
    if (clinicId) aggWhere.clinicId = clinicId;

    // Total faturado
    const totalFaturado = await this.prisma.payment.aggregate({
      where: aggWhere,
      _sum: {
        amount: true,
      },
    });

    // Total recebido
    const totalRecebido = await this.prisma.payment.aggregate({
      where: { ...aggWhere, status: 'pago' },
      _sum: {
        amount: true,
      },
    });

    // Total em aberto
    const totalAberto = await this.prisma.payment.aggregate({
      where: { ...aggWhere, status: 'pendente' },
      _sum: {
        amount: true,
      },
    });

    return {
      totalFaturado: Number(totalFaturado._sum.amount || 0),
      totalRecebido: Number(totalRecebido._sum.amount || 0),
      totalAberto: Number(totalAberto._sum.amount || 0),
    };
  }
}




