import { Controller, Get, Post, Delete, Body, Req, UseGuards, Logger } from '@nestjs/common';
import { Request } from 'express';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { CurrentUser } from '../../common/decorators/current-user.decorator';
import { WhatsAppService, WhatsAppConfig } from './whatsapp.service';

class ConnectWhatsAppDto {
  provider?: 'zapi' | 'evolution';
  // Evolution GO
  evolutionApiUrl?: string;
  evolutionInstanceToken?: string;
  // Z-API (legado)
  instanceId?: string;
  token?: string;
  clientToken?: string;
  phoneNumber?: string;
}

class SendMessageDto {
  phone: string;
  message: string;
}

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
  @UseGuards(JwtAuthGuard)
  async connect(
    @CurrentUser() user: { id: string },
    @Req() req: Request,
    @Body() body: ConnectWhatsAppDto,
  ) {
    const clinicId = (req.headers['x-clinic-id'] as string)?.trim() || null;
    const cfg: WhatsAppConfig = {
      provider: body.provider ?? 'zapi',
      evolutionApiUrl: body.evolutionApiUrl,
      evolutionInstanceToken: body.evolutionInstanceToken,
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
    @Body() body: SendMessageDto,
  ) {
    const clinicId = (req.headers['x-clinic-id'] as string)?.trim() || null;
    const sent = await this.whatsApp.sendCustomMessage(user.id, clinicId, body.phone, body.message);
    return { sent };
  }
}
