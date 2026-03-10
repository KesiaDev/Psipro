import { IsEmail, IsEnum, IsOptional, IsString, MinLength } from 'class-validator';

export enum ProfessionalType {
  psychologist = 'psychologist',
  therapist = 'therapist',
  psychoanalyst = 'psychoanalyst',
  counselor = 'counselor',
  coach = 'coach',
  other = 'other',
}

export class RegisterDto {
  @IsEmail()
  email: string;

  @IsString()
  @MinLength(8, { message: 'Senha deve ter no mínimo 8 caracteres' })
  password: string;

  @IsString()
  @MinLength(2)
  fullName: string;

  @IsOptional()
  @IsEnum(ProfessionalType)
  professionalType?: ProfessionalType;
}

