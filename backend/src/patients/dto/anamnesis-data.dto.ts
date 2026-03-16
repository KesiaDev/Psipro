import { Type, Transform } from 'class-transformer';
import { IsArray, IsISO8601, IsOptional, ValidateNested } from 'class-validator';
import { AnamneseItemDto } from './anamnesis-item.dto';

/**
 * Estrutura da anamnese armazenada no paciente.
 * Usada no detalhe do paciente no dashboard.
 */
export class AnamneseDataDto {
  @Transform(({ value }) => (Array.isArray(value) ? value : []))
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => AnamneseItemDto)
  items: AnamneseItemDto[];

  @IsOptional()
  @IsISO8601()
  updatedAt?: string;
}
