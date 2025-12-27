import { IsString, IsDateString, IsInt, IsOptional, IsIn } from 'class-validator';

export class CreateSessionDto {
  @IsString()
  patientId: string;

  @IsDateString()
  date: string;

  @IsInt()
  @IsOptional()
  duration?: number;

  @IsString()
  @IsOptional()
  @IsIn(['realizada', 'falta', 'cancelada'])
  status?: string;

  @IsString()
  @IsOptional()
  notes?: string;

  @IsString()
  @IsOptional()
  appointmentId?: string;

  @IsString()
  @IsOptional()
  @IsIn(['app', 'web'])
  source?: string;
}

