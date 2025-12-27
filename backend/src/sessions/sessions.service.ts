import { Injectable, NotFoundException, ForbiddenException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { CreateSessionDto } from './dto/create-session.dto';

@Injectable()
export class SessionsService {
  constructor(private prisma: PrismaService) {}

  async findAll(userId: string) {
    return this.prisma.session.findMany({
      where: { userId },
      include: {
        patient: {
          select: {
            id: true,
            name: true,
          },
        },
      },
      orderBy: { date: 'desc' },
    });
  }

  async findByPatient(patientId: string, userId: string) {
    // Verificar se o paciente pertence ao usuário
    const patient = await this.prisma.patient.findUnique({
      where: { id: patientId },
    });

    if (!patient || patient.userId !== userId) {
      throw new ForbiddenException('Acesso negado');
    }

    return this.prisma.session.findMany({
      where: {
        patientId,
        userId,
      },
      orderBy: { date: 'desc' },
    });
  }

  async create(userId: string, createSessionDto: CreateSessionDto) {
    // Verificar se o paciente pertence ao usuário
    const patient = await this.prisma.patient.findUnique({
      where: { id: createSessionDto.patientId },
    });

    if (!patient || patient.userId !== userId) {
      throw new ForbiddenException('Paciente não encontrado ou acesso negado');
    }

    return this.prisma.session.create({
      data: {
        ...createSessionDto,
        userId,
        date: new Date(createSessionDto.date),
        source: createSessionDto.source || 'app',
      },
      include: {
        patient: {
          select: {
            id: true,
            name: true,
          },
        },
      },
    });
  }
}

