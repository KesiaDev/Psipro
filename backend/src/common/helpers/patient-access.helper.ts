import { Injectable } from '@nestjs/common';
import { PrismaService } from '../../prisma/prisma.service';

/**
 * Helper para validar acesso a paciente (próprio, clínica ou compartilhado).
 * Reutilizado em Payments, Sessions, Financial, etc.
 */
@Injectable()
export class PatientAccessHelper {
  constructor(private prisma: PrismaService) {}

  async hasAccessToPatient(patientId: string, userId: string): Promise<boolean> {
    const patient = await this.prisma.patient.findUnique({
      where: { id: patientId },
      select: {
        userId: true,
        clinicId: true,
        sharedWith: true,
      },
    });

    if (!patient) return false;

    if (patient.userId === userId) return true;

    if (patient.clinicId) {
      const clinicUser = await this.prisma.clinicUser.findUnique({
        where: {
          clinicId_userId: {
            clinicId: patient.clinicId,
            userId,
          },
        },
      });

      if (clinicUser?.status === 'active') {
        if (
          clinicUser.canViewAllPatients ||
          patient.sharedWith?.includes(userId)
        ) {
          return true;
        }
      }
    }

    if (patient.sharedWith?.includes(userId)) return true;

    return false;
  }
}
