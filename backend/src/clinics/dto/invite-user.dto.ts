import { IsString, IsEmail, IsNotEmpty, IsOptional } from 'class-validator';

export class InviteUserDto {
  @IsEmail()
  @IsNotEmpty()
  email: string;

  @IsString()
  @IsOptional()
  role?: string; // owner | admin | psychologist | assistant

  @IsOptional()
  canViewAllPatients?: boolean;

  @IsOptional()
  canEditAllPatients?: boolean;

  @IsOptional()
  canViewFinancial?: boolean;

  @IsOptional()
  canManageUsers?: boolean;
}



