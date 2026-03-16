import { IsString } from 'class-validator';

export class VoiceNoteDto {
  @IsString()
  sessionId: string;

  @IsString()
  transcript: string;
}
