import { IsString, MaxLength, MinLength } from 'class-validator';

export class SendWhatsAppMessageDto {
  @IsString()
  @MinLength(1)
  @MaxLength(32)
  phone!: string;

  @IsString()
  @MinLength(1)
  @MaxLength(8000)
  message!: string;
}
