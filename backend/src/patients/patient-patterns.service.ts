import { Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { whereNotDeleted } from '../prisma/soft-delete.helper';

export interface PatientPatternsResponse {
  dominantThemes: string[];
  dominantEmotions: string[];
  patterns: string[];
  alerts: string[];
}

type JsonValue = string | number | boolean | null | JsonValue[] | { [key: string]: JsonValue };

function toStrArray(val: JsonValue | null | undefined): string[] {
  if (val == null) return [];
  if (Array.isArray(val)) {
    return val.map((v) => (typeof v === 'string' ? v.trim() : String(v))).filter(Boolean);
  }
  return [];
}

@Injectable()
export class PatientPatternsService {
  constructor(private readonly prisma: PrismaService) {}

  async getPatterns(patientId: string, clinicId: string): Promise<PatientPatternsResponse> {
    const patient = await this.prisma.patient.findFirst({
      where: whereNotDeleted('patient', { id: patientId, clinicId }),
      select: { id: true },
    });
    if (!patient) {
      throw new NotFoundException('Paciente não encontrado');
    }

    const sessions = await this.prisma.session.findMany({
      where: {
        ...whereNotDeleted('session', { patientId }),
        status: 'realizada',
      },
      select: {
        id: true,
        date: true,
        themes: true,
        emotions: true,
        riskFlags: true,
      },
      orderBy: { date: 'asc' },
    });

    const dominantThemes = this.computeDominantThemes(sessions);
    const dominantEmotions = this.computeDominantEmotions(sessions);
    const patterns = this.computePatterns(sessions, dominantThemes, dominantEmotions);
    const alerts = this.computeAlerts(sessions);

    return {
      dominantThemes,
      dominantEmotions,
      patterns,
      alerts,
    };
  }

  private computeDominantThemes(
    sessions: { themes: JsonValue }[],
  ): string[] {
    const counts = new Map<string, number>();
    for (const s of sessions) {
      const themes = toStrArray(s.themes);
      for (const t of themes) {
        const key = t.toLowerCase().trim();
        counts.set(key, (counts.get(key) ?? 0) + 1);
      }
    }
    return this.topByFrequency(counts, sessions.length, 5);
  }

  private computeDominantEmotions(
    sessions: { emotions: JsonValue }[],
  ): string[] {
    const counts = new Map<string, number>();
    for (const s of sessions) {
      const emotions = toStrArray(s.emotions);
      for (const e of emotions) {
        const key = e.toLowerCase().trim();
        counts.set(key, (counts.get(key) ?? 0) + 1);
      }
    }
    return this.topByFrequency(counts, sessions.length, 5);
  }

  private topByFrequency(counts: Map<string, number>, totalSessions: number, maxItems: number): string[] {
    if (counts.size === 0) return [];
    const minOccurrences = Math.max(1, Math.ceil(totalSessions * 0.25));
    const entries = Array.from(counts.entries())
      .filter(([, c]) => c >= minOccurrences)
      .sort((a, b) => b[1] - a[1])
      .slice(0, maxItems);
    return entries.map(([label]) => this.capitalize(label));
  }

  private capitalize(s: string): string {
    if (!s) return s;
    return s.charAt(0).toUpperCase() + s.slice(1).toLowerCase();
  }

  private computePatterns(
    sessions: { themes: JsonValue; emotions: JsonValue }[],
    dominantThemes: string[],
    dominantEmotions: string[],
  ): string[] {
    const result: string[] = [];
    if (sessions.length < 2) return result;

    const themeEmotionPairs = new Map<string, number>();
    for (const s of sessions) {
      const themes = toStrArray(s.themes);
      const emotions = toStrArray(s.emotions);
      for (const t of themes) {
        for (const e of emotions) {
          const key = `${t.toLowerCase()}|${e.toLowerCase()}`;
          themeEmotionPairs.set(key, (themeEmotionPairs.get(key) ?? 0) + 1);
        }
      }
    }

    const minPairs = Math.max(2, Math.ceil(sessions.length * 0.3));
    for (const [key, count] of themeEmotionPairs.entries()) {
      if (count >= minPairs) {
        const [theme, emotion] = key.split('|');
        result.push(`${this.capitalize(emotion)} recorrente ligada(o) ao contexto de ${this.capitalize(theme)}`);
      }
    }

    if (dominantEmotions.length > 0 && dominantThemes.length > 0 && result.length === 0) {
      result.push(
        `${dominantEmotions[0]} recorrente em discussões sobre ${dominantThemes[0].toLowerCase()}`,
      );
    }

    return result.slice(0, 5);
  }

  private computeAlerts(
    sessions: { date: Date; emotions: JsonValue; riskFlags: JsonValue }[],
  ): string[] {
    const result: string[] = [];
    if (sessions.length < 2) return result;

    const recentCount = Math.min(3, Math.ceil(sessions.length / 2));
    const recent = sessions.slice(-recentCount);
    const older = sessions.slice(0, -recentCount);
    if (older.length === 0) return result;

    const recentEmotionCounts = new Map<string, number>();
    const olderEmotionCounts = new Map<string, number>();
    for (const s of recent) {
      for (const e of toStrArray(s.emotions)) {
        const k = e.toLowerCase();
        recentEmotionCounts.set(k, (recentEmotionCounts.get(k) ?? 0) + 1);
      }
    }
    for (const s of older) {
      for (const e of toStrArray(s.emotions)) {
        const k = e.toLowerCase();
        olderEmotionCounts.set(k, (olderEmotionCounts.get(k) ?? 0) + 1);
      }
    }

    for (const [emotion, recentFreq] of recentEmotionCounts.entries()) {
      const olderCount = olderEmotionCounts.get(emotion) ?? 0;
      if (recentFreq > olderCount && recentFreq >= 2) {
        result.push(`Aumento de ${this.capitalize(emotion)} nas últimas ${recent.length} sessões`);
      }
    }

    const allRiskFlags = sessions.flatMap((s) => toStrArray(s.riskFlags));
    const recentRiskFlags = recent.flatMap((s) => toStrArray(s.riskFlags));
    if (recentRiskFlags.length > 0 && recentRiskFlags.length >= allRiskFlags.length * 0.5) {
      result.push('Concentração de sinais de alerta nas sessões recentes');
    }

    return result.slice(0, 5);
  }
}
