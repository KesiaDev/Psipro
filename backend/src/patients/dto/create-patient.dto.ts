import { IsString, IsOptional, IsDateString, IsIn, IsArray, MinLength } from 'class-validator';
import { Transform } from 'class-transformer';

export class CreatePatientDto {
  @Transform(({ obj }) => (obj.name ?? obj.full_name ?? '').trim())
  @IsString()
  @MinLength(1, { message: 'Nome é obrigatório' })
  name: string;

  /** Alias para name (compatibilidade com clientes que enviam full_name) */
  @IsOptional()
  @IsString()
  full_name?: string;

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

  // Campos para clínica
  @IsOptional()
  @IsString()
  clinicId?: string;

  @IsOptional()
  @IsArray()
  @IsString({ each: true })
  sharedWith?: string[];
}

