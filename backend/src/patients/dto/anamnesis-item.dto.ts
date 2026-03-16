import { IsString, IsBoolean, IsOptional } from 'class-validator';
import { Transform } from 'class-transformer';

/**
 * Item da anamnese do paciente (formulário livre do dashboard).
 * Campos padrão: historia_pessoal, queixa_principal, motivo_consulta, etc.
 * Campos customizados: custom_* (isCustom: true).
 */
export class AnamneseItemDto {
  @IsString()
  key: string;

  @IsString()
  label: string;

  @IsOptional()
  @Transform(({ value }) => (value == null ? '' : value))
  @IsString()
  value?: string;

  @IsOptional()
  @IsBoolean()
  isCustom?: boolean;
}
