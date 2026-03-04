import {
  IsDateString,
  IsInt,
  IsOptional,
  IsString,
  IsUUID,
  Min,
} from 'class-validator';

export class SyncAppointmentDto {
  @IsUUID()
  id: string;

  @IsUUID()
  patientId: string;

  @IsUUID()
  professionalId: string; // userId do profissional no backend

  @IsDateString()
  scheduledAt: string;

  @IsInt()
  @Min(1)
  duration: number;

  @IsDateString()
  updatedAt: string;

  @IsOptional()
  @IsString()
  type?: string;

  @IsOptional()
  @IsString()
  notes?: string;

  @IsOptional()
  @IsString()
  status?: string;
}
