import { IsJWT, IsOptional, IsString } from 'class-validator';

export class HandoffDto {
  /**
   * JWT para validação.
   * Pode ser enviado no body (`token`) ou via `Authorization: Bearer <token>`.
   */
  @IsOptional()
  @IsString()
  @IsJWT()
  token?: string;
}

