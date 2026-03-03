import { Test, TestingModule } from '@nestjs/testing';
import { UnauthorizedException, ForbiddenException } from '@nestjs/common';
import { JwtModule } from '@nestjs/jwt';
import { ConfigModule } from '@nestjs/config';
import { AuthService } from '../src/auth/auth.service';
import { RefreshTokenService } from '../src/auth/refresh-token.service';
import { PrismaService } from '../src/prisma/prisma.service';
import { AuditService } from '../src/audit/audit.service';
import { hashPassword } from './helpers/test-utils';

describe('Auth (e2e)', () => {
  let authService: AuthService;
  let prisma: { user: any; refreshToken: any; clinicUser: any };
  const hashedPass = hashPassword('senha12345');

  const mockUser = (overrides: Partial<{ failedLoginAttempts: number; lockUntil: Date | null }> = {}) => ({
    id: 'u1',
    email: 'test@test.com',
    name: 'Test User',
    password: 'hashed',
    clinicId: 'c1',
    failedLoginAttempts: overrides.failedLoginAttempts ?? 0,
    lockUntil: overrides.lockUntil ?? null,
    role: 'OWNER',
  });

  beforeAll(async () => {
    const userFindUnique = jest.fn();
    const userUpdate = jest.fn();
    const refreshTokenCreate = jest.fn().mockResolvedValue({});
    const refreshTokenFindUnique = jest.fn();
    const refreshTokenUpdate = jest.fn().mockResolvedValue({});
    const refreshTokenUpdateMany = jest.fn().mockResolvedValue({});
    const refreshTokenFindFirst = jest.fn();
    const clinicUserFindUnique = jest.fn();

    prisma = {
      user: { findUnique: userFindUnique, update: userUpdate },
      refreshToken: {
        create: refreshTokenCreate,
        findUnique: refreshTokenFindUnique,
        findFirst: refreshTokenFindFirst,
        update: refreshTokenUpdate,
        updateMany: refreshTokenUpdateMany,
      },
      clinicUser: { findUnique: clinicUserFindUnique },
    } as any;

    const module: TestingModule = await Test.createTestingModule({
      imports: [
        ConfigModule.forRoot({ isGlobal: true }),
        JwtModule.register({ secret: 'test-secret-key-min-32-chars!!', signOptions: { expiresIn: '15m' } }),
      ],
      providers: [
        AuthService,
        RefreshTokenService,
        {
          provide: PrismaService,
          useValue: {
            user: prisma.user,
            refreshToken: prisma.refreshToken,
            clinicUser: prisma.clinicUser,
            $transaction: (arg: any) =>
              typeof arg === 'function' ? arg({ ...prisma }) : Promise.all(arg),
          },
        },
        { provide: AuditService, useValue: { log: jest.fn().mockResolvedValue(undefined) } },
      ],
    }).compile();

    authService = module.get(AuthService);
  });

  describe('Login bem-sucedido', () => {
    it('retorna accessToken e refreshToken', async () => {
      const h = await hashedPass;
      (prisma.user.findUnique as jest.Mock).mockResolvedValue({
        ...mockUser(),
        password: h,
      });
      (prisma.refreshToken.create as jest.Mock).mockResolvedValue({});
      (prisma.user.update as jest.Mock).mockResolvedValue({});

      const result = await authService.login({
        email: 'test@test.com',
        password: 'senha12345',
      });
      expect(result).toHaveProperty('accessToken');
      expect(result).toHaveProperty('refreshToken');
      expect(result.user.email).toBe('test@test.com');
    });
  });

  describe('Login bloqueado após 5 falhas', () => {
    it('bloqueia após 5 tentativas com senha errada', async () => {
      const h = await hashedPass;
      (prisma.user.findUnique as jest.Mock).mockResolvedValue({
        ...mockUser({ failedLoginAttempts: 4 }),
        password: h,
      });
      (prisma.user.update as jest.Mock).mockImplementation((args: any) => {
        const data = args.data;
        if (data.lockUntil) {
          (prisma.user.findUnique as jest.Mock).mockResolvedValue({
            ...mockUser({ failedLoginAttempts: 5, lockUntil: data.lockUntil }),
            password: h,
          });
        }
        return Promise.resolve({});
      });

      await expect(
        authService.login({ email: 'test@test.com', password: 'wrong' }),
      ).rejects.toThrow(UnauthorizedException);

      (prisma.user.findUnique as jest.Mock).mockResolvedValue({
        ...mockUser({ failedLoginAttempts: 5, lockUntil: new Date(Date.now() + 900000) }),
        password: h,
      });

      await expect(
        authService.login({ email: 'test@test.com', password: 'senha12345' }),
      ).rejects.toThrow(/Conta temporariamente bloqueada/);
    });
  });

  describe('Refresh token', () => {
    it('refresh token válido retorna novo accessToken', async () => {
      const validRecord = {
        id: 'rt1',
        userId: 'u1',
        clinicId: 'c1',
        token: require('crypto').createHash('sha256').update('valid-plain-token').digest('hex'),
        revoked: false,
        expiresAt: new Date(Date.now() + 86400000),
      };
      (prisma.refreshToken.findUnique as jest.Mock).mockImplementation((args: any) =>
        Promise.resolve(args?.where?.token === validRecord.token ? validRecord : null),
      );
      (prisma.refreshToken.create as jest.Mock).mockResolvedValue({});
      (prisma.refreshToken.updateMany as jest.Mock).mockResolvedValue({});

      const moduleRef = await Test.createTestingModule({
        imports: [JwtModule.register({ secret: 'test-secret-key-min-32-chars!!', signOptions: { expiresIn: '15m' } })],
        providers: [
          AuthService,
          RefreshTokenService,
          { provide: PrismaService, useValue: { refreshToken: prisma.refreshToken, $transaction: (x: any) => (Array.isArray(x) ? Promise.all(x) : x) } },
          { provide: AuditService, useValue: { log: jest.fn() } },
        ],
      }).compile();
      const authSvc = moduleRef.get(AuthService);
      const result = await authSvc.refresh('valid-plain-token');
      expect(result).toHaveProperty('accessToken');
      expect(result).toHaveProperty('refreshToken');
    });

    it('refresh token revogado lança UnauthorizedException', async () => {
      (prisma.refreshToken.findUnique as jest.Mock).mockResolvedValue({
        id: 'rt1',
        userId: 'u1',
        revoked: true,
        expiresAt: new Date(Date.now() + 86400000),
      });
      const moduleRef = await Test.createTestingModule({
        imports: [JwtModule.register({ secret: 'x', signOptions: { expiresIn: '15m' } })],
        providers: [
          AuthService,
          RefreshTokenService,
          { provide: PrismaService, useValue: { refreshToken: prisma.refreshToken, $transaction: (x: any) => (Array.isArray(x) ? Promise.all(x) : x) } },
          { provide: AuditService, useValue: { log: jest.fn() } },
        ],
      }).compile();
      const svc = moduleRef.get(RefreshTokenService);
      await expect(svc.validateRefreshToken('any')).rejects.toThrow(UnauthorizedException);
    });
  });

  describe('Switch clinic', () => {
    it('switch clinic inválido lança ForbiddenException', async () => {
      (prisma.clinicUser.findUnique as jest.Mock).mockResolvedValue(null);

      await expect(
        authService.switchClinic('u1', 'clinic-inexistente'),
      ).rejects.toThrow(ForbiddenException);
    });
  });
});
