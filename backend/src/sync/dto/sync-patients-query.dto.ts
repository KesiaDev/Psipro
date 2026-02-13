import { IsISO8601, IsOptional, IsString } from 'class-validator';

export class SyncPatientsQueryDto {
  /**
   * Clínica alvo do sync. Se não enviada, o backend tenta usar a primeira
   * clínica ativa do usuário (quando existir).
   */
  @IsOptional()
  @IsString()
  clinicId?: string;

  /**
   * Retornar apenas pacientes atualizados após esta data/hora (ISO 8601).
   * Ex: 2026-01-29T12:34:56.000Z
   */
  @IsOptional()
  @IsISO8601()
  updatedAfter?: string;
}

