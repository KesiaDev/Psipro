import { Controller, Get, Post, Query, UseGuards, Res, Req, Logger } from '@nestjs/common';
import { Response, Request } from 'express';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { CurrentUser } from '../../common/decorators/current-user.decorator';
import { GoogleCalendarService } from './google-calendar.service';

@Controller('integrations/google-calendar')
export class GoogleCalendarController {
  private readonly logger = new Logger(GoogleCalendarController.name);

  constructor(private readonly googleCalendar: GoogleCalendarService) {}

  /**
   * GET /api/integrations/google-calendar/connect
   * Inicia o fluxo OAuth. Retorna a URL para o frontend redirecionar o usuário ao Google.
   */
  @Get('connect')
  @UseGuards(JwtAuthGuard)
  connect(
    @CurrentUser() user: { id: string },
    @Req() req: Request,
  ): { url: string } {
    const clinicId = (req.headers['x-clinic-id'] as string)?.trim() || null;
    const url = this.googleCalendar.getAuthUrl(user.id, clinicId);
    this.logger.log(`[connect] Usuário ${user.id} solicitou URL de autorização`);
    return { url };
  }

  /**
   * GET /api/integrations/google-calendar/callback
   * Callback OAuth do Google. Recebe code + state, troca por tokens e redireciona para o dashboard.
   */
  @Get('callback')
  async callback(
    @Query('code') code: string,
    @Query('state') state: string,
    @Res() res: Response,
  ): Promise<void> {
    if (!code || !state) {
      this.logger.warn('[callback] Parâmetros code ou state ausentes');
      res.redirect(this.googleCalendar.getRedirectUrlOnError());
      return;
    }

    const result = await this.googleCalendar.handleCallback(code, state);

    if (result.success && result.redirectTo) {
      this.logger.log('[callback] Autorização concluída com sucesso');
      res.redirect(result.redirectTo);
      return;
    }

    this.logger.warn('[callback] Falha na autorização');
    res.redirect(this.googleCalendar.getRedirectUrlOnError());
  }

  /**
   * GET /api/integrations/google-calendar/status
   */
  @Get('status')
  @UseGuards(JwtAuthGuard)
  status(
    @CurrentUser() user: { id: string },
    @Req() req: Request,
  ) {
    const clinicId = (req.headers['x-clinic-id'] as string)?.trim() || null;
    return this.googleCalendar.getIntegration(user.id, clinicId);
  }

  /**
   * POST /api/integrations/google-calendar/disconnect
   */
  @Post('disconnect')
  @UseGuards(JwtAuthGuard)
  disconnect(
    @CurrentUser() user: { id: string },
    @Req() req: Request,
  ) {
    const clinicId = (req.headers['x-clinic-id'] as string)?.trim() || null;
    return this.googleCalendar.disconnect(user.id, clinicId);
  }
}
