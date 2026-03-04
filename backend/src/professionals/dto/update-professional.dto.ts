import { IsOptional, IsString, MaxLength } from 'class-validator';

export class UpdateProfessionalDto {
  @IsOptional()
  @IsString()
  @MaxLength(200)
  name?: string;

  @IsOptional()
  @IsString()
  @MaxLength(50)
  license?: string;

  @IsOptional()
  @IsString()
  @MaxLength(100)
  specialty?: string;

  @IsOptional()
  @IsString()
  role?: string; // psychologist | assistant | admin

  @IsOptional()
  canViewAllPatients?: boolean;
  @IsOptional()
  canEditAllPatients?: boolean;
  @IsOptional()
  canViewFinancial?: boolean;
  @IsOptional()
  canManageUsers?: boolean;
}
