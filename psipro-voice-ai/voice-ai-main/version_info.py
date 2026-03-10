"""
Gera arquivos de version_info para PyInstaller + version_cache.ini para Inno Setup.
Lê a VERSION de whisper_transcriber.py e gera os .txt automaticamente.
Executar antes do build: python version_info.py
"""
import re

with open("whisper_transcriber.py", "r", encoding="utf-8") as f:
    content = f.read()

match = re.search(r'^VERSION\s*=\s*"([^"]+)"', content, re.MULTILINE)
if not match:
    raise RuntimeError("VERSION not found in whisper_transcriber.py")

version = match.group(1)
parts = version.split(".")
major, minor, patch = int(parts[0]), int(parts[1]), int(parts[2])

TEMPLATE = """# UTF-8
VSVersionInfo(
  ffi=FixedFileInfo(
    filevers=({major}, {minor}, {patch}, 0),
    prodvers=({major}, {minor}, {patch}, 0),
    mask=0x3f,
    flags=0x0,
    OS=0x40004,
    fileType=0x1,
    subtype=0x0,
    date=(0, 0)
  ),
  kids=[
    StringFileInfo([
      StringTable(
        u'040904B0',
        [StringStruct(u'CompanyName', u'Expert Integrado'),
         StringStruct(u'FileDescription', u'{description}'),
         StringStruct(u'FileVersion', u'{version}'),
         StringStruct(u'InternalName', u'{internal_name}'),
         StringStruct(u'LegalCopyright', u'Copyright (C) 2026 Expert Integrado'),
         StringStruct(u'OriginalFilename', u'{original_filename}'),
         StringStruct(u'ProductName', u'Voice AI'),
         StringStruct(u'ProductVersion', u'{version}')])
    ]),
    VarFileInfo([VarStruct(u'Translation', [0x0409, 1200])])
  ]
)
"""

configs = [
    {
        "filename": "version_voiceai.txt",
        "description": "Voice AI - Transcritor de audio por voz",
        "internal_name": "VoiceAI",
        "original_filename": "VoiceAI.exe",
    },
    {
        "filename": "version_voiceagent.txt",
        "description": "Voice Agent - Assistente de voz com IA",
        "internal_name": "VoiceAgent",
        "original_filename": "VoiceAgent.exe",
    },
]

for cfg in configs:
    out = TEMPLATE.format(
        major=major, minor=minor, patch=patch,
        version=version,
        description=cfg["description"],
        internal_name=cfg["internal_name"],
        original_filename=cfg["original_filename"],
    )
    with open(cfg["filename"], "w", encoding="utf-8") as f:
        f.write(out)
    print(f"  Generated {cfg['filename']} (v{version})")

# version_cache.ini para Inno Setup
with open("version_cache.ini", "w", encoding="utf-8") as f:
    f.write(f"[version]\nvalue={version}\n")
print(f"  Generated version_cache.ini (v{version})")
