import { IsISO8601, IsOptional, IsString } from 'class-validator';

export class SyncDocumentsQueryDto {
  /** Aceito para não rejeitar (forbidNonWhitelisted); backend usa X-Clinic-Id. */
  @IsOptional()
  @IsString()
  clinicId?: string;

  @IsOptional()
  @IsString()
  patientId?: string;

  @IsOptional()
  @IsISO8601()
  updatedAfter?: string;
}
