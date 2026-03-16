import { IsString } from 'class-validator';

export class VoiceInsightsDto {
  @IsString()
  sessionId: string;

  @IsString()
  text: string;
}
