import {
  Controller,
  Post,
  Body,
  UseGuards,
  UseInterceptors,
  UploadedFile,
  BadRequestException,
} from '@nestjs/common';
import { FileInterceptor } from '@nestjs/platform-express';
import { VoiceService } from './voice.service';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { RolesGuard } from '../common/guards/roles.guard';
import { Roles } from '../common/decorators/roles.decorator';
import { CurrentClinicId } from '../common/decorators/current-clinic.decorator';

import { IsString, IsOptional } from 'class-validator';
import { VoiceInsightsDto } from './dto/insights.dto';

export class SummarizeDto {
  @IsString()
  @IsOptional()
  text?: string;
}

@Controller('voice')
export class VoiceController {
  constructor(private readonly voiceService: VoiceService) {}

  /**
   * POST /api/voice/transcribe
   * multipart/form-data, campo: file (audio.wav, .mp3, .m4a, .webm)
   * Retorna: { transcript: string }
   * Não exige X-Clinic-Id (psicólogos independentes podem usar).
   */
  @Post('transcribe')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles('admin', 'psychologist', 'assistant')
  @UseInterceptors(FileInterceptor('file', { limits: { fileSize: 25 * 1024 * 1024 } }))
  async transcribe(
    @UploadedFile()
    file: { buffer: Buffer; originalname?: string } | undefined,
  ) {
    if (!file?.buffer) {
      throw new BadRequestException('Arquivo de áudio não enviado (campo: file)');
    }
    return this.voiceService.transcribe(file.buffer, file.originalname);
  }

  /**
   * POST /api/voice/summarize
   * Body: { text: string }
   * Retorna: { summary: string, keyPoints: string[] }
   * Requer OPENAI_API_KEY
   */
  @Post('summarize')
  @Roles('admin', 'psychologist', 'assistant')
  async summarize(@Body() dto: SummarizeDto) {
    return this.voiceService.summarize(dto?.text ?? '');
  }

  /**
   * POST /api/voice/insights
   * Body: { sessionId: string, text: string }
   * Gera insights clínicos via OpenAI e salva na sessão.
   * Apenas admin e psychologist.
   */
  @Post('insights')
  @Roles('admin', 'psychologist')
  async insights(
    @Body() dto: VoiceInsightsDto,
    @CurrentClinicId() clinicId: string,
  ) {
    return this.voiceService.generateAndSaveInsights(
      dto.sessionId,
      dto.text,
      clinicId,
    );
  }
}
