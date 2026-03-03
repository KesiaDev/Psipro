import {
  ConflictException,
  ForbiddenException,
  Injectable,
  UnauthorizedException,
} from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import * as bcrypt from 'bcrypt';
import { PrismaService } from '../prisma/prisma.service';
import { AuditService, type AuditRequest } from '../audit/audit.service';
import { RefreshTokenService } from './refresh-token.service';
import { LoginDto } from './dto/login.dto';
import { RegisterDto } from './dto/register.dto';

const ACCESS_TOKEN_EXPIRY = '15m';
const BCRYPT_SALT_ROUNDS = 12;
const MAX_LOGIN_ATTEMPTS = 5;
const LOCK_DURATION_MS = 15 * 60 * 1000; // 15 min

type AuthMeRole = 'ADMIN' | 'USER';

@Injectable()
export class AuthService {
  constructor(
    private prisma: PrismaService,
    private jwtService: JwtService,
    private refreshTokenService: RefreshTokenService,
    private auditService: AuditService,
  ) {}

  async validateUser(email: string, password: string): Promise<any> {
    const user = await this.prisma.user.findUnique({
      where: { email },
    });

    if (!user) {
      throw new UnauthorizedException('Credenciais inválidas');
    }

    const now = new Date();
    if (user.lockUntil && user.lockUntil > now) {
      const minutos = Math.ceil((user.lockUntil.getTime() - now.getTime()) / 60_000);
      throw new UnauthorizedException(
        `Conta temporariamente bloqueada. Tente novamente em ${minutos} minuto(s).`,
      );
    }

    const isPasswordValid = await bcrypt.compare(password, user.password);
    if (!isPasswordValid) {
      const attempts = (user.failedLoginAttempts ?? 0) + 1;
      const lockUntil = attempts >= MAX_LOGIN_ATTEMPTS ? new Date(now.getTime() + LOCK_DURATION_MS) : null;
      await this.prisma.user.update({
        where: { id: user.id },
        data: {
          failedLoginAttempts: attempts,
          ...(lockUntil && { lockUntil }),
        },
      });
      throw new UnauthorizedException('Credenciais inválidas');
    }

    if (user.failedLoginAttempts > 0 || user.lockUntil) {
      await this.prisma.user.update({
        where: { id: user.id },
        data: { failedLoginAttempts: 0, lockUntil: null },
      });
    }

    const { password: _, ...result } = user;
    return result;
  }

  async login(
    loginDto: LoginDto,
    options?: { deviceInfo?: string; ipAddress?: string; request?: AuditRequest },
  ) {
    const user = await this.validateUser(loginDto.email, loginDto.password);

    const payload = { email: user.email, sub: user.id };
    const accessToken = this.jwtService.sign(payload, { expiresIn: ACCESS_TOKEN_EXPIRY });
    const refreshToken = await this.refreshTokenService.createRefreshToken(
      user.id,
      user.clinicId ?? undefined,
      options,
    );

    this.auditService.log({
      userId: user.id,
      clinicId: user.clinicId ?? '',
      action: 'login',
      entity: 'User',
      entityId: user.id,
      metadata: { email: user.email },
      request: options?.request,
    }).catch(() => {});

    return {
      accessToken,
      access_token: accessToken,
      refreshToken,
      user: {
        id: user.id,
        email: user.email,
        name: user.name,
        fullName: user.name,
      },
    };
  }

  async refresh(refreshToken: string) {
    const record = await this.refreshTokenService.validateRefreshToken(refreshToken);
    const payload = { sub: record.userId };
    const accessToken = this.jwtService.sign(payload, { expiresIn: ACCESS_TOKEN_EXPIRY });
    const newRefreshToken = await this.refreshTokenService.rotateRefreshToken(refreshToken);

    return {
      accessToken,
      refreshToken: newRefreshToken,
    };
  }

  async logout(refreshToken: string, request?: AuditRequest): Promise<void> {
    const tokenInfo = await this.refreshTokenService.getTokenInfo(refreshToken);
    await this.refreshTokenService.revokeToken(refreshToken);
    if (tokenInfo) {
      this.auditService.log({
        userId: tokenInfo.userId,
        clinicId: tokenInfo.clinicId ?? '',
        action: 'logout',
        entity: 'User',
        entityId: tokenInfo.userId,
        request,
      }).catch(() => {});
    }
  }

  async register(registerDto: RegisterDto) {
    const existing = await this.prisma.user.findUnique({
      where: { email: registerDto.email },
      select: { id: true },
    });

    if (existing) {
      throw new ConflictException('Email já cadastrado');
    }

    const hashedPassword = await bcrypt.hash(registerDto.password, BCRYPT_SALT_ROUNDS);

    // ETAPA 5: Se não vier clinic, criar Clinic com planType INDIVIDUAL (psicólogo individual)
    const clinic = await this.prisma.clinic.create({
      data: {
        name: registerDto.fullName,
        email: registerDto.email,
        plan: 'basic',
        planType: 'INDIVIDUAL',
        status: 'active',
      },
    });

    const user = await this.prisma.user.create({
      data: {
        email: registerDto.email,
        name: registerDto.fullName,
        password: hashedPassword,
        isIndependent: true,
        clinicId: clinic.id,
        role: 'OWNER',
      },
      select: {
        id: true,
        email: true,
        name: true,
      },
    });

    const payload = { email: user.email, sub: user.id };
    const accessToken = this.jwtService.sign(payload, { expiresIn: ACCESS_TOKEN_EXPIRY });
    const refreshToken = await this.refreshTokenService.createRefreshToken(
      user.id,
      clinic.id,
    );

    return {
      accessToken,
      access_token: accessToken,
      refreshToken,
      user: {
        id: user.id,
        email: user.email,
        name: user.name,
        fullName: user.name,
      },
    };
  }

  /**
   * Fonte única de identidade do PsiPro.
   * Android e Web consomem o mesmo contrato mínimo aqui: GET /auth/me.
   */
  async validateToken(userId: string) {
    const user = await this.prisma.user.findUnique({
      where: { id: userId },
      select: {
        id: true,
        email: true,
        name: true,
        isIndependent: true,
        clinicId: true,
        role: true,
      },
    });

    if (!user) {
      throw new UnauthorizedException('Usuário não encontrado');
    }

    // clinicId: preferir user.clinicId (clínica principal) depois ClinicUser
    let clinicId = user.clinicId ?? null;
    let clinicUserRole: string | null = null;
    if (!clinicId) {
      const clinicMembership = await this.prisma.clinicUser.findFirst({
        where: { userId: user.id, status: 'active' },
        select: { clinicId: true, role: true },
        orderBy: { joinedAt: 'asc' },
      });
      clinicId = clinicMembership?.clinicId ?? null;
      if (clinicMembership) {
        clinicUserRole = clinicMembership.role;
      }
    }

    const role: AuthMeRole =
      user.role === 'OWNER' ||
      clinicUserRole === 'owner' ||
      clinicUserRole === 'admin'
        ? 'ADMIN'
        : 'USER';

    return {
      id: user.id,
      email: user.email,
      role,
      clinicId,
      name: user.name,
    };
  }

  /**
   * POST /auth/switch-clinic
   * Troca a clínica ativa do usuário. Gera novo accessToken com clinicId atualizado.
   * Não altera refreshToken.
   */
  async switchClinic(userId: string, clinicId: string, request?: AuditRequest) {
    const membership = await this.prisma.clinicUser.findUnique({
      where: {
        clinicId_userId: { clinicId, userId },
      },
    });

    if (!membership || membership.status !== 'active') {
      throw new ForbiddenException('Usuário não pertence a esta clínica');
    }

    const payload = { sub: userId, clinicId };
    const accessToken = this.jwtService.sign(payload, { expiresIn: ACCESS_TOKEN_EXPIRY });

    this.auditService.log({
      userId,
      clinicId,
      action: 'switch_clinic',
      entity: 'Clinic',
      entityId: clinicId,
      request,
    }).catch(() => {});

    return {
      accessToken,
      clinicId,
    };
  }

  /**
   * Endpoint de handoff (SSO) Android -> Web.
   * - Não cria token novo
   * - Não altera claims
   * - Apenas valida assinatura/expiração e devolve o payload mínimo do usuário
   */
  async handoff(token: string) {
    try {
      const payload: any = await this.jwtService.verifyAsync(token);
      if (!payload?.sub) {
        throw new UnauthorizedException('Token inválido');
      }

      const user = await this.validateToken(String(payload.sub));

      return {
        token,
        user,
      };
    } catch (err) {
      // Padronizar para 401 sem vazar detalhes do verificador JWT
      throw new UnauthorizedException('Token inválido');
    }
  }
}




