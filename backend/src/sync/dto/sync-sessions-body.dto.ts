import { Type } from 'class-transformer';
import { IsArray, ValidateNested } from 'class-validator';
import { SyncSessionDto } from './sync-session.dto';

export class SyncSessionsBodyDto {
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => SyncSessionDto)
  sessions: SyncSessionDto[];
}
