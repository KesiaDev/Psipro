import { IsISO8601, IsOptional, IsString } from 'class-validator';

export class SyncDocumentsQueryDto {
  @IsOptional()
  @IsString()
  patientId?: string;

  @IsOptional()
  @IsISO8601()
  updatedAfter?: string;
}
