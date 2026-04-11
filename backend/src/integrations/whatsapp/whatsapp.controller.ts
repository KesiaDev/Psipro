import {
  Body,
  Controller,
  Delete,
  Get,
  HttpCode,
  Logger,
  Param,
  Post,
  Query,
  Req,
  UseGuards,
} from '@nestjs/common';
import { Request } from 'express';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { CurrentUser } from '../../common/decorators/current-user.decorator';
import { WhatsAppService, WhatsAppConfig } from './whatsapp.service';
import { ConnectWhatsAppDto } from './dto/connect-whatsapp.dto';
import { SendWhatsAppMessageDto } from './dto/send-whatsapp-message.dto';

@Controller('integrations/whatsapp')
export class WhatsAppController {
  private readonly logger = new Logger(WhatsAppController.name);

  constructor(private readonly whatsApp: WhatsAppService) {}

  /** GET /api/integrations/whatsapp/status */
  @Get('status')
  @UseGuards(JwtAuthGuard)
  async getStatus(
    @CurrentUser() user: { id: string },
    @Req() req: Request,
  ) {
    const clinicId = (req.headers['x-clinic-id'] as string)?.trim() || null;
    return this.whatsApp.getIntegration(user.id, clinicId);
  }

  /** POST /api/integrations/whatsapp/connect */
  @Post('connect')
  @HttpCode(200)
  @UseGuards(JwtAuthGuard)
  async connect(
    @CurrentUser() user: { id: string },
    @Req() req: Request,
    @Body() body: ConnectWhatsAppDto,
  ) {
    const clinicId = (req.headers['x-clinic-id'] as string)?.trim() || null;
    const evolutionUrl = body.evolutionApiUrl ?? body.evolutionUrl;
    const inferredEvolution = Boolean(evolutionUrl?.trim() || body.evolutionInstanceToken?.trim());
    const provider: WhatsAppConfig['provider'] =
      body.provider ?? (inferredEvolution ? 'evolution' : 'zapi');
    const cfg: WhatsAppConfig = {
      provider,
      evolutionApiUrl: evolutionUrl,
      evolutionInstanceToken: body.evolutionInstanceToken,
      evolutionInstanceName: body.evolutionInstanceName ?? body.instanceName,
      instanceId: body.instanceId,
      token: body.token,
      clientToken: body.clientToken,
      phoneNumber: body.phoneNumber,
    };
    const result = await this.whatsApp.connect(user.id, clinicId, cfg);
    this.logger.log(`[connect] userId=${user.id} success=${result.success}`);
    return result;
  }

  /** DELETE /api/integrations/whatsapp/disconnect */
  @Delete('disconnect')
  @UseGuards(JwtAuthGuard)
  async disconnect(
    @CurrentUser() user: { id: string },
    @Req() req: Request,
  ) {
    const clinicId = (req.headers['x-clinic-id'] as string)?.trim() || null;
    await this.whatsApp.disconnect(user.id, clinicId);
    return { success: true };
  }

  /** POST /api/integrations/whatsapp/send — envio manual de mensagem */
  @Post('send')
  @UseGuards(JwtAuthGuard)
  async sendMessage(
    @CurrentUser() user: { id: string },
    @Req() req: Request,
    @Body() body: SendWhatsAppMessageDto,
  ) {
    const clinicId = (req.headers['x-clinic-id'] as string)?.trim() || null;
    const sent = await this.whatsApp.sendCustomMessage(user.id, clinicId, body.phone, body.message);
    return { sent };
  }

  /**
   * POST /api/integrations/whatsapp/webhook
   * Chamado pelo Evolution GO ao receber eventos (sem autenticação JWT — IP/token externo).
   */
  @Post('webhook')
  @HttpCode(200)
  async receiveWebhook(@Body() payload: any) {
    this.logger.log(`[webhook] Evento recebido: ${payload?.event} | instância: ${payload?.instance}`);
    await this.whatsApp.handleWebhook(payload);
    return { ok: true };
  }

  /** GET /api/integrations/whatsapp/conversations — lista conversas do usuário */
  @Get('conversations')
  @UseGuards(JwtAuthGuard)
  async getConversations(
    @CurrentUser() user: { id: string },
    @Req() req: Request,
  ) {
    const clinicId = (req.headers['x-clinic-id'] as string)?.trim() || null;
    return this.whatsApp.getConversations(user.id, clinicId);
  }

  /** GET /api/integrations/whatsapp/conversations/:id/messages — mensagens de uma conversa */
  @Get('conversations/:id/messages')
  @UseGuards(JwtAuthGuard)
  async getMessages(
    @CurrentUser() user: { id: string },
    @Param('id') conversationId: string,
    @Query('take') take?: string,
    @Query('skip') skip?: string,
  ) {
    const messages = await this.whatsApp.getMessages(
      user.id,
      conversationId,
      take ? parseInt(take, 10) : 50,
      skip ? parseInt(skip, 10) : 0,
    );
    if (messages === null) return { error: 'Conversa não encontrada' };
    return messages;
  }
}
