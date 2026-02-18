import { BadRequestException, Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
@Injectable()
export class DashboardService {
  constructor(private prisma: PrismaService) {}

  private startOfMonth(): Date {
    const now = new Date();
    return new Date(now.getFullYear(), now.getMonth(), 1, 0, 0, 0, 0);
  }

  private startOfWeek(): Date {
    const d = new Date();
    const day = d.getDay();
    const diff = d.getDate() - day + (day === 0 ? -6 : 1); // segunda-feira
    return new Date(d.getFullYear(), d.getMonth(), diff, 0, 0, 0, 0);
  }

  private startOfToday(): Date {
    const now = new Date();
    return new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0, 0);
  }

  private endOfToday(): Date {
    const now = new Date();
    return new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59, 999);
  }

  async getMetrics(clinicId: string | undefined) {
    if (!clinicId) {
      throw new BadRequestException('clinicId é obrigatório');
    }

    const monthStart = this.startOfMonth();
    const weekStart = this.startOfWeek();

    const [activePatients, sessionsThisMonth, scheduledThisWeek] = await Promise.all([
      this.prisma.patient.count({
        where: { clinicId },
      }),
      this.prisma.appointment.count({
        where: {
          clinicId,
          status: 'realizada',
          scheduledAt: { gte: monthStart },
        },
      }),
      this.prisma.appointment.count({
        where: {
          clinicId,
          status: { in: ['agendada', 'confirmada'] },
          scheduledAt: { gte: weekStart },
        },
      }),
    ]);

    return {
      activePatients,
      sessionsThisMonth,
      scheduledThisWeek,
    };
  }

  async getAgendaSummary(clinicId: string | undefined) {
    if (!clinicId) {
      throw new BadRequestException('clinicId é obrigatório');
    }

    const todayStart = this.startOfToday();
    const todayEnd = this.endOfToday();

    const [today, upcoming] = await Promise.all([
      this.prisma.appointment.count({
        where: {
          clinicId,
          scheduledAt: {
            gte: todayStart,
            lte: todayEnd,
          },
        },
      }),
      this.prisma.appointment.count({
        where: {
          clinicId,
          scheduledAt: { gt: todayEnd },
        },
      }),
    ]);

    return { today, upcoming };
  }

  async getFinanceSummary(clinicId: string | undefined) {
    if (!clinicId) {
      throw new BadRequestException('clinicId é obrigatório');
    }

    const monthStart = this.startOfMonth();

    const records = await this.prisma.financialRecord.findMany({
      where: {
        clinicId,
        date: { gte: monthStart },
      },
      select: { type: true, amount: true },
    });

    let monthRevenue = 0;
    for (const r of records) {
      if (r.type === 'receita' || r.type === 'INCOME') {
        monthRevenue += Number(r.amount);
      }
    }

    const pendingPayments = await this.prisma.payment.aggregate({
      where: {
        clinicId,
        status: 'pendente',
      },
      _sum: { amount: true },
    });

    const receivable = Number(pendingPayments._sum.amount ?? 0);

    return {
      monthRevenue,
      receivable,
    };
  }
}
