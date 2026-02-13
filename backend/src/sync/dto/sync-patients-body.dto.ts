import { Type } from 'class-transformer';
import { ArrayMinSize, IsArray, ValidateNested } from 'class-validator';
import { SyncPatientDto } from './sync-patient.dto';

export class SyncPatientsBodyDto {
  @IsArray()
  @ArrayMinSize(1)
  @ValidateNested({ each: true })
  @Type(() => SyncPatientDto)
  patients: SyncPatientDto[];
}

