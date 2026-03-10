import { IsArray, ValidateNested } from 'class-validator';
import { Type } from 'class-transformer';
import { SyncDocumentDto } from './sync-document.dto';

export class SyncDocumentsBodyDto {
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => SyncDocumentDto)
  documents: SyncDocumentDto[];
}
