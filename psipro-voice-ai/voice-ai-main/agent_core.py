"""
Agent Core — Loop principal do Claude Voice Agent.
Orquestra: VAD → STT → Claude API → TTS, com barge-in e streaming.
"""

import logging
import threading
import queue
import time
import re
import numpy as np
import sounddevice as sd

from agent_config import (
    SYSTEM_PROMPT, MAX_CONVERSATION_TURNS,
    AUDIO_SAMPLE_RATE, AUDIO_CHANNELS, AUDIO_DTYPE,
    VAD_FRAME_MS,
)
from agent_vad import SileroVAD, VADStream
from agent_stt import LocalSTT
from agent_tts import EdgeTTS

log = logging.getLogger(__name__).info


class SentenceSplitter:
    """Acumula tokens de streaming e emite frases completas para TTS paralelo."""

    TERMINATORS = re.compile(r'[.!?;:]\s')

    def __init__(self):
        self._buffer = ""

    def feed(self, token: str):
        """Alimenta um token e retorna frase completa se houver, ou None."""
        self._buffer += token
        match = self.TERMINATORS.search(self._buffer)
        if match:
            end = match.end()
            sentence = self._buffer[:end].strip()
            self._buffer = self._buffer[end:]
            return sentence
        return None

    def flush(self) -> str:
        """Retorna texto restante no buffer."""
        remaining = self._buffer.strip()
        self._buffer = ""
        return remaining


class AgentCore:
    """Loop principal do Claude Voice Agent.

    Gerencia o pipeline completo:
    1. Captura de áudio do microfone
    2. VAD: detecta fala e silêncio
    3. STT: transcreve fala para texto
    4. LLM: envia para Claude API (streaming)
    5. TTS: sintetiza resposta em voz (streaming paralelo)
    6. Barge-in: interrompe se usuário falar durante resposta
    """

    STATE_IDLE = "idle"               # aguardando ativação
    STATE_LISTENING = "listening"     # ouvindo (VAD ativo)
    STATE_TRANSCRIBING = "transcribing"  # STT processando
    STATE_THINKING = "thinking"       # Claude processando
    STATE_SPEAKING = "speaking"       # TTS reproduzindo
    STATE_ERROR = "error"

    def __init__(self, config_manager=None):
        self.cfg = config_manager

        # Componentes
        self.vad = SileroVAD()
        self.vad_stream = None
        self.stt = LocalSTT()
        self.tts = EdgeTTS()

        # Claude API
        self._anthropic_client = None

        # Estado
        self._state = self.STATE_IDLE
        self._running = False
        self._audio_stream = None
        self._conversation = []  # histórico [{role, content}]
        self._current_stream = None  # Claude stream ativo (para cancelar)

        # Callbacks para UI
        self.on_state_change = None       # callback(state: str)
        self.on_user_transcript = None    # callback(text: str)
        self.on_agent_text = None         # callback(text: str, partial: bool)
        self.on_audio_level = None        # callback(level: float)
        self.on_error = None              # callback(error: str)

        # Queue para coordenar threads
        self._event_queue = queue.Queue()

    @property
    def state(self):
        return self._state

    def _set_state(self, new_state):
        self._state = new_state
        if self.on_state_change:
            self.on_state_change(new_state)

    # ─── Inicialização ───────────────────────────────────────

    def initialize(self, anthropic_api_key: str, model: str = None,
                   whisper_model: str = None, voice: str = None,
                   silence_ms: int = None, max_turns: int = None):
        """Inicializa todos os componentes do agente.

        Args:
            anthropic_api_key: API key da Anthropic
            model: modelo Claude (claude-haiku-4-5 / claude-sonnet-4-6)
            whisper_model: modelo Whisper local (tiny/small/medium/large-v3)
            voice: voz Kokoro (af_heart / am_adam / etc.)
            silence_ms: ms de silêncio para considerar fim de fala
            max_turns: máximo de turnos na conversa
        """
        import anthropic

        self._anthropic_client = anthropic.Anthropic(api_key=anthropic_api_key)
        self._model = model or "claude-haiku-4-5"
        self._max_turns = max_turns or MAX_CONVERSATION_TURNS

        # Configurar VAD
        if silence_ms:
            self.vad.min_silence_ms = silence_ms

        # Configurar STT
        if whisper_model:
            self.stt.model_size = whisper_model

        # Configurar TTS
        if voice:
            self.tts.voice = voice

        # Pré-carregar modelos em threads separadas
        threads = [
            threading.Thread(target=self.vad.load_model, daemon=True),
            threading.Thread(target=self.stt.load_model, daemon=True),
            threading.Thread(target=self.tts.load_model, daemon=True),
        ]
        for t in threads:
            t.start()
        for t in threads:
            t.join(timeout=120)

        log("AgentCore initialized")

    # ─── Controle do loop ────────────────────────────────────

    def start_listening(self):
        """Inicia captura de áudio e VAD. O agente fica ouvindo."""
        if self._running:
            return

        self._running = True

        # Criar VADStream com callbacks
        self.vad_stream = VADStream(
            vad=self.vad,
            on_speech_start=self._on_speech_start,
            on_speech_end=self._on_speech_end,
            on_audio_level=self.on_audio_level,
        )
        self.vad_stream.start()

        # Abrir stream de áudio do microfone
        frame_samples = int(AUDIO_SAMPLE_RATE * VAD_FRAME_MS / 1000)
        self._audio_stream = sd.InputStream(
            samplerate=AUDIO_SAMPLE_RATE,
            channels=AUDIO_CHANNELS,
            dtype=AUDIO_DTYPE,
            blocksize=frame_samples,
            callback=self._audio_callback,
        )
        self._audio_stream.start()
        self._set_state(self.STATE_LISTENING)
        log("Listening started")

    def stop_listening(self):
        """Para captura de áudio e VAD."""
        self._running = False

        if self._audio_stream:
            try:
                self._audio_stream.stop()
                self._audio_stream.close()
            except Exception:
                pass
            self._audio_stream = None

        if self.vad_stream:
            self.vad_stream.stop()

        # Cancelar qualquer stream do Claude
        self._cancel_claude_stream()

        # Parar TTS
        self.tts.stop()

        self._set_state(self.STATE_IDLE)
        log("Listening stopped")

    def new_conversation(self):
        """Limpa histórico de conversa."""
        self._conversation = []
        log("Conversation cleared")

    # ─── Callbacks de áudio ──────────────────────────────────

    def _audio_callback(self, indata, frames, time_info, status):
        """Callback do sounddevice — processa cada frame no VADStream."""
        if not self._running:
            return
        if status:
            log(f"Audio status: {status}")

        # indata shape: (frames, channels) → flatten para mono
        frame = indata[:, 0].copy()

        # Durante TTS playback, verificar barge-in
        if self._state == self.STATE_SPEAKING:
            if self.vad.is_speech(frame):
                log("Barge-in detected!")
                self._handle_barge_in()
                return

        # Processar no VADStream (só quando ouvindo)
        if self._state == self.STATE_LISTENING:
            self.vad_stream.process_frame(frame)

    def _on_speech_start(self):
        """Callback do VADStream: usuário começou a falar."""
        log("Speech started")

    def _on_speech_end(self, audio: np.ndarray):
        """Callback do VADStream: usuário parou de falar. Transcrever e processar."""
        if not self._running:
            return

        # Processar em thread separada para não bloquear áudio
        threading.Thread(
            target=self._process_speech,
            args=(audio,),
            daemon=True,
        ).start()

    # ─── Pipeline principal ──────────────────────────────────

    def _process_speech(self, audio: np.ndarray):
        """Pipeline: STT → Claude → TTS (executado em thread separada)."""
        try:
            # 1. STT
            self._set_state(self.STATE_TRANSCRIBING)
            text = self.stt.transcribe(audio)

            if not text or len(text.strip()) < 2:
                log("Empty transcription, ignoring")
                self._set_state(self.STATE_LISTENING)
                return

            log(f"User said: {text}")
            if self.on_user_transcript:
                self.on_user_transcript(text)

            # 2. Claude API (streaming)
            self._set_state(self.STATE_THINKING)
            self._conversation.append({"role": "user", "content": text})

            # Limitar turnos
            if len(self._conversation) > self._max_turns * 2:
                self._conversation = self._conversation[-(self._max_turns * 2):]

            response_text = self._call_claude_streaming()

            if response_text:
                self._conversation.append({"role": "assistant", "content": response_text})
                if self.on_agent_text:
                    self.on_agent_text(response_text, partial=False)

            # Voltar a ouvir
            if self._running:
                self._set_state(self.STATE_LISTENING)

        except Exception as e:
            log(f"Pipeline error: {e}")
            self._set_state(self.STATE_ERROR)
            if self.on_error:
                self.on_error(str(e))
            # Recuperar para listening
            time.sleep(1)
            if self._running:
                self._set_state(self.STATE_LISTENING)

    def _call_claude_streaming(self) -> str:
        """Chama Claude API com streaming e TTS paralelo via SentenceSplitter."""
        if not self._anthropic_client:
            raise RuntimeError("Anthropic client not initialized")

        splitter = SentenceSplitter()
        full_response = []
        tts_queue = queue.Queue()

        # Thread de TTS que consome frases da fila
        def _tts_worker():
            while True:
                sentence = tts_queue.get()
                if sentence is None:  # sentinel
                    break
                if not self._running:
                    break
                self._set_state(self.STATE_SPEAKING)
                # Speak bloqueante nesta thread
                done_event = threading.Event()
                self.tts.speak(
                    sentence,
                    on_done=lambda interrupted: done_event.set(),
                )
                done_event.wait(timeout=30)

        tts_thread = threading.Thread(target=_tts_worker, daemon=True)
        tts_thread.start()

        try:
            with self._anthropic_client.messages.stream(
                model=self._model,
                max_tokens=1024,
                system=SYSTEM_PROMPT,
                messages=self._conversation,
            ) as stream:
                self._current_stream = stream

                for text in stream.text_stream:
                    if not self._running:
                        break

                    full_response.append(text)
                    if self.on_agent_text:
                        self.on_agent_text("".join(full_response), partial=True)

                    # Sentence splitter → TTS
                    sentence = splitter.feed(text)
                    if sentence:
                        tts_queue.put(sentence)

            # Flush remaining text
            remaining = splitter.flush()
            if remaining and self._running:
                tts_queue.put(remaining)

        except Exception as e:
            log(f"Claude API error: {e}")
            if self.on_error:
                self.on_error(f"Erro na API do Claude: {e}")
        finally:
            self._current_stream = None
            tts_queue.put(None)  # sentinel para encerrar TTS worker
            tts_thread.join(timeout=30)

        return "".join(full_response)

    # ─── Barge-in ────────────────────────────────────────────

    def _handle_barge_in(self):
        """Interrompe TTS e Claude stream quando usuário fala."""
        log("Handling barge-in")

        # Parar TTS
        self.tts.stop()

        # Cancelar stream do Claude
        self._cancel_claude_stream()

        # Voltar a ouvir
        self.vad.reset()
        self._set_state(self.STATE_LISTENING)

    def _cancel_claude_stream(self):
        """Cancela stream ativo do Claude."""
        if self._current_stream:
            try:
                self._current_stream.close()
            except Exception:
                pass
            self._current_stream = None

    # ─── Modo PTT (Push-to-Talk) ─────────────────────────────

    def ptt_start(self):
        """Inicia gravação manual (PTT)."""
        if self._state == self.STATE_SPEAKING:
            self._handle_barge_in()

        self._ptt_buffer = []
        self._set_state(self.STATE_LISTENING)

        frame_samples = int(AUDIO_SAMPLE_RATE * VAD_FRAME_MS / 1000)

        def _ptt_callback(indata, frames, time_info, status):
            if self._running:
                self._ptt_buffer.append(indata[:, 0].copy())
                if self.on_audio_level:
                    level = float(np.abs(indata).mean())
                    self.on_audio_level(level)

        self._ptt_running = True
        self._audio_stream = sd.InputStream(
            samplerate=AUDIO_SAMPLE_RATE,
            channels=AUDIO_CHANNELS,
            dtype=AUDIO_DTYPE,
            blocksize=frame_samples,
            callback=_ptt_callback,
        )
        self._audio_stream.start()
        self._running = True

    def ptt_stop(self):
        """Para gravação PTT e processa o áudio."""
        self._ptt_running = False
        if self._audio_stream:
            try:
                self._audio_stream.stop()
                self._audio_stream.close()
            except Exception:
                pass
            self._audio_stream = None

        if hasattr(self, "_ptt_buffer") and self._ptt_buffer:
            audio = np.concatenate(self._ptt_buffer)
            self._ptt_buffer = []
            threading.Thread(
                target=self._process_speech,
                args=(audio,),
                daemon=True,
            ).start()

    # ─── Método para processar texto digitado ────────────────

    def process_text_input(self, text: str):
        """Processa texto digitado (modo push_enter)."""
        if not text.strip():
            return

        if self.on_user_transcript:
            self.on_user_transcript(text)

        self._conversation.append({"role": "user", "content": text})

        if len(self._conversation) > self._max_turns * 2:
            self._conversation = self._conversation[-(self._max_turns * 2):]

        self._running = True

        def _worker():
            try:
                self._set_state(self.STATE_THINKING)
                response = self._call_claude_streaming()
                if response:
                    self._conversation.append({"role": "assistant", "content": response})
                    if self.on_agent_text:
                        self.on_agent_text(response, partial=False)
                self._set_state(self.STATE_IDLE)
            except Exception as e:
                log(f"Text input error: {e}")
                if self.on_error:
                    self.on_error(str(e))
                self._set_state(self.STATE_IDLE)

        threading.Thread(target=_worker, daemon=True).start()

    # ─── Cleanup ─────────────────────────────────────────────

    def shutdown(self):
        """Desliga todos os componentes."""
        self.stop_listening()
        self.stt.unload_model()
        self.tts.unload_model()
        self._conversation = []
        self._anthropic_client = None
        log("AgentCore shut down")
