import { Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { whereNotDeleted } from '../prisma/soft-delete.helper';

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

  private endOfWeek(): Date {
    const start = this.startOfWeek();
    return new Date(start.getTime() + 7 * 24 * 60 * 60 * 1000 - 1);
  }

  async getMetrics(clinicId: string) {
    const monthStart = this.startOfMonth();
    const weekStart = this.startOfWeek();

    const [
      activePatients,
      sessionsThisMonth,
      sessionsThisWeek,
      revenueResult,
      pendingResult,
    ] = await Promise.all([
      this.prisma.patient.count({ where: whereNotDeleted('patient', { clinicId }) }),
      this.prisma.appointment.count({
        where: whereNotDeleted('appointment', {
          clinicId,
          status: 'realizada',
          scheduledAt: { gte: monthStart },
        }),
      }),
      this.prisma.appointment.count({
        where: whereNotDeleted('appointment', {
          clinicId,
          status: { in: ['agendada', 'confirmada'] },
          scheduledAt: { gte: weekStart },
        }),
      }),
      this.prisma.payment.aggregate({
        where: whereNotDeleted('payment', { clinicId, date: { gte: monthStart } }),
        _sum: { amount: true },
      }),
      this.prisma.payment.aggregate({
        where: whereNotDeleted('payment', { clinicId, status: 'pendente' }),
        _sum: { amount: true },
      }),
    ]);

    return {
      activePatients,
      sessionsThisMonth,
      sessionsThisWeek,
      monthlyRevenue: Number(revenueResult._sum.amount ?? 0),
      pendingRevenue: Number(pendingResult._sum.amount ?? 0),
    };
  }

  async getAgendaSummary(clinicId: string) {
    const weekStart = this.startOfWeek();
    const weekEnd = this.endOfWeek();

    const appointments = await this.prisma.appointment.findMany({
      where: whereNotDeleted('appointment', {
        clinicId,
        scheduledAt: {
          gte: weekStart,
          lte: weekEnd,
        },
      }),
      select: { scheduledAt: true },
    });

    const totalSessionsThisWeek = appointments.length;

    const dayCounts: Record<number, number> = {};
    appointments.forEach((apt) => {
      const day = new Date(apt.scheduledAt).getDay();
      dayCounts[day] = (dayCounts[day] || 0) + 1;
    });

    const dayNames = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'];
    const sortedDays = Object.entries(dayCounts)
      .sort(([, a], [, b]) => b - a)
      .map(([day]) => parseInt(day, 10));

    const busiestDays = sortedDays.slice(0, 2).map((d) => dayNames[d]);
    const emptiestDays = sortedDays.slice(-2).reverse().map((d) => dayNames[d]);

    return {
      totalSessionsThisWeek,
      busiestDays,
      emptiestDays,
      isEmpty: totalSessionsThisWeek === 0,
    };
  }

  async getFinanceSummary(clinicId: string) {
    const monthStart = this.startOfMonth();

    const [revenueResult, pendingResult, sessionsCount] = await Promise.all([
      this.prisma.payment.aggregate({
        where: whereNotDeleted('payment', { clinicId, date: { gte: monthStart } }),
        _sum: { amount: true },
      }),
      this.prisma.payment.count({
        where: whereNotDeleted('payment', { clinicId, status: 'pendente' }),
      }),
      this.prisma.appointment.count({
        where: whereNotDeleted('appointment', {
          clinicId,
          status: 'realizada',
          scheduledAt: { gte: monthStart },
        }),
      }),
    ]);

    const monthlyRevenue = Number(revenueResult._sum.amount ?? 0);
    const unpaidSessions = pendingResult;
    const averagePerSession =
      sessionsCount > 0 ? monthlyRevenue / sessionsCount : 0;

    return {
      monthlyRevenue,
      averagePerSession,
      unpaidSessions,
      isEmpty: sessionsCount === 0 && monthlyRevenue === 0,
    };
  }

  async getCount(clinicId: string) {
    const [patients, appointments, sessions] = await Promise.all([
      this.prisma.patient.count({ where: whereNotDeleted('patient', { clinicId }) }),
      this.prisma.appointment.count({
        where: whereNotDeleted('appointment', {
          clinicId,
          status: { notIn: ['cancelada', 'cancelado'] },
        }),
      }),
      this.prisma.session.count({
        where: whereNotDeleted('session', {
          status: 'realizada',
          patient: { clinicId, deletedAt: null },
        }),
      }),
    ]);
    return { patients, appointments, sessions };
  }

  async getStats(clinicId: string) {
    const [metrics, agendaSummary, financeSummary] = await Promise.all([
      this.getMetrics(clinicId),
      this.getAgendaSummary(clinicId),
      this.getFinanceSummary(clinicId),
    ]);
    return {
      ...metrics,
      agendaSummary,
      financeSummary,
    };
  }

  async getSummary(clinicId: string) {
    const count = await this.getCount(clinicId);
    const financeSummary = await this.getFinanceSummary(clinicId);
    return {
      ...count,
      monthlyRevenue: financeSummary.monthlyRevenue,
      averagePerSession: financeSummary.averagePerSession,
    };
  }
}
