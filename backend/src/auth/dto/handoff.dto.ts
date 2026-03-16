import { IsOptional, IsString } from 'class-validator';

export class HandoffDto {
  /**
   * JWT (App → criar handoff) ou handoff token (Web → trocar por sessão).
   * Pode ser enviado no body (`token`) ou via `Authorization: Bearer <token>`.
   */
  @IsOptional()
  @IsString()
  token?: string;

  /**
   * Path para redirecionar após login (ex: /dashboard). Usado quando App cria handoff.
   */
  @IsOptional()
  @IsString()
  returnUrl?: string;
}

