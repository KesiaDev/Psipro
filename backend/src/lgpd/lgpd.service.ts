import { Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { AuditService } from '../audit/audit.service';

@Injectable()
export class LgpdService {
  constructor(
    private prisma: PrismaService,
    private auditService: AuditService,
  ) {}

  async recordConsent(params: {
    userId: string;
    clinicId: string;
    patientId?: string;
    type: string;
    version?: string;
    ipAddress?: string;
    userAgent?: string;
  }) {
    const consent = await this.prisma.lgpdConsent.create({
      data: {
        userId: params.userId,
        clinicId: params.clinicId,
        patientId: params.patientId ?? null,
        type: params.type,
        version: params.version ?? '1.0',
        accepted: true,
        ipAddress: params.ipAddress ?? null,
        userAgent: params.userAgent ?? null,
      },
    });

    await this.auditService.log({
      userId: params.userId,
      clinicId: params.clinicId,
      action: 'lgpd_consent_recorded',
      entity: 'LgpdConsent',
      entityId: consent.id,
      metadata: { type: params.type, version: params.version, patientId: params.patientId },
    });

    return consent;
  }

  async getConsents(userId: string, clinicId: string, patientId?: string) {
    return this.prisma.lgpdConsent.findMany({
      where: {
        clinicId,
        ...(patientId ? { patientId } : { userId }),
        revokedAt: null,
      },
      orderBy: { createdAt: 'desc' },
    });
  }

  async revokeConsent(consentId: string, clinicId: string, userId: string) {
    const consent = await this.prisma.lgpdConsent.findFirst({
      where: { id: consentId, clinicId },
    });

    if (!consent) {
      throw new NotFoundException('Consentimento não encontrado');
    }

    const updated = await this.prisma.lgpdConsent.update({
      where: { id: consentId },
      data: { revokedAt: new Date() },
    });

    await this.auditService.log({
      userId,
      clinicId,
      action: 'lgpd_consent_revoked',
      entity: 'LgpdConsent',
      entityId: consentId,
      metadata: { type: consent.type },
    });

    return updated;
  }

  /** GET /api/lgpd/export — portabilidade de dados Art. 18, II LGPD */
  async exportData(userId: string, clinicId: string) {
    const [user, patients, sessions, appointments, consents, auditLogs] = await Promise.all([
      this.prisma.user.findUnique({
        where: { id: userId },
        select: { id: true, name: true, email: true, createdAt: true, lgpdAcceptedAt: true },
      }),
      this.prisma.patient.findMany({
        where: { clinicOwnerId: userId, deletedAt: null },
        select: {
          id: true, name: true, phone: true, email: true, birthDate: true,
          gender: true, profession: true, status: true, createdAt: true,
          anamnesis: true, consentGiven: true, consentAt: true,
        },
      }),
      this.prisma.session.findMany({
        where: { userId, deletedAt: null },
        select: {
          id: true, date: true, duration: true, status: true,
          notes: true, source: true, createdAt: true,
          patient: { select: { id: true, name: true } },
        },
      }),
      this.prisma.appointment.findMany({
        where: { userId, deletedAt: null },
        select: {
          id: true, scheduledAt: true, duration: true, type: true,
          status: true, createdAt: true,
          patient: { select: { id: true, name: true } },
        },
      }),
      this.prisma.lgpdConsent.findMany({
        where: { userId },
        select: { id: true, type: true, version: true, accepted: true, createdAt: true, revokedAt: true },
      }),
      this.prisma.auditLog.findMany({
        where: { userId },
        select: { id: true, action: true, entity: true, entityId: true, createdAt: true },
        orderBy: { createdAt: 'desc' },
        take: 200,
      }),
    ]);

    await this.auditService.log({
      userId,
      clinicId,
      action: 'lgpd_data_export',
      entity: 'User',
      entityId: userId,
      metadata: { exportedAt: new Date().toISOString() },
    });

    return {
      exportedAt: new Date().toISOString(),
      requestedBy: userId,
      legalBasis: 'LGPD Art. 18, II — Portabilidade de dados',
      profile: user,
      patients: { total: patients.length, data: patients },
      sessions: { total: sessions.length, data: sessions },
      appointments: { total: appointments.length, data: appointments },
      consents: { total: consents.length, data: consents },
      auditLog: { total: auditLogs.length, data: auditLogs },
    };
  }

  async anonymizePatient(patientId: string, clinicId: string, userId: string) {
    const patient = await this.prisma.patient.findFirst({
      where: { id: patientId, clinicId, deletedAt: null },
    });

    if (!patient) {
      throw new NotFoundException('Paciente não encontrado');
    }

    const anonymized = await this.prisma.patient.update({
      where: { id: patientId },
      data: {
        name: `ANONIMIZADO-${patientId.slice(0, 8)}`,
        cpf: null,
        phone: null,
        email: null,
        birthDate: null,
        address: null,
        emergencyContact: null,
        observations: '[dados anonimizados conforme LGPD]',
        deletedAt: new Date(),
      },
    });

    await this.auditService.log({
      userId,
      clinicId,
      action: 'lgpd_patient_anonymized',
      entity: 'Patient',
      entityId: patientId,
      metadata: { reason: 'LGPD anonymization request' },
    });

    return { message: 'Paciente anonimizado com sucesso', id: anonymized.id };
  }
}
