import { IsString, IsOptional, IsBoolean } from 'class-validator';

export class UpdateClinicUserDto {
  @IsString()
  @IsOptional()
  role?: string;

  @IsString()
  @IsOptional()
  status?: string;

  @IsBoolean()
  @IsOptional()
  canViewAllPatients?: boolean;

  @IsBoolean()
  @IsOptional()
  canEditAllPatients?: boolean;

  @IsBoolean()
  @IsOptional()
  canViewFinancial?: boolean;

  @IsBoolean()
  @IsOptional()
  canManageUsers?: boolean;
}



