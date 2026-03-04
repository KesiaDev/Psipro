import {
  Injectable,
  NotFoundException,
  ForbiddenException,
  BadRequestException,
} from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { CreateClinicDto } from './dto/create-clinic.dto';
import { UpdateClinicDto } from './dto/update-clinic.dto';
import { InviteUserDto } from './dto/invite-user.dto';
import { UpdateClinicUserDto } from './dto/update-clinic-user.dto';
import { PlanType } from '@prisma/client';
import { AuditService } from '../audit/audit.service';
import { whereNotDeleted } from '../prisma/soft-delete.helper';

@Injectable()
export class ClinicsService {
  constructor(
    private prisma: PrismaService,
    private auditService: AuditService,
  ) {}

  /**
   * POST /clinics — Cria nova clínica e associa usuário como OWNER.
   * Não exige clinicId; usuário pode não ter clínica ainda.
   * Retorna clinic + novo accessToken com userId, email, clinicId, role.
   */
  async create(userId: string, createClinicDto: CreateClinicDto) {
    const user = await this.prisma.user.findUnique({
      where: { id: userId },
      select: { id: true, email: true },
    });
    if (!user) {
      throw new NotFoundException('Usuário não encontrado');
    }

    // Criar clínica com planType INDIVIDUAL e status active
    const clinic = await this.prisma.clinic.create({
      data: {
        name: createClinicDto.name,
        planType: PlanType.INDIVIDUAL,
        status: 'active',
        plan: 'basic',
        ...(createClinicDto.email && { email: createClinicDto.email }),
        ...(createClinicDto.phone && { phone: createClinicDto.phone }),
        ...(createClinicDto.address && { address: createClinicDto.address }),
        ...(createClinicDto.cnpj && { cnpj: createClinicDto.cnpj }),
      },
    });

    // NÃO alterar User. Relação via ClinicUser (N:N).
    await this.prisma.clinicUser.upsert({
      where: {
        clinicId_userId: { clinicId: clinic.id, userId },
      },
      create: {
        clinicId: clinic.id,
        userId,
        role: 'owner',
        status: 'active',
        canViewAllPatients: true,
        canEditAllPatients: true,
        canViewFinancial: true,
        canManageUsers: true,
      },
      update: { role: 'owner', status: 'active' },
    });

    // Retorna apenas clinic. JWT não é regenerado.
    return { clinic };
  }

  async findAll(userId: string) {
    // Buscar todas as clínicas do usuário via ClinicUser
    const clinicUsers = await this.prisma.clinicUser.findMany({
      where: {
        userId: userId,
        status: 'active',
      },
      include: {
        clinic: true,
      },
    });

    if (clinicUsers.length > 0) {
      return clinicUsers.map((cu) => ({
        ...cu.clinic,
        role: cu.role,
        permissions: {
          canViewAllPatients: cu.canViewAllPatients,
          canEditAllPatients: cu.canEditAllPatients,
          canViewFinancial: cu.canViewFinancial,
          canManageUsers: cu.canManageUsers,
        },
      }));
    }

    // Fallback: usuário com User.clinicId mas sem ClinicUser (registros antigos)
    const user = await this.prisma.user.findUnique({
      where: { id: userId },
      select: { clinicId: true },
    });
    if (user?.clinicId) {
      const clinic = await this.prisma.clinic.findUnique({
        where: { id: user.clinicId },
      });
      if (clinic) {
        return [{ ...clinic, role: 'owner', permissions: { canViewAllPatients: true, canEditAllPatients: true, canViewFinancial: true, canManageUsers: true } }];
      }
    }

    return [];
  }

  async findOne(id: string, userId: string) {
    // Verificar se usuário tem acesso à clínica
    const clinicUser = await this.prisma.clinicUser.findUnique({
      where: {
        clinicId_userId: {
          clinicId: id,
          userId: userId,
        },
      },
      include: {
        clinic: {
          include: {
            users: {
              include: {
                user: {
                  select: {
                    id: true,
                    name: true,
                    email: true,
                  },
                },
              },
            },
          },
        },
      },
    });

    if (!clinicUser) {
      throw new NotFoundException('Clínica não encontrada ou sem acesso');
    }

    return {
      ...clinicUser.clinic,
      role: clinicUser.role,
      permissions: {
        canViewAllPatients: clinicUser.canViewAllPatients,
        canEditAllPatients: clinicUser.canEditAllPatients,
        canViewFinancial: clinicUser.canViewFinancial,
        canManageUsers: clinicUser.canManageUsers,
      },
      members: clinicUser.clinic.users.map((cu) => ({
        id: cu.user.id,
        name: cu.user.name,
        email: cu.user.email,
        role: cu.role,
        status: cu.status,
        joinedAt: cu.joinedAt,
      })),
    };
  }

  async update(id: string, userId: string, updateClinicDto: UpdateClinicDto) {
    await this.assertCanManageClinic(id, userId);
    return this.prisma.clinic.update({
      where: { id },
      data: updateClinicDto,
    });
  }

  /**
   * DELETE /clinics/:id — Apenas owner ou admin.
   * Remove clínica e dados em cascata (ClinicUser, Patient, etc).
   */
  async delete(id: string, userId: string) {
    await this.assertCanManageClinic(id, userId);

    await this.prisma.user.updateMany({
      where: { clinicId: id },
      data: { clinicId: null },
    });

    return this.prisma.clinic.delete({
      where: { id },
    });
  }

  private async assertCanManageClinic(clinicId: string, userId: string) {
    const clinicUser = await this.prisma.clinicUser.findUnique({
      where: {
        clinicId_userId: { clinicId, userId },
      },
    });
    if (!clinicUser) {
      throw new NotFoundException('Clínica não encontrada');
    }
    if (!['owner', 'admin'].includes(clinicUser.role)) {
      throw new ForbiddenException('Sem permissão para gerenciar esta clínica');
    }
  }

  async inviteUser(clinicId: string, userId: string, inviteUserDto: InviteUserDto) {
    // Verificar se usuário pode gerenciar membros
    const clinicUser = await this.prisma.clinicUser.findUnique({
      where: {
        clinicId_userId: {
          clinicId: clinicId,
          userId: userId,
        },
      },
    });

    if (!clinicUser || !clinicUser.canManageUsers) {
      throw new ForbiddenException('Sem permissão para convidar usuários');
    }

    // Buscar usuário por email
    const user = await this.prisma.user.findUnique({
      where: { email: inviteUserDto.email },
    });

    if (!user) {
      throw new NotFoundException('Usuário não encontrado');
    }

    // Verificar se já está na clínica
    const existing = await this.prisma.clinicUser.findUnique({
      where: {
        clinicId_userId: {
          clinicId: clinicId,
          userId: user.id,
        },
      },
    });

    if (existing) {
      throw new BadRequestException('Usuário já faz parte desta clínica');
    }

    // Criar convite
    const created = await this.prisma.clinicUser.create({
      data: {
        clinicId: clinicId,
        userId: user.id,
        role: inviteUserDto.role || 'psychologist',
        status: 'invited',
        canViewAllPatients: inviteUserDto.canViewAllPatients || false,
        canEditAllPatients: inviteUserDto.canEditAllPatients || false,
        canViewFinancial: inviteUserDto.canViewFinancial || false,
        canManageUsers: inviteUserDto.canManageUsers || false,
      },
    });

    this.auditService.log({
      userId,
      clinicId,
      action: 'clinic_invite',
      entity: 'ClinicUser',
      entityId: created.id,
      metadata: { targetEmail: inviteUserDto.email, role: created.role },
    }).catch(() => {});

    return created;
  }

  async updateUser(
    clinicId: string,
    targetUserId: string,
    currentUserId: string,
    updateDto: UpdateClinicUserDto,
  ) {
    // Verificar se usuário pode gerenciar membros
    const currentUser = await this.prisma.clinicUser.findUnique({
      where: {
        clinicId_userId: {
          clinicId: clinicId,
          userId: currentUserId,
        },
      },
    });

    if (!currentUser || !currentUser.canManageUsers) {
      throw new ForbiddenException('Sem permissão para gerenciar usuários');
    }

    // Verificar se target user existe na clínica
    const targetUser = await this.prisma.clinicUser.findUnique({
      where: {
        clinicId_userId: {
          clinicId: clinicId,
          userId: targetUserId,
        },
      },
    });

    if (!targetUser) {
      throw new NotFoundException('Usuário não encontrado na clínica');
    }

    // Não permitir mudar role de owner
    if (targetUser.role === 'owner' && updateDto.role && updateDto.role !== 'owner') {
      throw new ForbiddenException('Não é possível alterar role do owner');
    }

    const updated = await this.prisma.clinicUser.update({
      where: {
        clinicId_userId: {
          clinicId: clinicId,
          userId: targetUserId,
        },
      },
      data: updateDto,
    });

    if (updateDto.role && targetUser.role !== updateDto.role) {
      this.auditService.log({
        userId: currentUserId,
        clinicId,
        action: 'role_change',
        entity: 'ClinicUser',
        entityId: targetUserId,
        metadata: { targetUserId, from: targetUser.role, to: updateDto.role },
      }).catch(() => {});
    }

    return updated;
  }

  async removeUser(clinicId: string, targetUserId: string, currentUserId: string) {
    // Verificar se usuário pode gerenciar membros
    const currentUser = await this.prisma.clinicUser.findUnique({
      where: {
        clinicId_userId: {
          clinicId: clinicId,
          userId: currentUserId,
        },
      },
    });

    if (!currentUser || !currentUser.canManageUsers) {
      throw new ForbiddenException('Sem permissão para remover usuários');
    }

    const targetUser = await this.prisma.clinicUser.findUnique({
      where: {
        clinicId_userId: {
          clinicId: clinicId,
          userId: targetUserId,
        },
      },
    });

    if (!targetUser) {
      throw new NotFoundException('Usuário não encontrado na clínica');
    }

    // Não permitir remover owner
    if (targetUser.role === 'owner') {
      throw new ForbiddenException('Não é possível remover o owner da clínica');
    }

    return this.prisma.clinicUser.delete({
      where: {
        clinicId_userId: {
          clinicId: clinicId,
          userId: targetUserId,
        },
      },
    });
  }

  async getProfessionals(clinicId: string, userId: string) {
    const clinicUser = await this.prisma.clinicUser.findUnique({
      where: {
        clinicId_userId: { clinicId, userId },
      },
    });
    if (!clinicUser) {
      throw new NotFoundException('Clínica não encontrada');
    }

    const clinicUsers = await this.prisma.clinicUser.findMany({
      where: { clinicId, status: 'active' },
      include: { user: true },
      orderBy: { joinedAt: 'desc' },
    });

    return clinicUsers.map((cu) => ({
      id: cu.user.id,
      name: cu.user.name,
      email: cu.user.email,
      role: cu.role,
      status: cu.status,
      joinedAt: cu.joinedAt,
    }));
  }

  async getClinicStats(clinicId: string, userId: string) {
    // Verificar acesso
    const clinicUser = await this.prisma.clinicUser.findUnique({
      where: {
        clinicId_userId: {
          clinicId: clinicId,
          userId: userId,
        },
      },
    });

    if (!clinicUser) {
      throw new NotFoundException('Clínica não encontrada');
    }

    // Só admin/owner pode ver stats consolidados
    if (!['owner', 'admin'].includes(clinicUser.role) && !clinicUser.canViewFinancial) {
      throw new ForbiddenException('Sem permissão para ver estatísticas');
    }

    const [patientsCount, appointmentsCount, sessionsCount, revenue] = await Promise.all([
      this.prisma.patient.count({
        where: whereNotDeleted('patient', { clinicId: clinicId }),
      }),
      this.prisma.appointment.count({
        where: whereNotDeleted('appointment', { clinicId: clinicId }),
      }),
      this.prisma.session.count({
        where: whereNotDeleted('session', {
          patient: { clinicId: clinicId, deletedAt: null },
        }),
      }),
      this.prisma.payment.aggregate({
        where: {
          ...whereNotDeleted('payment', { status: 'pago' }),
          patient: { clinicId: clinicId, deletedAt: null },
        },
        _sum: {
          amount: true,
        },
      }),
    ]);

    return {
      patients: patientsCount,
      appointments: appointmentsCount,
      sessions: sessionsCount,
      revenue: revenue._sum.amount || 0,
    };
  }
}




