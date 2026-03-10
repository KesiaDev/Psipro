import { ForbiddenException, Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { whereNotDeleted } from '../prisma/soft-delete.helper';

@Injectable()
export class AnamneseService {
  constructor(private prisma: PrismaService) {}

  async getModels(clinicId: string) {
    return this.prisma.anamneseModel.findMany({
      where: { clinicId },
      include: { campos: { orderBy: { ordem: 'asc' } } },
      orderBy: { nome: 'asc' },
    });
  }

  async createModel(clinicId: string, body: { nome: string; isDefault?: boolean }) {
    return this.prisma.anamneseModel.create({
      data: {
        clinicId,
        nome: body.nome,
        isDefault: body.isDefault ?? false,
      },
    });
  }

  async updateModel(clinicId: string, id: string, body: { nome?: string; isDefault?: boolean }) {
    const existing = await this.prisma.anamneseModel.findFirst({
      where: { id, clinicId },
    });
    if (!existing) throw new NotFoundException('Modelo não encontrado');
    return this.prisma.anamneseModel.update({
      where: { id },
      data: {
        ...(body.nome != null && { nome: body.nome }),
        ...(body.isDefault != null && { isDefault: body.isDefault }),
      },
    });
  }

  async deleteModel(clinicId: string, id: string) {
    const existing = await this.prisma.anamneseModel.findFirst({
      where: { id, clinicId },
    });
    if (!existing) throw new NotFoundException('Modelo não encontrado');
    return this.prisma.anamneseModel.delete({ where: { id } });
  }

  async getCampos(modeloId: string, clinicId: string) {
    const modelo = await this.prisma.anamneseModel.findFirst({
      where: { id: modeloId, clinicId },
    });
    if (!modelo) throw new NotFoundException('Modelo não encontrado');
    return this.prisma.anamneseCampo.findMany({
      where: { modeloId },
      orderBy: { ordem: 'asc' },
    });
  }

  async syncCampos(
    modeloId: string,
    clinicId: string,
    campos: Array<{
      id?: string;
      tipo: string;
      label: string;
      opcoes?: string;
      obrigatorio?: boolean;
      ordem?: number;
    }>,
  ) {
    const modelo = await this.prisma.anamneseModel.findFirst({
      where: { id: modeloId, clinicId },
    });
    if (!modelo) throw new NotFoundException('Modelo não encontrado');

    const results = [];
    for (let i = 0; i < campos.length; i++) {
      const c = campos[i];
      const data = {
        modeloId,
        tipo: c.tipo,
        label: c.label,
        opcoes: c.opcoes ?? null,
        obrigatorio: c.obrigatorio ?? false,
        ordem: c.ordem ?? i,
      };
      if (c.id) {
        const updated = await this.prisma.anamneseCampo.upsert({
          where: { id: c.id },
          create: { ...data, id: c.id },
          update: data,
        });
        results.push(updated);
      } else {
        const created = await this.prisma.anamneseCampo.create({
          data,
        });
        results.push(created);
      }
    }
    return results;
  }

  async getPreenchidas(patientId: string, clinicId: string) {
    const patient = await this.prisma.patient.findFirst({
      where: whereNotDeleted('patient', { id: patientId, clinicId }),
    });
    if (!patient) throw new NotFoundException('Paciente não encontrado');
    return this.prisma.anamnesePreenchida.findMany({
      where: { patientId },
      include: {
        modelo: {
          select: { id: true, nome: true },
          include: { campos: { orderBy: { ordem: 'asc' } } },
        },
      },
      orderBy: { data: 'desc' },
    });
  }

  async createPreenchida(
    clinicId: string,
    body: {
      patientId: string;
      modeloId: string;
      respostas: Record<string, unknown>;
      assinaturaPath?: string;
    },
  ) {
    const [patient, modelo] = await Promise.all([
      this.prisma.patient.findFirst({
        where: whereNotDeleted('patient', { id: body.patientId, clinicId }),
      }),
      this.prisma.anamneseModel.findFirst({
        where: { id: body.modeloId, clinicId },
      }),
    ]);
    if (!patient) throw new NotFoundException('Paciente não encontrado');
    if (!modelo) throw new NotFoundException('Modelo não encontrado');
    return this.prisma.anamnesePreenchida.create({
      data: {
        patientId: body.patientId,
        modeloId: body.modeloId,
        respostas: body.respostas as object,
        assinaturaPath: body.assinaturaPath ?? null,
        data: new Date(),
      },
    });
  }
}
