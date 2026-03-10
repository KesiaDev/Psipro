"""
Agent STT — Speech-to-Text local usando faster-whisper.
Alternativa gratuita ao OpenAI Whisper API para o modo ditado e agente.
"""

import os
import io
import wave
import logging
import threading
import numpy as np

from agent_config import (
    WHISPER_MODELS_DIR, WHISPER_MODEL_SIZES, DEFAULT_WHISPER_MODEL,
    AUDIO_SAMPLE_RATE,
)

log = logging.getLogger(__name__).info


class LocalSTT:
    """Wrapper para faster-whisper com modelo configurável e download automático."""

    def __init__(self, model_size=None, language="pt", device="cpu", compute_type="int8"):
        self.model_size = model_size or DEFAULT_WHISPER_MODEL
        self.language = language
        self.device = device
        self.compute_type = compute_type

        self._model = None
        self._loaded = False
        self._lock = threading.Lock()
        self._loading = False

    @property
    def is_loaded(self):
        return self._loaded

    @property
    def is_loading(self):
        return self._loading

    def get_model_path(self) -> str:
        """Retorna o caminho do modelo no disco."""
        return os.path.join(WHISPER_MODELS_DIR, self.model_size)

    def is_model_downloaded(self, model_size=None) -> bool:
        """Verifica se o modelo já foi baixado."""
        size = model_size or self.model_size
        path = os.path.join(WHISPER_MODELS_DIR, size)
        return os.path.isdir(path) and any(
            f.endswith(".bin") for f in os.listdir(path)
        ) if os.path.isdir(path) else False

    def download_model(self, model_size=None, on_progress=None):
        """Baixa o modelo faster-whisper (CTranslate2 format).

        Args:
            model_size: tamanho do modelo (tiny/base/small/medium/large-v3)
            on_progress: callback(downloaded_mb, total_mb)
        """
        size = model_size or self.model_size
        if self.is_model_downloaded(size):
            log(f"Model {size} already downloaded")
            return

        self._loading = True
        try:
            from faster_whisper import WhisperModel
            os.makedirs(WHISPER_MODELS_DIR, exist_ok=True)

            # faster-whisper baixa automaticamente via huggingface_hub
            log(f"Downloading whisper model '{size}'...")
            _model = WhisperModel(
                size,
                device=self.device,
                compute_type=self.compute_type,
                download_root=WHISPER_MODELS_DIR,
            )
            del _model
            log(f"Model '{size}' downloaded successfully")
        finally:
            self._loading = False

    def load_model(self, model_size=None):
        """Carrega o modelo na memória. Baixa se necessário."""
        size = model_size or self.model_size

        if self._loaded and self.model_size == size:
            return

        with self._lock:
            if self._loaded and self.model_size == size:
                return

            try:
                from faster_whisper import WhisperModel

                os.makedirs(WHISPER_MODELS_DIR, exist_ok=True)

                log(f"Loading whisper model '{size}' (device={self.device}, compute={self.compute_type})")
                self._model = WhisperModel(
                    size,
                    device=self.device,
                    compute_type=self.compute_type,
                    download_root=WHISPER_MODELS_DIR,
                )
                self.model_size = size
                self._loaded = True
                log(f"Whisper model '{size}' loaded")
            except Exception as e:
                log(f"Failed to load whisper model: {e}")
                raise

    def unload_model(self):
        """Descarrega o modelo da memória."""
        with self._lock:
            self._model = None
            self._loaded = False
            log("Whisper model unloaded")

    def transcribe(self, audio: np.ndarray, language=None) -> str:
        """Transcreve áudio para texto.

        Args:
            audio: array float32, mono, 16kHz
            language: código do idioma (pt/en/es). None = auto-detect.

        Returns:
            Texto transcrito.
        """
        if not self._loaded:
            self.load_model()

        lang = language or self.language

        segments, info = self._model.transcribe(
            audio,
            language=lang,
            beam_size=5,
            vad_filter=True,
            vad_parameters=dict(
                min_silence_duration_ms=300,
                speech_pad_ms=200,
            ),
        )

        text = " ".join(seg.text.strip() for seg in segments)
        log(f"Transcribed ({info.language}, {info.duration:.1f}s): {text[:80]}...")
        return text.strip()

    def transcribe_wav_bytes(self, wav_bytes: bytes, language=None) -> str:
        """Transcreve bytes WAV para texto.

        Args:
            wav_bytes: bytes de um arquivo WAV
            language: código do idioma

        Returns:
            Texto transcrito.
        """
        with wave.open(io.BytesIO(wav_bytes), "rb") as wf:
            frames = wf.readframes(wf.getnframes())
            audio = np.frombuffer(frames, dtype=np.int16).astype(np.float32) / 32768.0

        return self.transcribe(audio, language)

    @staticmethod
    def list_available_models():
        """Retorna info sobre modelos disponíveis."""
        return dict(WHISPER_MODEL_SIZES)

    def change_model(self, new_size: str):
        """Troca o modelo (descarrega o atual e carrega o novo)."""
        if new_size == self.model_size and self._loaded:
            return
        if new_size not in WHISPER_MODEL_SIZES:
            raise ValueError(f"Invalid model: {new_size}. Options: {list(WHISPER_MODEL_SIZES.keys())}")
        self.unload_model()
        self.load_model(new_size)
