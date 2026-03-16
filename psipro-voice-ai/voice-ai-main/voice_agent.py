"""
Voice Agent — Assistente de voz bidirecional com Claude Code.
Fala → Claude processa (com MCPs) → Responde em voz natural.

Uso: python voice_agent.py
"""

import os
import sys
import io
import re
import json
import wave
import time
import asyncio
import logging
import threading
import subprocess
import ctypes
import ctypes.wintypes
import tkinter as tk
from tkinter import ttk

import random
import numpy as np
import sounddevice as sd

# ─── Diretório base ──────────────────────────────────────────

if getattr(sys, 'frozen', False):
    SCRIPT_DIR = os.path.dirname(sys.executable)
else:
    SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))

# ─── Logging ─────────────────────────────────────────────────

logging.basicConfig(
    filename=os.path.join(SCRIPT_DIR, "voice_agent.log"),
    level=logging.INFO,
    format="%(asctime)s  %(levelname)s  %(message)s",
    datefmt="%H:%M:%S",
)
# Também mostrar no console (skip se rodando via pythonw sem console)
if sys.stderr is not None:
    console_handler = logging.StreamHandler()
    console_handler.setLevel(logging.INFO)
    console_handler.setFormatter(logging.Formatter("%(asctime)s  %(message)s", "%H:%M:%S"))
    logging.getLogger().addHandler(console_handler)
log = logging.getLogger(__name__)

# ─── Config persistente ─────────────────────────────────────

CONFIG_PATH = os.path.join(SCRIPT_DIR, "voice_agent_config.json")

DEFAULT_CONFIG = {
    "openai_api_key": "",
    "groq_api_key": "",
    "tts_engine": "openai",       # "openai" | "edge"
    "tts_voice": "coral",         # voz do motor selecionado
    "tts_speed": 1.0,             # 0.5-2.0
    "tts_instructions": "",       # estilo de voz (OpenAI gpt-4o-mini-tts)
    "stt_engine": "groq",         # "groq" | "openai"
    "claude_model": "sonnet",
    "hotkey": "Ctrl+Shift+Alt+F4",
    "mic_device": None,
    "max_history": 20,
}


def load_config() -> dict:
    cfg = dict(DEFAULT_CONFIG)
    if os.path.exists(CONFIG_PATH):
        try:
            with open(CONFIG_PATH, "r", encoding="utf-8") as f:
                saved = json.load(f)
            for k in DEFAULT_CONFIG:
                if k in saved:
                    cfg[k] = saved[k]
        except Exception as e:
            log.error(f"Config load error: {e}")
    return cfg


def save_config(cfg: dict):
    try:
        with open(CONFIG_PATH, "w", encoding="utf-8") as f:
            json.dump(cfg, f, indent=2, ensure_ascii=False)
    except Exception as e:
        log.error(f"Config save error: {e}")


# ─── Áudio ───────────────────────────────────────────────────

SAMPLE_RATE = 16000
CHANNELS = 1

OPENAI_TTS_VOICES = {
    "coral": "Coral (Feminina, natural)",
    "sage": "Sage (Feminina, calma)",
    "alloy": "Alloy (Neutra)",
    "ash": "Ash (Masculina, suave)",
    "ballad": "Ballad (Masculina, expressiva)",
    "echo": "Echo (Masculina)",
    "fable": "Fable (Feminina, storytelling)",
    "nova": "Nova (Feminina, jovem)",
    "onyx": "Onyx (Masculina, grave)",
    "shimmer": "Shimmer (Feminina, clara)",
    "vale": "Vale (Feminina, dinâmica)",
}

EDGE_TTS_VOICES = {
    "pt-BR-FranciscaNeural": "Francisca (Feminina, BR)",
    "pt-BR-AntonioNeural": "Antonio (Masculino, BR)",
    "pt-BR-ThalitaNeural": "Thalita (Feminina jovem, BR)",
    "pt-BR-ThalitaMultilingualNeural": "Thalita Multilingual (Feminina, BR)",
    "pt-PT-RaquelNeural": "Raquel (Feminina, PT)",
    "pt-PT-DuarteNeural": "Duarte (Masculino, PT)",
}


def get_input_devices() -> list:
    """Retorna lista de dispositivos de entrada de áudio."""
    devices = []
    seen = set()
    for i, d in enumerate(sd.query_devices()):
        if d["max_input_channels"] > 0:
            name = d["name"]
            # Filtrar duplicatas e drivers genéricos
            if name in seen or "Mapeador" in name or "prim" in name.lower():
                continue
            seen.add(name)
            devices.append({"index": i, "name": name})
    return devices


# ─── VAD (Silero VAD via ONNX) ────────────────────────────────

class SileroVADWrapper:
    """VAD baseado em Silero (deep learning, ONNX puro) — detecção precisa de fala.
    Usa onnxruntime diretamente (sem PyTorch).
    Requer frames de exatamente 512 samples @ 16kHz (32ms).
    """

    def __init__(self, threshold=0.5, min_silence_sec=0.5, min_speech_sec=0.25):
        from agent_vad import _find_onnx_model, _OnnxVADModel
        model_path = _find_onnx_model()
        self._model = _OnnxVADModel(model_path)
        self.threshold = threshold
        self.min_silence_sec = min_silence_sec
        self.min_speech_sec = min_speech_sec

        self._is_speaking = False
        self._speech_buffer = []
        self._silence_frames = 0
        self._speech_frames = 0
        self._frames_per_sec = SAMPLE_RATE / 512  # blocksize=512

    def process_frame(self, frame: np.ndarray) -> tuple:
        """Processa um frame de áudio (512 samples float32).

        Returns:
            (event, audio_or_none)
            event: None, "speech_start", "speech_end"
            audio_or_none: np.ndarray concatenado quando event=="speech_end"
        """
        prob = self._model(frame.astype(np.float32), SAMPLE_RATE)
        is_speech = prob >= self.threshold

        if not self._is_speaking:
            if is_speech:
                self._speech_frames += 1
                self._speech_buffer.append(frame.copy())
                if self._speech_frames >= self.min_speech_sec * self._frames_per_sec:
                    self._is_speaking = True
                    self._silence_frames = 0
                    return ("speech_start", None)
            else:
                # Tolerância: manter buffer se estava quase detectando
                if self._speech_frames > 0:
                    self._speech_buffer.append(frame.copy())
                    self._silence_frames += 1
                    if self._silence_frames > 3:  # ~100ms de tolerância
                        self._speech_frames = 0
                        self._silence_frames = 0
                        self._speech_buffer = []
                else:
                    self._speech_frames = 0
                    self._speech_buffer = []
            return (None, None)
        else:
            self._speech_buffer.append(frame.copy())
            if not is_speech:
                self._silence_frames += 1
                if self._silence_frames >= self.min_silence_sec * self._frames_per_sec:
                    audio = np.concatenate(self._speech_buffer)
                    self._speech_buffer = []
                    self._is_speaking = False
                    self._speech_frames = 0
                    self._silence_frames = 0
                    return ("speech_end", audio)
            else:
                self._silence_frames = 0
            return (None, None)

    def reset(self):
        self._is_speaking = False
        self._speech_buffer = []
        self._silence_frames = 0
        self._speech_frames = 0


# ─── Claude CLI ──────────────────────────────────────────────

CLAUDE_CMD = os.path.join(os.environ.get("APPDATA", ""), "npm", "claude.cmd")
if not os.path.exists(CLAUDE_CMD):
    CLAUDE_CMD = "claude.cmd"

AGENT_SYSTEM_PROMPT = """Você é o Voice Agent, um assistente pessoal de voz em português brasileiro.
Sua resposta será convertida em áudio por um sintetizador de voz (TTS), então siga TODAS estas regras rigorosamente:

FORMATO DE RESPOSTA (CRÍTICO):
- Responda SOMENTE com texto puro. ZERO formatação.
- PROIBIDO: markdown, asteriscos, backticks, hashtags, listas com bullets, colchetes, parênteses com URLs, emojis, caracteres especiais decorativos.
- PROIBIDO: "**texto**", "*texto*", "`código`", "# título", "- item", "[link](url)", qualquer emoji unicode.
- PROIBIDO: abreviações técnicas (ex: "API", "SDK", "CLI"). Fale por extenso ou simplifique.
- Use apenas letras, números, pontuação básica (ponto, vírgula, exclamação, interrogação, dois pontos) e acentos do português.
- Separe ideias com pontos finais. Não use travessões ou hífens como separadores.

ESTILO DE FALA:
- Fale como uma pessoa real numa conversa ao telefone. Natural, fluente, direto.
- Frases curtas e claras. Máximo 3 frases por resposta, a menos que peçam detalhes.
- Use contrações naturais do português falado: "tô", "tá", "pra", "pro", "né", "num", "dum".
- Evite linguagem robótica ou formal demais. Não diga "Certamente", "Com certeza", "Entendido".
- Diga "Feito!" ou "Pronto!" quando completar uma ação, seguido de uma frase curta do que fez.
- Se precisar listar coisas, fale em sequência natural: "Primeiro tal coisa, depois tal outra, e por último isso."
- Nunca comece resposta com "Claro!", "Certo!", "Entendi!" toda vez. Varie.

CONTEXTO:
- Você tem acesso a ferramentas de CRM, tarefas, email e outras integrações.
- Ao executar ações, confirme de forma breve e natural.
- Use nomes legíveis, nunca IDs numéricos.
- Se não souber algo, diga naturalmente que não sabe.
- Não mencione que é uma inteligência artificial ou que está processando algo.
- Valores monetários: diga "novecentos e noventa e sete reais" ao invés de "R$ 997".
- Datas: diga "vinte e oito de fevereiro" ao invés de "28/02".
- Responda SEMPRE em português brasileiro."""


def clean_for_tts(text: str) -> str:
    """Remove emojis, markdown, URLs e artefatos para TTS limpo e natural."""
    if not text:
        return ""

    # 1. Emojis (todos os ranges unicode conhecidos)
    text = re.sub(r'[\U00010000-\U0010ffff]', '', text, flags=re.UNICODE)
    text = re.sub(r'[\u2600-\u27BF\u2B50-\u2BFF\u231A-\u23FF\u2702-\u27B0]', '', text)
    text = re.sub(r'[\u200d\ufe0f\ufe0e\u00AE\u00A9\u2122\u203C\u2049\u20E3]', '', text)
    text = re.sub(r'[\u2139\u2194-\u21AA\u25AA-\u25FE\u2934-\u2935\u3030\u303D\u3297\u3299]', '', text)
    text = re.sub(r'[\U0001F000-\U0001FFFF]', '', text)  # catch-all suplementar

    # 2. Markdown — headers, bold, italic, code, strikethrough
    text = re.sub(r'^#{1,6}\s*(.+)$', r'\1.', text, flags=re.MULTILINE)  # headers → texto com ponto
    text = re.sub(r'\*\*\*(.+?)\*\*\*', r'\1', text)
    text = re.sub(r'\*\*(.+?)\*\*', r'\1', text)
    text = re.sub(r'\*(.+?)\*', r'\1', text)
    text = re.sub(r'__(.+?)__', r'\1', text)
    text = re.sub(r'_(.+?)_', r'\1', text)
    text = re.sub(r'~~(.+?)~~', r'\1', text)
    text = re.sub(r'```[\s\S]*?```', '', text)  # code blocks
    text = re.sub(r'`(.+?)`', r'\1', text)  # inline code

    # 3. Links markdown [texto](url)
    text = re.sub(r'\[(.+?)\]\(.+?\)', r'\1', text)

    # 4. URLs soltas
    text = re.sub(r'https?://\S+', '', text)

    # 5. Bullets e listas numeradas
    text = re.sub(r'^\s*[-*•]\s+', '', text, flags=re.MULTILINE)
    text = re.sub(r'^\s*\d+\.\s+', '', text, flags=re.MULTILINE)

    # 6. Tabelas markdown (pipes)
    text = re.sub(r'\|', ',', text)  # pipes → vírgulas
    text = re.sub(r'^[\s,:-]+$', '', text, flags=re.MULTILINE)  # linhas separadoras

    # 7. Caracteres decorativos e blocos
    text = re.sub(r'[─━═│┃┌┐└┘├┤┬┴┼╔╗╚╝╠╣╦╩╬]', '', text)
    text = re.sub(r'>{1,}\s*', '', text)  # blockquotes

    # 8. Referências tipo "Assistente:" no início
    text = re.sub(r'^(Assistente|Assistant|Claude|Voice Agent)\s*:\s*', '', text, flags=re.IGNORECASE)

    # 9. Expandir abreviações comuns para fala natural
    replacements = {
        'R$': 'reais ',
        'US$': 'dólares ',
        '€': 'euros ',
        '%': ' por cento',
        'nº': 'número',
        'Nº': 'Número',
        'etc.': 'etcétera.',
        'ex:': 'por exemplo,',
        'Ex:': 'Por exemplo,',
        'obs:': 'observação,',
        'Obs:': 'Observação,',
    }
    for old, new in replacements.items():
        text = text.replace(old, new)

    # 10. Limpar newlines e espaços
    text = re.sub(r'\n{2,}', '. ', text)
    text = re.sub(r'\n', ' ', text)
    text = re.sub(r'\s{2,}', ' ', text)
    text = re.sub(r'\.\s*\.', '.', text)  # pontos duplos

    return text.strip()


class ClaudeSession:
    """Gerencia sessão do Claude Code CLI com --resume."""

    def __init__(self, model="sonnet"):
        self.model = model
        self.session_id = None
        self._env = os.environ.copy()
        self._env.pop("CLAUDECODE", None)

    def ask(self, message: str) -> str:
        """Envia mensagem e retorna resposta. Mantém sessão."""
        try:
            cmd = [CLAUDE_CMD, "-p", "--output-format", "json",
                   "--max-turns", "10", "--model", self.model,
                   "--append-system-prompt", AGENT_SYSTEM_PROMPT]

            if self.session_id:
                cmd.extend(["--resume", self.session_id])

            # Usar Popen para poder matar o processo no timeout
            proc = subprocess.Popen(
                cmd,
                stdin=subprocess.PIPE,
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE,
                env=self._env,
                cwd=SCRIPT_DIR,
                shell=True,
            )
            try:
                stdout_bytes, stderr_bytes = proc.communicate(
                    input=message.encode("utf-8"), timeout=90
                )
            except subprocess.TimeoutExpired:
                proc.kill()
                proc.communicate()
                raise

            if proc.returncode != 0:
                stderr = stderr_bytes.decode("utf-8", errors="replace")[:300]
                log.error(f"Claude CLI error: {stderr}")
                return "Desculpe, tive um problema ao processar. Pode repetir?"

            stdout = stdout_bytes.decode("utf-8", errors="replace")
            data = json.loads(stdout)

            # Guardar session_id para resume
            new_sid = data.get("session_id")
            if new_sid:
                self.session_id = new_sid

            response = data.get("result", "").strip()

            # Limpar
            if response.startswith("Assistente:"):
                response = response[len("Assistente:"):].strip()
            response = clean_for_tts(response)

            log.info(f"Claude [{self.model}]: {response[:120]}")
            return response

        except subprocess.TimeoutExpired:
            log.error("Claude CLI timeout (90s)")
            return "Desculpe, demorei demais. Pode tentar de novo?"
        except json.JSONDecodeError as e:
            log.error(f"Claude JSON parse error: {e}")
            return "Desculpe, recebi uma resposta inesperada. Pode repetir?"
        except Exception as e:
            log.error(f"Claude error: {e}")
            return "Desculpe, algo deu errado. Pode repetir?"

    def reset(self):
        """Reseta sessão (nova conversa)."""
        self.session_id = None
        log.info("Claude session reset")


# ─── STT ─────────────────────────────────────────────────────

def _audio_to_wav_buffer(audio_data: np.ndarray) -> io.BytesIO:
    """Converte ndarray float32 para buffer WAV."""
    audio_int16 = (audio_data * 32767).astype(np.int16)
    wav_buffer = io.BytesIO()
    with wave.open(wav_buffer, "wb") as wf:
        wf.setnchannels(CHANNELS)
        wf.setsampwidth(2)
        wf.setframerate(SAMPLE_RATE)
        wf.writeframes(audio_int16.tobytes())
    wav_buffer.seek(0)
    wav_buffer.name = "audio.wav"
    return wav_buffer


def transcribe_audio(audio_data: np.ndarray, cfg: dict) -> str:
    """Transcreve áudio usando Groq ou OpenAI Whisper."""
    stt_engine = cfg.get("stt_engine", "groq")
    wav_buffer = _audio_to_wav_buffer(audio_data)

    try:
        if stt_engine == "groq":
            groq_key = cfg.get("groq_api_key", "")
            if not groq_key:
                log.warning("Groq API key vazia, fallback para OpenAI")
                return _transcribe_openai(wav_buffer, cfg.get("openai_api_key", ""))
            return _transcribe_groq(wav_buffer, groq_key)
        else:
            return _transcribe_openai(wav_buffer, cfg.get("openai_api_key", ""))
    except Exception as e:
        log.error(f"STT error: {e}")
        return ""


def _transcribe_groq(wav_buffer: io.BytesIO, api_key: str) -> str:
    """Transcreve via Groq Whisper (whisper-large-v3-turbo)."""
    from groq import Groq
    client = Groq(api_key=api_key)
    result = client.audio.transcriptions.create(
        file=wav_buffer,
        model="whisper-large-v3-turbo",
        language="pt",
        temperature=0.0,
    )
    text = result.text.strip()
    log.info(f"STT [Groq]: {text}")
    return text


def _transcribe_openai(wav_buffer: io.BytesIO, api_key: str) -> str:
    """Transcreve via OpenAI Whisper."""
    from openai import OpenAI
    client = OpenAI(api_key=api_key)
    result = client.audio.transcriptions.create(
        model="whisper-1",
        file=wav_buffer,
        language="pt",
    )
    text = result.text.strip()
    log.info(f"STT [OpenAI]: {text}")
    return text


# ─── TTS ─────────────────────────────────────────────────────

def speak_text(text: str, cfg: dict, stop_event: threading.Event):
    """Sintetiza e reproduz texto em voz. Despacha para o motor configurado."""
    engine = cfg.get("tts_engine", "openai")
    voice = cfg.get("tts_voice", "coral")
    try:
        if engine == "openai":
            _speak_openai(text, voice, cfg, stop_event)
        else:
            _speak_edge(text, voice, stop_event)
    except Exception as e:
        log.error(f"TTS error: {e}")


def _speak_openai(text: str, voice: str, cfg: dict, stop_event: threading.Event):
    """TTS via OpenAI gpt-4o-mini-tts com streaming PCM 24kHz."""
    from openai import OpenAI
    client = OpenAI(api_key=cfg.get("openai_api_key", ""))

    speed = cfg.get("tts_speed", 1.0)
    instructions = cfg.get("tts_instructions", "") or "Fale em português brasileiro de forma natural."

    kwargs = {
        "model": "gpt-4o-mini-tts",
        "voice": voice,
        "input": text,
        "response_format": "pcm",
        "speed": max(0.25, min(4.0, speed)),
        "instructions": instructions,
    }

    # Streaming: tocar áudio enquanto recebe chunks
    with client.audio.speech.with_streaming_response.create(**kwargs) as response:
        # PCM 24kHz 16-bit mono — acumular e tocar
        pcm_data = bytearray()
        for chunk in response.iter_bytes(chunk_size=4096):
            if stop_event.is_set():
                return
            pcm_data.extend(chunk)

    if stop_event.is_set() or not pcm_data:
        return

    # Converter PCM 16-bit → float32
    audio = np.frombuffer(bytes(pcm_data), dtype=np.int16).astype(np.float32) / 32768.0

    sd.play(audio, samplerate=24000, blocking=False)
    duration = len(audio) / 24000
    start = time.time()
    while time.time() - start < duration + 0.1:
        if stop_event.is_set():
            sd.stop()
            return
        time.sleep(0.04)
    sd.stop()


def _speak_edge(text: str, voice: str, stop_event: threading.Event):
    """TTS via Edge TTS (fallback gratuito)."""
    try:
        import edge_tts
        import av
    except ImportError:
        log.error("edge-tts ou av não instalados. Use pip install edge-tts av")
        return

    loop = asyncio.new_event_loop()

    async def _synth():
        c = edge_tts.Communicate(text, voice)
        audio_data = b""
        async for chunk in c.stream():
            if stop_event.is_set():
                return b""
            if chunk["type"] == "audio":
                audio_data += chunk["data"]
        return audio_data

    mp3_data = loop.run_until_complete(_synth())
    loop.close()

    if not mp3_data or stop_event.is_set():
        return

    container = av.open(io.BytesIO(mp3_data))
    frames = []
    for frame in container.decode(audio=0):
        arr = frame.to_ndarray()
        if arr.ndim > 1:
            arr = arr.mean(axis=0)
        frames.append(arr.astype(np.float32))
    container.close()

    if not frames:
        return

    audio = np.concatenate(frames)
    mx = np.abs(audio).max()
    if mx > 1.0:
        audio = audio / mx

    sd.play(audio, samplerate=24000, blocking=False)
    duration = len(audio) / 24000
    start = time.time()
    while time.time() - start < duration + 0.1:
        if stop_event.is_set():
            sd.stop()
            return
        time.sleep(0.04)
    sd.stop()


# ─── Hotkey global (Windows) ────────────────────────────────

MOD_ALT = 0x0001
MOD_CONTROL = 0x0002
MOD_SHIFT = 0x0004

VK_MAP = {
    "f1": 0x70, "f2": 0x71, "f3": 0x72, "f4": 0x73,
    "f5": 0x74, "f6": 0x75, "f7": 0x76, "f8": 0x77,
    "f9": 0x78, "f10": 0x79, "f11": 0x7A, "f12": 0x7B,
}

MOD_MAP = {"ctrl": MOD_CONTROL, "shift": MOD_SHIFT, "alt": MOD_ALT}
HOTKEY_ID = 99


def parse_hotkey(hotkey_str: str):
    """Parseia 'Ctrl+Shift+Alt+F4' para (modifiers, vk)."""
    parts = [p.strip().lower() for p in hotkey_str.split("+")]
    mods = 0
    vk = 0
    for p in parts:
        if p in MOD_MAP:
            mods |= MOD_MAP[p]
        elif p in VK_MAP:
            vk = VK_MAP[p]
        elif len(p) == 1 and p.isalpha():
            vk = ord(p.upper())
    return mods, vk


class GlobalHotkey:
    """Registra hotkey global no Windows via RegisterHotKey."""

    def __init__(self, hotkey_str: str, callback):
        self.hotkey_str = hotkey_str
        self.callback = callback
        self._thread = None
        self._running = False

    def start(self):
        mods, vk = parse_hotkey(self.hotkey_str)
        if not vk:
            log.error(f"Invalid hotkey: {self.hotkey_str}")
            return

        self._running = True
        self._thread = threading.Thread(target=self._run, args=(mods, vk), daemon=True)
        self._thread.start()
        log.info(f"Hotkey registered: {self.hotkey_str}")

    def _run(self, mods, vk):
        user32 = ctypes.windll.user32

        if not user32.RegisterHotKey(None, HOTKEY_ID, mods, vk):
            log.error(f"Failed to register hotkey {self.hotkey_str}")
            return

        msg = ctypes.wintypes.MSG()
        while self._running:
            if user32.PeekMessageW(ctypes.byref(msg), None, 0, 0, 1):
                if msg.message == 0x0312:  # WM_HOTKEY
                    if msg.wParam == HOTKEY_ID:
                        try:
                            self.callback()
                        except Exception as e:
                            log.error(f"Hotkey callback error: {e}")
            else:
                time.sleep(0.05)

        user32.UnregisterHotKey(None, HOTKEY_ID)

    def stop(self):
        self._running = False
        if self._thread and self._thread.is_alive():
            self._thread.join(timeout=1.0)
        self._thread = None


# ─── UI ──────────────────────────────────────────────────────

class VoiceAgentApp:
    """Interface gráfica completa do Voice Agent."""

    BG = "#141820"
    BG_CARD = "#191E26"
    BG_INPUT = "#1C2D4A"
    BG_BUBBLE_AGENT = "#1A1F2A"
    ACCENT = "#21A353"
    TEXT = "#F1F3F5"
    TEXT_DIM = "#8D9DB5"
    RED = "#ff4444"
    BLUE = "#6c8dfa"
    PURPLE = "#a78bfa"
    YELLOW = "#ffaa00"
    PLAY_COLOR = "#4CAF50"

    def __init__(self):
        self.cfg = load_config()
        self.root = tk.Tk()
        self.root.title("Voice Agent")
        self.root.geometry("640x740")
        self.root.configure(bg=self.BG)
        self.root.minsize(480, 420)

        # Ícone
        try:
            icon = os.path.join(SCRIPT_DIR, "assets", "icon.ico")
            if os.path.exists(icon):
                self.root.iconbitmap(icon)
        except Exception:
            pass

        # Estado
        self.claude = ClaudeSession(model=self.cfg["claude_model"])
        self.recording = False
        self.audio_buffer = []
        self.audio_stream = None
        self.tts_stop = threading.Event()
        self.speaking = False
        self.processing = False
        self.cancelled = False
        self._audio_level = 0.0
        self._message_texts = []  # guarda textos das mensagens do agent p/ replay

        # VAD Silero (deep learning, não precisa de ajuste manual)
        self.vad = SileroVADWrapper(threshold=0.5, min_silence_sec=0.5, min_speech_sec=0.25)
        self._continuous_stream = None
        self._continuous_running = False

        # Hotkey
        self.hotkey = None

        # Estado do modo ao vivo (ANTES de _build_ui)
        self._live_mode = False
        self._live_muted = False

        # Verificar API keys — precisa de pelo menos uma
        if not self.cfg["openai_api_key"] and not self.cfg.get("groq_api_key", ""):
            self._ask_api_key()

        self._build_ui()
        self._add_welcome()
        self._bind_keys()
        self._start_hotkey()

        # Update loop para nível de áudio
        self._update_level_loop()

    # ─── API key dialog ──────────────────────────────────

    def _ask_api_key(self):
        """Pede API key na primeira execução."""
        dialog = tk.Toplevel(self.root)
        dialog.title("Configuração inicial")
        dialog.geometry("480x200")
        dialog.configure(bg=self.BG)
        dialog.transient(self.root)
        dialog.grab_set()
        dialog.resizable(False, False)

        tk.Label(
            dialog, text="Insira sua API key da OpenAI",
            font=("Segoe UI Semibold", 13), fg=self.TEXT, bg=self.BG,
        ).pack(pady=(20, 4))
        tk.Label(
            dialog, text="Usada para transcrição de voz (Whisper API)",
            font=("Segoe UI", 9), fg=self.TEXT_DIM, bg=self.BG,
        ).pack(pady=(0, 12))

        entry = tk.Entry(
            dialog, font=("Segoe UI", 11), fg=self.TEXT, bg=self.BG_INPUT,
            insertbackground=self.TEXT, bd=0, width=50, show="*",
        )
        entry.pack(padx=20, ipady=6)
        entry.focus_set()

        def _save():
            key = entry.get().strip()
            if key:
                self.cfg["openai_api_key"] = key
                save_config(self.cfg)
                dialog.destroy()

        tk.Button(
            dialog, text="Salvar", font=("Segoe UI Semibold", 10),
            fg="#fff", bg=self.ACCENT, bd=0, padx=20, pady=6,
            cursor="hand2", command=_save,
        ).pack(pady=16)

        entry.bind("<Return>", lambda e: _save())
        self.root.wait_window(dialog)

    # ─── Build UI ────────────────────────────────────────

    def _build_ui(self):
        # Header
        header = tk.Frame(self.root, bg=self.BG_CARD, height=52)
        header.pack(fill="x")
        header.pack_propagate(False)

        tk.Label(
            header, text="Voice Agent",
            font=("Segoe UI Semibold", 15), fg=self.TEXT, bg=self.BG_CARD,
        ).pack(side="left", padx=16, pady=10)

        tk.Label(
            header, text="Claude",
            font=("Segoe UI", 10), fg=self.ACCENT, bg=self.BG_CARD,
        ).pack(side="left", pady=10)

        # Info button (i)
        self._info_btn = tk.Button(
            header, text=" i ", font=("Segoe UI", 10, "bold"),
            fg=self.BLUE, bg=self.BG_CARD, bd=0,
            cursor="hand2", command=self._show_info,
        )
        self._info_btn.pack(side="right", padx=(0, 2), pady=10)

        # Settings button
        self._settings_btn = tk.Button(
            header, text="  \u2699  ", font=("Segoe UI", 12),
            fg=self.TEXT_DIM, bg=self.BG_CARD, bd=0,
            cursor="hand2", command=self._open_settings,
        )
        self._settings_btn.pack(side="right", padx=(0, 2), pady=10)

        btn_clear = tk.Button(
            header, text="Nova conversa", font=("Segoe UI", 9),
            fg=self.TEXT_DIM, bg=self.BG_INPUT, bd=0, padx=10, pady=3,
            cursor="hand2", command=self._new_conversation,
        )
        btn_clear.pack(side="right", padx=(0, 8), pady=14)

        # Chat area (scrollable)
        chat_outer = tk.Frame(self.root, bg=self.BG)
        chat_outer.pack(fill="both", expand=True)

        self.canvas = tk.Canvas(chat_outer, bg=self.BG, highlightthickness=0)
        scrollbar = ttk.Scrollbar(chat_outer, orient="vertical", command=self.canvas.yview)
        self.chat_frame = tk.Frame(self.canvas, bg=self.BG)

        self.chat_frame.bind(
            "<Configure>",
            lambda e: self.canvas.configure(scrollregion=self.canvas.bbox("all")),
        )
        self.canvas_window = self.canvas.create_window((0, 0), window=self.chat_frame, anchor="nw")
        self.canvas.configure(yscrollcommand=scrollbar.set)
        self.canvas.bind("<Configure>", lambda e: self.canvas.itemconfig(self.canvas_window, width=e.width))
        self.canvas.bind("<MouseWheel>", lambda e: self.canvas.yview_scroll(int(-1*(e.delta/120)), "units"))
        self.chat_frame.bind("<MouseWheel>", lambda e: self.canvas.yview_scroll(int(-1*(e.delta/120)), "units"))

        scrollbar.pack(side="right", fill="y")
        self.canvas.pack(side="left", fill="both", expand=True)

        # ─── Bottom bar ──────────────────────────────────
        self._bottom = tk.Frame(self.root, bg=self.BG_CARD)
        self._bottom.pack(fill="x", side="bottom")

        # Status row
        status_row = tk.Frame(self._bottom, bg=self.BG_CARD)
        status_row.pack(fill="x", padx=16, pady=(8, 0))

        self.status_dot = tk.Canvas(status_row, width=12, height=12, bg=self.BG_CARD, highlightthickness=0)
        self.status_dot.pack(side="left", padx=(0, 6))
        self.dot_id = self.status_dot.create_oval(2, 2, 10, 10, fill=self.TEXT_DIM, outline="")

        self.status_label = tk.Label(
            status_row, text="Pronto", font=("Segoe UI", 9),
            fg=self.TEXT_DIM, bg=self.BG_CARD,
        )
        self.status_label.pack(side="left")

        # Audio level bar
        self.level_canvas = tk.Canvas(status_row, width=120, height=6, bg="#0d1017", highlightthickness=0)
        self.level_canvas.pack(side="right", padx=(0, 0), pady=3)
        self.level_bar = self.level_canvas.create_rectangle(0, 0, 0, 6, fill=self.ACCENT, outline="")

        # Hotkey hint
        tk.Label(
            status_row, text=f"  {self.cfg['hotkey']}",
            font=("Segoe UI", 8), fg="#4a5568", bg=self.BG_CARD,
        ).pack(side="right", padx=(8, 4))

        # ─── Modo Texto (input row) ──────────────────────
        self._text_input_frame = tk.Frame(self._bottom, bg=self.BG_CARD)
        self._text_input_frame.pack(fill="x", padx=12, pady=8)

        # Mic button (grava → transcreve para o campo)
        self.mic_btn = tk.Button(
            self._text_input_frame, text="  MIC  ", font=("Segoe UI Semibold", 10),
            fg="#fff", bg=self.ACCENT, activebackground="#1A8744",
            bd=0, padx=14, pady=8, cursor="hand2",
            command=self._toggle_mic,
        )
        self.mic_btn.pack(side="left", padx=(0, 8))

        # Text input
        self.text_input = tk.Entry(
            self._text_input_frame, font=("Segoe UI", 11),
            fg=self.TEXT, bg=self.BG_INPUT,
            insertbackground=self.TEXT, bd=0, relief="flat",
        )
        self.text_input.pack(side="left", fill="x", expand=True, ipady=8)
        self.text_input.bind("<Return>", self._on_text_submit)

        # Botão Ao Vivo (waveform icon)
        self._live_btn = tk.Button(
            self._text_input_frame, text=" AO VIVO ", font=("Segoe UI Semibold", 9),
            fg="#fff", bg="#7c3aed", activebackground="#6d28d9",
            bd=0, padx=10, pady=8, cursor="hand2",
            command=self._enter_live_mode,
        )
        self._live_btn.pack(side="right", padx=(8, 0))

        # Send button
        self.send_btn = tk.Button(
            self._text_input_frame, text=" Enviar ", font=("Segoe UI", 10),
            fg="#fff", bg=self.BLUE, activebackground="#5070c0",
            bd=0, padx=10, pady=8, cursor="hand2",
            command=lambda: self._on_text_submit(None),
        )
        self.send_btn.pack(side="right", padx=(8, 0))

        # ─── Modo Ao Vivo (barra alternativa, oculta inicialmente) ──
        self._live_bar = tk.Frame(self._bottom, bg="#1a1040")
        # NÃO pack ainda — só aparece quando ativar

        self._live_label = tk.Label(
            self._live_bar, text="Ao Vivo", font=("Segoe UI Semibold", 12),
            fg="#c084fc", bg="#1a1040",
        )
        self._live_label.pack(side="left", padx=(16, 8), pady=12)

        # Waveform placeholder (canvas animado)
        self._live_waveform = tk.Canvas(
            self._live_bar, width=120, height=24, bg="#1a1040", highlightthickness=0,
        )
        self._live_waveform.pack(side="left", padx=4, pady=12)
        self._waveform_bars = []
        for i in range(12):
            bar = self._live_waveform.create_rectangle(
                i * 10 + 2, 12, i * 10 + 8, 12, fill="#7c3aed", outline="",
            )
            self._waveform_bars.append(bar)

        # Botão MUTE
        self._mute_btn = tk.Button(
            self._live_bar, text="  MUTE  ", font=("Segoe UI Semibold", 10),
            fg="#fff", bg="#4a5568", activebackground="#374151",
            bd=0, padx=12, pady=8, cursor="hand2",
            command=self._toggle_mute,
        )
        self._mute_btn.pack(side="left", padx=(16, 0), pady=12)

        # Botão ENCERRAR
        self._end_live_btn = tk.Button(
            self._live_bar, text=" ENCERRAR ", font=("Segoe UI Semibold", 10),
            fg="#fff", bg=self.RED, activebackground="#cc3333",
            bd=0, padx=12, pady=8, cursor="hand2",
            command=self._exit_live_mode,
        )
        self._end_live_btn.pack(side="right", padx=(0, 16), pady=12)

    # ─── Keyboard bindings ───────────────────────────────

    def _bind_keys(self):
        self.root.bind("<Escape>", self._on_escape)
        self.root.bind("<Control-n>", lambda e: self._new_conversation())

    def _on_escape(self, event=None):
        """ESC: cancela gravação, processamento, TTS, ou sai do modo ao vivo."""
        if self.recording:
            self.cancelled = True
            self._stop_recording_to_field(discard=True)
            self._set_status("Cancelado", self.TEXT_DIM)
        elif self.speaking:
            self.tts_stop.set()
            sd.stop()
            self.speaking = False
            if self._live_mode:
                self._set_status("Ao Vivo \u2014 escutando", self.ACCENT)
            else:
                self._set_status("Interrompido", self.TEXT_DIM)
        elif self.processing:
            self.cancelled = True
            self._set_status("Cancelando...", self.YELLOW)
        elif self._live_mode:
            self._exit_live_mode()

    # ─── Hotkey ──────────────────────────────────────────

    def _start_hotkey(self):
        hk_str = self.cfg.get("hotkey", "Ctrl+Shift+Alt+F4")
        self.hotkey = GlobalHotkey(hk_str, self._on_hotkey)
        self.hotkey.start()

    def _on_hotkey(self):
        """Hotkey global pressionada — toggle mic ou mostrar janela."""
        self.root.after(0, self._hotkey_action)

    def _hotkey_action(self):
        # Mostrar janela se minimizada
        self.root.deiconify()
        self.root.lift()
        self.root.focus_force()
        # No modo ao vivo: toggle mute. No modo texto: toggle MIC
        if self._live_mode:
            self._toggle_mute()
        else:
            self._toggle_mic()

    # ─── Status ──────────────────────────────────────────

    def _set_status(self, text, color=None):
        """Atualiza status bar — thread-safe via root.after."""
        color = color or self.TEXT_DIM
        def _update():
            try:
                self.status_dot.itemconfig(self.dot_id, fill=color)
                self.status_label.config(text=text, fg=color)
            except tk.TclError:
                pass
        try:
            self.root.after(0, _update)
        except RuntimeError:
            pass

    def _update_level_loop(self):
        """Atualiza barra de nível de áudio a cada 50ms."""
        try:
            width = min(120, int(self._audio_level * 2500))
            color = self.ACCENT if not self.recording else self.RED
            self.level_canvas.coords(self.level_bar, 0, 0, width, 6)
            self.level_canvas.itemconfig(self.level_bar, fill=color)
        except tk.TclError:
            pass
        self.root.after(50, self._update_level_loop)

    # ─── Chat bubbles ────────────────────────────────────

    def _add_welcome(self):
        self._add_bubble(
            "Olá! Sou o Voice Agent. Clique no MIC para ditar, "
            "digite abaixo, ou clique em Ao Vivo para conversar por voz.",
            "agent",
        )

    # ─── Modo Ao Vivo ────────────────────────────────────

    def _enter_live_mode(self):
        """Ativa modo ao vivo: VAD contínuo + resposta em áudio."""
        if self.processing or self.recording:
            return
        self._live_mode = True
        self._live_muted = False

        # Trocar barras
        self._text_input_frame.pack_forget()
        self._live_bar.pack(fill="x", padx=12, pady=8)

        self._mute_btn.config(text="  MUTE  ", bg="#4a5568")
        self._set_status("Ao Vivo \u2014 escutando", self.ACCENT)

        # Iniciar VAD
        self._start_continuous()

        # Iniciar animação waveform
        self._animate_waveform()

    def _exit_live_mode(self):
        """Desativa modo ao vivo, volta ao modo texto."""
        self._live_mode = False
        self._live_muted = False

        # Parar VAD
        self._stop_continuous()

        # Parar TTS se estiver falando
        if self.speaking:
            self.tts_stop.set()
            sd.stop()
            self.speaking = False

        # Trocar barras
        self._live_bar.pack_forget()
        self._text_input_frame.pack(fill="x", padx=12, pady=8)

        self._set_status("Pronto", self.TEXT_DIM)

    def _toggle_mute(self):
        """Alterna mute no modo ao vivo."""
        if not self._live_mode:
            return
        self._live_muted = not self._live_muted
        if self._live_muted:
            self._mute_btn.config(text=" UNMUTE ", bg="#dc2626")
            self._set_status("Ao Vivo \u2014 mudo", "#6b7280")
        else:
            self._mute_btn.config(text="  MUTE  ", bg="#4a5568")
            self._set_status("Ao Vivo \u2014 escutando", self.ACCENT)

    def _animate_waveform(self):
        """Anima as barras do waveform no modo ao vivo."""
        if not self._live_mode:
            # Resetar barras
            for bar in self._waveform_bars:
                self._live_waveform.coords(bar, 0, 12, 0, 12)
            return
        for i, bar in enumerate(self._waveform_bars):
            if self._live_muted or not self._continuous_running:
                h = 1
            elif self.speaking:
                h = random.randint(3, 11)
            else:
                h = max(1, int(self._audio_level * 500)) + random.randint(0, 3)
            x1 = i * 10 + 2
            x2 = i * 10 + 8
            self._live_waveform.coords(bar, x1, 12 - h, x2, 12 + h)
            color = "#7c3aed" if not self.speaking else self.BLUE
            if self._live_muted:
                color = "#4a5568"
            self._live_waveform.itemconfig(bar, fill=color)
        self.root.after(80, self._animate_waveform)

    # ─── MIC (modo texto — grava e transcreve para o campo) ──

    def _toggle_mic(self):
        """No modo texto: grava áudio e transcreve para o campo de texto."""
        if self.processing:
            return
        if self.recording:
            self._stop_recording_to_field()
        else:
            self._start_recording_to_field()

    def _start_recording_to_field(self):
        """Inicia gravação para transcrição no campo de texto."""
        if self.speaking:
            self.tts_stop.set()
            sd.stop()
            self.speaking = False

        self.recording = True
        self.cancelled = False
        self.audio_buffer = []
        self._audio_level = 0.0
        self.mic_btn.config(text="  PARE  ", bg=self.RED)
        self._set_status("Gravando... (ESC cancela)", self.RED)

        mic_device = self.cfg.get("mic_device")

        def _callback(indata, frames, time_info, status):
            if self.recording:
                self.audio_buffer.append(indata[:, 0].copy())
                self._audio_level = float(np.abs(indata).mean())

        try:
            kwargs = {
                "samplerate": SAMPLE_RATE,
                "channels": CHANNELS,
                "dtype": "float32",
                "blocksize": 512,
                "callback": _callback,
            }
            if mic_device is not None:
                kwargs["device"] = mic_device
            self.audio_stream = sd.InputStream(**kwargs)
            self.audio_stream.start()
        except Exception as e:
            log.error(f"Mic error: {e}")
            self._set_status(f"Erro mic: {e}", self.RED)
            self.recording = False
            self.mic_btn.config(text="  MIC  ", bg=self.ACCENT)

    def _stop_recording_to_field(self, discard=False):
        """Para gravação e transcreve para o campo de texto."""
        self.recording = False
        self._audio_level = 0.0
        self.mic_btn.config(text="  MIC  ", bg=self.ACCENT)

        if self.audio_stream:
            try:
                self.audio_stream.stop()
                self.audio_stream.close()
            except Exception:
                pass
            self.audio_stream = None

        if discard or self.cancelled:
            self.audio_buffer = []
            self._set_status("Pronto")
            return

        if not self.audio_buffer:
            self._set_status("Pronto")
            return

        audio = np.concatenate(self.audio_buffer)
        self.audio_buffer = []

        if len(audio) < SAMPLE_RATE * 0.3:
            self._set_status("Muito curto", self.YELLOW)
            return

        # Transcrever em thread e inserir no campo de texto
        def _transcribe():
            self._set_status("Transcrevendo...", self.YELLOW)
            # Verificar se alguma API key está disponível
            has_groq = bool(self.cfg.get("groq_api_key", ""))
            has_openai = bool(self.cfg.get("openai_api_key", ""))
            if not has_groq and not has_openai:
                self._set_status("API key não configurada", self.RED)
                return
            text = transcribe_audio(audio, self.cfg)
            if text and len(text.strip()) >= 2:
                self.root.after(0, lambda t=text: self._insert_transcription(t))
                self._set_status("Pronto", self.ACCENT)
            else:
                self._set_status("Não entendi", self.YELLOW)

        threading.Thread(target=_transcribe, daemon=True).start()

    def _insert_transcription(self, text):
        """Insere texto transcrito no campo de entrada."""
        current = self.text_input.get()
        if current:
            self.text_input.insert("end", " " + text)
        else:
            self.text_input.insert(0, text)
        self.text_input.focus_set()

    # ─── Continuous listening (VAD) ──────────────────────

    def _start_continuous(self):
        """Inicia escuta contínua com VAD."""
        if self._continuous_running:
            return
        self._continuous_running = True
        self.vad.reset()

        mic_device = self.cfg.get("mic_device")

        _dbg_counter = [0]

        def _callback(indata, frames, time_info, status):
            if not self._continuous_running or self.processing:
                return
            frame = indata[:, 0].copy()
            self._audio_level = float(np.abs(frame).mean())

            # Debug: log nível de áudio a cada ~2s (62 frames = ~2s com blocksize=512)
            _dbg_counter[0] += 1
            if _dbg_counter[0] % 62 == 0:
                log.info(f"VAD debug: level={self._audio_level:.4f} speaking={self.speaking} muted={self._live_muted} vad_speaking={self.vad._is_speaking}")

            # Se mudo, não processar VAD
            if self._live_muted:
                return
            # Barge-in: se agente está falando e usuário fala alto, interromper TTS
            if self.speaking:
                if self._audio_level > 0.02:
                    # Sinalizar parada — sd.stop() via root.after para evitar deadlock no callback
                    self.tts_stop.set()
                    self.vad.reset()
                    self.root.after(0, self._barge_in_stop)
                return
            event, audio = self.vad.process_frame(frame)
            if event == "speech_start":
                log.info(f"VAD: speech_start detected (level={self._audio_level:.4f})")
                self.root.after(0, lambda: self._set_status("Ao Vivo \u2014 ouvindo...", self.ACCENT))
            elif event == "speech_end" and audio is not None:
                log.info(f"VAD: speech_end detected (audio_len={len(audio)}, min={SAMPLE_RATE * 0.4})")
                if len(audio) >= SAMPLE_RATE * 0.4:
                    self.root.after(0, lambda a=audio: self._process_audio_vad(a))

        try:
            kwargs = {
                "samplerate": SAMPLE_RATE,
                "channels": CHANNELS,
                "dtype": "float32",
                "blocksize": 512,
                "callback": _callback,
            }
            if mic_device is not None:
                kwargs["device"] = mic_device

            self._continuous_stream = sd.InputStream(**kwargs)
            self._continuous_stream.start()
            log.info("Continuous listening started")
        except Exception as e:
            log.error(f"Continuous mic error: {e}")
            self._continuous_running = False

    def _stop_continuous(self):
        """Para escuta contínua."""
        self._continuous_running = False
        if self._continuous_stream:
            try:
                self._continuous_stream.stop()
                self._continuous_stream.close()
            except Exception:
                pass
            self._continuous_stream = None
        self.vad.reset()
        log.info("Continuous listening stopped")

    def _barge_in_stop(self):
        """Para TTS na thread principal (seguro, fora do callback de áudio)."""
        sd.stop()
        self.speaking = False
        self._set_status("Ao Vivo \u2014 ouvindo...", self.ACCENT)

    def _process_audio_vad(self, audio):
        """Processa áudio capturado pelo VAD (modo contínuo)."""
        if self.processing:
            return
        threading.Thread(target=self._process_audio, args=(audio,), daemon=True).start()

    # ─── Info dialog ─────────────────────────────────────

    def _show_info(self):
        """Mostra diálogo com informações detalhadas."""
        win = tk.Toplevel(self.root)
        win.title("Informações")
        win.geometry("500x560")
        win.configure(bg=self.BG)
        win.transient(self.root)
        win.grab_set()
        win.resizable(False, False)

        info_text = (
            "VOICE AGENT\n"
            "Assistente de voz bidirecional integrado ao Claude Code.\n\n"
            "MODO TEXTO (padrão)\n\n"
            "Digite sua mensagem e clique Enviar (ou Enter). "
            "O Claude responde em texto no chat.\n\n"
            "Botão MIC: Clique para gravar sua voz. O áudio é "
            "transcrito e inserido no campo de texto para revisão. "
            "Clique PARE ou ESC para encerrar. Depois clique Enviar.\n\n"
            "MODO AO VIVO\n\n"
            "Clique no botão Ao Vivo para ativar a conversa por voz contínua. "
            "O microfone fica sempre aberto e detecta automaticamente sua fala (VAD). "
            "O Claude responde em áudio e texto simultaneamente.\n\n"
            "MUTE: Silencia o microfone temporariamente.\n"
            "ENCERRAR: Volta ao modo texto.\n"
            "Barge-in: Fale enquanto o agente fala para interrompê-lo.\n\n"
            "BOTÃO PLAY\n"
            "Cada mensagem do agente tem um botão de play que permite "
            "ouvir a resposta novamente a qualquer momento.\n\n"
            "ATALHOS\n"
            f"Hotkey global: {self.cfg['hotkey']}\n"
            "  No modo texto: ativa a janela e o MIC\n"
            "  No modo ao vivo: toggle mute\n"
            "ESC: Cancela gravação, interrompe fala, ou sai do modo ao vivo\n"
            "Ctrl+N: Nova conversa\n"
            "Enter: Envia texto digitado\n\n"
            "INTEGRAÇÃO\n"
            "O Voice Agent usa o Claude Code como backend, "
            "com acesso a todas as ferramentas configuradas: "
            "Pipedrive, ClickUp, Outlook, n8n, ChatGuru e outras."
        )

        text_widget = tk.Text(
            win, font=("Segoe UI", 10), fg=self.TEXT, bg=self.BG,
            wrap="word", bd=0, padx=20, pady=16,
            highlightthickness=0, relief="flat",
        )
        text_widget.pack(fill="both", expand=True)
        text_widget.insert("1.0", info_text)
        text_widget.config(state="disabled")

        tk.Button(
            win, text="Fechar", font=("Segoe UI", 10),
            fg="#fff", bg=self.BG_INPUT, bd=0, padx=20, pady=6,
            cursor="hand2", command=win.destroy,
        ).pack(pady=12)

    # ─── Chat bubbles ────────────────────────────────────

    def _add_bubble(self, text, role):
        is_agent = role == "agent"

        # Podar bubbles antigas para evitar memory leak
        max_bubbles = self.cfg.get("max_history", 20) * 2  # user + agent = 2 por interação
        children = self.chat_frame.winfo_children()
        while len(children) > max_bubbles:
            children[0].destroy()
            children.pop(0)

        row = tk.Frame(self.chat_frame, bg=self.BG)
        row.pack(fill="x", padx=14, pady=4)

        # Avatar
        av_canvas = tk.Canvas(row, width=28, height=28, bg=self.BG, highlightthickness=0)
        av_color = self.ACCENT if is_agent else self.BLUE
        av_canvas.create_oval(1, 1, 27, 27, fill=av_color, outline="")
        av_canvas.create_text(14, 14, text="VA" if is_agent else "Eu",
                              font=("Segoe UI Semibold", 7), fill="#fff")

        # Bubble
        bubble_bg = self.BG_BUBBLE_AGENT if is_agent else self.BG_INPUT
        bubble = tk.Frame(row, bg=bubble_bg, padx=12, pady=8)
        lbl = tk.Label(
            bubble, text=text, font=("Segoe UI", 10),
            fg=self.TEXT, bg=bubble_bg, wraplength=420, justify="left", anchor="w",
        )
        lbl.pack(side="left", fill="x", expand=True)

        # Botão Play para mensagens do agente
        if is_agent and text and len(text) > 5:
            msg_index = len(self._message_texts)
            self._message_texts.append(text)
            play_btn = tk.Button(
                bubble, text="\u25B6", font=("Segoe UI", 9),
                fg=self.PLAY_COLOR, bg=bubble_bg, bd=0,
                cursor="hand2", padx=4,
                command=lambda idx=msg_index: self._play_message(idx),
            )
            play_btn.pack(side="right", padx=(4, 0), anchor="n")

        if is_agent:
            av_canvas.pack(side="left", anchor="n", padx=(0, 8), pady=3)
            bubble.pack(side="left", fill="x", expand=True)
        else:
            bubble.pack(side="right", fill="x", expand=True)
            av_canvas.pack(side="right", anchor="n", padx=(8, 0), pady=3)

        self.root.after(50, lambda: self.canvas.yview_moveto(1.0))
        return lbl

    def _play_message(self, msg_index):
        """Reproduz uma mensagem do agente pelo índice."""
        if self.processing:
            return
        if msg_index < 0 or msg_index >= len(self._message_texts):
            return
        text = self._message_texts[msg_index]
        if self.speaking:
            self.tts_stop.set()
            sd.stop()
            self.speaking = False

        def _worker():
            self.speaking = True
            self.tts_stop.clear()
            self._set_status("Reproduzindo...", self.BLUE)
            speak_text(text, self.cfg, self.tts_stop)
            self.speaking = False
            if self._live_mode:
                self._set_status("Ao Vivo \u2014 escutando", self.ACCENT)
            else:
                self._set_status("Pronto")

        threading.Thread(target=_worker, daemon=True).start()

    # ─── Process audio (usado pelo modo ao vivo via VAD) ──

    def _process_audio(self, audio):
        """Pipeline modo ao vivo: STT → bubble → Claude → TTS + bubble."""
        self.processing = True
        self.cancelled = False
        # Capturar modo no início — evita race condition se usuário sair do ao vivo durante pipeline
        was_live = self._live_mode

        try:
            # 1. STT
            self._set_status("Ao Vivo \u2014 transcrevendo..." if was_live else "Transcrevendo...", self.YELLOW)
            has_groq = bool(self.cfg.get("groq_api_key", ""))
            has_openai = bool(self.cfg.get("openai_api_key", ""))
            if not has_groq and not has_openai:
                self._set_status("API key não configurada", self.RED)
                self.processing = False
                return

            text = transcribe_audio(audio, self.cfg)

            if self.cancelled:
                self._set_status("Cancelado")
                self.processing = False
                return

            if not text or len(text.strip()) < 2:
                if was_live and self._live_mode:
                    self._set_status("Ao Vivo \u2014 escutando", self.ACCENT)
                else:
                    self._set_status("Pronto", self.TEXT_DIM)
                self.processing = False
                return

            self.root.after(0, lambda t=text: self._add_bubble(t, "user"))

            # 2. Claude
            self._set_status("Ao Vivo \u2014 pensando..." if was_live else "Pensando...", self.PURPLE)
            response = self.claude.ask(text)

            if self.cancelled:
                self._set_status("Cancelado")
                self.processing = False
                return

            if response:
                # Sempre mostrar texto na bubble
                self.root.after(0, lambda r=response: self._add_bubble(r, "agent"))

                # TTS automático só se começou no modo ao vivo E ainda está nele
                if was_live and self._live_mode:
                    self._set_status("Ao Vivo \u2014 respondendo", self.BLUE)
                    self.speaking = True
                    self.tts_stop.clear()
                    speak_text(response, self.cfg, self.tts_stop)
                    self.speaking = False

            # Status final: checar estado ATUAL (pode ter mudado)
            if self._live_mode:
                self._set_status("Ao Vivo \u2014 escutando", self.ACCENT)
            else:
                self._set_status("Pronto")

        except Exception as e:
            log.error(f"Pipeline error: {e}")
            self._set_status(f"Erro: {e}", self.RED)
        finally:
            self.processing = False

    # ─── Text input ──────────────────────────────────────

    def _on_text_submit(self, event=None):
        text = self.text_input.get().strip()
        if not text or self.processing:
            return
        self.text_input.delete(0, "end")

        # Barge-in via texto
        if self.speaking:
            self.tts_stop.set()
            sd.stop()
            self.speaking = False

        self._add_bubble(text, "user")
        threading.Thread(target=self._process_text, args=(text,), daemon=True).start()

    def _process_text(self, text):
        """Processa texto digitado — resposta em TEXTO (sem TTS)."""
        self.processing = True
        self.cancelled = False
        try:
            self._set_status("Pensando...", self.PURPLE)
            response = self.claude.ask(text)

            if self.cancelled:
                self._set_status("Cancelado")
                self.processing = False
                return

            if response:
                self.root.after(0, lambda r=response: self._add_bubble(r, "agent"))

            self._set_status("Pronto")
        except Exception as e:
            log.error(f"Text error: {e}")
            self._set_status(f"Erro: {e}", self.RED)
        finally:
            self.processing = False

    # ─── Settings ────────────────────────────────────────

    def _test_voice(self, voice_key, engine=None):
        """Reproduz uma amostra da voz selecionada."""
        if self.speaking:
            self.tts_stop.set()
            sd.stop()
            self.speaking = False
            return

        def _worker():
            self.speaking = True
            self.tts_stop.clear()
            self._set_status("Testando voz...", self.BLUE)
            test_cfg = dict(self.cfg)
            test_cfg["tts_voice"] = voice_key
            if engine:
                test_cfg["tts_engine"] = engine
            speak_text("Olá! Essa é a minha voz. Como posso te ajudar?", test_cfg, self.tts_stop)
            self.speaking = False
            if self._live_mode:
                self._set_status("Ao Vivo \u2014 escutando", self.ACCENT)
            else:
                self._set_status("Pronto")

        threading.Thread(target=_worker, daemon=True).start()

    def _open_settings(self):
        win = tk.Toplevel(self.root)
        win.title("Voice Agent \u2014 Configura\u00e7\u00f5es")
        win.configure(bg=self.BG)
        win.transient(self.root)
        win.grab_set()
        win.resizable(False, False)
        win.attributes("-topmost", True)

        w, h = 480, 720
        sx = (win.winfo_screenwidth() - w) // 2
        sy = (win.winfo_screenheight() - h) // 2
        win.geometry(f"{w}x{h}+{sx}+{sy}")

        # Estilo do combobox (dark mode)
        style = ttk.Style()
        style.theme_use("clam")
        style.configure("TCombobox",
                         fieldbackground=self.BG_INPUT, background=self.BG_CARD,
                         foreground=self.TEXT, arrowcolor=self.ACCENT, borderwidth=0)
        style.map("TCombobox",
                  fieldbackground=[("readonly", self.BG_INPUT), ("disabled", self.BG)],
                  foreground=[("readonly", self.TEXT), ("disabled", self.TEXT_DIM)],
                  selectbackground=[("readonly", self.BG_INPUT)],
                  selectforeground=[("readonly", self.TEXT)])
        win.option_add("*TCombobox*Listbox.background", self.BG_INPUT)
        win.option_add("*TCombobox*Listbox.foreground", self.TEXT)
        win.option_add("*TCombobox*Listbox.selectBackground", self.ACCENT)
        win.option_add("*TCombobox*Listbox.selectForeground", "#ffffff")

        # Título
        tk.Label(win, text="Configura\u00e7\u00f5es",
                 font=("Segoe UI Semibold", 14), fg="#ffffff", bg=self.BG
                 ).pack(pady=(16, 4))

        # Footer (ancorado no fundo ANTES do conteúdo)
        footer = tk.Frame(win, bg=self.BG)
        footer.pack(side="bottom", fill="x", padx=16, pady=(4, 12))

        save_btn = tk.Button(footer, text="Salvar", font=("Segoe UI", 11, "bold"),
                              bg=self.ACCENT, fg="#ffffff", relief="flat", cursor="hand2",
                              padx=30, pady=6, command=lambda: _save())
        save_btn.pack(pady=(0, 2))
        save_btn.bind("<Enter>", lambda e: save_btn.config(bg="#1A8744"))
        save_btn.bind("<Leave>", lambda e: save_btn.config(bg=self.ACCENT))

        session_text = self.claude.session_id or "Sem sess\u00e3o"
        if self.claude.session_id and len(self.claude.session_id) > 16:
            session_text = self.claude.session_id[:16] + "..."
        tk.Label(footer, text=f"Sess\u00e3o: {session_text}",
                 font=("Segoe UI", 8), fg="#4a5568", bg=self.BG).pack()

        # Área scrollável
        settings_canvas = tk.Canvas(win, bg=self.BG_INPUT, highlightthickness=0)
        settings_scroll = ttk.Scrollbar(win, orient="vertical", command=settings_canvas.yview)
        settings_frame = tk.Frame(settings_canvas, bg=self.BG_INPUT)
        settings_frame.bind("<Configure>",
            lambda e: settings_canvas.configure(scrollregion=settings_canvas.bbox("all")))
        settings_canvas.create_window((0, 0), window=settings_frame, anchor="nw", width=w - 20)
        settings_canvas.configure(yscrollcommand=settings_scroll.set)
        settings_canvas.bind("<MouseWheel>", lambda e: settings_canvas.yview_scroll(int(-1*(e.delta/120)), "units"))
        settings_frame.bind("<MouseWheel>", lambda e: settings_canvas.yview_scroll(int(-1*(e.delta/120)), "units"))
        settings_scroll.pack(side="right", fill="y")
        settings_canvas.pack(side="left", fill="both", expand=True, padx=10, pady=(4, 0))

        bg = self.BG_INPUT
        pad = {"padx": 16, "pady": (10, 2)}
        field_pad = {"padx": 16, "pady": (0, 6)}

        def _lbl(text):
            tk.Label(settings_frame, text=text, font=("Segoe UI", 9),
                     fg=self.TEXT_DIM, bg=bg, anchor="w").pack(fill="x", **pad)

        # ─── API Key OpenAI (TTS + STT fallback)
        _lbl("API Key OpenAI (TTS e STT fallback):")
        key_frame = tk.Frame(settings_frame, bg=bg)
        key_frame.pack(fill="x", **field_pad)
        api_entry = tk.Entry(key_frame, font=("Consolas", 10), fg=self.TEXT, bg=self.BG,
                             insertbackground=self.ACCENT, relief="flat", show="*",
                             bd=0, highlightthickness=1,
                             highlightbackground=self.BG_CARD, highlightcolor=self.ACCENT)
        api_entry.pack(side="left", fill="x", expand=True, ipady=4)
        api_entry.insert(0, self.cfg.get("openai_api_key", ""))
        show_key = [False]
        def _toggle_key():
            show_key[0] = not show_key[0]
            api_entry.config(show="" if show_key[0] else "*")
            toggle_btn.config(text="Ocultar" if show_key[0] else "Mostrar")
        toggle_btn = tk.Button(key_frame, text="Mostrar", font=("Segoe UI", 8),
                               bg=self.BG_CARD, fg=self.TEXT_DIM, relief="flat",
                               cursor="hand2", command=_toggle_key)
        toggle_btn.pack(side="right", padx=(5, 0))

        # ─── API Key Groq (STT rápido)
        _lbl("API Key Groq (STT rápido):")
        groq_frame = tk.Frame(settings_frame, bg=bg)
        groq_frame.pack(fill="x", **field_pad)
        groq_entry = tk.Entry(groq_frame, font=("Consolas", 10), fg=self.TEXT, bg=self.BG,
                              insertbackground=self.ACCENT, relief="flat", show="*",
                              bd=0, highlightthickness=1,
                              highlightbackground=self.BG_CARD, highlightcolor=self.ACCENT)
        groq_entry.pack(side="left", fill="x", expand=True, ipady=4)
        groq_entry.insert(0, self.cfg.get("groq_api_key", ""))
        show_groq = [False]
        def _toggle_groq():
            show_groq[0] = not show_groq[0]
            groq_entry.config(show="" if show_groq[0] else "*")
            toggle_groq_btn.config(text="Ocultar" if show_groq[0] else "Mostrar")
        toggle_groq_btn = tk.Button(groq_frame, text="Mostrar", font=("Segoe UI", 8),
                                     bg=self.BG_CARD, fg=self.TEXT_DIM, relief="flat",
                                     cursor="hand2", command=_toggle_groq)
        toggle_groq_btn.pack(side="right", padx=(5, 0))

        # ─── Motor STT
        _lbl("Motor de transcrição (STT):")
        stt_var = tk.StringVar(value=self.cfg.get("stt_engine", "groq"))
        stt_frame = tk.Frame(settings_frame, bg=bg)
        stt_frame.pack(fill="x", **field_pad)
        for val, label in [("groq", "Groq Whisper (rápido)"), ("openai", "OpenAI Whisper")]:
            tk.Radiobutton(
                stt_frame, text=label, variable=stt_var, value=val,
                font=("Segoe UI", 10), fg=self.TEXT, bg=bg,
                selectcolor=self.BG, activebackground=bg,
                activeforeground=self.TEXT,
            ).pack(side="left", padx=(0, 12))

        # ─── Modelo Claude
        _lbl("Modelo Claude:")
        model_var = tk.StringVar(value=self.cfg.get("claude_model", "sonnet"))
        model_frame = tk.Frame(settings_frame, bg=bg)
        model_frame.pack(fill="x", **field_pad)
        for val, label in [("haiku", "Haiku 4.5 (rápido)"), ("sonnet", "Sonnet 4.6 (recomendado)"), ("opus", "Opus 4.6 (máximo)")]:
            tk.Radiobutton(
                model_frame, text=label, variable=model_var, value=val,
                font=("Segoe UI", 10), fg=self.TEXT, bg=bg,
                selectcolor=self.BG, activebackground=bg,
                activeforeground=self.TEXT,
            ).pack(side="left", padx=(0, 12))

        # ─── Motor TTS
        _lbl("Motor de voz (TTS):")
        tts_engine_var = tk.StringVar(value=self.cfg.get("tts_engine", "openai"))
        tts_engine_frame = tk.Frame(settings_frame, bg=bg)
        tts_engine_frame.pack(fill="x", **field_pad)
        for val, label in [("openai", "OpenAI (recomendado)"), ("edge", "Edge TTS (gratuito)")]:
            tk.Radiobutton(
                tts_engine_frame, text=label, variable=tts_engine_var, value=val,
                font=("Segoe UI", 10), fg=self.TEXT, bg=bg,
                selectcolor=self.BG, activebackground=bg,
                activeforeground=self.TEXT,
            ).pack(side="left", padx=(0, 12))

        # ─── Voz TTS + botão de teste (dinâmico por motor)
        _lbl("Voz (Modo Ao Vivo e Play):")
        voice_data = [{}]  # mutable container for current voice map

        def _get_voice_map():
            engine = tts_engine_var.get()
            return OPENAI_TTS_VOICES if engine == "openai" else EDGE_TTS_VOICES

        def _update_voice_combo(*args):
            vmap = _get_voice_map()
            voice_data[0] = vmap
            keys = list(vmap.keys())
            labels = list(vmap.values())
            voice_combo["values"] = labels
            # Tentar manter a voz atual se existir no novo motor
            current = self.cfg.get("tts_voice", "")
            if current in vmap:
                voice_var.set(vmap[current])
            else:
                voice_var.set(labels[0] if labels else "")

        voice_var = tk.StringVar()
        voice_frame = tk.Frame(settings_frame, bg=bg)
        voice_frame.pack(fill="x", **field_pad)
        voice_combo = ttk.Combobox(voice_frame, textvariable=voice_var, state="readonly",
                                    values=[], font=("Segoe UI", 10))
        voice_combo.pack(side="left", fill="x", expand=True, ipady=4)

        tts_engine_var.trace_add("write", _update_voice_combo)
        _update_voice_combo()  # init

        def _test_selected_voice():
            vmap = _get_voice_map()
            keys = list(vmap.keys())
            labels = list(vmap.values())
            sel_label = voice_var.get()
            idx = labels.index(sel_label) if sel_label in labels else 0
            self._test_voice(keys[idx], engine=tts_engine_var.get())

        test_voice_btn = tk.Button(voice_frame, text="\u25B6 Testar", font=("Segoe UI", 9),
                                    bg=self.BG_CARD, fg=self.PLAY_COLOR, relief="flat",
                                    cursor="hand2", padx=8, command=_test_selected_voice)
        test_voice_btn.pack(side="right", padx=(8, 0))

        # ─── Velocidade da voz
        _lbl("Velocidade da voz:")
        speed_frame = tk.Frame(settings_frame, bg=bg)
        speed_frame.pack(fill="x", **field_pad)
        tk.Label(speed_frame, text="0.5x", font=("Segoe UI", 8), fg=self.TEXT_DIM, bg=bg).pack(side="left")
        speed_scale = tk.Scale(
            speed_frame, from_=0.5, to=2.0, resolution=0.1, orient="horizontal",
            font=("Segoe UI", 8), fg=self.TEXT, bg=bg, troughcolor=self.BG,
            highlightthickness=0, bd=0, length=220,
        )
        speed_scale.set(self.cfg.get("tts_speed", 1.0))
        speed_scale.pack(side="left", fill="x", expand=True, padx=4)
        tk.Label(speed_frame, text="2.0x", font=("Segoe UI", 8), fg=self.TEXT_DIM, bg=bg).pack(side="left")

        # ─── Instruções de voz (estilo)
        _lbl("Instruções de voz (estilo — só OpenAI TTS):")
        tts_instr_entry = tk.Entry(settings_frame, font=("Segoe UI", 10), fg=self.TEXT, bg=self.BG,
                                    insertbackground=self.ACCENT, relief="flat",
                                    bd=0, highlightthickness=1,
                                    highlightbackground=self.BG_CARD, highlightcolor=self.ACCENT)
        tts_instr_entry.pack(fill="x", ipady=4, **field_pad)
        tts_instr_entry.insert(0, self.cfg.get("tts_instructions", ""))

        # ─── Microfone
        _lbl("Microfone:")
        devices = get_input_devices()
        dev_names = ["Padrão do sistema"] + [d["name"] for d in devices]
        dev_indices = [None] + [d["index"] for d in devices]
        mic_var = tk.StringVar()

        current_mic = self.cfg.get("mic_device")
        if current_mic is not None:
            for d in devices:
                if d["index"] == current_mic:
                    mic_var.set(d["name"])
                    break
            else:
                mic_var.set("Padrão do sistema")
        else:
            mic_var.set("Padrão do sistema")

        mic_combo = ttk.Combobox(settings_frame, textvariable=mic_var, state="readonly",
                                  values=dev_names, font=("Segoe UI", 10))
        mic_combo.pack(fill="x", ipady=4, **field_pad)

        # ─── Hotkey (captura visual — clique para alterar)
        _lbl("Hotkey global (clique para alterar):")
        hk_var = tk.StringVar(value=self.cfg.get("hotkey", "Ctrl+Shift+Alt+F4"))
        hk_capturing = [False]
        hk_label = tk.Label(settings_frame, textvariable=hk_var,
                            font=("Segoe UI", 11), fg=self.TEXT, bg=self.BG,
                            relief="flat", anchor="center", cursor="hand2",
                            highlightthickness=1, highlightbackground=self.BG_CARD,
                            highlightcolor=self.ACCENT)
        hk_label.pack(fill="x", ipady=6, **field_pad)

        def _start_hk_capture(event=None):
            if hk_capturing[0]:
                return
            hk_capturing[0] = True
            hk_label.config(fg=self.BLUE, highlightbackground=self.BLUE)
            hk_var.set("Pressione o atalho...")
            hk_label.focus_set()
            hk_label.bind("<KeyPress>", _on_hk_key)
            hk_label.bind("<Escape>", lambda e: _cancel_hk())
            hk_label.bind("<FocusOut>", lambda e: _cancel_hk())

        _valid_fkeys = {f"F{i}" for i in range(1, 13)}

        def _on_hk_key(event):
            ignore = {"Shift_L", "Shift_R", "Control_L", "Control_R", "Alt_L", "Alt_R"}
            if event.keysym in ignore:
                return
            parts = []
            if event.state & 0x4:
                parts.append("Ctrl")
            if event.state & 0x1:
                parts.append("Shift")
            if event.state & 0x20000:
                parts.append("Alt")
            if not parts:
                return
            key = event.keysym
            if len(key) == 1 and key.isalpha():
                key = key.upper()
            elif key not in _valid_fkeys:
                return
            parts.append(key)
            hk_var.set("+".join(parts))
            _finish_hk()

        def _cancel_hk():
            if not hk_capturing[0]:
                return
            hk_var.set(self.cfg.get("hotkey", "Ctrl+Shift+Alt+F4"))
            _finish_hk()

        def _finish_hk():
            hk_capturing[0] = False
            hk_label.config(fg=self.TEXT, highlightbackground=self.BG_CARD)
            hk_label.unbind("<KeyPress>")
            hk_label.unbind("<Escape>")
            hk_label.unbind("<FocusOut>")

        hk_label.bind("<Button-1>", _start_hk_capture)

        # ─── Save
        def _save():
            self.cfg["openai_api_key"] = api_entry.get().strip()
            self.cfg["groq_api_key"] = groq_entry.get().strip()
            self.cfg["stt_engine"] = stt_var.get()
            self.cfg["claude_model"] = model_var.get()
            self.cfg["tts_engine"] = tts_engine_var.get()
            self.cfg["hotkey"] = hk_var.get().strip()
            self.cfg["tts_speed"] = speed_scale.get()
            self.cfg["tts_instructions"] = tts_instr_entry.get().strip()

            # Voz (do motor selecionado)
            vmap = _get_voice_map()
            keys = list(vmap.keys())
            labels = list(vmap.values())
            sel_voice_label = voice_var.get()
            voice_idx = labels.index(sel_voice_label) if sel_voice_label in labels else 0
            self.cfg["tts_voice"] = keys[voice_idx]

            # Mic
            sel = mic_var.get()
            idx_sel = dev_names.index(sel) if sel in dev_names else 0
            self.cfg["mic_device"] = dev_indices[idx_sel]

            save_config(self.cfg)
            self.claude.model = self.cfg["claude_model"]

            # Reiniciar hotkey se mudou
            if self.hotkey:
                self.hotkey.stop()
            self._start_hotkey()

            win.destroy()
            log.info("Settings saved")

    # ─── Actions ─────────────────────────────────────────

    def _new_conversation(self):
        # Parar modo ao vivo se ativo
        if self._live_mode:
            self._exit_live_mode()
        # Parar tudo
        if self.speaking:
            self.tts_stop.set()
            sd.stop()
            self.speaking = False
        if self.recording:
            self._stop_recording_to_field(discard=True)

        self.claude.reset()
        self._message_texts = []
        for w in self.chat_frame.winfo_children():
            w.destroy()
        self._add_welcome()
        self._set_status("Nova conversa")

    # ─── Run ─────────────────────────────────────────────

    def run(self):
        log.info("Voice Agent started")
        self.root.protocol("WM_DELETE_WINDOW", self._on_close)
        self.root.mainloop()

    def _on_close(self):
        if self.hotkey:
            self.hotkey.stop()
        if self._live_mode:
            self._exit_live_mode()
        if self._continuous_running:
            self._stop_continuous()
        if self.recording:
            self._stop_recording_to_field(discard=True)
        if self.speaking:
            self.tts_stop.set()
            sd.stop()
        self.root.destroy()


# ─── Entry point ─────────────────────────────────────────────

if __name__ == "__main__":
    app = VoiceAgentApp()
    app.run()
