import { Test, TestingModule } from '@nestjs/testing';
import { JwtModule } from '@nestjs/jwt';
import { ConfigModule } from '@nestjs/config';
import { UnauthorizedException, ForbiddenException } from '@nestjs/common';
import * as bcrypt from 'bcrypt';
import { AuthService } from '../../src/auth/auth.service';
import { RefreshTokenService } from '../../src/auth/refresh-token.service';
import { PrismaService } from '../../src/prisma/prisma.service';
import { AuditService } from '../../src/audit/audit.service';
import { createPrismaMock } from '../utils/prisma-mock';

describe('AuthService', () => {
  let authService: AuthService;
  let prismaMock: ReturnType<typeof createPrismaMock>;

  const hashedPassword = bcrypt.hashSync('senha12345', 10);
  const validUser = {
    id: 'user-1',
    email: 'test@test.com',
    name: 'Test User',
    password: hashedPassword,
    clinicId: 'clinic-1',
    role: 'OWNER',
    failedLoginAttempts: 0,
    lockUntil: null,
  };

  beforeEach(async () => {
    prismaMock = createPrismaMock();

    const module: TestingModule = await Test.createTestingModule({
      imports: [
        ConfigModule.forRoot({ isGlobal: true }),
        JwtModule.register({ secret: 'test-secret', signOptions: { expiresIn: '15m' } }),
      ],
      providers: [
        AuthService,
        { provide: PrismaService, useValue: prismaMock },
        {
          provide: RefreshTokenService,
          useValue: {
            createRefreshToken: jest.fn().mockResolvedValue('refresh-token'),
          },
        },
        { provide: AuditService, useValue: { log: jest.fn().mockResolvedValue(undefined) } },
      ],
    }).compile();

    authService = module.get<AuthService>(AuthService);
    jest.clearAllMocks();
  });

  describe('Login', () => {
    it('deve realizar login com sucesso', async () => {
      prismaMock.user.findUnique.mockResolvedValue(validUser);

      const result = await authService.login({
        email: 'test@test.com',
        password: 'senha12345',
      });

      expect(result).toHaveProperty('accessToken');
      expect(result).toHaveProperty('refreshToken');
      expect(result.user.email).toBe('test@test.com');
    });

    it('deve bloquear login após 5 falhas consecutivas', async () => {
      const lockedUser = {
        ...validUser,
        failedLoginAttempts: 5,
        lockUntil: new Date(Date.now() + 15 * 60 * 1000),
      };
      prismaMock.user.findUnique.mockResolvedValue(lockedUser);

      await expect(
        authService.login({ email: 'test@test.com', password: 'senha12345' }),
      ).rejects.toThrow(/bloqueada/);
    });
  });

  describe('Refresh token', () => {
    it('refresh token válido deve retornar novos tokens', async () => {
      const moduleRef = await Test.createTestingModule({
        imports: [JwtModule.register({ secret: 'test-secret', signOptions: { expiresIn: '15m' } })],
        providers: [
          AuthService,
          { provide: PrismaService, useValue: prismaMock },
          {
            provide: RefreshTokenService,
            useValue: {
              validateRefreshToken: jest.fn().mockResolvedValue({ userId: 'user-1', clinicId: 'clinic-1' }),
              rotateRefreshToken: jest.fn().mockResolvedValue('new-refresh-token'),
              createRefreshToken: jest.fn(),
              revokeToken: jest.fn(),
              getTokenInfo: jest.fn(),
            },
          },
          { provide: AuditService, useValue: { log: jest.fn().mockResolvedValue(undefined) } },
        ],
      }).compile();

      const svc = moduleRef.get<AuthService>(AuthService);
      const result = await svc.refresh('valid-refresh-token');
      expect(result.accessToken).toBeDefined();
      expect(result.refreshToken).toBe('new-refresh-token');
    });

    it('refresh token revogado deve lançar UnauthorizedException', async () => {
      const moduleRef = await Test.createTestingModule({
        imports: [JwtModule.register({ secret: 'test-secret', signOptions: { expiresIn: '15m' } })],
        providers: [
          AuthService,
          { provide: PrismaService, useValue: prismaMock },
          {
            provide: RefreshTokenService,
            useValue: {
              validateRefreshToken: jest.fn().mockRejectedValue(new UnauthorizedException('Token revogado')),
              rotateRefreshToken: jest.fn(),
              createRefreshToken: jest.fn(),
              revokeToken: jest.fn(),
              getTokenInfo: jest.fn(),
            },
          },
          { provide: AuditService, useValue: { log: jest.fn().mockResolvedValue(undefined) } },
        ],
      }).compile();

      const svc = moduleRef.get<AuthService>(AuthService);
      await expect(svc.refresh('revoked-token')).rejects.toThrow(UnauthorizedException);
    });
  });

  describe('Switch clinic', () => {
    it('switch clinic inválido deve lançar ForbiddenException', async () => {
      prismaMock.clinicUser.findUnique.mockResolvedValue(null);

      await expect(authService.switchClinic('user-1', 'clinic-outra')).rejects.toThrow(ForbiddenException);
    });

    it('switch clinic com membership inativo deve lançar ForbiddenException', async () => {
      prismaMock.clinicUser.findUnique.mockResolvedValue({ status: 'inactive' });

      await expect(authService.switchClinic('user-1', 'clinic-1')).rejects.toThrow(ForbiddenException);
    });

    it('switch clinic válido deve retornar novo accessToken', async () => {
      prismaMock.clinicUser.findUnique.mockResolvedValue({ clinicId: 'clinic-1', userId: 'user-1', status: 'active' });

      const result = await authService.switchClinic('user-1', 'clinic-1');
      expect(result.accessToken).toBeDefined();
      expect(result.clinicId).toBe('clinic-1');
    });
  });
});
