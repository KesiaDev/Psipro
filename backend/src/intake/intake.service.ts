import {
  Injectable,
  BadRequestException,
  UnauthorizedException,
} from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { AuditService, AuditRequest } from '../audit/audit.service';
import { CreateIntakeDto } from './dto/create-intake.dto';
import { randomUUID } from 'crypto';

const INTAKE_TOKEN_TTL_DAYS = 7;

@Injectable()
export class IntakeService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly auditService: AuditService,
  ) {}

  async generateToken(userId: string, clinicId: string) {
    const expiresAt = new Date();
    expiresAt.setDate(expiresAt.getDate() + INTAKE_TOKEN_TTL_DAYS);

    const intakeToken = await this.prisma.intakeToken.create({
      data: {
        token: randomUUID(),
        clinicId,
        userId,
        expiresAt,
      },
    });

    const webAppUrl =
      process.env.WEB_APP_URL ||
      (process.env.RAILWAY_PUBLIC_DOMAIN
        ? `https://${process.env.RAILWAY_PUBLIC_DOMAIN}`
        : 'https://psipro-backend-production.up.railway.app');
    const link = `${webAppUrl}/intake?token=${intakeToken.token}`;

    return {
      token: intakeToken.token,
      link,
      expiresAt: intakeToken.expiresAt,
    };
  }

  async processIntake(
    token: string,
    dto: CreateIntakeDto,
    request?: AuditRequest,
  ) {
    if (!token) {
      throw new BadRequestException('Token de intake obrigatório');
    }

    if (!dto.consentGiven) {
      throw new BadRequestException(
        'Consentimento LGPD obrigatório para prosseguir',
      );
    }

    const intakeToken = await this.prisma.intakeToken.findUnique({
      where: { token },
    });

    if (!intakeToken) {
      throw new UnauthorizedException('Token inválido');
    }

    if (intakeToken.expiresAt < new Date()) {
      throw new BadRequestException('TOKEN_EXPIRED');
    }

    const patient = await this.prisma.patient.create({
      data: {
        name: dto.name,
        birthDate: dto.birthDate ? new Date(dto.birthDate) : null,
        gender: dto.gender ?? null,
        profession: dto.profession ?? null,
        email: dto.email ?? null,
        phone: dto.phone ?? null,
        anamnesis: dto.anamnesis
          ? ({
              items: dto.anamnesis.items,
              updatedAt: dto.anamnesis.updatedAt ?? new Date().toISOString(),
            } as object)
          : null,
        consentGiven: true,
        consentAt: new Date(),
        clinicId: intakeToken.clinicId,
        clinicOwnerId: intakeToken.userId,
        status: 'Ativo',
        source: 'web',
        origin: 'WEB',
      },
    });

    // Marcar token como usado (mas não bloquear — permite reenvio em caso de erro)
    await this.prisma.intakeToken.update({
      where: { id: intakeToken.id },
      data: { usedAt: new Date() },
    });

    await this.auditService.log({
      userId: intakeToken.userId,
      clinicId: intakeToken.clinicId,
      action: 'CREATE',
      entity: 'Patient',
      entityId: patient.id,
      metadata: {
        via: 'intake-form',
        tokenId: intakeToken.id,
        consentGiven: true,
      },
      request,
    });

    return {
      message:
        'Dados enviados com sucesso! Seu terapeuta receberá as informações.',
      patientId: patient.id,
    };
  }
}
