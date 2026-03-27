import {
  IsString,
  IsOptional,
  IsDateString,
  IsBoolean,
  ValidateNested,
  IsArray,
  MinLength,
} from 'class-validator';
import { Type } from 'class-transformer';

class AnamnesisItemDto {
  @IsString()
  key: string;

  @IsString()
  label: string;

  @IsString()
  value: string;
}

class AnamnesisDto {
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => AnamnesisItemDto)
  items: AnamnesisItemDto[];

  @IsOptional()
  @IsString()
  updatedAt?: string;
}

export class CreateIntakeDto {
  @IsString()
  @MinLength(1, { message: 'Nome é obrigatório' })
  name: string;

  @IsOptional()
  @IsDateString()
  birthDate?: string;

  @IsOptional()
  @IsString()
  gender?: string;

  @IsOptional()
  @IsString()
  profession?: string;

  @IsOptional()
  @IsString()
  email?: string;

  @IsOptional()
  @IsString()
  phone?: string;

  @IsOptional()
  @ValidateNested()
  @Type(() => AnamnesisDto)
  anamnesis?: AnamnesisDto;

  @IsBoolean()
  consentGiven: boolean;
}
