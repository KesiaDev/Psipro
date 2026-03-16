import { Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { whereNotDeleted } from '../prisma/soft-delete.helper';

const MONTH_NAMES = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'];

@Injectable()
export class ReportsService {
  constructor(private prisma: PrismaService) {}

  private last6Months(): { start: Date; labels: string[] } {
    const now = new Date();
    const labels: string[] = [];
    for (let i = 5; i >= 0; i--) {
      const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
      labels.push(MONTH_NAMES[d.getMonth()]);
    }
    const start = new Date(now.getFullYear(), now.getMonth() - 5, 1, 0, 0, 0, 0);
    return { start, labels };
  }

  async findAll(userId: string, clinicId: string) {
    const { start, labels } = this.last6Months();
    const patientWhere = whereNotDeleted('patient', { clinicId }) as Record<string, unknown>;
    const sessionWhereBase = {
      ...whereNotDeleted('session', {}),
      status: 'realizada',
      date: { gte: start },
      patient: { ...patientWhere, deletedAt: null },
    };

    const [sessions, paymentsByMonth, financialByMonth, patientsWithSessions, totalPatients, sessionTypesRaw] =
      await Promise.all([
        this.prisma.session.findMany({
          where: sessionWhereBase,
          select: { id: true, patientId: true, duration: true, date: true, appointment: { select: { type: true } } },
        }),
      this.prisma.payment.findMany({
        where: whereNotDeleted('payment', { clinicId, status: 'pago', date: { gte: start } }),
        select: { amount: true, date: true },
      }),
      this.prisma.financialRecord.findMany({
        where: { clinicId: clinicId || null, date: { gte: start } },
        select: { type: true, amount: true, date: true },
      }),
      this.prisma.session.groupBy({
        by: ['patientId'],
        where: sessionWhereBase,
        _count: { id: true },
      }),
      this.prisma.patient.count({ where: patientWhere }),
      this.prisma.session.findMany({
        where: sessionWhereBase,
        select: { appointment: { select: { type: true } } },
      }),
    ]);

    const monthlySessions = labels.map((month, i) => {
      const refDate = new Date();
      refDate.setMonth(refDate.getMonth() - (5 - i));
      const count = sessions.filter(
        (s) =>
          new Date(s.date).getMonth() === refDate.getMonth() &&
          new Date(s.date).getFullYear() === refDate.getFullYear(),
      ).length;
      return { month, sessions: count };
    });

    const revenueByMonth = labels.map((month, i) => {
      const refDate = new Date();
      refDate.setMonth(refDate.getMonth() - (5 - i));
      const paymentsSum = paymentsByMonth
        .filter(
          (p) =>
            new Date(p.date).getMonth() === refDate.getMonth() &&
            new Date(p.date).getFullYear() === refDate.getFullYear(),
        )
        .reduce((acc, p) => acc + Number(p.amount), 0);
      const receitaSum = financialByMonth
        .filter(
          (f) =>
            f.type === 'receita' &&
            new Date(f.date).getMonth() === refDate.getMonth() &&
            new Date(f.date).getFullYear() === refDate.getFullYear(),
        )
        .reduce((acc, f) => acc + Number(f.amount), 0);
      return { month, value: paymentsSum + receitaSum };
    });

    const typeMap = new Map<string, number>();
    sessionTypesRaw.forEach((s) => {
      const t = s.appointment?.type ?? 'Consulta regular';
      typeMap.set(t, (typeMap.get(t) ?? 0) + 1);
    });
    const totalForTypes = [...typeMap.values()].reduce((a, b) => a + b, 0);
    const typeData = [...typeMap.entries()].map(([name, count]) => ({
      name,
      value: totalForTypes > 0 ? Math.round((count / totalForTypes) * 100) : 0,
    }));

    const patientIds = [...new Set(patientsWithSessions.map((p) => p.patientId))];
    const patientNames = await this.prisma.patient.findMany({
      where: { id: { in: patientIds } },
      select: { id: true, name: true },
    });
    const nameMap = new Map(patientNames.map((p) => [p.id, p.name]));
    const topPatients = patientsWithSessions
      .sort((a, b) => b._count.id - a._count.id)
      .slice(0, 10)
      .map((p) => ({
        name: nameMap.get(p.patientId) ?? '—',
        sessions: p._count.id,
        percentage: 0,
      }));
    const maxSessions = Math.max(...topPatients.map((p) => p.sessions), 1);
    topPatients.forEach((p) => {
      p.percentage = Math.round((p.sessions / maxSessions) * 100);
    });

    const patientsWith2Plus = patientsWithSessions.filter((p) => p._count.id >= 2).length;
    const patientsWith1Plus = patientsWithSessions.length;
    const returnRate = patientsWith1Plus > 0 ? Math.round((patientsWith2Plus / patientsWith1Plus) * 100) : 0;

    const totalDuration = sessions.reduce((acc, s) => acc + (s.duration ?? 0), 0);
    const weeksCount = 26;
    const avgHoursPerWeek = weeksCount > 0 ? Math.round((totalDuration / 60 / weeksCount) * 10) / 10 : 0;

    return {
      monthlySessions,
      revenue: revenueByMonth,
      revenueData: revenueByMonth,
      sessionTypes: typeData,
      typeData: typeData,
      topPatients,
      stats: {
        totalSessions: sessions.length,
        activePatients: totalPatients,
        returnRate,
        avgHoursPerWeek,
      },
    };
  }

  async getSummary(userId: string, clinicId: string) {
    const now = new Date();
    const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0, 0);
    const todayEnd = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59, 999);

    const patientWhere = whereNotDeleted('patient', { clinicId }) as Record<string, unknown>;
    const sessionWhereBase = {
      ...whereNotDeleted('session', {}),
      status: 'realizada',
      patient: { ...patientWhere, deletedAt: null },
    };

    const [totalPatients, totalSessions, revenueRes, expensesRes, todaySessions, receitaFromFr] =
      await Promise.all([
        this.prisma.patient.count({ where: patientWhere }),
        this.prisma.session.count({
          where: sessionWhereBase,
        }),
        this.prisma.payment.aggregate({
          where: whereNotDeleted('payment', { clinicId, status: 'pago' }),
          _sum: { amount: true },
        }),
        this.prisma.financialRecord.aggregate({
          where: { clinicId: clinicId || null, type: 'despesa' },
          _sum: { amount: true },
        }),
        this.prisma.session.count({
          where: {
            ...sessionWhereBase,
            date: { gte: todayStart, lt: todayEnd },
          },
        }),
        this.prisma.financialRecord.aggregate({
          where: { clinicId: clinicId || null, type: 'receita' },
          _sum: { amount: true },
        }),
      ]);

    const totalRevenue =
      Number(revenueRes._sum.amount ?? 0) + Number(receitaFromFr._sum.amount ?? 0);

    return {
      totalPatients,
      totalSessions,
      totalRevenue,
      totalExpenses: Number(expensesRes._sum.amount ?? 0),
      todaySessions,
    };
  }

  async getToday(userId: string, clinicId: string) {
    const todayStart = new Date();
    todayStart.setHours(0, 0, 0, 0);
    const todayEnd = new Date();
    todayEnd.setHours(23, 59, 59, 999);
    const patientWhere = whereNotDeleted('patient', { clinicId }) as Record<string, unknown>;

    const sessions = await this.prisma.session.findMany({
      where: {
        ...whereNotDeleted('session', {}),
        status: 'realizada',
        patient: { ...patientWhere, deletedAt: null },
        date: { gte: todayStart, lt: todayEnd },
      },
      include: { patient: { select: { name: true } } },
    });

    return { todaySessions: sessions.length, sessions };
  }

  async getStats(userId: string, clinicId: string) {
    const summary = await this.getSummary(userId, clinicId);
    return {
      totalSessions: summary.totalSessions,
      activePatients: summary.totalPatients,
      totalRevenue: summary.totalRevenue,
      totalExpenses: summary.totalExpenses,
      todaySessions: summary.todaySessions,
    };
  }

  async getCount(userId: string, clinicId: string) {
    const patientWhere = whereNotDeleted('patient', { clinicId }) as Record<string, unknown>;
    const [patients, sessions] = await Promise.all([
      this.prisma.patient.count({ where: patientWhere }),
      this.prisma.session.count({
        where: {
          ...whereNotDeleted('session', {}),
          status: 'realizada',
          patient: { ...patientWhere, deletedAt: null },
        },
      }),
    ]);
    return { patients, sessions };
  }
}
