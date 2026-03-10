# -*- mode: python ; coding: utf-8 -*-
import os, sys

# Conda env: DLLs ficam em Library/bin (PyInstaller nao encontra automaticamente)
_conda = os.path.dirname(sys.executable)
_lib_bin = os.path.join(_conda, 'Library', 'bin')

_conda_dlls = []
for dll in ['ffi.dll', 'tk86t.dll', 'tcl86t.dll', 'liblzma.dll', 'libbz2.dll', 'libexpat.dll']:
    p = os.path.join(_lib_bin, dll)
    if os.path.exists(p):
        _conda_dlls.append((p, '.'))

a = Analysis(
    ['whisper_transcriber.py'],
    pathex=[],
    binaries=_conda_dlls,
    datas=[],
    hiddenimports=['openai', 'numpy', 'sounddevice', 'pyperclip', 'pystray', 'PIL', 'tkinter', 'win32api', 'win32con', 'win32gui'],
    hookspath=[],
    hooksconfig={},
    runtime_hooks=[],
    excludes=['torch', 'torchaudio', 'torchvision', 'matplotlib', 'onnxruntime', 'silero_vad', 'groq'],
    noarchive=False,
    optimize=0,
)
pyz = PYZ(a.pure)

exe = EXE(
    pyz,
    a.scripts,
    a.binaries,
    a.datas,
    [],
    name='VoiceAI',
    debug=False,
    bootloader_ignore_signals=False,
    strip=False,
    upx=False,
    upx_exclude=[],
    runtime_tmpdir=None,
    console=False,
    disable_windowed_traceback=False,
    argv_emulation=False,
    target_arch=None,
    codesign_identity=None,
    entitlements_file=None,
    icon=['assets/icon.ico'],
    version='version_voiceai.txt',
)
