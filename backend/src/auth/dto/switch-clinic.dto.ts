import { IsNotEmpty, IsString } from 'class-validator';

/**
 * Para usuários que pertencem a múltiplas clínicas (ClinicUser).
 * Endpoint futuro: POST /auth/switch-clinic
 */
export class SwitchClinicDto {
  @IsString()
  @IsNotEmpty()
  clinicId: string;
}
