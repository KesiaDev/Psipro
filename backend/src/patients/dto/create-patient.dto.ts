import { IsString, IsOptional, IsDateString, IsIn, IsArray, MinLength } from 'class-validator';
import { Transform } from 'class-transformer';

function toName(obj: any): string {
  const v = obj?.name ?? obj?.full_name ?? obj?.nome ?? '';
  return (typeof v === 'string' ? v : String(v ?? '')).trim();
}

export class CreatePatientDto {
  @Transform(({ obj }) => toName(obj))
  @IsString()
  @MinLength(1, { message: 'Nome é obrigatório' })
  name: string;

  /** Aliases para name (compatibilidade) */
  @IsOptional()
  @IsString()
  full_name?: string;

  @IsOptional()
  @IsString()
  nome?: string;

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
  gender?: string;

  @IsOptional()
  @IsString()
  profession?: string;

  @IsOptional()
  @IsString()
  @IsIn(['app', 'web'])
  source?: string;

  // Campos para clínica
  @IsOptional()
  @IsString()
  clinicId?: string;

  @IsOptional()
  @IsArray()
  @IsString({ each: true })
  sharedWith?: string[];
}

