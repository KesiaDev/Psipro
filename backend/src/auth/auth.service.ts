import {
  ConflictException,
  ForbiddenException,
  Injectable,
  UnauthorizedException,
} from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { JwtService } from '@nestjs/jwt';
import * as bcrypt from 'bcrypt';
import * as crypto from 'crypto';
import { PrismaService } from '../prisma/prisma.service';
import { AuditService, type AuditRequest } from '../audit/audit.service';
import { RefreshTokenService } from './refresh-token.service';
import { LoginDto } from './dto/login.dto';
import { RegisterDto } from './dto/register.dto';

const ACCESS_TOKEN_EXPIRY = '15m';
const BCRYPT_SALT_ROUNDS = 12;
const MAX_LOGIN_ATTEMPTS = 5;
const LOCK_DURATION_MS = 15 * 60 * 1000; // 15 min
const HANDOFF_TOKEN_EXPIRY_SEC = 30;

type AuthMeRole = 'ADMIN' | 'USER';

@Injectable()
export class AuthService {
  constructor(
    private prisma: PrismaService,
    private jwtService: JwtService,
    private refreshTokenService: RefreshTokenService,
    private auditService: AuditService,
    private config: ConfigService,
  ) {}

  async validateUser(email: string, password: string): Promise<any> {
    const emailNorm = email.trim().toLowerCase();
    const user = await this.prisma.user.findFirst({
      where: { email: { equals: emailNorm, mode: 'insensitive' } },
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

    const professionalType = user.professionalType ?? 'psychologist';
    return {
      accessToken,
      access_token: accessToken,
      refreshToken,
      user: {
        id: user.id,
        email: user.email,
        name: user.name,
        fullName: user.name,
        role: user.role,
        professionalType,
        clinicId: user.clinicId ?? undefined,
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
    const email = registerDto.email.trim().toLowerCase();
    const existing = await this.prisma.user.findFirst({
      where: { email: { equals: email, mode: 'insensitive' } },
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
        email,
        plan: 'basic',
        planType: 'INDIVIDUAL',
        status: 'active',
      },
    });

    const user = await this.prisma.user.create({
      data: {
        email,
        name: registerDto.fullName,
        password: hashedPassword,
        isIndependent: true,
        clinicId: clinic.id,
        role: 'OWNER',
        ...(registerDto.professionalType && { professionalType: registerDto.professionalType }),
      },
      select: {
        id: true,
        email: true,
        name: true,
        professionalType: true,
      },
    });

    // Criar ClinicUser para GET /clinics retornar a clínica do usuário
    await this.prisma.clinicUser.upsert({
      where: {
        clinicId_userId: { clinicId: clinic.id, userId: user.id },
      },
      create: {
        clinicId: clinic.id,
        userId: user.id,
        role: 'owner',
        status: 'active',
        canViewAllPatients: true,
        canEditAllPatients: true,
        canViewFinancial: true,
        canManageUsers: true,
      },
      update: { role: 'owner', status: 'active' },
    });

    const payload = { email: user.email, sub: user.id };
    const accessToken = this.jwtService.sign(payload, { expiresIn: ACCESS_TOKEN_EXPIRY });
    const refreshToken = await this.refreshTokenService.createRefreshToken(
      user.id,
      clinic.id,
    );

    const professionalType = user.professionalType ?? 'psychologist';
    return {
      accessToken,
      access_token: accessToken,
      refreshToken,
      user: {
        id: user.id,
        email: user.email,
        name: user.name,
        fullName: user.name,
        role: 'OWNER',
        professionalType,
        clinicId: clinic.id,
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
        professionalType: true,
        lgpdAcceptedAt: true,
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

    // Compatibilidade: usuários antigos sem professionalType assumem psychologist
    const professionalType = user.professionalType ?? 'psychologist';

    return {
      id: user.id,
      name: user.name,
      role,
      professionalType,
      email: user.email,
      clinicId,
      lgpdAcceptedAt: user.lgpdAcceptedAt?.toISOString() ?? null,
    };
  }

  /**
   * POST /auth/consent
   * Registra aceite do termo LGPD pelo usuário.
   */
  async recordLgpdConsent(userId: string) {
    await this.prisma.user.update({
      where: { id: userId },
      data: { lgpdAcceptedAt: new Date() },
    });
    return { success: true, message: 'Consentimento LGPD registrado' };
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
   * Atualiza perfil do usuário (nome, email, phone, license).
   */
  async updateProfile(userId: string, data: { name?: string; email?: string; phone?: string; license?: string }) {
    const updateData: Record<string, unknown> = {};
    if (data.name != null) updateData.name = data.name;
    if (data.email != null) updateData.email = data.email;
    if (data.phone != null) updateData.phone = data.phone;
    if (data.license != null) updateData.license = data.license;

    if (Object.keys(updateData).length === 0) {
      return this.validateToken(userId);
    }

    if (data.email) {
      const existing = await this.prisma.user.findFirst({
        where: { email: data.email, id: { not: userId } },
      });
      if (existing) {
        throw new ConflictException('Email já está em uso');
      }
    }

    await this.prisma.user.update({
      where: { id: userId },
      data: updateData,
    });

    return this.validateToken(userId);
  }

  /**
   * Altera senha do usuário.
   */
  async changePassword(userId: string, currentPassword: string, newPassword: string) {
    const user = await this.prisma.user.findUnique({
      where: { id: userId },
      select: { password: true },
    });
    if (!user) {
      throw new UnauthorizedException('Usuário não encontrado');
    }

    const isValid = await bcrypt.compare(currentPassword, user.password);
    if (!isValid) {
      throw new UnauthorizedException('Senha atual incorreta');
    }

    const hashed = await bcrypt.hash(newPassword, BCRYPT_SALT_ROUNDS);
    await this.prisma.user.update({
      where: { id: userId },
      data: { password: hashed },
    });

    return { success: true };
  }

  /**
   * Recuperação de senha. Por segurança, não revela se o e-mail existe.
   * Quando o serviço de e-mail estiver configurado, gerar token e enviar link.
   */
  async forgotPassword(email: string): Promise<void> {
    const emailNorm = email.trim().toLowerCase();
    const user = await this.prisma.user.findFirst({
      where: { email: { equals: emailNorm, mode: 'insensitive' } },
      select: { id: true, email: true },
    });
    if (user) {
      // TODO: gerar token de reset, salvar em tabela, enviar e-mail
      // Por ora, apenas log para auditoria
      this.auditService.log({
        userId: user.id,
        clinicId: 'system',
        action: 'forgot_password_requested',
        entity: 'User',
        entityId: user.id,
        metadata: { email: user.email },
      }).catch(() => {});
    }
  }

  /**
   * Endpoint de handoff (SSO) Android -> Web.
   *
   * Dois fluxos:
   * 1) JWT (App): Valida JWT, cria handoff token single-use (30s), retorna redirectUrl
   * 2) Handoff token (Web): Valida, marca usado, retorna accessToken + refreshToken + user
   */
  async handoff(token: string, returnUrl?: string) {
    const isJwt = this.looksLikeJwt(token);

    if (isJwt) {
      return this.handoffCreateFromJwt(token, returnUrl ?? '/dashboard');
    }
    return this.handoffExchange(token);
  }

  private looksLikeJwt(str: string): boolean {
    const parts = str.split('.');
    return parts.length === 3 && parts.every((p) => /^[A-Za-z0-9_-]+$/.test(p));
  }

  /**
   * App envia JWT → criar handoff token (30s, single-use) e retornar redirectUrl
   */
  private async handoffCreateFromJwt(jwt: string, returnUrl: string) {
    try {
      const payload: { sub?: string } = await this.jwtService.verifyAsync(jwt);
      if (!payload?.sub) {
        throw new UnauthorizedException('Token inválido');
      }

      const user = await this.validateToken(String(payload.sub));
      const clinicId = user.clinicId ?? null;

      const handoffToken = crypto.randomBytes(32).toString('hex');
      const expiresAt = new Date(Date.now() + HANDOFF_TOKEN_EXPIRY_SEC * 1000);

      await this.prisma.handoffToken.create({
        data: {
          token: handoffToken,
          userId: user.id,
          clinicId,
          expiresAt,
        },
      });

      const dashboardUrl = this.config.get<string>('DASHBOARD_URL', 'https://psipro-dashboard-production.up.railway.app');
      const base = dashboardUrl.replace(/\/+$/, '');
      const safeReturn =
        returnUrl && returnUrl.startsWith('/') && !returnUrl.includes('//')
          ? returnUrl
          : '/dashboard';
      const redirectUrl = `${base}/login?token=${encodeURIComponent(handoffToken)}&returnUrl=${encodeURIComponent(safeReturn)}`;

      return {
        handoffToken,
        redirectUrl,
      };
    } catch (err) {
      if (err instanceof UnauthorizedException) throw err;
      throw new UnauthorizedException('Token inválido');
    }
  }

  /**
   * Web envia handoff token → trocar por accessToken + refreshToken + user
   */
  private async handoffExchange(handoffToken: string) {
    const now = new Date();
    const record = await this.prisma.handoffToken.findUnique({
      where: { token: handoffToken },
      include: { user: true },
    });

    if (!record) {
      throw new UnauthorizedException('Token inválido');
    }
    if (record.usedAt) {
      throw new UnauthorizedException('Token já utilizado');
    }
    if (record.expiresAt < now) {
      throw new UnauthorizedException('Token expirado');
    }

    await this.prisma.handoffToken.update({
      where: { id: record.id },
      data: { usedAt: now },
    });

    const userId = record.userId;
    const clinicId = record.clinicId ?? undefined;

    const payload = { sub: userId, ...(clinicId && { clinicId }) };
    const accessToken = this.jwtService.sign(payload, { expiresIn: ACCESS_TOKEN_EXPIRY });
    const refreshToken = await this.refreshTokenService.createRefreshToken(userId, clinicId ?? undefined);

    const user = await this.validateToken(userId);
    const professionalType = record.user.professionalType ?? 'psychologist';

    return {
      accessToken,
      refreshToken,
      user: {
        id: user.id,
        email: user.email,
        name: user.name,
        fullName: user.name,
        role: user.role,
        professionalType,
        clinicId: user.clinicId ?? undefined,
      },
    };
  }
}




