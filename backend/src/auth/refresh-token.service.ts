import { Injectable, UnauthorizedException } from '@nestjs/common';
import { Cron, CronExpression } from '@nestjs/schedule';
import { PrismaService } from '../prisma/prisma.service';
import { RefreshToken } from '@prisma/client';
import * as crypto from 'crypto';

const REFRESH_TOKEN_EXPIRY_DAYS = 7;

function hashToken(plainToken: string): string {
  return crypto.createHash('sha256').update(plainToken).digest('hex');
}

@Injectable()
export class RefreshTokenService {
  constructor(private prisma: PrismaService) {}

  async createRefreshToken(
    userId: string,
    clinicId?: string,
    options?: { deviceInfo?: string; ipAddress?: string },
  ): Promise<string> {
    const plainToken = crypto.randomBytes(64).toString('hex');
    const tokenHash = hashToken(plainToken);
    const expiresAt = new Date();
    expiresAt.setDate(expiresAt.getDate() + REFRESH_TOKEN_EXPIRY_DAYS);

    await this.prisma.refreshToken.create({
      data: {
        token: tokenHash,
        userId,
        clinicId: clinicId ?? null,
        expiresAt,
        deviceInfo: options?.deviceInfo ?? null,
        ipAddress: options?.ipAddress ?? null,
      },
    });

    return plainToken;
  }

  async validateRefreshToken(plainToken: string): Promise<RefreshToken> {
    const tokenHash = hashToken(plainToken);
    const record = await this.prisma.refreshToken.findUnique({
      where: { token: tokenHash },
    });

    if (!record) {
      throw new UnauthorizedException('Token inválido');
    }

    if (record.revoked) {
      throw new UnauthorizedException('Token revogado');
    }

    if (record.expiresAt < new Date()) {
      throw new UnauthorizedException('Token expirado');
    }

    return record;
  }

  /**
   * Retorna userId e clinicId do token (para auditoria antes de revogar).
   * Retorna null se token não encontrado.
   */
  async getTokenInfo(plainToken: string): Promise<{ userId: string; clinicId: string | null } | null> {
    const tokenHash = hashToken(plainToken);
    const record = await this.prisma.refreshToken.findFirst({
      where: { token: tokenHash },
      select: { userId: true, clinicId: true },
    });
    return record ? { userId: record.userId, clinicId: record.clinicId } : null;
  }

  async revokeToken(plainToken: string): Promise<void> {
    const tokenHash = hashToken(plainToken);
    await this.prisma.refreshToken.updateMany({
      where: { token: tokenHash },
      data: { revoked: true },
    });
  }

  async rotateRefreshToken(oldPlainToken: string): Promise<string> {
    const record = await this.validateRefreshToken(oldPlainToken);

    const newPlainToken = crypto.randomBytes(64).toString('hex');
    const newTokenHash = hashToken(newPlainToken);
    const expiresAt = new Date();
    expiresAt.setDate(expiresAt.getDate() + REFRESH_TOKEN_EXPIRY_DAYS);

    await this.prisma.$transaction([
      this.prisma.refreshToken.update({
        where: { id: record.id },
        data: { revoked: true, replacedByToken: newTokenHash },
      }),
      this.prisma.refreshToken.create({
        data: {
          token: newTokenHash,
          userId: record.userId,
          clinicId: record.clinicId,
          expiresAt,
          deviceInfo: record.deviceInfo,
          ipAddress: record.ipAddress,
        },
      }),
    ]);

    return newPlainToken;
  }

  async deleteExpiredTokens(): Promise<number> {
    const result = await this.prisma.refreshToken.deleteMany({
      where: { expiresAt: { lt: new Date() } },
    });
    return result.count;
  }

  @Cron(CronExpression.EVERY_DAY_AT_MIDNIGHT)
  async cleanupExpiredTokens(): Promise<void> {
    const count = await this.deleteExpiredTokens();
    if (count > 0) {
      // eslint-disable-next-line no-console
      console.log(`[RefreshToken] Removidos ${count} tokens expirados`);
    }
  }
}
