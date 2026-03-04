import { Type } from 'class-transformer';
import { IsArray, ValidateNested } from 'class-validator';
import { SyncPaymentDto } from './sync-payment.dto';

export class SyncPaymentsBodyDto {
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => SyncPaymentDto)
  payments: SyncPaymentDto[];
}
