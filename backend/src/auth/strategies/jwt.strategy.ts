import { Injectable, UnauthorizedException } from '@nestjs/common';
import { PassportStrategy } from '@nestjs/passport';
import { ExtractJwt, Strategy } from 'passport-jwt';
import { ConfigService } from '@nestjs/config';
import { PrismaService } from '../../prisma/prisma.service';

@Injectable()
export class JwtStrategy extends PassportStrategy(Strategy) {
  constructor(
    private configService: ConfigService,
    private prisma: PrismaService,
  ) {
    super({
      jwtFromRequest: ExtractJwt.fromAuthHeaderAsBearerToken(),
      ignoreExpiration: false,
      secretOrKey: configService.get<string>('JWT_SECRET'),
    });
  }

  async validate(payload: any) {
    const user = await this.prisma.user.findUnique({
      where: { id: payload.sub },
      select: { id: true, email: true, clinicId: true, role: true },
    });

    if (!user) {
      throw new UnauthorizedException();
    }

    // clinicId para isolamento multi-tenant (ETAPA 4)
    let clinicId = user.clinicId;
    if (!clinicId) {
      const cu = await this.prisma.clinicUser.findFirst({
        where: { userId: user.id, status: 'active' },
        select: { clinicId: true },
      });
      clinicId = cu?.clinicId ?? undefined;
    }

    return {
      id: user.id,
      sub: user.id,
      email: user.email,
      clinicId: clinicId ?? undefined,
      role: user.role,
    };
  }
}




