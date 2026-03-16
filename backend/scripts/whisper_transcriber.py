#!/usr/bin/env python3
"""
PsiPro - Transcrição de áudio usando OpenAI Whisper.

Uso:
  python whisper_transcriber.py <caminho_arquivo_audio>

  O arquivo pode ser: .wav, .mp3, .m4a, .webm, etc.
  Saída (JSON no stdout): {"transcript": "texto transcrito"}
"""

import json
import sys
import os


def transcribe(filepath: str) -> str:
    try:
        import whisper
    except ImportError:
        raise RuntimeError(
            "Instale o whisper: pip install openai-whisper"
        )

    if not os.path.isfile(filepath):
        raise FileNotFoundError(f"Arquivo não encontrado: {filepath}")

    model = whisper.load_model("base")  # base | small | medium | large
    result = model.transcribe(filepath, language="pt", fp16=False)
    return result.get("text", "").strip()


def main():
    if len(sys.argv) < 2:
        print(json.dumps({"error": "Uso: whisper_transcriber.py <arquivo>"}))
        sys.exit(1)

    filepath = sys.argv[1]
    try:
        transcript = transcribe(filepath)
        print(json.dumps({"transcript": transcript}))
    except Exception as e:
        print(json.dumps({"error": str(e)}))
        sys.exit(1)


if __name__ == "__main__":
    main()
