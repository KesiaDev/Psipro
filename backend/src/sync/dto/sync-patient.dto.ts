import {
  IsArray,
  IsDateString,
  IsIn,
  IsISO8601,
  IsOptional,
  IsString,
  IsUUID,
} from 'class-validator';

export class SyncPatientDto {
  /**
   * ID global do paciente.
   * - Pode vir vazio para criações novas (backend gera UUID).
   * - Quando existir, deve ser UUID.
   */
  @IsOptional()
  @IsUUID()
  id?: string;

  @IsString()
  clinicId: string;

  @IsString()
  name: string;

  @IsOptional()
  @IsDateString()
  birthDate?: string;

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
  @IsArray()
  @IsString({ each: true })
  sharedWith?: string[];

  /**
   * Origem do update no cliente.
   * Mantemos `source` (app|web) por compatibilidade e derivamos `origin`.
   */
  @IsOptional()
  @IsString()
  @IsIn(['ANDROID', 'WEB'])
  origin?: 'ANDROID' | 'WEB';

  @IsOptional()
  @IsString()
  @IsIn(['app', 'web'])
  source?: 'app' | 'web';

  /**
   * Base de conflito enviada pelo cliente.
   * O backend compara com `updatedAt` do registro persistido.
   */
  @IsISO8601()
  updatedAt: string;

  // Aceitos por compatibilidade; ignorados na persistência.
  @IsOptional()
  @IsISO8601()
  createdAt?: string;
}

