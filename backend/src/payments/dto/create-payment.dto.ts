import { IsString, IsDateString, IsNumber, IsOptional, IsIn } from 'class-validator';

export class CreatePaymentDto {
  @IsString()
  patientId: string;

  @IsNumber()
  amount: number;

  @IsDateString()
  date: string;

  @IsString()
  @IsOptional()
  method?: string;

  @IsString()
  @IsOptional()
  @IsIn(['pago', 'pendente', 'cancelado'])
  status?: string;

  @IsString()
  @IsOptional()
  notes?: string;

  @IsString()
  @IsOptional()
  sessionId?: string;

  @IsString()
  @IsOptional()
  @IsIn(['app', 'web'])
  source?: string;
}



