import {
  IsDateString,
  IsObject,
  IsOptional,
  IsString,
  IsUUID,
} from 'class-validator';

export class SyncDocumentDto {
  @IsOptional()
  @IsUUID()
  id?: string;

  @IsUUID()
  patientId: string;

  @IsString()
  name: string;

  @IsString()
  type: string;

  @IsOptional()
  @IsString()
  fileUrl?: string;

  @IsOptional()
  @IsObject()
  content?: Record<string, unknown>; // { html, signatures, ... }

  @IsDateString()
  updatedAt: string;

  @IsOptional()
  @IsString()
  status?: string;

  @IsOptional()
  @IsString()
  source?: string;
}
