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
    console.log('JWT Payload:', payload);
    const user = await this.prisma.user.findUnique({
      where: { id: payload.sub },
      select: { id: true, email: true, clinicId: true },
    });

    if (!user) {
      throw new UnauthorizedException();
    }

    // clinicId: payload (switch-clinic) > user.clinicId > primeira ClinicUser
    let clinicId = payload.clinicId ?? user.clinicId ?? null;
    if (!clinicId) {
      const clinicMembership = await this.prisma.clinicUser.findFirst({
        where: { userId: user.id, status: 'active' },
        select: { clinicId: true },
        orderBy: { joinedAt: 'asc' },
      });
      clinicId = clinicMembership?.clinicId ?? null;
    }

    return {
      id: user.id,
      sub: user.id,
      email: user.email,
      clinicId,
    };
  }
}




