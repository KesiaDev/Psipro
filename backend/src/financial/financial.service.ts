import { Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class FinancialService {
  constructor(private prisma: PrismaService) {}

  async getSummary(userId: string) {
    const now = new Date();
    const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);
    const startOfWeek = new Date(now);
    startOfWeek.setDate(now.getDate() - now.getDay());

    // Receita do mês
    const monthlyRevenue = await this.prisma.payment.aggregate({
      where: {
        userId,
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
        userId,
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
        userId,
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
        userId,
        date: { gte: threeMonthsAgo },
        status: 'pago',
      },
      _sum: {
        amount: true,
      },
    });

    // Ticket médio
    const sessionsCount = await this.prisma.session.count({
      where: {
        userId,
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

  async getPatientFinancial(patientId: string, userId: string) {
    // Verificar se o paciente pertence ao usuário
    const patient = await this.prisma.patient.findUnique({
      where: { id: patientId },
    });

    if (!patient || patient.userId !== userId) {
      throw new Error('Acesso negado');
    }

    // Total faturado
    const totalFaturado = await this.prisma.payment.aggregate({
      where: {
        patientId,
        userId,
      },
      _sum: {
        amount: true,
      },
    });

    // Total recebido
    const totalRecebido = await this.prisma.payment.aggregate({
      where: {
        patientId,
        userId,
        status: 'pago',
      },
      _sum: {
        amount: true,
      },
    });

    // Total em aberto
    const totalAberto = await this.prisma.payment.aggregate({
      where: {
        patientId,
        userId,
        status: 'pendente',
      },
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

