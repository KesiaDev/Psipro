import { Test, TestingModule } from '@nestjs/testing';
import { NotFoundException } from '@nestjs/common';
import { PatientsService } from '../../src/patients/patients.service';
import { PrismaService } from '../../src/prisma/prisma.service';
import { AuditService } from '../../src/audit/audit.service';
import { createPrismaMock } from '../utils/prisma-mock';

describe('Multi-tenant: isolamento de dados', () => {
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

  it('usuário não deve acessar pacientes de outra clínica', async () => {
    prismaMock.patient.findMany.mockResolvedValue([]);
    prismaMock.patient.findFirst.mockResolvedValue(null);

    const result = await patientsService.findAll('clinic-A');
    expect(result).toEqual([]);
    expect(prismaMock.patient.findMany).toHaveBeenCalledWith(
      expect.objectContaining({
        where: expect.objectContaining({ clinicId: 'clinic-A', deletedAt: null }),
      }),
    );

    prismaMock.patient.findFirst.mockResolvedValue(null);
    await expect(patientsService.findOne('patient-1', 'clinic-B')).rejects.toThrow(NotFoundException);
  });

  it('findOne com clinicId errado retorna 404', async () => {
    prismaMock.patient.findFirst.mockResolvedValue(null);

    await expect(patientsService.findOne('patient-id', 'outra-clinica')).rejects.toThrow(NotFoundException);
  });
});
