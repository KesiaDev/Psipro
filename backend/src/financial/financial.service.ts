import { Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { whereNotDeleted } from '../prisma/soft-delete.helper';

export interface CreateFinancialRecordDto {
  patient_id?: string | null;
  type: 'income' | 'expense';
  category: string;
  description: string;
  amount: number;
  payment_method?: string | null;
  status?: string;
  due_date?: string | null;
  paid_at?: string | null;
}

export interface UpdateFinancialRecordDto {
  patient_id?: string | null;
  type?: 'income' | 'expense';
  category?: string;
  description?: string;
  amount?: number;
  payment_method?: string | null;
  status?: string;
  due_date?: string | null;
  paid_at?: string | null;
}

@Injectable()
export class FinancialService {
  constructor(private prisma: PrismaService) {}

  async getSummary(userId: string, clinicId: string) {
    const now = new Date();
    const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);
    const startOfWeek = new Date(now);
    startOfWeek.setDate(now.getDate() - now.getDay());

    const paymentWhere = whereNotDeleted('payment', { userId, clinicId });

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

    // Ticket médio - sessões de pacientes da clínica
    const sessionsCount = await this.prisma.session.count({
      where: whereNotDeleted('session', {
        userId,
        patient: { clinicId, deletedAt: null },
        date: { gte: threeMonthsAgo },
        status: 'realizada',
      }),
    });

    const ticketMedio = sessionsCount > 0 && totalReceived._sum.amount
      ? Number(totalReceived._sum.amount) / sessionsCount
      : 0;

    const expensesRes = await this.prisma.financialRecord.aggregate({
      where: { userId, clinicId: clinicId || null, type: 'despesa' },
      _sum: { amount: true },
    });
    const totalExpensesVal = Number(expensesRes._sum.amount ?? 0);
    const totalIncomeVal = Number(totalReceived._sum.amount || 0);
    const pendingVal = Number(pendingAmount._sum.amount || 0);

    return {
      receitaHoje: Number(todayRevenue._sum.amount || 0),
      receitaMes: Number(monthlyRevenue._sum.amount || 0),
      totalRecebido: totalIncomeVal,
      totalAReceber: pendingVal,
      ticketMedio: ticketMedio,
      totalIncome: totalIncomeVal,
      totalExpenses: totalExpensesVal,
      netProfit: totalIncomeVal - totalExpensesVal,
      pending: pendingVal,
    };
  }

  async getPatientFinancial(patientId: string, userId: string, clinicId: string) {
    const patient = await this.prisma.patient.findFirst({
      where: whereNotDeleted('patient', { id: patientId, clinicId }),
    });

    if (!patient) {
      throw new Error('Acesso negado ao paciente');
    }

    const paymentWhere = whereNotDeleted('payment', { patientId, userId, clinicId });

    // Total faturado - isolamento por clinicId
    const totalFaturado = await this.prisma.payment.aggregate({
      where: paymentWhere,
      _sum: {
        amount: true,
      },
    });

    // Total recebido
    const totalRecebido = await this.prisma.payment.aggregate({
      where: { ...paymentWhere, status: 'pago' },
      _sum: {
        amount: true,
      },
    });

    // Total em aberto
    const totalAberto = await this.prisma.payment.aggregate({
      where: { ...paymentWhere, status: 'pendente' },
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

  async findAllRecords(userId: string, clinicId: string) {
    const records = await this.prisma.financialRecord.findMany({
      where: {
        userId,
        clinicId: clinicId || null,
      },
      orderBy: { date: 'desc' },
    });
    // DEBUG: remover depois de validar fluxo
    console.log('FinancialRecords Debug:', { userId, clinicId, total: records.length });
    return records.map((r) => ({
      id: r.id,
      user_id: r.userId,
      patient_id: null,
      type: r.type === 'receita' ? 'income' : 'expense',
      category: r.category ?? '',
      description: r.description ?? '',
      amount: Number(r.amount),
      payment_method: null,
      status: 'paid' as const,
      due_date: r.date.toISOString().split('T')[0],
      paid_at: r.date.toISOString(),
      created_at: r.createdAt.toISOString(),
      updated_at: r.updatedAt.toISOString(),
      patient_name: null,
    }));
  }

  async createRecord(userId: string, clinicId: string | null, dto: CreateFinancialRecordDto) {
    const date = dto.due_date ? new Date(dto.due_date) : new Date();
    const type = dto.type === 'income' ? 'receita' : 'despesa';
    return this.prisma.financialRecord.create({
      data: {
        userId,
        clinicId: clinicId ?? null,
        date,
        type,
        amount: dto.amount,
        description: dto.description,
        category: dto.category ?? null,
      },
    });
  }

  async updateRecord(id: string, userId: string, clinicId: string | null, dto: UpdateFinancialRecordDto) {
    const existing = await this.prisma.financialRecord.findFirst({
      where: { id, userId, clinicId: clinicId ?? null },
    });
    if (!existing) throw new Error('Registro não encontrado');
    const updateDate = dto.paid_at ? new Date(dto.paid_at) : dto.due_date ? new Date(dto.due_date) : undefined;
    return this.prisma.financialRecord.update({
      where: { id },
      data: {
        ...(dto.type !== undefined && { type: dto.type === 'income' ? 'receita' : 'despesa' }),
        ...(dto.category !== undefined && { category: dto.category }),
        ...(dto.description !== undefined && { description: dto.description }),
        ...(dto.amount !== undefined && { amount: dto.amount }),
        ...(updateDate !== undefined && { date: updateDate }),
      },
    });
  }

  async deleteRecord(id: string, userId: string, clinicId: string | null) {
    const existing = await this.prisma.financialRecord.findFirst({
      where: { id, userId, clinicId: clinicId ?? null },
    });
    if (!existing) throw new Error('Registro não encontrado');
    return this.prisma.financialRecord.delete({ where: { id } });
  }
}




