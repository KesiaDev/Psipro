import {
  BadRequestException,
  Injectable,
  NotFoundException,
  ForbiddenException,
} from '@nestjs/common';
import { AuditService } from '../audit/audit.service';
import { PrismaService } from '../prisma/prisma.service';
import { whereNotDeleted } from '../prisma/soft-delete.helper';
import { CreatePatientDto } from './dto/create-patient.dto';
import { UpdatePatientDto } from './dto/update-patient.dto';
import * as XLSX from 'xlsx';

@Injectable()
export class PatientsService {
  constructor(
    private prisma: PrismaService,
    private auditService: AuditService,
  ) {}

  async findAll(clinicId: string) {
    return this.prisma.patient.findMany({
      where: whereNotDeleted('patient', { clinicId }),
      orderBy: { updatedAt: 'desc' },
    });
  }

  async getCount(clinicId: string) {
    return this.prisma.patient.count({
      where: whereNotDeleted('patient', { clinicId }),
    });
  }

  async getRecent(clinicId: string) {
    const patients = await this.prisma.patient.findMany({
      where: whereNotDeleted('patient', { clinicId }),
      include: {
        sessions: {
          where: { deletedAt: null },
          orderBy: { date: 'desc' },
          take: 1,
        },
        _count: { select: { sessions: true } },
      },
      orderBy: { updatedAt: 'desc' },
      take: 10,
    });

    return {
      patients: patients.map((p) => {
        const lastSession = p.sessions[0];
        return {
          id: p.id,
          name: p.name,
          full_name: p.name,
          last_session_at: lastSession?.date?.toISOString() ?? null,
          lastSession: lastSession?.date?.toISOString() ?? null,
          sessions_count: p._count.sessions,
          sessions: p._count.sessions,
          progress: 'stable' as const,
        };
      }),
    };
  }

  async findOne(id: string, clinicId: string) {
    const patient = await this.prisma.patient.findFirst({
      where: whereNotDeleted('patient', { id, clinicId }),
      include: {
        sessions: {
          where: { deletedAt: null },
          orderBy: { date: 'desc' },
          take: 10,
        },
        payments: {
          where: { deletedAt: null },
          orderBy: { date: 'desc' },
        },
      },
    });

    if (!patient) {
      throw new NotFoundException('Paciente não encontrado');
    }

    return patient;
  }

  async create(
    createPatientDto: CreatePatientDto,
    clinicId: string,
    userId: string,
  ) {
    const source = createPatientDto.source || 'web';
    const origin = source === 'app' ? 'ANDROID' : 'WEB';

    const { full_name: _fn, nome: _n, ...dto } = createPatientDto;

    const formattedBirthDate = dto.birthDate
      ? new Date(`${dto.birthDate}T00:00:00.000Z`)
      : null;

    const patient = await this.prisma.patient.create({
      data: {
        ...dto,
        birthDate: formattedBirthDate,
        clinicId,
        clinicOwnerId: userId,
        sharedWith: dto.sharedWith || [],
        source,
        origin,
      },
    });

    this.auditService.log({
      userId,
      clinicId,
      action: 'patient_creation',
      entity: 'Patient',
      entityId: patient.id,
      metadata: { name: patient.name },
    }).catch(() => {});

    return patient;
  }

  /**
   * Importa pacientes a partir de um Excel, usando o mesmo mapeamento do Web.
   * Retorna a lista de pacientes criados (persistidos).
   * Pacientes são associados à clínica ativa (clinicId).
   */
  async importFromExcel(
    userId: string,
    fileBuffer: Buffer,
    mapping: Record<string, string>,
    clinicId: string,
  ) {
    if (!clinicId?.trim()) {
      throw new BadRequestException('clinicId é obrigatório para importação');
    }
    const clinicIdTrim = clinicId.trim();

    const nomeCol = mapping?.nome;
    const telefoneCol = mapping?.telefone;
    const cpfCol = mapping?.cpf;
    const emailCol = mapping?.email;
    const nascimentoCol = mapping?.dataNascimento;

    if (!nomeCol || !telefoneCol) {
      throw new BadRequestException(
        'Mapeamento inválido: nome e telefone são obrigatórios',
      );
    }

    const workbook = XLSX.read(fileBuffer, { type: 'buffer' });
    const firstSheet = workbook.Sheets[workbook.SheetNames[0]];
    const range = firstSheet['!ref']
      ? XLSX.utils.decode_range(firstSheet['!ref'])
      : null;
    if (!range) {
      throw new BadRequestException('Arquivo Excel inválido');
    }

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
          obj[header] =
            row[idx] !== undefined && row[idx] !== null ? row[idx] : '';
        });
        return obj;
      })
      .filter((row) =>
        Object.values(row).some((v) => String(v).trim() !== ''),
      );

    if (rows.length === 0) {
      throw new BadRequestException('Arquivo Excel não contém dados válidos');
    }

    const toBirthDateISO = (value: any): string | undefined => {
      if (
        value === null ||
        value === undefined ||
        String(value).trim() === ''
      )
        return undefined;
      if (value instanceof Date && !isNaN(value.getTime()))
        return value.toISOString();

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
      clinicId: clinicIdTrim,
      status: 'Ativo',
      source: 'web',
    }));

    const firstInvalid = createDtos.findIndex((p) => !p.name || !p.phone);
    if (firstInvalid >= 0) {
      throw new BadRequestException(
        `Linha ${firstInvalid + 2}: Nome e telefone são obrigatórios`,
      );
    }

    return Promise.all(
      createDtos.map((dto) => this.create(dto, clinicIdTrim, userId)),
    );
  }

  async update(id: string, clinicId: string, updatePatientDto: UpdatePatientDto, userId: string) {
    const patient = await this.prisma.patient.findFirst({
      where: whereNotDeleted('patient', { id, clinicId }),
    });

    if (!patient) {
      throw new NotFoundException('Paciente não encontrado');
    }

    const data: Record<string, unknown> = {
      lastSyncedAt: new Date(),
    };
    if (updatePatientDto.name !== undefined) data.name = updatePatientDto.name;
    if (updatePatientDto.cpf !== undefined) data.cpf = updatePatientDto.cpf;
    if (updatePatientDto.phone !== undefined) data.phone = updatePatientDto.phone;
    if (updatePatientDto.email !== undefined) data.email = updatePatientDto.email;
    if (updatePatientDto.birthDate !== undefined && updatePatientDto.birthDate !== '') {
      const d = new Date(updatePatientDto.birthDate);
      if (!isNaN(d.getTime())) data.birthDate = d;
    }
    if (updatePatientDto.address !== undefined) data.address = updatePatientDto.address;
    if (updatePatientDto.emergencyContact !== undefined)
      data.emergencyContact = updatePatientDto.emergencyContact;
    if (updatePatientDto.observations !== undefined)
      data.observations = updatePatientDto.observations;
    if (updatePatientDto.status !== undefined) data.status = updatePatientDto.status;
    if (updatePatientDto.type !== undefined) data.type = updatePatientDto.type;
    if (updatePatientDto.sharedWith !== undefined) data.sharedWith = updatePatientDto.sharedWith;
    if (updatePatientDto.source) {
      (data as any).origin = updatePatientDto.source === 'app' ? 'ANDROID' : 'WEB';
    }

    const updated = await this.prisma.patient.update({
      where: { id },
      data: data as any,
    });

    this.auditService.log({
      userId,
      clinicId,
      action: 'patient_update',
      entity: 'Patient',
      entityId: id,
      metadata: { name: updated.name },
    }).catch(() => {});

    return updated;
  }

  async delete(id: string, clinicId: string, userId: string) {
    const patient = await this.prisma.patient.findFirst({
      where: whereNotDeleted('patient', { id, clinicId }),
      select: { name: true },
    });

    if (!patient) {
      throw new NotFoundException('Paciente não encontrado');
    }

    await this.prisma.patient.updateMany({
      where: { id, clinicId },
      data: { deletedAt: new Date() },
    });

    this.auditService.log({
      userId,
      clinicId,
      action: 'patient_deletion',
      entity: 'Patient',
      entityId: id,
      metadata: { name: patient.name },
    }).catch(() => {});

    return { success: true };
  }
}
