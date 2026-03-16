"""
Agent Config — Constantes e configuração do Claude Voice Agent.
Separado do ConfigManager principal para mínimo impacto no whisper_transcriber.py.
"""

import os
import sys

# ─── Diretório base ──────────────────────────────────────────

if getattr(sys, "frozen", False):
    SCRIPT_DIR = os.path.dirname(sys.executable)
else:
    SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))

MODELS_DIR = os.path.join(SCRIPT_DIR, "models")

# ─── Caminhos de modelos ─────────────────────────────────────

WHISPER_MODELS_DIR = os.path.join(MODELS_DIR, "whisper")
KOKORO_MODELS_DIR = os.path.join(MODELS_DIR, "kokoro")
VAD_MODELS_DIR = os.path.join(MODELS_DIR, "vad")

# ─── Whisper local (faster-whisper) ──────────────────────────

WHISPER_MODEL_SIZES = {
    "tiny":     {"size_mb": 74,   "wer_ptbr": "~25%", "speed": "~0.1s"},
    "base":     {"size_mb": 142,  "wer_ptbr": "~20%", "speed": "~0.2s"},
    "small":    {"size_mb": 466,  "wer_ptbr": "~18%", "speed": "~0.5s"},
    "medium":   {"size_mb": 1500, "wer_ptbr": "~16%", "speed": "~1.0s"},
    "large-v3": {"size_mb": 3100, "wer_ptbr": "~14%", "speed": "~2.0s"},
}

DEFAULT_WHISPER_MODEL = "small"

# ─── VAD (Silero) ────────────────────────────────────────────

VAD_THRESHOLD = 0.5         # confiança mínima para considerar como fala
MIN_SILENCE_MS = 500        # ms de silêncio antes de considerar fim de fala
MIN_SPEECH_MS = 200         # ignora segmentos < 200ms (ruídos)
VAD_SAMPLE_RATE = 16000     # Hz — Silero espera 16kHz
VAD_FRAME_MS = 32           # tamanho do frame em ms (Silero aceita 32ms)

# ─── TTS (Edge TTS — Microsoft, gratuito) ────────────────────

EDGE_VOICES = {
    "pt-BR-FranciscaNeural":  "Francisca (Feminina, BR)",
    "pt-BR-AntonioNeural":    "Antonio (Masculino, BR)",
    "pt-BR-ThalitaNeural":    "Thalita (Feminina jovem, BR)",
    "pt-BR-MacerioNeural":    "Macério (Masculino, BR)",
}

DEFAULT_VOICE = "pt-BR-FranciscaNeural"
TTS_SAMPLE_RATE = 24000  # Hz — output do Edge TTS

# ─── LLM (Claude) ────────────────────────────────────────────

CLAUDE_MODELS = {
    "claude-haiku-4-5":  {"label": "Haiku (rápido, ~R$0.20/h)",  "default": True},
    "claude-sonnet-4-6": {"label": "Sonnet (potente, ~R$0.75/h)", "default": False},
}

DEFAULT_CLAUDE_MODEL = "claude-haiku-4-5"
MAX_CONVERSATION_TURNS = 25   # janela deslizante de contexto

SYSTEM_PROMPT = """Você é o Voice AI Agent, um assistente de voz integrado ao desktop do usuário.

Regras:
- Responda de forma concisa e natural, como uma conversa falada (não texto escrito).
- Use frases curtas. Máximo 2-3 frases por resposta, a menos que o usuário peça detalhes.
- Quando executar ações (CRM, tasks, email), confirme em 1 frase: "Feito! [o que fez]."
- Nunca diga IDs numéricos. Use nomes legíveis.
- Idioma padrão: português brasileiro.
- Se não tiver certeza de uma ação, pergunte antes de executar.
- Não use markdown, emojis ou formatação de texto — sua resposta será lida em voz alta.
"""

# ─── Áudio ────────────────────────────────────────────────────

AUDIO_SAMPLE_RATE = 16000   # captura do mic
AUDIO_CHANNELS = 1
AUDIO_DTYPE = "float32"

# ─── Agente — defaults para ConfigManager ────────────────────

AGENT_CONFIG_DEFAULTS = {
    "stt_engine": "cloud",              # "cloud" | "local"
    "local_whisper_model": "small",     # tiny/base/small/medium/large-v3
    "anthropic_api_key": "",
    "agent_hotkey": "Ctrl+Shift+Alt+F4",
    "agent_activation_mode": "vad",     # "vad" | "ptt" | "push_enter"
    "agent_model": "claude-haiku-4-5",
    "agent_voice": "pt-BR-FranciscaNeural",
    "agent_show_transcripts": True,
    "agent_auto_open": False,
    "agent_silence_ms": 500,
    "agent_max_turns": 25,
}
