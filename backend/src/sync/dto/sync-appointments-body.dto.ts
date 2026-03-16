import { Type } from 'class-transformer';
import { ArrayMinSize, IsArray, ValidateNested } from 'class-validator';
import { SyncAppointmentDto } from './sync-appointment.dto';

export class SyncAppointmentsBodyDto {
  @IsArray()
  @ArrayMinSize(0)
  @ValidateNested({ each: true })
  @Type(() => SyncAppointmentDto)
  appointments: SyncAppointmentDto[];
}
