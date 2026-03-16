import { Test, TestingModule } from '@nestjs/testing';
import { PatientsService } from '../../src/patients/patients.service';
import { PrismaService } from '../../src/prisma/prisma.service';
import { AuditService } from '../../src/audit/audit.service';
import { createPrismaMock } from '../utils/prisma-mock';

describe('Soft delete', () => {
  let patientsService: PatientsService;
  let prismaMock: ReturnType<typeof createPrismaMock>;

  beforeEach(async () => {
    prismaMock = createPrismaMock();

    const module: TestingModule = await Test.createTestingModule({
      providers: [
        PatientsService,
        { provide: PrismaService, useValue: prismaMock },
        { provide: AuditService, useValue: { log: jest.fn().mockResolvedValue(undefined) } },
      ],
    }).compile();

    patientsService = module.get<PatientsService>(PatientsService);
    jest.clearAllMocks();
  });

  it('registros deletados (deletedAt set) não aparecem em findAll', async () => {
    const activePatient = {
      id: 'p1',
      name: 'Ativo',
      clinicId: 'c1',
      deletedAt: null,
    };
    prismaMock.patient.findMany.mockResolvedValue([activePatient]);

    const result = await patientsService.findAll('c1');
    expect(result).toHaveLength(1);
    expect(prismaMock.patient.findMany).toHaveBeenCalledWith(
      expect.objectContaining({
        where: expect.objectContaining({ deletedAt: null, clinicId: 'c1' }),
      }),
    );
  });

  it('delete usa update (soft delete) ao invés de deleteMany', async () => {
    prismaMock.patient.findFirst.mockResolvedValue({ id: 'p1', name: 'Test', clinicId: 'c1' });
    prismaMock.patient.updateMany.mockResolvedValue({ count: 1 });

    await patientsService.delete('p1', 'c1', 'user-1');

    expect(prismaMock.patient.updateMany).toHaveBeenCalledWith(
      expect.objectContaining({
        where: { id: 'p1', clinicId: 'c1' },
        data: expect.objectContaining({ deletedAt: expect.any(Date) }),
      }),
    );
    expect(prismaMock.patient.deleteMany).not.toHaveBeenCalled();
  });

  it('hard delete bloqueado em produção', async () => {
    const originalEnv = process.env.NODE_ENV;
    process.env.NODE_ENV = 'production';

    try {
      const { PrismaService: RealPrisma } = await import('../../src/prisma/prisma.service');
      const prisma = new (RealPrisma as unknown as new () => InstanceType<typeof RealPrisma>)();

      await expect(
        (prisma as unknown as { patient: { deleteMany: (args: unknown) => Promise<unknown> } }).patient.deleteMany({
          where: { id: 'x' },
        }),
      ).rejects.toThrow(/bloqueado em produção/);
    } finally {
      process.env.NODE_ENV = originalEnv;
    }
  });
});
