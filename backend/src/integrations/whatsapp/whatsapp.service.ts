import { Injectable, Logger, forwardRef, Inject } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { PrismaService } from '../../prisma/prisma.service';
import { Cron, CronExpression } from '@nestjs/schedule';
import type { SdrService } from './sdr.service';

const PROVIDER = 'whatsapp';

export interface WhatsAppConfig {
  provider?: 'zapi' | 'evolution'; // 'evolution' = Evolution GO (padrão novo)
  // Evolution GO
  evolutionApiUrl?: string;
  evolutionInstanceToken?: string;
  /** Nome técnico da instância no Evolution (ex.: TerapeutaClaudiaCruz) — necessário para testar com token de instância */
  evolutionInstanceName?: string;
  // Z-API (legado)
  instanceId?: string;
  token?: string;
  clientToken?: string;
  phoneNumber?: string;
}

@Injectable()
export class WhatsAppService {
  private readonly logger = new Logger(WhatsAppService.name);

  constructor(
    private config: ConfigService,
    private prisma: PrismaService,
    @Inject(forwardRef(() => 'SdrService')) private sdr?: SdrService,
  ) {}

  // ─── Evolution GO helpers ───────────────────────────────────────────────────

  private normalizePhone(phone: string): string {
    const digits = phone.replace(/\D/g, '');
    return digits.startsWith('55') ? digits : `55${digits}`;
  }

  /** Remove barras finais (trimEnd não aceita delimitador em lib dom padrão). */
  private trimTrailingSlashes(s: string): string {
    return s.trim().replace(/\/+$/, '');
  }

  private async sendEvolutionMessage(
    apiUrl: string,
    instanceToken: string,
    phone: string,
    message: string,
  ): Promise<boolean> {
    const url = `${this.trimTrailingSlashes(apiUrl)}/send/text`;
    const number = this.normalizePhone(phone);
    try {
      const res = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', apikey: instanceToken },
        body: JSON.stringify({ number, text: message }),
      });
      if (!res.ok) {
        const body = await res.text();
        this.logger.warn(`[Evolution GO] Falha ao enviar: ${res.status} — ${body}`);
        return false;
      }
      this.logger.log(`[Evolution GO] Mensagem enviada para ${number}`);
      return true;
    } catch (err) {
      this.logger.error('[Evolution GO] Erro de rede', err);
      return false;
    }
  }

  /**
   * Valida URL + token da instância.
   * - **Evolution GO** (Go): `GET /instance/status` + header `apikey` — não usa `connectionState`.
   * - **Evolution API** (Node v2): `GET /instance/connectionState/{nome}`.
   * Rotas sob `/manager/*` devolvem HTML do painel; não contam como sucesso.
   */
  private async testEvolutionConnection(
    apiUrl: string,
    instanceToken: string,
    instanceName?: string,
  ): Promise<boolean> {
    const base = this.trimTrailingSlashes(apiUrl);
    const token = instanceToken.trim();
    const headers: Record<string, string> = { apikey: token };

    const probe = async (path: string): Promise<{ ok: boolean; status: number }> => {
      try {
        const res = await fetch(`${base}${path}`, { method: 'GET', headers });
        const ct = (res.headers.get('content-type') ?? '').toLowerCase();
        if (res.ok && ct.includes('text/html')) {
          this.logger.warn(
            `[Evolution test] GET ${path} -> ${res.status} (HTML do Manager/SPA; ignorado)`,
          );
          return { ok: false, status: res.status };
        }
        const ok = res.ok && !ct.includes('text/html');
        return { ok, status: res.status };
      } catch {
        return { ok: false, status: 0 };
      }
    };

    const name = instanceName?.trim();

    // 1) Evolution GO — documentação: GET /instance/status com token da instância
    {
      const { ok, status } = await probe('/instance/status');
      this.logger.log(`[Evolution test] Evolution GO GET /instance/status -> ${status}`);
      if (ok) return true;
    }

    // 2) Evolution API (Node) v2
    if (name) {
      for (const p of [
        `/instance/connectionState/${encodeURIComponent(name)}`,
        `/instance/connectionState/${name}`,
      ]) {
        const { ok, status } = await probe(p);
        this.logger.log(`[Evolution test] Evolution API GET ${p} -> ${status}`);
        if (ok) return true;
      }
    }

    // 3) Lista (normalmente exige API global; às vezes aceita token de instância)
    {
      const { ok, status } = await probe('/instance/all');
      this.logger.log(`[Evolution test] GET /instance/all -> ${status}`);
      if (ok) return true;
    }

    if (name) {
      const { ok, status } = await probe(
        `/api/instance/connectionState/${encodeURIComponent(name)}`,
      );
      this.logger.log(`[Evolution test] GET /api/instance/connectionState/... -> ${status}`);
      if (ok) return true;
    }

    return false;
  }

  // ─── Z-API helpers (legado) ─────────────────────────────────────────────────

  private async sendZApiMessage(
    instanceId: string,
    token: string,
    clientToken: string,
    phone: string,
    message: string,
  ): Promise<boolean> {
    const normalized = this.normalizePhone(phone);
    const url = `https://api.z-api.io/instances/${instanceId}/token/${token}/send-text`;
    try {
      const res = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Client-Token': clientToken },
        body: JSON.stringify({ phone: normalized, message }),
      });
      if (!res.ok) {
        this.logger.warn(`[Z-API] Falha: ${res.status}`);
        return false;
      }
      return true;
    } catch (err) {
      this.logger.error('[Z-API] Erro de rede', err);
      return false;
    }
  }

  // ─── Método unificado de envio ──────────────────────────────────────────────

  private async sendMessage(cfg: WhatsAppConfig, phone: string, message: string): Promise<boolean> {
    if (cfg.provider === 'evolution' || cfg.evolutionApiUrl) {
      return this.sendEvolutionMessage(
        cfg.evolutionApiUrl!,
        cfg.evolutionInstanceToken!,
        phone,
        message,
      );
    }
    // fallback Z-API
    return this.sendZApiMessage(cfg.instanceId!, cfg.token!, cfg.clientToken!, phone, message);
  }

  // ─── Integração por clínica ────────────────────────────────────────────────

  async getIntegration(userId: string, clinicId?: string | null) {
    const integration = await this.prisma.userIntegration.findFirst({
      where: { userId, provider: PROVIDER, clinicId: clinicId ?? null },
    });
    if (!integration) return { connected: false };
    const cfg = integration.config as WhatsAppConfig;
    return {
      connected: integration.status === 'connected',
      provider: cfg?.provider ?? 'zapi',
      phoneNumber: cfg?.phoneNumber ?? null,
      lastSyncAt: integration.lastSyncAt,
    };
  }

  async connect(
    userId: string,
    clinicId: string | null,
    cfg: WhatsAppConfig,
  ): Promise<{ success: boolean; error?: string }> {
    // Testa conexão
    let testOk: boolean;
    if (cfg.provider === 'evolution' || cfg.evolutionApiUrl) {
      testOk = await this.testEvolutionConnection(
        cfg.evolutionApiUrl!,
        cfg.evolutionInstanceToken!,
        cfg.evolutionInstanceName,
      );
      if (!testOk) {
        return {
          success: false,
          error:
            'Não foi possível validar a Evolution. Confira a URL base (host da API, sem /manager), o token da instância (apikey no Manager) e o nome técnico se usar Evolution API Node. Evolution GO usa GET /instance/status — token incorreto costuma retornar 401.',
        };
      }
    } else {
      testOk = await this.testZApiConnection(cfg);
      if (!testOk) return { success: false, error: 'Não foi possível conectar à Z-API. Verifique as credenciais.' };
    }

    const existing = await this.prisma.userIntegration.findFirst({
      where: { userId, provider: PROVIDER, clinicId: clinicId ?? null },
    });

    if (existing) {
      await this.prisma.userIntegration.update({
        where: { id: existing.id },
        data: { config: cfg as any, status: 'connected', updatedAt: new Date() },
      });
    } else {
      await this.prisma.userIntegration.create({
        data: { userId, clinicId: clinicId ?? null, provider: PROVIDER, config: cfg as any, status: 'connected' },
      });
    }

    this.logger.log(`[connect] WhatsApp (${cfg.provider ?? 'zapi'}) conectado para userId=${userId}`);
    return { success: true };
  }

  async disconnect(userId: string, clinicId?: string | null): Promise<void> {
    await this.prisma.userIntegration.deleteMany({
      where: { userId, provider: PROVIDER, clinicId: clinicId ?? null },
    });
  }

  async testConnection(cfg: WhatsAppConfig): Promise<boolean> {
    if (cfg.provider === 'evolution' || cfg.evolutionApiUrl) {
      return this.testEvolutionConnection(
        cfg.evolutionApiUrl!,
        cfg.evolutionInstanceToken!,
        cfg.evolutionInstanceName,
      );
    }
    return this.testZApiConnection(cfg);
  }

  private async testZApiConnection(cfg: WhatsAppConfig): Promise<boolean> {
    const url = `https://api.z-api.io/instances/${cfg.instanceId}/token/${cfg.token}/status`;
    try {
      const res = await fetch(url, { headers: { 'Client-Token': cfg.clientToken! } });
      return res.ok;
    } catch { return false; }
  }

  // ─── Envio de mensagens ────────────────────────────────────────────────────

  async sendReminderToPatient(
    userId: string,
    clinicId: string | null,
    patient: { name: string; phone: string },
    appointment: { scheduledAt: Date; therapistName: string },
  ): Promise<boolean> {
    const integration = await this.prisma.userIntegration.findFirst({
      where: { userId, provider: PROVIDER, clinicId: clinicId ?? null, status: 'connected' },
    });
    if (!integration) return false;

    const cfg = integration.config as WhatsAppConfig;
    const date = new Date(appointment.scheduledAt);
    const dateStr = date.toLocaleDateString('pt-BR', { weekday: 'long', day: '2-digit', month: 'long' });
    const timeStr = date.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });

    const message =
      `Olá ${patient.name}! 👋\n\n` +
      `Lembrete da sua consulta com *${appointment.therapistName}*:\n\n` +
      `📅 *${dateStr}*\n` +
      `⏰ *${timeStr}*\n\n` +
      `Em caso de dúvidas ou necessidade de reagendamento, entre em contato.\n\n` +
      `_Mensagem automática enviada pelo PsiPro_ 🧠`;

    return this.sendMessage(cfg, patient.phone, message);
  }

  async sendConfirmationToPatient(
    userId: string,
    clinicId: string | null,
    patient: { name: string; phone: string },
    appointment: { scheduledAt: Date; therapistName: string },
  ): Promise<boolean> {
    const integration = await this.prisma.userIntegration.findFirst({
      where: { userId, provider: PROVIDER, clinicId: clinicId ?? null, status: 'connected' },
    });
    if (!integration) return false;

    const cfg = integration.config as WhatsAppConfig;
    const date = new Date(appointment.scheduledAt);
    const dateStr = date.toLocaleDateString('pt-BR', { weekday: 'long', day: '2-digit', month: 'long' });
    const timeStr = date.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });

    const message =
      `Olá ${patient.name}! 😊\n\n` +
      `Sua consulta foi agendada com sucesso!\n\n` +
      `👩‍⚕️ *Terapeuta:* ${appointment.therapistName}\n` +
      `📅 *Data:* ${dateStr}\n` +
      `⏰ *Horário:* ${timeStr}\n\n` +
      `Caso precise reagendar ou cancelar, entre em contato com antecedência.\n\n` +
      `_PsiPro — Plataforma de Psicologia_ 🧠`;

    return this.sendMessage(cfg, patient.phone, message);
  }

  async sendCustomMessage(
    userId: string,
    clinicId: string | null,
    phone: string,
    message: string,
  ): Promise<boolean> {
    const integration = await this.prisma.userIntegration.findFirst({
      where: { userId, provider: PROVIDER, clinicId: clinicId ?? null, status: 'connected' },
    });
    if (!integration) return false;
    const cfg = integration.config as WhatsAppConfig;
    return this.sendMessage(cfg, phone, message);
  }

  // ─── Normaliza payload do Evolution GO / Evolution API ─────────────────────

  private normalizeEvolutionPayload(
    payload: any,
  ): { event: string; instanceName: string; data: any } | null {
    if (!payload) return null;

    // Formato Evolution API (Node): { event, instance, data }
    if (payload.event && payload.instance && payload.data) {
      return { event: payload.event, instanceName: payload.instance, data: payload.data };
    }

    // Formato Evolution GO: { event, data: { instanceName, ... } }
    if (payload.event && payload.data?.instanceName) {
      return { event: payload.event, instanceName: payload.data.instanceName, data: payload.data };
    }

    // Formato Evolution GO alternativo: { type/event, instance: { instanceName }, ... }
    if (payload.instance?.instanceName) {
      return {
        event: payload.event ?? payload.type ?? 'Message',
        instanceName: payload.instance.instanceName,
        data: payload,
      };
    }

    // Formato plano (alguns builds do Evolution GO): { event, sender, body }
    if (payload.event && (payload.sender || payload.phone)) {
      return {
        event: payload.event,
        instanceName: payload.instanceName ?? payload.instance ?? '',
        data: payload,
      };
    }

    // Log do payload desconhecido para diagnóstico
    this.logger.warn(`[webhook] Formato desconhecido: ${JSON.stringify(payload).slice(0, 200)}`);
    return null;
  }

  // ─── Webhook: recebe eventos do Evolution GO ───────────────────────────────

  async handleWebhook(payload: any): Promise<void> {
    // Evolution GO (Go) usa formato diferente do Evolution API (Node)
    // Normaliza para estrutura interna comum
    const normalized = this.normalizeEvolutionPayload(payload);
    if (!normalized) return;

    const { event, instanceName, data } = normalized;
    const MESSAGE_EVENTS = ['messages.upsert', 'message', 'Message', 'MESSAGES_UPSERT'];
    if (!MESSAGE_EVENTS.includes(event)) return;

    const key = data.key ?? {};
    // Evolution GO pode ter remoteJid direto no data ou dentro de key
    const remoteJid: string = key.remoteJid ?? data.remoteJid ?? data.phone ?? '';
    const fromMe: boolean = key.fromMe ?? data.fromMe ?? false;
    const remoteId: string = key.id ?? data.id ?? data.messageId ?? `${Date.now()}`;
    const pushName: string = data.pushName ?? data.senderName ?? data.name ?? '';
    const timestampRaw = data.messageTimestamp ?? data.timestamp ?? data.dateTime;
    const timestamp = timestampRaw
      ? new Date(Number(timestampRaw) * (timestampRaw < 1e12 ? 1000 : 1)) // suporta segundos e ms
      : new Date();

    // Extrai texto da mensagem (suporte a vários tipos e formatos)
    const msgObj = data.message ?? {};
    const content: string =
      msgObj.conversation ??
      msgObj.extendedTextMessage?.text ??
      msgObj.imageMessage?.caption ??
      msgObj.videoMessage?.caption ??
      msgObj.documentMessage?.title ??
      data.body ??          // Evolution GO plano
      data.text ??          // alternativo
      data.caption ??       // alternativo
      '[mensagem não textual]';

    const messageType = msgObj.conversation || msgObj.extendedTextMessage
      ? 'text'
      : msgObj.imageMessage ? 'image'
      : msgObj.audioMessage ? 'audio'
      : msgObj.videoMessage ? 'video'
      : msgObj.documentMessage ? 'document'
      : msgObj.stickerMessage ? 'sticker'
      : 'text';

    if (!remoteJid || !remoteId || !instanceName) return;

    // Encontra o usuário dono desta instância pelo config salvo
    const integration = await this.prisma.userIntegration.findFirst({
      where: {
        provider: PROVIDER,
        status: 'connected',
        config: { path: ['evolutionInstanceName'], equals: instanceName },
      },
    });

    // Fallback: tenta por instanceToken que inclua o nome (busca all e filtra)
    let userId: string | null = integration?.userId ?? null;
    let clinicId: string | null = integration?.clinicId ?? null;

    if (!userId) {
      const allIntegrations = await this.prisma.userIntegration.findMany({
        where: { provider: PROVIDER, status: 'connected' },
      });
      const found = allIntegrations.find((i) => {
        const cfg = i.config as any;
        return (
          cfg?.evolutionInstanceName === instanceName ||
          cfg?.instanceName === instanceName
        );
      });
      if (found) {
        userId = found.userId;
        clinicId = found.clinicId ?? null;
      }
    }

    if (!userId) {
      this.logger.warn(`[webhook] Instância desconhecida: ${instanceName}`);
      return;
    }

    // Normaliza número do contato
    const contactPhone = remoteJid.split('@')[0];

    // Upsert da conversa
    const conversation = await this.prisma.whatsAppConversation.upsert({
      where: {
        userId_instanceName_remoteJid: { userId, instanceName, remoteJid },
      },
      create: {
        userId,
        clinicId,
        instanceName,
        remoteJid,
        contactName: pushName || null,
        contactPhone,
        lastMessageAt: timestamp,
        lastMessagePreview: content.slice(0, 100),
        unreadCount: fromMe ? 0 : 1,
      },
      update: {
        contactName: pushName || undefined,
        lastMessageAt: timestamp,
        lastMessagePreview: content.slice(0, 100),
        unreadCount: fromMe
          ? undefined
          : { increment: 1 },
      },
    });

    // Upsert da mensagem (ignora duplicatas pelo remoteId)
    await this.prisma.whatsAppMessage.upsert({
      where: { conversationId_remoteId: { conversationId: conversation.id, remoteId } },
      create: {
        conversationId: conversation.id,
        remoteId,
        fromMe,
        content,
        messageType,
        status: fromMe ? 'sent' : 'received',
        timestamp,
      },
      update: {},
    });

    this.logger.log(`[webhook] Mensagem salva: ${instanceName} ← ${remoteJid} (fromMe=${fromMe})`);

    // Relay para o psipro-chat (Supabase) — não bloqueia e ignora falhas
    const chatWebhookUrl = process.env.PSIPRO_CHAT_WEBHOOK_URL;
    const supabaseAnonKey = process.env.SUPABASE_ANON_KEY ?? '';
    if (chatWebhookUrl) {
      fetch(chatWebhookUrl, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...(supabaseAnonKey
            ? { 'Authorization': `Bearer ${supabaseAnonKey}`, 'apikey': supabaseAnonKey }
            : {}),
        },
        body: JSON.stringify(payload),
      }).catch((err) => this.logger.warn(`[webhook] Relay para psipro-chat falhou: ${err.message}`));
    }

    // Aciona SDR apenas para mensagens recebidas (não enviadas pelo terapeuta)
    if (!fromMe && this.sdr && content !== '[mensagem não textual]') {
      this.sdr
        .processIncomingMessage({
          userId,
          clinicId,
          conversationId: conversation.id,
          remoteJid,
          contactPhone: contactPhone,
          contactName: pushName || null,
          messageText: content,
        })
        .catch((err) => this.logger.error('[SDR] Erro ao processar mensagem', err));
    }
  }

  // ─── Conversas e mensagens ─────────────────────────────────────────────────

  async getConversations(userId: string, clinicId?: string | null) {
    return this.prisma.whatsAppConversation.findMany({
      where: { userId, clinicId: clinicId ?? null },
      orderBy: { lastMessageAt: 'desc' },
      select: {
        id: true,
        instanceName: true,
        remoteJid: true,
        contactName: true,
        contactPhone: true,
        unreadCount: true,
        lastMessageAt: true,
        lastMessagePreview: true,
      },
    });
  }

  async getMessages(userId: string, conversationId: string, take = 50, skip = 0) {
    // Verifica que a conversa pertence ao usuário
    const conv = await this.prisma.whatsAppConversation.findFirst({
      where: { id: conversationId, userId },
    });
    if (!conv) return null;

    // Zera unread
    await this.prisma.whatsAppConversation.update({
      where: { id: conversationId },
      data: { unreadCount: 0 },
    });

    return this.prisma.whatsAppMessage.findMany({
      where: { conversationId },
      orderBy: { timestamp: 'asc' },
      take,
      skip,
      select: {
        id: true,
        remoteId: true,
        fromMe: true,
        content: true,
        messageType: true,
        status: true,
        timestamp: true,
      },
    });
  }

  // ─── Cron: lembretes automáticos 24h antes ─────────────────────────────────

  /** Envia lembrete genérico para uma janela de horas futura */
  private async sendRemindersForWindow(
    windowHours: number,
    toleranceHours = 1,
    label: string,
  ): Promise<void> {
    const now = new Date();
    const windowStart = new Date(now.getTime() + (windowHours - toleranceHours) * 3_600_000);
    const windowEnd = new Date(now.getTime() + (windowHours + toleranceHours) * 3_600_000);
    const flagField = `reminder${label}SentAt`;

    const appointments = await this.prisma.appointment.findMany({
      where: {
        scheduledAt: { gte: windowStart, lte: windowEnd },
        status: { in: ['agendada', 'confirmada'] },
        deletedAt: null,
        ...(label === '24h' ? { reminderSentAt: null } : {}),
      },
      include: {
        patient: { select: { name: true, phone: true } },
        user: { select: { id: true, name: true } },
      },
    });

    this.logger.log(`[cron-${label}] ${appointments.length} consultas para lembrete em ${windowHours}h`);

    for (const appt of appointments) {
      if (!appt.patient?.phone) continue;
      const therapistName = (appt.user.name ?? '').trim() || 'Terapeuta';
      const date = new Date(appt.scheduledAt);
      const timeStr = date.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit', timeZone: 'America/Sao_Paulo' });

      let message: string;
      if (label === '12h') {
        message =
          `Olá, ${appt.patient.name}! 👋\n\n` +
          `Lembrete: você tem uma consulta *hoje* com *${therapistName}* às *${timeStr}*. 🗓️\n\n` +
          `Até logo!`;
      } else {
        message =
          `Olá, ${appt.patient.name}! ⏰\n\n` +
          `Sua consulta com *${therapistName}* é em *2 horas* (${timeStr}). Não se esqueça! 😊`;
      }

      const integration = await this.prisma.userIntegration.findFirst({
        where: { userId: appt.userId, provider: PROVIDER, status: 'connected' },
      });
      if (!integration) continue;

      const cfg = integration.config as WhatsAppConfig;
      await this.sendMessage(cfg, appt.patient.phone, message).catch(() => {});
      this.logger.log(`[cron-${label}] Lembrete enviado para ${appt.patient.name}`);
    }
  }

  @Cron(CronExpression.EVERY_HOUR)
  async sendScheduledReminders(): Promise<void> {
    const now = new Date();
    const windowStart = new Date(now.getTime() + 23 * 60 * 60 * 1000);
    const windowEnd = new Date(now.getTime() + 25 * 60 * 60 * 1000);

    const appointments = await this.prisma.appointment.findMany({
      where: {
        scheduledAt: { gte: windowStart, lte: windowEnd },
        status: { in: ['agendada', 'confirmada'] },
        reminderSentAt: null,
        deletedAt: null,
      },
      include: {
        patient: { select: { name: true, phone: true } },
        user: { select: { id: true, name: true } },
      },
    });

    this.logger.log(`[cron] ${appointments.length} consultas para lembrete nas próximas 24h`);

    for (const appt of appointments) {
      if (!appt.patient?.phone) continue;
      const therapistName = (appt.user.name ?? '').trim() || 'Terapeuta';

      const sent = await this.sendReminderToPatient(
        appt.userId,
        appt.clinicId ?? null,
        { name: appt.patient.name, phone: appt.patient.phone },
        { scheduledAt: appt.scheduledAt, therapistName },
      );

      if (sent) {
        await this.prisma.appointment.update({
          where: { id: appt.id },
          data: { reminderSentAt: new Date() },
        });
        this.logger.log(`[cron] Lembrete enviado para ${appt.patient.name}`);
      }
    }
  }

  /** Lembrete 12h antes (roda a cada hora, janela ±1h) */
  @Cron(CronExpression.EVERY_HOUR)
  async sendReminders12h(): Promise<void> {
    await this.sendRemindersForWindow(12, 1, '12h');
  }

  /** Lembrete 2h antes (roda a cada 30 minutos) */
  @Cron('*/30 * * * *')
  async sendReminders2h(): Promise<void> {
    await this.sendRemindersForWindow(2, 0.5, '2h');
  }
}
