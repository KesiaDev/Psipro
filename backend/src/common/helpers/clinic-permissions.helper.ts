import { PrismaService } from '../../prisma/prisma.service';

export class ClinicPermissionsHelper {
  constructor(private prisma: PrismaService) {}

  async getUserClinicAccess(userId: string, clinicId: string) {
    return this.prisma.clinicUser.findUnique({
      where: {
        clinicId_userId: {
          clinicId: clinicId,
          userId: userId,
        },
      },
    });
  }

  async canViewAllPatients(userId: string, clinicId: string): Promise<boolean> {
    const access = await this.getUserClinicAccess(userId, clinicId);
    return (
      access?.status === 'active' &&
      (access.canViewAllPatients || ['owner', 'admin'].includes(access.role))
    );
  }

  async canEditAllPatients(userId: string, clinicId: string): Promise<boolean> {
    const access = await this.getUserClinicAccess(userId, clinicId);
    return (
      access?.status === 'active' &&
      (access.canEditAllPatients || ['owner', 'admin'].includes(access.role))
    );
  }

  async canViewFinancial(userId: string, clinicId: string): Promise<boolean> {
    const access = await this.getUserClinicAccess(userId, clinicId);
    return (
      access?.status === 'active' &&
      (access.canViewFinancial || ['owner', 'admin'].includes(access.role))
    );
  }

  async canManageUsers(userId: string, clinicId: string): Promise<boolean> {
    const access = await this.getUserClinicAccess(userId, clinicId);
    return (
      access?.status === 'active' &&
      (access.canManageUsers || ['owner', 'admin'].includes(access.role))
    );
  }
}




