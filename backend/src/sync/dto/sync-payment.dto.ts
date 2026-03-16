import {
  IsDateString,
  IsNumber,
  IsOptional,
  IsString,
  IsUUID,
  Min,
} from 'class-validator';

export class SyncPaymentDto {
  @IsUUID()
  id: string;

  @IsUUID()
  sessionId: string;

  @IsNumber()
  @Min(0)
  amount: number;

  @IsDateString()
  updatedAt: string;

  @IsOptional()
  @IsString()
  status?: string; // pago | pendente | cancelado

  @IsOptional()
  @IsDateString()
  paidAt?: string;
}
