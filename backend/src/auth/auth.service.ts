import { Injectable, UnauthorizedException } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import * as bcrypt from 'bcrypt';
import { PrismaService } from '../prisma/prisma.service';
import { LoginDto } from './dto/login.dto';

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
      },
    });

    if (!user) {
      throw new UnauthorizedException('Usuário não encontrado');
    }

    // Se o usuário pertence a uma clínica ativa, usamos a primeira como "contexto" padrão.
    // Isso evita quebra de clientes existentes e permite que o backend seja a fonte única de verdade.
    const clinicMembership = await this.prisma.clinicUser.findFirst({
      where: {
        userId: user.id,
        status: 'active',
      },
      select: {
        clinicId: true,
        role: true,
        joinedAt: true,
      },
      orderBy: {
        joinedAt: 'asc',
      },
    });

    const role: AuthMeRole =
      clinicMembership && ['owner', 'admin'].includes(clinicMembership.role) ? 'ADMIN' : 'USER';

    return {
      id: user.id,
      email: user.email,
      role,
      clinicId: clinicMembership?.clinicId ?? null,
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




