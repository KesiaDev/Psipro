import { IsString, IsDateString, IsInt, IsOptional, IsObject } from 'class-validator';

export class UpdateSessionDto {
  @IsOptional()
  @IsString()
  patientId?: string;

  @IsOptional()
  @IsString()
  professionalId?: string;

  @IsOptional()
  @IsDateString()
  date?: string;

  @IsOptional()
  @IsInt()
  duration?: number;

  @IsOptional()
  @IsString()
  notes?: string;

  /** Tipo da sessão (ex: Consulta) - aceito pelo dashboard */
  @IsOptional()
  @IsString()
  type?: string;

  /** Prontuário clínico (estado emocional, evolução, técnicas, etc.) - aceito pelo dashboard */
  @IsOptional()
  @IsObject()
  clinical?: Record<string, unknown>;
}
