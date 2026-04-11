import { IsIn, IsOptional, IsString, MaxLength } from 'class-validator';

/**
 * Corpo de POST /api/integrations/whatsapp/connect.
 * Todos os campos opcionais (Evolution vs Z-API); validação reforçada no serviço.
 */
export class ConnectWhatsAppDto {
  @IsOptional()
  @IsIn(['zapi', 'evolution'])
  provider?: 'zapi' | 'evolution';

  @IsOptional()
  @IsString()
  @MaxLength(2048)
  evolutionApiUrl?: string;

  @IsOptional()
  @IsString()
  @MaxLength(512)
  evolutionInstanceToken?: string;

  @IsOptional()
  @IsString()
  @MaxLength(128)
  instanceId?: string;

  @IsOptional()
  @IsString()
  @MaxLength(512)
  token?: string;

  @IsOptional()
  @IsString()
  @MaxLength(512)
  clientToken?: string;

  @IsOptional()
  @IsString()
  @MaxLength(32)
  phoneNumber?: string;
}
