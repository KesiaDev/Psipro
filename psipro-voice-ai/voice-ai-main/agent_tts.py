"""
Agent TTS — Text-to-Speech usando Microsoft Edge TTS (gratuito, alta qualidade).
Gera áudio a partir de texto com streaming e interrupção (barge-in).
"""

import io
import asyncio
import logging
import threading
import tempfile
import os
import wave
import numpy as np
import sounddevice as sd

log = logging.getLogger(__name__).info

# Vozes Edge TTS para PT-BR
EDGE_VOICES = {
    "pt-BR-FranciscaNeural":  "Francisca (Feminina, BR)",
    "pt-BR-AntonioNeural":    "Antonio (Masculino, BR)",
    "pt-BR-ThalitaNeural":    "Thalita (Feminina jovem, BR)",
    "pt-BR-MacerioNeural":    "Macério (Masculino, BR)",
}

DEFAULT_VOICE = "pt-BR-FranciscaNeural"
TTS_SAMPLE_RATE = 24000


class EdgeTTS:
    """Wrapper para Microsoft Edge TTS com streaming e barge-in."""

    def __init__(self, voice=None, rate="+0%", volume="+0%"):
        self.voice = voice or DEFAULT_VOICE
        self.rate = rate
        self.volume = volume
        self.sample_rate = TTS_SAMPLE_RATE

        self._playing = False
        self._stop_event = threading.Event()
        self._play_thread = None
        self._loop = None

    @property
    def is_playing(self):
        return self._playing

    def load_model(self):
        """Noop — Edge TTS não precisa de download de modelo."""
        pass

    def unload_model(self):
        """Noop."""
        pass

    async def _synthesize_async(self, text: str, voice: str = None) -> bytes:
        """Sintetiza texto para MP3 bytes via edge-tts (async)."""
        import edge_tts

        v = voice or self.voice
        communicate = edge_tts.Communicate(text, v, rate=self.rate, volume=self.volume)

        audio_data = b""
        async for chunk in communicate.stream():
            if chunk["type"] == "audio":
                audio_data += chunk["data"]
            if self._stop_event.is_set():
                break

        return audio_data

    def _mp3_to_pcm(self, mp3_bytes: bytes) -> np.ndarray:
        """Converte MP3 bytes para PCM float32 numpy array."""
        try:
            import av

            container = av.open(io.BytesIO(mp3_bytes))
            audio_frames = []

            for frame in container.decode(audio=0):
                arr = frame.to_ndarray()
                # Converter para mono se necessário
                if arr.ndim > 1:
                    arr = arr.mean(axis=0)
                audio_frames.append(arr.astype(np.float32))

            container.close()

            if not audio_frames:
                return np.array([], dtype=np.float32)

            audio = np.concatenate(audio_frames)

            # Normalizar para -1.0 a 1.0
            max_val = np.abs(audio).max()
            if max_val > 0:
                if max_val > 1.0:
                    audio = audio / max_val

            return audio

        except Exception as e:
            log(f"MP3 decode error: {e}")
            return np.array([], dtype=np.float32)

    def synthesize(self, text: str, voice=None) -> np.ndarray:
        """Sintetiza texto para áudio PCM float32 (bloqueante)."""
        loop = asyncio.new_event_loop()
        try:
            mp3_data = loop.run_until_complete(self._synthesize_async(text, voice))
        finally:
            loop.close()

        if not mp3_data:
            return np.array([], dtype=np.float32)

        return self._mp3_to_pcm(mp3_data)

    def speak(self, text: str, voice=None, on_start=None, on_done=None):
        """Sintetiza e reproduz texto (não-bloqueante, em thread separada).

        Permite barge-in via stop().
        """
        self._stop_event.clear()

        def _worker():
            self._playing = True
            try:
                audio = self.synthesize(text, voice)

                if self._stop_event.is_set():
                    if on_done:
                        on_done(interrupted=True)
                    return

                if len(audio) == 0:
                    log("TTS produced empty audio")
                    if on_done:
                        on_done(interrupted=False)
                    return

                if on_start:
                    on_start()

                # Reproduzir
                sd.play(audio, samplerate=self.sample_rate, blocking=False)

                # Esperar reprodução completar ou ser interrompida
                duration = len(audio) / self.sample_rate
                start = 0
                check_interval = 0.05  # 50ms
                elapsed = 0.0
                while elapsed < duration:
                    if self._stop_event.is_set():
                        sd.stop()
                        log("TTS interrupted (barge-in)")
                        if on_done:
                            on_done(interrupted=True)
                        return
                    threading.Event().wait(check_interval)
                    elapsed += check_interval

                sd.stop()  # garantir que parou
                if on_done:
                    on_done(interrupted=False)

            except Exception as e:
                log(f"TTS speak error: {e}")
                if on_done:
                    on_done(interrupted=False)
            finally:
                self._playing = False

        self._play_thread = threading.Thread(target=_worker, daemon=True)
        self._play_thread.start()

    def stop(self):
        """Para reprodução imediatamente (barge-in)."""
        self._stop_event.set()
        try:
            sd.stop()
        except Exception:
            pass
        self._playing = False

    def change_voice(self, new_voice: str):
        """Troca a voz ativa."""
        if new_voice not in EDGE_VOICES:
            raise ValueError(f"Voz inválida: {new_voice}. Opções: {list(EDGE_VOICES.keys())}")
        self.voice = new_voice
        log(f"Voice changed to: {new_voice}")

    @staticmethod
    def list_voices():
        """Retorna vozes disponíveis."""
        return dict(EDGE_VOICES)
