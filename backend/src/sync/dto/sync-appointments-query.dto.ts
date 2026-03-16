import { IsISO8601, IsOptional, IsString } from 'class-validator';

export class SyncAppointmentsQueryDto {
  /**
   * Retornar apenas agendamentos atualizados após esta data/hora (ISO 8601).
   * Ex: 2026-01-29T12:34:56.000Z
   */
  @IsOptional()
  @IsISO8601()
  updatedAfter?: string;
}
