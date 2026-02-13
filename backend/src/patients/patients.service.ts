import { BadRequestException, Injectable, NotFoundException, ForbiddenException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { CreatePatientDto } from './dto/create-patient.dto';
import { UpdatePatientDto } from './dto/update-patient.dto';
import * as XLSX from 'xlsx';

@Injectable()
export class PatientsService {
  constructor(private prisma: PrismaService) {}

  private async getUserClinics(userId: string) {
    return this.prisma.clinicUser.findMany({
      where: {
        userId: userId,
        status: 'active',
      },
      select: {
        clinicId: true,
        role: true,
        canViewAllPatients: true,
        canEditAllPatients: true,
      },
    });
  }

  private async hasAccessToPatient(patientId: string, userId: string): Promise<boolean> {
    const patient = await this.prisma.patient.findUnique({
      where: { id: patientId },
      select: {
        userId: true,
        clinicId: true,
        sharedWith: true,
      },
    });

    if (!patient) return false;

    // Se é paciente próprio
    if (patient.userId === userId) return true;

    // Se é paciente da clínica
    if (patient.clinicId) {
      const clinicUser = await this.prisma.clinicUser.findUnique({
        where: {
          clinicId_userId: {
            clinicId: patient.clinicId,
            userId: userId,
          },
        },
      });

      if (clinicUser && clinicUser.status === 'active') {
        // Se tem permissão para ver todos ou se está compartilhado
        if (clinicUser.canViewAllPatients || patient.sharedWith.includes(userId)) {
          return true;
        }
      }
    }

    // Se está na lista de compartilhados
    if (patient.sharedWith && patient.sharedWith.includes(userId)) {
      return true;
    }

    return false;
  }

  async findAll(userId: string, clinicId?: string) {
    const userClinics = await this.getUserClinics(userId);
    const clinicIds = userClinics.map((uc) => uc.clinicId);

    const where: any = {
      OR: [
        { userId: userId }, // Próprios pacientes
      ],
    };

    // Se especificou clínica e tem acesso
    if (clinicId) {
      const hasAccess = userClinics.some((uc) => uc.clinicId === clinicId);
      if (hasAccess) {
        where.OR.push({ clinicId: clinicId });
      }
    } else {
      // Adicionar pacientes de todas as clínicas que tem acesso
      if (clinicIds.length > 0) {
        where.OR.push({ clinicId: { in: clinicIds } });
      }
    }

    // Adicionar pacientes compartilhados
    where.OR.push({ sharedWith: { has: userId } });

    return this.prisma.patient.findMany({
      where,
      orderBy: { updatedAt: 'desc' },
    });
  }

  async findOne(id: string, userId: string) {
    const hasAccess = await this.hasAccessToPatient(id, userId);
    if (!hasAccess) {
      throw new ForbiddenException('Acesso negado');
    }

    return this.prisma.patient.findUnique({
      where: { id },
      include: {
        sessions: {
          orderBy: { date: 'desc' },
          take: 10,
        },
        payments: {
          orderBy: { date: 'desc' },
        },
      },
    });
  }

  async create(userId: string, createPatientDto: CreatePatientDto) {
    const source = createPatientDto.source || 'web';
    const origin = source === 'app' ? 'ANDROID' : 'WEB';

    // Se for paciente de clínica
    if (createPatientDto.clinicId) {
      // Verificar se usuário pertence à clínica
      const clinicUser = await this.prisma.clinicUser.findUnique({
        where: {
          clinicId_userId: {
            clinicId: createPatientDto.clinicId,
            userId: userId,
          },
        },
      });

      if (!clinicUser || clinicUser.status !== 'active') {
        throw new ForbiddenException('Sem acesso a esta clínica');
      }

      return this.prisma.patient.create({
        data: {
          ...createPatientDto,
          clinicId: createPatientDto.clinicId,
          clinicOwnerId: userId,
          sharedWith: createPatientDto.sharedWith || [],
          source,
          origin,
        },
      });
    }

    // Paciente independente
    return this.prisma.patient.create({
      data: {
        ...createPatientDto,
        userId: userId,
        sharedWith: createPatientDto.sharedWith || [],
        source,
        origin,
      },
    });
  }

  /**
   * Importa pacientes a partir de um Excel, usando o mesmo mapeamento do Web.
   * Retorna a lista de pacientes criados (persistidos).
   */
  async importFromExcel(
    userId: string,
    fileBuffer: Buffer,
    mapping: Record<string, string>,
    clinicId?: string,
  ) {
    const nomeCol = mapping?.nome;
    const telefoneCol = mapping?.telefone;
    const cpfCol = mapping?.cpf;
    const emailCol = mapping?.email;
    const nascimentoCol = mapping?.dataNascimento;

    if (!nomeCol || !telefoneCol) {
      throw new BadRequestException('Mapeamento inválido: nome e telefone são obrigatórios');
    }

    // Se veio clinicId, valida acesso uma vez (a criação também valida, mas isso evita trabalho desnecessário)
    if (clinicId) {
      const clinicUser = await this.prisma.clinicUser.findUnique({
        where: {
          clinicId_userId: {
            clinicId,
            userId,
          },
        },
        select: { status: true },
      });
      if (!clinicUser || clinicUser.status !== 'active') {
        throw new ForbiddenException('Sem acesso a esta clínica');
      }
    }

    const workbook = XLSX.read(fileBuffer, { type: 'buffer' });
    const firstSheet = workbook.Sheets[workbook.SheetNames[0]];
    const range = firstSheet['!ref'] ? XLSX.utils.decode_range(firstSheet['!ref']) : null;
    if (!range) {
      throw new BadRequestException('Arquivo Excel inválido');
    }

    // Construir headers exatamente como o Web faz (inclui "Coluna A/B/...")
    const sheetHeaders: string[] = [];
    for (let col = 0; col <= range.e.c; col++) {
      const cellAddress = XLSX.utils.encode_cell({ r: 0, c: col });
      const cell = firstSheet[cellAddress] as any;
      let headerValue = '';
      if (cell && cell.v !== undefined && cell.v !== null) {
        headerValue = String(cell.v).trim();
      }
      if (headerValue === '') {
        const colLetter = XLSX.utils.encode_col(col);
        headerValue = `Coluna ${colLetter}`;
      }
      sheetHeaders.push(headerValue);
    }

    const rawData = XLSX.utils.sheet_to_json(firstSheet, {
      header: 1,
      defval: '',
      blankrows: false,
    }) as any[][];

    const rows = rawData
      .slice(1)
      .map((row) => {
        const obj: Record<string, any> = {};
        sheetHeaders.forEach((header, idx) => {
          obj[header] = row[idx] !== undefined && row[idx] !== null ? row[idx] : '';
        });
        return obj;
      })
      .filter((row) => Object.values(row).some((v) => String(v).trim() !== ''));

    if (rows.length === 0) {
      throw new BadRequestException('Arquivo Excel não contém dados válidos');
    }

    const toBirthDateISO = (value: any): string | undefined => {
      if (value === null || value === undefined || String(value).trim() === '') return undefined;
      if (value instanceof Date && !isNaN(value.getTime())) return value.toISOString();

      // Excel às vezes envia datas como número serial
      if (typeof value === 'number') {
        const parsed = XLSX.SSF.parse_date_code(value);
        if (parsed && parsed.y && parsed.m && parsed.d) {
          const dt = new Date(Date.UTC(parsed.y, parsed.m - 1, parsed.d));
          if (!isNaN(dt.getTime())) return dt.toISOString();
        }
      }

      const dt = new Date(String(value));
      if (!isNaN(dt.getTime())) return dt.toISOString();
      return undefined;
    };

    const createDtos: CreatePatientDto[] = rows.map((row) => ({
      name: String(row[nomeCol] ?? '').trim(),
      phone: String(row[telefoneCol] ?? '').trim() || undefined,
      cpf: cpfCol ? String(row[cpfCol] ?? '').trim() || undefined : undefined,
      email: emailCol ? String(row[emailCol] ?? '').trim() || undefined : undefined,
      birthDate: nascimentoCol ? toBirthDateISO(row[nascimentoCol]) : undefined,
      clinicId: clinicId || undefined,
      status: 'Ativo',
      source: 'web',
    }));

    // Validação mínima server-side
    const firstInvalid = createDtos.findIndex((p) => !p.name || !p.phone);
    if (firstInvalid >= 0) {
      // +2 por causa do header e índice base 0
      throw new BadRequestException(`Linha ${firstInvalid + 2}: Nome e telefone são obrigatórios`);
    }

    return await Promise.all(createDtos.map((dto) => this.create(userId, dto)));
  }

  async update(id: string, userId: string, updatePatientDto: UpdatePatientDto) {
    const patient = await this.prisma.patient.findUnique({
      where: { id },
    });

    if (!patient) {
      throw new NotFoundException('Paciente não encontrado');
    }

    // Verificar permissão de edição
    const canEdit =
      patient.userId === userId ||
      (patient.clinicId &&
        (await this.hasEditPermission(patient.clinicId, userId, patient.userId === userId)));

    if (!canEdit) {
      throw new ForbiddenException('Sem permissão para editar este paciente');
    }

    return this.prisma.patient.update({
      where: { id },
      data: {
        ...updatePatientDto,
        ...(updatePatientDto.source
          ? { origin: updatePatientDto.source === 'app' ? 'ANDROID' : 'WEB' }
          : {}),
        lastSyncedAt: new Date(),
      },
    });
  }

  private async hasEditPermission(
    clinicId: string | null,
    userId: string,
    isOwner: boolean,
  ): Promise<boolean> {
    if (isOwner) return true;

    if (!clinicId) return false;

    const clinicUser = await this.prisma.clinicUser.findUnique({
      where: {
        clinicId_userId: {
          clinicId: clinicId,
          userId: userId,
        },
      },
    });

    return (
      clinicUser?.status === 'active' &&
      (clinicUser.canEditAllPatients || ['owner', 'admin'].includes(clinicUser.role))
    );
  }
}

