"""
Voice AI — Transcritor de áudio global para Windows
Atalho configurável: toggle gravar/transcrever/colar.
Dual hotkey: RegisterHotKey (primário) + WH_KEYBOARD_LL (fallback).
Histórico de transcrições (clique na barra).
Tela de configuração (API key, atalho, idioma).
Desenvolvido por Expert Integrado.
"""

import io
import os
import re
import sys
import json
import base64
import wave
import threading
from typing import Optional
import time
import ctypes
import ctypes.wintypes
import logging
import queue
import webbrowser
import urllib.request
from datetime import datetime
import tkinter as tk
from tkinter import ttk, messagebox

import numpy as np
import sounddevice as sd
import pyperclip
from openai import OpenAI
import httpx
import openai
try:
    import win32crypt
    _DPAPI_AVAILABLE = True
except ImportError:
    _DPAPI_AVAILABLE = False
import pystray
from PIL import Image, ImageDraw

# ─── Guard para pythonw.exe (sem console) ─────────────────────

if sys.stdout is None:
    sys.stdout = open(os.devnull, "w")
if sys.stderr is None:
    sys.stderr = open(os.devnull, "w")

# ─── Diretório base ──────────────────────────────────────────

if getattr(sys, "frozen", False):
    script_dir = os.path.dirname(sys.executable)
else:
    script_dir = os.path.dirname(os.path.abspath(__file__))

# ─── Logging ─────────────────────────────────────────────────

logging.basicConfig(
    filename=os.path.join(script_dir, "whisper.log"),
    level=logging.INFO,
    format="%(asctime)s  %(message)s",
    datefmt="%H:%M:%S",
)
log = logging.info

# ─── Constantes Windows ─────────────────────────────────────

RATE = 16000
CHANNELS = 1

user32 = ctypes.windll.user32
kernel32 = ctypes.windll.kernel32

VK_CONTROL = 0x11
VK_V = 0x56
VK_RETURN = 0x0D
KEYUP_FLAG = 0x0002

# SendInput Unicode constants
INPUT_KEYBOARD = 1
KEYEVENTF_KEYUP_SI = 0x0002
KEYEVENTF_UNICODE = 0x0004


class KEYBDINPUT(ctypes.Structure):
    _fields_ = [
        ("wVk", ctypes.wintypes.WORD),
        ("wScan", ctypes.wintypes.WORD),
        ("dwFlags", ctypes.wintypes.DWORD),
        ("time", ctypes.wintypes.DWORD),
        ("dwExtraInfo", ctypes.c_void_p),
    ]


class _INPUT_UNION(ctypes.Union):
    _fields_ = [
        ("ki", KEYBDINPUT),
        ("_pad", ctypes.c_byte * 32),  # match MOUSEINPUT size on 64-bit
    ]


class INPUT_STRUCT(ctypes.Structure):
    _fields_ = [
        ("type", ctypes.wintypes.DWORD),
        ("u", _INPUT_UNION),
    ]


CTRL_VKS = {0x11, 0xA2, 0xA3}
SHIFT_VKS = {0x10, 0xA0, 0xA1}
ALT_VKS = {0x12, 0xA4, 0xA5}

WH_KEYBOARD_LL = 13
WM_KEYDOWN = 0x0100
WM_KEYUP = 0x0101
WM_SYSKEYDOWN = 0x0104
WM_SYSKEYUP = 0x0105
WM_HOTKEY = 0x0312

MOD_ALT = 0x0001
MOD_CONTROL = 0x0002
MOD_SHIFT = 0x0004
HOTKEY_ID = 1

MUTEX_NAME = "VoiceAIMutex_v1"

# Mapa de teclas para parsing do atalho
VK_MAP = {
    "f1": 0x70, "f2": 0x71, "f3": 0x72, "f4": 0x73,
    "f5": 0x74, "f6": 0x75, "f7": 0x76, "f8": 0x77,
    "f9": 0x78, "f10": 0x79, "f11": 0x7A, "f12": 0x7B,
    "a": 0x41, "b": 0x42, "c": 0x43, "d": 0x44, "e": 0x45,
    "f": 0x46, "g": 0x47, "h": 0x48, "i": 0x49, "j": 0x4A,
    "k": 0x4B, "l": 0x4C, "m": 0x4D, "n": 0x4E, "o": 0x4F,
    "p": 0x50, "q": 0x51, "r": 0x52, "s": 0x53, "t": 0x54,
    "u": 0x55, "v": 0x56, "w": 0x57, "x": 0x58, "y": 0x59, "z": 0x5A,
    "0": 0x30, "1": 0x31, "2": 0x32, "3": 0x33, "4": 0x34,
    "5": 0x35, "6": 0x36, "7": 0x37, "8": 0x38, "9": 0x39,
}

MOD_MAP = {
    "ctrl": MOD_CONTROL,
    "shift": MOD_SHIFT,
    "alt": MOD_ALT,
}

VERSION = "4.1.6"

RECORDING_MODE_OPTIONS = [
    ("Toggle (padrão)", "toggle"),
    ("Push-to-talk (segura = grava)", "ptt"),
]

GITHUB_RELEASES_URL = "https://api.github.com/repos/ericluciano/voice-ai/releases/latest"
GITHUB_RELEASE_PAGE = "https://github.com/ericluciano/voice-ai/releases/latest"

# ─── Sistema de Temas ─────────────────────────────────────

THEMES = {
    "dark": {
        "bg_primary": "#141820",
        "bg_secondary": "#191E26",
        "bg_tertiary": "#1C2D4A",
        "bg_elevated": "#1A1F2A",
        "accent": "#21A353",
        "accent_hover": "#1A8744",
        "text_primary": "#F1F3F5",
        "text_secondary": "#8D9DB5",
        "text_dim": "#6B7A90",
        "success": "#66bb6a",
        "warning": "#ffaa00",
        "error": "#ff4444",
        "info": "#6c8dfa",
        "processing": "#a78bfa",
        "border": "#1C2D4A",
        "waveform": "#5b8af5",
        "rec_dot": "#ff4444",
        "separator": "#21A353",
        "white": "#ffffff",
    },
    "light": {
        "bg_primary": "#F5F6F8",
        "bg_secondary": "#FFFFFF",
        "bg_tertiary": "#E8EBF0",
        "bg_elevated": "#FFFFFF",
        "accent": "#21A353",
        "accent_hover": "#1A8744",
        "text_primary": "#1A1D24",
        "text_secondary": "#5A6577",
        "text_dim": "#8B95A5",
        "success": "#18A550",
        "warning": "#E5960A",
        "error": "#DC3545",
        "info": "#4A6CF7",
        "processing": "#7C5CF7",
        "border": "#D8DCE3",
        "waveform": "#4A6CF7",
        "rec_dot": "#DC3545",
        "separator": "#21A353",
        "white": "#ffffff",
    },
}

_current_theme = "dark"


def _detect_system_theme():
    """Detecta se o Windows está em modo escuro ou claro via registry."""
    try:
        import winreg
        key = winreg.OpenKey(
            winreg.HKEY_CURRENT_USER,
            r"Software\Microsoft\Windows\CurrentVersion\Themes\Personalize"
        )
        value, _ = winreg.QueryValueEx(key, "AppsUseLightTheme")
        winreg.CloseKey(key)
        return "light" if value == 1 else "dark"
    except Exception:
        return "dark"


def get_theme(name=None):
    """Retorna o dict de cores do tema ativo (ou do tema especificado)."""
    if name is None:
        name = _current_theme
    if name == "system":
        name = _detect_system_theme()
    return THEMES.get(name, THEMES["dark"])


def hex_to_rgb(hex_color):
    """Converte #RRGGBB para (r, g, b)."""
    h = hex_color.lstrip("#")
    return int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16)


def rgb_to_hex(r, g, b):
    """Converte (r, g, b) para #RRGGBB."""
    return f"#{r:02x}{g:02x}{b:02x}"


def interpolate_color(color1, color2, ratio):
    """Interpolação linear entre duas cores hex. ratio: 0=color1, 1=color2."""
    r1, g1, b1 = hex_to_rgb(color1)
    r2, g2, b2 = hex_to_rgb(color2)
    r = int(r1 + (r2 - r1) * ratio)
    g = int(g1 + (g2 - g1) * ratio)
    b = int(b1 + (b2 - b1) * ratio)
    return rgb_to_hex(r, g, b)


THEME_OPTIONS = [
    ("Escuro", "dark"),
    ("Claro", "light"),
    ("Sistema", "system"),  # v3.3.3: detecta tema do Windows automaticamente
]


def check_for_update():
    """Consulta GitHub Releases e retorna (nova_versao, url) se houver atualização, ou (None, None)."""
    try:
        req = urllib.request.Request(
            GITHUB_RELEASES_URL,
            headers={"User-Agent": f"VoiceAI/{VERSION}"},
        )
        with urllib.request.urlopen(req, timeout=8) as resp:
            data = json.loads(resp.read().decode())
        tag = data.get("tag_name", "").lstrip("v")
        if not tag:
            return None, None
        # Comparação semântica simples (tupla de ints)
        def to_tuple(v):
            try:
                return tuple(int(x) for x in v.split("."))
            except Exception:
                return (0,)
        if to_tuple(tag) > to_tuple(VERSION):
            url = data.get("html_url", GITHUB_RELEASE_PAGE)
            return tag, url
    except Exception as e:
        log(f"Update check failed: {e}")
    return None, None


LANGUAGE_OPTIONS = [
    ("Português", "pt"),
    ("English", "en"),
    ("Español", "es"),
]

MODEL_OPTIONS = [
    ("whisper-1", "whisper-1"),
    ("gpt-4o-transcribe", "gpt-4o-transcribe"),
]

# ─── Comandos Inline (pós-processamento) ──────────────────

INLINE_COMMANDS_PT = {
    "vírgula": ",", "virgula": ",",
    "ponto final": ".", "ponto": ".",
    "nova linha": "\n", "próxima linha": "\n", "proxima linha": "\n",
    "parágrafo": "\n\n", "paragrafo": "\n\n",
    "dois pontos": ":", "dois-pontos": ":",
    "ponto e vírgula": ";", "ponto e virgula": ";",
    "interrogação": "?", "interrogacao": "?", "ponto de interrogação": "?",
    "exclamação": "!", "exclamacao": "!", "ponto de exclamação": "!",
    "abre parênteses": "(", "abre parenteses": "(",
    "fecha parênteses": ")", "fecha parenteses": ")",
    "aspas": '"', "abre aspas": '"', "fecha aspas": '"',
    "travessão": " — ", "travessao": " — ",
    "reticências": "...", "reticencias": "...",
}

INLINE_COMMANDS_EN = {
    "comma": ",",
    "period": ".", "full stop": ".", "dot": ".",
    "new line": "\n", "next line": "\n",
    "new paragraph": "\n\n", "paragraph": "\n\n",
    "colon": ":",
    "semicolon": ";",
    "question mark": "?",
    "exclamation mark": "!", "exclamation point": "!",
    "open parenthesis": "(", "close parenthesis": ")",
    "open quote": '"', "close quote": '"',
    "ellipsis": "...",
}

INLINE_COMMANDS_ES = {
    "coma": ",",
    "punto final": ".", "punto": ".",
    "nueva línea": "\n", "nueva linea": "\n",
    "párrafo": "\n\n", "parrafo": "\n\n",
    "dos puntos": ":",
    "punto y coma": ";",
    "signo de interrogación": "?", "interrogación": "?", "interrogacion": "?",
    "signo de exclamación": "!", "exclamación": "!", "exclamacion": "!",
    "abre paréntesis": "(", "abre parentesis": "(",
    "cierra paréntesis": ")", "cierra parentesis": ")",
    "comillas": '"',
    "puntos suspensivos": "...",
}

INLINE_COMMANDS_MAP = {
    "pt": INLINE_COMMANDS_PT,
    "en": INLINE_COMMANDS_EN,
    "es": INLINE_COMMANDS_ES,
}


def apply_inline_commands(text, language="pt"):
    """Substitui comandos de voz por pontuação/formatação no texto transcrito."""
    commands = INLINE_COMMANDS_MAP.get(language, INLINE_COMMANDS_PT)
    # Ordena por tamanho descendente para evitar substituições parciais
    sorted_cmds = sorted(commands.keys(), key=len, reverse=True)
    import re
    for cmd in sorted_cmds:
        replacement = commands[cmd]
        # Regex case-insensitive para o comando, com espaços ao redor
        pattern = re.compile(r'\s*\b' + re.escape(cmd) + r'\b\s*', re.IGNORECASE)
        text = pattern.sub(replacement, text)
    return text


# ─── Estilos de texto (pós-processamento GPT) ─────────────

TEXT_STYLE_OPTIONS = [
    ("Nenhum", "none"),
    ("Apenas correção", "correction"),
    ("Formal", "formal"),
    ("Informal", "informal"),
    ("Gíria", "slang"),   # v4.1.0: estilo jovem/coloquial brasileiro
]

GPT_MODEL_OPTIONS = [
    ("gpt-4.1-mini", "gpt-4.1-mini"),
    ("gpt-4.1-nano", "gpt-4.1-nano"),
    ("gpt-4o-mini", "gpt-4o-mini"),
    ("gpt-4o", "gpt-4o"),
]

FAQ_TEXT = """\
VOICE AI — GUIA COMPLETO
\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550

\u25a0 O QUE \u00c9 O VOICE AI
Transcreve sua voz em texto e cola automaticamente onde o cursor estiver.
Funciona em qualquer campo de texto do Windows \u2014 Word, WhatsApp Web,
navegador, e-mail, etc.

\u25a0 COMO USAR
1. Pressione o atalho (padr\u00e3o: Ctrl+Shift+Alt+F3) para iniciar a grava\u00e7\u00e3o
2. Fale o que deseja transcrever
3. Pressione o atalho novamente (modo Toggle) ou solte a tecla (modo PTT)
4. O texto aparece automaticamente onde o cursor estiver

\u25a0 MODOS DE GRAVA\u00c7\u00c3O
\u2022 Toggle: pressione uma vez para iniciar, pressione de novo para parar
\u2022 Push-to-Talk (PTT): segure o atalho enquanto fala, solte para transcrever

\u25a0 CANCELAR
Pressione ESC durante a grava\u00e7\u00e3o ou a transcri\u00e7\u00e3o para cancelar.

\u25a0 PAINEL PRINCIPAL (cone na bandeja do Windows)
\u2022 \u2699  Configura\u00e7\u00f5es \u2014 abre todas as op\u00e7\u00f5es do app
\u2022 \u2139  Ajuda \u2014 este guia
\u2022 \u2715  Fechar \u2014 fecha o painel (o app continua rodando na bandeja)
\u2022 Cards \u2014 hist\u00f3rico das \u00faltimas transcri\u00e7\u00f5es
  - Clique no card para expandir/recolher o texto
  - \U0001f4cb Copia o texto para a \u00e1rea de transfer\u00eancia
  - \u2715  Remove o card do hist\u00f3rico
  - \u25ba  Ouvir o \u00e1udio salvo (se "Salvar \u00e1udio" estiver ativo)

\u25a0 MENU DA BANDEJA (clique direito no \u00edcone)
\u2022 Abrir painel \u2014 abre o hist\u00f3rico
\u2022 Configura\u00e7\u00f5es \u2014 abre as configura\u00e7\u00f5es
\u2022 Aplicar estilo \u2014 reaplica estilo GPT \u00e0 \u00faltima transcri\u00e7\u00e3o
\u2022 \U0001f515 Silenciar notifica\u00e7\u00f5es \u2014 silencia por 24h
\u2022 Sair \u2014 encerra o app

\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550

\u25a0 CONFIGURA\u00c7\u00d5ES \u2014 ABA TRANSCRI\u00c7\u00c3O
\u2022 Atalho: tecla que ativa/para a grava\u00e7\u00e3o. Clique no campo e pressione a combina\u00e7\u00e3o desejada.
\u2022 Modo: Toggle (pressiona 2x) ou PTT (segura a tecla enquanto fala).
\u2022 Idioma: idioma esperado na fala (Portugu\u00eas, Ingl\u00eas, Espanhol).
\u2022 Modelo de transcri\u00e7\u00e3o:
  - whisper-1: modelo padr\u00e3o, r\u00e1pido e econ\u00f4mico (recomendado)
  - gpt-4o-transcribe: maior precis\u00e3o, especialmente para sotaques
\u2022 Auto-colar: cola o texto automaticamente ap\u00f3s transcrever. Se desligado, s\u00f3 copia para a \u00e1rea de transfer\u00eancia.
\u2022 Comandos por voz: permite ditar pontua\u00e7\u00e3o falando "v\u00edrgula", "ponto", "nova linha" etc.
\u2022 Dura\u00e7\u00e3o m\u00e1xima: tempo m\u00e1ximo de uma grava\u00e7\u00e3o em segundos (padr\u00e3o: 300s = 5 min).

\u25a0 CONFIGURA\u00c7\u00d5ES \u2014 ABA REVIS\u00c3O
\u2022 Estilo de texto: aplica p\u00f3s-processamento GPT ap\u00f3s a transcri\u00e7\u00e3o
  - Nenhum: mant\u00e9m o texto exatamente como transcrito
  - Apenas corre\u00e7\u00e3o: corrige gram\u00e1tica e ortografia sem mudar o estilo
  - Formal: reescreve em tom profissional/corporativo
  - Informal: reescreve em tom casual e amig\u00e1vel
\u2022 Modelo GPT: modelo usado para aplicar o estilo
  - gpt-4.1-nano: mais r\u00e1pido e econ\u00f4mico (recomendado)
  - gpt-4.1-mini: equil\u00edbrio entre velocidade e qualidade
  - gpt-4o-mini / gpt-4o: maior qualidade, mais lento
\u2022 Aplicar estilo automaticamente: aplica o estilo em toda transcri\u00e7\u00e3o. Se desligado, use o menu da bandeja para aplicar manualmente.
\u2022 Revisar antes de colar: exibe um popup para editar o texto antes de colar.

\u25a0 CONFIGURA\u00c7\u00d5ES \u2014 ABA NOTIFICA\u00c7\u00d5ES
\u2022 Exibir notifica\u00e7\u00e3o: mostra um bal\u00e3o no canto da tela ap\u00f3s cada transcri\u00e7\u00e3o. Clique no bal\u00e3o para copiar o texto.
\u2022 Notifica\u00e7\u00e3o com som: quando ativado, toca dois bipes curtos ao finalizar cada transcri\u00e7\u00e3o \u2014 um som mais agudo no in\u00edcio da grava\u00e7\u00e3o (880Hz) e um mais grave ao parar (660Hz). \u00datil para saber que o texto foi colado sem precisar olhar para a tela.
\u2022 Salvar \u00e1udio: guarda o arquivo WAV de cada grava\u00e7\u00e3o em AppData\\VoiceAI\\audio\\

\u25a0 CONFIGURA\u00c7\u00d5ES \u2014 ABA DADOS
\u2022 Exibe estat\u00edsticas de uso: total de transcri\u00e7\u00f5es, tempo gravado, palavras, custo estimado.
\u2022 Exportar CSV: salva o hist\u00f3rico de uso em um arquivo CSV.

\u25a0 CONFIGURA\u00c7\u00d5ES \u2014 ABA SISTEMA
\u2022 API Key: chave da OpenAI (come\u00e7a com sk-). Obtenha em platform.openai.com
\u2022 Tema: Escuro, Claro ou Sistema (detecta automaticamente o tema do Windows)
\u2022 Microfone: selecione o microfone a usar. "Padr\u00e3o do sistema" usa o microfone padr\u00e3o do Windows.
\u2022 Iniciar com o Windows: inicia o Voice AI automaticamente ao ligar o computador.
\u2022 Vers\u00e3o: vers\u00e3o instalada do app.

\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550

\u25a0 CUSTOS (estimativa)
Cada transcri\u00e7\u00e3o consome tokens da sua API key OpenAI.
\u2022 whisper-1: ~$0.006 por minuto de \u00e1udio
\u2022 gpt-4o-transcribe: ~$0.006\u2013$0.012 por minuto
\u2022 Estilos GPT: ~$0.001\u2013$0.01 por transcri\u00e7\u00e3o (depende do modelo e tamanho)

\u25a0 DESINSTALAR
Acesse Configura\u00e7\u00f5es do Windows \u2192 Apps \u2192 Voice AI \u2192 Desinstalar.
Seus dados (hist\u00f3rico, configura\u00e7\u00f5es, \u00e1udios) s\u00e3o preservados em:
C:\\Users\\[seu usu\u00e1rio]\\AppData\\Local\\VoiceAI\\
"""


# ─── Emoji por voz (v3.4.0) ──────────────────────────────────

EMOJI_CATEGORIES = [
    ("Reações", ["😂", "🥺", "😍", "😅", "🤣", "😭", "🙏", "😊", "😎", "🤔",
                 "😤", "😡", "🥰", "😘", "😏", "😒", "🤯", "😱", "🙄", "😴"]),
    ("Gestos",  ["👍", "👎", "👏", "🙌", "🤝", "🤜", "💪", "✌️", "🤞", "👌",
                 "🤌", "🫰", "🫶", "❤️", "🔥"]),
    ("Símbolos",["✅", "❌", "⚠️", "💯", "🎯", "🚀", "💡", "📌", "🔑", "⭐",
                 "🏆", "💰", "📣", "🔔", "🎉"]),
    ("Objetos", ["📱", "💻", "📋", "📄", "🔗", "📧", "📞", "🗓️", "⏰", "🔍"]),
    ("Natureza",["🌟", "☀️", "🌙", "🌈", "🌊", "🌸", "🍀"]),
]


def apply_emoji_mask(text, emoji_map):
    """Substitui keywords por placeholders [[EMOJI_N]] antes do GPT."""
    placeholders = []
    # Palavras-chave mais longas têm prioridade
    sorted_map = sorted(emoji_map, key=lambda x: len(x.get("keyword", "")), reverse=True)
    for entry in sorted_map:
        kw = entry.get("keyword", "").strip()
        emoji = entry.get("emoji", "")
        if not kw or not emoji:
            continue
        pattern = r'' + re.escape(kw) + r''
        if re.search(pattern, text, re.IGNORECASE):
            token = f"[[EMOJI_{len(placeholders)}]]"
            text = re.sub(pattern, token, text, flags=re.IGNORECASE)
            placeholders.append((token, emoji))
    return text, placeholders


def apply_emoji_restore(text, placeholders):
    """Restaura emojis a partir dos placeholders após o GPT."""
    for token, emoji in placeholders:
        text = text.replace(token, emoji)
    return text


def _load_emoji_map():
    """Carrega emoji_map.json do AppData. Retorna lista vazia se não existir."""
    try:
        path = os.path.join(os.environ.get("LOCALAPPDATA", ""), "VoiceAI", "emoji_map.json")
        if os.path.exists(path):
            with open(path, "r", encoding="utf-8") as f:
                return json.load(f)
    except Exception as e:
        log(f"emoji_map load error: {e}")
    return []


def _save_emoji_map(emoji_map):
    """Salva emoji_map.json no AppData."""
    try:
        path = os.path.join(os.environ.get("LOCALAPPDATA", ""), "VoiceAI", "emoji_map.json")
        os.makedirs(os.path.dirname(path), exist_ok=True)
        with open(path, "w", encoding="utf-8") as f:
            json.dump(emoji_map, f, ensure_ascii=False, indent=2)
    except Exception as e:
        log(f"emoji_map save error: {e}")


STYLE_PROMPTS = {
    "correction": (
        "Corrija APENAS erros gramaticais, ortográficos e de pontuação no texto abaixo. "
        "NÃO mude o estilo, tom ou vocabulário. Mantenha exatamente as mesmas palavras quando possível. "
        "Responda SOMENTE com o texto corrigido, sem explicações."
    ),
    "formal": (
        "Reescreva o texto abaixo em tom formal e profissional. "
        "Corrija gramática e pontuação. Use vocabulário adequado para ambiente corporativo. "
        "Mantenha o significado original. Responda SOMENTE com o texto reescrito, sem explicações."
    ),
    "informal": (
        "Reescreva o texto abaixo em tom casual e amigável. "
        "Corrija gramática básica mas mantenha um estilo natural e descontraído. "
        "Mantenha o significado original. Responda SOMENTE com o texto reescrito, sem explicações."
    ),
    "slang": (                                                          # v4.1.0
        "Reescreva o texto abaixo usando gírias e expressões coloquiais do português brasileiro moderno. "
        "Use linguagem jovem e descontraída: palavras como 'top', 'maneiro', 'mano', 'tá ligado', "
        "'caramba', 'parça', 'barato', abreviações comuns (vc, pq, tbm, kk). "
        "Mantenha o significado original mas deixe soar como conversa de WhatsApp entre amigos. "
        "Responda SOMENTE com o texto reescrito, sem explicações."
    ),
}


def apply_text_style(text, style, api_key, gpt_model="gpt-4.1-mini", client=None):
    """Aplica estilo de texto usando GPT. Retorna texto processado ou original em caso de erro."""
    if style == "none" or style not in STYLE_PROMPTS:
        return text
    try:
        if client is None:
            client = OpenAI(
                api_key=api_key,
                timeout=httpx.Timeout(30.0, connect=10.0),
            )
        resp = client.chat.completions.create(
            model=gpt_model,
            messages=[
                {"role": "system", "content": STYLE_PROMPTS[style]},
                {"role": "user", "content": text},
            ],
            temperature=0.3,
            max_tokens=len(text) * 3,
        )
        result = resp.choices[0].message.content.strip()
        return result if result else text
    except Exception as e:
        log(f"Text style error ({style}): {e}")
        return text


def _no_scroll(combo):
    """Bloqueia scroll do mouse em ttk.Combobox enquanto não tem foco.
    Evita que a roda do mouse mude o valor sem o usuário ter clicado no campo.
    """
    def _block(event):
        if combo.focus_get() != combo:
            return "break"
    combo.bind("<MouseWheel>", _block)


def _make_beep_wav(freq, duration_ms, volume=0.22, fade_ms=12):
    """Gera WAV suave em memória (sine wave) — sem dependências externas."""
    import wave, io, array, math
    rate = 22050
    n = int(rate * duration_ms / 1000)
    fade = max(1, int(rate * fade_ms / 1000))
    samples = []
    for i in range(n):
        v = math.sin(2 * math.pi * freq * i / rate) * volume
        if i < fade:
            v *= i / fade
        elif i > n - fade:
            v *= (n - i) / fade
        samples.append(int(v * 32767))
    buf = io.BytesIO()
    with wave.open(buf, "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(rate)
        w.writeframes(array.array("h", samples).tobytes())
    return buf.getvalue()


def _make_chime_wav(freqs, durations_ms, gap_ms=18, volume=0.18, fade_ms=10):
    """v4.1.1: Gera WAV com sequência de tons (chime). freqs e durations_ms devem ter o mesmo comprimento."""
    import wave, io, array, math
    rate = 22050
    all_samples = []
    gap_n = int(rate * gap_ms / 1000)
    for freq, dur_ms in zip(freqs, durations_ms):
        n = int(rate * dur_ms / 1000)
        fade = max(1, int(rate * fade_ms / 1000))
        for i in range(n):
            v = math.sin(2 * math.pi * freq * i / rate) * volume
            if i < fade:
                v *= i / fade
            elif i > n - fade:
                v *= (n - i) / fade
            all_samples.append(int(v * 32767))
        all_samples.extend([0] * gap_n)  # silêncio entre tons
    buf = io.BytesIO()
    with wave.open(buf, "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(rate)
        w.writeframes(array.array("h", all_samples).tobytes())
    return buf.getvalue()


def get_input_devices():
    """Retorna lista de dispositivos de entrada de áudio [(nome, index), ...]."""
    devices = []
    try:
        all_devices = sd.query_devices()
        for i, d in enumerate(all_devices):
            if d.get("max_input_channels", 0) > 0:
                name = d.get("name", f"Device {i}")
                devices.append((name, i))
    except Exception as e:
        log(f"get_input_devices error: {e}")
    return devices


# ─── Ícone embutido (base64) ─────────────────────────────
ICON_B64 = "iVBORw0KGgoAAAANSUhEUgAAAIAAAACACAYAAADDPmHLAABE0klEQVR42s29abBkx3Ue+J2891bVW3tDA92NfQcIEARIAKIgEJBJWxApSqLCJkGPPaJjZJsaWRw7whrbER5JcHibzQ6GraAjxp6QRx55KGtoQuRIogVRIEGJIEGQ4AIIIBYCjQYaS6Mb3f22qro388yP3E7mzVuv8UMT09Ij3ntVr+rWzcyzfOc736G1A29nAADb/xApgAj2HwPs/ksEgEBKucfcc9g/xmD3Gv61QAQiso+z+DPDYLB4Twqv71+PlAIz+5e2j/v39t8zxy8lr9s9J7xu6R/b9yPqXXN8Cqe/l58vf17v5bn/2vnvd73G7PXz95WP+3uTP0det/znfl/HB8nefzAI8gXc3xuTbQCOC+oWOCyWInctYgP5vzLcu9nxmtm+Hhswu/diky5M2FDZgrEBoOKN8BuC/EUyYOSNyG5+uHj5ocUNzG+yv+jS4uy2oKXFzT9T/rMxi/9WLuzQ5ik8tw4n1S06I7tJnJ5UNgakKvu9txD5Dgsbwv49M7t1UMGaUHKhHDZMsAZGi9NOQOme+psUNqX7V1X9xQcBdXZKpHXp3TDqn1xIa7jLohOdm6VIroeGn3MuFqZ08kufUVxzTaSShSekF8LBVLrv/Ski5U46x4UDAcSAMe49KTO44j0gTi4p/xZxwYji6yoV1jBcvFx06R4S0+q/V8Eq2c9F8efg5tw/w3HD+M/LEBtQ9U/tuSyyfI60JOF9TX9jyZOfb45FGy95bUL6AZFYrzrcV8QTwUaHjRB+708zDNgYQDFgVLQc3l+7zUEk3AastWD/IQn9kwhrFViYW/8zGWNPtbxJflWUX1zKNohbZGb3vUofL/l1hv1c8n5V7jHvusxATOBjkuIpzNzXubiD5LMuOPlFNzZgjQr/arAB+xcwJm4OqPQEe1/sgjdmZZeV+nFV7grCPuzdHJP4WwZAisBGJX8HY9IgT6l4iuWHV5RaBCWeI2OCcEPEDWO21+NjmBD3+AX2i29SKygDxdJi5gtLqVvtPZa/xlBcUIobpJtiTjcElV1a7X+wPp0A1gBV4UnsF0maS+p/UMr9sIv0gwUBgZILMGGDMLz1kNkCpW6k64CmSU+4XHglgjp52n08oIQrKPp8Ec/4+2aMO/XGR8LWBUirajhudBkcylM8ZK7l84ei/N1+LrmQ9A8WuojaPyluJCXiPyOifbE44Z5plxlQDPR83MD5+1M89ckm4uRxuxkoPe0l/+mvqVLxv/mJBwF1FTYBB6ugUqvFBuR9v782bdINYAzACui0+Dth+uUp1bp/oqU7YC5kMbsEdYvSwHN9vBCj1MlGYQz7HWFKmA3In2CwO8zOlSQRPrsg0Z9kDgFfTA9l0skOh3Dm1e/qZMHYLqg08ZWKG6Wq4iLX9nlcVSIzsI9xYo6dRWLh5yv7M2ltF1Rr93txHf58GMRYQ5v+pigFe4s2gHDJCVbgN+NuMcVbyCbqkNawcYFcliqxNP0c0ruYPUCkeiJuoHj8SWABFuAxCY5AqrKbKmQG7n/8osnT7815JRa7UnbDVJX9vq7BlQobhasKqGugcr/3mQHFkxyyIG2su9Ea1HX2+aayG6HTcXN5q2CM+4Aa0CJ+gNgUppAdLAoa/XPlRtot31+ID3AvEQhpIDMDpnMnV5UvJJxYZ6LDe/RBpATdS9yQtxYm2WA+dkgQSHlzKPPtfgGUcovtrIFbZK4roKqBugJXNdDU4GYUFp3yAJVi1M1E4JFzU10HtC3Qtfb0dx2gdNwIRtvTrrVdfFQiQJTRPA+naINBHMqbwh+AIRcxhEMwhtPAFAiiGEGGQMCI9EmkejDCr8vUuRTVynTIf6/6iFx+2nPo15/e2p30phYLbxcddQNuavB4DNQNiK3V0ctLmB08gO7QecD+PcBkEjdnp4EzG6ATb2L82htozpwFgWBGI8Bo0GwOtHNQ24K1BqoO1BKATiy2u0+d3wSZzy8FgrlrkJ9XLpS3OLuhkYtcQOkaPBIYMYAskhQnH3KDiCfYHN+4TeQDfBNjgRA6UHAXIKSgU+7niPobwPv52pt6d/LrGjxqgKYBNw14NAZGjY3WRyNsXnohzI3XYPmqS3D1xYdw+Z5VXDZucFApjEGYg3GWGS91Gs/vzPCD10/h5AvHgcefxvKfPov69BnwZAIejcDzOaidg+ZzsMMp7HVrQIt6hkYEkAz3/XepRjCE45csgfz7cwGeFvyjtX03snPS8TQaI1I4lWSBMVCjxHfmdRYiAlVVumncqfeOhPICDwpFHxnYNVVcfOfr0dR2cSZLMMvLQFVBkcLWVZfC3HUbbr7levzUvjX8WKVwPTP2yYA2u+gpCC8ohYcB/Odpiy8+dww7X/kmVr75ONTmFowC1M4UNJ2CZlOg06C2ta7CB4lGWyugNaA5XaTSKZYLmQe7+YkNsQH1YPriwufxA2VpITNobf+NnJhnuNzf+2hSMeATv/dvFM69yw7yk00exs1uOskT7lE+fxNk6ldVMeCrlfXvjfPxTQMejYCmgVlZBdUN2kPnY/rj78HNt92AX9y3ho8SsCJuhHbOSwLPfgNUHq522dBDIPxaa3D/8y+je+CrWH30e9YKzGduE8ysa+i0ixc6+zm0sW6l0xFBHEL7/P0sWQe5AfKiUw4q5T/ncULiYmNQSGv7bmD5JixxcBecBfMdQCYjijvOKoRIXvzexRZ+sTl8YICqOv0gPjiUG8Cbfh/s1S6a92Z/NHLmeQwFwuatN2H5L38Q/+DSQ/gEGGvuqlsBd1O4rvz8u2oFs4N72GV8Cn9EhL+/M8OjDz6Ctf/7C+CtLYCNtQZuE6DToPkcaDtnDQzQuvSRuRyIDZn0HDwqlZOVKruGRShjIeisxkvn38cyuBM+OucEECmQIhGxcxoNkOrtNnKBpN8IpCj1/aWCjrQMlfD7tQ364uIvwSwtQakKG++7A1d+7EP49KH9+KtgjAG0btmVW3rlLVrBBYQw2CPKpGAcMHUlDO6tFF674mI8csFB1M+/DJrNwU1jzai7RwSBGspzxIX7WTqlKBS7hvL83YpP51g0qsZL599nF07k71kqFwpDopBD4tOFv1fK/RyhZFLkcBYD8nBsXrUrmTWf8oXFd77fmXxeWoJZWoaqG5z96b+A99z7fnx2bQnvIGDurrDqBb2Ecl1ZBrr+07Hb1IQOwDIBP03A6oXn479cfjHo2WNQG1uAIpBhC4wxW0RRnm5GyonI6weLAKHdFrS3gejceQLuedV4+YL7IMCbWNalCPiINyNk7B+QNee90iOHCl/gBRhj3cGiXe03gU/1fNBX1y7Sr8HjCXhlFUpVOHvPXbj7I/fgs5Mah8me+lqgi/7zSHTSu7UUpcywC8SNqohgABgC7gTj0MH9+NxFh1E/8ax1AS7CJ21svUPCxz7wymOBQewe5UyhlC0NcgCozDrKg0YiVOOlg/eFwo9kA3FE/UKdX9Tqg4mXJzq5obGc7PH9APp4F6AoxQEqAeqowuKPRsB4DF6agEhh651vx3U/+9P43PII55P19bUP7sIl5iXS1ArY+8JxoyZlTRIb2QWRRLidDcwFB/DA6grG33nKnXxjy9YsNgGQlpAZiwtEJd+/G4lkCEvYjd7m/qtkOZZCmibSBua0ZpNsDhXq9uGGceb0KOX8UY+sULgJSn6JALCpwZMxUNXoLjiI5Q/fg3+3vpSc/HI5gwp8QOH9pQsEudPfc95QpKBA6IjwP5gOP3nHzdi463aozthU1MUosTAlStAq40GcC3ew5B5KsYLcOJI6lmcEhbhBhVRNvpA0/+CIBjKLG8YRFhY3lShDFDkLuEidG4jhYwDnBlgAPTSeYPuD78UvXXEh7gDbxSc4bgIPLHofrCAi8YXkpPfvFQV3ABBqUvgXtcLBn7gbs8sustfcNPZaq1oUpWQxawEIVKr35ylxKfhbVDk9B3BIyVMQUjYqLJgHhXzgpyoHHprsQBUoSPmC+M2U1QVSKhcA5Yo5IgAkBravvwo33nkL/g58yib5qSpDLoFeHVtwEP1XahFIxAV5kEiolUJHhKuNxicOn4fpj90J8hC0qmLaSq5IVTL5Q4zgEjl0qI4gwaW8XJ7/vuRmbLrrc39B/R6MJqX/t3h++rdZREoZfBwoXwMZgEe5nGuxFqAKp4qrGhiP0d19G/7O+grWwdCgXkJX9vsIeX7fOlDBNZQei9ZFATBE+Hk2uOK2G7Fz+SUgJgtDK2W5B5US5p8Wl3FLTB9j+ocjLzVLs08FaH0oVXSPqQDecM/Ru+qeKZgd5RbS2BPn6VT+exdl+wAwxAVGp5to6ARU4sP6QLBpQCBsX3QI1914NT4MBisVnpoxPJwb67sCIuqd6GgRHJUdvEuRzboPQwoHAXxs/zraW2+wpe5mZMvPAenMmEvJInK5AJT/PFTsOZc6QJ5uDrkA9qmdQAMTli44IY3KErNcZJKVRZLkmczUyuJIYvKQ8v2IwC4GIGPQXXcF/uLeVayzQcdpnNqP88i9ROmEc/J7ZpnJcCFgFL0OcDm/Czo/rAh733YV5utroUAVNkBe1yC5SNSvEyzaBFxIJXsLbsop5gAmoCx4pcMFsOnARmdcQL9XyP0/CQDIR/lK/B3HDdWrf6eWpIyTxxvGimwsUFfQy8uYXHsF7lGUVJ/jSS8AvGFvZrWIno/nYuSfWw+7kd0Gcp/zGmNwy5GDmF50yHJm6iYW0RIyaoGmnS+ixBFK90bCvpLlJHmNQ65AHjz3eiqwfMEL8tIUAfTPtZaABXDkX25BmkOFdCzJm4XvFVEwGcb00EFccvmFuMFlH7QQ3ZOn2iTU85TtnFsQcutiMnCLU+q7pw4CqNjgjvUV8DWX23tQiT4EygisCVbGw4WeoZTuXCFgYHcCqjFQaZzDaRCYpIUqBnHJTU7NuO8aKl2gzzR2gycTPoDznaQNuiMX4J0H9mI/M7pgAbgY9QdTHYo8mUWArV5aYisnvp9cXwP3mitYuILUkrxbKVSXXwQ9ahzBFOUMYFHr1lAKV4oB8t7IoZx/F2JpLUmczGxRLFIW+BNQJiP2BRBUMPE5jYzT4CDd4WrBzUjatTijb5MtuZ63F+8aj9CZFi0R6sDpN8E1eXayKpjvPAWMZr0U8ROI7KIbkAPzclq0/a4DcD2A8/bvwelRg8mmPRQsrQ15NLXAEF4E9iwK4kqPl7qVFrCHa8qCEX9yUog3LxBxvwlBNlgkkSrSjp/8wwbcQdLHEOFTx/FnFzFfwwa1JTOmmEOWxRi3ValI/kitErPJ9mI0vdp20AYycB5I+s90JQP7mgonqYDzk7KsobwEXFqood/tljKWIOQS5zD7XR1yYzYpMRTUB3WYBbcj5RHGBlBK0ULWdiFUtZjzrkSgVCvhPy2YYqAwXl/F/aRwrDOYVRW08JHSCdxIwI+5gNPkPYdIzbqsA7CAxH3WUoPwDDMeBOEsc8KLgAIqUpgwsEXA5tIEdV07RpUA1SjnPhZizSG0b6grOmcIyUaWUk2hZHEB1OxyeYboo+uRQ+NJiX3/KnT1xGobh0CPjbb1f6ocRxCho5hKNQAJRXtjQrG2wOMRmm8/if/jtTfw61UF7NvjmEQusKyr2Ahy2RH8tcuO4FOTBuNgAPgcQR8OlP+KgN/XjI+9cRonnvgBsLFlr6frIl8BAHamwPYOlrd3UG/vWL5g12ZtaJxBFQuIICUrsYggwvTWeQJlSlguNmDiucoS7lQoItK9Q/1AuBB7+jnTF0Cs+gGO7KFC0QfjETBqwJMJzNIyeGXFXprWwNIEvLwCWlm2z3VdP9zUMEtL0KvLmN/1TvzeDVfg/cpVCXOXNhgj+PUymBPwoyc38LUHv4Hlp49Cbe9Y1s+8jU0x89bSwza3YGYzUKWAtoWaToH5DDRvgdk88gY9TUybYfOeR+yLQJ5SHWCIglZwHbU92TpJ9VJWGKfNlJTl1D61I8H0gVD4CG0DyuIN7DbEoqZIQkqjcpQxGo9hRsvAeAzTNDY7aGpQXccNMG5Qbe2ATm/gNVKumaVnb5PAN4WJOYS5O6RwcnsH1fETME0FTEb2VtTOqrUt0FQgGtuP0VTAdOasV8gXYiqoswroImWR0ulf9PxwcAc0G0rNKMxQoeiSB3ZyV3myp4okUUsQNTF7COmRSRtAZAOoUrZp1Ohyj3xuIv3SGPea2vHsZnPQdOa+5mBtLH2LAZrOsXP+fkwuP4J3BD2CHInkAeAnxjIahHVm3LRvHfqyI6i2psC8te9hGGrWgnZmUDsz0Ky14KCBrQFw4bOwRDp5ALpYQAvPU71cN4DZ8Q93wQGy7+v4PkrkwixKv44QKmv+BBBz8nwOBJC0SYQl/5ZcaxgtwKxJIoHCQnQG+PPvRveOa8Fnt0LrF7tyK1cVuK5ADBw4by/+2QUHcIsiaDao3LVLDINDi1ta8pWlAgXCP1+e4LUfvglfP3IQpu1csKUj9cvYljqMRxi1LdSnfxf00nHxmfPiVNoBPUgOHaoeluKFEry+qPdAvF5d3iychrrwUi86kECYM0w7XLjqmSZvVL1VIVntqqoFXI3IFCIAs8suxD959ztwB2tsWTaL3YiIzTgKwBXG4EpFMKJjuVTrz38vi0cVWxrY1YrwwME9ePTgXmxSbHH3B0Pbiho6qvDfn9nAs59/ECuAaxzJWuOIs7R1gflflPIt0ghKegx4Vz2iusdY9YFc8uKxSZTZgAwS2nfoCRC7khFBpZRbwH3yYrLTB3ralYLZ3MKNusOdGFLPiIWkjlmAQTRAFOGB+xitnAYwAXAnm75pl+mxYvyjU2fAbevSMjXg5ylpSt21ty8/3buxfXOLsqgoxGybQ8nnkbkJYu6nMHA+XLx4aAgR2gLElKSQQf0LykbK0pcNRLucc/QY2HG8vM6rt4i6pecmECPDuHuRbZHtI6VuQoptYTGYXEdJ4qJksOWIo2kxB2lNwJeEdc6YWgDgvJVWr6EqYg4dJ93BRjvVC6EURru/uMUPbBpH8nR4v+9jANFWbu+DGebEl3gHoYJoQHWFWlHM/TMySKhSogS2ELCrRRjiW3L5mSKmqwCops7QUERyiwimk5Jwnu/vIut2zkJUQ9yBfi3AgTakAFVFhQ6PLwiOQMS1U+6/Ne0eUPKs2FR+jqQLMNpmHkmwItyNiigk+fqCC7ZWAFRgVKAFpVtGxy5GoEWl3b7v9x3QoUzvfHxdClQ4wsErAJThVGiCeUFs405aCbHLF6vX4zdQIyAaVhMbcC11rAEYgCPCl5R+CTGIk+qhoBgY+jggyL7FNizJEvKsG+KMDaP6lPOYpVl3UI8aPEYVJsSYuXpAEme5G3YpgEtIKrz1hI2sEGUmgiV/9turAmMOwveIsCXhbs8NIKsfNYPC5tLEbUzHYcjhWpO7A9hYIU/pdrMMpR7CIfZvKW4Qr1EnPtdoMBkoVYebEm5Mzy9xdvOqaA16KYxyvYM6KIIkqKPnz1VZYSjEDZb+1bz0Kn71q49Bz1rwZBR7Dh2i6A/V3gN78I/P24uPjxsY5iywFKkrqz4CGNRfrJV53jD++plN/Mlrp2Bmc9fk4dJAj723HaAI9bzDaGcHRpENlHMGkN/o7HyyNilAw2bxyR+KC85FMKL0PCLQ6t7rOOfo2x5ABTYi9Uv08FJ+nMS7SdVpvcDz83L8Pyy8b/j0TFqyP48a8HgE1DXMygp4ecURQyvQeOSaREa2VWw0gmlswygxMN27hsmP34E/vvQQ3uXKtRWpYg0g6XyGFDmzruCjWzP89kPfwtK3nrSWq9OgrgV1GjybAzPbKWxlZYx9bD4HzaagnR1Q28XOYe06h9nEFnJJbdJmWENoN///VoSjhGVRAe8XreBsrBik1AJKe/ljs0ekYXPgC6bmVCx+IHioTASC+40hCc3ZdQ9XCrQ0Bk/G4FENVgSqbLOnYgPVdiA2WHnjTUxfPoFHWWqgcoEePsD7A6ECcIaBR7d2UB19BcTGvge5GEERuFL2OpYmbjPWsb7hxaiSz5Kpmvn2N+4zoAalY4eCuhKdfKgDSfIBEpMtoVzdBV5+gp+z5/s5sx8CbqHsmZhxKjcvKEH9ljo2XvDJPY8lsXLeQhkHCVcVoCp7wlzrmBmPwGigV1eAfWu4xGscLGwE9UfQZGoBwDIRLhg1eH7fOqrjr9tDEbSDNNiLSbUd0LUws9am/84tUVXbU15X8YQrJzfnOxokoyqnfZe4DPniK7V7p1HOGRDpYe3TlsTMB4zfOBKLAhNHtS8ROFJi7mNVzZeD7aJX6QeUqh+SCq682pfoD/SbwRjg1puwcfXlthpXZRoCSllOfjMCDu3H37j4EN5bKZgACHGvEDSEERAYhghjAL+8uoSfvfMWnDx/vy37Gif+INm8LtAaz+aoH/oG+NSbNuapa7thlHGgeycUw9wmYBcTaB7o9t2F9jUkpVdkW1FJJ1Dy3EwB9HFsGapStE/eulwHyIksMzOUqso1f1VQ9KxjGzg3dRCDsHCxgr79JvytP/dDuL6zlDD/YYw8K0S4khk/WdmKnpEsJlH1i2kgB36gZEF5x/SBpsYfHzmABw4fwNzVPwwzFBEqd9MNAK0U/t3pTRz9zlMYnz5jRSyMATW1TadbZ4m6zi2ijpvXL5LmxSihFNPYTStokXyt+Ls6PqaFafSagXVU/pCMoVwbiDJpV7Y+MqGVJSRPqfen0q4XL/WmlG24bBrbdMlAt7mNjxiNu4jLLHDRvWSVPvJUz/P88vtjsu7mGDBqZlwH4DqS2QT1Wc1E+PzWDp4lYFxVNsV3n0FqJZNULCcnNSf7Bw2nJfihBpD8hGudysjl+sqlTiJrATLkKmH4ZLw5jsRO4oIwge8VhJWASVRHZHBUy9RPSr/Ek28X3rWE17WLkBmnwOgEFNxH98jp/VAh+OUCczjvFkqhbyV0hbiHK1Iol80AzKXSqbMA8AUkiaD6BdKC0VN5EkHWM+FlZ3eL/v1zfXEtwbPVIKJYxyqVVARN9fxZVgmNtnk8oSwTLkimnItBSdHmIPJo274waqDmLaBnMMsVzGTiBB+tuicxgFGDmggV2/pilXFCAwXU8wEZmbZh5u+Di/Vcx5QxLMWqlagcSmjBv9oIQF1XIWhlItDItomRMZZN1LYwlQKPx1ZYwhj7GU0rsh+HDywSkJS4fh7w+ZOv1DkVmuqAnqAwTkWKRrFv7tRgzU7vRyUtzyQbOvKKVJL/+76/Our5jic4e9s70a0uo37zLJZeORFTKHLp1mSM2pWAK4fADY4DWvQzeCgD3IWTz0I3MaO/+4i8aWwTKxu3+Iz28CGcvfkGAMDK88dQv/iStWpehh4OTPKaxVTgDZ6rXx8KAgc2VA2jYxuzoHflWD57alUiGec6bWTbBEX5+ZBGypQvqH45ubemAeoamx/7CN7z4XvwQ0R4cN7iG59/EKuf/QNHBycL+IxH6KjCVDG2iVAVWkLKEhAYnJ7BQv6WsuKSGSoCha66+LgBQTc1MBnZz6cZNJ1ifuVlWP35j+K/umA/SCn89ouvof1nv4bRU89YS9C22UAK0WLvZyZxIQgcaiUXWQlKaiHZhqkRIv8qqfFzkIKNyp/RX7oLM9qe0kQRLO3tS94wqH+5TTBqoDqNM3/hbrz/3vfjtyqbe58dL+FnfuJuPPT401h69ihM3YCJMNrcxt9+402sbm1DdxrEzhJljS0cahj2GhShrNkbWtYR6v95o1QQsSZEPnNBNFWPR3h53mLcdjCNM6x1g8kH7sJ/uPQwPmA0wAbvuepifPy/uRd8378EdAc2CqSpDwAt8vsloclSuleywNlz6iD/biM3a/YJiR8PYEpodxbgheMGkNMKCFpApQZF2SvvUjyjKuC2m/CLowbLeo5Nw1gH48OTEf7o6suBHxwLEDH9/kN48UuPwJDz9qQCK9jHHv40y1PBiBQ2GQeGb8WcIBZjZSizFARO5eWNibqAAEZaQ21PbSu7Nti66BDuvOIifIANOneP/ypr/Ovrr8C3L74Qa8+/YMmtnXZawxTVxbORfMVy7m7dQIuEpGQWEFM7HauBFDRX0nROdvj6v3PQb2wLH2gBo1Tpm6sKZjLGaHkJyy5Nqt3Lj4mAA3vBa6uuOdQA0ylGs5lF2Brbgk1tG92z1ASgkh+nXjUzb3yJ158KYfbFHh0iqS0iaIWkPU7iRK0OHsB4aRIPhssmlhXBrK6C65HFA7xSqnEBYDgsarhDmGjx9xIoyhVYhYWo+yVHv7DoK4f3pMejzKlhxxoWxSB5MhO9HFfUsejdCFxX6BI/zGgqBUzG9kYqBTIGbDhgR+GUsuyLj0Fm4CSIQVWR3MpRyYsKvMYFo3Hy4Rmerwh3fYFQU1fAeAyqqpjZOQvVGo7pr2ab5XRCl7kk6zY0aWRRa5mEigc4AnVUAHGXxyaaV5AY9ua08PJxK0JKPlCJEqiSRbguoNu6CcJPOckKrisndCpVlf1ce5awvWfdSsE3VSwSycZTwxm/EPFU9eYF5adKStsj5TMOaRj41+g6jDe2UJ04mUTeSkrjSLdTV2BSFi5u24EJYzw8RKJEBClpA5msASWTm69Tzf7K+UrHgqOqEDAJ8ITj3L8ApsixKnJhpAVQESyx6ZCJ20ziENpFwL7t68fuxO233oB9WqPjdKytbwYnKWDpHlNukUwSo4rcgKOXF9sdRrCHSE4+46iRZu+9AlWE757ZxKlfv99uAmNiC1myxZ2bIrJuzAM9SqVVUCk3u1uX8FCzaYlCLhHDUAxK+ACW/ZNsApblVJW2PIN6Q5/gC0EkZ/jFdJAd/s3KcvmR6PS5l3L0KlbKys43DWarK/hf11dwp9Gi8+b/B/+YAVXhrukcX1lewmpdZ5PD0l4Qi6gqqyjGplAjoXIfYT4vaNFoWGOGew0FHb+OyJ1ONf2TwC8bqwu2rF+idNCTGC8LrQuScxTNrBBSokpZU8mcpXQANXYYhHGmee7OXsdw9KtzJUvyAIWQC6XhIWyJe5pksZjMcbFHowDLyvnJ/q/ZN74SwN56ik5owM0mCuacd58FUNIfLtHJfMDZtkBdS64jlfvWPAUcudBzdsM4ImXk/ZTJsGkx048lSCT5g3nLuCNNkGZQ10GzCBazqmRqBqXULTLRKk6IqFSchpIJYCKSOHsicz4z7BzDR6qdZ/WSIF7hgTJHH+fkdanfvbYIGt5tZMyQhkDX+SCwoOKNuPtsSVgFnCBkCYTYU5BsCpUJQlEByHC+W8rTZqEgk8S0WRzidDydnFmYAFaJpA1FiTdkTSiUvn8UgSoIXErOg5x8C4IOgheIxa6hsaoyFqIFWDRlY2eGhB/y+ztEBpUzDUNjSC+VMJlQMkU6t5u8RY5Czpw2S1jQxdh0MFfIogFegBxIkV0HF05wbzMURJ1zQeiwTESDQHFKCy8phbLgE2T8B1kgJxJcyAHqmRhszUpFkIky5nBpUNRQA0neVSxjgYA1mIScU8cTpAvBWBaMIBIZOGOwMkmx0UwjTwo5hU1QuQ8uI245ytri4773zzeBlpouLbmDevx+7ilHn4uOsFx4LjSURFq5hBVJQtwU5eHzUnNkuovhlRJA64lt07nNBRrSCZYzDguDqZ32cYHuncuvUYzW2TWK9hlElFKb80HRSboCIUBViM+o4DaIspyFh2V1RKlncQBvCoOveBcxiQWC1+SIoFR+nCH6KKUszpAG0ZCUvEQJlSr3Awy1nVWRlq8C8MViWJR3A+HLnfxQDeSglCnFHntk0AQ6HtBt9hNF+oOJE2EKzqlooT2dMk1A9JXvdhF/pMGs4BwSTSr1ionrJCpzJhT1tY2Zd+/vHxLbGkL7Fv2O2dPCkXQEodcU6omiqRpYOl0sxxNK00f7w40Cp7DQtSvVQr1snUm6mDHQ7JkZpQX6vyx7GpmBXVrO0KOYl0a1IJvyOSSCgQUL1x/xNgjwDKteDmMFvlIqPwxRgRjq4wJS6UCIoAJmyuJLXGj5zuFKkd6o7OYoNzbGgkZu8JQxhVtJA9oGMqDlgXvDrvsJKeupuMipsliuKczZKWZfJyhq9Kd+nTir33vqnTTlubx+XvNfJBY5pAAraeHMXQLpBpYs533t6eYg//dGQ1W1aA1jFDXcxIciY+wsXl+OFrdTgULFzxdvDNuOnTQ750GN4BLg01dhoYwyHlVE++JRJhOSzF7bzQYiQenKN0AIBTntkg6tZsiYwSV/XmL/5EMph/gDmftQ6QuagjUwmSgipSogsi+ARE6dd8YmEn0U6+mdhp61mGV/s0QEGrmxbGxvqp7PsVVyFWH6h1x8FgonqqcRnOsFDpn69LRzsqPlqJwWjB1jwSpLLjFAU2O1rhIMURMw71wziQfLkgIO9WcLLVr80mBpKliPXoGIxcwgYerjLle93Rs2RJgxHOMAZhPFo6mERqUBnhd+Iq3RbW3jdCIkTdhLhGY8ih6HGdiZ4Q0uCzxEFW8pBd/HBfK4oS8Qgd7om1SRHGIzRbd3FsCbsznUzM4VhmFgMsZ5YQPY/+1AmE7noO1pn4TMItZi7O7L803hkcdSvJBvIvZBYG/WDxUbRORNBZe7bFgqfvd8MdLF9/MS2hZ8ZhMnJGJIwPkErK0tW56d5wVubOOZTkfYKTHBJhNzTkGbYa5nBnbBJJug//moP3GUGS+B8ObZTTROJg4AsGcVl8sNwMAmgI2tHaidaVmBxXA/GM81APNBUaUhUou0BJTqD8dLg7nUKvSceYCEM9VwYAGql03MdJq6pO3o9qMuqPEXdIQZF66vol1djny+k6fx2PYUmmzfflAx46GCznBxR1oGSuRtVXZ4uDdbQKaWxi3g49pg67VTqOetFY+qK6gDe/C2BCpnvATg5OmzqGcz+ys/Whbct5iK+oOjUNBpLOkClTZBMQ3M26WlDExAj1wdICh+cpI6sYRuXVCX9Vpnb+wsge6suTx1Gk/O2rABOgZWwbh+fQXm4D6Q1uCqwujkaTx56iyOCf5uukBUdAG8QBo2dwlEtKBiqHpBpm/6eHg2B796wgW2Bt1kjP0H9+GGTKX8WWZsnHgT9bxNJ3zkE0cpSy97w6FNecik31BDBJFMN0j1lDOSnNjp6SclSVVUn6JSQJULE8rARNu2KFaE6uRpPHt2C2dJofJhJzNum4yAQ+eBtE3/mp0pXn/5dXyN04au1MfHYddEolqX5PkZjwG5mWdxz1WvjuBdnWE7cGETwMNntlC9dtISVuct2tUVXLZ3HRe5xhpfcXy8M+ATp6z8vdGxCaTH5EFaE8gRQK3Lpr3kKgoCkQIHGJiQJU4xswabLg594qw50Rc+VBUqhZxHoFpO0EwfG53ZwLE3TuOFLIB5jyIsHTkfulKA1lDMMM+/hP88a6NYZTFij2JVOcrYwwkWgD7D9AK7EbwO4ZeZ8OTxE5ic2bD8vnkLPrgPty6PUTOjA6NyV/HYdAa8ftK+r9Ygo23a6K2AMX253EWdwaXWcJkFlAZSiw2n+sCBKRMgizdLpamLDJqGTr8W5s51ytbTGTaeO4Y/Ef38DOAmADddsB/TvetQ8xamqTF+6TX8/itv4NsgVGAxyIH70G8SX6WbQKZ1AQbP2siGhCXT6ILxb2ctuqePQjndAlNXaC4+hPeP6kRu81VS+N6psxi9csICRY5VHEfMc5yVYHjxRBBp4s2AFSnNDs5cg9p1wCMyarisFwSL4G6oHzzlI3Kt04sLO90Gf8SOOdS2wDMv4IEdq8FTOVmXCTN+fO8qzDWXWRRQKTRb29h8/Fn8y3mXnWFVONlGnPp8NnBEAeVMoPJcQRaBbgz+KjC+yIQvvPgqlp45agO/rsPs8EFce+VFeE+iOAB8nRSOHz2O0ck37WbrWjFSb2AhS2hg/pj8nb/n5zJuXuv8rg1QpXtwnjc3nCBecfScgH7lBYYPIfRwug7MBuPnjuLhV0/imLJ9/f56/lJdYe/N12K+f69rrqyw9K0/xX969hgeZCt1WhznAhJzAJEtIPVaw5n7wFIKFxvB7LVVxhmAf7S1g/aPv4VqYzM0sHQ3XYu/eHAf9sHONlIuBvjd2Rz68WegZjMnHd/ZLMBk94azekje4pXHBHlwlw+bkrBxVqNRZWHhErSqnf6PjsOWepJlcaHDzQ6nX0cfZ3QwfdS1gDEYv3kGr/7pc7ifYxzQAbgRwL1HDmJ283VWhw9AtbGJ7kvfwD88s41t38g2MDyQegMk0wFQ/cIaJ5uHM8zD8hHtCJlPGeAr33kay088A1NXoFmL2cEDuOjGq/A3axU4RRUYLxHhC6+exOjJ5+wGauc2EPQHwZt+aS1loLewuINzqxCGBpFsdGyxapcXQ5JqIAsApRLuAWKKmJzXJ+DfLroC6jo7gKFtwcag+sZ38R/ObmHTZQO+DPz36woHb74Wsz1roJ0pTFNj+dmjePgbj+NXDLvMQYzADcCVGpCEpULhiArDpfpiEpoZIwB/woRfffUkJg89ah2NtiJV7Y1X4RcP7MERAnSgmgO/aRgvffspTF4/aV9wPgd0Z++JjANMFjPlJr64CSht1RsAfaI1IFELKA4TjYubgEJUqoyZFDlkz4RBmqtK068dZ959UTsHK8LSc0fxze98H78rZCk1My4H4787uBfzm6+F6uwH1KMGK1/7Lj759Iv4N0xo2D7XsKz8pWXjuOBmYFYFZXMCIRhHjJYNGgDPMPCxzR1s/eHXUZ86Cx41UG2LnSMHccONV+HjFTmL5NTGQPg/T55F/fXvAK1deOo6awE6GScVTuyQFRiUfclod16KrlR2VsrrHshIMdKFuEeMpD7MG2oBLOrvJumEKWYDnQZabYNBZwnUzhR48Ov41MY2OtGMYcD4RKVw+y3XYevKi6FmraVeT2dofu8r+Ns/OI7/DYQmgrlFVTBZ+ePiwMgUUiZxHzrYk/8EAx/anOK5L3wVy08+B7M0hlIKWF4C3/ku/MqBdewl2y7uM5r/i4HHv/0Ulp87aonF8zaeepkBGLN49l9plExxAluB18HloZPVaHLgvkTnv5j7UDkI8Tcp8Nn7BFNSzgiWOoblpHAi8HiM0ZlNPHPZRbj+ksO4ycm1A8AygOuXJvjt8/ejffUUqo1NcF1BbW4Dz76Iz62tQB3cjz+nrPXofNdOJh0TtD7CpLCc/6eSQFI7K9AA+BID975xFs9+7ktYe+xJmFFjwZSqwtb73o1PvPN6/NK4RufYUhWAkwB+7vQmNn7r99C8fgLoWqidHVA7d5ZQzhEqVPAWqYGWBCDzhhQUhKSEW6jGk/Pug+uz7/H6kt6pKCSZY+gSgUsCMOXiAykcGZogKtESVYEUhYZR3tjCY2+/Bh9ZGmHdLYwBcBkBF+5dx2cOHkD1/HGozW0r6bq5jeap5/EHy0v4/uGD+BFF2OMWvxMuqCym3ZeYYEfxZtjeOcXAr4HwN46/gZOfeQDLTzwLPW5ARKgY2PyRd+IDd96CfztpLCvABYsVA/+AgS/84dew+tAjlmOxs21VRNsuBsedQAQZA7h+xudbxAwquQgp0CWyBGsBSshYb+BxWiJNGy6E1mC2QYJeQF6OVJEUSYInz+MRmpNn8GpV4cS1V+BDimDIMoQ0CLcowmR9FV/Ytwejp48CW9uWNcSM8fPH8a3W4HfO24vJ0gRvI8IYLISeInLIYg5WwF7cc/ygSEWEh0jhE/MOn3zqBfDnv4zxi8ehHU+hmXfYuPUG3P6+2/GZpTHWBA21BuFzAP7e949i6T/+P8B0CppOoWazEPjahRcWwHCZVMtiKGcOzi3qEi5a7ZShVTXjA/f1cmNSBQo1ZdG/CA6DnoB2m6MSQkxpo8bQF/m2bhC4aTB64WU8evA8HL7oAtyuCJqU3QTMuEsRtvau4ctLE4yee8kCSZMxuFIYH3sFr7/wKj7fdvjiZILZZIQLKsJ+v6jJl00h/c+V+5qC8AUQfnVril95+kU88ZXHsPKVb1q30zQgAE2ncfaW63HLPXfgM+vLOKzsIAuQ1YQ8xsDPnjyLN3/jdzB6/kXAdHa83Hwe9YPb1mZFUie41NaVDXxePCOgHOylrxM3RTUa77uvlCL1CyZcMP2yFqCSxZYugeScwVItu6KU9q0USBuol17Dgzdeg7v3ruFSBjriUCy6p1LAgb148MgF0Gc2MdqwloDrCqM3z2L05HN44ZkX8bs7c3x6NMK3mgZv1DW2lMKMCK372iDCawQ8C8KfgPCbncEvn93Bv/j+UTz+wNfQfOkbmBw7bvOGqoLSGrw0webdt+FHf/RWfHrfKi4lH3PY628BfKzVePgzD2D1q9+0wfR0BzSf2c3adk5ZxMTTzwOBGu8yVibx/ecwiygDgmhl7UpOJluFnUID1CnqqWyTay3vTQr3fh7RPVBllUDtl4rqoE75mydj8MQNhqwbbP3QLbj8r/8lPLC2hMtg0CIihRURPq0Zf/f0Jo4//F2sPPo4aDqFqayMndL2s0z370V3+CBw+Dw0+9awsrKM1eUJliqFzjC2dqbY2tzG1sY2cOIU8NoprLxyAmpnB5qsKDQZg8owti+/GPq9t+MXrrgQ/2NTYc2hfb5dvmLGJ0D4tS8+gvXf+CzMfAba2YGa7gBTN0iynQOzNqaAnh5W6vopWQRZ5Al9mKYviy9j31JXMTNoZf0qDkMeFlKi00GR6XBpyROUglJOMNn/TVVH7qCXgM2nhTZWJp6Xl2FW16DqBmfvuh03/ZUP4rMrY1wBRiti9RqEHwD4u3ON+//0B6gf/DqWXn4dXFcwk5Fl5mpPPrVEDVPX0E1t0TvDqNoO1WyOaj63FcbaPgZmUNtCzVrolSVsv+sGXHrHO/A/HdiDex2aphEFphQDf48J/8sjj2Pt1z8D3tgAbW/bxZ/NQDM3ddRbAR/4+VggX+CeFUjpacwmHjKBwKbS/BznNsqD7TaOzQLEwhFoQbsU9WYK9psiRB2BxJhZjrN8+puHUv3GIPMCmNEIk2Ov4MWNbTxw1aW4u2lwmBCUQjsA54Hw0Urh8MF9ePbKS/DyvnXMO4NmOkO1M7OXXLu5gkSotEHdthhNZ6jnc1TaKo5xpdzgR4bqWhAIs317MX3HtVj68+/Gf33Ldfj3e1bwI8q+L3tdIwc+fcIAn3z4u1j9jfuBsxtuZsA2aOZO/tzNDegELK6z3F/rssZPgbRDiawP9aWRZDs7F0g/RKDVPdcwZ5Gn1QfmQuGk/3Nv2njWhmxdgbQCjcAOnCVQQjl0VAPjsR0WMR6HucEKhM0fficu+ss/gV8/sI73Eodc3zKprYDzKQY+bRj/fnOKR196Dfz0C1AvHMf49FlUbWf5iN5fGk47cAEYpdAuTzC/8ALg2stx+IoL8VMH9uLnGoXbHEzeOvejGWjAOMvALxjgNx98BGv/8fPg7W2L9e/YWcO0M3WL3wJzMThCVv9kabdU0BEKLbHLkML3Xn8oWFjZeCPJLiQUS+IGEESNXl+gxNRZmPhsGFSyG6toblyET1SJC3TWpKpT+XjvCkYN0FhlUIxGMEtL4KUlKChs3XIDlv/KT+J/vvgC/LyDoFsX0bMYhDgjwpdA+J15h4dOb+D5109h+9RZ4MymnQI+b+Nm9e+5bx20bx3nH9yHm/at4ydXJvgZBVzk7o8Wpd3Kffc9KHx8OsfD/+WrWLv/D8A7NthT29tAa6eHYDpPzX5p8f3p9/eY8wFTcaRdJA1zb3JanFSWj5RHoo4e/nZl/WoOwxKKWjPZ2NOEq8RZG7ZMH1XSL6DqJgpJOwl58qCGUkEyVm4ANA141DiLMIZZWQFVDdpLLsT0Q+/DR2+5Dv9kVONK956t0AuuEQc8nQHhKRCeYMZzWuP1TuNNbTB1uf4aEfYpwsVNjeuVwg0ArhC6RV2GhtZOoeRTIPzjV07ijfu/iPWHvwXDxp767W0b8ftJ421niz+tjilfHvT5GMDzLDjrqErmK0bWU3CpcrJbmFfYZwT3LMfK+lWcLFqyqCbB0Vn4G4LK+vI4qoiG4dNVHBsXMgKKkihuNlGIZn124F2BnxkwasDjMXg8gVldBaoaNGqwcfftOHLPnfilQwfw1wjY5669y4Qj6lJv/kKNYUtp0WLhK1E5ewCEf7ozw5e/8QTGn/8jjI8dt4OiZjOb7oWAz538uROT7ESJV/bp5/l/0haejeOVvZlFqn5GActej4VFoGgBylRqErx3LwzBPSRQllcdZ9+f8JxWrlTYreQkYkgqiXtxJZ8djJ0lqKzmHk/GVlpusgSeTKBIYXrRYczfcyvefvvb8YmDe/FhYuwVlcAubGOpCdzvZix1AnpgyP/7mqrxqbbDp58+ivYPvoq1x54A5jOw1g7kmdnC1nTmFr9zFsAxpXWJHCMmfpQqf14qj9H36Yl7Nn1BzxKtL9sQtLLnapY8c0pIlCn6J+cKs4gP4gQOjk1QYVIIpeNjoYTSOGLQIvnveXroZglgPAI3I3BTR6zAWZetay6HvvNduObGq/EzB9bxwarCO8BYk6KOgT2EXv8iO8w/b/t4jghfYsJ/2priyz94CbNHvovlbz6B+s3TMIQwIcz7e/Jpnjf9odCTVf38VBWtXTxUpUFgQvA0yamXopNUaNPLxSWChA6l3dtsjLcA+URNHlTTIMrpU1RU3yCitLFECEpHbMAHiJmMqVAQs5vAzRRwG4KbUfgvLy2BlyZ209UNti8+An3DVZhcfSmuufgQ3rN/He9tKtwAxsUgLIPFBpY6PwRNwOsgPAfgEcP4w50ZvvnaKbz+3DHgiWew/ORzqM9u2PmPbZuceji5WMzmrtTtTr7H902/0hd7KCimy4Nlf0HXy1rVZBYQ3LXx1liVewu9G7AxQKmTxqSBSLEpc1hYwVfEgohEEIOSAk8U6OQUBJNUKjHvpeVrFS1BbWcE2v+OwCMbJGI0Cm5Gr65geslhmMsuAi48H/suOICLVpZw/tIE/2p9GW+rVFAcU0T4h5s7+P2tKU7N5nj1zbOYvXICOPYqRi+8hNGJU6D53PpPV8yxmP48klr8oned9fltJyhwsq1bh+icZUevaAMjUbHj4rh5DoEeufa3hJaXaTgn5fuwIex61Qnxgcu6+EFC3msJhro697IEOR7W1tYcuOS1hXwMQHWcVsoGCh4lLBQ7Op1OzzJOK2BkXOewFWzmrg1jZmg6xdILL4OOvQqMR9hZX8MTdY3v3XMHjt/6NryNrdhE5YKsL+3M8Nj9f4TxiVOoZnOsbW6B2w5G29fFbAqau7Sua+1pd0Mkg7kPRJeuHOn7DMBbw6qyVsBou77GBHVTb6JTboVKLYHPUwpTXdN6TRa3EVldYzbp1LDeDgsFHepzBYgHJm/3Z96F4ND3FIZexBjBmq6Fqkf2/bSOds4jY36+juEgHRsmfzHb/nqtQU1nb6rWYK1hVpYt33BrC8tQaNsOjUuV5NWvMkOd2cTo7CY0wZI6/El3pVyaz+2Ce0avl4r39DZf3NGmz+uDVCIxgSch+xKs5D7AuoOqalBdg3UXhngmG8IH5SV3Ldv1kNYWmFlsCfYqYaafBVBZFaOsuiViaV6ge1fqeQs6Q6K9XJFdUDKRwMAcdfd0LhrlZwEYp/ePqAbuxKKZx+DRCIYoo4xRSHgNwVqStgXN5i6tmzoo15n8tnVtXS6lC3V9sfgS2SsIN5Mw0UlzauXgJaNhdAdilc5ZDvT7TBcpEWFT6RBvn0UEV+GTOjsOoB7q+kn74xfJq2UYAkn3kE/pMmHsPJtOxAUq6TWwE21NFFUOjSjsxngZO3LNE0s6d0uUcgIN1tX4KZ9cVwCP3EZgETolQkJhUckFdNS2jrzZOgqXdlRuHXobQyFHDzR1iGFOrLUgXrtrUbbxlgwJQq3TO/CETrCwxizaMfq0MQ/rs2hc7fMMrQugtDeQMvo3FYcql1E/KoxeQ3mij9+diYyPFtPKOWLWjAVKFyy6ahEZx/4mGgG6CJoVFbkPAkGTRZmkZYvTlngWPZJmgT5PQvQUHUacybl7/551KXurSCqCWyzXgDL+hhzfQ8gGZPQtt+o10REVYBEutlDLwdHDvIV0M9jGEp2SEhI1Le73GC6ajTsgDcY9QMSERGl4iLToy+vN2FFRDEoOepSFrdJwxuQExgNBSiCiJYVDI9nZsJPcjUlH9Mr5BvJgxakaSWd3KgNoD1qdF3d8oMdZe1S8GaonlMRyFGoyhFKVhAEzjQHjQCOKusRK/i4Tmg54gRw+qRyhVEwqq+zsXg5j6iqn9c+JKHPyCSkfa+dIqlUNNCaoebH2MQql84u9tZElXXFPfGocYPIkODMRg+kBapzMYg733CCLyyiL4YTv50wA1C1D3Wf/RDUw7nEFF5xuGkCjkPfcVanwlJSbddWuMHFTVX1aQhg7pwKphP38wZFDCv3kUT91tG6cfo5CVsdMN4KXpuMaqLUI2ijIu1I7B8h19JjavU4nFNA4g3ZThdSw5cQcgdjAooJsP7uMgKTbMcZOJw+jLTLwzQV2XMjpaUAnp86BHA/tJhoBRMOafLmYM5dchxRg6FxMU2UyxEZ0qxmQcSIPvogkN2oAh+xCo6ltsWg0duNnm4gWjkbg8Qg0aqKCeF8aKoosNXVQL/VwNNd1ZPJWyoJCzqrYcS+wWIB0CVrvzukLcwQiayeol/u0j8QkU/ZyeSZLz8VbSXn/UC308QQ7ir4KYXBdFkrKGg5ZxgZUYKBSqiBSAIj6o2nFTD6YfvtZ0kAihKddHwHqGrw0sfh9Z8BKgytjLQDZeTygyo11sfp9TISKyI1LzTIopVA73p+FpiswaUtmqQCqjAWCOgPqDAAFM1LgWomA023qVhyMMBPA9AAbzqleyiJ1rE2PDk5k+yQ9esjBQsYOJOZC0UeuXQCP3MAPW9qW+X1VSPm4r3+btxslg6Y9OGMKLeYptEy9njbOKO8qVSHxRaJKhXLmxtVXoDtygeUQjMeWV+j5hsmAarfXlyeYaTuRQ0bdpwF011yGM0fOt+8vxRtCnt9GTt/GFta+/yyqN96wdQrZ3KnEBLPsfnJukpkLEvixydZbWSndQ0lanfl/SpteSVDuSLmD4AWyrVIoi/c3aYNksjhmwQhSTppFcwJpvgOSdq106oLrKKpjsClHznq/707WmY99BLf81HvxI6vLGLmbZhJJQpszG3EN9WyOq9mAoYREmsHfXJng1h+/AxOiIP3iT4txk0dIWQuiCPh+Z/DA91+A/uT/jvET34dZWnJopBFZgynr+oN7PZYk+i4lhh/6M0mBWQsGVpoRsbS6Iq5gdw9IVREUcpsAhkEra1eUdbZpwVDFEl9wsDOl0MUCOWlUiZeoAnvI8wXCaW4qe8qXJkDTYPO//Vn8rXs/gH9aE1aS4deFxlBK1cltJdYk08BVEpBRho+VgbD7QfiF54/j9D//FCZPPQOez4GdHVsMmndpe7cRaiUMIUWr0oZcj1eoqpcVsCwYJdBwivVzloYG0o0ogXqehxqOTrigGZgBRYRhpWpf2SvM3ovsYtM7DSQBkbw8WimoeYuz77sLH/zIB/BJxVhhRuu4+W3xy9K3OkcOaQ3bElXmtjr2zye0sFJ19neWfNr6vxev+yGj8a+vvBjdz90LMx73VdR6c3szVVBkaVxpRB/F9rVEcBt9ddGkKuBdoEQJ3fziwAZSqrQBSjLowzp7qTh0aRRLftGUavFwJoXGwm8VBI8ZAP3wzfj4qIFmYBsikh6ShA//Z3/SbFU+tN8cYWhEfI2kjzD5b9QdmxHw06zxjmsuw/bBA1Dzueh77E9MY0RJXcr7+TmtgIbFMlJ4Wsxxlu34wTqYpA2PkoIRUkzAxWL1whSFZH95PjpWRvRULCDJ/JYLxKsSiTTZDPkgKACmabC8vISrjEFFhOWSiabdxBRQlIHvifcu0pMWSNvlAL41HtkYICCEpRSQsgYPJTAXpxguq4YUM4AQ+SNVM5cgT+yjrhJ3E2IBpRLtBiIs2ABDIFC+c5O0MROF7vknlQk15cGigEGV77BHoE+F99+zht9SCldDYRZ4f31/Roun/qSDroqoRU9xKMHQ/dUd3bOGZmkSF2OgFSsGln5B/eeiiNpS2hHkYwGEOok4mErZbylVTg1knpxDACNqBOSg4IX/UlnY4mnIT1VpgE8QbSb0mXgkhk8I7lpShJHFQgP+vQfxy9990ubL43FkE5cEKGRkTALvp1IMA2S52LDqqe/pa1uMiTB57YRtJzOmr+MvJ5eQ5UUY3VpoOef0cf8+RneZqbEY7eYnioPWCzC5MEfJl8yplAUMtqI6U252mUjh0UQzwClUGWiEdO6Qo4dRVaeZwKhxXUMjYDZHpY2Ffpcn4PHEIoK1m0pe1xEellM4sGDAos/hRfUuYIah6mh1gNE5Auh0BzSdwWgDnozs86bziBt4HMHomHr5PRYCsgKrN2wUJZpCTNRjZokYysNDkcWdUPFjddBo24hKVe2AoLfwb+H8JY6LmkzWEN3GPUGmQFpI4wT2ZBC5YbQGOgKoBZoGekwBuuXKmkX2pq+zsnYwVZjSnRgvaZY5tzYmGfUKN1WNyLgimQZBW0y+aezr+H6/roubxQs2Bml8Tj2fB8yMiTI6+YSxjDAiTz/JeMlkncCyzB5G01J6P929f0sbAL05Agu7K7ydyVyBTvkDAjGMN8FY9MZFw2SUfR1Pse50nLxtDKhpbOOnT33EhG6WhFOOkXQ6qUuaf3Zsosxl+I2hrbBVQgWTjCCTy7sNjH0NJ9a4WxCLO5Slzjk3ULKDZQzGhVZ+EpiOJ4HIZ9Vv8fyf2ybxHUKctzNxb06Pn7aT+C3RkEKcNUxWVazZVyrccGqrOLRR+H9yU8d6uEVPUFEkESyCzhCAcsr68dSvABVzWf1bFNRIyNFGRXPlav06gjaFDeNJM5LMKM19mJMsYo3gOowMhdLJ72/NAryVcepe3yufzOEKGJypkXszF7jtnNK0khtSVX2mEOlMQlXIz/j9Xqnd9fZ6wxfFxu5Juhq78F4A2/v9bG4ve5Kr6NSR4pWkHEvXVez8JgiHIkFgVTztnE+WM4WUFmnBLsgA2DX5s9kAJRhWjp8TRQxOctgYRMaZRJYlS956iKmXYCWo1khzcN8hm3fZZFPCepCviSTKosax/D5oUps06Atmm1Ne5WBpmGMMZKJLSBo6E+tAsYNRqLQHnqBPC1W0AsxpAcrHQn/GGwDptFERxUZq04CryfvgHHrGjgAaK29krU2S9rkXrmhg8lZBFT2XmZe6AbmGD2NYzdvRugOUa3TctAXKWG9sLXkavZu94F0DSKieZ/R7h9MlPMoMhSTJ7yQVPv2f8QbggsBBaTBzCppEnoDb1UqF8iop20xBcqya5OdJ7F1n2oTypCfSt5ROOuM80MpUNw0X5yEEWnsWuBndlXv4AkOIU2nbhF+RwuLU24DoiT5wD2mhbAiFsZpOWv9/YAGSI2WSQCTZCNQf6gz2ZA4TGS2ecyBTG0OJSlk44SWyZlhIXcTre8JMJbVOCfYkDF85hyDry2dJcy/xI1jU++WwZxVb7CWzRzaISBEJ0Tga6iqIGkHhuroW5oa34f8FPZjR2Ezzz6QAAAAASUVORK5CYII="


# ─── ctypes types ────────────────────────────────────────────

class KBDLLHOOKSTRUCT(ctypes.Structure):
    _fields_ = [
        ("vkCode", ctypes.wintypes.DWORD),
        ("scanCode", ctypes.wintypes.DWORD),
        ("flags", ctypes.wintypes.DWORD),
        ("time", ctypes.wintypes.DWORD),
        ("dwExtraInfo", ctypes.c_void_p),
    ]

HOOKPROC = ctypes.WINFUNCTYPE(
    ctypes.c_long, ctypes.c_int,
    ctypes.wintypes.WPARAM, ctypes.wintypes.LPARAM,
)


# ─── SingleInstance ──────────────────────────────────────────

class SingleInstance:
    """Garante uma única instância via named mutex do Windows."""

    def __init__(self):
        self.mutex = kernel32.CreateMutexW(None, True, MUTEX_NAME)
        self.already_running = (kernel32.GetLastError() == 183)

    def is_running(self):
        return self.already_running

    def release(self):
        if self.mutex:
            kernel32.ReleaseMutex(self.mutex)
            kernel32.CloseHandle(self.mutex)


# ─── UsageTracker ────────────────────────────────────────

class UsageTracker:
    """Rastreia uso da API Whisper por mês: segundos, requests, custo.
    v1.1: log detalhado em usage_log.jsonl para garantia condicional."""

    PRICE_PER_MIN_USD = 0.006
    USD_TO_BRL = 5.80
    # Estimativa: cada minuto falado ≈ 4 min de digitação economizados
    SAVING_RATIO = 4.0
    # v3.5.0: custo por modelo Whisper (USD/min)
    WHISPER_COST_PER_MIN = {"whisper-1": 0.006, "gpt-4o-transcribe": 0.006}
    # v3.5.1: custo GPT por 1K tokens (USD) — input e output separados
    GPT_COST_PER_1K_INPUT = {
        "gpt-4.1-nano": 0.0001, "gpt-4.1-mini": 0.0004,
        "gpt-4o-mini": 0.00015, "gpt-4o": 0.0025,
        "gpt-4.1": 0.002,
    }
    GPT_COST_PER_1K_OUTPUT = {
        "gpt-4.1-nano": 0.0004, "gpt-4.1-mini": 0.0016,
        "gpt-4o-mini": 0.0006, "gpt-4o": 0.010,
        "gpt-4.1": 0.008,
    }
    WPM_BASELINE = 40  # velocidade de digitação baseline (palavras por minuto)
    _cached_rate = None
    _rate_fetched_at = 0

    @staticmethod
    def get_usd_brl():
        """Busca cotação USD→BRL 1x/dia, fallback para constante."""
        now = time.time()
        if UsageTracker._cached_rate and (now - UsageTracker._rate_fetched_at) < 86400:
            return UsageTracker._cached_rate
        try:
            url = "https://economia.awesomeapi.com.br/json/last/USD-BRL"
            with urllib.request.urlopen(url, timeout=3) as resp:
                data = json.loads(resp.read())
                rate = float(data["USDBRL"]["bid"])
                UsageTracker._cached_rate = rate
                UsageTracker._rate_fetched_at = now
                return rate
        except Exception:
            return UsageTracker._cached_rate or UsageTracker.USD_TO_BRL

    def __init__(self):
        _data_dir = os.path.join(os.environ.get("LOCALAPPDATA", script_dir), "VoiceAI")
        os.makedirs(_data_dir, exist_ok=True)
        self._path = os.path.join(_data_dir, "usage.json")
        self._log_path = os.path.join(_data_dir, "usage_log.jsonl")
        # Migração: se existe em script_dir mas não em LOCALAPPDATA, mover
        for _fname in ("usage.json", "usage_log.jsonl"):
            _old = os.path.join(script_dir, _fname)
            _new = os.path.join(_data_dir, _fname)
            if os.path.exists(_old) and not os.path.exists(_new):
                try:
                    import shutil
                    shutil.move(_old, _new)
                except Exception:
                    pass
        self._data = self._load()
        self.log_event("app_start")

    @staticmethod
    def _month_key():
        return datetime.now().strftime("%Y-%m")

    def _load(self):
        try:
            with open(self._path, "r", encoding="utf-8") as f:
                raw = json.load(f)
            # Migrar formato antigo (flat) para mensal
            if "total_seconds" in raw:
                key = self._month_key()
                migrated = {key: {"seconds": raw["total_seconds"],
                                   "requests": raw["total_requests"]}}
                self._data = migrated
                self._save()
                return migrated
            return raw
        except Exception:
            return {}

    def _save(self):
        try:
            with open(self._path, "w", encoding="utf-8") as f:
                json.dump(self._data, f)
        except Exception:
            pass

    def _month_data(self):
        key = self._month_key()
        if key not in self._data:
            self._data[key] = {"seconds": 0, "requests": 0, "chars": 0}
        elif "chars" not in self._data[key]:
            self._data[key]["chars"] = 0
        return self._data[key]

    def log_event(self, event, **kwargs):
        """Registra evento no log JSONL (para garantia condicional)."""
        try:
            entry = {"ts": datetime.now().strftime("%Y-%m-%dT%H:%M:%S"),
                     "event": event}
            entry.update(kwargs)
            with open(self._log_path, "a", encoding="utf-8") as f:
                f.write(json.dumps(entry, ensure_ascii=False) + "\n")
        except Exception:
            pass

    def add(self, seconds, chars=0, lang="", words=0, cost_whisper_brl=0.0, cost_gpt_brl=0.0):
        """v3.5.0: adiciona cost_whisper_brl e cost_gpt_brl ao log JSONL."""
        m = self._month_data()
        m["seconds"] += seconds
        m["requests"] += 1
        m["chars"] += chars
        # v3.5.0: acumula custo separado no JSON mensal
        m["cost_whisper_brl"] = m.get("cost_whisper_brl", 0.0) + cost_whisper_brl
        m["cost_gpt_brl"] = m.get("cost_gpt_brl", 0.0) + cost_gpt_brl
        self._save()
        self.log_event("transcription", duration_s=round(seconds, 1),
                       chars=chars, lang=lang, words=words,
                       cost_whisper_brl=round(cost_whisper_brl, 5),
                       cost_gpt_brl=round(cost_gpt_brl, 5),
                       cost_brl=round(cost_whisper_brl + cost_gpt_brl, 5))

    def load_log_entries(self):
        """Retorna lista de entradas do log JSONL."""
        entries = []
        try:
            with open(self._log_path, "r", encoding="utf-8") as f:
                for line in f:
                    line = line.strip()
                    if line:
                        try:
                            entries.append(json.loads(line))
                        except Exception:
                            pass
        except Exception:
            pass
        return entries

    def monthly_summary(self):
        """Retorna dict com resumo por mês: {YYYY-MM: {requests, seconds}}."""
        return dict(self._data)

    def export_csv(self):
        """Exporta log para CSV e retorna o caminho do arquivo."""
        import csv
        now = datetime.now()
        filename = f"relatorio_uso_{now.strftime('%m%Y')}.csv"
        path = os.path.join(script_dir, filename)
        entries = self.load_log_entries()
        transcriptions = [e for e in entries if e.get("event") == "transcription"]
        with open(path, "w", newline="", encoding="utf-8-sig") as f:
            writer = csv.writer(f)
            writer.writerow(["Data/Hora", "Duração (s)", "Caracteres", "Idioma"])
            for e in transcriptions:
                writer.writerow([
                    e.get("ts", ""),
                    e.get("duration_s", ""),
                    e.get("chars", ""),
                    e.get("lang", ""),
                ])
        return path

    @property
    def month_label(self):
        now = datetime.now()
        MESES = ["Jan", "Fev", "Mar", "Abr", "Mai", "Jun",
                 "Jul", "Ago", "Set", "Out", "Nov", "Dez"]
        return f"{MESES[now.month - 1]} {now.year}"

    @property
    def total_seconds(self):
        return int(self._month_data()["seconds"])

    @property
    def total_requests(self):
        return int(self._month_data()["requests"])

    @property
    def cost_usd(self):
        return (self.total_seconds / 60) * self.PRICE_PER_MIN_USD

    @property
    def cost_brl(self):
        return self.cost_usd * self.get_usd_brl()

    @property
    def cost_whisper_brl(self):
        """v3.5.0: custo acumulado de transcrição Whisper no mês (BRL)."""
        return float(self._month_data().get("cost_whisper_brl", 0.0))

    @property
    def cost_gpt_brl(self):
        """v3.5.0: custo acumulado de estilos GPT no mês (BRL)."""
        return float(self._month_data().get("cost_gpt_brl", 0.0))

    @property
    def total_words_from_log(self):
        """v3.5.0: total de palavras somado do log JSONL (mais preciso que chars/5.5)."""
        entries = self.load_log_entries()
        return sum(e.get("words", 0) for e in entries if e.get("event") == "transcription")

    @property
    def saved_minutes(self):
        """Estimativa de minutos de digitação economizados no mês."""
        return int((self.total_seconds / 60) * self.SAVING_RATIO)

    @property
    def total_chars(self):
        return int(self._month_data().get("chars", 0))

    @property
    def total_words(self):
        """Estimativa de palavras transcritas (chars ÷ 5.5, média PT/EN)."""
        return int(self.total_chars / 5.5)




# ─── DPAPI helpers (v4.0.5) ──────────────────────────────────

def _dpapi_encrypt(plaintext: str) -> Optional[str]:
    """Criptografa string com DPAPI. Retorna base64 ou None se indisponível."""
    if not _DPAPI_AVAILABLE:
        return None
    try:
        encrypted = win32crypt.CryptProtectData(
            plaintext.encode("utf-8"), None, None, None, None, 0
        )
        return base64.b64encode(encrypted).decode("ascii")
    except Exception as e:
        log(f"DPAPI encrypt error: {e}")
        return None


def _dpapi_decrypt(b64_ciphertext: str) -> Optional[str]:
    """Descriptografa base64 DPAPI. Retorna plaintext ou None em falha."""
    if not _DPAPI_AVAILABLE:
        return None
    try:
        raw = base64.b64decode(b64_ciphertext)
        _, plaintext = win32crypt.CryptUnprotectData(raw, None, None, None, 0)
        return plaintext.decode("utf-8")
    except Exception as e:
        log(f"DPAPI decrypt error: {e}")
        return None


# ─── ConfigManager ───────────────────────────────────────────

class ConfigManager:
    """Lê/escreve config.json. Substitui o .env."""

    CONFIG_FILE = "config.json"
    DEFAULTS = {
        "api_key": "",
        "hotkey": "Ctrl+Shift+Alt+F3",
        "language": "pt",
        "model": "whisper-1",            # v3.3.3: padrão volta para whisper-1
        "auto_paste": True,
        "auto_start": True,
        "notification": True,
        "notification_sound": True,
        "inline_commands": True,
        "max_recording_secs": 300,
        "mic_device": None,
        "text_style": "correction",     # v3.3.2: padrão atualizado
        "text_style_auto": False,       # v3.3.3: desligado por padrão
        "gpt_model": "gpt-4.1-nano",   # v3.3.3: padrão atualizado
        "recording_mode": "toggle",
        "edit_before_paste": False,
        "theme": "dark",
        "silent_until": 0.0,
        "save_audio": False,
        "silence_stop": False,          # v3.4.0: auto-stop por silêncio
        "silence_timeout_secs": 30,     # v3.4.0: segundos de silêncio até parar
        "auto_enter": False,            # v3.4.0: auto-Enter após colar
        "auto_enter_delay_ms": 100,     # v3.4.0: delay entre paste e Enter (ms)
        "double_click_enter": False,    # v3.6.0: duplo-clique na hotkey envia Enter
        "paste_method": "auto",         # v3.7.0: "auto" | "ctrlv" | "charbychar"
        "first_run": True,              # v3.8.0: True = onboarding wizard na inicialização
        "emoji_replace": False,         # v3.4.0: substituição de emojis por voz
        "show_statusbar": True,          # v3.4.0: exibir/ocultar StatusBar
    }

    def __init__(self):
        self.path = os.path.join(script_dir, self.CONFIG_FILE)
        self.data = dict(self.DEFAULTS)
        self._migrate_from_env()
        self.load()

    def _migrate_from_env(self):
        """Se config.json não existe mas .env sim, migra os valores."""
        if os.path.exists(self.path):
            return
        env_path = os.path.join(script_dir, ".env")
        if not os.path.exists(env_path):
            return
        try:
            with open(env_path, "r", encoding="utf-8") as f:
                for line in f:
                    line = line.strip()
                    if "=" in line and not line.startswith("#"):
                        key, val = line.split("=", 1)
                        key = key.strip()
                        val = val.strip()
                        if key == "OPENAI_API_KEY" and val:
                            self.data["api_key"] = val
                        elif key == "LANGUAGE" and val:
                            self.data["language"] = val
                        elif key == "WHISPER_MODEL" and val:
                            self.data["model"] = val
            if self.data["api_key"]:
                self.save()
                log("Migrated config from .env to config.json")
        except Exception as e:
            log(f"Migration from .env failed: {e}")

    def load(self):
        if not os.path.exists(self.path):
            return
        try:
            with open(self.path, "r", encoding="utf-8") as f:
                saved = json.load(f)
            for k in self.DEFAULTS:
                if k in saved:
                    self.data[k] = saved[k]

            # v4.0.5: DPAPI — resolver api_key a partir do armazenamento seguro
            api_key_enc = saved.get("api_key_enc")
            if api_key_enc:
                # Campo criptografado presente — descriptografar
                decrypted = _dpapi_decrypt(api_key_enc)
                if decrypted:
                    self.data["api_key"] = decrypted
                else:
                    # Falha na descriptografia (ex: perfil Windows diferente) — limpa chave
                    self.data["api_key"] = ""
                    log("DPAPI decrypt failed — api_key cleared, re-enter required")
            elif self.data.get("api_key") and _DPAPI_AVAILABLE:
                # Migração automática: api_key em texto puro → criptografar e reescrever
                log("Migrating api_key to DPAPI encrypted storage")
                self.save()

            # v3.3.2: migração de defaults obsoletos para instalações existentes
            if self.data.get("text_style") == "none" and not self.data.get("text_style_auto"):
                self.data["text_style"] = "correction"
                self.data["text_style_auto"] = True
            if self.data.get("language") not in ("pt", "en", "es"):
                self.data["language"] = "pt"
            # v3.3.2: reset silent_until se travado no futuro distante (>24h)
            if self.data.get("silent_until", 0) > time.time() + 86400:
                self.data["silent_until"] = 0.0
                log("silent_until resetado (estava no futuro distante)")
            # Aplica tema salvo
            global _current_theme
            _current_theme = self.data.get("theme", "dark")
            log(f"Config loaded: hotkey={self.data['hotkey']}, lang={self.data['language']}")
        except Exception as e:
            log(f"Config load error: {e}")

    def save(self):
        try:
            to_save = dict(self.data)
            # v4.0.5: DPAPI — gravar api_key criptografada, nunca em texto puro
            api_key = to_save.pop("api_key", "")
            if api_key and _DPAPI_AVAILABLE:
                enc = _dpapi_encrypt(api_key)
                if enc:
                    to_save["api_key_enc"] = enc
                    # api_key não entra no JSON (chave removida acima via pop)
                else:
                    # Fallback: DPAPI falhou, gravar em texto puro para não perder a chave
                    to_save["api_key"] = api_key
            else:
                # DPAPI indisponível (pywin32 não instalado) — gravar em texto puro
                to_save["api_key"] = api_key
            # Garantir que api_key_enc antigo seja removido se chave foi limpa
            if not api_key:
                to_save.pop("api_key_enc", None)
            with open(self.path, "w", encoding="utf-8") as f:
                json.dump(to_save, f, indent=2)
            log("Config saved")
        except Exception as e:
            log(f"Config save error: {e}")

    def needs_setup(self):
        return not self.data.get("api_key")

    def api_key_is_unencrypted(self) -> bool:
        """True se a chave está sendo gravada em texto puro (DPAPI indisponível)."""
        return bool(self.data.get("api_key")) and not _DPAPI_AVAILABLE

    @property
    def api_key(self):
        return self.data["api_key"]

    @property
    def hotkey(self):
        return self.data["hotkey"]

    @property
    def language(self):
        return self.data["language"]

    @property
    def model(self):
        return self.data["model"]

    @property
    def auto_paste(self):
        return self.data.get("auto_paste", True)

    @property
    def auto_start(self):
        return self.data.get("auto_start", True)

    @property
    def notification(self):
        return self.data.get("notification", True)

    @property
    def notification_sound(self):
        return self.data.get("notification_sound", True)

    @property
    def inline_commands(self):
        return self.data.get("inline_commands", True)

    @property
    def max_recording_secs(self):
        return self.data.get("max_recording_secs", 300)

    def is_silenced(self):
        """Retorna True se notificações estão silenciadas (dentro do prazo)."""
        import time as _t
        return _t.time() < float(self.data.get("silent_until", 0.0))

    def silence_for_day(self):
        """Silencia notificações por 24 horas."""
        import time as _t
        self.data["silent_until"] = _t.time() + 86400
        self.save()

    def unsilence(self):
        """Reativa notificações imediatamente."""
        self.data["silent_until"] = 0.0
        self.save()

    @property
    def mic_device(self):
        return self.data.get("mic_device", None)

    @property
    def text_style(self):
        return self.data.get("text_style", "none")

    @property
    def text_style_auto(self):
        return self.data.get("text_style_auto", False)

    @property
    def gpt_model(self):
        return self.data.get("gpt_model", "gpt-4.1-mini")

    @property
    def recording_mode(self):
        return self.data.get("recording_mode", "toggle")

    @property
    def edit_before_paste(self):
        return self.data.get("edit_before_paste", False)

    @property
    def save_audio(self):
        return self.data.get("save_audio", False)

    @property
    def theme(self):
        return self.data.get("theme", "dark")

    @staticmethod
    def parse_hotkey(hotkey_str):
        """Converte 'Ctrl+Shift+Alt+F3' → (mod_flags, vk_code, set_ctrl, set_shift, set_alt)"""
        parts = [p.strip().lower() for p in hotkey_str.split("+")]
        mods = 0
        vk = 0
        has_ctrl = False
        has_shift = False
        has_alt = False
        for p in parts:
            if p in MOD_MAP:
                mods |= MOD_MAP[p]
                if p == "ctrl":
                    has_ctrl = True
                elif p == "shift":
                    has_shift = True
                elif p == "alt":
                    has_alt = True
            elif p in VK_MAP:
                vk = VK_MAP[p]
        return mods, vk, has_ctrl, has_shift, has_alt


# ─── SettingsDialog ──────────────────────────────────────────

class EmojiPickerDialog:
    """Dialog para adicionar um par keyword → emoji à tabela do usuário."""

    def __init__(self, parent, on_confirm):
        t = get_theme()
        bg = t["bg_primary"]
        fg = t["text_primary"]
        accent = t["accent"]
        btn_bg = t["bg_card"]
        btn_fg = t["text_primary"]

        self.on_confirm = on_confirm
        # StringVars associadas ao parent para garantir contexto Tk consistente (evita GC)
        self.selected_emoji = tk.StringVar(master=parent, value="😊")
        self._kw_var = tk.StringVar(master=parent)

        win = tk.Toplevel()  # sem parent → evita z-order conflito com SettingsDialog -topmost
        win.title("Adicionar emoji")
        win.geometry("400x480")
        win.configure(bg=bg)
        win.resizable(False, False)
        win.attributes("-topmost", True)
        win.lift()
        win.after(50, win.focus_force)
        self.win = win

        tk.Label(win, text="Palavra-chave (o que você fala):", font=("Segoe UI", 10),
                 fg=fg, bg=bg).pack(anchor="w", padx=16, pady=(14, 2))
        tk.Entry(win, textvariable=self._kw_var, font=("Segoe UI", 11),
                 bg=t["bg_card"], fg=fg, insertbackground=accent,
                 relief="flat", bd=0, highlightthickness=1,
                 highlightbackground=btn_bg, highlightcolor=accent
                 ).pack(fill="x", padx=16, pady=(0, 10), ipady=5)

        tk.Label(win, text="Selecione o emoji:", font=("Segoe UI", 10),
                 fg=fg, bg=bg).pack(anchor="w", padx=16, pady=(0, 4))

        # Preview do emoji selecionado
        preview_frame = tk.Frame(win, bg=bg)
        preview_frame.pack(fill="x", padx=16, pady=(0, 6))
        tk.Label(preview_frame, text="Selecionado:", font=("Segoe UI", 9),
                 fg=t["text_secondary"], bg=bg).pack(side="left")
        self._preview_lbl = tk.Label(preview_frame, textvariable=self.selected_emoji,
                                      font=("Segoe UI Emoji", 18), fg=fg, bg=bg)
        self._preview_lbl.pack(side="left", padx=(8, 0))

        # Grade de emojis por categoria (canvas scrollável)
        canvas_frame = tk.Frame(win, bg=bg)
        canvas_frame.pack(fill="both", expand=True, padx=16, pady=(0, 6))
        canvas = tk.Canvas(canvas_frame, bg=t["bg_card"], highlightthickness=0)
        sb = tk.Scrollbar(canvas_frame, orient="vertical", command=canvas.yview)
        canvas.configure(yscrollcommand=sb.set)
        sb.pack(side="right", fill="y")
        canvas.pack(side="left", fill="both", expand=True)
        inner = tk.Frame(canvas, bg=t["bg_card"])
        canvas_win = canvas.create_window((0, 0), window=inner, anchor="nw")

        def _on_resize(e):
            canvas.itemconfig(canvas_win, width=e.width)
        canvas.bind("<Configure>", _on_resize)

        def _on_inner_resize(e):
            canvas.configure(scrollregion=canvas.bbox("all"))
        inner.bind("<Configure>", _on_inner_resize)

        def _select(em):
            self.selected_emoji.set(em)

        COLS = 8
        for cat_name, emojis in EMOJI_CATEGORIES:
            tk.Label(inner, text=cat_name, font=("Segoe UI", 8, "bold"),
                     fg=t["text_secondary"], bg=t["bg_card"], anchor="w"
                     ).pack(fill="x", padx=4, pady=(6, 2))
            # v4.1.0: cria um frame por linha (8 emojis) para evitar grid truncado
            for row_start in range(0, len(emojis), COLS):
                row_frame = tk.Frame(inner, bg=t["bg_card"])
                row_frame.pack(fill="x", padx=4)
                for col, em in enumerate(emojis[row_start:row_start + COLS]):
                    lbl = tk.Label(row_frame, text=em, font=("Segoe UI Emoji", 16),
                                   bg=t["bg_card"], fg=fg, cursor="hand2",
                                   padx=3, pady=3)
                    lbl.grid(row=0, column=col, padx=1, pady=1)
                    lbl.bind("<Button-1>", lambda e, emoji=em: _select(emoji))
                    lbl.bind("<Enter>", lambda e, w=lbl: w.config(bg=t["bg_primary"]))
                    lbl.bind("<Leave>", lambda e, w=lbl: w.config(bg=t["bg_card"]))

        # Botões
        btn_frame = tk.Frame(win, bg=bg)
        btn_frame.pack(fill="x", padx=16, pady=(0, 14))
        tk.Button(btn_frame, text="Cancelar", font=("Segoe UI", 9),
                  bg=btn_bg, fg=btn_fg, relief="flat", cursor="hand2",
                  command=win.destroy).pack(side="right", padx=(6, 0))
        tk.Button(btn_frame, text="Adicionar", font=("Segoe UI", 9),
                  bg=accent, fg="#ffffff", relief="flat", cursor="hand2",
                  command=self._confirm).pack(side="right")

    def _confirm(self):
        kw = self._kw_var.get().strip()
        em = self.selected_emoji.get()
        if kw:
            self.on_confirm(kw, em)
        self.win.destroy()


class SettingsDialog:
    """Tela de configuração — API key, atalho, idioma."""

    def __init__(self, config, on_save=None, first_run=False, usage=None):
        self.config = config
        self.on_save = on_save
        self.saved = False
        self.usage = usage

        self.win = tk.Tk() if first_run else tk.Toplevel()
        self.win.title("Voice AI \u2014 Configura\u00e7\u00f5es")
        self.win.configure(bg=get_theme()["bg_primary"])
        self.win.resizable(False, False)
        self.win.attributes("-topmost", True)

        w, h = 560, 560
        sx = (self.win.winfo_screenwidth() - w) // 2
        sy = (self.win.winfo_screenheight() - h) // 2
        self.win.geometry(f"{w}x{h}+{sx}+{sy}")

        self._build_ui(first_run)
        # Garante que a janela (não algum combobox) recebe o foco ao abrir
        self.win.after(50, self.win.focus_set)

        if first_run:
            self.win.protocol("WM_DELETE_WINDOW", self._on_close_first_run)
        else:
            self.win.protocol("WM_DELETE_WINDOW", self.win.destroy)

    def _build_ui(self, first_run):
        t = get_theme()
        bg = t["bg_primary"]
        fg = t["text_primary"]
        label_fg = t["text_secondary"]
        entry_bg = t["bg_secondary"]
        accent = t["accent"]
        accent_hover = t["accent_hover"]
        dim = t["text_secondary"]
        btn_bg = t["bg_tertiary"]
        btn_fg = t["text_secondary"]

        # Estilo do combobox e notebook (dark mode fix)
        style = ttk.Style()
        style.theme_use("clam")
        style.configure("TCombobox",
                         fieldbackground=entry_bg, background=btn_bg,
                         foreground=fg, arrowcolor=accent, borderwidth=0)
        style.map("TCombobox",
                  fieldbackground=[("readonly", entry_bg), ("disabled", bg)],
                  foreground=[("readonly", fg), ("disabled", dim)],
                  selectbackground=[("readonly", entry_bg)],
                  selectforeground=[("readonly", fg)])
        style.configure("TNotebook", background=bg, borderwidth=0, tabmargins=0)
        style.configure("TNotebook.Tab", background=btn_bg, foreground=dim,
                         padding=[14, 6], font=("Segoe UI", 9))
        style.map("TNotebook.Tab",
                  background=[("selected", entry_bg)],
                  foreground=[("selected", fg)])

        # Dropdown listbox (Tk option database)
        self.win.option_add("*TCombobox*Listbox.background", entry_bg)
        self.win.option_add("*TCombobox*Listbox.foreground", fg)
        self.win.option_add("*TCombobox*Listbox.selectBackground", accent)
        self.win.option_add("*TCombobox*Listbox.selectForeground", "#ffffff")

        # Titulo
        title_text = "Bem-vindo ao Voice AI!" if first_run else "Configura\u00e7\u00f5es"
        tk.Label(self.win, text=title_text,
                 font=("Segoe UI Semibold", 14), fg="#ffffff", bg=bg
                 ).pack(pady=(16, 4))

        if first_run:
            tk.Label(self.win, text="Configure sua API key para come\u00e7ar.",
                     font=("Segoe UI", 9), fg=dim, bg=bg
                     ).pack(pady=(0, 8))

        # Rodape: Salvar + status + branding (empacotado ANTES do notebook para ancorar no fundo)
        tk.Label(self.win, text=f"v{VERSION}",
                 font=("Segoe UI", 8), fg=dim, bg=bg
                 ).pack(side="bottom", pady=(0, 2))

        link = tk.Label(self.win, text="Desenvolvido por Expert Integrado",
                        font=("Segoe UI", 8, "italic"), fg=dim, bg=bg,
                        cursor="hand2")
        link.pack(side="bottom", pady=(0, 4))
        link.bind("<Enter>", lambda e: link.config(fg=accent))
        link.bind("<Leave>", lambda e: link.config(fg=dim))
        link.bind("<Button-1>", lambda e: webbrowser.open("https://expertintegrado.com.br"))

        footer = tk.Frame(self.win, bg=bg)
        footer.pack(side="bottom", fill="x", padx=16, pady=(4, 4))

        self._status_var = tk.StringVar()
        self._status_label = tk.Label(footer, textvariable=self._status_var,
                                       font=("Segoe UI", 9), fg=get_theme()["error"], bg=bg)
        self._status_label.pack()

        save_btn = tk.Button(footer, text="Salvar e Iniciar" if first_run else "Salvar",
                              font=("Segoe UI", 11, "bold"), bg=accent, fg="#ffffff",
                              relief="flat", cursor="hand2", padx=30, pady=6,
                              command=self._save)
        save_btn.pack(pady=(0, 2))
        save_btn.bind("<Enter>", lambda e: save_btn.config(bg=accent_hover))
        save_btn.bind("<Leave>", lambda e: save_btn.config(bg=accent))

        # Notebook com abas de largura igual, texto centralizado e negrito (v4.1.0)
        N_TABS = 6
        TAB_W = (560 - 32) // N_TABS  # 560px janela, 16px padding cada lado
        style.configure("TNotebook.Tab", padding=[0, 6], font=("Segoe UI", 9, "bold"),
                        width=TAB_W, anchor="center")
        notebook = ttk.Notebook(self.win)
        notebook.pack(fill="both", expand=True, padx=16, pady=(4, 0))
        notebook.bind("<MouseWheel>", lambda e: "break")  # v3.3.3: bloqueia troca de aba por scroll

        def _tab(label):
            f = tk.Frame(notebook, bg=entry_bg)
            notebook.add(f, text=label)
            inner = tk.Frame(f, bg=entry_bg)
            inner.pack(padx=16, pady=12, fill="both", expand=True)
            return inner

        def _set_tab_widths():
            """Ajusta largura das abas para preencher o espaço disponível após render."""
            total_w = notebook.winfo_width()
            if total_w <= 1:
                notebook.after(50, _set_tab_widths)
                return
            tab_w = max(1, total_w // N_TABS)
            style.configure("TNotebook.Tab", width=tab_w, padding=[0, 6],
                            font=("Segoe UI", 9, "bold"), anchor="center")
        notebook.after(10, _set_tab_widths)

        def _lbl(parent, text):
            tk.Label(parent, text=text, font=("Segoe UI", 9),
                     fg=dim, bg=entry_bg, anchor="w").pack(fill="x", pady=(8, 2))

        def _chk(parent, text, var):
            tk.Checkbutton(parent, text=text, variable=var,
                           font=("Segoe UI", 10), fg=fg, bg=entry_bg,
                           selectcolor=bg, activebackground=entry_bg,
                           activeforeground=fg, anchor="w"
                           ).pack(fill="x", pady=(4, 0))

        def _combo(parent, var, values):
            cb = ttk.Combobox(parent, textvariable=var, values=values,
                              state="readonly", font=("Segoe UI", 10))
            cb.pack(fill="x", pady=(0, 4), ipady=4)
            _no_scroll(cb)
            return cb

        # ABA 1: Transcricao
        tab1 = _tab("Transcri\u00e7\u00e3o")

        _lbl(tab1, "Atalho (clique para alterar):")
        self._hotkey_var = tk.StringVar(value=self.config.data["hotkey"])
        self._hotkey_capturing = False
        self._hotkey_label = tk.Label(
            tab1, textvariable=self._hotkey_var,
            font=("Segoe UI", 11), fg=fg, bg=bg,
            relief="flat", anchor="center", cursor="hand2",
            highlightthickness=1, highlightbackground=btn_bg,
            highlightcolor=accent)
        self._hotkey_label.pack(fill="x", pady=(0, 4), ipady=6)
        self._hotkey_label.bind("<Button-1>", lambda e: self._start_hotkey_capture())

        _lbl(tab1, "Modo de grava\u00e7\u00e3o:")
        self._recmode_labels = [lbl for lbl, _ in RECORDING_MODE_OPTIONS]
        self._recmode_codes = [code for _, code in RECORDING_MODE_OPTIONS]
        current_recmode = self.config.data.get("recording_mode", "toggle")
        recmode_idx = self._recmode_codes.index(current_recmode) if current_recmode in self._recmode_codes else 0
        self._recmode_var = tk.StringVar(value=self._recmode_labels[recmode_idx])
        _combo(tab1, self._recmode_var, self._recmode_labels)

        _lbl(tab1, "Idioma da transcri\u00e7\u00e3o:")
        self._lang_labels = [lbl for lbl, _ in LANGUAGE_OPTIONS]
        self._lang_codes = [code for _, code in LANGUAGE_OPTIONS]
        current_lang = self.config.data["language"]
        lang_idx = self._lang_codes.index(current_lang) if current_lang in self._lang_codes else 0
        self._lang_var = tk.StringVar(value=self._lang_labels[lang_idx])
        _combo(tab1, self._lang_var, self._lang_labels)

        _lbl(tab1, "Modelo de transcri\u00e7\u00e3o:")
        self._model_labels = [lbl for lbl, _ in MODEL_OPTIONS]
        self._model_codes = [code for _, code in MODEL_OPTIONS]
        current_model = self.config.data.get("model", "whisper-1")
        model_idx = self._model_codes.index(current_model) if current_model in self._model_codes else 0
        self._model_var = tk.StringVar(value=self._model_labels[model_idx])
        _combo(tab1, self._model_var, self._model_labels)

        self._auto_paste_var = tk.BooleanVar(value=self.config.data.get("auto_paste", True))
        _chk(tab1, "Colar automaticamente ap\u00f3s transcri\u00e7\u00e3o", self._auto_paste_var)

        self._inline_cmd_var = tk.BooleanVar(value=self.config.data.get("inline_commands", True))
        _chk(tab1, "Comandos por voz", self._inline_cmd_var)
        tk.Label(tab1, text='  \u2139  Conv\u00e9rte palavras como "v\u00edrgula", "ponto", "nova linha" em pontua\u00e7\u00e3o',
                 font=("Segoe UI", 8), fg=dim, bg=entry_bg, anchor="w", wraplength=380, justify="left"
                 ).pack(fill="x", pady=(0, 4))

        _lbl(tab1, "Dura\u00e7\u00e3o m\u00e1xima da grava\u00e7\u00e3o (segundos):")
        self._max_rec_var = tk.StringVar(value=str(self.config.data.get("max_recording_secs", 300)))
        tk.Entry(tab1, textvariable=self._max_rec_var,
                 font=("Consolas", 10), bg=bg, fg=fg,
                 insertbackground=accent, relief="flat",
                 bd=0, highlightthickness=1, width=8,
                 highlightbackground=btn_bg, highlightcolor=accent
                 ).pack(anchor="w", pady=(0, 4), ipady=4)

        # Auto-stop por silêncio (v3.4.0)
        self._silence_stop_var = tk.BooleanVar(value=self.config.data.get("silence_stop", False))
        _chk(tab1, "Parar automaticamente ao detectar silêncio", self._silence_stop_var)

        silence_row = tk.Frame(tab1, bg=bg)
        silence_row.pack(fill="x", pady=(0, 4))
        tk.Label(silence_row, text="  Segundos de silêncio para parar:", font=("Segoe UI", 9),
                 fg=dim, bg=bg).pack(side="left")
        self._silence_timeout_var = tk.StringVar(value=str(self.config.data.get("silence_timeout_secs", 30)))
        tk.Entry(silence_row, textvariable=self._silence_timeout_var,
                 font=("Consolas", 10), bg=bg, fg=fg,
                 insertbackground=accent, relief="flat",
                 bd=0, highlightthickness=1, width=5,
                 highlightbackground=btn_bg, highlightcolor=accent
                 ).pack(side="left", padx=(6, 0), ipady=3)
        tk.Label(silence_row, text="s  (5-120)", font=("Segoe UI", 8), fg=dim, bg=bg).pack(side="left", padx=(4, 0))

        def _toggle_silence_row(*_):
            state = "normal" if self._silence_stop_var.get() else "disabled"
            for w in silence_row.winfo_children():
                try:
                    w.config(state=state)
                except Exception:
                    pass
        self._silence_stop_var.trace_add("write", _toggle_silence_row)
        _toggle_silence_row()  # estado inicial

        # Auto-Enter após colar (v3.4.0)
        self._auto_enter_var = tk.BooleanVar(value=self.config.data.get("auto_enter", False))
        _chk(tab1, "Auto-Enter após colar (envia a mensagem automaticamente)", self._auto_enter_var)
        tk.Label(tab1, text='  \u2139  Útil em WhatsApp Web, Telegram, Slack. Ignorado quando "Revisar antes de colar" está ativo.',
                 font=("Segoe UI", 8), fg=dim, bg=entry_bg, anchor="w", wraplength=380, justify="left"
                 ).pack(fill="x", pady=(0, 4))

        # Duplo-clique Enter (v3.6.0)
        self._double_click_enter_var = tk.BooleanVar(value=self.config.data.get("double_click_enter", False))
        _chk(tab1, "Duplo-clique na hotkey envia Enter (para gravações < 1s)", self._double_click_enter_var)
        tk.Label(tab1, text='  \u2139  Pressione a hotkey 2x rápido durante a gravação para parar e enviar Enter automaticamente.',
                 font=("Segoe UI", 8), fg=dim, bg=entry_bg, anchor="w", wraplength=380, justify="left"
                 ).pack(fill="x", pady=(0, 4))

        # Método de colagem (v3.7.0)
        tk.Label(tab1, text="Método de colagem",
                 font=("Segoe UI", 9, "bold"), fg=label_fg, bg=entry_bg,
                 anchor="w").pack(fill="x", pady=(8, 2))
        PASTE_METHOD_OPTIONS = [
            ("Automático (recomendado)", "auto"),
            ("Ctrl+V direto", "ctrlv"),
            ("Caractere por caractere", "charbychar"),
        ]
        paste_labels = [o[0] for o in PASTE_METHOD_OPTIONS]
        paste_codes  = [o[1] for o in PASTE_METHOD_OPTIONS]
        cur_paste = self.config.data.get("paste_method", "auto")
        cur_paste_label = next((lbl for lbl, c in PASTE_METHOD_OPTIONS if c == cur_paste),
                               paste_labels[0])
        self._paste_method_var = tk.StringVar(value=cur_paste_label)
        cb_paste = ttk.Combobox(tab1, textvariable=self._paste_method_var,
                                values=paste_labels, state="readonly",
                                font=("Segoe UI", 10), width=32)
        cb_paste.pack(anchor="w", pady=(0, 2))
        tk.Label(tab1, text='  ℹ  "Automático" tenta Ctrl+V e recorre a digitação caractere por caractere se o app bloquear.',
                 font=("Segoe UI", 8), fg=dim, bg=entry_bg, anchor="w", wraplength=380, justify="left"
                 ).pack(fill="x", pady=(0, 4))
        self._paste_method_options = PASTE_METHOD_OPTIONS

        # Exibir StatusBar (v3.4.0)
        self._show_statusbar_var = tk.BooleanVar(value=self.config.data.get("show_statusbar", True))
        _chk(tab1, "Exibir ícone de gravação na tela", self._show_statusbar_var)
        tk.Label(tab1, text='  ℹ  Quando desligado, o app funciona em modo totalmente silencioso (sem barra visual).',
                 font=("Segoe UI", 8), fg=dim, bg=entry_bg, anchor="w", wraplength=380, justify="left"
                 ).pack(fill="x", pady=(0, 4))

        # ABA 2: Revisao de texto
        tab2 = _tab("Revis\u00e3o")

        _lbl(tab2, "Estilo de texto:")
        self._style_labels = [lbl for lbl, _ in TEXT_STYLE_OPTIONS]
        self._style_codes = [code for _, code in TEXT_STYLE_OPTIONS]
        current_style = self.config.data.get("text_style", "none")
        style_idx = self._style_codes.index(current_style) if current_style in self._style_codes else 0
        self._style_var = tk.StringVar(value=self._style_labels[style_idx])
        _combo(tab2, self._style_var, self._style_labels)

        _lbl(tab2, "Modelo GPT (estilos):")
        self._gpt_labels = [lbl for lbl, _ in GPT_MODEL_OPTIONS]
        self._gpt_codes = [code for _, code in GPT_MODEL_OPTIONS]
        current_gpt = self.config.data.get("gpt_model", "gpt-4.1-mini")
        gpt_idx = self._gpt_codes.index(current_gpt) if current_gpt in self._gpt_codes else 0
        self._gpt_var = tk.StringVar(value=self._gpt_labels[gpt_idx])
        _combo(tab2, self._gpt_var, self._gpt_labels)

        self._auto_style_var = tk.BooleanVar(value=self.config.data.get("text_style_auto", False))
        _chk(tab2, "Aplicar estilo automaticamente ap\u00f3s transcri\u00e7\u00e3o", self._auto_style_var)

        self._edit_before_paste_var = tk.BooleanVar(value=self.config.data.get("edit_before_paste", False))
        _chk(tab2, "Revisar texto antes de colar", self._edit_before_paste_var)

        # ABA 3: Emojis por voz (v3.4.0) — movido para antes de Notificações
        tab6 = _tab("Emojis")

        self._emoji_replace_var = tk.BooleanVar(value=self.config.data.get("emoji_replace", False))
        _chk(tab6, "Substituição de palavras-chave por emojis", self._emoji_replace_var)
        tk.Label(tab6, text='  ℹ  Ao falar a palavra-chave, ela é substituída pelo emoji correspondente.',
                 font=("Segoe UI", 8), fg=dim, bg=entry_bg, anchor="w", wraplength=460, justify="left"
                 ).pack(fill="x", pady=(0, 6))

        # Tabela de mapeamentos
        self._emoji_map = _load_emoji_map()

        list_frame = tk.Frame(tab6, bg=bg)
        list_frame.pack(fill="both", expand=True, pady=(0, 4))

        cols = ("Palavra-chave", "Emoji")
        self._emoji_tree = ttk.Treeview(list_frame, columns=cols, show="headings", height=6)
        self._emoji_tree.heading("Palavra-chave", text="Palavra-chave")
        self._emoji_tree.heading("Emoji", text="Emoji")
        self._emoji_tree.column("Palavra-chave", width=240)
        self._emoji_tree.column("Emoji", width=80, anchor="center")
        sb_e = tk.Scrollbar(list_frame, orient="vertical", command=self._emoji_tree.yview)
        self._emoji_tree.configure(yscrollcommand=sb_e.set)
        self._emoji_tree.pack(side="left", fill="both", expand=True)
        sb_e.pack(side="right", fill="y")

        for entry in self._emoji_map:
            self._emoji_tree.insert("", "end", values=(entry.get("keyword", ""), entry.get("emoji", "")))

        btn_row = tk.Frame(tab6, bg=bg)
        btn_row.pack(fill="x", pady=(0, 4))

        _emoji_dlg_ref = []  # mantém referência para evitar GC do dialog

        def _add_emoji_entry():
            dlg = EmojiPickerDialog(self.win, on_confirm=lambda kw, em: _insert_emoji(kw, em))
            _emoji_dlg_ref.clear()
            _emoji_dlg_ref.append(dlg)  # impede garbage collection do objeto

        def _insert_emoji(keyword, emoji):
            if not keyword.strip():
                return
            self._emoji_map.append({"keyword": keyword.strip(), "emoji": emoji})
            self._emoji_tree.insert("", "end", values=(keyword.strip(), emoji))

        def _remove_emoji_entry():
            selected = self._emoji_tree.selection()
            if not selected:
                return
            for item in selected:
                self._emoji_tree.delete(item)
            # Reconstruir _emoji_map a partir do estado atual da árvore
            self._emoji_map = [
                {"keyword": self._emoji_tree.set(row, "Palavra-chave"),
                 "emoji":   self._emoji_tree.set(row, "Emoji")}
                for row in self._emoji_tree.get_children()
            ]

        tk.Button(btn_row, text="+ Adicionar", font=("Segoe UI", 9),
                  bg=btn_bg, fg=btn_fg, relief="flat", cursor="hand2",
                  command=_add_emoji_entry).pack(side="left", padx=(0, 6))
        tk.Button(btn_row, text="- Remover selecionado", font=("Segoe UI", 9),
                  bg=btn_bg, fg=btn_fg, relief="flat", cursor="hand2",
                  command=_remove_emoji_entry).pack(side="left")

        # ABA 4: Notificacoes
        tab3 = _tab("Notifica\u00e7\u00f5es")

        self._notif_var = tk.BooleanVar(value=self.config.data.get("notification", True))
        _chk(tab3, "Exibir notifica\u00e7\u00e3o ap\u00f3s transcri\u00e7\u00e3o", self._notif_var)

        self._notif_sound_var = tk.BooleanVar(value=self.config.data.get("notification_sound", True))
        _chk(tab3, "Notifica\u00e7\u00e3o com som", self._notif_sound_var)

        self._save_audio_var = tk.BooleanVar(value=self.config.data.get("save_audio", False))
        _chk(tab3, "Salvar \u00e1udio das grava\u00e7\u00f5es", self._save_audio_var)

        # ABA 5: Dados
        tab4 = _tab("Dados")

        if self.usage and self.usage.total_requests > 0:
            t_now = get_theme()
            hero_bg = t_now.get("bg_tertiary", "#2a2a2a")
            hero_fg = t_now.get("text_primary", "#ffffff")
            hero_dim = t_now.get("text_secondary", "#aaaaaa")

            # ── Hero Stats card (v3.5.0) ──────────────────────────────
            hero = tk.Frame(tab4, bg=hero_bg, padx=12, pady=10)
            hero.pack(fill="x", pady=(0, 12))
            tk.Label(hero, text=f"🎙  Voice AI — {self.usage.month_label}",
                     font=("Segoe UI Semibold", 10), fg=hero_fg, bg=hero_bg
                     ).pack(anchor="w", pady=(0, 8))

            # 4 métricas em linha
            def _fmt_duration(total_secs):
                """Formata segundos em escala progressiva: min → horas → dias."""
                m = int(total_secs // 60)
                if m < 60:
                    return f"{m}min {int(total_secs % 60)}s"
                h = m // 60
                if h < 24:
                    return f"{h}h {m % 60}min"
                d = h // 24
                return f"{d}d {h % 24}h"

            _total_secs = self.usage.total_seconds
            _time_str = _fmt_duration(_total_secs)
            _words_log = self.usage.total_words_from_log or self.usage.total_words
            _wpm_base = UsageTracker.WPM_BASELINE
            _saved_total = _words_log / _wpm_base / 60 if _words_log else 0.0
            _saved_secs = int(_saved_total * 3600)
            _saved_str = "~" + _fmt_duration(_saved_secs)

            metrics = [
                (str(self.usage.total_requests), "transcrições"),
                (_time_str, "gravadas"),
                (f"~{_words_log:,}".replace(",", "."), "palavras transcritas"),
                (_saved_str, "economizadas"),
            ]
            mf = tk.Frame(hero, bg=hero_bg)
            mf.pack(fill="x")
            for _v, _l in metrics:
                col = tk.Frame(mf, bg=hero_bg)
                col.pack(side="left", expand=True)
                tk.Label(col, text=_v, font=("Segoe UI Semibold", 12, "bold"),
                         fg=hero_fg, bg=hero_bg).pack()
                tk.Label(col, text=_l, font=("Segoe UI", 8),
                         fg=hero_dim, bg=hero_bg).pack()

            if _saved_total >= 0.1:
                tk.Label(hero,
                         text=f"Digitando {_wpm_base} palavras/min, o Voice AI economizou {_saved_str} de digitação.",
                         font=("Segoe UI", 8, "italic"), fg=hero_dim, bg=hero_bg,
                         wraplength=460, justify="left").pack(anchor="w", pady=(6, 0))

            # ── Custo detalhado (v3.5.0) ───────────────────────────
            tk.Label(tab4, text="Custo estimado da API OpenAI",
                     font=("Segoe UI Semibold", 9), fg=label_fg, bg=entry_bg
                     ).pack(anchor="w", pady=(4, 4))

            _c_whisper = self.usage.cost_whisper_brl
            _c_gpt = self.usage.cost_gpt_brl
            if _c_whisper == 0.0 and _c_gpt == 0.0:
                _c_whisper = self.usage.cost_brl
            _c_total = _c_whisper + _c_gpt

            cost_rows = [
                ("Transcrição (Whisper)", f"R$ {_c_whisper:.2f}"),
                ("Correção de texto (GPT)", f"R$ {_c_gpt:.2f}"),
                ("Total", f"R$ {_c_total:.2f}"),
            ]
            bg_tert = t_now.get("bg_tertiary", "#2a2a2a")
            cost_frame = tk.Frame(tab4, bg=entry_bg, relief="flat",
                                  highlightbackground=bg_tert, highlightthickness=1)
            cost_frame.pack(fill="x", padx=0, pady=(0, 10))
            for _i_r, (_l_txt, _v_txt) in enumerate(cost_rows):
                _is_total = (_i_r == len(cost_rows) - 1)
                _row_bg = bg_tert if _is_total else entry_bg
                _row = tk.Frame(cost_frame, bg=_row_bg)
                _row.pack(fill="x", padx=8, pady=3)
                tk.Label(_row, text=_l_txt, font=("Segoe UI", 9),
                         fg=dim if not _is_total else fg,
                         bg=_row_bg, anchor="w").pack(side="left")
                tk.Label(_row, text=_v_txt,
                         font=("Segoe UI", 9, "bold") if _is_total else ("Segoe UI", 9),
                         fg=fg, bg=_row_bg, anchor="e").pack(side="right")

            def _export_csv_from_settings():
                try:
                    path = self.usage.export_csv()
                    import subprocess as _sp
                    _sp.Popen(["explorer", "/select,", path])
                except Exception as _e:
                    messagebox.showerror("Erro", str(_e), parent=self.win)

            tk.Button(tab4, text="Exportar CSV",
                      font=("Segoe UI", 9), bg=btn_bg, fg="#ffffff",
                      relief="flat", cursor="hand2", padx=10, pady=4,
                      command=_export_csv_from_settings
                      ).pack(anchor="w", pady=(0, 0))
            tk.Label(tab4, text="* Valores estimados. Consulte platform.openai.com para custos exatos.",
                     font=("Segoe UI", 8), fg=dim, bg=entry_bg
                     ).pack(anchor="w", pady=(6, 0))
        else:
            tk.Label(tab4, text="Nenhuma transcrição registrada ainda.",
                     font=("Segoe UI", 9), fg=dim, bg=entry_bg).pack(pady=20)

        # ABA 5: Sistema
        tab5 = _tab("Sistema")

        _lbl(tab5, "API Key OpenAI:")
        key_frame = tk.Frame(tab5, bg=entry_bg)
        key_frame.pack(fill="x", pady=(0, 4))
        self._key_var = tk.StringVar(value=self.config.data["api_key"])
        self._key_entry = tk.Entry(key_frame, textvariable=self._key_var,
                                    font=("Consolas", 10), bg=bg, fg=fg,
                                    insertbackground=accent, relief="flat", show="*",
                                    bd=0, highlightthickness=1,
                                    highlightbackground=btn_bg, highlightcolor=accent)
        self._key_entry.pack(side="left", fill="x", expand=True, ipady=4)
        self._show_key = False
        self._toggle_btn = tk.Button(key_frame, text="Mostrar",
                                      font=("Segoe UI", 8), bg=btn_bg,
                                      fg=btn_fg, relief="flat", cursor="hand2",
                                      command=self._toggle_key_visibility)
        self._toggle_btn.pack(side="right", padx=(5, 0))

        _lbl(tab5, "Tema:")
        self._theme_labels = [lbl for lbl, _ in THEME_OPTIONS]
        self._theme_codes = [code for _, code in THEME_OPTIONS]
        current_theme_val = self.config.data.get("theme", "dark")
        theme_idx = self._theme_codes.index(current_theme_val) if current_theme_val in self._theme_codes else 0
        self._theme_var = tk.StringVar(value=self._theme_labels[theme_idx])
        _combo(tab5, self._theme_var, self._theme_labels)

        _lbl(tab5, "Microfone:")
        mic_devices = get_input_devices()
        self._mic_names = ["Padr\u00e3o do sistema"] + [name for name, _ in mic_devices]
        self._mic_indices = [None] + [idx for _, idx in mic_devices]
        current_mic = self.config.data.get("mic_device", None)
        mic_idx = 0
        if current_mic is not None and current_mic in self._mic_indices:
            mic_idx = self._mic_indices.index(current_mic)
        self._mic_var = tk.StringVar(value=self._mic_names[mic_idx])
        _combo(tab5, self._mic_var, self._mic_names)

        self._auto_start_var = tk.BooleanVar(value=self.config.data.get("auto_start", True))
        _chk(tab5, "Iniciar com o Windows", self._auto_start_var)

        # Versao na aba Sistema (v3.3.1)
        tk.Frame(tab5, bg=entry_bg, height=14).pack()
        tk.Label(tab5, text=f"Voice AI  v{VERSION}",
                 font=("Segoe UI", 9, "bold"), fg=fg, bg=entry_bg,
                 anchor="w").pack(fill="x")





    def _toggle_key_visibility(self):
        self._show_key = not self._show_key
        self._key_entry.config(show="" if self._show_key else "*")
        self._toggle_btn.config(text="Ocultar" if self._show_key else "Mostrar")

    def _start_hotkey_capture(self):
        if self._hotkey_capturing:
            return
        self._hotkey_capturing = True
        self._hotkey_label.config(fg="#6c8dfa", highlightbackground="#6c8dfa")
        self._hotkey_var.set("Pressione o atalho...")
        self._hotkey_label.focus_set()
        self._hotkey_label.bind("<KeyPress>", self._on_hotkey_key)
        self._hotkey_label.bind("<Escape>", lambda e: self._cancel_hotkey_capture())
        self._hotkey_label.bind("<FocusOut>", lambda e: self._cancel_hotkey_capture())

    def _on_hotkey_key(self, event):
        IGNORE = {"Shift_L", "Shift_R", "Control_L", "Control_R", "Alt_L", "Alt_R"}
        if event.keysym in IGNORE:
            return
        parts = []
        if event.state & 0x4:
            parts.append("Ctrl")
        if event.state & 0x1:
            parts.append("Shift")
        if event.state & 0x20000:
            parts.append("Alt")
        if not parts:
            return  # precisa de pelo menos 1 modifier
        key = event.keysym
        if len(key) == 1 and key.isalpha():
            key = key.upper()
        elif key.startswith("F") and key[1:].isdigit():
            pass  # F1-F12 já vem correto
        parts.append(key)
        hotkey_str = "+".join(parts)
        self._hotkey_var.set(hotkey_str)
        self._finish_hotkey_capture()

    def _cancel_hotkey_capture(self):
        if not self._hotkey_capturing:
            return
        self._hotkey_var.set(self.config.data["hotkey"])
        self._finish_hotkey_capture()

    def _finish_hotkey_capture(self):
        self._hotkey_capturing = False
        self._hotkey_label.config(fg="#e0e0e0", highlightbackground="#35355a")
        self._hotkey_label.unbind("<KeyPress>")
        self._hotkey_label.unbind("<Escape>")
        self._hotkey_label.unbind("<FocusOut>")

    def _save(self):
        api_key = self._key_var.get().strip()
        if not api_key:
            self._status_var.set("API Key é obrigatória!")
            return
        if not api_key.startswith("sk-"):
            self._status_var.set("API Key deve começar com sk-")
            return

        lang_label = self._lang_var.get()
        lang_idx = self._lang_labels.index(lang_label) if lang_label in self._lang_labels else 0
        lang_code = self._lang_codes[lang_idx]

        model_label = self._model_var.get()
        model_idx = self._model_labels.index(model_label) if model_label in self._model_labels else 0
        model_code = self._model_codes[model_idx]

        try:
            max_rec = max(10, min(600, int(self._max_rec_var.get())))
        except ValueError:
            max_rec = 120

        try:
            silence_timeout = max(5, min(120, int(self._silence_timeout_var.get())))
        except ValueError:
            silence_timeout = 30

        # Microfone
        mic_label = self._mic_var.get()
        mic_list_idx = self._mic_names.index(mic_label) if mic_label in self._mic_names else 0
        mic_device = self._mic_indices[mic_list_idx]

        # Estilo de texto
        style_label = self._style_var.get()
        style_idx = self._style_labels.index(style_label) if style_label in self._style_labels else 0
        style_code = self._style_codes[style_idx]

        # Modelo GPT
        gpt_label = self._gpt_var.get()
        gpt_idx = self._gpt_labels.index(gpt_label) if gpt_label in self._gpt_labels else 0
        gpt_code = self._gpt_codes[gpt_idx]

        # Modo de gravação
        recmode_label = self._recmode_var.get()
        recmode_idx2 = self._recmode_labels.index(recmode_label) if recmode_label in self._recmode_labels else 0
        recmode_code = self._recmode_codes[recmode_idx2]

        # Tema
        theme_label = self._theme_var.get()
        theme_idx2 = self._theme_labels.index(theme_label) if theme_label in self._theme_labels else 0
        theme_code = self._theme_codes[theme_idx2]

        self.config.data["api_key"] = api_key
        self.config.data["hotkey"] = self._hotkey_var.get()
        self.config.data["language"] = lang_code
        self.config.data["model"] = model_code
        self.config.data["auto_paste"] = self._auto_paste_var.get()
        self.config.data["edit_before_paste"] = self._edit_before_paste_var.get()
        self.config.data["auto_start"] = self._auto_start_var.get()
        self.config.data["notification"] = self._notif_var.get()
        self.config.data["notification_sound"] = self._notif_sound_var.get()
        self.config.data["save_audio"] = self._save_audio_var.get()
        self.config.data["inline_commands"] = self._inline_cmd_var.get()
        self.config.data["max_recording_secs"] = max_rec
        self.config.data["silence_stop"] = self._silence_stop_var.get()
        self.config.data["silence_timeout_secs"] = silence_timeout
        self.config.data["auto_enter"] = self._auto_enter_var.get()
        self.config.data["auto_enter_delay_ms"] = self.config.data.get("auto_enter_delay_ms", 100)
        self.config.data["mic_device"] = mic_device
        self.config.data["text_style"] = style_code
        self.config.data["text_style_auto"] = self._auto_style_var.get()
        self.config.data["gpt_model"] = gpt_code
        self.config.data["recording_mode"] = recmode_code
        self.config.data["theme"] = theme_code
        self.config.data["emoji_replace"] = self._emoji_replace_var.get()
        self.config.data["show_statusbar"] = self._show_statusbar_var.get()
        self.config.data["double_click_enter"] = self._double_click_enter_var.get()
        paste_label = self._paste_method_var.get()
        paste_code = next((c for lbl, c in self._paste_method_options if lbl == paste_label), "auto")
        self.config.data["paste_method"] = paste_code
        _save_emoji_map(self._emoji_map)
        self.config.save()

        self.saved = True
        log(f"Settings saved: hotkey={self.config.hotkey}, lang={lang_code}, mic={mic_device}, style={style_code}, mode={recmode_code}")

        if self.on_save:
            self.on_save()

        self.win.destroy()

    def _on_close_first_run(self):
        if not self.saved:
            sys.exit(0)
        self.win.destroy()

    def run(self):
        """Bloqueia até o dialog fechar (usado no first-run)."""
        self.win.mainloop()
        return self.saved


# ─── AudioRecorder ───────────────────────────────────────────

class AudioRecorder:
    def __init__(self, rate=RATE, channels=CHANNELS, on_volume=None, device=None):
        self.rate = rate
        self.channels = channels
        self._recording = False
        self._frames = []
        self._thread = None
        self._on_volume = on_volume  # callback(rms: float 0–1) chamado a cada chunk
        self._rec_start = 0.0
        self.device = device  # índice do dispositivo de áudio (None = padrão)

    def start(self):
        self._frames = []
        self._recording = True
        self._rec_start = time.time()
        self._thread = threading.Thread(target=self._loop, daemon=True)
        self._thread.start()

    @property
    def elapsed(self):
        return time.time() - self._rec_start if self._recording else 0.0

    def stop(self):
        self._recording = False
        if self._thread:
            self._thread.join(timeout=3)
            self._thread = None
        if not self._frames:
            return None
        data = np.concatenate(self._frames, axis=0)
        buf = io.BytesIO()
        with wave.open(buf, "wb") as wf:
            wf.setnchannels(self.channels)
            wf.setsampwidth(2)
            wf.setframerate(self.rate)
            wf.writeframes(data.tobytes())
        buf.seek(0)
        buf.name = "audio.wav"
        return buf

    def _loop(self):
        # InputStream persistente — mantém microfone aberto o tempo todo
        # (evita abrir/fechar a cada chunk, que causa flicker no tray)
        chunk_samples = int(self.rate * 0.1)  # chunks de 100ms para volume mais responsivo
        try:
            with sd.InputStream(samplerate=self.rate, channels=self.channels,
                                dtype="int16", device=self.device) as stream:
                while self._recording:
                    chunk, _ = stream.read(chunk_samples)
                    if self._recording:
                        self._frames.append(chunk.copy())
                        if self._on_volume:
                            rms = float(np.sqrt(np.mean(chunk.astype(np.float32) ** 2)))
                            normalized = min(rms / 8000.0, 1.0)
                            self._on_volume(normalized)
        except Exception as e:
            log(f"AudioRecorder error: {e}")


# ─── Transcriber ─────────────────────────────────────────────

class Transcriber:
    MAX_RETRIES = 2

    def __init__(self, api_key, model, language):
        self.client = OpenAI(
            api_key=api_key,
            timeout=httpx.Timeout(30.0, connect=10.0),  # v3.3.1: 30s por req, evita travamento infinito
        )
        self.model = model
        self.language = language

    def transcribe(self, wav_buf, cancelled_flag=None):
        """cancelled_flag: lista [True] — setar [0]=False para cancelar entre retries."""
        last_err = None
        for attempt in range(1 + self.MAX_RETRIES):
            # Checar cancelamento antes de cada tentativa
            if cancelled_flag is not None and not cancelled_flag[0]:
                raise Exception("cancelled")
            try:
                wav_buf.seek(0)
                resp = self.client.audio.transcriptions.create(
                    model=self.model, file=wav_buf, language=self.language,
                )
                duration_sec = 0
                if hasattr(resp, "usage") and resp.usage and hasattr(resp.usage, "seconds"):
                    duration_sec = resp.usage.seconds
                return resp.text.strip(), duration_sec
            except (openai.APIConnectionError, openai.APITimeoutError) as e:
                last_err = e
                # Checar cancelamento após erro de conexão
                if cancelled_flag is not None and not cancelled_flag[0]:
                    raise Exception("cancelled")
                if attempt < self.MAX_RETRIES:
                    log(f"Transcribe retry {attempt + 1}/{self.MAX_RETRIES}: {e}")
                    time.sleep(1.0 * (attempt + 1))
                else:
                    raise
            except Exception:
                raise


# ─── TextPaster ──────────────────────────────────────────────

class TextPaster:
    def __init__(self):
        self._send_input = user32.SendInput
        self._send_input.argtypes = [
            ctypes.wintypes.UINT,
            ctypes.POINTER(INPUT_STRUCT),
            ctypes.wintypes.INT,
        ]
        self._send_input.restype = ctypes.wintypes.UINT
        # Clipboard/memória — argtypes obrigatórios para 64-bit
        kernel32.GlobalAlloc.argtypes = [ctypes.c_uint, ctypes.c_size_t]
        kernel32.GlobalAlloc.restype = ctypes.c_void_p
        kernel32.GlobalLock.argtypes = [ctypes.c_void_p]
        kernel32.GlobalLock.restype = ctypes.c_void_p
        kernel32.GlobalUnlock.argtypes = [ctypes.c_void_p]
        kernel32.GlobalUnlock.restype = ctypes.c_bool
        user32.SetClipboardData.argtypes = [ctypes.c_uint, ctypes.c_void_p]
        user32.SetClipboardData.restype = ctypes.c_void_p
        user32.OpenClipboard.argtypes = [ctypes.c_void_p]
        user32.OpenClipboard.restype = ctypes.c_bool
        user32.RegisterClipboardFormatW.argtypes = [ctypes.c_wchar_p]
        user32.RegisterClipboardFormatW.restype = ctypes.c_uint

    def paste(self, text):
        """Legado: copia pro clipboard e cola com Ctrl+V."""
        pyperclip.copy(text)
        time.sleep(0.15)
        user32.keybd_event(VK_CONTROL, 0, 0, 0)
        user32.keybd_event(VK_V, 0, 0, 0)
        user32.keybd_event(VK_V, 0, KEYUP_FLAG, 0)
        user32.keybd_event(VK_CONTROL, 0, KEYUP_FLAG, 0)

    def type_text(self, text):
        """Digita texto via SendInput Unicode — NÃO toca no clipboard."""
        size = ctypes.sizeof(INPUT_STRUCT)
        sent = 0
        for char in text:
            code = ord(char)
            # Keydown
            inp_down = INPUT_STRUCT()
            inp_down.type = INPUT_KEYBOARD
            inp_down.u.ki.wVk = 0
            inp_down.u.ki.wScan = code
            inp_down.u.ki.dwFlags = KEYEVENTF_UNICODE
            inp_down.u.ki.time = 0
            inp_down.u.ki.dwExtraInfo = 0
            # Keyup
            inp_up = INPUT_STRUCT()
            inp_up.type = INPUT_KEYBOARD
            inp_up.u.ki.wVk = 0
            inp_up.u.ki.wScan = code
            inp_up.u.ki.dwFlags = KEYEVENTF_UNICODE | KEYEVENTF_KEYUP_SI
            inp_up.u.ki.time = 0
            inp_up.u.ki.dwExtraInfo = 0
            # Send both
            inputs = (INPUT_STRUCT * 2)(inp_down, inp_up)
            r = self._send_input(2, inputs, size)
            sent += r
            time.sleep(0.005)
        log(f"type_text: {len(text)} chars, {sent} events sent (size={size})")

    def _clipboard_set_stealth(self, text):
        """Seta texto no clipboard com flag de exclusão do Win+V."""
        if not user32.OpenClipboard(None):
            return False
        try:
            user32.EmptyClipboard()
            # Flag que exclui do Win+V
            fmt = user32.RegisterClipboardFormatW(
                "ExcludeClipboardContentFromMonitorProcessing")
            if fmt:
                h = kernel32.GlobalAlloc(0x0042, 1)
                if h:
                    user32.SetClipboardData(fmt, h)
            # Texto como CF_UNICODETEXT
            encoded = text.encode("utf-16-le") + b"\x00\x00"
            hText = kernel32.GlobalAlloc(0x0042, len(encoded))
            if hText:
                ptr = kernel32.GlobalLock(hText)
                ctypes.memmove(ptr, encoded, len(encoded))
                kernel32.GlobalUnlock(hText)
                user32.SetClipboardData(13, hText)  # CF_UNICODETEXT
            return True
        finally:
            user32.CloseClipboard()

    def stealth_paste(self, text):
        """Cola texto via Ctrl+V SEM aparecer no Win+V."""
        # Salva clipboard original
        try:
            old_clip = pyperclip.paste()
        except Exception:
            old_clip = ""
        # Seta texto com exclusão do Win+V
        if not self._clipboard_set_stealth(text):
            log("stealth_paste: OpenClipboard failed, fallback")
            self.paste(text)
            return
        # Ctrl+V via SendInput — replica o padrão exato do Wispr Flow:
        # key-down atômico → delay → key-up atômico
        time.sleep(0.02)
        sz = ctypes.sizeof(INPUT_STRUCT)
        scan_ctrl = user32.MapVirtualKeyW(VK_CONTROL, 0)
        scan_v = user32.MapVirtualKeyW(VK_V, 0)
        # Key-down: Ctrl + V
        down = (INPUT_STRUCT * 2)()
        down[0].type = INPUT_KEYBOARD
        down[0].u.ki.wVk = VK_CONTROL
        down[0].u.ki.wScan = scan_ctrl
        down[1].type = INPUT_KEYBOARD
        down[1].u.ki.wVk = VK_V
        down[1].u.ki.wScan = scan_v
        self._send_input(2, down, sz)
        time.sleep(0.02)
        # Key-up: V + Ctrl
        up = (INPUT_STRUCT * 2)()
        up[0].type = INPUT_KEYBOARD
        up[0].u.ki.wVk = VK_V
        up[0].u.ki.wScan = scan_v
        up[0].u.ki.dwFlags = KEYEVENTF_KEYUP_SI
        up[1].type = INPUT_KEYBOARD
        up[1].u.ki.wVk = VK_CONTROL
        up[1].u.ki.wScan = scan_ctrl
        up[1].u.ki.dwFlags = KEYEVENTF_KEYUP_SI
        self._send_input(2, up, sz)
        # Restaura clipboard original em 500ms (também stealth)
        def _restore():
            time.sleep(0.5)
            if old_clip:
                self._clipboard_set_stealth(old_clip)
            else:
                if user32.OpenClipboard(None):
                    user32.EmptyClipboard()
                    user32.CloseClipboard()
        threading.Thread(target=_restore, daemon=True).start()
        log(f"stealth_paste: {len(text)} chars (excl from Win+V)")

    def _clipboard_verify(self, text, timeout=0.35):
        """Verifica se o clipboard ainda contém o texto esperado após paste (detecção de falha)."""
        time.sleep(timeout)
        try:
            return pyperclip.paste() == text
        except Exception:
            return True  # não conseguiu verificar → assume OK

    def smart_paste(self, text, method="auto"):
        """Cola texto com fallback progressivo (v3.7.0).

        method:
          "auto"       → tenta stealth_paste; se falhar (clipboard intacto após 350ms), tenta charbychar
          "ctrlv"      → só stealth_paste (comportamento legado)
          "charbychar" → só type_text (char-by-char via SendInput Unicode)
        """
        if method == "charbychar":
            self.type_text(text)
            return True

        if method == "ctrlv":
            try:
                self.stealth_paste(text)
            except Exception:
                self.paste(text)
            return True

        # method == "auto": stealth_paste direto, char-by-char só se exceção
        # NOTA: a heurística anterior de "clipboard intacto" era falsa —
        # stealth_paste restaura o clipboard em 500ms, então após 350ms de espera
        # o clipboard ainda continha o texto, fazendo o fallback sempre disparar
        # e duplicar o texto (Ctrl+V colava + char-by-char digitava de novo).
        try:
            self.stealth_paste(text)
            log("smart_paste: nível 1 (stealth_paste) OK")
            return True
        except Exception as e:
            log(f"smart_paste: nível 1 exception: {e}, tentando char-by-char")

        # Nível 2: char-by-char via SendInput Unicode (só se stealth_paste falhar)
        try:
            self.type_text(text)
            log("smart_paste: nível 2 (char-by-char) OK")
            return True
        except Exception as e:
            log(f"smart_paste: nível 2 exception: {e}")

        log("smart_paste: todos os métodos falharam")
        return False

    def get_foreground_window(self):
        return user32.GetForegroundWindow()

    def set_foreground_window(self, hwnd):
        """Força foco na janela — funciona com Chrome/Edge."""
        cur_fg = user32.GetForegroundWindow()
        cur_tid = kernel32.GetCurrentThreadId()
        tgt_tid = user32.GetWindowThreadProcessId(hwnd, None)
        attached = False
        if cur_tid != tgt_tid:
            attached = bool(user32.AttachThreadInput(cur_tid, tgt_tid, True))
        # ALT trick SÓ quando alvo NÃO é a janela atual
        # (evita ativar menu bar de Edge/Chrome quando alvo já tem foco)
        if cur_fg != hwnd:
            user32.keybd_event(0x12, 0, 0, 0)
            user32.keybd_event(0x12, 0, KEYUP_FLAG, 0)
        ret = user32.SetForegroundWindow(hwnd)
        user32.BringWindowToTop(hwnd)
        if attached:
            user32.AttachThreadInput(cur_tid, tgt_tid, False)
        log(f"set_foreground({hwnd}): ret={ret}, attach={attached}, was_fg={cur_fg == hwnd}")


# ─── TranscriptionHistory ───────────────────────────────────

class TranscriptionHistory:
    _DATA_DIR = os.path.join(os.environ.get("LOCALAPPDATA", script_dir), "VoiceAI")
    _PATH = os.path.join(_DATA_DIR, "history.json")

    def __init__(self, max_items=50):
        self.items = []
        self.max_items = max_items
        self._load()

    # ── Persistência ──────────────────────────────────────────
    def _save(self):
        try:
            os.makedirs(self._DATA_DIR, exist_ok=True)
            with open(self._PATH, "w", encoding="utf-8") as f:
                json.dump(self.items, f, ensure_ascii=False, indent=2)
        except Exception as e:
            log(f"History save error: {e}")

    def _load(self):
        try:
            if os.path.exists(self._PATH):
                with open(self._PATH, "r", encoding="utf-8") as f:
                    data = json.load(f)
                if isinstance(data, list):
                    self.items = data[:self.max_items]
        except Exception as e:
            log(f"History load error: {e}")

    # ── API pública ───────────────────────────────────────────
    def add(self, text, audio_path=None, original_text=None):
        now = datetime.now()
        entry = {
            "text": text,
            "time_str": now.strftime("%H:%M"),
            "date_str": now.strftime("%d/%m  %H:%M"),
            "preview": text[:60] + ("..." if len(text) > 60 else ""),
            "audio_path": audio_path,
            "pinned": False,
        }
        # Salva original_text apenas se foi modificado pelo GPT
        if original_text and original_text != text:
            entry["original_text"] = original_text
        self.items.insert(0, entry)
        if len(self.items) > self.max_items:
            self.items.pop()
        self._save()

    def get_all(self):
        return list(self.items)

    def remove(self, entry):
        try:
            self.items.remove(entry)
            self._save()
        except ValueError:
            pass

    def clear(self):
        self.items.clear()
        self._save()

    def is_empty(self):
        return len(self.items) == 0

    def export_txt(self):
        """Exporta histórico como arquivo .txt e retorna o caminho."""
        now = datetime.now()
        filename = f"historico_voiceai_{now.strftime('%Y%m%d_%H%M%S')}.txt"
        path = os.path.join(self._DATA_DIR, filename)
        os.makedirs(self._DATA_DIR, exist_ok=True)
        with open(path, "w", encoding="utf-8") as f:
            f.write(f"Voice AI — Histórico de Transcrições\n")
            f.write(f"Exportado em: {now.strftime('%d/%m/%Y %H:%M')}\n")
            f.write("=" * 50 + "\n\n")
            for item in self.items:
                date = item.get("date_str", item.get("time_str", ""))
                f.write(f"[{date}]\n{item['text']}\n\n")
        return path


# ─── StatusBar ───────────────────────────────────────────────

class StatusBar:
    def __init__(self, on_click=None, on_exit=None, on_clear=None, on_settings=None):
        t = get_theme()
        self.root = tk.Tk()
        self.root.title("Voice AI")
        self.root.overrideredirect(True)
        self.root.attributes("-topmost", True)
        self.root.attributes("-alpha", 0.92)   # v3.4.0: leve transparência para blend com o fundo
        _TC = "#010101"   # transparentcolor para área fora do pill arredondado
        self.root.configure(bg=_TC)
        self.root.attributes("-transparentcolor", _TC)

        self._on_click = on_click
        self._on_exit = on_exit
        self._on_clear = on_clear
        self._on_settings = on_settings

        # ── Dimensões e posição ──
        self.BAR_W, self.BAR_H = 150, 28   # v4.1.2: reduzido 10% de cada lado (era 168x28)
        x = (self.root.winfo_screenwidth() - self.BAR_W) // 2
        y = self.root.winfo_screenheight() - self.BAR_H - 56
        self.root.geometry(f"{self.BAR_W}x{self.BAR_H}+{x}+{y}")
        self._screen_h = self.root.winfo_screenheight()
        self._screen_w = self.root.winfo_screenwidth()

        # ── Canvas principal ──
        self._canvas = tk.Canvas(
            self.root, width=self.BAR_W, height=self.BAR_H,
            bg=_TC, highlightthickness=0, cursor="hand2"
        )
        self._canvas.pack(fill="both", expand=True)

        # Inicializa atributos usados por _draw_gradient_bg / tag_raise
        self._bg_photo = None
        self._text_id = None
        self._timer_id = None
        self._wave_bars = []

        # Gradiente de fundo (PIL)
        self._draw_gradient_bg(t)

        # Texto principal (centralizado — usado para status e animação de transcribing)
        self._text_id = self._canvas.create_text(
            self.BAR_W // 2, self.BAR_H // 2, text="", font=("Segoe UI", 8, "bold"),
            fill=t["text_secondary"], anchor="center"
        )

        self._dots_anim_id = None

        # Waveform (barras) — layout centrado no canvas (v4.1.2: centralização precisa)
        N_BARS = 12
        bar_w = 3
        gap = 2
        total_wave_w = N_BARS * (bar_w + gap) - gap   # = 59px
        INNER_GAP = 6   # pixels entre timer e waveform
        # Calcula grupo total (timer + gap + waveform) e centraliza no canvas
        # timer_est_w é usado só para o cálculo do group; o texto real se ancora à direita
        # do ponto de junção, garantindo simetria independente do conteúdo do timer
        timer_est_w = 28  # largura real aproximada de "0:00" em Segoe UI 8pt bold
        total_group_w = timer_est_w + INNER_GAP + total_wave_w
        group_x = (self.BAR_W - total_group_w) // 2   # margem esquerda do grupo
        # Ponto de junção: fim do timer / início do gap
        join_x = group_x + timer_est_w
        wave_x_start = join_x + INNER_GAP

        # Timer: âncora "e" no ponto de junção → texto cresce para a esquerda
        self._timer_id = self._canvas.create_text(
            join_x, self.BAR_H // 2, text="", font=("Segoe UI", 8, "bold"),
            fill=t["rec_dot"], anchor="e"
        )
        self._canvas.itemconfigure(self._timer_id, state="hidden")
        self._wave_bars = []
        for i in range(N_BARS):
            bx = wave_x_start + i * (bar_w + gap)
            bar = self._canvas.create_rectangle(
                bx, self.BAR_H // 2, bx + bar_w, self.BAR_H // 2,
                fill=t["waveform"], outline=""
            )
            self._canvas.itemconfigure(bar, state="hidden")
            self._wave_bars.append(bar)
        self._wave_x_start = wave_x_start
        self._wave_bar_w = bar_w
        self._wave_gap = gap

        # Bindings
        if on_click:
            self._canvas.bind("<Button-1>", lambda e: on_click())
        self._build_context_menu(t)
        self._canvas.bind("<Button-3>", lambda e: self._menu.tk_popup(e.x_root, e.y_root))

        self.root.withdraw()
        self._visible = False
        self._hide_timer = None
        self._state = "idle"  # idle, recording, transcribing, result
        self._pulse_anim_id = None
        self.enabled = True   # v3.4.0: False = modo silencioso sem barra visual
        self._fade_anim_id = None  # v3.8.0: fade in/out alpha animation

    def _draw_gradient_bg(self, t):
        """v3.4.0: desenha pill arredondado preto via PIL + tag_raise nos itens."""
        from PIL import Image, ImageDraw, ImageTk
        # Cria imagem RGBA com pill arredondado preto
        img = Image.new("RGBA", (self.BAR_W, self.BAR_H), (0, 0, 0, 0))
        draw = ImageDraw.Draw(img)
        radius = self.BAR_H // 2
        # Converte hex preto para RGBA
        r, g, b = 10, 10, 10   # quase preto (não 0,0,0 pois transparentcolor usa #010101)
        draw.rounded_rectangle([0, 0, self.BAR_W - 1, self.BAR_H - 1],
                                radius=radius, fill=(r, g, b, 230))
        # Converte para PhotoImage via RGB (transparentcolor lida com fundo)
        bg_rgb = Image.new("RGB", (self.BAR_W, self.BAR_H), (1, 1, 1))   # #010101
        bg_rgb.paste(img, mask=img.split()[3])
        self._bg_photo = ImageTk.PhotoImage(bg_rgb)
        if hasattr(self, "_bg_img_id") and self._bg_img_id:
            self._canvas.itemconfig(self._bg_img_id, image=self._bg_photo)
        else:
            self._bg_img_id = self._canvas.create_image(0, 0, anchor="nw", image=self._bg_photo)
        # tag_raise nos itens de conteúdo
        items = list(self._wave_bars)
        if self._text_id:
            items.append(self._text_id)
        if self._timer_id:
            items.append(self._timer_id)
        for item in items:
            if item:
                self._canvas.tag_raise(item)

    def _draw_mic_icon(self, t, recording=False):
        """Stub — mic icon removido no v3.1.3 (design minimalista)."""
        pass

    def _build_context_menu(self, t):
        """Monta menu de contexto do botão direito."""
        self._menu = tk.Menu(self.root, tearoff=0,
                             bg=t["bg_secondary"], fg=t["text_primary"],
                             activebackground=t["bg_tertiary"],
                             activeforeground=t["white"])
        if self._on_settings:
            self._menu.add_command(label="Configurações", command=self._on_settings)
        if self._on_clear:
            self._menu.add_command(label="Limpar histórico", command=self._on_clear)
        self._menu.add_separator()
        if self._on_exit:
            self._menu.add_command(label="Sair", command=self._on_exit)

    def _start_dots_animation(self):
        """Inicia animação de texto 'Transcrevendo...' (substitui dots bouncing — v3.1.3)."""
        self._stop_dots_animation()
        self._dots_frame = 0
        t = get_theme()
        self._canvas.itemconfigure(self._text_id, state="normal",
                                   fill=t["processing"])

        def _animate():
            dots = "." * (self._dots_frame % 4)
            label = f"Transcrevendo{dots}"
            self._canvas.itemconfigure(self._text_id, text=label)
            self._dots_frame += 1
            self._dots_anim_id = self.root.after(400, _animate)
        _animate()

    def _stop_dots_animation(self):
        """Para animação de texto transcribing."""
        if self._dots_anim_id:
            self.root.after_cancel(self._dots_anim_id)
            self._dots_anim_id = None
        self._canvas.itemconfigure(self._text_id, state="hidden")

    def _start_pulse_animation(self):
        """Stub — pulse animation removida no v3.1.3."""
        pass

    def _stop_pulse_animation(self):
        """Stub — pulse animation removida no v3.1.3."""
        if self._pulse_anim_id:
            self.root.after_cancel(self._pulse_anim_id)
            self._pulse_anim_id = None

    def _enter_state(self, new_state):
        """Transição de estado."""
        if new_state == self._state:
            return
        old_state = self._state
        self._state = new_state

        # Limpa estado anterior
        if old_state == "recording":
            for bar in self._wave_bars:
                self._canvas.itemconfigure(bar, state="hidden")
            self._canvas.itemconfigure(self._timer_id, state="hidden")
        elif old_state == "transcribing":
            self._stop_dots_animation()

        # Configura novo estado
        if new_state == "recording":
            # Apenas waveform + timer, sem mic, sem texto
            self._move_to_top()
            self._canvas.itemconfigure(self._text_id, state="hidden")
            for bar in self._wave_bars:
                self._canvas.itemconfigure(bar, state="normal")
            self._canvas.itemconfigure(self._timer_id, state="normal")
        elif new_state == "transcribing":
            # v3.4.0: move para baixo para não obstruir o campo de texto do usuário
            self._move_to_bottom()
            self._canvas.itemconfigure(self._text_id, state="hidden")
            self._start_dots_animation()
        elif new_state in ("result", "idle"):
            self._move_to_bottom()   # resultado aparece embaixo (não interfere na digitação)
            self._canvas.itemconfigure(self._text_id, state="normal")

    def _move_to_top(self):
        """v4.0.9: sempre posiciona embaixo (nome mantido por compatibilidade)."""
        self._move_to_bottom()

    def _move_to_bottom(self):
        """v3.4.0: Move a StatusBar para o fundo (estado transcribing — não obstrui o campo de texto)."""
        x = (self._screen_w - self.BAR_W) // 2
        y = self._screen_h - self.BAR_H - 56   # acima da barra de tarefas (~40px) + margem
        self.root.geometry(f"{self.BAR_W}x{self.BAR_H}+{x}+{y}")

    def set_enabled(self, enabled: bool):
        """v3.4.0: Liga/desliga exibição da StatusBar (modo silencioso)."""
        self.enabled = enabled
        if not enabled and self._visible:
            self.hide()

    def show(self, text, color, auto_hide_ms=0):
        """Exibe mensagem na StatusBar. color pode ser hex ou nome semântico."""
        if not self.enabled:
            return
        t = get_theme()
        # Resolve cor semântica
        resolved_color = t.get(color, color) if not color.startswith("#") else color

        def _do():
            # Para na transição — sai de recording/transcribing
            if self._state in ("recording", "transcribing"):
                self._enter_state("result")
            else:
                self._state = "result" if auto_hide_ms > 0 else "idle"

            self._canvas.itemconfigure(self._text_id, text=text, fill=resolved_color)
            self._canvas.itemconfigure(self._text_id, state="normal")

            if not self._visible:
                self.root.deiconify()
                self.root.attributes("-topmost", True)
                self._visible = True
                self._fade_in()   # v3.8.0: fade-in ao aparecer
            if self._hide_timer:
                self.root.after_cancel(self._hide_timer)
                self._hide_timer = None
            if auto_hide_ms > 0:
                self._hide_timer = self.root.after(auto_hide_ms, self.hide)
        self.root.after(0, _do)

    def show_transcribing(self):
        """Mostra animação de transcribing (dots bouncing)."""
        if not self.enabled:
            return
        def _do():
            if not self._visible:
                self.root.deiconify()
                self.root.attributes("-topmost", True)
                self._visible = True
                self._fade_in()   # v3.8.0: fade-in ao aparecer
            self._enter_state("transcribing")
        self.root.after(0, _do)

    def update_recording(self, elapsed_secs, rms_normalized, ptt=False, max_secs=0):
        """Atualiza StatusBar durante gravação: waveform + timer no Canvas."""
        if not self.enabled:
            return
        import math

        # v4.1.6: não força "recording" se já estiver em "transcribing" (gravação paralela)
        # — evita que o timer/waveform da nova gravação sobrescreva a animação de dots
        if self._state not in ("recording", "transcribing"):
            self.root.after(0, lambda: self._enter_state("recording"))

        mins = int(elapsed_secs) // 60
        secs = int(elapsed_secs) % 60
        timer_text = f"0:{secs:02d}" if mins == 0 else f"{mins}:{secs:02d}"

        # v4.1.4: piscar vermelho nos últimos 15s antes do limite
        warning_active = (
            max_secs > 0
            and not ptt
            and (max_secs - elapsed_secs) <= 15
        )
        # Pisca a cada ~500ms: visível nos ticks pares do loop (80ms × ~6 = ~480ms)
        blink_visible = (int(elapsed_secs * 2) % 2 == 0)

        N_BARS = len(self._wave_bars)
        canvas_h = self.BAR_H
        bar_w = self._wave_bar_w
        gap = self._wave_gap
        now = time.time()
        t = get_theme()

        RMS_THRESHOLD = 0.04   # abaixo disso = silêncio, barras ficam flat
        heights = []
        for i in range(N_BARS):
            phase = now * 5.0 + i * 0.9
            sine = (math.sin(phase) + 1) / 2.0
            min_h = max(2, int(canvas_h * 0.08))
            if rms_normalized < RMS_THRESHOLD:
                h = min_h   # flat quando silencioso
            else:
                h = min_h + int(rms_normalized * sine * (canvas_h * 0.72))
            heights.append(max(min_h, h))

        def _do():
            if warning_active:
                # Pisca alternando hidden/normal — Canvas não suporta alfa em texto
                self._canvas.itemconfigure(self._timer_id,
                                           text=timer_text, fill="#ff4444",
                                           state="normal" if blink_visible else "hidden")
            else:
                self._canvas.itemconfigure(self._timer_id,
                                           text=timer_text, fill=t["rec_dot"],
                                           state="normal")
            for i, (bar_id, h) in enumerate(zip(self._wave_bars, heights)):
                x0 = self._wave_x_start + i * (bar_w + gap)
                y0 = (canvas_h - h) // 2
                y1 = y0 + h
                self._canvas.coords(bar_id, x0, y0, x0 + bar_w, y1)
                ratio = h / canvas_h
                color = interpolate_color(t["waveform"], t["white"], ratio * 0.4)
                self._canvas.itemconfig(bar_id, fill=color)
            if not self._visible:
                self.root.deiconify()
                self.root.attributes("-topmost", True)
                self._visible = True
                self._fade_in()   # v3.8.0: fade-in ao aparecer
        self.root.after(0, _do)

    def _fade_in(self, target_alpha=0.92, steps=6, interval_ms=20):
        """v3.8.0: animação de fade-in do StatusBar."""
        if self._fade_anim_id:
            self.root.after_cancel(self._fade_anim_id)
            self._fade_anim_id = None
        step_size = target_alpha / steps
        self.root.attributes("-alpha", 0.0)

        def _step(current):
            nxt = min(current + step_size, target_alpha)
            try:
                self.root.attributes("-alpha", nxt)
            except Exception:
                return
            if nxt < target_alpha:
                self._fade_anim_id = self.root.after(interval_ms, lambda: _step(nxt))
            else:
                self._fade_anim_id = None
        self.root.after(0, lambda: _step(0.0))

    def _fade_out(self, on_done=None, steps=6, interval_ms=20):
        """v3.8.0: animação de fade-out antes de esconder."""
        if self._fade_anim_id:
            self.root.after_cancel(self._fade_anim_id)
            self._fade_anim_id = None
        try:
            current_alpha = float(self.root.attributes("-alpha"))
        except Exception:
            current_alpha = 0.92
        # Garante step_size > 0 mesmo se alpha já for 0 (evita loop infinito)
        if current_alpha <= 0.0:
            if on_done:
                on_done()
            return
        step_size = current_alpha / steps

        def _step(alpha):
            nxt = max(alpha - step_size, 0.0)
            try:
                self.root.attributes("-alpha", nxt)
            except Exception:
                if on_done:
                    on_done()
                return
            if nxt > 0.0:
                self._fade_anim_id = self.root.after(interval_ms, lambda: _step(nxt))
            else:
                self._fade_anim_id = None
                if on_done:
                    on_done()
        self.root.after(0, lambda: _step(current_alpha))

    def hide(self):
        def _do():
            self._stop_pulse_animation()
            self._stop_dots_animation()
            if self._visible:
                def _after_fade():
                    self.root.withdraw()
                    self._visible = False
                    self._state = "idle"
                self._fade_out(on_done=_after_fade)
            else:
                self._visible = False
                self._state = "idle"
        self.root.after(0, _do)

    def apply_theme(self):
        """Reaplica o tema ao StatusBar (para troca de tema ao vivo)."""
        t = get_theme()
        _TC = "#010101"
        self.root.configure(bg=_TC)
        self._canvas.configure(bg=_TC)
        self._draw_gradient_bg(t)
        self._build_context_menu(t)
        self._canvas.bind("<Button-3>", lambda e: self._menu.tk_popup(e.x_root, e.y_root))

    def run(self):
        self.root.mainloop()

    def quit(self):
        self.root.quit()


# ─── MainPanel (painel unificado) ────────────────────────────

class MainPanel:
    """Painel unificado — histórico de transcrições + acesso a configurações.
    Design moderno: cards com texto completo, hover accent, 650px de largura.
    """

    PW, PH = 650, 500

    # Propriedades dinâmicas que delegam para get_theme()
    @property
    def BG_DARK(self):
        return get_theme()["bg_primary"]

    @property
    def BG_CARD(self):
        return get_theme()["bg_secondary"]

    @property
    def BG_CARD_HOVER(self):
        return get_theme()["bg_tertiary"]

    @property
    def BORDER_CARD(self):
        return get_theme()["border"]

    @property
    def ACCENT(self):
        return get_theme()["accent"]

    @property
    def TEXT_PRIMARY(self):
        return get_theme()["text_primary"]

    @property
    def TEXT_SECONDARY(self):
        return get_theme()["text_secondary"]

    @property
    def TEXT_DIM(self):
        return get_theme()["text_dim"]

    @property
    def SUCCESS(self):
        return get_theme()["success"]

    def __init__(self, root, history, paster, on_settings=None, hotkey_str="",
                 config=None, usage=None, on_settings_saved=None):
        self.root = root
        self.history = history
        self.paster = paster
        self._on_settings = on_settings
        self._on_settings_saved = on_settings_saved
        self._hotkey_str = hotkey_str
        self._config = config
        self._usage = usage
        self._status_text = "Pronto"
        self._panel = None
        self._prev_hwnd = None
        self._subtitle_label = None
        self._drag_x = 0
        self._drag_y = 0

    def toggle(self):
        if self._panel and self._panel.winfo_exists():
            self.close()
        else:
            self.open()

    def update_status(self, text):
        self._status_text = text
        if self._subtitle_label and self._panel and self._panel.winfo_exists():
            self._subtitle_label.config(
                text=f"{self._hotkey_str}  \u2022  {self._status_text}")

    def open(self):
        self._prev_hwnd = self.paster.get_foreground_window()

        self._panel = tk.Toplevel(self.root)
        self._panel.overrideredirect(True)
        self._panel.attributes("-topmost", True)
        self._panel.attributes("-alpha", 0.97)
        self._panel.configure(bg=self.BG_DARK)

        sx = (self._panel.winfo_screenwidth() - self.PW) // 2
        sy = (self._panel.winfo_screenheight() - self.PH) // 2 - 50
        self._panel.geometry(f"{self.PW}x{self.PH}+{sx}+{sy}")

        self._expanded_cards = set()
        self._build_header()
        self._build_cards_area(animate=True)
        self._build_footer()

        self._panel.bind("<Escape>", lambda e: self.close())
        self._panel.focus_set()

    # ---- Header ----

    def _build_header(self):
        t = get_theme()
        header = tk.Frame(self._panel, bg=self.BG_DARK)
        header.pack(fill="x", padx=20, pady=(15, 0))

        title_left = tk.Frame(header, bg=self.BG_DARK)
        title_left.pack(side="left")

        # Avatar circle com iniciais "VA"
        avatar_size = 36
        avatar_canvas = tk.Canvas(title_left, width=avatar_size, height=avatar_size,
                                   bg=self.BG_DARK, highlightthickness=0)
        avatar_canvas.pack(side="left", padx=(0, 10))
        avatar_canvas.create_oval(2, 2, avatar_size - 2, avatar_size - 2,
                                   fill=t["accent"], outline="")
        avatar_canvas.create_text(avatar_size // 2, avatar_size // 2,
                                   text="VA", font=("Segoe UI", 11, "bold"),
                                   fill=t["white"])

        title_texts = tk.Frame(title_left, bg=self.BG_DARK)
        title_texts.pack(side="left")

        title_label = tk.Label(title_texts, text="Voice AI",
                 font=("Segoe UI", 13, "bold"),
                 fg=self.TEXT_PRIMARY, bg=self.BG_DARK)
        title_label.pack(anchor="w")

        self._subtitle_label = tk.Label(
            title_texts,
            text=f"{self._hotkey_str}  \u2022  {self._status_text}",
            font=("Segoe UI", 9), fg=self.TEXT_SECONDARY, bg=self.BG_DARK)
        self._subtitle_label.pack(anchor="w")

        # Drag bindings
        for w in (header, title_left, avatar_canvas, title_texts, title_label, self._subtitle_label):
            w.bind("<ButtonPress-1>", self._start_drag)
            w.bind("<B1-Motion>", self._on_drag)

        # Botao fechar
        close_btn = tk.Label(header, text="\u2715", font=("Segoe UI", 13),
                             fg=self.TEXT_SECONDARY, bg=self.BG_DARK, cursor="hand2")
        close_btn.pack(side="right", pady=(0, 5))
        close_btn.bind("<Enter>", lambda e: close_btn.config(fg=t["error"]))
        close_btn.bind("<Leave>", lambda e: close_btn.config(fg=self.TEXT_SECONDARY))
        close_btn.bind("<Button-1>", lambda e: self.close())

        # Engrenagem
        gear = tk.Label(header, text="\u2699", font=("Segoe UI", 13),
                        fg=self.TEXT_SECONDARY, bg=self.BG_DARK, cursor="hand2")
        gear.pack(side="right", padx=(0, 8), pady=(0, 5))
        gear.bind("<Enter>", lambda e: gear.config(fg=self.ACCENT))
        gear.bind("<Leave>", lambda e: gear.config(fg=self.TEXT_SECONDARY))
        gear.bind("<Button-1>", lambda e: self._open_settings())

        # Botão Modo Agente
        agent_btn = tk.Label(header, text="Agente", font=("Segoe UI", 9, "bold"),
                             fg=self.TEXT_SECONDARY, bg=self.BG_DARK, cursor="hand2",
                             padx=6, pady=2)
        agent_btn.pack(side="right", padx=(0, 8), pady=(0, 5))
        agent_btn.bind("<Enter>", lambda e: agent_btn.config(fg=t["info"], bg=t["bg_tertiary"]))
        agent_btn.bind("<Leave>", lambda e: agent_btn.config(fg=self.TEXT_SECONDARY, bg=self.BG_DARK))
        agent_btn.bind("<Button-1>", lambda e: self._open_voice_agent())

        # Botão info (v3.3.5)
        info_btn = tk.Label(header, text="\u2139", font=("Segoe UI", 13),
                            fg=self.TEXT_SECONDARY, bg=self.BG_DARK, cursor="hand2")
        info_btn.pack(side="right", padx=(0, 8), pady=(0, 5))
        info_btn.bind("<Enter>", lambda e: info_btn.config(fg=self.ACCENT))
        info_btn.bind("<Leave>", lambda e: info_btn.config(fg=self.TEXT_SECONDARY))
        info_btn.bind("<Button-1>", lambda e: self._open_info())

        tk.Frame(self._panel, bg=self.ACCENT, height=1).pack(
            fill="x", padx=20, pady=(10, 0))

    # ---- Search + Cards (scrollable) ----

    def _build_cards_area(self, animate=False):
        # Barra de busca
        search_frame = tk.Frame(self._panel, bg=self.BG_DARK)
        search_frame.pack(fill="x", padx=20, pady=(8, 0))

        self._search_var = tk.StringVar()
        search_entry = tk.Entry(
            search_frame, textvariable=self._search_var,
            font=("Segoe UI", 10), bg=self.BG_CARD, fg=self.TEXT_PRIMARY,
            insertbackground=self.ACCENT, relief="flat",
            bd=0, highlightthickness=1,
            highlightbackground=self.BORDER_CARD, highlightcolor=self.ACCENT)
        search_entry.pack(side="left", fill="x", expand=True, ipady=5)

        self._search_var.trace_add("write", lambda *_: self._filter_cards())

        # Bot\u00e3o exportar TXT
        export_btn = tk.Label(
            search_frame, text="TXT", font=("Segoe UI", 9, "bold"),
            bg=self.BG_CARD_HOVER, fg=self.TEXT_SECONDARY, cursor="hand2",
            padx=8, pady=3)
        export_btn.pack(side="right", padx=(6, 0))
        export_btn.bind("<Enter>", lambda e: export_btn.config(fg=self.ACCENT))
        export_btn.bind("<Leave>", lambda e: export_btn.config(fg=self.TEXT_SECONDARY))
        export_btn.bind("<Button-1>", lambda e: self._export_txt())

        # Placeholder text
        self._search_placeholder = tk.Label(
            search_frame, text="Buscar transcri\u00e7\u00f5es...",
            font=("Segoe UI", 10), fg=self.TEXT_DIM, bg=self.BG_CARD,
            cursor="xterm")
        self._search_placeholder.place(x=6, rely=0.5, anchor="w")

        def _update_placeholder(*_):
            if self._search_var.get():
                self._search_placeholder.place_forget()
            else:
                self._search_placeholder.place(x=6, rely=0.5, anchor="w")
        self._search_var.trace_add("write", _update_placeholder)
        self._search_placeholder.bind("<Button-1>", lambda e: search_entry.focus_set())

        # Cards container
        container = tk.Frame(self._panel, bg=self.BG_DARK)
        container.pack(fill="both", expand=True, padx=15, pady=(8, 0))

        canvas = tk.Canvas(container, bg=self.BG_DARK, highlightthickness=0)
        scrollbar = tk.Scrollbar(container, orient="vertical", command=canvas.yview)
        self._scroll_inner = tk.Frame(canvas, bg=self.BG_DARK)

        self._scroll_inner.bind(
            "<Configure>",
            lambda e: canvas.configure(scrollregion=canvas.bbox("all")))
        canvas.create_window((0, 0), window=self._scroll_inner, anchor="nw",
                             width=self.PW - 50)
        canvas.configure(yscrollcommand=scrollbar.set)

        scrollbar.pack(side="right", fill="y")
        canvas.pack(fill="both", expand=True)

        def _on_mousewheel(event):
            canvas.yview_scroll(int(-1 * (event.delta / 120)), "units")
        canvas.bind_all("<MouseWheel>", _on_mousewheel)
        self._cards_canvas = canvas

        self._render_cards(animate=animate)

    def _render_cards(self, filter_text="", animate=False):
        for w in self._scroll_inner.winfo_children():
            w.destroy()
        self._expanded_cards = set()
        items = self.history.get_all()
        if filter_text:
            ft = filter_text.lower()
            items = [e for e in items if ft in e.get("text", "").lower()]
        # Fixados ao topo (Feature 4)
        items = sorted(items, key=lambda e: (0 if e.get("pinned") else 1))
        if items:
            for i, entry in enumerate(items):
                card = self._create_card(entry)
                if animate:
                    # Fade-in staggered
                    self._schedule_card_fade(card, delay_ms=i * 50)
        else:
            msg = "Nenhum resultado." if filter_text else "Nenhuma transcri\u00e7\u00e3o ainda.\nUse o atalho para gravar."
            tk.Label(self._scroll_inner,
                     text=msg,
                     font=("Segoe UI", 10), fg=self.TEXT_SECONDARY,
                     bg=self.BG_DARK, justify="center").pack(pady=60)

    def _schedule_card_fade(self, card, delay_ms=0):
        """Animação de fade-in do card (bg interpolation)."""
        t = get_theme()
        bg_start = t["bg_primary"]
        bg_end = t["bg_secondary"]
        steps = 6
        interval = 25

        # Inicia invisível
        card.config(highlightbackground=t["bg_primary"])
        for w in card.winfo_children():
            try:
                w.config(bg=bg_start)
                for child in w.winfo_children():
                    try:
                        child.config(bg=bg_start)
                    except Exception:
                        pass
            except Exception:
                pass

        def _fade(step):
            if not card.winfo_exists():
                return
            ratio = step / steps
            color = interpolate_color(bg_start, bg_end, ratio)
            border_color = interpolate_color(bg_start, t["border"], ratio)
            card.config(bg=color, highlightbackground=border_color)
            for w in card.winfo_children():
                try:
                    w.config(bg=color)
                    for child in w.winfo_children():
                        try:
                            child.config(bg=color)
                        except Exception:
                            pass
                except Exception:
                    pass
            if step < steps:
                self.root.after(interval, lambda: _fade(step + 1))

        self.root.after(delay_ms, lambda: _fade(0))

    def _filter_cards(self):
        if not self._panel or not self._panel.winfo_exists():
            return
        self._render_cards(self._search_var.get())

    def _export_txt(self):
        try:
            path = self.history.export_txt()
            import subprocess as _sp
            _sp.Popen(["explorer", "/select,", path])
        except Exception as e:
            log(f"Export TXT error: {e}")

    def refresh(self):
        """Atualiza os cards se o painel estiver aberto."""
        if not self._panel or not self._panel.winfo_exists():
            return
        filter_text = self._search_var.get() if hasattr(self, '_search_var') else ""
        self._render_cards(filter_text)

    def _create_card(self, entry):
        full_text = entry["text"]
        is_long = len(full_text) > 120
        entry_id = id(entry)
        is_pinned = entry.get("pinned", False)

        # v3.8.0: cards fixados recebem borda accent e fundo levemente diferenciado
        card_border = self.ACCENT if is_pinned else self.BORDER_CARD
        card_bg = get_theme().get("bg_card_pinned", self.BG_CARD) if is_pinned else self.BG_CARD

        card = tk.Frame(self._scroll_inner, bg=card_bg,
                        highlightbackground=card_border,
                        highlightthickness=2 if is_pinned else 1, cursor="hand2")
        card.pack(fill="x", pady=4, padx=5)

        inner = tk.Frame(card, bg=card_bg)
        inner.pack(fill="x", padx=14, pady=10)

        # Timestamp + contagem de palavras
        date_str = entry.get('date_str', entry.get('time_str', ''))
        word_count = len(full_text.split())
        ts_text = f"[{date_str}]  \u2022  {word_count} palavras"
        # v3.8.0: indicador de pin no timestamp
        pin_indicator = "  ★" if is_pinned else ""
        ts = tk.Label(inner, text=ts_text + pin_indicator,
                      font=("Consolas", 8), fg=self.ACCENT if is_pinned else self.TEXT_DIM,
                      bg=card_bg, anchor="w")
        ts.pack(anchor="w")

        # Texto (colapsado se longo)
        expanded = entry_id in getattr(self, '_expanded_cards', set())
        if is_long and not expanded:
            display_text = full_text[:117] + "..."
        else:
            display_text = full_text

        txt = tk.Label(inner, text=display_text,
                       font=("Segoe UI", 10), fg=self.TEXT_PRIMARY,
                       bg=card_bg, anchor="w", justify="left",
                       wraplength=self.PW - 130)
        txt.pack(fill="x", pady=(4, 0))

        # Indicador expandir/recolher
        expand_label = None
        if is_long:
            expand_text = "\u25b4 Recolher" if expanded else "\u25be Expandir"
            expand_label = tk.Label(inner, text=expand_text,
                                     font=("Segoe UI", 8), fg=self.TEXT_DIM,
                                     bg=card_bg, cursor="hand2", anchor="e")
            expand_label.pack(anchor="e", pady=(2, 0))

            def _toggle_expand(e, eid=entry_id, et=entry):
                if not hasattr(self, '_expanded_cards'):
                    self._expanded_cards = set()
                if eid in self._expanded_cards:
                    self._expanded_cards.discard(eid)
                else:
                    self._expanded_cards.add(eid)
                # Re-render mantendo filtro
                filter_text = self._search_var.get() if hasattr(self, '_search_var') else ""
                self._render_cards(filter_text)

            expand_label.bind("<Button-1>", _toggle_expand)

        # Botão ⋯ (menu contextual) — canto superior direito, visível no hover
        audio_path = entry.get("audio_path")
        has_audio = bool(audio_path and os.path.exists(audio_path))

        btn_frame = tk.Frame(card, bg=card_bg)
        hover_bg = get_theme()["bg_tertiary"]
        menu_btn = tk.Label(
            btn_frame, text="\u22ef", font=("Segoe UI", 13),
            bg=hover_bg, fg=self.TEXT_PRIMARY,
            cursor="hand2", padx=8, pady=2, relief="flat"
        )
        menu_btn.pack(side="left")

        def _show_menu(e, ent=entry, tx=full_text, ap=audio_path, ha=has_audio):
            t = get_theme()
            m = tk.Menu(self._panel, tearoff=0,
                        bg=t["bg_secondary"], fg=t["text_primary"],
                        activebackground=t["accent"], activeforeground="#ffffff",
                        relief="flat", bd=0)
            m.add_command(label="\U0001f4cb  Copiar", command=lambda: self._card_copy_only(tx))
            if ha:
                # Verifica se está tocando
                playing = getattr(self, "_playing_path", None) == ap
                lbl_play = "\u23f9  Parar" if playing else "\u25b6  Ouvir"
                m.add_command(label=lbl_play, command=lambda: self._card_toggle_audio(ap))
                m.add_command(label="\u2b07  Baixar áudio", command=lambda: self._card_download_audio(ap))
            # Opções de estilo se há API key configurada
            orig = ent.get("original_text")
            styled = orig and ent.get("text") != orig
            has_key = bool(getattr(self, "_config", None) and self._config.api_key)
            if orig and styled:
                m.add_separator()
                m.add_command(label="\U0001f4c4  Ver original",
                              command=lambda: self._card_show_original(ent))
            if has_key:
                if not m.index("end") or m.type(m.index("end")) != "separator":
                    m.add_separator()
                m.add_command(label="\u2728  Aplicar estilo",
                              command=lambda: self._card_apply_style(ent))
            m.add_separator()
            pin_lbl = "\u2605  Desfixar" if ent.get("pinned") else "\u2606  Fixar"
            m.add_command(label=pin_lbl, command=lambda: self._card_toggle_pin(ent))
            m.add_separator()
            m.add_command(label="\u2715  Deletar",
                          command=lambda c=card: self._card_delete(ent, c))
            try:
                m.tk_popup(e.x_root, e.y_root)
            finally:
                m.grab_release()

        menu_btn.bind("<Button-1>", _show_menu)

        # Indicador de pin
        if is_pinned:
            pin_lbl = tk.Label(btn_frame, text="\u2605", font=("Segoe UI", 9),
                               bg=hover_bg, fg="#f0a500", cursor="hand2",
                               padx=2, pady=2)
            pin_lbl.pack(side="left")

        all_widgets = [card, inner, ts, txt]
        if expand_label:
            all_widgets.append(expand_label)

        def on_enter(e, c=card, i=inner, t_w=ts, x=txt, bf=btn_frame, ex=expand_label,
                     cbg=card_bg, cborder=card_border):
            self._card_hover(c, i, t_w, x, True, expand_label=ex, base_bg=cbg, base_border=cborder)
            bf.place(relx=1.0, rely=0.0, anchor="ne", x=-6, y=6)
            bf.lift()

        def on_leave(e, c=card, i=inner, t_w=ts, x=txt, bf=btn_frame, ex=expand_label,
                     cbg=card_bg, cborder=card_border):
            mx = c.winfo_pointerx()
            my = c.winfo_pointery()
            cx1, cy1 = c.winfo_rootx(), c.winfo_rooty()
            cx2 = cx1 + c.winfo_width()
            cy2 = cy1 + c.winfo_height()
            if cx1 <= mx <= cx2 and cy1 <= my <= cy2:
                return
            self._card_hover(c, i, t_w, x, False, expand_label=ex, base_bg=cbg, base_border=cborder)
            bf.place_forget()

        for w in all_widgets:
            w.bind("<Enter>", on_enter)
            w.bind("<Leave>", on_leave)
            w.bind("<Button-1>",
                   lambda e, text=full_text, c=card: self._card_click(text, c))

        menu_btn.bind("<Enter>", on_enter)
        menu_btn.bind("<Leave>", on_leave)

        return card

    def _card_hover(self, card, inner, ts, txt, entering, expand_label=None,
                    base_bg=None, base_border=None):
        if base_bg is None:
            base_bg = self.BG_CARD
        if base_border is None:
            base_border = self.BORDER_CARD
        if entering:
            bg = self.BG_CARD_HOVER
            card.config(highlightbackground=self.ACCENT, bg=bg)
        else:
            bg = base_bg
            card.config(highlightbackground=base_border, bg=bg)
        widgets = [inner, ts, txt]
        if expand_label:
            widgets.append(expand_label)
        for w in widgets:
            w.config(bg=bg)

    def _card_copy_only(self, text):
        """Copia para área de transferência sem colar nem fechar o painel."""
        pyperclip.copy(text)
        self._show_copied_toast()

    def _card_delete(self, entry, card):
        """Remove o card da história."""
        self.history.remove(entry)
        card.destroy()

    def _card_toggle_audio(self, path):
        """Toca ou para o áudio do card (toggle play/stop)."""
        if getattr(self, "_playing_path", None) == path:
            # Parar reprodução
            try:
                sd.stop()
            except Exception:
                pass
            self._playing_path = None
            return
        # Parar eventual áudio anterior
        try:
            sd.stop()
        except Exception:
            pass
        self._playing_path = path

        def _play():
            try:
                import wave as _wave
                import numpy as _np
                with _wave.open(path, "rb") as wf:
                    frames = wf.readframes(wf.getnframes())
                    rate = wf.getframerate()
                    data = _np.frombuffer(frames, dtype=_np.int16)
                sd.play(data, rate)
                sd.wait()
            except Exception as e:
                log(f"Play audio error: {e}")
            finally:
                if getattr(self, "_playing_path", None) == path:
                    self._playing_path = None
        threading.Thread(target=_play, daemon=True).start()

    def _card_download_audio(self, path):
        """Abre diálogo para salvar o áudio em local escolhido pelo usuário (v3.2.0)."""
        import shutil
        from tkinter import filedialog
        default_name = os.path.basename(path)
        dest = filedialog.asksaveasfilename(
            defaultextension=".wav",
            filetypes=[("WAV audio", "*.wav")],
            initialfile=default_name,
            title="Salvar áudio",
        )
        if dest:
            try:
                shutil.copy2(path, dest)
                self._show_saved_toast()
            except Exception as e:
                log(f"Download audio error: {e}")

    def _card_apply_style(self, entry):
        """Aplica estilo GPT ao texto do card e atualiza histórico."""
        if not hasattr(self, "_config") or not self._config.api_key:
            return
        text = entry.get("text", "")
        if not text:
            return
        # Salva original antes de aplicar estilo (se ainda não salvo)
        if not entry.get("original_text"):
            entry["original_text"] = text

        def _do():
            styled = apply_text_style(text, self._config.text_style or "correction",
                                      self._config.api_key, self._config.gpt_model)
            entry["text"] = styled
            entry["preview"] = styled[:60] + ("..." if len(styled) > 60 else "")
            self.history._save()
            # Só faz refresh se o painel ainda está aberto
            if self._panel and self._panel.winfo_exists():
                self.root.after(0, self.refresh)
        threading.Thread(target=_do, daemon=True).start()

    def _card_show_original(self, entry):
        """Alterna entre texto original e revisado no card."""
        orig = entry.get("original_text")
        current = entry.get("text", "")
        if not orig:
            return
        if current != orig:
            # Mostrar original
            entry["_styled_text"] = current
            entry["text"] = orig
            entry["preview"] = orig[:60] + ("..." if len(orig) > 60 else "")
        else:
            # Restaurar revisado
            styled = entry.get("_styled_text", orig)
            entry["text"] = styled
            entry["preview"] = styled[:60] + ("..." if len(styled) > 60 else "")
        self.history._save()
        self.refresh()

    def _card_toggle_pin(self, entry):
        """Fixa ou desfixar um card no topo do histórico."""
        entry["pinned"] = not entry.get("pinned", False)
        self.history._save()
        self.refresh()

    def _show_saved_toast(self):
        toast = tk.Label(self._panel, text="\u2713 Salvo!",
                         font=("Segoe UI", 10, "bold"),
                         fg=self.SUCCESS, bg=self.BG_DARK)
        toast.place(relx=0.5, rely=0.5, anchor="center")
        self.root.after(350, toast.destroy)

    def _card_click(self, text, card):
        card.config(highlightbackground=self.SUCCESS)
        self._show_copied_toast()
        self.root.after(400, lambda: self._do_paste(text))

    def _show_copied_toast(self):
        toast = tk.Label(self._panel, text="\u2713 Copiado!",
                         font=("Segoe UI", 10, "bold"),
                         fg=self.SUCCESS, bg=self.BG_DARK)
        toast.place(relx=0.5, rely=0.5, anchor="center")
        self.root.after(350, toast.destroy)

    def _do_paste(self, text):
        self.close()
        self.root.after(100, lambda: self._paste_to_previous(text))

    def _paste_to_previous(self, text):
        if self._prev_hwnd:
            self.paster.set_foreground_window(self._prev_hwnd)
            time.sleep(0.1)
        self.paster.paste(text)

    # ---- Footer ----

    def _build_footer(self):
        sep = tk.Frame(self._panel, bg=self.BORDER_CARD, height=1)
        sep.pack(fill="x", padx=20, pady=(5, 0))

        footer = tk.Frame(self._panel, bg=self.BG_DARK)
        footer.pack(fill="x", padx=20, pady=(6, 12))

        tk.Label(footer, text="ESC para fechar  \u00b7  Clique para colar  \u00b7  \u22ef menu de ações",
                 font=("Segoe UI", 8), fg=self.TEXT_DIM,
                 bg=self.BG_DARK).pack(side="left")

        link = tk.Label(footer, text="Desenvolvido por Expert Integrado",
                       font=("Segoe UI", 8, "italic"), fg=self.TEXT_SECONDARY,
                       bg=self.BG_DARK, cursor="hand2")
        link.pack(side="right")
        link.bind("<Enter>", lambda e: link.config(fg=self.ACCENT))
        link.bind("<Leave>", lambda e: link.config(fg=self.TEXT_SECONDARY))
        link.bind("<Button-1>", lambda e: webbrowser.open("https://expertintegrado.com.br"))

    # ---- Drag ----

    def _start_drag(self, event):
        self._drag_x = event.x
        self._drag_y = event.y

    def _on_drag(self, event):
        x = self._panel.winfo_x() + (event.x - self._drag_x)
        y = self._panel.winfo_y() + (event.y - self._drag_y)
        self._panel.geometry(f"+{x}+{y}")

    # ---- Settings / Close ----

    def _open_settings(self):
        # v3.3.4: abre SettingsDialog com abas (igual ao menu do tray)
        if self._on_settings:
            self._on_settings()

    def _open_voice_agent(self):
        """Abre o Voice Agent como processo separado (sem console)."""
        import subprocess as _sp
        import shutil
        agent_path = os.path.join(script_dir, "voice_agent.py")
        if not os.path.exists(agent_path):
            log(f"Voice Agent não encontrado: {agent_path}")
            return
        # Usar pythonw (sem console) — fallback para python se não encontrar
        if getattr(sys, "frozen", False):
            python = (shutil.which("pythonw") or shutil.which("python")
                      or shutil.which("python3") or shutil.which("py"))
            if not python:
                log("Voice Agent: Python não encontrado no PATH (frozen mode)")
                return
        else:
            # Trocar python.exe por pythonw.exe se existir
            pythonw = sys.executable.replace("python.exe", "pythonw.exe")
            python = pythonw if os.path.exists(pythonw) else sys.executable
        try:
            log(f"Voice Agent launching: {python} {agent_path}")
            _sp.Popen(
                [python, agent_path],
                cwd=script_dir,
                stdout=_sp.DEVNULL, stderr=_sp.DEVNULL,
                creationflags=0x00000008,  # DETACHED_PROCESS
            )
            log("Voice Agent launched from MainPanel")
        except Exception as e:
            log(f"Voice Agent launch error: {e}")

    def _open_info(self):
        """Abre popup com FAQ — posicionado acima do cards_area do painel (v4.1.0)."""
        t = get_theme()
        W, H = 520, 520
        win = tk.Toplevel(self._panel)
        win.title("Voice AI — Ajuda")
        win.configure(bg=t["bg_primary"])
        win.resizable(False, True)
        win.attributes("-topmost", True)

        # Posiciona acima do painel de histórico (logo por cima do cards_area)
        try:
            px = self._panel.winfo_x()
            py = self._panel.winfo_y()
            pw = self._panel.winfo_width()
            # Alinha horizontalmente com o painel; empilha verticalmente acima do painel
            wx = px + (pw - W) // 2
            wy = max(0, py - H - 8)  # 8px de margem acima do painel
            win.geometry(f"{W}x{H}+{wx}+{wy}")
        except Exception:
            sx = (win.winfo_screenwidth() - W) // 2
            sy = (win.winfo_screenheight() - H) // 2
            win.geometry(f"{W}x{H}+{sx}+{sy}")

        tk.Label(win, text="Voice AI — Como usar", font=("Segoe UI", 13, "bold"),
                 fg=t["text_primary"], bg=t["bg_primary"]).pack(pady=(16, 4))
        frame = tk.Frame(win, bg=t["bg_primary"])
        frame.pack(fill="both", expand=True, padx=16, pady=(0, 16))
        sb = tk.Scrollbar(frame)
        sb.pack(side="right", fill="y")
        txt = tk.Text(frame, wrap="word", font=("Segoe UI", 10),
                      fg=t["text_primary"], bg=t["bg_secondary"],
                      relief="flat", yscrollcommand=sb.set,
                      padx=12, pady=8, cursor="arrow")
        txt.pack(fill="both", expand=True)
        sb.config(command=txt.yview)
        txt.insert("end", FAQ_TEXT)
        txt.config(state="disabled")
        # v4.1.0: bloqueia propagação do scroll do popup para a janela pai (cards_area)
        txt.bind("<MouseWheel>", lambda e: "break")

    def _show_settings_view(self):
        """Mostra configurações inline no painel (substitui o conteúdo)."""
        if not self._panel or not self._panel.winfo_exists():
            return
        if not self._config:
            return

        # Limpa todo o conteúdo do painel
        for w in self._panel.winfo_children():
            w.destroy()

        t = get_theme()
        bg = t["bg_primary"]
        fg = t["text_primary"]
        label_fg = t["text_secondary"]
        entry_bg = t["bg_secondary"]
        accent = t["accent"]
        btn_bg = t["bg_tertiary"]
        btn_fg = t["text_secondary"]
        dim = t["text_dim"]

        # Header: ← Voltar ... Configurações
        header = tk.Frame(self._panel, bg=bg)
        header.pack(fill="x", padx=20, pady=(15, 0))

        back_btn = tk.Label(header, text="\u2190 Voltar", font=("Segoe UI", 11),
                            fg=label_fg, bg=bg, cursor="hand2")
        back_btn.pack(side="left")
        back_btn.bind("<Enter>", lambda e: back_btn.config(fg=accent))
        back_btn.bind("<Leave>", lambda e: back_btn.config(fg=label_fg))
        back_btn.bind("<Button-1>", lambda e: self._show_history_view())

        tk.Label(header, text="Configura\u00e7\u00f5es", font=("Segoe UI", 13, "bold"),
                 fg=fg, bg=bg).pack(side="right")

        tk.Frame(self._panel, bg=accent, height=1).pack(
            fill="x", padx=20, pady=(10, 0))

        # Scroll container para o formulário
        container = tk.Frame(self._panel, bg=bg)
        container.pack(fill="both", expand=True, padx=15, pady=(10, 0))

        canvas = tk.Canvas(container, bg=bg, highlightthickness=0)
        scroll_inner = tk.Frame(canvas, bg=bg)
        scroll_inner.bind("<Configure>",
                          lambda e: canvas.configure(scrollregion=canvas.bbox("all")))
        canvas.create_window((0, 0), window=scroll_inner, anchor="nw",
                             width=self.PW - 50)
        canvas.pack(side="left", fill="both", expand=True)

        def _on_mousewheel(event):
            canvas.yview_scroll(int(-1 * (event.delta / 120)), "units")
        canvas.bind_all("<MouseWheel>", _on_mousewheel)

        form = tk.Frame(scroll_inner, bg=bg)
        form.pack(fill="x", padx=15, pady=(5, 10))

        # API Key
        tk.Label(form, text="API Key OpenAI:", font=("Segoe UI", 10),
                 fg=label_fg, bg=bg, anchor="w").pack(fill="x", pady=(0, 3))

        key_frame = tk.Frame(form, bg=bg)
        key_frame.pack(fill="x", pady=(0, 12))

        key_var = tk.StringVar(value=self._config.data["api_key"])
        key_entry = tk.Entry(key_frame, textvariable=key_var,
                             font=("Consolas", 10), bg=entry_bg, fg=fg,
                             insertbackground=accent, relief="flat", show="*",
                             bd=0, highlightthickness=1,
                             highlightbackground=btn_bg, highlightcolor=accent)
        key_entry.pack(side="left", fill="x", expand=True, ipady=4)

        show_key = [False]
        def _toggle_key():
            show_key[0] = not show_key[0]
            key_entry.config(show="" if show_key[0] else "*")
            toggle_btn.config(text="Ocultar" if show_key[0] else "Mostrar")
        toggle_btn = tk.Button(key_frame, text="Mostrar", font=("Segoe UI", 8),
                               bg=btn_bg, fg=btn_fg, relief="flat", cursor="hand2",
                               command=_toggle_key)
        toggle_btn.pack(side="right", padx=(5, 0))

        # Atalho
        tk.Label(form, text="Atalho (clique para alterar):", font=("Segoe UI", 10),
                 fg=label_fg, bg=bg, anchor="w").pack(fill="x", pady=(0, 3))

        hotkey_var = tk.StringVar(value=self._config.data["hotkey"])
        hotkey_capturing = [False]
        hotkey_label = tk.Label(form, textvariable=hotkey_var,
                                font=("Segoe UI", 11), fg=fg, bg=entry_bg,
                                relief="flat", anchor="center", cursor="hand2",
                                highlightthickness=1, highlightbackground=btn_bg,
                                highlightcolor=accent)
        hotkey_label.pack(fill="x", pady=(0, 12), ipady=6)

        def _start_capture():
            if hotkey_capturing[0]:
                return
            hotkey_capturing[0] = True
            hotkey_label.config(fg=accent, highlightbackground=accent)
            hotkey_var.set("Pressione o atalho...")
            hotkey_label.focus_set()
            hotkey_label.bind("<KeyPress>", _on_key)
            hotkey_label.bind("<Escape>", lambda e: _cancel_capture())
            hotkey_label.bind("<FocusOut>", lambda e: _cancel_capture())

        def _on_key(event):
            IGNORE = {"Shift_L", "Shift_R", "Control_L", "Control_R", "Alt_L", "Alt_R"}
            if event.keysym in IGNORE:
                return
            parts = []
            if event.state & 0x4: parts.append("Ctrl")
            if event.state & 0x1: parts.append("Shift")
            if event.state & 0x20000: parts.append("Alt")
            if not parts:
                return
            key = event.keysym
            if len(key) == 1 and key.isalpha():
                key = key.upper()
            parts.append(key)
            hotkey_var.set("+".join(parts))
            _finish_capture()

        def _cancel_capture():
            if not hotkey_capturing[0]:
                return
            hotkey_var.set(self._config.data["hotkey"])
            _finish_capture()

        def _finish_capture():
            hotkey_capturing[0] = False
            hotkey_label.config(fg=fg, highlightbackground=btn_bg)
            hotkey_label.unbind("<KeyPress>")
            hotkey_label.unbind("<Escape>")
            hotkey_label.unbind("<FocusOut>")

        hotkey_label.bind("<Button-1>", lambda e: _start_capture())

        # Modo de gravação
        tk.Label(form, text="Modo de gravação:", font=("Segoe UI", 10),
                 fg=label_fg, bg=bg, anchor="w").pack(fill="x", pady=(0, 3))

        recmode_labels = [lbl for lbl, _ in RECORDING_MODE_OPTIONS]
        recmode_codes = [code for _, code in RECORDING_MODE_OPTIONS]
        current_recmode = self._config.data.get("recording_mode", "toggle")
        rm_idx = recmode_codes.index(current_recmode) if current_recmode in recmode_codes else 0
        recmode_var = tk.StringVar(value=recmode_labels[rm_idx])
        ttk.Combobox(form, textvariable=recmode_var,
                     values=recmode_labels, state="readonly",
                     font=("Segoe UI", 10)).pack(fill="x", pady=(0, 12), ipady=4)

        # Idioma
        tk.Label(form, text="Idioma da transcri\u00e7\u00e3o:", font=("Segoe UI", 10),
                 fg=label_fg, bg=bg, anchor="w").pack(fill="x", pady=(0, 3))

        lang_labels = [lbl for lbl, _ in LANGUAGE_OPTIONS]
        lang_codes = [code for _, code in LANGUAGE_OPTIONS]
        current_lang = self._config.data["language"]
        lang_idx = lang_codes.index(current_lang) if current_lang in lang_codes else 0

        lang_var = tk.StringVar(value=lang_labels[lang_idx])

        # Combobox style
        style = ttk.Style()
        style.theme_use("clam")
        style.configure("TCombobox",
                         fieldbackground=entry_bg, background=btn_bg,
                         foreground=fg, arrowcolor=accent, borderwidth=0)
        style.map("TCombobox",
                  fieldbackground=[("readonly", entry_bg), ("disabled", bg)],
                  foreground=[("readonly", fg), ("disabled", dim)],
                  selectbackground=[("readonly", entry_bg)],
                  selectforeground=[("readonly", fg)])

        lang_combo = ttk.Combobox(form, textvariable=lang_var,
                                   values=lang_labels, state="readonly",
                                   font=("Segoe UI", 10))
        lang_combo.pack(fill="x", pady=(0, 12), ipady=4)

        # Modelo de transcri\u00e7\u00e3o
        tk.Label(form, text="Modelo:", font=("Segoe UI", 10),
                 fg=label_fg, bg=bg, anchor="w").pack(fill="x", pady=(0, 3))

        model_labels = [lbl for lbl, _ in MODEL_OPTIONS]
        model_codes = [code for _, code in MODEL_OPTIONS]
        current_model = self._config.data.get("model", "whisper-1")
        m_idx = model_codes.index(current_model) if current_model in model_codes else 0
        model_var = tk.StringVar(value=model_labels[m_idx])
        model_combo = ttk.Combobox(form, textvariable=model_var,
                                    values=model_labels, state="readonly",
                                    font=("Segoe UI", 10))
        model_combo.pack(fill="x", pady=(0, 8), ipady=4)

        # Toggles
        toggles_row = tk.Frame(form, bg=bg)
        toggles_row.pack(fill="x", pady=(0, 5))

        auto_paste_var = tk.BooleanVar(value=self._config.data.get("auto_paste", True))
        tk.Checkbutton(toggles_row, text="Auto-colar",
                       variable=auto_paste_var, font=("Segoe UI", 10),
                       fg=fg, bg=bg, selectcolor=entry_bg, activebackground=bg,
                       activeforeground=fg, anchor="w"
                       ).pack(side="left")

        auto_start_var = tk.BooleanVar(value=self._config.data.get("auto_start", True))
        tk.Checkbutton(toggles_row, text="Iniciar com Windows",
                       variable=auto_start_var, font=("Segoe UI", 10),
                       fg=fg, bg=bg, selectcolor=entry_bg, activebackground=bg,
                       activeforeground=fg, anchor="w"
                       ).pack(side="left", padx=(20, 0))

        edit_before_paste_var = tk.BooleanVar(value=self._config.data.get("edit_before_paste", False))
        tk.Checkbutton(toggles_row, text="Revisar antes",
                       variable=edit_before_paste_var, font=("Segoe UI", 10),
                       fg=fg, bg=bg, selectcolor=entry_bg, activebackground=bg,
                       activeforeground=fg, anchor="w"
                       ).pack(side="left", padx=(20, 0))

        toggles_row2 = tk.Frame(form, bg=bg)
        toggles_row2.pack(fill="x", pady=(0, 8))

        inline_cmd_var = tk.BooleanVar(value=self._config.data.get("inline_commands", True))
        tk.Checkbutton(toggles_row2, text="Comandos por voz",
                       variable=inline_cmd_var, font=("Segoe UI", 10),
                       fg=fg, bg=bg, selectcolor=entry_bg, activebackground=bg,
                       activeforeground=fg, anchor="w"
                       ).pack(side="left")

        # Limite de grava\u00e7\u00e3o
        tk.Label(toggles_row2, text="M\u00e1x (s):", font=("Segoe UI", 9),
                 fg=label_fg, bg=bg).pack(side="left", padx=(20, 4))
        max_rec_var = tk.StringVar(value=str(self._config.data.get("max_recording_secs", 120)))
        tk.Entry(toggles_row2, textvariable=max_rec_var,
                 font=("Consolas", 9), bg=entry_bg, fg=fg,
                 insertbackground=accent, relief="flat", width=5,
                 bd=0, highlightthickness=1,
                 highlightbackground=btn_bg, highlightcolor=accent
                 ).pack(side="left", ipady=2)

        # Microfone
        tk.Label(form, text="Microfone:", font=("Segoe UI", 10),
                 fg=label_fg, bg=bg, anchor="w").pack(fill="x", pady=(8, 3))

        mic_devices = get_input_devices()
        mic_names = ["Padrão do sistema"] + [name for name, _ in mic_devices]
        mic_indices = [None] + [idx for _, idx in mic_devices]
        current_mic = self._config.data.get("mic_device", None)
        mic_sel = 0
        if current_mic is not None and current_mic in mic_indices:
            mic_sel = mic_indices.index(current_mic)

        mic_var = tk.StringVar(value=mic_names[mic_sel])
        ttk.Combobox(form, textvariable=mic_var,
                     values=mic_names, state="readonly",
                     font=("Segoe UI", 10)).pack(fill="x", pady=(0, 8), ipady=4)

        # Estilo de texto
        tk.Label(form, text="Estilo de texto:", font=("Segoe UI", 10),
                 fg=label_fg, bg=bg, anchor="w").pack(fill="x", pady=(0, 3))

        style_labels = [lbl for lbl, _ in TEXT_STYLE_OPTIONS]
        style_codes = [code for _, code in TEXT_STYLE_OPTIONS]
        current_style = self._config.data.get("text_style", "none")
        s_idx = style_codes.index(current_style) if current_style in style_codes else 0
        style_var = tk.StringVar(value=style_labels[s_idx])
        ttk.Combobox(form, textvariable=style_var,
                     values=style_labels, state="readonly",
                     font=("Segoe UI", 10)).pack(fill="x", pady=(0, 5), ipady=4)

        auto_style_var = tk.BooleanVar(value=self._config.data.get("text_style_auto", False))
        tk.Checkbutton(form, text="Aplicar estilo automaticamente",
                       variable=auto_style_var, font=("Segoe UI", 10),
                       fg=fg, bg=bg, selectcolor=entry_bg, activebackground=bg,
                       activeforeground=fg, anchor="w"
                       ).pack(fill="x", pady=(0, 5))

        # Modelo GPT (estilos)
        tk.Label(form, text="Modelo GPT:", font=("Segoe UI", 10),
                 fg=label_fg, bg=bg, anchor="w").pack(fill="x", pady=(0, 3))

        gpt_labels = [lbl for lbl, _ in GPT_MODEL_OPTIONS]
        gpt_codes = [code for _, code in GPT_MODEL_OPTIONS]
        current_gpt = self._config.data.get("gpt_model", "gpt-4.1-mini")
        g_idx = gpt_codes.index(current_gpt) if current_gpt in gpt_codes else 0
        gpt_var = tk.StringVar(value=gpt_labels[g_idx])
        ttk.Combobox(form, textvariable=gpt_var,
                     values=gpt_labels, state="readonly",
                     font=("Segoe UI", 10)).pack(fill="x", pady=(0, 8), ipady=4)

        # Tema
        tk.Label(form, text="Tema:", font=("Segoe UI", 10),
                 fg=label_fg, bg=bg, anchor="w").pack(fill="x", pady=(0, 3))

        theme_labels_inline = [lbl for lbl, _ in THEME_OPTIONS]
        theme_codes_inline = [code for _, code in THEME_OPTIONS]
        current_theme_inline = self._config.data.get("theme", "dark")
        th_idx = theme_codes_inline.index(current_theme_inline) if current_theme_inline in theme_codes_inline else 0
        theme_var = tk.StringVar(value=theme_labels_inline[th_idx])
        ttk.Combobox(form, textvariable=theme_var,
                     values=theme_labels_inline, state="readonly",
                     font=("Segoe UI", 10)).pack(fill="x", pady=(0, 8), ipady=4)

        # Uso mensal
        if self._usage:
            uso_frame = tk.Frame(form, bg=entry_bg, highlightbackground=btn_bg,
                                  highlightthickness=1)
            uso_frame.pack(fill="x", pady=(0, 15))

            tk.Label(uso_frame, text=f"Uso ({self._usage.month_label})",
                     font=("Segoe UI Semibold", 10),
                     fg=label_fg, bg=entry_bg).pack(anchor="w", padx=12, pady=(8, 4))

            mins = self._usage.total_seconds // 60
            secs = self._usage.total_seconds % 60
            uso_text = (f"{self._usage.total_requests} transcri\u00e7\u00f5es  \u00b7  "
                        f"{mins}min {secs}s  \u00b7  "
                        f"~R${self._usage.cost_brl:.2f}")
            tk.Label(uso_frame, text=uso_text, font=("Segoe UI", 9),
                     fg=dim, bg=entry_bg, justify="left"
                     ).pack(anchor="w", padx=12, pady=(0, 8))

        # Status label
        status_var = tk.StringVar()
        status_label = tk.Label(form, textvariable=status_var,
                                font=("Segoe UI", 9), fg=get_theme()["error"], bg=bg)
        status_label.pack(pady=(0, 5))

        # Botão Salvar
        def _save():
            api_key = key_var.get().strip()
            if not api_key:
                status_var.set("API Key \u00e9 obrigat\u00f3ria!")
                return
            if not api_key.startswith("sk-"):
                status_var.set("API Key deve come\u00e7ar com sk-")
                return

            lang_label = lang_var.get()
            l_idx = lang_labels.index(lang_label) if lang_label in lang_labels else 0
            lang_code = lang_codes[l_idx]

            m_label = model_var.get()
            mi = model_labels.index(m_label) if m_label in model_labels else 0
            m_code = model_codes[mi]

            try:
                max_rec = max(10, min(600, int(max_rec_var.get())))
            except ValueError:
                max_rec = 120

            # Microfone
            mic_lbl = mic_var.get()
            mic_li = mic_names.index(mic_lbl) if mic_lbl in mic_names else 0
            mic_dev = mic_indices[mic_li]

            # Estilo de texto
            st_lbl = style_var.get()
            st_i = style_labels.index(st_lbl) if st_lbl in style_labels else 0
            st_code = style_codes[st_i]

            # Modelo GPT
            gpt_lbl = gpt_var.get()
            gpt_i = gpt_labels.index(gpt_lbl) if gpt_lbl in gpt_labels else 0
            gpt_code = gpt_codes[gpt_i]

            # Modo de gravação
            rm_lbl = recmode_var.get()
            rm_i = recmode_labels.index(rm_lbl) if rm_lbl in recmode_labels else 0
            rm_code = recmode_codes[rm_i]

            # Tema
            th_lbl = theme_var.get()
            th_i = theme_labels_inline.index(th_lbl) if th_lbl in theme_labels_inline else 0
            th_code = theme_codes_inline[th_i]

            self._config.data["api_key"] = api_key
            self._config.data["hotkey"] = hotkey_var.get()
            self._config.data["language"] = lang_code
            self._config.data["model"] = m_code
            self._config.data["auto_paste"] = auto_paste_var.get()
            self._config.data["edit_before_paste"] = edit_before_paste_var.get()
            self._config.data["auto_start"] = auto_start_var.get()
            self._config.data["inline_commands"] = inline_cmd_var.get()
            self._config.data["max_recording_secs"] = max_rec
            self._config.data["mic_device"] = mic_dev
            self._config.data["text_style"] = st_code
            self._config.data["text_style_auto"] = auto_style_var.get()
            self._config.data["gpt_model"] = gpt_code
            self._config.data["recording_mode"] = rm_code
            self._config.data["theme"] = th_code
            self._config.save()
            log(f"Settings saved (inline): hotkey={self._config.hotkey}, lang={lang_code}, model={m_code}, style={st_code}, mode={rm_code}")

            if self._on_settings_saved:
                self._on_settings_saved()

            self._show_history_view()

        save_btn = tk.Button(form, text="Salvar", font=("Segoe UI", 11, "bold"),
                             bg=accent, fg="#ffffff", relief="flat", cursor="hand2",
                             padx=30, pady=6, command=_save)
        save_btn.pack(pady=(5, 15))
        accent_hover = "#1A8744"
        save_btn.bind("<Enter>", lambda e: save_btn.config(bg=accent_hover))
        save_btn.bind("<Leave>", lambda e: save_btn.config(bg=accent))

        self._panel.bind("<Escape>", lambda e: self._show_history_view())

    def _show_history_view(self):
        """Volta para a view de histórico (rebuild do painel no lugar)."""
        if not self._panel or not self._panel.winfo_exists():
            return

        # Salva posição atual
        geo = self._panel.geometry()

        # Limpa tudo
        for w in self._panel.winfo_children():
            w.destroy()

        # Reconstroi views
        self._build_header()
        self._build_cards_area()
        self._build_footer()

        # Restaura posição
        self._panel.geometry(geo)
        self._panel.bind("<Escape>", lambda e: self.close())
        self._panel.focus_set()

    def close(self):
        if self._panel and self._panel.winfo_exists():
            try:
                self._cards_canvas.unbind_all("<MouseWheel>")
            except Exception:
                pass
            self._panel.destroy()
            self._panel = None
            self._subtitle_label = None


# ─── HotkeyManager ──────────────────────────────────────────

class HotkeyManager:
    """
    Detecção dual de hotkey (configurável):
    1. RegisterHotKey (primário) — nível do SO, mais confiável (modo toggle)
    2. WH_KEYBOARD_LL — usado no modo push-to-talk para detectar key-up
    Alimentam a mesma queue.
    """

    def __init__(self, hotkey_queue, hotkey_str="Ctrl+Shift+Alt+F3",
                 on_conflict=None, recording_mode="toggle"):
        self._queue = hotkey_queue
        self._last_trigger = 0.0
        self._hotkey_str = hotkey_str
        self._mods, self._vk, self._has_ctrl, self._has_shift, self._has_alt = \
            ConfigManager.parse_hotkey(hotkey_str)
        self._threads = []
        self._reg_thread_id = None
        self._ll_thread_id = None
        self._on_conflict = on_conflict
        self._recording_mode = recording_mode

    def start(self):
        if self._recording_mode == "ptt":
            t = threading.Thread(target=self._ptt_hook_thread, daemon=True)
            t.start()
            self._threads.append(t)
            log(f"HotkeyManager started ({self._hotkey_str}): PTT via LL hook")
        else:
            t1 = threading.Thread(target=self._register_hotkey_thread, daemon=True)
            t1.start()
            self._threads.append(t1)
            log(f"HotkeyManager started ({self._hotkey_str}): RegisterHotKey only")

    def change(self, new_hotkey_str, new_mode=None):
        """Troca o hotkey e/ou modo ao vivo sem reiniciar o app."""
        mode_changed = new_mode is not None and new_mode != self._recording_mode
        hotkey_changed = new_hotkey_str != self._hotkey_str

        if not hotkey_changed and not mode_changed:
            return

        log(f"HotkeyManager.change: {self._hotkey_str} → {new_hotkey_str}, mode={self._recording_mode} → {new_mode or self._recording_mode}")

        # Encerra threads anteriores
        self._stop_threads()

        # Atualiza parâmetros
        self._hotkey_str = new_hotkey_str
        self._mods, self._vk, self._has_ctrl, self._has_shift, self._has_alt = \
            ConfigManager.parse_hotkey(new_hotkey_str)
        if new_mode is not None:
            self._recording_mode = new_mode

        # Inicia novas threads
        self.start()

    def _stop_threads(self):
        """Para todas as threads ativas (RegisterHotKey e/ou LL hook)."""
        WM_QUIT = 0x0012
        user32.UnregisterHotKey(None, HOTKEY_ID)
        if self._reg_thread_id is not None:
            user32.PostThreadMessageW(self._reg_thread_id, WM_QUIT, 0, 0)
            self._reg_thread_id = None
        if self._ll_thread_id is not None:
            user32.PostThreadMessageW(self._ll_thread_id, WM_QUIT, 0, 0)
            self._ll_thread_id = None
        # Aguarda threads terminarem
        for t in self._threads:
            t.join(timeout=1.0)
        self._threads.clear()

    def _fire(self, source):
        now = time.time()
        # v4.1.6: debounce aumentado 0.5→1.2s para evitar disparo duplo acidental
        if now - self._last_trigger > 1.2:
            self._last_trigger = now
            log(f"HOTKEY via {source}")
            self._queue.put("toggle")

    # --- RegisterHotKey (modo toggle) ---

    def _register_hotkey_thread(self):
        self._reg_thread_id = kernel32.GetCurrentThreadId()
        ok = user32.RegisterHotKey(None, HOTKEY_ID, self._mods, self._vk)
        if not ok:
            err = ctypes.GetLastError()
            log(f"RegisterHotKey FAILED: err={err} ({self._hotkey_str})")
            if self._on_conflict:
                self._on_conflict(self._hotkey_str)
            return
        log(f"RegisterHotKey OK ({self._hotkey_str})")

        msg = ctypes.wintypes.MSG()
        while True:
            ret = user32.GetMessageW(ctypes.byref(msg), None, 0, 0)
            if ret <= 0:
                break
            if msg.message == WM_HOTKEY and msg.wParam == HOTKEY_ID:
                self._fire("RegisterHotKey")
            user32.TranslateMessage(ctypes.byref(msg))
            user32.DispatchMessageW(ctypes.byref(msg))

    # --- WH_KEYBOARD_LL (modo push-to-talk) ---

    def _ptt_hook_thread(self):
        """LL hook para PTT: key-down inicia gravação, key-up para e transcreve."""
        self._ll_thread_id = kernel32.GetCurrentThreadId()
        has_ctrl = self._has_ctrl
        has_shift = self._has_shift
        has_alt = self._has_alt
        target_vk = self._vk
        pressed = set()
        ptt_active = [False]  # True enquanto o combo está pressionado
        action_flag = [None]  # "start" ou "stop"

        @HOOKPROC
        def ll_proc(nCode, wParam, lParam):
            if nCode >= 0:
                info = ctypes.cast(lParam,
                                   ctypes.POINTER(KBDLLHOOKSTRUCT)).contents
                vk = info.vkCode
                injected = info.flags & 0x10  # LLKHF_INJECTED
                if not injected:
                    if wParam in (WM_KEYDOWN, WM_SYSKEYDOWN):
                        pressed.add(vk)
                    else:
                        pressed.discard(vk)

                    # Verifica se o combo completo está pressionado
                    combo_held = True
                    if has_ctrl and not (pressed & CTRL_VKS):
                        combo_held = False
                    if has_shift and not (pressed & SHIFT_VKS):
                        combo_held = False
                    if has_alt and not (pressed & ALT_VKS):
                        combo_held = False
                    if target_vk not in pressed:
                        combo_held = False

                    if combo_held and not ptt_active[0]:
                        # Combo pressionado pela primeira vez → iniciar gravação
                        ptt_active[0] = True
                        action_flag[0] = "start"
                    elif not combo_held and ptt_active[0]:
                        # Alguma tecla do combo solta → parar gravação
                        ptt_active[0] = False
                        action_flag[0] = "stop"

            return user32.CallNextHookEx(None, nCode, wParam, lParam)

        self._ll_proc_ref = ll_proc  # prevent GC

        h = user32.SetWindowsHookExW(WH_KEYBOARD_LL, ll_proc, None, 0)
        if not h:
            log(f"PTT LL hook FAILED: err={ctypes.GetLastError()}")
            if self._on_conflict:
                self._on_conflict(self._hotkey_str)
            return
        log(f"PTT LL hook installed (handle={h})")

        WM_TIMER = 0x0113
        TIMER_ID = 99
        user32.SetTimer(None, TIMER_ID, 30, None)

        msg = ctypes.wintypes.MSG()
        while True:
            ret = user32.GetMessageW(ctypes.byref(msg), None, 0, 0)
            if ret <= 0:
                break
            user32.TranslateMessage(ctypes.byref(msg))
            user32.DispatchMessageW(ctypes.byref(msg))
            if action_flag[0] == "start":
                action_flag[0] = None
                log("PTT: key-down → ptt_start")
                self._queue.put("ptt_start")
            elif action_flag[0] == "stop":
                action_flag[0] = None
                log("PTT: key-up → ptt_stop")
                self._queue.put("ptt_stop")

        user32.KillTimer(None, TIMER_ID)
        user32.UnhookWindowsHookEx(h)


# ─── UsageReportWindow ───────────────────────────────────────

def _fmt_report_duration(total_secs):
    """Formata segundos em escala progressiva: min → horas → dias."""
    m = int(total_secs // 60)
    if m < 60:
        return f"{m}min {int(total_secs % 60)}s"
    h = m // 60
    if h < 24:
        return f"{h}h {m % 60}min"
    d = h // 24
    return f"{d}d {h % 24}h"


class UsageReportWindow:
    """Janela de relatório de uso — para comprovação da garantia condicional."""

    @property
    def BG(self):
        return get_theme()["bg_primary"]

    @property
    def FG(self):
        return get_theme()["text_primary"]

    @property
    def ACCENT(self):
        return get_theme()["info"]

    def __init__(self, usage: "UsageTracker"):
        self._usage = usage
        self._win = None

    def show(self):
        if self._win and self._win.winfo_exists():
            self._win.lift()
            return
        self._build()

    def _build(self):
        win = tk.Toplevel()
        win.title(f"Voice AI {VERSION} — Relatório de Uso")
        win.configure(bg=self.BG)
        win.resizable(False, False)
        win.attributes("-topmost", True)
        self._win = win

        pad = {"padx": 16, "pady": 6}

        # ── Cabeçalho ──
        tk.Label(win, text=f"Relatório de Uso  —  {self._usage.month_label}",
                 bg=self.BG, fg=self.ACCENT, font=("Segoe UI", 13, "bold")).pack(**pad)
        tk.Frame(win, bg=self.ACCENT, height=1).pack(fill="x", padx=16)

        # ── Resumo do mês ──
        summary_frame = tk.Frame(win, bg=self.BG)
        summary_frame.pack(fill="x", **pad)
        rows = [
            ("Transcrições no mês", str(self._usage.total_requests)),
            ("Tempo gravado", _fmt_report_duration(self._usage.total_seconds)),
            ("Tempo economizado (est.)", "~" + _fmt_report_duration(int(self._usage.saved_minutes * 60))),
            ("Custo API no mês", f"R$ {self._usage.cost_brl:.2f}"),
        ]
        for label, value in rows:
            row = tk.Frame(summary_frame, bg=self.BG)
            row.pack(fill="x", pady=2)
            tk.Label(row, text=label, bg=self.BG, fg=self.FG,
                     font=("Segoe UI", 10), anchor="w").pack(side="left")
            tk.Label(row, text=value, bg=self.BG, fg=self.ACCENT,
                     font=("Segoe UI", 10, "bold"), anchor="e").pack(side="right")

        t = get_theme()
        tk.Frame(win, bg=t["border"], height=1).pack(fill="x", padx=16, pady=4)

        # ── Histórico por mês ──
        tk.Label(win, text="Histórico mensal", bg=self.BG, fg=self.FG,
                 font=("Segoe UI", 10, "bold")).pack(anchor="w", padx=16)

        monthly = self._usage.monthly_summary()
        for month_key in sorted(monthly.keys(), reverse=True)[:6]:
            d = monthly[month_key]
            reqs = d.get("requests", 0)
            secs = int(d.get("seconds", 0))
            try:
                import datetime as _dt
                y, m = month_key.split("-")
                label = _dt.date(int(y), int(m), 1).strftime("%b %Y")
            except Exception:
                label = month_key
            row = tk.Frame(win, bg=self.BG)
            row.pack(fill="x", padx=16, pady=1)
            tk.Label(row, text=label, bg=self.BG, fg=t["text_dim"],
                     font=("Segoe UI", 9), width=10, anchor="w").pack(side="left")
            tk.Label(row, text=f"{reqs} transcrições  |  {secs // 60}min",
                     bg=self.BG, fg=self.FG, font=("Segoe UI", 9)).pack(side="left", padx=8)

        tk.Frame(win, bg=t["border"], height=1).pack(fill="x", padx=16, pady=8)

        # ── Botões ──
        btn_frame = tk.Frame(win, bg=self.BG)
        btn_frame.pack(pady=(0, 12))

        tk.Button(btn_frame, text="Exportar CSV",
                  bg=self.ACCENT, fg="white", font=("Segoe UI", 10),
                  relief="flat", padx=12, pady=4,
                  command=self._export_csv).pack(side="left", padx=8)
        tk.Button(btn_frame, text="Fechar",
                  bg=t["bg_tertiary"], fg=self.FG, font=("Segoe UI", 10),
                  relief="flat", padx=12, pady=4,
                  command=win.destroy).pack(side="left", padx=8)

    def _export_csv(self):
        try:
            path = self._usage.export_csv()
            import subprocess
            subprocess.Popen(["explorer", "/select,", path])
            messagebox.showinfo("CSV Exportado",
                                f"Arquivo salvo em:\n{path}",
                                parent=self._win)
        except Exception as e:
            messagebox.showerror("Erro", str(e), parent=self._win)


# ─── TrayIcon ────────────────────────────────────────────────

class TrayIcon:
    """Ícone na bandeja do sistema com menu.
    Clique esquerdo abre MainPanel, clique direito abre menu.
    Suporta ícone customizado (icon.png na pasta do app).
    """

    def __init__(self, on_open=None, on_settings=None, on_exit=None,
                 hotkey_str="", recording_mode="toggle"):
        self._on_open = on_open
        self._on_settings = on_settings
        self._on_exit = on_exit
        self._hotkey_str = hotkey_str
        self._recording_mode = recording_mode
        self._icon = None
        self._custom_icon = self._load_custom_icon() or self._load_embedded_icon()
        self._custom_icon_rec = self._load_custom_icon("icon_rec.png")

    def _load_custom_icon(self, name="icon.png"):
        path = os.path.join(script_dir, name)
        if os.path.exists(path):
            try:
                img = Image.open(path).convert("RGBA")
                img = img.resize((64, 64), Image.LANCZOS)
                return img
            except Exception:
                pass
        return None

    def _load_embedded_icon(self):
        try:
            if not ICON_B64:
                return None
            data = base64.b64decode(ICON_B64)
            img = Image.open(io.BytesIO(data)).convert("RGBA")
            return img.resize((64, 64), Image.LANCZOS)
        except Exception:
            return None

    def _create_icon(self, recording=False):
        if recording and self._custom_icon_rec:
            return self._custom_icon_rec
        if not recording and self._custom_icon:
            return self._custom_icon
        # Fallback: microfone gerado via Pillow (64x64)
        sz = 64
        img = Image.new("RGBA", (sz, sz), (0, 0, 0, 0))
        draw = ImageDraw.Draw(img)
        # Fundo arredondado
        draw.rounded_rectangle([0, 0, sz - 1, sz - 1], radius=14,
                                fill="#1a1a2e", outline="#4fc3f7" if not recording else "#ff4444",
                                width=2)
        # Corpo do microfone (retângulo com cantos arredondados no topo)
        mic_color = "#ffffff" if not recording else "#ff4444"
        draw.rounded_rectangle([22, 12, 42, 36], radius=8, fill=mic_color)
        # Arco (suporte do mic)
        draw.arc([16, 20, 48, 48], start=0, end=180, fill=mic_color, width=3)
        # Haste
        draw.line([32, 48, 32, 55], fill=mic_color, width=3)
        # Base
        draw.line([24, 55, 40, 55], fill=mic_color, width=3)
        return img

    def show_toast(self, title, message, sound=True):
        """Exibe balloon tip na system tray (sem som — sons de gravação substituem)."""
        if self._icon:
            try:
                self._icon.notify(message, title)
            except Exception:
                pass

    def _hook_balloon_click(self, on_balloon_click):
        """Intercepta clique na notificação via WndProc hook (ctypes).
        v4.1.6: captura NIN_BALLOONUSERCLICK (balloon clássico) E NIN_SELECT (toast Action Center).
        """
        import ctypes
        from ctypes import wintypes

        GWLP_WNDPROC = -4
        WMAPP_NOTIFYICON = 0x8001   # WM_APP + 1 — mensagem padrão do pystray
        NIN_BALLOONUSERCLICK = 0x405  # balloon legado
        NIN_SELECT           = 0x400  # clique simples no ícone / toast no Action Center (Win10/11)
        WM_LBUTTONUP         = 0x0202

        WndProcType = ctypes.WINFUNCTYPE(
            ctypes.c_long,
            wintypes.HWND, ctypes.c_uint, wintypes.WPARAM, wintypes.LPARAM
        )

        def _do_hook():
            hwnd = 0
            # Tenta "pystray" (versão antiga) e depois variações de class name (versão nova)
            for class_name in ("pystray", "Dummy", None):
                for _ in range(30):
                    if class_name is not None:
                        hwnd = ctypes.windll.user32.FindWindowW(class_name, None)
                    else:
                        # Última tentativa: busca janela com título "pystray"
                        hwnd = ctypes.windll.user32.FindWindowW(None, "pystray")
                    if hwnd:
                        break
                    time.sleep(0.1)
                if hwnd:
                    break

            if not hwnd:
                log("balloon click hook: janela pystray não encontrada — clique em notificação não abrirá painel")
                return

            old_proc = ctypes.windll.user32.GetWindowLongPtrW(hwnd, GWLP_WNDPROC)

            def _wndproc(h, msg, wp, lp):
                if msg == WMAPP_NOTIFYICON:
                    event = lp & 0xFFFF
                    if event in (NIN_BALLOONUSERCLICK, NIN_SELECT):
                        try:
                            on_balloon_click()
                        except Exception as e:
                            log(f"balloon click cb error: {e}")
                return ctypes.windll.user32.CallWindowProcW(old_proc, h, msg, wp, lp)

            proc_ref = WndProcType(_wndproc)
            self._wndproc_ref = proc_ref  # Mantém referência para evitar GC
            ctypes.windll.user32.SetWindowLongPtrW(hwnd, GWLP_WNDPROC, proc_ref)
            log("balloon click hook instalado (NIN_BALLOONUSERCLICK + NIN_SELECT)")

        threading.Thread(target=_do_hook, daemon=True).start()

    def start(self, config=None):
        self._config = config  # Para acesso ao is_silenced / silence_for_day

        def _silence_label(item):
            if self._config and self._config.is_silenced():
                return "🔔 Retomar notificações"
            return "🔕 Silenciar hoje"

        def _toggle_silence():
            if self._config:
                if self._config.is_silenced():
                    self._config.unsilence()
                else:
                    self._config.silence_for_day()
                if self._icon:
                    self._icon.update_menu()

        menu = pystray.Menu(
            pystray.MenuItem("Abrir historico", self._click_open, default=True),
            pystray.MenuItem("Configuracoes",
                             lambda: self._on_settings() if self._on_settings else None),
            pystray.MenuItem("Modo Agente", self._open_voice_agent),
            pystray.MenuItem(_silence_label, _toggle_silence),
            pystray.Menu.SEPARATOR,
            pystray.MenuItem("Sair",
                             lambda: self._on_exit() if self._on_exit else None),
        )
        self._icon = pystray.Icon(
            name="VoiceAI",
            icon=self._create_icon(),
            title=f"Voice AI \u2014 {self._hotkey_str} ({'PTT' if self._recording_mode == 'ptt' else 'Toggle'})",
            menu=menu,
        )
        t = threading.Thread(target=self._icon.run, daemon=True)
        t.start()
        log("TrayIcon started")

    def _click_open(self):
        if self._on_open:
            self._on_open()

    def _open_voice_agent(self):
        """Abre o Voice Agent como processo separado (sem console)."""
        import subprocess as _sp
        import shutil
        agent_path = os.path.join(script_dir, "voice_agent.py")
        if not os.path.exists(agent_path):
            log(f"Voice Agent não encontrado: {agent_path}")
            return
        if getattr(sys, "frozen", False):
            python = (shutil.which("pythonw") or shutil.which("python")
                      or shutil.which("python3") or shutil.which("py"))
            if not python:
                log("Voice Agent: Python não encontrado no PATH (frozen mode)")
                return
        else:
            pythonw = sys.executable.replace("python.exe", "pythonw.exe")
            python = pythonw if os.path.exists(pythonw) else sys.executable
        try:
            log(f"Voice Agent launching: {python} {agent_path}")
            _sp.Popen(
                [python, agent_path],
                cwd=script_dir,
                stdout=_sp.DEVNULL, stderr=_sp.DEVNULL,
                creationflags=0x00000008,  # DETACHED_PROCESS
            )
            log("Voice Agent launched from TrayIcon")
        except Exception as e:
            log(f"Voice Agent launch error: {e}")

    def set_recording(self, recording):
        if self._icon:
            self._icon.icon = self._create_icon(recording)
            if recording:
                rec_text = "Gravando (PTT)..." if self._recording_mode == "ptt" else "Gravando..."
                self._icon.title = f"Voice AI \u2014 {rec_text}"
            else:
                mode_tag = "PTT" if self._recording_mode == "ptt" else "Toggle"
                self._icon.title = f"Voice AI \u2014 {self._hotkey_str} ({mode_tag})"

    def stop(self):
        if self._icon:
            self._icon.stop()


# ─── Start Menu Shortcut ────────────────────────────────────

def create_start_menu_shortcut():
    """Cria atalho no Menu Iniciar para o app ser pesquisável."""
    try:
        if not getattr(sys, "frozen", False):
            return  # só cria atalho para .exe
        exe_path = sys.executable
        shortcut_dir = os.path.join(os.environ["APPDATA"],
                                     "Microsoft", "Windows", "Start Menu", "Programs")
        shortcut_path = os.path.join(shortcut_dir, "Voice AI.lnk")
        if os.path.exists(shortcut_path):
            return  # já existe

        # Criar .lnk via VBScript (sem dependência extra)
        vbs = os.path.join(script_dir, "_create_shortcut.vbs")
        with open(vbs, "w") as f:
            f.write(f'Set ws = CreateObject("WScript.Shell")\n')
            f.write(f'Set sc = ws.CreateShortcut("{shortcut_path}")\n')
            f.write(f'sc.TargetPath = "{exe_path}"\n')
            f.write(f'sc.WorkingDirectory = "{script_dir}"\n')
            f.write(f'sc.Description = "Voice AI \u2014 Transcritor de audio por Expert Integrado"\n')
            f.write(f'sc.Save\n')
        os.system(f'cscript //nologo "{vbs}"')
        os.remove(vbs)
        log(f"Start Menu shortcut created: {shortcut_path}")
    except Exception as e:
        log(f"Start Menu shortcut failed: {e}")


def create_startup_shortcut():
    """Cria atalho na pasta Startup para iniciar com o Windows."""
    try:
        if not getattr(sys, "frozen", False):
            return
        exe_path = sys.executable
        startup_dir = os.path.join(os.environ["APPDATA"],
                                    "Microsoft", "Windows", "Start Menu",
                                    "Programs", "Startup")
        shortcut_path = os.path.join(startup_dir, "Voice AI.lnk")
        if os.path.exists(shortcut_path):
            return
        vbs = os.path.join(script_dir, "_create_startup.vbs")
        with open(vbs, "w") as f:
            f.write(f'Set ws = CreateObject("WScript.Shell")\n')
            f.write(f'Set sc = ws.CreateShortcut("{shortcut_path}")\n')
            f.write(f'sc.TargetPath = "{exe_path}"\n')
            f.write(f'sc.WorkingDirectory = "{script_dir}"\n')
            f.write(f'sc.Description = "Voice AI"\n')
            f.write(f'sc.Save\n')
        os.system(f'cscript //nologo "{vbs}"')
        os.remove(vbs)
        log(f"Startup shortcut created: {shortcut_path}")
    except Exception as e:
        log(f"Startup shortcut failed: {e}")


def remove_startup_shortcut():
    """Remove atalho da pasta Startup."""
    try:
        shortcut_path = os.path.join(os.environ["APPDATA"],
                                      "Microsoft", "Windows", "Start Menu",
                                      "Programs", "Startup", "Voice AI.lnk")
        if os.path.exists(shortcut_path):
            os.remove(shortcut_path)
            log(f"Startup shortcut removed: {shortcut_path}")
    except Exception as e:
        log(f"Startup shortcut removal failed: {e}")




# ─── OnboardingWizard (v3.8.0) ────────────────────────────────

class OnboardingWizard:
    """Wizard de boas-vindas para primeiro uso (4 passos).
    Dispara quando config.first_run == True.
    Ao concluir ou pular: seta first_run = False e salva config.
    """

    STEPS = [
        {
            "icon": "🎙️",
            "title": "Bem-vindo ao Voice AI!",
            "body": (
                "O Voice AI transcreve sua fala em texto e cola\n"
                "automaticamente onde o cursor estiver.\n\n"
                "Fala → texto → colado — em menos de 2 segundos."
            ),
        },
        {
            "icon": "🔑",
            "title": "Configure sua API Key",
            "body": (
                "O Voice AI usa a API da OpenAI para transcrever.\n"
                "Você precisa de uma chave API da OpenAI.\n\n"
                "Você pode obter em:\nplatform.openai.com/api-keys\n\n"
                "Configure agora nas Configurações → aba Transcrição."
            ),
        },
        {
            "icon": "⌨️",
            "title": "Atalho de teclado",
            "body": (
                "O atalho padrão é:\n\nCtrl + Shift + Alt + F3\n\n"
                "Pressione uma vez para INICIAR a gravação.\n"
                "Pressione novamente para PARAR e transcrever.\n\n"
                "Você pode personalizar o atalho nas Configurações."
            ),
        },
        {
            "icon": "🚀",
            "title": "Pronto para usar!",
            "body": (
                "Tudo configurado! Agora é só usar:\n\n"
                "1. Posicione o cursor onde quer o texto\n"
                "2. Pressione o atalho e fale\n"
                "3. Pressione novamente — texto colado!\n\n"
                "O ícone na bandeja do sistema fica sempre disponível."
            ),
        },
    ]

    def __init__(self, root, config):
        self._root = root
        self._config = config
        self._step = 0
        self._done = False

        self._win = tk.Toplevel(root)
        self._win.title("Voice AI — Bem-vindo")
        self._win.overrideredirect(True)
        self._win.attributes("-topmost", True)
        self._win.attributes("-alpha", 0.97)

        t = get_theme()
        bg = t["bg_primary"]
        self._win.configure(bg=bg)

        W, H = 480, 400
        sx = (self._win.winfo_screenwidth() - W) // 2
        sy = (self._win.winfo_screenheight() - H) // 2 - 40
        self._win.geometry(f"{W}x{H}+{sx}+{sy}")
        self._W, self._H = W, H

        self._build()
        self._win.protocol("WM_DELETE_WINDOW", self._skip)

    def _build(self):
        t = get_theme()
        bg = t["bg_primary"]
        accent = t["accent"]

        # Limpa
        for w in self._win.winfo_children():
            w.destroy()

        step = self.STEPS[self._step]
        n = self._step + 1
        total = len(self.STEPS)

        # Header com gradiente accent
        header = tk.Frame(self._win, bg=accent, height=6)
        header.pack(fill="x")

        # Indicador de progresso
        prog_frame = tk.Frame(self._win, bg=bg)
        prog_frame.pack(fill="x", padx=30, pady=(16, 0))
        for i in range(total):
            dot_color = accent if i <= self._step else t["bg_tertiary"]
            dot = tk.Frame(prog_frame, bg=dot_color,
                           width=28 if i == self._step else 8, height=8)
            dot.pack(side="left", padx=2)
            dot.pack_propagate(False)

        step_lbl = tk.Label(prog_frame,
                            text=f"{n} de {total}",
                            font=("Segoe UI", 9), fg=t["text_dim"], bg=bg)
        step_lbl.pack(side="right")

        # Ícone
        icon_lbl = tk.Label(self._win, text=step["icon"],
                            font=("Segoe UI Emoji", 36),
                            bg=bg, fg=t["text_primary"])
        icon_lbl.pack(pady=(20, 8))

        # Título
        tk.Label(self._win, text=step["title"],
                 font=("Segoe UI Semibold", 15),
                 fg=t["text_primary"], bg=bg).pack(padx=30)

        # Divisor
        tk.Frame(self._win, bg=accent, height=1).pack(
            fill="x", padx=40, pady=(10, 0))

        # Corpo
        tk.Label(self._win, text=step["body"],
                 font=("Segoe UI", 10), fg=t["text_secondary"],
                 bg=bg, justify="center", wraplength=380).pack(
                     pady=(14, 0), padx=30)

        # Botões
        btn_frame = tk.Frame(self._win, bg=bg)
        btn_frame.pack(side="bottom", pady=24, fill="x", padx=30)

        # Botão Pular (apenas nos passos iniciais)
        if self._step < total - 1:
            skip_btn = tk.Label(btn_frame, text="Pular",
                                font=("Segoe UI", 10), fg=t["text_dim"],
                                bg=bg, cursor="hand2")
            skip_btn.pack(side="left")
            skip_btn.bind("<Enter>", lambda e: skip_btn.config(fg=t["text_secondary"]))
            skip_btn.bind("<Leave>", lambda e: skip_btn.config(fg=t["text_dim"]))
            skip_btn.bind("<Button-1>", lambda e: self._skip())

        # Botão principal (Próximo / Começar)
        is_last = self._step == total - 1
        btn_text = "Começar!" if is_last else "Próximo →"
        btn_cmd = self._finish if is_last else self._next

        next_btn = tk.Frame(btn_frame, bg=accent, cursor="hand2")
        next_btn.pack(side="right")
        next_lbl = tk.Label(next_btn, text=btn_text,
                            font=("Segoe UI Semibold", 10),
                            fg=t["white"], bg=accent,
                            padx=20, pady=8)
        next_lbl.pack()

        def _on_enter_btn(e):
            next_btn.config(bg=t["accent_hover"])
            next_lbl.config(bg=t["accent_hover"])

        def _on_leave_btn(e):
            next_btn.config(bg=accent)
            next_lbl.config(bg=accent)

        next_btn.bind("<Enter>", _on_enter_btn)
        next_btn.bind("<Leave>", _on_leave_btn)
        next_lbl.bind("<Enter>", _on_enter_btn)
        next_lbl.bind("<Leave>", _on_leave_btn)
        next_btn.bind("<Button-1>", lambda e: btn_cmd())
        next_lbl.bind("<Button-1>", lambda e: btn_cmd())

    def _next(self):
        self._step += 1
        self._build()

    def _finish(self):
        self._done = True
        self._config.data["first_run"] = False
        self._config.save()
        self._win.destroy()

    def _skip(self):
        self._config.data["first_run"] = False
        self._config.save()
        self._win.destroy()

    def run(self):
        """Aguarda o wizard ser fechado (modal via wait_window)."""
        self._win.grab_set()
        self._root.wait_window(self._win)


# ─── EditPopup ────────────────────────────────────────────────

class EditPopup:
    """Popup flutuante para editar texto antes de colar.
    Enter (sem Shift) ou botão Colar = confirma.
    Shift+Enter = nova linha no texto.
    ESC = cancela.
    """

    @property
    def BG(self):
        return get_theme()["bg_primary"]

    @property
    def FG(self):
        return get_theme()["text_primary"]

    @property
    def ACCENT(self):
        return get_theme()["accent"]

    @property
    def ACCENT_HOVER(self):
        return get_theme()["accent_hover"]

    @property
    def DIM(self):
        return get_theme()["text_secondary"]

    @property
    def ENTRY_BG(self):
        return get_theme()["bg_secondary"]

    @property
    def BTN_BG(self):
        return get_theme()["bg_tertiary"]

    def __init__(self, root, text, on_confirm, on_cancel=None):
        self._root = root
        self._on_confirm = on_confirm
        self._on_cancel = on_cancel
        self._result = None

        self._win = tk.Toplevel(root)
        self._win.title("Voice AI — Revisar texto")
        self._win.overrideredirect(True)
        self._win.attributes("-topmost", True)
        self._win.attributes("-alpha", 0.97)
        self._win.configure(bg=self.BG)

        w, h = 500, 280
        sx = (self._win.winfo_screenwidth() - w) // 2
        sy = (self._win.winfo_screenheight() - h) // 2 - 50
        self._win.geometry(f"{w}x{h}+{sx}+{sy}")

        # Header
        header = tk.Frame(self._win, bg=self.BG)
        header.pack(fill="x", padx=16, pady=(12, 0))

        tk.Label(header, text="Revisar antes de colar",
                 font=("Segoe UI Semibold", 12), fg=self.FG,
                 bg=self.BG).pack(side="left")

        cancel_btn = tk.Label(header, text="\u2715", font=("Segoe UI", 14),
                              fg=self.DIM, bg=self.BG, cursor="hand2")
        cancel_btn.pack(side="right")
        cancel_btn.bind("<Button-1>", lambda e: self._cancel())
        cancel_btn.bind("<Enter>", lambda e: cancel_btn.config(fg=get_theme()["error"]))
        cancel_btn.bind("<Leave>", lambda e: cancel_btn.config(fg=self.DIM))

        tk.Frame(self._win, bg=self.ACCENT, height=1).pack(
            fill="x", padx=16, pady=(8, 0))

        # Textarea
        text_frame = tk.Frame(self._win, bg=self.BG)
        text_frame.pack(fill="both", expand=True, padx=16, pady=(10, 0))

        self._text = tk.Text(text_frame, font=("Segoe UI", 11),
                             bg=self.ENTRY_BG, fg=self.FG,
                             insertbackground=self.ACCENT,
                             relief="flat", wrap="word",
                             bd=0, highlightthickness=1,
                             highlightbackground=self.BTN_BG,
                             highlightcolor=self.ACCENT,
                             padx=10, pady=8)
        self._text.pack(fill="both", expand=True)
        self._text.insert("1.0", text)
        self._text.focus_set()
        # Seleciona tudo para facilitar edição
        self._text.tag_add("sel", "1.0", "end-1c")

        # Footer com botões
        footer = tk.Frame(self._win, bg=self.BG)
        footer.pack(fill="x", padx=16, pady=(8, 12))

        tk.Label(footer, text="Enter = colar  \u00b7  Shift+Enter = nova linha  \u00b7  ESC = cancelar",
                 font=("Segoe UI", 8), fg=self.DIM,
                 bg=self.BG).pack(side="left")

        paste_btn = tk.Button(footer, text="Colar", font=("Segoe UI", 10, "bold"),
                              bg=self.ACCENT, fg="#ffffff", relief="flat",
                              cursor="hand2", padx=16, pady=3,
                              command=self._confirm)
        paste_btn.pack(side="right")
        paste_btn.bind("<Enter>", lambda e: paste_btn.config(bg=self.ACCENT_HOVER))
        paste_btn.bind("<Leave>", lambda e: paste_btn.config(bg=self.ACCENT))

        copy_btn = tk.Button(footer, text="Copiar", font=("Segoe UI", 10),
                             bg=self.BTN_BG, fg=self.DIM, relief="flat",
                             cursor="hand2", padx=12, pady=3,
                             command=self._copy_only)
        copy_btn.pack(side="right", padx=(0, 8))

        # Bindings
        self._text.bind("<Return>", self._on_enter)
        self._win.bind("<Escape>", lambda e: self._cancel())

        # Drag support
        header.bind("<Button-1>", self._start_drag)
        header.bind("<B1-Motion>", self._on_drag)

    def _start_drag(self, event):
        self._drag_x = event.x
        self._drag_y = event.y

    def _on_drag(self, event):
        x = self._win.winfo_x() + (event.x - self._drag_x)
        y = self._win.winfo_y() + (event.y - self._drag_y)
        self._win.geometry(f"+{x}+{y}")

    def _on_enter(self, event):
        # Shift+Enter → nova linha (comportamento padrão do Text)
        if event.state & 0x1:
            return  # let default handler insert newline
        # Enter sem Shift → confirmar
        self._confirm()
        return "break"  # prevent newline insertion

    def _confirm(self):
        text = self._text.get("1.0", "end-1c").strip()
        self._win.destroy()
        if text and self._on_confirm:
            self._on_confirm(text)

    def _copy_only(self):
        text = self._text.get("1.0", "end-1c").strip()
        self._win.destroy()
        if text:
            pyperclip.copy(text)
            log("EditPopup: copied to clipboard (no paste)")

    def _cancel(self):
        self._win.destroy()
        if self._on_cancel:
            self._on_cancel()


# ─── App ─────────────────────────────────────────────────────

class App:
    def __init__(self, config):
        self.config = config
        self._active_hotkey = config.hotkey
        self.recording = False
        self._transcribing = False       # compatibilidade ESC monitor
        self._transcribing_count = 0     # v3.6.0: permite transcrições paralelas
        self._lock = threading.Lock()
        self._transcribing_lock = threading.Lock()  # v4.0.2: lock para _transcribing_count
        self._queue = queue.Queue()
        self._settings_open = False
        self._prev_hwnd = None
        self._rec_start_time = 0.0       # v3.6.0: timestamp início da gravação
        self.usage = UsageTracker()

        self._last_rms = 0.0
        self.recorder = AudioRecorder(on_volume=self._on_volume, device=config.mic_device)
        self.transcriber = Transcriber(config.api_key, config.model, config.language)
        self.paster = TextPaster()
        self.history = TranscriptionHistory()

        self.bar = StatusBar(
            on_click=self._on_open_panel,
            on_exit=self._on_exit,
            on_clear=self._on_clear_history,
            on_settings=self._on_settings,
        )
        self.main_panel = MainPanel(
            self.bar.root, self.history, self.paster,
            on_settings=self._on_settings,
            hotkey_str=config.hotkey,
            config=config,
            usage=self.usage,
            on_settings_saved=self._on_settings_saved,
        )
        self.hotkey_mgr = HotkeyManager(
            self._queue, config.hotkey,
            on_conflict=self._on_hotkey_conflict,
            recording_mode=config.recording_mode,
        )
        self._last_transcription = ""  # última transcrição para "Aplicar estilo"
        self._last_notif_text = ""    # texto da última notificação (para balloon click)
        # Sons sutis de gravação (v4.1.1: chimes distintos para início e fim)
        # Início: dois tons ascendentes (528→660 Hz) — like Wispr Flow "start"
        self._snd_start = _make_chime_wav([528, 660], [55, 70], gap_ms=14, volume=0.16)
        # Fim (stop recording): tom descendente único (528 Hz fade) — sinal de "parou"
        self._snd_stop  = _make_chime_wav([440], [90], gap_ms=0, volume=0.14)
        self.tray = TrayIcon(
            on_open=lambda: self.bar.root.after(0, self._on_open_panel),
            on_settings=lambda: self.bar.root.after(0, self._on_settings),
            on_exit=lambda: self.bar.root.after(0, self._on_exit),
            hotkey_str=config.hotkey,
            recording_mode=config.recording_mode,
        )

    def run(self, open_panel=False):
        self.hotkey_mgr.start()
        self.tray.start(config=self.config)
        # Respeita configuração show_statusbar na inicialização (v3.4.0)
        self.bar.set_enabled(self.config.data.get("show_statusbar", True))
        # Hook de click na notificação — copia texto + abre MainPanel
        self.tray._hook_balloon_click(self._on_balloon_click)
        hotkey_display = self.config.hotkey
        mode_label = "PTT" if self.config.recording_mode == "ptt" else "Toggle"
        is_first_run = self.config.data.get("first_run", True)
        # v4.1.4: no primeiro uso, não exibe banner na StatusBar (texto longo corta feio)
        if not is_first_run:
            self.bar.show(f"Voice AI pronto  [{hotkey_display}] {mode_label}", "#66bb6a", 3000)
        # v4.0.6: aviso explícito se DPAPI indisponível e chave em texto puro
        if self.config.api_key_is_unencrypted():
            self.bar.root.after(3500, lambda: self.bar.show(
                "⚠ pywin32 ausente — API key salva sem criptografia", "#ffaa00", 6000))
            log("WARNING: api_key stored in plaintext (pywin32 not installed)")
        if open_panel:
            self.bar.root.after(800, self.main_panel.open)
        # v3.8.0: Onboarding wizard no primeiro uso
        if is_first_run:
            self.bar.root.after(600, self._show_onboarding)
        # Verifica atualização 5s após iniciar (sem bloquear startup)
        self.bar.root.after(5000, self._schedule_update_check)
        self._poll_queue()
        log("Entering mainloop")
        print(f"Voice AI rodando. {hotkey_display} (mouse ou teclado).")
        self.bar.run()

    def _show_onboarding(self):
        """v3.8.0: Exibe wizard de boas-vindas (primeiro uso)."""
        try:
            wizard = OnboardingWizard(self.bar.root, self.config)
            wizard.run()
        except Exception as e:
            log(f"Onboarding error: {e}")

    def _schedule_update_check(self):
        threading.Thread(target=self._do_update_check, daemon=True).start()

    def _do_update_check(self):
        new_ver, url = check_for_update()
        if new_ver:
            log(f"Update available: v{new_ver}")
            msg = f"\u2b06\ufe0f  Nova vers\u00e3o v{new_ver} dispon\u00edvel!"
            self.bar.root.after(0, lambda: self._show_update_banner(msg, url))

    def _show_update_banner(self, msg, url):
        self.bar.show(msg, "#f0a500")  # auto_hide_ms=0 → persiste até próxima ação
        # Clique na barra abre o release no browser
        self.bar._canvas.bind("<Button-1>", lambda e: webbrowser.open(url))

    def _poll_queue(self):
        try:
            while True:
                msg = self._queue.get_nowait()
                if msg == "cancel":
                    threading.Thread(target=self._cancel_recording, daemon=True).start()
                elif msg == "ptt_start":
                    threading.Thread(target=self._ptt_start, daemon=True).start()
                elif msg == "ptt_stop":
                    threading.Thread(target=self._ptt_stop, daemon=True).start()
                else:
                    threading.Thread(target=self._toggle, daemon=True).start()
        except queue.Empty:
            pass
        self.bar.root.after(50, self._poll_queue)

    def _toggle(self, send_enter=False):
        if not self._lock.acquire(blocking=False):
            return
        try:
            if not self.recording:
                self._start_recording()
            else:
                # Duplo-clique: 2ª pressão < 1s após início → envia Enter (v3.6.0)
                if self.config.data.get("double_click_enter", False):
                    elapsed_since_start = time.time() - self._rec_start_time
                    if elapsed_since_start < 1.0:
                        send_enter = True
                # Para gravação e inicia transcrição em thread separada (gravação paralela)
                self.recording = False
                wav_buf = self.recorder.stop()
                prev_hwnd = self._prev_hwnd
                self.tray.set_recording(False)
                if self.config.notification_sound:
                    threading.Thread(
                        target=lambda: winsound.PlaySound(self._snd_stop, winsound.SND_MEMORY),
                        daemon=True,
                    ).start()
                log("REC stop -> dispatching transcription thread")
                # Salvar áudio em disco se configurado
                audio_path = None
                if self.config.save_audio and wav_buf:
                    try:
                        audio_dir = os.path.join(
                            os.environ.get("LOCALAPPDATA", script_dir), "VoiceAI", "audio"
                        )
                        os.makedirs(audio_dir, exist_ok=True)
                        fname = datetime.now().strftime("%Y%m%d_%H%M%S") + ".wav"
                        audio_path = os.path.join(audio_dir, fname)
                        wav_buf.seek(0)
                        with open(audio_path, "wb") as _af:
                            _af.write(wav_buf.read())
                        wav_buf.seek(0)
                        log(f"Audio saved: {audio_path}")
                    except Exception as e:
                        log(f"Audio save error: {e}")
                        audio_path = None
                threading.Thread(
                    target=self._process_transcription,
                    args=(wav_buf, prev_hwnd, audio_path, send_enter),
                    daemon=True,
                ).start()
        finally:
            self._lock.release()

    def _start_recording(self):
        self.recording = True
        self._rec_start_time = time.time()  # v3.6.0: para detecção de duplo-clique
        self._prev_hwnd = self.paster.get_foreground_window()
        self._last_rms = 0.0
        self._silence_elapsed = 0.0  # v3.4.0: acumulador de silêncio (segundos)
        self.recorder.start()
        self.tray.set_recording(True)
        if self.config.notification_sound:
            threading.Thread(
                target=lambda: winsound.PlaySound(self._snd_start, winsound.SND_MEMORY),
                daemon=True,
            ).start()
        log(f"REC start (prev_hwnd={self._prev_hwnd})")
        # Loop de UI para timer + volume (atualiza a cada 200ms via mainloop)
        self.bar.root.after(0, self._recording_ui_loop)
        # Monitor de ESC para cancelar gravação
        threading.Thread(target=self._esc_monitor, daemon=True).start()

    def _on_volume(self, rms):
        self._last_rms = rms

    def _recording_ui_loop(self):
        if not self.recording:
            return
        elapsed = self.recorder.elapsed
        is_ptt = self.config.recording_mode == "ptt"
        # Auto-stop se atingir limite máximo de duração (não em PTT)
        if not is_ptt:
            max_secs = self.config.max_recording_secs
            if max_secs > 0 and elapsed >= max_secs:
                log(f"Max recording duration reached ({max_secs}s)")
                threading.Thread(target=self._toggle, daemon=True).start()
                return
            # Auto-stop por silêncio (v3.4.0 — não em PTT)
            if self.config.data.get("silence_stop", False):
                SILENCE_THRESHOLD = 0.01
                interval_secs = 0.08  # intervalo do loop em segundos
                if self._last_rms < SILENCE_THRESHOLD:
                    self._silence_elapsed += interval_secs
                    timeout = self.config.data.get("silence_timeout_secs", 30)
                    if self._silence_elapsed >= timeout:
                        log(f"Silence auto-stop ({timeout}s of silence)")
                        threading.Thread(target=self._toggle, daemon=True).start()
                        return
                else:
                    self._silence_elapsed = 0.0  # reset ao detectar som
        self.bar.update_recording(elapsed, self._last_rms, ptt=is_ptt,
                                   max_secs=self.config.max_recording_secs if not is_ptt else 0)
        self.bar.root.after(80, self._recording_ui_loop)

    def _esc_monitor(self):
        VK_ESCAPE = 0x1B
        # Aguarda ESC ser solto se já estava pressionado no início da gravação
        # (evita falso-positivo quando hotkey é acionada enquanto ESC está held)
        time.sleep(0.25)
        while self.recording and user32.GetAsyncKeyState(VK_ESCAPE) & 0x8000:
            time.sleep(0.05)
        # Detecção por borda: espera ESC ir para 0 e depois subir para 1
        was_down = False
        while self.recording:
            is_down = bool(user32.GetAsyncKeyState(VK_ESCAPE) & 0x8000)
            if is_down and not was_down:
                # Nova pressão de ESC (borda de subida) — cancela gravação
                self._queue.put("cancel")
                break
            was_down = is_down
            time.sleep(0.05)

    def _cancel_recording(self):
        if not self.recording:
            return
        if not self._lock.acquire(blocking=False):
            return
        try:
            self.recording = False
            self.recorder.stop()
            self.tray.set_recording(False)
            log("REC cancelled (ESC)")
            self.usage.log_event("recording_cancelled")
            self.bar.root.after(0, lambda: self.bar.show("Cancelado", "#ffaa00", 2000))
        finally:
            self._lock.release()

    def _ptt_start(self):
        """Push-to-talk: inicia gravação quando a hotkey é pressionada."""
        if not self._lock.acquire(blocking=False):
            return
        try:
            if self.recording:
                return  # já gravando
            self._start_recording()
        finally:
            self._lock.release()

    def _ptt_stop(self):
        """Push-to-talk: para gravação e inicia transcrição paralela."""
        if not self._lock.acquire(blocking=False):
            return
        try:
            if not self.recording:
                return
            self.recording = False
            wav_buf = self.recorder.stop()
            prev_hwnd = self._prev_hwnd
            self.tray.set_recording(False)
            if self.config.notification_sound:
                threading.Thread(
                    target=lambda: winsound.PlaySound(self._snd_stop, winsound.SND_MEMORY),
                    daemon=True,
                ).start()
            # Salvar áudio
            audio_path = None
            if self.config.save_audio and wav_buf:
                try:
                    audio_dir = os.path.join(
                        os.environ.get("LOCALAPPDATA", script_dir), "VoiceAI", "audio"
                    )
                    os.makedirs(audio_dir, exist_ok=True)
                    fname = datetime.now().strftime("%Y%m%d_%H%M%S") + ".wav"
                    audio_path = os.path.join(audio_dir, fname)
                    wav_buf.seek(0)
                    with open(audio_path, "wb") as _af:
                        _af.write(wav_buf.read())
                    wav_buf.seek(0)
                except Exception as e:
                    log(f"Audio save error (ptt): {e}")
                    audio_path = None
            threading.Thread(
                target=self._process_transcription,
                args=(wav_buf, prev_hwnd, audio_path, False),
                daemon=True,
            ).start()
        finally:
            self._lock.release()

    def _process_transcription(self, wav_buf, prev_hwnd, audio_path, send_enter=False):
        """Executa transcrição + paste em thread separada (v3.6.0 — gravação paralela)."""
        if wav_buf is None:
            self.bar.root.after(0, lambda: self.bar.show("Nenhum audio", "#ffaa00", 2000))
            return

        # v4.1.6: rejeitar áudio muito curto (< 0.5s) — evita alucinações da API em silêncio
        wav_buf.seek(0)
        import wave as _wave_mod
        try:
            with _wave_mod.open(wav_buf) as _wf:
                _n_frames = _wf.getnframes()
                _framerate = _wf.getframerate()
                _dur = _n_frames / _framerate if _framerate > 0 else 0
        except Exception:
            _dur = 99  # se não conseguir ler, deixa a API decidir
        wav_buf.seek(0)
        if _dur < 0.5:
            log(f"Audio ignorado: duração {_dur:.2f}s < 0.5s (muito curto)")
            self.bar.root.after(0, lambda: self.bar.show("Audio muito curto", "#ffaa00", 1500))
            return

        self.bar.root.after(0, self.bar.show_transcribing)
        cancelled_flag = [True]
        self._transcribing = True
        with self._transcribing_lock:
            self._transcribing_count += 1
        self._cancelled_flag = cancelled_flag  # referência ao último (para compatibilidade)
        # Monitor de ESC — passa a flag local para evitar race condition com transcrições paralelas
        threading.Thread(target=self._esc_transcribe_monitor,
                         args=(cancelled_flag,), daemon=True).start()

        was_cancelled = False
        try:
            _t0 = time.time()
            text, secs = self.transcriber.transcribe(wav_buf, cancelled_flag=cancelled_flag)
            log(f"API ok: {time.time()-_t0:.2f}s | {len(text)} chars")
            # v4.0.4: capturar estado da flag antes do finally zerá-la
            was_cancelled = not cancelled_flag[0]
        except Exception as e:
            err = str(e)
            if "cancelled" in err.lower():
                log("Transcription cancelled (ESC)")
                self.bar.root.after(0, lambda: self.bar.show("Cancelado", "#ffaa00", 2000))
                return
            log(f"API error: {e}")
            if isinstance(e, openai.AuthenticationError):
                msg = "API Key inv\u00e1lida. Verifique nas Configura\u00e7\u00f5es."
            elif isinstance(e, openai.RateLimitError):
                if "quota" in err.lower() or "insufficient" in err.lower():
                    msg = "Cota da OpenAI esgotada. Adicione cr\u00e9ditos em platform.openai.com."
                else:
                    msg = "Limite de requisi\u00e7\u00f5es atingido. Aguarde alguns segundos."
            elif isinstance(e, openai.APIConnectionError):
                msg = "Sem conex\u00e3o com a OpenAI. Verifique sua internet."
            elif isinstance(e, openai.APITimeoutError):
                msg = "\u23f0 Timeout — \u00e1udio muito longo ou conex\u00e3o lenta."
            else:
                msg = f"Erro: {err[:50]}"
            self.bar.root.after(0, lambda m=msg: self.bar.show(m, "#ff4444", 5000))
            return
        finally:
            cancelled_flag[0] = False  # encerra o _esc_transcribe_monitor desta thread
            with self._transcribing_lock:
                self._transcribing_count -= 1
                self._transcribing = self._transcribing_count > 0

        # v4.0.4: ESC pressionado enquanto API estava bloqueada — não colar
        if was_cancelled:
            log("Transcription cancelled (ESC) — post-API check")
            return

        if not text:
            self.bar.root.after(0, lambda: self.bar.show("Vazio", "#ffaa00", 2000))
            return

        # Comandos inline (v2.4.0)
        if self.config.inline_commands:
            text = apply_inline_commands(text, self.config.language)

        raw_text = text  # texto bruto antes do GPT — salvo como original_text no histórico

        # Emojis por voz — mascaramento antes do GPT (v3.4.0)
        _emoji_placeholders = []
        if self.config.data.get("emoji_replace", False):
            _emoji_map = _load_emoji_map()
            if _emoji_map:
                text, _emoji_placeholders = apply_emoji_mask(text, _emoji_map)

        # Pós-processamento GPT automático (v2.5.0)
        if self.config.text_style_auto and self.config.text_style != "none":
            self.bar.root.after(0, lambda: self.bar.show("\u2728  Aplicando estilo...", "#a78bfa"))
            text = apply_text_style(text, self.config.text_style,
                                    self.config.api_key, self.config.gpt_model,
                                    client=self.transcriber.client)

        # Emojis por voz — restauração após o GPT (v3.4.0)
        if _emoji_placeholders:
            text = apply_emoji_restore(text, _emoji_placeholders)

        log(f"OK ({secs}s): {text[:80]}")
        self._last_transcription = text
        # v3.5.0: calcula custo separado Whisper e GPT
        _words = len(text.split()) if text else 0
        _whisper_model = self.config.model  # "whisper-1" ou "gpt-4o-transcribe"
        _whisper_price_per_min = UsageTracker.WHISPER_COST_PER_MIN.get(_whisper_model, 0.006)
        _usd_brl = UsageTracker.get_usd_brl()
        _cost_whisper_brl = (secs / 60) * _whisper_price_per_min * _usd_brl
        _cost_gpt_brl = 0.0
        if self.config.text_style_auto and self.config.text_style != "none":
            # v3.5.1: PT-BR ~1.6 tokens/palavra; input + output separados
            _token_ratio = 1.6
            _input_tokens = max(1, int(len(text.split()) * _token_ratio))
            _output_tokens = _input_tokens  # correção de estilo gera output similar
            _price_in = UsageTracker.GPT_COST_PER_1K_INPUT.get(self.config.gpt_model, 0.0004)
            _price_out = UsageTracker.GPT_COST_PER_1K_OUTPUT.get(self.config.gpt_model, 0.0016)
            _cost_gpt_usd = (_input_tokens / 1000) * _price_in + (_output_tokens / 1000) * _price_out
            _cost_gpt_brl = _cost_gpt_usd * _usd_brl
        self.usage.add(secs, chars=len(text), lang=self.config.language,
                       words=_words, cost_whisper_brl=_cost_whisper_brl,
                       cost_gpt_brl=_cost_gpt_brl)
        self.history.add(text, audio_path=audio_path, original_text=raw_text)
        self.bar.root.after(0, self.main_panel.refresh)

        # Edição antes de colar (v2.7.0)
        if self.config.edit_before_paste:
            def _do_paste(edited_text):
                # Atualiza histórico e última transcrição com texto editado
                self._last_transcription = edited_text
                if self.history.items:
                    self.history.items[0]["text"] = edited_text
                    self.history.items[0]["preview"] = edited_text[:60] + ("..." if len(edited_text) > 60 else "")
                    self.history._save()
                self.main_panel.refresh()
                if self.config.auto_paste:
                    def _paste_thread():
                        if prev_hwnd:
                            self.paster.set_foreground_window(prev_hwnd)
                            time.sleep(0.15)
                        method = self.config.data.get("paste_method", "auto")
                        ok = self.paster.smart_paste(edited_text, method=method)
                        if ok:
                            self.bar.root.after(0, lambda: self.bar.show("Concluído ✓", "#66bb6a", 2000))
                        else:
                            pyperclip.copy(edited_text)
                            self.bar.root.after(0, lambda: self.bar.show(
                                "Não foi possível colar — Ctrl+V para inserir", "#ffaa00", 4000))
                    threading.Thread(target=_paste_thread, daemon=True).start()
                else:
                    pyperclip.copy(edited_text)
                    self.bar.root.after(0, lambda: self.bar.show("Concluído ✓", "#66bb6a", 2000))
                self._notify_transcription(edited_text)

            def _on_cancel():
                self.bar.root.after(0, lambda: self.bar.show("Edição cancelada", "#ffaa00", 2000))

            self.bar.root.after(0, lambda: EditPopup(
                self.bar.root, text,
                on_confirm=_do_paste,
                on_cancel=_on_cancel,
            ))
            self.bar.root.after(0, lambda: self.bar.show("\u270f\ufe0f  Revise o texto...", "#a78bfa"))
            return

        if self.config.auto_paste:
            if prev_hwnd:
                self.paster.set_foreground_window(prev_hwnd)
                time.sleep(0.15)
            method = self.config.data.get("paste_method", "auto")
            ok = self.paster.smart_paste(text, method=method)
            if not ok:
                # Fallback total: texto já está no clipboard, avisa o usuário
                pyperclip.copy(text)
                self.bar.root.after(0, lambda: self.bar.show(
                    "Não foi possível colar — Ctrl+V para inserir", "#ffaa00", 4000))
                self._notify_transcription(text)
                return
            # Auto-Enter após colar: via config ou via duplo-clique (v3.6.0)
            do_enter = send_enter or self.config.data.get("auto_enter", False)
            if do_enter:
                delay_ms = self.config.data.get("auto_enter_delay_ms", 100)
                time.sleep(delay_ms / 1000.0)
                scan_ret = user32.MapVirtualKeyW(VK_RETURN, 0)
                inp = (INPUT_STRUCT * 2)()
                inp[0].type = INPUT_KEYBOARD
                inp[0].u.ki.wVk = VK_RETURN
                inp[0].u.ki.wScan = scan_ret
                inp[1].type = INPUT_KEYBOARD
                inp[1].u.ki.wVk = VK_RETURN
                inp[1].u.ki.wScan = scan_ret
                inp[1].u.ki.dwFlags = KEYEVENTF_KEYUP_SI
                user32.SendInput(2, inp, ctypes.sizeof(INPUT_STRUCT))
            self.bar.root.after(0, lambda: self.bar.show("Concluído ✓", "#66bb6a", 2000))
        else:
            self.bar.root.after(0, lambda: self.bar.show("Concluído ✓", "#66bb6a", 2000))

        self._notify_transcription(text)

    def _on_balloon_click(self):
        """Chamado quando o usuário clica na notificação balloon — copia texto + abre painel."""
        if not self._last_notif_text:
            return
        text = self._last_notif_text
        def _do():
            try:
                self.bar.root.clipboard_clear()
                self.bar.root.clipboard_append(text)
            except Exception:
                pass
            self.main_panel.open()
        self.bar.root.after(0, _do)

    def _notify_transcription(self, text):
        """Notificação toast após transcrição (respeitando preferências do usuário)."""
        self._last_notif_text = text
        if self.config.notification and not self.config.is_silenced():
            toast_preview = text[:80] + ("..." if len(text) > 80 else "")
            self.tray.show_toast("Voice AI — Transcrito", toast_preview,
                                 sound=self.config.notification_sound)

    def _esc_transcribe_monitor(self, cancelled_flag):
        """Monitora ESC durante a chamada da API. Cancela a transcrição desta thread."""
        VK_ESCAPE = 0x1B
        # Descarta ESC que já estava pressionado antes de entrar na transcrição
        while cancelled_flag[0] and user32.GetAsyncKeyState(VK_ESCAPE) & 0x8000:
            time.sleep(0.05)
        was_down = False
        while cancelled_flag[0]:  # para quando a própria transcrição terminar
            is_down = bool(user32.GetAsyncKeyState(VK_ESCAPE) & 0x8000)
            if is_down and not was_down:
                cancelled_flag[0] = False  # cancela apenas esta transcrição (sem race condition)
                self.usage.log_event("transcription_cancelled")
                self.bar.root.after(0, lambda: self.bar.show(
                    "Cancelado", "#ffaa00", 2000))
                break
            was_down = is_down
            time.sleep(0.05)

    def _on_open_panel(self):
        # v4.1.1: clique na StatusBar em estado "result" copia último texto para clipboard
        if self.bar._state == "result" and self._last_notif_text:
            try:
                self.bar.root.clipboard_clear()
                self.bar.root.clipboard_append(self._last_notif_text)
                self.bar.show("Copiado \u2713", "#66bb6a", 1200)
                log("StatusBar click: texto copiado para clipboard")
                return   # não abre painel — clique = copiar
            except Exception as e:
                log(f"StatusBar click copy error: {e}")
        self.main_panel.toggle()

    def _on_clear_history(self):
        self.history.clear()
        log("History cleared")

    def _on_settings(self):
        if self._settings_open:
            return
        self._settings_open = True
        SettingsDialog(self.config, on_save=self._on_settings_saved, first_run=False,
                       usage=self.usage)
        self._settings_open = False

    def _on_settings_saved(self):
        # Aplica API key, idioma e modelo ao vivo
        self.transcriber.client = OpenAI(
            api_key=self.config.api_key,
            timeout=httpx.Timeout(30.0, connect=10.0),
        )
        self.transcriber.language = self.config.language
        self.transcriber.model = self.config.model

        # Aplica microfone ao vivo
        self.recorder.device = self.config.mic_device

        # Atualiza modo de gravação no tray
        self.tray._recording_mode = self.config.recording_mode

        # Auto-start: cria ou remove atalho
        if self.config.data.get("auto_start", True):
            create_startup_shortcut()
        else:
            remove_startup_shortcut()

        # Aplica tema ao vivo
        global _current_theme
        new_theme = self.config.theme
        if new_theme != _current_theme:
            _current_theme = new_theme
            self.bar.apply_theme()
            log(f"Theme changed to: {new_theme}")

        # Show/hide statusbar (v3.4.0)
        self.bar.set_enabled(self.config.data.get("show_statusbar", True))

        # Troca hotkey e/ou modo ao vivo se mudou
        hotkey_changed = self.config.hotkey != self._active_hotkey
        mode_changed = self.config.recording_mode != self.hotkey_mgr._recording_mode
        if hotkey_changed or mode_changed:
            old = self._active_hotkey
            self.hotkey_mgr.change(self.config.hotkey, new_mode=self.config.recording_mode)
            self._active_hotkey = self.config.hotkey
            log(f"Hotkey/mode changed live: {old} → {self._active_hotkey}, mode={self.config.recording_mode}")

        log("Settings updated (live)")
        self.bar.show("\u2713  Configura\u00e7\u00f5es salvas!", "#66bb6a", 2000)

    def _on_hotkey_conflict(self, hotkey_str):
        """Chamado pela thread do HotkeyManager se RegisterHotKey falhar (conflito)."""
        msg = f"\u26a0\ufe0f Conflito: {hotkey_str} j\u00e1 est\u00e1 em uso por outro app."
        self.bar.root.after(0, lambda: self.bar.show(msg, "#ffaa00", 6000))

    def _on_exit(self):
        log("User exit")
        self.usage.log_event("app_stop")
        self.tray.stop()
        self.bar.quit()


# ─── Uninstall ────────────────────────────────────────────────

def _run_uninstall(quiet=False):
    """Desinstala o Voice AI completamente."""
    import winreg
    import subprocess  # v3.3.2: fix NameError ao agendar auto-deleção

    log("=== Voice AI UNINSTALL ===")

    if not quiet:
        root = tk.Tk()
        root.withdraw()
        answer = messagebox.askyesno(
            "Voice AI — Desinstalar",
            "Deseja desinstalar o Voice AI?\n\n"
            "Isso vai remover:\n"
            "• O aplicativo Voice AI\n"
            "• Atalhos (Desktop, Menu Iniciar, Startup)\n"
            "• Registro do Windows\n\n"
            "Seu histórico e configurações serão mantidos.",
        )
        root.destroy()
        if not answer:
            return

    # 0. Matar processos em execução (VoiceAI e VoiceAgent), exceto o próprio
    my_pid = os.getpid()
    try:
        subprocess.run(
            ["taskkill", "/F", "/IM", "VoiceAI.exe", "/FI", f"PID ne {my_pid}"],
            capture_output=True, creationflags=0x08000000,
        )
        subprocess.run(
            ["taskkill", "/F", "/IM", "VoiceAgent.exe"],
            capture_output=True, creationflags=0x08000000,
        )
        log(f"Running processes killed (excluded PID {my_pid})")
    except Exception as e:
        log(f"Process kill failed: {e}")

    # 1. Remove registry key
    try:
        winreg.DeleteKey(
            winreg.HKEY_CURRENT_USER,
            r"Software\Microsoft\Windows\CurrentVersion\Uninstall\VoiceAI"
        )
        log("Registry key removed")
    except FileNotFoundError:
        pass
    except Exception as e:
        log(f"Registry removal failed: {e}")

    # 2. Remove Start Menu shortcuts (Voice AI + Voice Agent)
    try:
        sm_dir = os.path.join(
            os.environ.get("APPDATA", ""),
            "Microsoft", "Windows", "Start Menu", "Programs"
        )
        for lnk_name in ["Voice AI.lnk", "Voice Agent.lnk"]:
            sm_path = os.path.join(sm_dir, lnk_name)
            if os.path.exists(sm_path):
                os.remove(sm_path)
                log(f"Start Menu shortcut removed: {lnk_name}")
    except Exception as e:
        log(f"Start Menu removal failed: {e}")

    # 3. Remove Desktop shortcut
    try:
        desktop_path = os.path.join(
            os.environ.get("USERPROFILE", ""), "Desktop", "Voice AI.lnk"
        )
        if os.path.exists(desktop_path):
            os.remove(desktop_path)
            log(f"Desktop shortcut removed")
    except Exception as e:
        log(f"Desktop removal failed: {e}")

    # 4. Remove Startup shortcut
    try:
        startup_path = os.path.join(
            os.environ.get("APPDATA", ""),
            "Microsoft", "Windows", "Start Menu", "Programs", "Startup", "Voice AI.lnk"
        )
        if os.path.exists(startup_path):
            os.remove(startup_path)
            log(f"Startup shortcut removed")
    except Exception as e:
        log(f"Startup removal failed: {e}")

    # 5. Schedule self-deletion (exe não pode deletar a si mesmo enquanto roda)
    files_to_delete = []
    for fname in ["VoiceAI.exe", "VoiceAgent.exe", "whisper.log", "usage.json"]:
        fpath = os.path.join(script_dir, fname)
        if os.path.exists(fpath):
            files_to_delete.append(fpath)

    if files_to_delete:
        del_cmds = " & ".join([f'del /f /q "{f}"' for f in files_to_delete])
        batch_cmd = (
            f'ping -n 3 127.0.0.1 > nul & '
            f'{del_cmds} & '
            f'rmdir /q "{script_dir}" 2>nul'
        )
        subprocess.Popen(
            ["cmd", "/c", batch_cmd],
            creationflags=0x08000000,  # CREATE_NO_WINDOW
            close_fds=True,
        )
        log("Self-deletion scheduled")

    if not quiet:
        root = tk.Tk()
        root.withdraw()
        messagebox.showinfo(
            "Voice AI — Desinstalado",
            "O Voice AI foi desinstalado com sucesso.\n\n"
            "Seu histórico de transcrições foi preservado."
        )
        root.destroy()

    log("=== Voice AI UNINSTALL complete ===")
    sys.exit(0)


# ─── Main ────────────────────────────────────────────────────

def main():
    log("=== Voice AI starting ===")

    # Uninstall mode
    if "--uninstall" in sys.argv:
        _run_uninstall(quiet="--quiet" in sys.argv)
        return

    print("[1] Iniciando...")

    # Instância única
    instance = SingleInstance()
    if instance.is_running():
        log("Another instance already running. Exiting.")
        print("Ja rodando! Saindo.")
        sys.exit(0)
    print("[2] Instancia unica OK")
    log("SingleInstance OK")

    # Carregar config
    config = ConfigManager()
    print("[3] Config carregado")

    # Flag --settings: forçar tela de config mesmo com API key já salva
    force_settings = "--settings" in sys.argv
    # Flag --open-panel: abrir MainPanel automaticamente ao iniciar (usado pelo installer)
    force_open_panel = "--open-panel" in sys.argv

    # First-run OU --settings: tela de configuração
    if config.needs_setup() or force_settings:
        log("Opening setup dialog (needs_setup=%s, --settings=%s)" % (config.needs_setup(), force_settings))
        print("[3a] Abrindo tela de configuracao...")
        dialog = SettingsDialog(config, first_run=True)
        saved = dialog.run()
        if not saved and not force_settings:
            log("Setup cancelled")
            sys.exit(0)
        config.load()  # recarregar após salvar

    if not config.api_key:
        log("OPENAI_API_KEY not configured")
        print("ERRO: API Key nao configurada!")
        sys.exit(1)
    print("[4] API key OK")
    log(f"API key OK, hotkey={config.hotkey}, lang={config.language}")

    # Atalho no Menu Iniciar (só para .exe)
    create_start_menu_shortcut()

    # Auto-start com Windows (configurável)
    if config.auto_start:
        create_startup_shortcut()
    else:
        remove_startup_shortcut()

    # Verifica microfone
    try:
        devices = sd.query_devices()
        has_input = any(d.get("max_input_channels", 0) > 0
                        for d in devices) if isinstance(devices, list) else True
        if not has_input:
            log("WARNING: No input device detected")
            root = tk.Tk()
            root.withdraw()
            messagebox.showwarning(
                "Voice AI",
                "Nenhum microfone detectado!\n\n"
                "Conecte um microfone e reinicie o Voice AI.")
            root.destroy()
    except Exception as e:
        log(f"Mic check error: {e}")

    # App
    app = App(config)
    print("[5] App criado")
    log("App created")

    app.run(open_panel=force_open_panel)

    # Cleanup
    instance.release()
    log("=== Voice AI exited ===")


if __name__ == "__main__":
    try:
        main()
    except Exception as e:
        log(f"FATAL: {e}")
        import traceback
        log(traceback.format_exc())
        print(f"ERRO: {e}")
        print(traceback.format_exc())
        input("Pressione Enter para sair...")
