import { Transform } from 'class-transformer';
import { IsIn, IsOptional, IsString, MaxLength } from 'class-validator';

const emptyToUndef = ({ value }: { value: unknown }) =>
  value === '' || value === null ? undefined : value;

/**
 * Corpo de POST /api/integrations/whatsapp/connect.
 * Todos os campos opcionais (Evolution vs Z-API); validação reforçada no serviço.
 */
export class ConnectWhatsAppDto {
  @IsOptional()
  @Transform(emptyToUndef)
  @IsIn(['zapi', 'evolution'])
  provider?: 'zapi' | 'evolution';

  @IsOptional()
  @Transform(emptyToUndef)
  @IsString()
  @MaxLength(2048)
  evolutionApiUrl?: string;

  /** Alias de `evolutionApiUrl` (alguns clientes enviam só isto) */
  @IsOptional()
  @Transform(emptyToUndef)
  @IsString()
  @MaxLength(2048)
  evolutionUrl?: string;

  @IsOptional()
  @Transform(emptyToUndef)
  @IsString()
  @MaxLength(512)
  evolutionInstanceToken?: string;

  /** Igual a "Nome da instância" no Evolution Manager (ex.: TerapeutaClaudiaCruz) */
  @IsOptional()
  @Transform(emptyToUndef)
  @IsString()
  @MaxLength(128)
  evolutionInstanceName?: string;

  /** Alias de `evolutionInstanceName` */
  @IsOptional()
  @Transform(emptyToUndef)
  @IsString()
  @MaxLength(128)
  instanceName?: string;

  @IsOptional()
  @Transform(emptyToUndef)
  @IsString()
  @MaxLength(128)
  instanceId?: string;

  @IsOptional()
  @Transform(emptyToUndef)
  @IsString()
  @MaxLength(512)
  token?: string;

  @IsOptional()
  @Transform(emptyToUndef)
  @IsString()
  @MaxLength(512)
  clientToken?: string;

  @IsOptional()
  @Transform(emptyToUndef)
  @IsString()
  @MaxLength(32)
  phoneNumber?: string;
}
