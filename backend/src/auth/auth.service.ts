import { ConflictException, Injectable, UnauthorizedException } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import * as bcrypt from 'bcrypt';
import { PrismaService } from '../prisma/prisma.service';
import { LoginDto } from './dto/login.dto';
import { RegisterDto } from './dto/register.dto';

type AuthMeRole = 'ADMIN' | 'USER';

@Injectable()
export class AuthService {
  constructor(
    private prisma: PrismaService,
    private jwtService: JwtService,
  ) {}

  async validateUser(email: string, password: string): Promise<any> {
    const user = await this.prisma.user.findUnique({
      where: { email },
    });

    if (!user) {
      throw new UnauthorizedException('Credenciais inválidas');
    }

    const isPasswordValid = await bcrypt.compare(password, user.password);
    if (!isPasswordValid) {
      throw new UnauthorizedException('Credenciais inválidas');
    }

    const { password: _, ...result } = user;
    return result;
  }

  async login(loginDto: LoginDto) {
    const user = await this.validateUser(loginDto.email, loginDto.password);

    const payload = { email: user.email, sub: user.id };
    const accessToken = this.jwtService.sign(payload);

    return {
      access_token: accessToken,
      user: {
        id: user.id,
        email: user.email,
        name: user.name,
        fullName: user.name,
      },
    };
  }

  async register(registerDto: RegisterDto) {
    const existing = await this.prisma.user.findUnique({
      where: { email: registerDto.email },
      select: { id: true },
    });

    if (existing) {
      throw new ConflictException('Email já cadastrado');
    }

    const hashedPassword = await bcrypt.hash(registerDto.password, 10);

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
    const accessToken = this.jwtService.sign(payload);

    return {
      access_token: accessToken,
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




