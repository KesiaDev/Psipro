import { IsString, IsIn } from 'class-validator';

export class CreateCheckoutDto {
  @IsString()
  @IsIn(['starter', 'pro', 'enterprise'])
  planId: string;
}
