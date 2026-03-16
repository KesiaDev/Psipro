# -*- mode: python ; coding: utf-8 -*-
import os, sys
from PyInstaller.utils.hooks import collect_data_files

silero_datas = collect_data_files('silero_vad')

# Conda env: DLLs ficam em Library/bin (PyInstaller nao encontra automaticamente)
_conda = os.path.dirname(sys.executable)
_lib_bin = os.path.join(_conda, 'Library', 'bin')

_conda_dlls = []
for dll in ['ffi.dll', 'liblzma.dll', 'libbz2.dll', 'libexpat.dll']:
    p = os.path.join(_lib_bin, dll)
    if os.path.exists(p):
        _conda_dlls.append((p, '.'))

a = Analysis(
    ['voice_agent.py'],
    pathex=[],
    binaries=_conda_dlls,
    datas=silero_datas,
    hiddenimports=['openai', 'groq', 'silero_vad', 'silero_vad.data', 'onnxruntime', 'numpy', 'sounddevice', 'httpx', 'httpcore'],
    hookspath=[],
    hooksconfig={},
    runtime_hooks=[],
    excludes=['torch', 'torchaudio', 'torchvision'],
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
    name='VoiceAgent',
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
    version='version_voiceagent.txt',
)
