import { Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';

/** Request mínimo para extrair ip e user-agent */
export interface AuditRequest {
  ip?: string;
  headers?: { 'user-agent'?: string; [k: string]: string | string[] | undefined };
}

export interface AuditLogParams {
  userId: string;
  clinicId: string;
  action: string;
  entity: string;
  entityId?: string | null;
  metadata?: Record<string, unknown> | null;
  request?: AuditRequest | null;
}

/**
 * Serviço de auditoria enterprise — registra eventos críticos.
 * Captura ipAddress e userAgent do request.
 * Nunca quebra o fluxo principal: falhas são absorvidas silenciosamente.
 */
@Injectable()
export class AuditService {
  constructor(private prisma: PrismaService) {}

  private getIp(request?: AuditRequest | null): string | null {
    if (!request?.headers) return request?.ip ?? null;
    const forwarded = request.headers['x-forwarded-for'];
    if (forwarded) {
      const val = Array.isArray(forwarded) ? forwarded[0] : forwarded;
      return typeof val === 'string' ? val.split(',')[0]?.trim() ?? null : null;
    }
    return request?.ip ?? null;
  }

  private getUserAgent(request?: AuditRequest | null): string | null {
    if (!request?.headers) return null;
    const ua = request.headers['user-agent'];
    return typeof ua === 'string' ? ua : null;
  }

  /**
   * Registra um evento de auditoria.
   * Try/catch silencioso: se falhar, não propaga exceção.
   */
  async log(params: AuditLogParams): Promise<void> {
    try {
      const ipAddress = this.getIp(params.request) ?? null;
      const userAgent = this.getUserAgent(params.request) ?? null;

      await this.prisma.auditLog.create({
        data: {
          userId: params.userId,
          clinicId: params.clinicId,
          action: params.action,
          entity: params.entity,
          entityId: params.entityId ?? null,
          metadata: (params.metadata ?? undefined) as object | undefined,
          ipAddress,
          userAgent,
        },
      });
    } catch {
      // Auditoria nunca deve quebrar o fluxo principal
    }
  }
}
