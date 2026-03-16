import { Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { VoiceService } from '../voice/voice.service';
import { extractAnamnesisText } from '../voice/voice.service';
import { whereNotDeleted } from '../prisma/soft-delete.helper';

export interface SessionEmotionEntry {
  sessionId: string;
  date: string;
  emotions: string[];
}

export interface EmotionFrequencyEntry {
  emotion: string;
  count: number;
}

export interface EmotionTrendEntry {
  emotion: string;
  trend: 'increasing' | 'decreasing' | 'stable';
}

export interface TimelineDataPoint {
  date: string;
  [emotion: string]: string | number;
}

export interface EmotionalEvolutionResponse {
  sessions: SessionEmotionEntry[];
  timelineData: TimelineDataPoint[];
  emotionFrequency: EmotionFrequencyEntry[];
  trend: EmotionTrendEntry[];
  /** Resumo narrativo da evolução emocional considerando anamnese e notas do profissional */
  aiContextSummary?: string;
}

type JsonValue = string | number | boolean | null | JsonValue[] | { [key: string]: JsonValue };

function extractEmotions(val: JsonValue | null | undefined): string[] {
  if (val == null) return [];
  if (Array.isArray(val)) {
    return val.map((v) => (typeof v === 'string' ? v.trim() : String(v))).filter(Boolean);
  }
  if (typeof val === 'object' && val !== null && !Array.isArray(val)) {
    const obj = val as Record<string, unknown>;
    if (Array.isArray(obj.emotions)) {
      return extractEmotions(obj.emotions as JsonValue);
    }
    if (obj.aiAnalysis && typeof obj.aiAnalysis === 'object') {
      const ai = obj.aiAnalysis as Record<string, unknown>;
      if (Array.isArray(ai.emotions)) {
        return extractEmotions(ai.emotions as JsonValue);
      }
    }
  }
  return [];
}

@Injectable()
export class EmotionalEvolutionService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly voiceService: VoiceService,
  ) {}

  async getEmotionalEvolution(patientId: string, clinicId: string): Promise<EmotionalEvolutionResponse> {
    const patient = await this.prisma.patient.findFirst({
      where: whereNotDeleted('patient', { id: patientId, clinicId }),
      select: { id: true, anamnesis: true, observations: true },
    });
    if (!patient) {
      throw new NotFoundException('Paciente não encontrado');
    }

    const sessions = await this.prisma.session.findMany({
      where: {
        ...whereNotDeleted('session', { patientId }),
        status: 'realizada',
      },
      select: { id: true, date: true, emotions: true, notes: true },
      orderBy: { date: 'asc' },
    });

    const sessionEntries: SessionEmotionEntry[] = sessions.map((s) => {
      const emotions = extractEmotions(s.emotions);
      return {
        sessionId: s.id,
        date: s.date.toISOString().split('T')[0],
        emotions,
      };
    });

    const emotionFrequency = this.computeEmotionFrequency(sessionEntries);
    const trend = this.computeTrend(sessionEntries);
    const timelineData = this.buildTimelineData(sessionEntries);

    const emotionSummary = emotionFrequency
      .map((e) => `${e.emotion}: ${e.count}x`)
      .join('; ');
    const sessionNotes = sessions.map((s) => s.notes || '').filter(Boolean);
    const aiContextSummary = await this.voiceService.generateEmotionalContextSummary(
      extractAnamnesisText(patient.anamnesis),
      patient.observations || '',
      sessionNotes,
      emotionSummary,
    );

    const result: EmotionalEvolutionResponse = {
      sessions: sessionEntries,
      timelineData,
      emotionFrequency,
      trend,
    };
    if (aiContextSummary) result.aiContextSummary = aiContextSummary;

    return result;
  }

  private buildTimelineData(sessions: SessionEmotionEntry[]): TimelineDataPoint[] {
    const allEmotions = new Set<string>();
    for (const s of sessions) {
      for (const e of s.emotions) {
        const k = e.toLowerCase().trim();
        if (k) allEmotions.add(k);
      }
    }
    const capitalize = (x: string) => (x ? x.charAt(0).toUpperCase() + x.slice(1).toLowerCase() : x);
    return sessions.map((s) => {
      const counts: Record<string, number> = {};
      for (const e of s.emotions) {
        const k = e.toLowerCase().trim();
        if (k) counts[k] = (counts[k] ?? 0) + 1;
      }
      const row: TimelineDataPoint = { date: s.date };
      for (const em of allEmotions) {
        row[capitalize(em)] = counts[em] ?? 0;
      }
      return row;
    });
  }

  private computeEmotionFrequency(sessions: SessionEmotionEntry[]): EmotionFrequencyEntry[] {
    const counts = new Map<string, number>();
    const capitalize = (x: string) => (x ? x.charAt(0).toUpperCase() + x.slice(1).toLowerCase() : x);
    for (const s of sessions) {
      for (const e of s.emotions) {
        const key = e.toLowerCase().trim();
        if (key) counts.set(key, (counts.get(key) ?? 0) + 1);
      }
    }
    return Array.from(counts.entries())
      .map(([emotion, count]) => ({ emotion: capitalize(emotion), count }))
      .sort((a, b) => b.count - a.count);
  }

  private computeTrend(sessions: SessionEmotionEntry[]): EmotionTrendEntry[] {
    if (sessions.length < 2) return [];
    const lastN = 5;
    const lastSessions = sessions.slice(-lastN);
    const previousSessions = sessions.slice(-lastN * 2, -lastN);
    if (previousSessions.length === 0) return [];

    const capitalize = (x: string) => (x ? x.charAt(0).toUpperCase() + x.slice(1).toLowerCase() : x);
    const allEmotions = new Set<string>();
    for (const s of [...lastSessions, ...previousSessions]) {
      for (const e of s.emotions) {
        if (e?.trim()) allEmotions.add(e.toLowerCase().trim());
      }
    }

    const result: EmotionTrendEntry[] = [];
    for (const emotion of allEmotions) {
      const lastCount = lastSessions.reduce(
        (acc, s) => acc + (s.emotions.some((e) => e.toLowerCase().trim() === emotion) ? 1 : 0),
        0,
      );
      const prevCount = previousSessions.reduce(
        (acc, s) => acc + (s.emotions.some((e) => e.toLowerCase().trim() === emotion) ? 1 : 0),
        0,
      );
      let trend: 'increasing' | 'decreasing' | 'stable' = 'stable';
      if (lastCount > prevCount) trend = 'increasing';
      else if (lastCount < prevCount) trend = 'decreasing';
      result.push({ emotion: capitalize(emotion), trend });
    }
    return result;
  }
}
