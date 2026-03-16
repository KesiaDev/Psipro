import { IsString, IsOptional, IsDateString, IsIn, IsArray, IsObject, ValidateIf, IsNumber } from 'class-validator';
import { Transform } from 'class-transformer';

function toAnamnesis(obj: any): object | null | undefined {
  const v = obj?.anamnesis ?? obj?.anamnesis_data ?? obj?.anamnesisData;
  return v === undefined ? undefined : (v === null ? null : typeof v === 'object' ? v : { items: [], updatedAt: new Date().toISOString() });
}

export class UpdatePatientDto {
  @IsOptional()
  @IsString()
  name?: string;

  @IsOptional()
  @IsString()
  cpf?: string;

  @IsOptional()
  @IsString()
  phone?: string;

  @IsOptional()
  @IsString()
  email?: string;

  @IsOptional()
  @IsDateString()
  birthDate?: string;

  @IsOptional()
  @IsString()
  address?: string;

  @IsOptional()
  @IsString()
  emergencyContact?: string;

  @IsOptional()
  @IsString()
  observations?: string;

  @IsOptional()
  @IsString()
  status?: string;

  @IsOptional()
  @IsString()
  type?: string;

  @IsOptional()
  @IsString()
  @IsIn(['app', 'web'])
  source?: string;

  /** Ignorado (vem da URL); aceito para não rejeitar forbidNonWhitelisted */
  @IsOptional()
  @IsString()
  id?: string;

  /** Ignorado (vem do header X-Clinic-Id); aceito para não rejeitar forbidNonWhitelisted */
  @IsOptional()
  @IsString()
  clinicId?: string;

  /** Ignorados (read-only); aceitos para não rejeitar forbidNonWhitelisted */
  @IsOptional()
  @IsDateString()
  createdAt?: string;
  @IsOptional()
  @IsDateString()
  updatedAt?: string;
  @IsOptional()
  @IsDateString()
  lastSyncedAt?: string;
  @IsOptional()
  @IsString()
  userId?: string;

  /** Aceitos para não rejeitar forbidNonWhitelisted */
  @IsOptional()
  @IsNumber()
  age?: number;
  @IsOptional()
  @IsString()
  nextSession?: string;
  @IsOptional()
  @IsNumber()
  sessionsCount?: number;
  @IsOptional()
  @IsArray()
  sessions?: unknown[];
  @IsOptional()
  @IsArray()
  payments?: unknown[];

  @IsOptional()
  @IsArray()
  @IsString({ each: true })
  sharedWith?: string[];

  /** Aliases aceitos pelo frontend; o Transform mapeia para anamnesis */
  @IsOptional()
  anamnesis_data?: unknown;
  @IsOptional()
  anamnesisData?: unknown;

  /** Aceita qualquer estrutura JSON; sem validação aninhada para evitar 400 */
  @IsOptional()
  @Transform(({ obj }) => toAnamnesis(obj))
  @ValidateIf((_o, v) => v != null)
  @IsObject()
  anamnesis?: object | null;
}

