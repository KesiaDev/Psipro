import { Body, Controller, Post } from '@nestjs/common';
import { BetaService } from './beta.service';
import { CreateBetaRequestDto } from './dto/create-beta-request.dto';

@Controller('beta')
export class BetaController {
  constructor(private readonly betaService: BetaService) {}

  /**
   * POST /api/beta/request
   * Recebe solicitação de acesso beta. Endpoint público (sem autenticação).
   */
  @Post('request')
  async createRequest(@Body() dto: CreateBetaRequestDto) {
    return this.betaService.create(dto);
  }
}
