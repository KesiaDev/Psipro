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

@Injectable()
export class ClinicsService {
  constructor(private prisma: PrismaService) {}

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
    // Buscar todas as clínicas do usuário
    const clinicUsers = await this.prisma.clinicUser.findMany({
      where: {
        userId: userId,
        status: 'active',
      },
      include: {
        clinic: true,
      },
    });

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
    // Verificar se usuário é owner ou admin
    const clinicUser = await this.prisma.clinicUser.findUnique({
      where: {
        clinicId_userId: {
          clinicId: id,
          userId: userId,
        },
      },
    });

    if (!clinicUser) {
      throw new NotFoundException('Clínica não encontrada');
    }

    if (!['owner', 'admin'].includes(clinicUser.role)) {
      throw new ForbiddenException('Sem permissão para editar clínica');
    }

    return this.prisma.clinic.update({
      where: { id },
      data: updateClinicDto,
    });
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
    return this.prisma.clinicUser.create({
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

    return this.prisma.clinicUser.update({
      where: {
        clinicId_userId: {
          clinicId: clinicId,
          userId: targetUserId,
        },
      },
      data: updateDto,
    });
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
        where: { clinicId: clinicId },
      }),
      this.prisma.appointment.count({
        where: { clinicId: clinicId },
      }),
      this.prisma.session.count({
        where: {
          patient: {
            clinicId: clinicId,
          },
        },
      }),
      this.prisma.payment.aggregate({
        where: {
          patient: {
            clinicId: clinicId,
          },
          status: 'pago',
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




