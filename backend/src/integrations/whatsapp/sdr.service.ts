import { Injectable, Logger } from '@nestjs/common';
import Anthropic from '@anthropic-ai/sdk';
import { PrismaService } from '../../prisma/prisma.service';
import { WhatsAppService } from './whatsapp.service';
import { AppointmentsService } from '../../appointments/appointments.service';
import { PatientsService } from '../../patients/patients.service';

interface SdrState {
  state: 'idle' | 'awaiting_patient_name' | 'awaiting_date' | 'awaiting_slot_choice' | 'awaiting_confirmation';
  patientId?: string;
  patientName?: string;
  chosenDate?: string; // YYYY-MM-DD
  offeredSlots?: Array<{ startTime: string; endTime: string; isoDateTime: string; label: string }>;
  chosenSlot?: { startTime: string; isoDateTime: string };
  lastInteractionAt?: string;
}

@Injectable()
export class SdrService {
  private readonly logger = new Logger(SdrService.name);
  private readonly anthropic = new Anthropic({
    apiKey: process.env.ANTHROPIC_API_KEY,
  });

  constructor(
    private prisma: PrismaService,
    private whatsApp: WhatsAppService,
    private appointmentsService: AppointmentsService,
    private patientsService: PatientsService,
  ) {}

  // ─── Entry point ────────────────────────────────────────────────────────────

  async processIncomingMessage(params: {
    userId: string;
    clinicId: string | null;
    conversationId: string;
    remoteJid: string;
    contactPhone: string;
    contactName: string | null;
    messageText: string;
  }): Promise<void> {
    const { userId, clinicId, conversationId, contactPhone, contactName, messageText } = params;

    // Busca estado atual da conversa SDR
    const conv = await this.prisma.whatsAppConversation.findUnique({
      where: { id: conversationId },
      select: { sdrState: true },
    });
    const state: SdrState = (conv?.sdrState as SdrState) ?? { state: 'idle' };

    // Reseta conversa se ficou mais de 24h sem interação
    if (state.lastInteractionAt) {
      const hoursSinceLastInteraction =
        (Date.now() - new Date(state.lastInteractionAt).getTime()) / 3_600_000;
      if (hoursSinceLastInteraction > 24) {
        state.state = 'idle';
        state.patientId = undefined;
        state.offeredSlots = undefined;
      }
    }

    state.lastInteractionAt = new Date().toISOString();

    const reply = await this.handleState(state, {
      userId,
      clinicId,
      contactPhone,
      contactName,
      messageText,
    });

    // Salva estado atualizado
    await this.prisma.whatsAppConversation.update({
      where: { id: conversationId },
      data: { sdrState: state as any },
    });

    if (reply) {
      await this.whatsApp.sendCustomMessage(userId, clinicId, contactPhone, reply);
    }
  }

  // ─── State machine ───────────────────────────────────────────────────────────

  private async handleState(
    state: SdrState,
    ctx: {
      userId: string;
      clinicId: string | null;
      contactPhone: string;
      contactName: string | null;
      messageText: string;
    },
  ): Promise<string | null> {
    const { messageText, contactPhone, contactName, userId, clinicId } = ctx;

    switch (state.state) {
      case 'idle':
        return this.handleIdle(state, ctx);

      case 'awaiting_patient_name':
        return this.handleAwaitingPatientName(state, ctx);

      case 'awaiting_date':
        return this.handleAwaitingDate(state, ctx);

      case 'awaiting_slot_choice':
        return this.handleAwaitingSlotChoice(state, ctx);

      case 'awaiting_confirmation':
        return this.handleAwaitingConfirmation(state, ctx);

      default:
        state.state = 'idle';
        return null;
    }
  }

  // ─── Idle: classifica intenção ───────────────────────────────────────────────

  private async handleIdle(
    state: SdrState,
    ctx: { userId: string; clinicId: string | null; contactPhone: string; contactName: string | null; messageText: string },
  ): Promise<string | null> {
    const intent = await this.classifyIntent(ctx.messageText);
    this.logger.log(`[SDR] Intent: ${intent} | msg: "${ctx.messageText}"`);

    if (intent === 'book_appointment') {
      // Tenta encontrar o paciente pelo telefone
      const patient = ctx.clinicId
        ? await this.patientsService.findByPhone(ctx.contactPhone, ctx.clinicId)
        : null;

      if (patient) {
        state.patientId = patient.id;
        state.patientName = patient.name;
        state.state = 'awaiting_date';
        const name = ctx.contactName || patient.name;
        return (
          `Olá, ${name}! 👋 Sou a assistente virtual da Dra. Claudia Cruz.\n\n` +
          `Ótimo, vou te ajudar a agendar uma consulta! 📅\n\n` +
          `Para qual data você gostaria? Pode me dizer assim:\n` +
          `_Ex: "amanhã", "sexta-feira", "20/04" ou "20 de abril"_`
        );
      } else {
        // Paciente não cadastrado, pede nome
        state.state = 'awaiting_patient_name';
        const name = ctx.contactName || 'você';
        return (
          `Olá, ${name}! 👋 Sou a assistente virtual da Dra. Claudia Cruz.\n\n` +
          `Ficaria feliz em te ajudar a agendar uma consulta! 😊\n\n` +
          `Pode me dizer seu *nome completo* para eu verificar seu cadastro?`
        );
      }
    }

    if (intent === 'confirm_appointment') {
      return (
        `Obrigada pelo contato! 😊\n\n` +
        `Para confirmar sua consulta, por favor entre em contato direto com a Dra. Claudia Cruz.\n\n` +
        `📞 Se precisar *agendar* uma nova consulta, é só me dizer!`
      );
    }

    if (intent === 'cancel_appointment') {
      return (
        `Entendido! Para cancelar ou reagendar sua consulta, entre em contato diretamente com a Dra. Claudia Cruz.\n\n` +
        `Se preferir, posso te ajudar a *agendar* um novo horário. É só me dizer! 📅`
      );
    }

    // Outras mensagens — responde gentilmente sem entrar em loop
    return null;
  }

  // ─── Aguardando nome do paciente ─────────────────────────────────────────────

  private async handleAwaitingPatientName(
    state: SdrState,
    ctx: { userId: string; clinicId: string | null; contactPhone: string; messageText: string; contactName: string | null },
  ): Promise<string | null> {
    const name = ctx.messageText.trim();
    if (name.length < 3) {
      return `Por favor, me diga seu *nome completo* para eu verificar seu cadastro. 😊`;
    }

    state.patientName = name;
    state.state = 'awaiting_date';

    return (
      `Obrigada, *${name}*! 😊\n\n` +
      `Para qual data você gostaria de agendar?\n` +
      `_Ex: "amanhã", "sexta-feira", "20/04" ou "20 de abril"_`
    );
  }

  // ─── Aguardando data ─────────────────────────────────────────────────────────

  private async handleAwaitingDate(
    state: SdrState,
    ctx: { userId: string; clinicId: string | null; messageText: string },
  ): Promise<string | null> {
    const date = await this.parseDate(ctx.messageText);
    if (!date) {
      return (
        `Não entendi a data. 😅 Pode tentar assim:\n` +
        `_Ex: "amanhã", "sexta-feira", "20/04" ou "20 de abril"_`
      );
    }

    const slots = await this.appointmentsService.getAvailableSlots(
      ctx.userId,
      ctx.clinicId!,
      date,
      60,
    );

    if (slots.length === 0) {
      return (
        `Infelizmente não há horários disponíveis para *${this.formatDatePtBr(date)}*. 😕\n\n` +
        `Gostaria de tentar outra data?`
      );
    }

    // Ofereça até 5 slots
    const offered = slots.slice(0, 5).map((s, i) => ({
      ...s,
      label: `*(${i + 1})* ${s.startTime} às ${s.endTime}`,
    }));
    state.offeredSlots = offered;
    state.chosenDate = date;
    state.state = 'awaiting_slot_choice';

    const slotList = offered.map((s) => s.label).join('\n');
    return (
      `Ótimo! 😊 Horários disponíveis para *${this.formatDatePtBr(date)}*:\n\n` +
      `${slotList}\n\n` +
      `Digite o *número* do horário que preferir.`
    );
  }

  // ─── Aguardando escolha de horário ───────────────────────────────────────────

  private async handleAwaitingSlotChoice(
    state: SdrState,
    ctx: { userId: string; clinicId: string | null; contactPhone: string; messageText: string },
  ): Promise<string | null> {
    const choice = parseInt(ctx.messageText.trim(), 10);
    const slots = state.offeredSlots ?? [];

    if (isNaN(choice) || choice < 1 || choice > slots.length) {
      // Talvez o paciente queira outra data
      const intentIsDate = await this.parseDate(ctx.messageText);
      if (intentIsDate) {
        // Processa como nova data
        return this.handleAwaitingDate(state, { ...ctx });
      }
      return `Por favor, responda com o *número* do horário desejado (1 a ${slots.length}). 😊`;
    }

    const chosen = slots[choice - 1];
    state.chosenSlot = chosen;
    state.state = 'awaiting_confirmation';

    const dateStr = this.formatDatePtBr(state.chosenDate!);
    return (
      `Perfeito! Confirma o agendamento?\n\n` +
      `📅 *Data:* ${dateStr}\n` +
      `⏰ *Horário:* ${chosen.startTime}\n` +
      `👩‍⚕️ *Terapeuta:* Dra. Claudia Cruz\n\n` +
      `*(1)* Sim, confirmar\n*(2)* Não, escolher outro horário`
    );
  }

  // ─── Aguardando confirmação final ────────────────────────────────────────────

  private async handleAwaitingConfirmation(
    state: SdrState,
    ctx: { userId: string; clinicId: string | null; contactPhone: string; messageText: string },
  ): Promise<string | null> {
    const text = ctx.messageText.trim().toLowerCase();
    const isYes = text === '1' || text.includes('sim') || text.includes('confirmo') || text.includes('ok') || text.includes('yes');
    const isNo = text === '2' || text.includes('não') || text.includes('nao') || text.includes('outro') || text.includes('cancel');

    if (isYes) {
      return this.createAppointment(state, ctx);
    }

    if (isNo) {
      state.state = 'awaiting_date';
      state.chosenSlot = undefined;
      state.offeredSlots = undefined;
      return `Tudo bem! Para qual data você gostaria de tentar? 📅`;
    }

    return `Por favor, responda *(1)* para confirmar ou *(2)* para escolher outro horário.`;
  }

  // ─── Cria o agendamento ──────────────────────────────────────────────────────

  private async createAppointment(
    state: SdrState,
    ctx: { userId: string; clinicId: string | null; contactPhone: string },
  ): Promise<string> {
    try {
      // Encontra ou cria paciente
      let patientId = state.patientId;

      if (!patientId && ctx.clinicId) {
        const patient = await this.patientsService.findByPhone(ctx.contactPhone, ctx.clinicId);
        if (patient) {
          patientId = patient.id;
        }
      }

      if (!patientId) {
        state.state = 'idle';
        return (
          `Não consegui encontrar seu cadastro no sistema. 😕\n\n` +
          `Por favor, entre em contato com a Dra. Claudia Cruz para se cadastrar e depois poderemos agendar via WhatsApp!`
        );
      }

      if (!ctx.clinicId) throw new Error('clinicId ausente');

      await this.appointmentsService.create(
        ctx.userId,
        {
          patientId,
          scheduledAt: state.chosenSlot!.isoDateTime,
          duration: 60,
          type: 'Consulta regular',
          status: 'confirmada',
          notes: 'Agendado via WhatsApp (SDR)',
        },
        ctx.clinicId,
      );

      state.state = 'idle';
      state.patientId = undefined;
      state.chosenSlot = undefined;
      state.offeredSlots = undefined;

      const dateStr = this.formatDatePtBr(state.chosenDate!);
      return (
        `✅ *Consulta confirmada!*\n\n` +
        `📅 *Data:* ${dateStr}\n` +
        `⏰ *Horário:* ${state.chosenSlot?.startTime ?? ''}\n` +
        `👩‍⚕️ *Terapeuta:* Dra. Claudia Cruz\n\n` +
        `Você receberá lembretes antes da consulta. Em caso de dúvidas ou reagendamento, entre em contato. Até lá! 😊`
      );
    } catch (err) {
      this.logger.error('[SDR] Erro ao criar agendamento', err);
      state.state = 'idle';
      return (
        `Ops! Tive um problema ao confirmar o agendamento. 😕\n\n` +
        `Por favor, entre em contato diretamente com a Dra. Claudia Cruz para finalizar.`
      );
    }
  }

  // ─── Claude: classifica intenção ────────────────────────────────────────────

  private async classifyIntent(
    text: string,
  ): Promise<'book_appointment' | 'confirm_appointment' | 'cancel_appointment' | 'other'> {
    try {
      const response = await this.anthropic.messages.create({
        model: 'claude-haiku-4-5-20251001',
        max_tokens: 20,
        system:
          'You are an intent classifier for a psychology clinic WhatsApp bot (Brazilian Portuguese). ' +
          'Classify the user message as exactly one of: book_appointment, confirm_appointment, cancel_appointment, other. ' +
          'Reply with ONLY the intent label, nothing else.',
        messages: [{ role: 'user', content: text }],
      });
      const label = (response.content[0] as any).text?.trim().toLowerCase() ?? 'other';
      if (['book_appointment', 'confirm_appointment', 'cancel_appointment'].includes(label)) {
        return label as any;
      }
      return 'other';
    } catch (err) {
      this.logger.warn('[SDR] Claude classify error, fallback regex', err);
      return this.fallbackClassify(text);
    }
  }

  private fallbackClassify(text: string): 'book_appointment' | 'confirm_appointment' | 'cancel_appointment' | 'other' {
    const t = text.toLowerCase();
    if (/agendar|marcar|consulta|atendimento|horário|disponível/.test(t)) return 'book_appointment';
    if (/confirmar|confirmado|confirmo/.test(t)) return 'confirm_appointment';
    if (/cancelar|cancelamento|desmarcar|remarcar|reagendar/.test(t)) return 'cancel_appointment';
    return 'other';
  }

  // ─── Utilitários ─────────────────────────────────────────────────────────────

  private async parseDate(text: string): Promise<string | null> {
    const t = text.toLowerCase().trim();
    const now = new Date();

    // "amanhã"
    if (/amanh[ãa]/.test(t)) {
      const d = new Date(now);
      d.setDate(d.getDate() + 1);
      return this.toYMD(d);
    }

    // Dias da semana
    const weekdays: Record<string, number> = {
      domingo: 0, segunda: 1, 'segunda-feira': 1, terça: 2, 'terça-feira': 2,
      quarta: 3, 'quarta-feira': 3, quinta: 4, 'quinta-feira': 4,
      sexta: 5, 'sexta-feira': 5, sábado: 6, sabado: 6,
    };
    for (const [name, dow] of Object.entries(weekdays)) {
      if (t.includes(name)) {
        const d = new Date(now);
        const diff = (dow - d.getDay() + 7) % 7 || 7;
        d.setDate(d.getDate() + diff);
        return this.toYMD(d);
      }
    }

    // DD/MM ou DD de MMMM
    const dmMatch = t.match(/(\d{1,2})[\/\-](\d{1,2})/);
    if (dmMatch) {
      const day = parseInt(dmMatch[1], 10);
      const month = parseInt(dmMatch[2], 10) - 1;
      const year = now.getFullYear();
      const d = new Date(year, month, day);
      if (d < now) d.setFullYear(year + 1);
      return this.toYMD(d);
    }

    const months: Record<string, number> = {
      janeiro: 0, fevereiro: 1, março: 2, marco: 2, abril: 3, maio: 4, junho: 5,
      julho: 6, agosto: 7, setembro: 8, outubro: 9, novembro: 10, dezembro: 11,
    };
    const longMatch = t.match(/(\d{1,2})\s+de\s+(\w+)/);
    if (longMatch) {
      const day = parseInt(longMatch[1], 10);
      const monthName = longMatch[2];
      const month = months[monthName];
      if (month !== undefined) {
        const year = now.getFullYear();
        const d = new Date(year, month, day);
        if (d < now) d.setFullYear(year + 1);
        return this.toYMD(d);
      }
    }

    return null;
  }

  private toYMD(d: Date): string {
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  }

  private formatDatePtBr(ymd: string): string {
    const [year, month, day] = ymd.split('-');
    const monthNames = [
      'janeiro', 'fevereiro', 'março', 'abril', 'maio', 'junho',
      'julho', 'agosto', 'setembro', 'outubro', 'novembro', 'dezembro',
    ];
    const weekdays = ['domingo', 'segunda-feira', 'terça-feira', 'quarta-feira', 'quinta-feira', 'sexta-feira', 'sábado'];
    const d = new Date(parseInt(year), parseInt(month) - 1, parseInt(day));
    return `${weekdays[d.getDay()]}, ${day} de ${monthNames[parseInt(month) - 1]}`;
  }
}
