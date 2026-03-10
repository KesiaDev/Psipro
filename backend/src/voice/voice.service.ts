import { Injectable, BadRequestException, NotFoundException, ForbiddenException, OnModuleInit } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { whereNotDeleted } from '../prisma/soft-delete.helper';
import { spawn } from 'child_process';
import * as fs from 'fs';
import * as path from 'path';
import * as os from 'os';
import OpenAI from 'openai';

@Injectable()
export class VoiceService implements OnModuleInit {
  private openai: OpenAI | null = null;

  constructor(private readonly prisma: PrismaService) {
    const apiKey = process.env.OPENAI_API_KEY;
    if (apiKey) {
      this.openai = new OpenAI({ apiKey });
    }
  }

  onModuleInit() {
    if (this.openai) {
      console.log('[Voice] OpenAI configurado – transcrição e insights disponíveis');
    } else {
      console.warn('[Voice] OPENAI_API_KEY não configurada – transcrição indisponível');
    }
  }

  /**
   * Transcrição via Whisper.
   * Prioridade: 1) OpenAI API (se OPENAI_API_KEY), 2) Script Python local.
   */
  async transcribe(audioBuffer: Buffer, filename?: string): Promise<{ transcript: string }> {
    const ext = (filename || '').toLowerCase();
    const allowed = ['.wav', '.mp3', '.m4a', '.webm', '.ogg', '.flac', '.mp4'];
    const hasValidExt = allowed.some((e) => ext.endsWith(e));
    if (!hasValidExt && filename) {
      throw new BadRequestException(
        `Formato não suportado. Use: ${allowed.join(', ')}`,
      );
    }

    const tmpDir = os.tmpdir();
    const safeName = (filename || 'audio.wav').replace(/[^a-zA-Z0-9._-]/g, '_');
    const tmpPath = path.join(tmpDir, `psipro-${Date.now()}-${safeName}`);

    try {
      fs.writeFileSync(tmpPath, audioBuffer);

      // 1. Tentar OpenAI API
      if (this.openai) {
        try {
          const stream = fs.createReadStream(tmpPath);
          const transcription = await this.openai.audio.transcriptions.create({
            file: stream as any,
            model: 'whisper-1',
            language: 'pt',
          });
          return { transcript: (transcription as { text: string }).text?.trim() || '' };
        } catch (e: any) {
          console.warn('[Voice] OpenAI transcription failed:', e?.message);
          // Continua para fallback Python
        }
      }

      // 2. Fallback: script Python whisper_transcriber.py
      return this.transcribeViaPython(tmpPath);
    } finally {
      try {
        if (fs.existsSync(tmpPath)) fs.unlinkSync(tmpPath);
      } catch (_) {}
    }
  }

  private async transcribeViaPython(tmpPath: string): Promise<{ transcript: string }> {
    const scriptDir = path.join(process.cwd(), 'scripts');
    const scriptPath = path.join(scriptDir, 'whisper_transcriber.py');

    if (!fs.existsSync(scriptPath)) {
      throw new BadRequestException(
        'Transcrição indisponível. Configure OPENAI_API_KEY ou instale whisper_transcriber.py.',
      );
    }

    const pythonCmd = process.platform === 'win32' ? 'python' : 'python3';
    const result = await new Promise<string>((resolve, reject) => {
      const proc = spawn(pythonCmd, [scriptPath, tmpPath], {
        cwd: process.cwd(),
        env: { ...process.env, PYTHONIOENCODING: 'utf-8' },
      });
      let stdout = '';
      let stderr = '';
      proc.stdout.on('data', (d) => (stdout += d.toString()));
      proc.stderr.on('data', (d) => (stderr += d.toString()));
      proc.on('close', (code) => {
        if (code === 0) resolve(stdout);
        else reject(new Error(stderr || `Python exit ${code}`));
      });
      proc.on('error', (err) => reject(err));
    });

    const parsed = JSON.parse(result);
    if (parsed.error) {
      throw new BadRequestException(parsed.error);
    }
    return { transcript: parsed.transcript || '' };
  }

  /**
   * Resumo da sessão via IA (OpenAI).
   * Opcional - requer OPENAI_API_KEY.
   */
  async summarize(text: string): Promise<{ summary: string; keyPoints: string[] }> {
    if (!this.openai) {
      throw new BadRequestException(
        'Resumo indisponível. Configure OPENAI_API_KEY.',
      );
    }

    if (!text?.trim()) {
      return { summary: '', keyPoints: [] };
    }

    const prompt = `Resuma o seguinte texto de sessão em psicologia de forma clínica e objetiva.
Gere um resumo em 2-4 frases e liste 3 a 5 pontos-chave em bullet points.

Texto:
"""
${text.slice(0, 8000)}
"""

Responda em JSON válido com exatamente esta estrutura:
{"summary": "resumo aqui", "keyPoints": ["ponto 1", "ponto 2", ...]}`;

    const completion = await this.openai.chat.completions.create({
      model: 'gpt-4o-mini',
      messages: [{ role: 'user', content: prompt }],
      temperature: 0.3,
    });

    const content = completion.choices[0]?.message?.content?.trim() || '{}';
    try {
      const parsed = JSON.parse(content);
      return {
        summary: String(parsed.summary || '').trim(),
        keyPoints: Array.isArray(parsed.keyPoints)
          ? parsed.keyPoints.map(String)
          : [],
      };
    } catch {
      return {
        summary: content.slice(0, 500),
        keyPoints: [],
      };
    }
  }

  /**
   * Gera insights clínicos estruturados a partir da transcrição da sessão.
   * Requer OPENAI_API_KEY.
   */
  async generateInsights(text: string): Promise<{
    summary: string;
    themes: string[];
    emotions: string[];
    actionItems: string[];
    riskFlags: string[];
  }> {
    if (!this.openai) {
      throw new BadRequestException(
        'Insights indisponíveis. Configure OPENAI_API_KEY.',
      );
    }

    if (!text?.trim()) {
      return {
        summary: '',
        themes: [],
        emotions: [],
        actionItems: [],
        riskFlags: [],
      };
    }

    const prompt = `Você é um assistente clínico para psicólogos.

Analise a transcrição da sessão terapêutica abaixo e gere um JSON estruturado contendo:

* summary: resumo clínico em até 5 linhas
* themes: principais temas discutidos (array de strings)
* emotions: emoções predominantes percebidas no relato do paciente (array de strings)
* actionItems: possíveis tarefas ou encaminhamentos terapêuticos (array de strings)
* riskFlags: possíveis sinais de alerta emocional (array de strings, vazio se não existirem)

Regras:
* NÃO gerar diagnósticos médicos ou psiquiátricos
* NÃO substituir avaliação do terapeuta
* apenas análise descritiva e apoio clínico

Transcrição:
"""
${text.slice(0, 12000)}
"""

Retorne APENAS um JSON válido, sem markdown, sem explicações extras:
{"summary":"...","themes":[],"emotions":[],"actionItems":[],"riskFlags":[]}`;

    const completion = await this.openai.chat.completions.create({
      model: 'gpt-4o-mini',
      messages: [{ role: 'user', content: prompt }],
      temperature: 0.2,
    });

    const content = completion.choices[0]?.message?.content?.trim() || '{}';
    try {
      const parsed = JSON.parse(content);
      return {
        summary: String(parsed.summary || '').trim(),
        themes: Array.isArray(parsed.themes) ? parsed.themes.map(String) : [],
        emotions: Array.isArray(parsed.emotions) ? parsed.emotions.map(String) : [],
        actionItems: Array.isArray(parsed.actionItems) ? parsed.actionItems.map(String) : [],
        riskFlags: Array.isArray(parsed.riskFlags) ? parsed.riskFlags.map(String) : [],
      };
    } catch {
      return {
        summary: content.slice(0, 500),
        themes: [],
        emotions: [],
        actionItems: [],
        riskFlags: [],
      };
    }
  }

  /**
   * Gera insights, salva na sessão e retorna. Valida que a sessão pertence à clínica.
   */
  async generateAndSaveInsights(
    sessionId: string,
    text: string,
    clinicId: string,
  ): Promise<{
    summary: string;
    themes: string[];
    emotions: string[];
    actionItems: string[];
    riskFlags: string[];
  }> {
    const session = await this.prisma.session.findFirst({
      where: whereNotDeleted('session', { id: sessionId }),
      include: { patient: { select: { clinicId: true } } },
    });
    if (!session) {
      throw new NotFoundException('Sessão não encontrada');
    }
    const patientClinicId = session.patient?.clinicId;
    if (!patientClinicId || patientClinicId !== clinicId) {
      throw new ForbiddenException('Sessão não pertence à clínica');
    }

    const insights = await this.generateInsights(text);

    await this.prisma.session.update({
      where: { id: sessionId },
      data: {
        transcript: text.trim(),
        summary: insights.summary || null,
        themes: insights.themes.length ? insights.themes : null,
        emotions: insights.emotions.length ? insights.emotions : null,
        actionItems: insights.actionItems.length ? insights.actionItems : null,
        riskFlags: insights.riskFlags.length ? insights.riskFlags : null,
      },
    });

    return insights;
  }
}
