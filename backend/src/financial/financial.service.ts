import { Injectable, NotFoundException, BadRequestException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { AuditService } from '../audit/audit.service';
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

export interface CreateChargeDto {
  patientId: string;
  sessionId?: string | null;
  amount: number;
  method?: string | null;
  dueDate?: string | null;
  notes?: string | null;
}

export interface UpdateChargeDto {
  amount?: number;
  method?: string | null;
  dueDate?: string | null;
  notes?: string | null;
}

export interface PayChargeDto {
  method?: string | null;
  paidAt?: string | null;
}

@Injectable()
export class FinancialService {
  constructor(private prisma: PrismaService, private auditService: AuditService) {}

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
    return records.map((r) => ({
      id: r.id,
      user_id: r.userId,
      patient_id: null,
      type: r.type === 'receita' ? 'income' : 'expense',
      category: r.category ?? '',
      description: r.description ?? '',
      amount: Number(r.amount),
      payment_method: null,
      status: r.type === 'receita' ? 'paid' : 'expense',
      due_date: r.date.toISOString().split('T')[0],
      paid_at: r.type === 'receita' ? r.date.toISOString() : null,
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
    const deleted = await this.prisma.financialRecord.delete({ where: { id } });
    await this.auditService.log({
      userId,
      clinicId: clinicId ?? '',
      action: 'financial_record_deleted',
      entity: 'FinancialRecord',
      entityId: id,
      metadata: { type: existing.type, amount: Number(existing.amount) },
    });
    return deleted;
  }

  // ─── Charges (cobranças vinculadas a sessões/pacientes) ───────────────────

  async findAllCharges(userId: string, clinicId: string) {
    const charges = await this.prisma.payment.findMany({
      where: whereNotDeleted('payment', { userId, clinicId }),
      orderBy: { date: 'desc' },
      include: { patient: { select: { name: true } } },
    });
    return charges.map((c) => this.mapCharge(c));
  }

  async findOneCharge(id: string, userId: string, clinicId: string) {
    const charge = await this.prisma.payment.findFirst({
      where: whereNotDeleted('payment', { id, userId, clinicId }),
      include: { patient: { select: { name: true } } },
    });
    if (!charge) throw new NotFoundException('Cobrança não encontrada');
    return this.mapCharge(charge);
  }

  async createCharge(userId: string, clinicId: string, dto: CreateChargeDto) {
    if (!dto.patientId) throw new BadRequestException('patientId é obrigatório');
    const patient = await this.prisma.patient.findFirst({
      where: { id: dto.patientId, clinicId, deletedAt: null },
    });
    if (!patient) throw new NotFoundException('Paciente não encontrado ou sem acesso');

    const charge = await this.prisma.payment.create({
      data: {
        userId,
        clinicId,
        patientId: dto.patientId,
        sessionId: dto.sessionId ?? null,
        amount: dto.amount,
        date: dto.dueDate ? new Date(dto.dueDate) : new Date(),
        method: dto.method ?? null,
        status: 'pendente',
        notes: dto.notes ?? null,
      },
      include: { patient: { select: { name: true } } },
    });
    return this.mapCharge(charge);
  }

  async updateCharge(id: string, userId: string, clinicId: string, dto: UpdateChargeDto) {
    const existing = await this.prisma.payment.findFirst({
      where: whereNotDeleted('payment', { id, userId, clinicId }),
    });
    if (!existing) throw new NotFoundException('Cobrança não encontrada');
    const charge = await this.prisma.payment.update({
      where: { id },
      data: {
        ...(dto.amount !== undefined && { amount: dto.amount }),
        ...(dto.method !== undefined && { method: dto.method }),
        ...(dto.dueDate !== undefined && { date: new Date(dto.dueDate) }),
        ...(dto.notes !== undefined && { notes: dto.notes }),
      },
      include: { patient: { select: { name: true } } },
    });
    return this.mapCharge(charge);
  }

  async payCharge(id: string, userId: string, clinicId: string, dto: PayChargeDto) {
    const existing = await this.prisma.payment.findFirst({
      where: whereNotDeleted('payment', { id, userId, clinicId }),
    });
    if (!existing) throw new NotFoundException('Cobrança não encontrada');
    if (existing.status === 'pago') throw new BadRequestException('Cobrança já foi paga');

    const paidAt = dto.paidAt ? new Date(dto.paidAt) : new Date();
    const charge = await this.prisma.payment.update({
      where: { id },
      data: {
        status: 'pago',
        date: paidAt,
        ...(dto.method && { method: dto.method }),
      },
      include: { patient: { select: { name: true } } },
    });
    await this.auditService.log({
      userId,
      clinicId,
      action: 'charge_paid',
      entity: 'Payment',
      entityId: id,
      metadata: { amount: Number(existing.amount), method: dto.method ?? existing.method },
    });
    return this.mapCharge(charge);
  }

  async deleteCharge(id: string, userId: string, clinicId: string) {
    const existing = await this.prisma.payment.findFirst({
      where: whereNotDeleted('payment', { id, userId, clinicId }),
    });
    if (!existing) throw new NotFoundException('Cobrança não encontrada');
    await this.prisma.payment.update({
      where: { id },
      data: { deletedAt: new Date() },
    });
    await this.auditService.log({
      userId,
      clinicId,
      action: 'charge_deleted',
      entity: 'Payment',
      entityId: id,
      metadata: { amount: Number(existing.amount), status: existing.status },
    });
    return { deleted: true, id };
  }

  private mapCharge(c: any) {
    return {
      id: c.id,
      patient_id: c.patientId,
      patient_name: c.patient?.name ?? null,
      session_id: c.sessionId ?? null,
      amount: Number(c.amount),
      method: c.method ?? null,
      status: c.status,
      due_date: c.date.toISOString().split('T')[0],
      paid_at: c.status === 'pago' ? c.date.toISOString() : null,
      notes: c.notes ?? null,
      created_at: c.createdAt.toISOString(),
      updated_at: c.updatedAt.toISOString(),
    };
  }
}




