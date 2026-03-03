import { IsDateString, IsInt, IsOptional, IsString, Min } from 'class-validator';

export class CreateAppointmentDto {
  @IsString()
  patientId: string;

  @IsDateString()
  scheduledAt: string;

  @IsOptional()
  @IsInt()
  @Min(1)
  duration?: number;

  @IsOptional()
  @IsString()
  type?: string;

  @IsOptional()
  @IsString()
  status?: string;

  @IsOptional()
  @IsString()
  notes?: string;

  @IsOptional()
  @IsString()
  clinicId?: string;
}
