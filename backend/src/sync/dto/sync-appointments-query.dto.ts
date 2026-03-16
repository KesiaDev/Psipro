import { IsISO8601, IsOptional, IsString } from 'class-validator';

export class SyncAppointmentsQueryDto {
  /**
   * Clínica alvo. O app envia na query; o backend ignora e usa X-Clinic-Id.
   * Aceito apenas para não rejeitar (forbidNonWhitelisted).
   */
  @IsOptional()
  @IsString()
  clinicId?: string;

  /**
   * Retornar apenas agendamentos atualizados após esta data/hora (ISO 8601).
   * Ex: 2026-01-29T12:34:56.000Z
   */
  @IsOptional()
  @IsISO8601()
  updatedAfter?: string;
}
