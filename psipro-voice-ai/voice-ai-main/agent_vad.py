"""
Agent VAD — Voice Activity Detection usando Silero VAD (ONNX puro).
Detecta início/fim de fala em stream de áudio do microfone.
Sem dependência de torch — usa apenas numpy + onnxruntime.
"""

import os
import sys
import logging
import threading
import time
import numpy as np

from agent_config import (
    VAD_THRESHOLD, MIN_SILENCE_MS, MIN_SPEECH_MS,
    VAD_SAMPLE_RATE, VAD_FRAME_MS, VAD_MODELS_DIR,
)

log = logging.getLogger(__name__).info


def _find_onnx_model():
    """Localiza silero_vad.onnx no pacote pip ou em _MEIPASS (frozen)."""
    # 1) Frozen (PyInstaller): silero_vad/data/silero_vad.onnx em _MEIPASS
    if getattr(sys, "frozen", False):
        meipass = getattr(sys, "_MEIPASS", "")
        candidate = os.path.join(meipass, "silero_vad", "data", "silero_vad.onnx")
        if os.path.exists(candidate):
            return candidate

    # 2) Pacote pip instalado
    try:
        from importlib import resources as impresources
        with impresources.path("silero_vad.data", "silero_vad.onnx") as f:
            if os.path.exists(str(f)):
                return str(f)
    except Exception:
        pass

    # 3) Fallback: importlib.resources.files (Python 3.9+)
    try:
        from importlib.resources import files
        p = str(files("silero_vad.data").joinpath("silero_vad.onnx"))
        if os.path.exists(p):
            return p
    except Exception:
        pass

    raise FileNotFoundError("silero_vad.onnx não encontrado")


class _OnnxVADModel:
    """Wrapper ONNX puro para Silero VAD — sem torch, apenas numpy."""

    def __init__(self, model_path: str):
        import onnxruntime

        opts = onnxruntime.SessionOptions()
        opts.inter_op_num_threads = 1
        opts.intra_op_num_threads = 1

        self.session = onnxruntime.InferenceSession(
            model_path,
            providers=["CPUExecutionProvider"],
            sess_options=opts,
        )
        self.sample_rates = [8000, 16000]
        self.reset_states()

    def reset_states(self, batch_size=1):
        self._state = np.zeros((2, batch_size, 128), dtype=np.float32)
        self._context = np.zeros(0, dtype=np.float32)
        self._last_sr = 0
        self._last_batch_size = 0

    def __call__(self, x: np.ndarray, sr: int) -> float:
        """Retorna probabilidade de fala (float) para um chunk de áudio.

        Args:
            x: array float32, shape (N,) com N=512 para 16kHz
            sr: sample rate (16000)
        """
        # Validação e reshape
        if x.ndim == 1:
            x = x[np.newaxis, :]  # (1, N)
        if x.ndim > 2:
            raise ValueError(f"Too many dimensions: {x.ndim}")

        if sr != 16000 and (sr % 16000 == 0):
            step = sr // 16000
            x = x[:, ::step]
            sr = 16000

        if sr not in self.sample_rates:
            raise ValueError(f"Supported: {self.sample_rates}")

        num_samples = 512 if sr == 16000 else 256
        if x.shape[-1] != num_samples:
            raise ValueError(f"Expected {num_samples} samples, got {x.shape[-1]}")

        batch_size = x.shape[0]
        context_size = 64 if sr == 16000 else 32

        if not self._last_batch_size:
            self.reset_states(batch_size)
        if self._last_sr and self._last_sr != sr:
            self.reset_states(batch_size)
        if self._last_batch_size and self._last_batch_size != batch_size:
            self.reset_states(batch_size)

        if self._context.size == 0:
            self._context = np.zeros((batch_size, context_size), dtype=np.float32)

        x = np.concatenate([self._context, x], axis=1).astype(np.float32)

        ort_inputs = {
            "input": x,
            "state": self._state,
            "sr": np.array(sr, dtype=np.int64),
        }
        ort_outs = self.session.run(None, ort_inputs)
        out, state = ort_outs

        self._state = state
        self._context = x[..., -context_size:]
        self._last_sr = sr
        self._last_batch_size = batch_size

        return float(out.squeeze())

    def eval(self):
        """Compatibilidade — noop para ONNX."""
        return self


class SileroVAD:
    """Wrapper para Silero VAD com detecção de fala/silêncio."""

    def __init__(self, threshold=None, min_silence_ms=None, min_speech_ms=None):
        self.threshold = threshold or VAD_THRESHOLD
        self.min_silence_ms = min_silence_ms or MIN_SILENCE_MS
        self.min_speech_ms = min_speech_ms or MIN_SPEECH_MS
        self.sample_rate = VAD_SAMPLE_RATE
        self.frame_samples = int(self.sample_rate * VAD_FRAME_MS / 1000)

        self._model = None
        self._loaded = False
        self._lock = threading.Lock()

    def load_model(self):
        """Carrega modelo Silero VAD ONNX (numpy puro, sem torch)."""
        if self._loaded:
            return

        with self._lock:
            if self._loaded:
                return

            try:
                model_path = _find_onnx_model()
                self._model = _OnnxVADModel(model_path)
                self._loaded = True
                log(f"Silero VAD loaded (ONNX, no torch): {model_path}")
            except Exception as e:
                log(f"Failed to load Silero VAD: {e}")
                raise

    def reset(self):
        """Reseta estado interno do modelo (chamar entre segmentos de fala)."""
        if self._model is not None:
            self._model.reset_states()

    def get_speech_prob(self, audio_chunk: np.ndarray) -> float:
        """Retorna probabilidade de fala (0.0–1.0) para um chunk de áudio.

        Args:
            audio_chunk: array float32, mono, 16kHz, shape (N,)
        """
        if not self._loaded:
            self.load_model()

        if audio_chunk.dtype != np.float32:
            audio_chunk = audio_chunk.astype(np.float32)

        return self._model(audio_chunk, self.sample_rate)

    def is_speech(self, audio_chunk: np.ndarray) -> bool:
        """Retorna True se o chunk contém fala acima do threshold."""
        return self.get_speech_prob(audio_chunk) >= self.threshold


class VADStream:
    """Stream VAD que processa áudio continuamente e emite eventos de fala."""

    STATE_IDLE = "idle"           # aguardando fala
    STATE_SPEAKING = "speaking"   # usuário está falando
    STATE_SILENCE = "silence"     # silêncio após fala (aguardando timeout)

    def __init__(self, vad: SileroVAD = None, on_speech_start=None,
                 on_speech_end=None, on_audio_level=None):
        self.vad = vad or SileroVAD()
        self.on_speech_start = on_speech_start  # callback()
        self.on_speech_end = on_speech_end      # callback(audio_buffer: np.ndarray)
        self.on_audio_level = on_audio_level    # callback(level: float) — para UI

        self._state = self.STATE_IDLE
        self._speech_buffer = []        # frames de áudio da fala atual
        self._speech_start_time = 0.0
        self._silence_start_time = 0.0
        self._running = False

    @property
    def state(self):
        return self._state

    @property
    def is_running(self):
        return self._running

    def start(self):
        """Inicia o processamento VAD."""
        self.vad.load_model()
        self.vad.reset()
        self._state = self.STATE_IDLE
        self._speech_buffer = []
        self._running = True
        log("VADStream started")

    def stop(self):
        """Para o processamento VAD."""
        self._running = False
        self._state = self.STATE_IDLE
        self._speech_buffer = []
        log("VADStream stopped")

    def process_frame(self, frame: np.ndarray):
        """Processa um frame de áudio (32ms, float32, mono, 16kHz).

        Gerencia a máquina de estados: idle → speaking → silence → idle.
        Chama callbacks on_speech_start/on_speech_end conforme transições.
        """
        if not self._running:
            return

        # Nível de áudio para visualização
        if self.on_audio_level:
            level = float(np.abs(frame).mean())
            self.on_audio_level(level)

        is_speech = self.vad.is_speech(frame)
        now = time.time()

        if self._state == self.STATE_IDLE:
            if is_speech:
                self._state = self.STATE_SPEAKING
                self._speech_start_time = now
                self._speech_buffer = [frame.copy()]
                if self.on_speech_start:
                    self.on_speech_start()

        elif self._state == self.STATE_SPEAKING:
            self._speech_buffer.append(frame.copy())
            if not is_speech:
                self._state = self.STATE_SILENCE
                self._silence_start_time = now

        elif self._state == self.STATE_SILENCE:
            self._speech_buffer.append(frame.copy())
            if is_speech:
                # Voltou a falar — continua
                self._state = self.STATE_SPEAKING
            else:
                elapsed_ms = (now - self._silence_start_time) * 1000
                if elapsed_ms >= self.vad.min_silence_ms:
                    # Silêncio suficiente — fim da fala
                    speech_duration_ms = (now - self._speech_start_time) * 1000
                    if speech_duration_ms >= self.vad.min_speech_ms:
                        # Fala válida — emitir
                        audio = np.concatenate(self._speech_buffer)
                        if self.on_speech_end:
                            self.on_speech_end(audio)
                    else:
                        log(f"Speech too short ({speech_duration_ms:.0f}ms), ignoring")

                    self._speech_buffer = []
                    self._state = self.STATE_IDLE
                    self.vad.reset()

    def force_end_speech(self):
        """Força o fim da fala atual (usado por barge-in)."""
        if self._state in (self.STATE_SPEAKING, self.STATE_SILENCE):
            if self._speech_buffer:
                audio = np.concatenate(self._speech_buffer)
                speech_ms = len(audio) / self.vad.sample_rate * 1000
                if speech_ms >= self.vad.min_speech_ms and self.on_speech_end:
                    self.on_speech_end(audio)
            self._speech_buffer = []
            self._state = self.STATE_IDLE
            self.vad.reset()
