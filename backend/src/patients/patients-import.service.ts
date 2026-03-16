import { BadRequestException, Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { AuditService } from '../audit/audit.service';
import * as XLSX from 'xlsx';

/** Colunas esperadas no Excel */
const COLS = {
  NOME: 'Nome completo',
  NASCIMENTO: 'Data de nascimento',
  EMAIL: 'E-mail',
  TELEFONE: 'Telefone',
  GENERO: 'Gênero',
} as const;

export interface ImportResult {
  imported: number;
  skipped: number;
}

@Injectable()
export class PatientsImportService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly auditService: AuditService,
  ) {}

  /**
   * Importa pacientes a partir de Excel com formato fixo:
   * Nome completo | Data de nascimento | E-mail | Telefone | Gênero
   * - Não duplica por email (na mesma clínica)
   * - Ignora linhas vazias
   */
  async importFromExcel(
    fileBuffer: Buffer,
    clinicId: string,
    userId: string,
  ): Promise<ImportResult> {
    if (!clinicId?.trim()) {
      throw new BadRequestException('clinicId é obrigatório para importação');
    }
    const clinicIdTrim = clinicId.trim();

    const workbook = XLSX.read(fileBuffer, { type: 'buffer' });
    const sheetName = workbook.SheetNames[0];
    if (!sheetName) {
      throw new BadRequestException('Arquivo Excel inválido ou vazio');
    }
    const sheet = workbook.Sheets[sheetName];
    const data = XLSX.utils.sheet_to_json<Record<string, unknown>>(sheet, {
      defval: '',
      blankrows: false,
    });

    let imported = 0;
    let skipped = 0;

    const toBirthDate = (value: unknown): Date | null => {
      if (value == null || String(value).trim() === '') return null;
      if (value instanceof Date && !isNaN(value.getTime())) return value;
      if (typeof value === 'number') {
        const parsed = XLSX.SSF.parse_date_code(value);
        if (parsed?.y && parsed?.m && parsed?.d) {
          const dt = new Date(Date.UTC(parsed.y, parsed.m - 1, parsed.d));
          if (!isNaN(dt.getTime())) return dt;
        }
      }
      const dt = new Date(String(value));
      return !isNaN(dt.getTime()) ? dt : null;
    };

    // Emails já existentes na clínica (para evitar duplicatas)
    const existingPatients = await this.prisma.patient.findMany({
      where: { clinicId: clinicIdTrim, deletedAt: null },
      select: { email: true },
    });
    const existingEmails = new Set(
      existingPatients
        .map((p) => p.email?.toLowerCase().trim())
        .filter(Boolean),
    );

    for (const row of data) {
      const name = String(row[COLS.NOME] ?? '').trim();
      const emailRaw = String(row[COLS.EMAIL] ?? '').trim();
      const phone = String(row[COLS.TELEFONE] ?? '').trim();
      const genero = String(row[COLS.GENERO] ?? '').trim();
      const birthDate = toBirthDate(row[COLS.NASCIMENTO]);

      // Ignorar linhas vazias
      if (!name && !emailRaw && !phone) {
        skipped++;
        continue;
      }

      // Nome obrigatório
      if (!name) {
        skipped++;
        continue;
      }

      // Não duplicar por email (se email preenchido)
      const email = emailRaw || undefined;
      if (email) {
        const emailNorm = email.toLowerCase().trim();
        if (existingEmails.has(emailNorm)) {
          skipped++;
          continue;
        }
      }

      try {
        await this.prisma.patient.create({
          data: {
            name,
            email: email || null,
            phone: phone || null,
            birthDate,
            clinicId: clinicIdTrim,
            clinicOwnerId: userId,
            observations: genero ? `Gênero: ${genero}` : null,
            status: 'Ativo',
            source: 'web',
            origin: 'WEB',
          },
        });

        if (email) {
          existingEmails.add(email.toLowerCase().trim());
        }
        imported++;
      } catch (err) {
        skipped++;
      }
    }

    if (imported > 0) {
      this.auditService
        .log({
          userId,
          clinicId: clinicIdTrim,
          action: 'patient_import_excel',
          entity: 'Patient',
          entityId: '',
          metadata: { imported, skipped },
        })
        .catch(() => {});
    }

    return { imported, skipped };
  }
}
