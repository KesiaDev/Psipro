import { Transform } from 'class-transformer';
import {
  IsEmail,
  IsEnum,
  IsOptional,
  IsString,
  MaxLength,
  MinLength,
} from 'class-validator';
import { ProfessionalType } from '@prisma/client';

const emptyToUndef = ({ value }: { value: unknown }) =>
  value === '' || value === null ? undefined : value;

export class UpdateProfileDto {
  @IsOptional()
  @Transform(emptyToUndef)
  @IsString()
  @MinLength(2)
  @MaxLength(200)
  name?: string;

  /** Alias usado pelo dashboard / registro (`fullName` → `name` no banco). */
  @IsOptional()
  @Transform(emptyToUndef)
  @IsString()
  @MinLength(2)
  @MaxLength(200)
  fullName?: string;

  @IsOptional()
  @Transform(emptyToUndef)
  @IsEmail()
  email?: string;

  @IsOptional()
  @Transform(emptyToUndef)
  @IsString()
  @MaxLength(50)
  phone?: string;

  @IsOptional()
  @Transform(emptyToUndef)
  @IsString()
  @MaxLength(50)
  license?: string;

  /** Alias comum para CRP (mesmo campo que `license`). */
  @IsOptional()
  @Transform(emptyToUndef)
  @IsString()
  @MaxLength(50)
  crp?: string;

  @IsOptional()
  @Transform(emptyToUndef)
  @IsEnum(ProfessionalType)
  professionalType?: ProfessionalType;

  @IsOptional()
  @Transform(emptyToUndef)
  @IsString()
  @MaxLength(4000)
  specialties?: string;
}
