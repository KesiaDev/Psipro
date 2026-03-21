import { Injectable, Logger, UnauthorizedException } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { PrismaService } from '../../prisma/prisma.service';
import { google, calendar_v3 } from 'googleapis';
import {
  createOAuth2Client,
  getAuthorizationUrl,
  buildState,
  parseState,
  isStateValid,
} from './google-calendar.oauth';

const PROVIDER = 'google_calendar';

export interface CreateCalendarEventParams {
  title: string;
  description?: string;
  startTime: Date;
  endTime: Date;
  /** ID do appointment no PsiPro (para rastreamento) */
  psiproAppointmentId?: string;
}

export class GoogleCalendarNotConnectedError extends Error {
  constructor(userId: string) {
    super(`Usuário ${userId} não possui Google Calendar conectado`);
    this.name = 'GoogleCalendarNotConnectedError';
  }
}

export class GoogleCalendarTokenExpiredError extends Error {
  constructor() {
    super('Token do Google Calendar expirado. Reconecte sua conta.');
    this.name = 'GoogleCalendarTokenExpiredError';
  }
}

export class GoogleCalendarEventCreationError extends Error {
  constructor(message: string, public readonly cause?: unknown) {
    super(message);
    this.name = 'GoogleCalendarEventCreationError';
  }
}

@Injectable()
export class GoogleCalendarService {
  private readonly logger = new Logger(GoogleCalendarService.name);
  private readonly clientId: string;
  private readonly clientSecret: string;
  private readonly redirectUri: string;
  private readonly dashboardUrl: string;

  constructor(
    private config: ConfigService,
    private prisma: PrismaService,
  ) {
    this.clientId = this.config.get<string>('GOOGLE_CLIENT_ID', '');
    this.clientSecret = this.config.get<string>('GOOGLE_CLIENT_SECRET', '');
    this.redirectUri =
      this.config.get<string>('GOOGLE_CALENDAR_REDIRECT_URI') ||
      `${this.config.get<string>('API_BASE_URL', 'http://localhost:3000')}/api/integrations/google-calendar/callback`;
    this.dashboardUrl = this.config.get<string>('DASHBOARD_URL', 'http://localhost:5173');
  }

  isConfigured(): boolean {
    return !!(this.clientId && this.clientSecret);
  }

  getAuthUrl(userId: string, clinicId: string | null): string {
    if (!this.isConfigured()) {
      this.logger.warn('Google Calendar: integração não configurada (faltam variáveis de ambiente)');
      throw new UnauthorizedException(
        'Integração Google Calendar não configurada. Configure GOOGLE_CLIENT_ID e GOOGLE_CLIENT_SECRET.',
      );
    }
    const oauth2Client = createOAuth2Client(
      this.clientId,
      this.clientSecret,
      this.redirectUri,
    );
    const state = buildState(userId, clinicId);
    const url = getAuthorizationUrl(oauth2Client, state);
    this.logger.log(`[connect] URL de autorização gerada para userId=${userId}`);
    return url;
  }

  async handleCallback(code: string, state: string): Promise<{ success: boolean; redirectTo?: string }> {
    if (!this.isConfigured()) {
      this.logger.error('[callback] Integração não configurada');
      return { success: false };
    }

    const parsed = parseState(state);
    if (!parsed || !parsed.userId || typeof parsed.userId !== 'string') {
      this.logger.warn('[callback] State inválido ou ausente');
      return { success: false };
    }
    if (!isStateValid(parsed)) {
      this.logger.warn('[callback] State expirado');
      return { success: false };
    }

    const { userId, clinicId } = parsed;

    try {
      const oauth2Client = createOAuth2Client(
        this.clientId,
        this.clientSecret,
        this.redirectUri,
      );
      const { tokens } = await oauth2Client.getToken(code);

      if (!tokens.refresh_token) {
        this.logger.warn('[callback] Google não retornou refresh_token (usuário pode já ter autorizado)');
        return { success: false };
      }

      const config = {
        refresh_token: tokens.refresh_token,
        access_token: tokens.access_token,
        expiry_date: tokens.expiry_date,
      };

      const effectiveClinicId = clinicId || '_user';
      const existing = await this.prisma.userIntegration.findFirst({
        where: {
          userId,
          provider: PROVIDER,
          clinicId: effectiveClinicId === '_user' ? null : effectiveClinicId,
        },
      });

      if (existing) {
        await this.prisma.userIntegration.update({
          where: { id: existing.id },
          data: { config, status: 'connected', updatedAt: new Date() },
        });
        this.logger.log(`[callback] Integração atualizada para userId=${userId}`);
      } else {
        await this.prisma.userIntegration.create({
          data: {
            userId,
            clinicId: effectiveClinicId === '_user' ? null : effectiveClinicId,
            provider: PROVIDER,
            config,
            status: 'connected',
          },
        });
        this.logger.log(`[callback] Integração criada para userId=${userId}`);
      }

      const redirectTo = `${this.dashboardUrl}/settings/integrations`;
      return { success: true, redirectTo };
    } catch (err) {
      this.logger.error('[callback] Erro ao trocar code por tokens', err);
      return { success: false };
    }
  }

  getRedirectUrlOnError(): string {
    return `${this.dashboardUrl}/settings/integrations?google_calendar=error`;
  }

  getRedirectUrlOnSuccess(): string {
    return `${this.dashboardUrl}/settings/integrations`;
  }

  async getIntegration(userId: string, clinicId?: string | null) {
    const integration = await this.prisma.userIntegration.findFirst({
      where: {
        userId,
        provider: PROVIDER,
        clinicId: clinicId ?? null,
      },
    });
    return integration
      ? { connected: true, lastSyncAt: integration.lastSyncAt }
      : { connected: false };
  }

  async disconnect(userId: string, clinicId?: string | null): Promise<void> {
    await this.prisma.userIntegration.deleteMany({
      where: {
        userId,
        provider: PROVIDER,
        clinicId: clinicId ?? null,
      },
    });
    this.logger.log(`[disconnect] Integração removida para userId=${userId}`);
  }

  private async getCalendarClient(userId: string, clinicId?: string | null) {
    // 1) Tenta primeiro com clinicId exato (integração da clínica)
    let integration = await this.prisma.userIntegration.findFirst({
      where: { userId, provider: PROVIDER, clinicId: clinicId ?? null },
    });
    // 2) Se não encontrar e clinicId foi passado, tenta integração user-level (clinicId=null)
    if (!integration && clinicId) {
      integration = await this.prisma.userIntegration.findFirst({
        where: { userId, provider: PROVIDER, clinicId: null },
      });
    }
    if (!integration || integration.status !== 'connected') {
      return null;
    }
    const cfg = integration.config as { refresh_token?: string; expiry_date?: number };
    if (!cfg?.refresh_token) {
      return null;
    }

    const oauth2Client = createOAuth2Client(
      this.clientId,
      this.clientSecret,
      this.redirectUri,
    );
    oauth2Client.setCredentials({ refresh_token: cfg.refresh_token });

    try {
      await oauth2Client.getAccessToken();
    } catch (err) {
      this.logger.warn(`[getCalendarClient] Token expirado para userId=${userId}`, err);
      throw new GoogleCalendarTokenExpiredError();
    }

    return google.calendar({ version: 'v3', auth: oauth2Client });
  }

  /**
   * Cria um evento no Google Calendar do terapeuta.
   * @throws GoogleCalendarNotConnectedError se o usuário não tiver integração
   * @throws GoogleCalendarTokenExpiredError se o token expirou
   * @throws GoogleCalendarEventCreationError se falhar ao criar o evento
   */
  async createCalendarEvent(
    userId: string,
    clinicId: string | null,
    params: CreateCalendarEventParams,
  ): Promise<string> {
    const cal = await this.getCalendarClient(userId, clinicId);
    if (!cal) {
      throw new GoogleCalendarNotConnectedError(userId);
    }

    const body: calendar_v3.Schema$Event = {
      summary: params.title,
      description: params.description || undefined,
      start: {
        dateTime: params.startTime.toISOString(),
        timeZone: 'America/Sao_Paulo',
      },
      end: {
        dateTime: params.endTime.toISOString(),
        timeZone: 'America/Sao_Paulo',
      },
    };

    if (params.psiproAppointmentId) {
      body.extendedProperties = {
        private: { psiproAppointmentId: params.psiproAppointmentId },
      };
    }

    try {
      const res = await cal.events.insert({
        calendarId: 'primary',
        requestBody: body,
      });
      const eventId = res.data.id;
      this.logger.log(`[createCalendarEvent] Evento criado userId=${userId} eventId=${eventId}`);
      return eventId || '';
    } catch (err) {
      this.logger.error('[createCalendarEvent] Falha ao criar evento', err);
      throw new GoogleCalendarEventCreationError(
        'Não foi possível criar o evento no Google Calendar',
        err,
      );
    }
  }

  /**
   * Sincroniza um appointment do PsiPro para o Google Calendar.
   * Não lança exceção — registra erros em log para não bloquear o fluxo de criação de appointment.
   */
  async syncAppointmentToGoogle(
    userId: string,
    clinicId: string | null,
    appointment: {
      id: string;
      patientId: string;
      scheduledAt: Date;
      duration: number;
      type?: string | null;
      notes?: string | null;
      patientName?: string;
    },
    googleEventId?: string | null,
  ): Promise<string | null> {
    if (!this.isConfigured()) return null;

    try {
      const start = new Date(appointment.scheduledAt);
      const end = new Date(start.getTime() + (appointment.duration || 60) * 60 * 1000);
      const title = `PsiPro: ${appointment.patientName || 'Consulta'}${appointment.type ? ` (${appointment.type})` : ''}`;

      if (googleEventId) {
        const cal = await this.getCalendarClient(userId, clinicId);
        if (!cal) return null;
        await cal.events.update({
          calendarId: 'primary',
          eventId: googleEventId,
          requestBody: {
            summary: title,
            description: appointment.notes || undefined,
            start: { dateTime: start.toISOString(), timeZone: 'America/Sao_Paulo' },
            end: { dateTime: end.toISOString(), timeZone: 'America/Sao_Paulo' },
          },
        });
        return googleEventId;
      }

      const eventId = await this.createCalendarEvent(userId, clinicId, {
        title,
        description: appointment.notes || undefined,
        startTime: start,
        endTime: end,
        psiproAppointmentId: appointment.id,
      });
      return eventId || null;
    } catch (err) {
      if (err instanceof GoogleCalendarNotConnectedError) {
        this.logger.debug(`[syncAppointment] Usuário não conectado, ignorando`);
      } else if (err instanceof GoogleCalendarTokenExpiredError) {
        this.logger.warn(`[syncAppointment] Token expirado para userId=${userId}`);
      } else {
        this.logger.error(`[syncAppointment] Erro ao sincronizar appointment ${appointment.id}`, err);
      }
      return null;
    }
  }

  async deleteEventFromGoogle(
    userId: string,
    clinicId: string | null,
    googleEventId: string,
  ): Promise<boolean> {
    const cal = await this.getCalendarClient(userId, clinicId);
    if (!cal) return false;
    try {
      await cal.events.delete({ calendarId: 'primary', eventId: googleEventId });
      return true;
    } catch (err) {
      this.logger.error(`[deleteEvent] Erro ao remover evento ${googleEventId}`, err);
      return false;
    }
  }
}
