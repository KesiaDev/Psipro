import { Test, TestingModule } from '@nestjs/testing';
import { Prisma } from '@prisma/client';
import { AppointmentsService } from '../../src/appointments/appointments.service';
import { PrismaService } from '../../src/prisma/prisma.service';
import { AuditService } from '../../src/audit/audit.service';
import { createPrismaMock } from '../utils/prisma-mock';
import { NotFoundException } from '@nestjs/common';

describe('Financeiro: Appointment e Payment', () => {
  let appointmentsService: AppointmentsService;
  let prismaMock: ReturnType<typeof createPrismaMock>;

  const patientId = 'patient-1';
  const userId = 'user-1';
  const clinicId = 'clinic-1';
  const appointmentId = 'apt-1';

  const baseAppointment = {
    id: appointmentId,
    userId,
    patientId,
    clinicId,
    scheduledAt: new Date('2025-03-01T10:00:00Z'),
    duration: 60,
    status: 'agendada',
    type: null,
    notes: null,
    source: 'app',
    syncHash: null,
    lastSyncedAt: null,
    createdAt: new Date(),
    updatedAt: new Date(),
    deletedAt: null,
    patient: { id: patientId, name: 'Paciente' },
    user: { id: userId, name: 'Dr' },
  };

  beforeEach(async () => {
    prismaMock = createPrismaMock();

    prismaMock.$transaction.mockImplementation(async (fn: (tx: unknown) => Promise<unknown>) => {
      return fn(prismaMock);
    });

    const module: TestingModule = await Test.createTestingModule({
      providers: [
        AppointmentsService,
        { provide: PrismaService, useValue: prismaMock },
        { provide: AuditService, useValue: { log: jest.fn().mockResolvedValue(undefined) } },
      ],
    }).compile();

    appointmentsService = module.get<AppointmentsService>(AppointmentsService);
    jest.clearAllMocks();
  });

  it('confirmar appointment (status realizada) deve gerar Session e Payment', async () => {
    const existingWithSession = {
      ...baseAppointment,
      status: 'agendada',
      session: null,
    };
    prismaMock.appointment.findFirst.mockResolvedValue(existingWithSession);
    prismaMock.appointment.findMany.mockResolvedValue([]);
    prismaMock.session.findFirst.mockResolvedValue(null);

    const updatedAppointment = { ...baseAppointment, status: 'realizada' };
    prismaMock.appointment.update.mockResolvedValue(updatedAppointment);

    const newSession = {
      id: 'session-1',
      appointmentId,
      patientId,
      userId,
      date: baseAppointment.scheduledAt,
      duration: 60,
      status: 'realizada',
      source: 'backend',
    };
    prismaMock.session.create.mockResolvedValue(newSession);

    prismaMock.payment.aggregate.mockResolvedValue({ _max: { sessionNumber: 0 } });
    prismaMock.payment.create.mockResolvedValue({
      id: 'pay-1',
      sessionId: newSession.id,
      patientId,
      userId,
      clinicId,
      sessionNumber: 1,
      amount: new Prisma.Decimal(0),
      status: 'pendente',
    });

    const result = await appointmentsService.update(appointmentId, userId, { status: 'realizada' }, clinicId);

    expect(result.status).toBe('realizada');
    expect(prismaMock.session.create).toHaveBeenCalled();
    expect(prismaMock.payment.create).toHaveBeenCalled();
  });

  it('cancelar appointment deve cancelar Payment vinculado', async () => {
    const existingSession = {
      id: 'session-1',
      appointmentId,
      payment: { id: 'pay-1', status: 'pendente' },
    };
    const existing = {
      ...baseAppointment,
      status: 'realizada',
      session: existingSession,
    };
    prismaMock.appointment.findFirst.mockResolvedValue(existing);
    prismaMock.appointment.findMany.mockResolvedValue([]);
    prismaMock.appointment.update.mockResolvedValue({ ...baseAppointment, status: 'cancelada' });

    await appointmentsService.update(appointmentId, userId, { status: 'cancelada' }, clinicId);

    expect(prismaMock.payment.update).toHaveBeenCalledWith(
      expect.objectContaining({
        where: { id: 'pay-1' },
        data: { status: 'cancelado' },
      }),
    );
  });

  it('não deve permitir duplicação de Payment (idempotência)', async () => {
    const existingSession = {
      id: 'session-1',
      appointmentId,
      payment: { id: 'pay-1' },
    };
    const existing = {
      ...baseAppointment,
      status: 'agendada',
      session: existingSession,
    };
    prismaMock.appointment.findFirst.mockResolvedValue(existing);
    prismaMock.appointment.findMany.mockResolvedValue([]);
    prismaMock.appointment.update.mockResolvedValue({ ...baseAppointment, status: 'realizada' });
    prismaMock.session.findFirst.mockResolvedValue(existingSession);

    await appointmentsService.update(appointmentId, userId, { status: 'realizada' }, clinicId);

    expect(prismaMock.session.create).not.toHaveBeenCalled();
    expect(prismaMock.payment.create).not.toHaveBeenCalled();
  });

  it('concorrência: múltiplos updates simultâneos devem ser tratados', async () => {
    prismaMock.appointment.findFirst.mockResolvedValue({
      ...baseAppointment,
      status: 'agendada',
      session: null,
    });
    prismaMock.appointment.findMany.mockResolvedValue([]);
    prismaMock.session.findFirst.mockResolvedValue(null);
    prismaMock.appointment.update.mockResolvedValue({ ...baseAppointment, status: 'realizada' });

    const newSession = { id: 'session-1', appointmentId, patientId, userId };
    prismaMock.session.create.mockResolvedValue(newSession);
    prismaMock.payment.aggregate.mockResolvedValue({ _max: { sessionNumber: 0 } });
    prismaMock.payment.create.mockResolvedValue({ id: 'pay-1' });

    const calls = [
      appointmentsService.update(appointmentId, userId, { status: 'realizada' }, clinicId),
      appointmentsService.update(appointmentId, userId, { status: 'realizada' }, clinicId),
    ];

    const results = await Promise.all(calls);
    expect(results).toHaveLength(2);
    expect(prismaMock.$transaction).toHaveBeenCalled();
  });
});
