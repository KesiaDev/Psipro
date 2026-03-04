import {
  IsDateString,
  IsInt,
  IsOptional,
  IsString,
  IsUUID,
  Min,
} from 'class-validator';

export class SyncSessionDto {
  @IsUUID()
  id: string;

  @IsUUID()
  patientId: string;

  @IsUUID()
  professionalId: string;

  @IsDateString()
  date: string;

  @IsInt()
  @Min(1)
  duration: number;

  @IsDateString()
  updatedAt: string;

  @IsOptional()
  @IsString()
  notes?: string;

  @IsOptional()
  @IsString()
  status?: string;
}
