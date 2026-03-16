import { Test, TestingModule } from '@nestjs/testing';
import { ExecutionContext, ForbiddenException } from '@nestjs/common';
import { Reflector } from '@nestjs/core';
import { RolesGuard } from '../../src/common/guards/roles.guard';
import { PrismaService } from '../../src/prisma/prisma.service';
import { ROLES_KEY } from '../../src/common/decorators/roles.decorator';
import { createPrismaMock } from '../utils/prisma-mock';

describe('RolesGuard (Multi-tenant)', () => {
  let guard: RolesGuard;
  let prismaMock: ReturnType<typeof createPrismaMock>;
  let reflector: Reflector;

  function createMockContext(user: unknown, clinicId?: string, params?: Record<string, string>): ExecutionContext {
    return {
      switchToHttp: () => ({
        getRequest: () => ({ user, clinicId, params: params ?? {} }),
      }),
      getHandler: () => ({}),
      getClass: () => ({}),
    } as unknown as ExecutionContext;
  }

  beforeEach(async () => {
    prismaMock = createPrismaMock();

    const module: TestingModule = await Test.createTestingModule({
      providers: [
        RolesGuard,
        Reflector,
        { provide: PrismaService, useValue: prismaMock },
      ],
    }).compile();

    guard = module.get<RolesGuard>(RolesGuard);
    reflector = module.get<Reflector>(Reflector);
    jest.clearAllMocks();
  });

  it('admin/owner pode acessar endpoints protegidos com role Admin', async () => {
    jest.spyOn(reflector, 'getAllAndOverride').mockReturnValue(['admin']);
    prismaMock.clinicUser.findUnique.mockResolvedValue({ role: 'admin', status: 'active' });

    const ctx = createMockContext({ id: 'u1', sub: 'u1' }, 'clinic-1');
    const result = await guard.canActivate(ctx);
    expect(result).toBe(true);
  });

  it('psychologist não pode acessar endpoints admin-only', async () => {
    jest.spyOn(reflector, 'getAllAndOverride').mockReturnValue(['admin']);
    prismaMock.clinicUser.findUnique.mockResolvedValue({ role: 'psychologist', status: 'active' });

    const ctx = createMockContext({ id: 'u1', sub: 'u1' }, 'clinic-1');
    await expect(guard.canActivate(ctx)).rejects.toThrow(ForbiddenException);
  });

  it('assistant não pode acessar endpoints admin-only', async () => {
    jest.spyOn(reflector, 'getAllAndOverride').mockReturnValue(['admin']);
    prismaMock.clinicUser.findUnique.mockResolvedValue({ role: 'assistant', status: 'active' });

    const ctx = createMockContext({ id: 'u1', sub: 'u1' }, 'clinic-1');
    await expect(guard.canActivate(ctx)).rejects.toThrow(ForbiddenException);
  });

  it('psychologist pode acessar endpoints que exigem psychologist', async () => {
    jest.spyOn(reflector, 'getAllAndOverride').mockReturnValue(['psychologist']);
    prismaMock.clinicUser.findUnique.mockResolvedValue({ role: 'psychologist', status: 'active' });

    const ctx = createMockContext({ id: 'u1', sub: 'u1' }, 'clinic-1');
    const result = await guard.canActivate(ctx);
    expect(result).toBe(true);
  });

  it('sem roles definidos permite acesso', async () => {
    jest.spyOn(reflector, 'getAllAndOverride').mockReturnValue(undefined);

    const ctx = createMockContext({ id: 'u1', sub: 'u1' }, 'clinic-1');
    const result = await guard.canActivate(ctx);
    expect(result).toBe(true);
  });
});
